import { Component, OnDestroy, OnInit } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { Subscription, timer } from 'rxjs';
import { RendezVousEntity } from 'src/models/RendezVous';
import { MODES_PAIEMENT, ModePaiement, Paiement, PaiementService } from '../services/paiement.service';
import { RendezVousService } from '../services/rendez-vous.service';

type Column = 'prevu' | 'arrive' | 'en_consultation' | 'termine';

/** Today's patients: arrival, consultation, checkout. Refreshes every 30 seconds. */
@Component({
  selector: 'app-salle-attente',
  templateUrl: './salle-attente.component.html',
  styleUrls: ['./salle-attente.component.css']
})
export class SalleAttenteComponent implements OnInit, OnDestroy {
  readonly today = new Date();
  readonly modes = MODES_PAIEMENT;
  readonly columns: { key: Column; title: string; icon: string }[] = [
    { key: 'prevu', title: 'À venir', icon: 'schedule' },
    { key: 'arrive', title: 'En salle d’attente', icon: 'chair' },
    { key: 'en_consultation', title: 'En consultation', icon: 'medical_services' },
    { key: 'termine', title: 'Terminés', icon: 'task_alt' }
  ];

  appointments: RendezVousEntity[] = [];
  payments: { [rdvId: number]: Paiement } = {};
  loading = true;
  busyId: number | null = null;
  now = Date.now();

  // Checkout dialog
  checkout: RendezVousEntity | null = null;
  montant: number | null = null;
  mode: ModePaiement = 'especes';
  note = '';
  openReceipt = false;
  paying = false;
  payError = '';
  private tarif: number | null = null;

  private polling: Subscription | null = null;

  constructor(private rdvService: RendezVousService,
              private paiementService: PaiementService,
              private toastr: ToastrService) {
  }

  ngOnInit(): void {
    this.paiementService.getTarif().subscribe((res) => this.tarif = res ? res.tarif : null, () => this.tarif = null);
    this.polling = timer(0, 30000).subscribe(() => {
      this.now = Date.now();
      this.load();
    });
  }

  ngOnDestroy(): void {
    if (this.polling) {
      this.polling.unsubscribe();
    }
  }

  load(): void {
    const day = this.dateValue(new Date());
    this.rdvService.getAgenda().subscribe(
      (list) => {
        this.appointments = (list || [])
          .filter((rdv) => this.dateValue(new Date(rdv.dateOfApt)) === day)
          .sort((a, b) => new Date(a.dateOfApt).getTime() - new Date(b.dateOfApt).getTime());
        this.loading = false;
      },
      () => this.loading = false
    );
    this.paiementService.list(day, day).subscribe((list) => {
      const map: { [rdvId: number]: Paiement } = {};
      (list || []).forEach((p) => {
        if (p.rendezVousId) {
          map[p.rendezVousId] = p;
        }
      });
      this.payments = map;
    });
  }

  // ------------------------------------------------------------------ columns

  inColumn(column: Column): RendezVousEntity[] {
    return this.appointments.filter((rdv) => (rdv.statutVisite || 'prevu') === column);
  }

  get absents(): RendezVousEntity[] {
    return this.appointments.filter((rdv) => rdv.statutVisite === 'absent');
  }

  get totalToday(): number {
    return Object.keys(this.payments).reduce((sum, key) => sum + this.payments[+key].montant, 0);
  }

  patientName(rdv: RendezVousEntity): string {
    const prenom = rdv.patient ? rdv.patient.prenom : rdv.patientPrenom;
    const nom = rdv.patient ? rdv.patient.nom : rdv.patientNom;
    return [prenom, nom].filter((part) => !!part).join(' ') || rdv.patientEmail || 'Patient';
  }

  patientId(rdv: RendezVousEntity): number | null {
    return rdv.patient ? rdv.patient.id : null;
  }

