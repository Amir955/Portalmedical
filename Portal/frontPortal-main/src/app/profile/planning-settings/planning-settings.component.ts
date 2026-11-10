import { Component, OnInit } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { Conge, Planning, PlanningJour, PlanningService } from '../../services/planning.service';

/** "Horaires du cabinet": weekly opening hours, slot length and days off. */
@Component({
  selector: 'app-planning-settings',
  templateUrl: './planning-settings.component.html',
  styleUrls: ['./planning-settings.component.css']
})
export class PlanningSettingsComponent implements OnInit {
  readonly dayNames = ['Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi', 'Dimanche'];
  readonly durations = [10, 15, 20, 30, 45, 60];
  readonly today = new Date().toISOString().slice(0, 10);

  loading = true;
  unavailable = '';
  saving = false;
  formError = '';

  dureeCreneau = 30;
  jours: PlanningJour[] = [];
  conges: Conge[] = [];

  newConge: Conge = { dateDebut: '', dateFin: '', motif: '' };
  addingConge = false;
  congeError = '';

  constructor(private planningService: PlanningService,
              private toastr: ToastrService) {
  }

  ngOnInit(): void {
    this.planningService.getMine().subscribe(
      (planning) => {
        this.apply(planning);
        this.loading = false;
      },
      (error) => {
        this.loading = false;
        this.unavailable = error && error.status === 400
          ? 'Votre compte n’est rattaché à aucun médecin : demandez à votre médecin de vous ajouter depuis « Mon secrétariat ».'
          : 'Les horaires n’ont pas pu être chargés. Rechargez la page.';
      }
    );
  }

  dayName(jour: PlanningJour): string {
    return this.dayNames[jour.jour - 1];
  }

  setDuration(minutes: number): void {
    this.dureeCreneau = minutes;
    this.formError = '';
  }

  toggleDay(jour: PlanningJour): void {
    jour.actif = !jour.actif;
    if (jour.actif && !jour.debutMatin && !jour.debutApresMidi) {
      jour.debutMatin = '08:30';
      jour.finMatin = '12:30';
      jour.debutApresMidi = '14:00';
      jour.finApresMidi = '17:30';
    }
    this.formError = '';
  }

  /** Same hours as Monday for every open day. */
  copyMonday(): void {
    const monday = this.jours[0];
    this.jours.slice(1).forEach((jour) => {
      if (jour.actif) {
        jour.debutMatin = monday.debutMatin;
        jour.finMatin = monday.finMatin;
        jour.debutApresMidi = monday.debutApresMidi;
        jour.finApresMidi = monday.finApresMidi;
      }
    });
  }

  /** "08:30 – 12:30 · 14:00 – 17:30" summary for a day. */
  summary(jour: PlanningJour): string {
    if (!jour.actif) {
      return 'Fermé';
    }
    const parts = [];
    if (jour.debutMatin && jour.finMatin) {
      parts.push(`${jour.debutMatin} – ${jour.finMatin}`);
    }
    if (jour.debutApresMidi && jour.finApresMidi) {
      parts.push(`${jour.debutApresMidi} – ${jour.finApresMidi}`);
    }
    return parts.join(' · ') || 'Horaires à compléter';
  }

  save(): void {
    const problem = this.validate();
    if (problem) {
      this.formError = problem;
      return;
    }
    this.formError = '';
    this.saving = true;
    this.planningService.saveMine({ dureeCreneau: this.dureeCreneau, jours: this.jours, conges: [] }).subscribe(
      (planning) => {
        this.saving = false;
        this.apply(planning);
        this.toastr.success('Le calendrier et la prise de rendez-vous en ligne suivent maintenant ces horaires.', 'Horaires enregistrés');
      },
      () => {
        this.saving = false;
        this.formError = 'Les horaires n’ont pas pu être enregistrés. Vérifiez les heures saisies.';
      }
    );
  }

  addConge(): void {
    if (!this.newConge.dateDebut || !this.newConge.dateFin) {
      this.congeError = 'Choisissez la date de début et la date de fin.';
      return;
    }
    if (this.newConge.dateFin < this.newConge.dateDebut) {
      this.congeError = 'La date de fin est avant la date de début.';
      return;
    }
    this.congeError = '';
    this.addingConge = true;
    this.planningService.addConge(this.newConge).subscribe(
      (conge) => {
        this.addingConge = false;
        this.conges = [...this.conges, conge].sort((a, b) => a.dateDebut.localeCompare(b.dateDebut));
        this.newConge = { dateDebut: '', dateFin: '', motif: '' };
        this.toastr.success('Aucun rendez-vous ne pourra être pris pendant cette période.', 'Congé ajouté');
      },
      () => {
        this.addingConge = false;
        this.congeError = 'Le congé n’a pas pu être ajouté.';
      }
    );
  }

  removeConge(conge: Conge): void {
    this.planningService.deleteConge(conge.id).subscribe(
      () => {
        this.conges = this.conges.filter((item) => item.id !== conge.id);
        this.toastr.success('Les rendez-vous sont de nouveau possibles sur cette période.', 'Congé supprimé');
      },
      () => this.toastr.error('Le congé n’a pas pu être supprimé.', 'Erreur')
    );
  }

  /** Days count of a period, both ends included. */
  congeDays(conge: Conge): number {
    const start = new Date(conge.dateDebut + 'T00:00:00').getTime();
    const end = new Date(conge.dateFin + 'T00:00:00').getTime();
    return Math.round((end - start) / 86400000) + 1;
  }

  private apply(planning: Planning): void {
    this.dureeCreneau = planning.dureeCreneau;
    this.jours = planning.jours.map((jour) => ({ ...jour }));
    this.conges = planning.conges || [];
  }

  private validate(): string {
    for (const jour of this.jours) {
      if (!jour.actif) {
        continue;
      }
      const ranges: [string | null, string | null, string][] = [
        [jour.debutMatin, jour.finMatin, 'le matin'],
        [jour.debutApresMidi, jour.finApresMidi, 'l’après-midi']
      ];
      let hasRange = false;
      for (const [start, end, label] of ranges) {
        if (!start && !end) {
          continue;
        }
        if (!start || !end) {
          return `${this.dayName(jour)} : indiquez le début et la fin pour ${label}.`;
        }
        if (start >= end) {
          return `${this.dayName(jour)} : l’heure de début doit précéder l’heure de fin (${label}).`;
        }
        hasRange = true;
      }
      if (!hasRange) {
        return `${this.dayName(jour)} est ouvert : indiquez au moins des horaires le matin ou l’après-midi.`;
      }
      if (jour.finMatin && jour.debutApresMidi && jour.finMatin > jour.debutApresMidi) {
        return `${this.dayName(jour)} : l’après-midi commence avant la fin de la matinée.`;
      }
    }
    return '';
  }
}
