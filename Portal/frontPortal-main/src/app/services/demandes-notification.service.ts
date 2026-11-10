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

  private polling: Subscription | null = null;
  private knownIds: Set<number> | null = null;

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
    this.polling = timer(0, DemandesNotificationService.POLL_MS).subscribe(() => this.refresh());
  }

  stop(): void {
    if (this.polling) {
      this.polling.unsubscribe();
      this.polling = null;
    }
    this.knownIds = null;
    this.pendingSubject.next([]);
    this.readSubject.next(new Set());
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
