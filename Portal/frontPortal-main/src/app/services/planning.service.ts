import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

/** One weekday (1 = lundi … 7 = dimanche); times are "HH:mm" or null. */
export interface PlanningJour {
  jour: number;
  actif: boolean;
  debutMatin: string | null;
  finMatin: string | null;
  debutApresMidi: string | null;
  finApresMidi: string | null;
}

export interface Conge {
  id?: number;
  dateDebut: string;
  dateFin: string;
  motif?: string | null;
}

export interface Planning {
  dureeCreneau: number;
  /** Default consultation fee in dinars, or null. */
  tarifConsultation?: number | null;
  jours: PlanningJour[];
  conges: Conge[];
}

/** Bookable times of a doctor on a day, or why the practice is closed. */
export interface Creneaux {
  date: string;
  dureeCreneau: number;
  slots: string[];
  /** null when open; "conge" (day off) or "ferme" (not a working day). */
  fermeture: 'conge' | 'ferme' | null;
  motif?: string | null;
}

/** Opening hours, slot length and days off of the practice. */
@Injectable({
  providedIn: 'root'
})
export class PlanningService {
  private readonly url = 'http://localhost:8085/planning/';

  constructor(private http: HttpClient) {
  }

  /** Public: bookable times of a doctor on a day (yyyy-MM-dd). */
  getCreneaux(medecinId: number, date: string): Observable<Creneaux> {
    return this.http.get<Creneaux>(this.url + medecinId + '/creneaux', { params: { date } });
  }

  getMine(): Observable<Planning> {
    return this.http.get<Planning>(this.url + 'mien');
  }

  saveMine(planning: Planning): Observable<Planning> {
    return this.http.put<Planning>(this.url + 'mien', planning);
  }

  addConge(conge: Conge): Observable<Conge> {
    return this.http.post<Conge>(this.url + 'mien/conges', conge);
  }

  deleteConge(id: number): Observable<void> {
    return this.http.delete<void>(this.url + 'mien/conges/' + id);
  }

  /** "Fermé le lundi" / "Congé : motif" message for a closed day. */
  static closedLabel(creneaux: Creneaux | null): string {
    if (!creneaux || !creneaux.fermeture) {
      return '';
    }
    if (creneaux.fermeture === 'conge') {
      return creneaux.motif ? `Cabinet fermé (${creneaux.motif}).` : 'Cabinet fermé ce jour-là (congé).';
    }
    return 'Le cabinet ne reçoit pas ce jour-là.';
  }
}
