import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { Patient } from 'src/models/Patient';
import { RendezVousEntity } from 'src/models/RendezVous';
import { TokenService } from '../../core/auth/token.service';
import { AjoutpatientComponent } from '../gestion-des-patients/ajoutpatient/ajoutpatient.component';
import { AccountService } from '../../core/services/account.service';
import { DemandesNotificationService } from '../../core/services/demandes-notification.service';
import { PatientService } from '../../core/services/patient.service';
import { RendezVousService } from '../../core/services/rendez-vous.service';

/** Home page for doctors and secretaries: what matters today at a glance. */
@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  readonly today = new Date();
  firstName = '';
  readonly isDoctor = this.tokenService.getUserRole() === 'Medecin';

  loading = true;
  appointments: RendezVousEntity[] = [];
  patients: Patient[] = [];
  requests: RendezVousEntity[] = [];

  private subscription: Subscription | null = null;

  constructor(private tokenService: TokenService,
              private accountService: AccountService,
              private rdvService: RendezVousService,
              private patientService: PatientService,
              private notifications: DemandesNotificationService,
              private dialog: MatDialog,
              private router: Router) {
  }

  ngOnInit(): void {
    const id = Number(this.tokenService.getId());
    if (id) {
      this.accountService.getById(id).subscribe((user) => this.firstName = (user && user.prenom) || '');
    }
    this.subscription = this.notifications.pending$.subscribe((list) => this.requests = list);
    this.notifications.refresh();
    this.load();
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  load(): void {
    this.loading = true;
    this.rdvService.getAgenda().subscribe(
      (list) => {
        this.appointments = (list || []).slice()
          .sort((a, b) => new Date(a.dateOfApt).getTime() - new Date(b.dateOfApt).getTime());
        this.loading = false;
      },
      () => this.loading = false
    );
    this.patientService.getAllpatient().subscribe((list) => this.patients = list || []);
  }

  get greeting(): string {
    const hour = new Date().getHours();
    const hello = hour < 18 ? 'Bonjour' : 'Bonsoir';
    if (!this.firstName) {
      return hello;
    }
    return this.isDoctor ? `${hello}, Dr ${this.firstName}` : `${hello}, ${this.firstName}`;
  }

  get todayAppointments(): RendezVousEntity[] {
    const start = this.startOfDay(new Date());
    const end = new Date(start);
    end.setDate(end.getDate() + 1);
    return this.between(start, end);
  }

  /** The next appointment today that has not started yet. */
  get nextToday(): RendezVousEntity | null {
    const now = Date.now();
    return this.todayAppointments.find((rdv) => new Date(rdv.dateOfApt).getTime() >= now) || null;
  }

  get upcoming(): RendezVousEntity[] {
    const tomorrow = this.startOfDay(new Date());
    tomorrow.setDate(tomorrow.getDate() + 1);
    return this.appointments.filter((rdv) => new Date(rdv.dateOfApt).getTime() >= tomorrow.getTime()).slice(0, 5);
  }

  get weekCount(): number {
    const start = this.startOfDay(new Date());
    start.setDate(start.getDate() - ((start.getDay() + 6) % 7));
    const end = new Date(start);
    end.setDate(end.getDate() + 7);
    return this.between(start, end).length;
  }

  get newPatientsThisMonth(): number {
    const now = new Date();
    return this.patients.filter((patient) => {
      if (!patient.creationDate) {
        return false;
      }
      const created = new Date(patient.creationDate);
      return created.getFullYear() === now.getFullYear() && created.getMonth() === now.getMonth();
    }).length;
  }

  isPast(rdv: RendezVousEntity): boolean {
    return new Date(rdv.dateOfApt).getTime() < Date.now();
  }

  patientName(rdv: RendezVousEntity): string {
    const prenom = rdv.patient ? rdv.patient.prenom : rdv.patientPrenom;
    const nom = rdv.patient ? rdv.patient.nom : rdv.patientNom;
    return [prenom, nom].filter((part) => !!part).join(' ') || rdv.patientEmail || 'Patient';
  }

  patientPhone(rdv: RendezVousEntity): string {
    return (rdv.patient && rdv.patient.numtelephone) || rdv.patientTelephone || '';
  }

  phoneLink(phone: string): string {
    return 'tel:' + String(phone || '').replace(/[^+0-9]/g, '');
  }

  newAppointment(): void {
    this.router.navigate(['/agenda'], { queryParams: { nouveau: 1 } });
  }

  newPatient(): void {
    this.dialog.open(AjoutpatientComponent, {
      width: '760px',
      maxWidth: '94vw',
      panelClass: 'pm-dialog',
      autoFocus: false,
      data: {}
    }).afterClosed().subscribe((saved) => {
      if (saved) {
        this.patientService.getAllpatient().subscribe((list) => this.patients = list || []);
      }
    });
  }

  private between(start: Date, end: Date): RendezVousEntity[] {
    return this.appointments.filter((rdv) => {
      const when = new Date(rdv.dateOfApt).getTime();
      return when >= start.getTime() && when < end.getTime();
    });
  }

  private startOfDay(date: Date): Date {
    return new Date(date.getFullYear(), date.getMonth(), date.getDate());
  }
}
