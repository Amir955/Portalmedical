import { Component, Input, OnChanges } from '@angular/core';
import { Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { UserEntity } from '../../../../../models/userEntity';
import { PlanningService } from '../../../../core/services/planning.service';
import { RendezVousService } from '../../../../core/services/rendez-vous.service';
import { doctorLocation, doctorSpecialty } from '../../landing.data';

interface DoctorCard {
  doctor: UserEntity;
  /** yyyy-MM-dd and HH:mm of the next free slot, once found. */
  date?: string;
  time?: string;
  loading: boolean;
}

/** A few doctors with their next free slot; "Réserver" opens the booking page on that day. */
@Component({
  selector: 'app-landing-doctors',
  templateUrl: './landing-doctors.component.html',
  styleUrls: ['../../landing-shared.css', './landing-doctors.component.css']
})
export class LandingDoctorsComponent implements OnChanges {
  @Input() doctors: UserEntity[] = [];

  private static readonly SHOWN = 4;
  cards: DoctorCard[] = [];
  readonly specialty = doctorSpecialty;
  readonly location = doctorLocation;

  constructor(private planningService: PlanningService,
              private rdvService: RendezVousService,
              private router: Router) {
  }

  ngOnChanges(): void {
    this.cards = (this.doctors || []).slice(0, LandingDoctorsComponent.SHOWN).map((doctor) => ({ doctor, loading: true }));
    this.cards.forEach((card) => this.findNextSlot(card));
  }

  book(card: DoctorCard): void {
    const queryParams: { [key: string]: any } = { medecin: card.doctor.id };
    if (card.date) {
      queryParams.date = card.date;
    }
    this.router.navigate(['/prendre-rendez-vous'], { queryParams });
  }

  name(doctor: UserEntity): string {
    return 'Dr ' + [doctor.prenom, doctor.nom].filter((part) => !!part).join(' ');
  }

  initials(doctor: UserEntity): string {
    return ((doctor.prenom || '').charAt(0) + (doctor.nom || '').charAt(0)).toUpperCase() || 'Dr';
  }

  /** "Aujourd’hui à 14:30", "Demain à 09:00", "Ven. 13 nov. à 08:30". */
  slotLabel(card: DoctorCard): string {
    if (!card.date || !card.time) {
      return '';
    }
    const day = new Date(card.date + 'T00:00:00');
    const diff = Math.round((day.getTime() - this.startOfDay(new Date()).getTime()) / 86400000);
    const label = diff === 0 ? 'Aujourd’hui' : diff === 1 ? 'Demain'
      : day.toLocaleDateString('fr-FR', { weekday: 'short', day: 'numeric', month: 'short' });
    return `${label.charAt(0).toUpperCase() + label.slice(1)} à ${card.time}`;
  }

  /** Looks at the doctor's next 6 open days for the first free slot. */
  private findNextSlot(card: DoctorCard): void {
    this.planningService.getCalendrier(card.doctor.id).subscribe(
      (calendrier) => {
        const days: string[] = [];
        const day = this.startOfDay(new Date());
        for (let i = 0; i < 60 && days.length < 6; i++) {
          const value = this.dateValue(day);
          const weekday = day.getDay() === 0 ? 7 : day.getDay();
          const off = calendrier.conges.some((c) => c.dateDebut <= value && value <= c.dateFin);
          if (!off && calendrier.joursOuverts.indexOf(weekday) !== -1) {
            days.push(value);
          }
          day.setDate(day.getDate() + 1);
        }
        this.tryDays(card, days);
      },
      () => card.loading = false
    );
  }

  private tryDays(card: DoctorCard, days: string[]): void {
    if (!days.length) {
      card.loading = false;
      return;
    }
    const date = days[0];
    forkJoin([
      this.planningService.getCreneaux(card.doctor.id, date).pipe(catchError(() => of(null))),
      this.rdvService.getTakenSlots(card.doctor.id, date).pipe(catchError(() => of([] as string[])))
    ]).subscribe(([creneaux, taken]) => {
      const now = new Date();
      const nowTime = `${this.pad(now.getHours())}:${this.pad(now.getMinutes())}`;
      const isToday = date === this.dateValue(now);
      const free = (creneaux ? creneaux.slots : [])
        .filter((slot) => (taken || []).indexOf(slot) === -1)
        .filter((slot) => !isToday || slot > nowTime);
      if (free.length) {
        card.date = date;
        card.time = free[0];
        card.loading = false;
      } else {
        this.tryDays(card, days.slice(1));
      }
    });
  }

  private startOfDay(date: Date): Date {
    return new Date(date.getFullYear(), date.getMonth(), date.getDate());
  }

  private pad(n: number): string {
    return n < 10 ? '0' + n : String(n);
  }

  private dateValue(date: Date): string {
    return `${date.getFullYear()}-${this.pad(date.getMonth() + 1)}-${this.pad(date.getDate())}`;
  }
}