  /** "Arrivé il y a 12 min" style waiting time. */
  waiting(rdv: RendezVousEntity): string {
    if (!rdv.heureArrivee) {
      return '';
    }
    const minutes = Math.max(0, Math.round((this.now - new Date(rdv.heureArrivee).getTime()) / 60000));
    return minutes < 1 ? 'à l’instant' : minutes < 60 ? `${minutes} min` : `${Math.floor(minutes / 60)} h ${minutes % 60} min`;
  }

  isLate(rdv: RendezVousEntity): boolean {
    return !rdv.statutVisite && new Date(rdv.dateOfApt).getTime() < this.now - 15 * 60000;
  }

  format(amount: number): string {
    return PaiementService.format(amount);
  }

  modeLabel(mode: string): string {
    return PaiementService.modeLabel(mode);
  }

  // ------------------------------------------------------------------ actions

  setStatut(rdv: RendezVousEntity, statut: string, message?: string): void {
    this.busyId = rdv.idapt;
    this.rdvService.setStatut(rdv.idapt, statut).subscribe(
      (updated) => {
        this.busyId = null;
        rdv.statutVisite = updated.statutVisite;
        rdv.heureArrivee = updated.heureArrivee;
        if (message) {
          this.toastr.success(message, this.patientName(rdv));
        }
      },
      () => {
        this.busyId = null;
        this.toastr.error('Le statut n’a pas pu être mis à jour.', 'Erreur');
      }
    );
  }

  openCheckout(rdv: RendezVousEntity): void {
    const existing = this.payments[rdv.idapt];
    this.checkout = rdv;
    this.montant = existing ? existing.montant : this.tarif;
    this.mode = existing ? existing.mode : 'especes';
    this.note = existing && existing.note ? existing.note : '';
    this.openReceipt = false;
    this.payError = '';
    this.paying = false;
  }

  closeCheckout(): void {
    this.checkout = null;
  }

  /** Visit finished without payment (free consultation, paid later…). */
  finishWithoutPayment(): void {
    const rdv = this.checkout;
    if (!rdv) {
      return;
    }
    this.checkout = null;
    this.setStatut(rdv, 'termine', 'Consultation terminée sans paiement.');
  }

  pay(): void {
    const rdv = this.checkout;
    if (!rdv || this.paying) {
      return;
    }
    const amount = Number(this.montant);
    if (this.montant === null || (this.montant as any) === '' || isNaN(amount) || amount < 0) {
      this.payError = 'Indiquez le montant reçu.';
      return;
    }
    // Open the receipt tab now (inside the click) so the browser does not block it.
    const tab = this.openReceipt ? window.open('', '_blank') : null;
    this.paying = true;
    this.paiementService.encaisser(rdv.idapt, amount, this.mode, this.note).subscribe(
      (payment) => {
        this.paying = false;
        this.checkout = null;
        this.payments = { ...this.payments, [rdv.idapt]: payment };
        rdv.statutVisite = 'termine';
        this.toastr.success(`${this.format(payment.montant)} encaissés (${this.modeLabel(payment.mode).toLowerCase()}).`, 'Paiement enregistré');
        if (tab) {
          this.paiementService.recu(payment.id).subscribe(
            (blob) => PaiementService.openPdf(tab, blob),
            () => tab.close()
          );
        }
      },
      () => {
        this.paying = false;
        if (tab) {
          tab.close();
        }
        this.payError = 'Le paiement n’a pas pu être enregistré. Réessayez dans un instant.';
      }
    );
  }

  receipt(rdv: RendezVousEntity): void {
    const payment = this.payments[rdv.idapt];
    if (!payment) {
      return;
    }
    const tab = window.open('', '_blank');
    this.paiementService.recu(payment.id).subscribe(
      (blob) => PaiementService.openPdf(tab, blob),
      () => {
        if (tab) {
          tab.close();
        }
        this.toastr.error('Le reçu n’a pas pu être ouvert.', 'Erreur');
      }
    );
  }

  private dateValue(date: Date): string {
    const pad = (n: number) => (n < 10 ? '0' + n : String(n));
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
  }
}
