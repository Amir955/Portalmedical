import { Injectable, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { BehaviorSubject, Subscription, timer } from 'rxjs';
import { RendezVousEntity } from 'src/models/RendezVous';
import { TokenService } from '../Authentification/token.service';
import { RendezVousService } from './rendez-vous.service';

/**
 * Keeps the list of pending appointment requests for the signed-in user and polls the
 * server for new ones. The navbar bell shows the unread count; a toast announces new
 * requests. "Read" is remembered per user in this browser.
 */
@Injectable({
  providedIn: 'root'
})
export class DemandesNotificationService implements OnDestroy {
  private static readonly POLL_MS = 60000;

  /** Pending requests, most recently received first. */
  private readonly pendingSubject = new BehaviorSubject<RendezVousEntity[]>([]);
  readonly pending$ = this.pendingSubject.asObservable();

  /** Ids of the requests this user has already seen. */
  private readonly readSubject = new BehaviorSubject<Set<number>>(new Set());
  readonly read$ = this.readSubject.asObservable();

  /** Appointments cancelled by patients (last 7 days), newest first. */
  private readonly cancelledSubject = new BehaviorSubject<RendezVousEntity[]>([]);
  readonly cancelled$ = this.cancelledSubject.asObservable();

  /** Ids of the cancellations this user has already seen. */
  private readonly cancelReadSubject = new BehaviorSubject<Set<number>>(new Set());
  readonly cancelRead$ = this.cancelReadSubject.asObservable();

  private polling: Subscription | null = null;
  private knownIds: Set<number> | null = null;
  private knownCancelIds: Set<number> | null = null;

  constructor(private rdvService: RendezVousService,
              private tokenService: TokenService,
              private toastr: ToastrService,
              private router: Router) {
  }

  /** Most recently received first (ids grow with each new request). */
  static newestFirst(list: RendezVousEntity[]): RendezVousEntity[] {
    return (list || []).slice().sort((a, b) => b.idapt - a.idapt);
  }

  /** Starts polling (once); safe to call from every page that shows the navbar. */
  start(): void {
    if (this.polling) {
      return;
    }
    this.readSubject.next(this.loadRead());
    this.cancelReadSubject.next(this.loadIds(this.cancelStorageKey()));
    this.polling = timer(0, DemandesNotificationService.POLL_MS).subscribe(() => this.refresh());
  }

  stop(): void {
    if (this.polling) {
      this.polling.unsubscribe();
      this.polling = null;
    }
    this.knownIds = null;
    this.knownCancelIds = null;
    this.pendingSubject.next([]);
    this.readSubject.next(new Set());
    this.cancelledSubject.next([]);
    this.cancelReadSubject.next(new Set());
  }

  refresh(): void {
    if (!this.tokenService.loggedIn()) {
      this.stop();
      return;
    }

    this.rdvService.getDemandes().subscribe(
      (requests) => {
        const list = DemandesNotificationService.newestFirst(requests);
        this.announceNew(list);
        this.pendingSubject.next(list);
        this.pruneRead(list);
      },
      () => { /* keep the last known list; the next poll retries */ }
    );

    this.rdvService.getAnnulations().subscribe(
      (list) => {
        const cancelled = list || [];
        this.announceCancelled(cancelled);
        this.cancelledSubject.next(cancelled);
        const ids = new Set(cancelled.map((rdv) => rdv.idapt));
        const current = this.cancelReadSubject.value;
        const kept = new Set(Array.from(current).filter((id) => ids.has(id)));
        if (kept.size !== current.size) {
          this.saveCancelRead(kept);
        }
      },
      () => { /* keep the last known list */ }
    );
  }

  isCancelRead(rdv: RendezVousEntity): boolean {
    return this.cancelReadSubject.value.has(rdv.idapt);
  }

  markCancelRead(rdv: RendezVousEntity): void {
    if (this.isCancelRead(rdv)) {
      return;
    }
    const next = new Set(this.cancelReadSubject.value);
    next.add(rdv.idapt);
    this.saveCancelRead(next);
  }

  private announceCancelled(list: RendezVousEntity[]): void {
    const ids = new Set(list.map((rdv) => rdv.idapt));
    if (this.knownCancelIds) {
      const fresh = list.filter((rdv) => !this.knownCancelIds.has(rdv.idapt));
      fresh.slice(0, 3).forEach((rdv) => {
        const who = DemandesNotificationService.patientName(rdv);
        const when = new Date(rdv.dateOfApt).toLocaleString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', hour: '2-digit', minute: '2-digit' });
        this.toastr.warning(`${who} a annulé son rendez-vous du ${when}. Le créneau est de nouveau libre.`, 'Rendez-vous annulé', { timeOut: 10000 })
          .onTap.subscribe(() => this.markCancelRead(rdv));
      });
    }
    this.knownCancelIds = ids;
  }

  static patientName(rdv: RendezVousEntity): string {
    const prenom = rdv.patient ? rdv.patient.prenom : rdv.patientPrenom;
    const nom = rdv.patient ? rdv.patient.nom : rdv.patientNom;
    return [prenom, nom].filter((part) => !!part).join(' ') || rdv.patientEmail || 'Un patient';
  }

  private cancelStorageKey(): string {
    return 'pm-annulations-lues-' + (this.tokenService.getId() || 'anon');
  }

  private loadIds(key: string): Set<number> {
    try {
      const raw = localStorage.getItem(key);
      const ids = raw ? JSON.parse(raw) : [];
      return new Set(Array.isArray(ids) ? ids.map(Number) : []);
    } catch (e) {
      return new Set();
    }
  }

  private saveCancelRead(ids: Set<number>): void {
    this.cancelReadSubject.next(ids);
    try {
      localStorage.setItem(this.cancelStorageKey(), JSON.stringify(Array.from(ids)));
    } catch (e) {
      // Storage unavailable: read marks last for this session only.
    }
  }

  isRead(rdv: RendezVousEntity): boolean {
    return this.readSubject.value.has(rdv.idapt);
  }

  markRead(rdv: RendezVousEntity): void {
    if (this.isRead(rdv)) {
      return;
    }
    const next = new Set(this.readSubject.value);
    next.add(rdv.idapt);
    this.saveRead(next);
  }

  markAllRead(): void {
    const next = new Set(this.readSubject.value);
    this.pendingSubject.value.forEach((rdv) => next.add(rdv.idapt));
    this.saveRead(next);
    const cancelled = new Set(this.cancelReadSubject.value);
    this.cancelledSubject.value.forEach((rdv) => cancelled.add(rdv.idapt));
    this.saveCancelRead(cancelled);
  }

  ngOnDestroy(): void {
    this.stop();
  }

  private announceNew(list: RendezVousEntity[]): void {
    const ids = new Set(list.map((rdv) => rdv.idapt));

    // First load of the session: remember what exists without announcing it.
    if (this.knownIds) {
      const fresh = list.filter((rdv) => !this.knownIds.has(rdv.idapt));
      if (fresh.length === 1) {
        const rdv = fresh[0];
        const who = [rdv.patientPrenom, rdv.patientNom].filter((part) => !!part).join(' ') || rdv.patientEmail;
        this.toastr.info(`${who} demande un rendez-vous.`, 'Nouvelle demande', { timeOut: 8000 })
          .onTap.subscribe(() => this.router.navigateByUrl('/listeRDV'));
      } else if (fresh.length > 1) {
        this.toastr.info(`${fresh.length} nouvelles demandes de rendez-vous.`, 'Nouvelles demandes', { timeOut: 8000 })
          .onTap.subscribe(() => this.router.navigateByUrl('/listeRDV'));
      }
    }

    this.knownIds = ids;
  }

  /** Forgets read marks of requests that are no longer pending, so storage stays small. */
  private pruneRead(list: RendezVousEntity[]): void {
    const pendingIds = new Set(list.map((rdv) => rdv.idapt));
    const current = this.readSubject.value;
    const kept = new Set(Array.from(current).filter((id) => pendingIds.has(id)));
    if (kept.size !== current.size) {
      this.saveRead(kept);
    }
  }

  private storageKey(): string {
    return 'pm-demandes-lues-' + (this.tokenService.getId() || 'anon');
  }

  private loadRead(): Set<number> {
    try {
      const raw = localStorage.getItem(this.storageKey());
      const ids = raw ? JSON.parse(raw) : [];
      return new Set(Array.isArray(ids) ? ids.map(Number) : []);
    } catch (e) {
      return new Set();
    }
  }

  private saveRead(ids: Set<number>): void {
    this.readSubject.next(ids);
    try {
      localStorage.setItem(this.storageKey(), JSON.stringify(Array.from(ids)));
    } catch (e) {
      // Storage unavailable (private mode…): read marks last for this session only.
    }
  }
}
