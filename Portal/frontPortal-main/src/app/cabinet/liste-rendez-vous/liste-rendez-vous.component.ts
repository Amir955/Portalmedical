import { Component, OnDestroy, OnInit } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { Subscription } from 'rxjs';
import { RendezVousEntity } from '../../../models/RendezVous';
import { TokenService } from '../../core/auth/token.service';
import { DemandesNotificationService } from '../../core/services/demandes-notification.service';
import { RendezVousService } from '../../core/services/rendez-vous.service';

type PendingAction = { kind: 'accept' | 'refuse'; rdv: RendezVousEntity };
type Period = 'all' | 'today' | 'week' | 'month' | 'past';

/** Pending appointment requests of the signed-in doctor (or of the secretary's doctor). */
@Component({
  selector: 'app-liste-rendez-vous',
  templateUrl: './liste-rendez-vous.component.html',
  styleUrls: ['./liste-rendez-vous.component.css']
})
export class ListeRendezVousComponent implements OnInit, OnDestroy {
  requests: RendezVousEntity[] = [];
  loading = true;

  readonly periods: { value: Period; label: string }[] = [
    { value: 'all', label: 'Toutes' },
    { value: 'today', label: 'Aujourd’hui' },
    { value: 'week', label: 'Cette semaine' },
    { value: 'month', label: 'Ce mois' },
    { value: 'past', label: 'Dépassées' }
  ];
  period: Period = 'all';
  search = '';

  pending: PendingAction | null = null;
  busyId: number | null = null;

  private subscription: Subscription | null = null;

  constructor(private rdvService: RendezVousService,
              private tokenService: TokenService,
              private notifications: DemandesNotificationService,
              private toaster: ToastrService) {
  }

  ngOnInit(): void {
    this.subscription = this.notifications.pending$.subscribe((list) => {
      this.requests = list;
    });
    this.load();
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  load(): void {
    this.loading = true;
    this.rdvService.getDemandes().subscribe(
      (list) => {
        this.requests = DemandesNotificationService.newestFirst(list);
        this.loading = false;
      },
      () => {
        this.loading = false;
        this.toaster.error('Les demandes n’ont pas pu être chargées.', 'Erreur');
      }
    );
  }

  /** Only show the doctor when the list mixes several doctors (older, unlinked accounts). */
  get showDoctor(): boolean {
    const ids = new Set(this.requests.filter((rdv) => !!rdv.user).map((rdv) => rdv.user.id));
    return ids.size > 1;
  }

  get filtered(): RendezVousEntity[] {
    const query = this.search.trim().toLowerCase();
    return this.requests.filter((rdv) => this.inPeriod(rdv, this.period)
      && (!query || [rdv.patientNom, rdv.patientPrenom, rdv.patientEmail, rdv.patientTelephone]
        .some((field) => (field || '').toLowerCase().indexOf(query) !== -1)));
  }

  count(period: Period): number {
    return this.requests.filter((rdv) => this.inPeriod(rdv, period)).length;
  }

  resetFilters(): void {
    this.period = 'all';
    this.search = '';
  }

  private inPeriod(rdv: RendezVousEntity, period: Period): boolean {
    if (period === 'all') {
      return true;
    }
    if (!rdv.dateOfApt) {
      return false;
    }
    const when = new Date(rdv.dateOfApt);
    const now = new Date();
    if (period === 'past') {
      return when.getTime() < now.getTime();
    }

    const start = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    let end: Date;
    if (period === 'today') {
      end = new Date(start.getFullYear(), start.getMonth(), start.getDate() + 1);
    } else if (period === 'week') {
      // Monday to Sunday of the current week.
      const fromMonday = (start.getDay() + 6) % 7;
      start.setDate(start.getDate() - fromMonday);
      end = new Date(start.getFullYear(), start.getMonth(), start.getDate() + 7);
    } else {
      start.setDate(1);
      end = new Date(start.getFullYear(), start.getMonth() + 1, 1);
    }
    return when.getTime() >= start.getTime() && when.getTime() < end.getTime();
  }

  patientName(rdv: RendezVousEntity): string {
    const name = [rdv.patientPrenom, rdv.patientNom].filter((part) => !!part).join(' ');
    return name || 'Patient non renseigné';
  }

  initials(rdv: RendezVousEntity): string {
    const first = (rdv.patientPrenom || rdv.patientEmail || '?').trim().charAt(0);
    const last = (rdv.patientNom || '').trim().charAt(0);
    return (first + last).toUpperCase();
  }

  age(rdv: RendezVousEntity): number | null {
    if (!rdv.patientDateNaissance) {
      return null;
    }
    const birth = new Date(rdv.patientDateNaissance);
    if (isNaN(birth.getTime())) {
      return null;
    }
    const now = new Date();
    let years = now.getFullYear() - birth.getFullYear();
    const beforeBirthday = now.getMonth() < birth.getMonth()
      || (now.getMonth() === birth.getMonth() && now.getDate() < birth.getDate());
    if (beforeBirthday) {
      years--;
    }
    return years >= 0 ? years : null;
  }

  phoneLink(phone: string): string {
    return 'tel:' + (phone || '').replace(/[^+0-9]/g, '');
  }

  isPast(rdv: RendezVousEntity): boolean {
    return !!rdv.dateOfApt && new Date(rdv.dateOfApt).getTime() < Date.now();
  }

  ask(kind: 'accept' | 'refuse', rdv: RendezVousEntity): void {
    this.pending = { kind, rdv };
  }

  cancelAction(): void {
    this.pending = null;
  }

  confirmAction(): void {
    if (!this.pending) {
      return;
    }
    const { kind, rdv } = this.pending;
    this.pending = null;
    this.busyId = rdv.idapt;

    const request = kind === 'accept' ? this.rdvService.confirmerRdv(rdv) : this.rdvService.refuserRdv(rdv);
    request.subscribe(
      () => {
        this.busyId = null;
        this.toaster.success(
          kind === 'accept'
            ? 'Le rendez-vous est confirmé et le patient a été prévenu par e-mail.'
            : 'La demande est refusée et le patient a été prévenu par e-mail.',
          kind === 'accept' ? 'Rendez-vous confirmé' : 'Demande refusée');
        this.requests = this.requests.filter((item) => item.idapt !== rdv.idapt);
        this.notifications.refresh();
      },
      (error) => {
        this.busyId = null;
        if (error && error.status === 409) {
          this.toaster.error('Le médecin a déjà un rendez-vous à cette heure-là.', 'Créneau déjà pris');
        } else {
          this.toaster.error('L’action n’a pas pu être enregistrée. Réessayez dans un instant.', 'Erreur');
        }
      }
    );
  }
}
