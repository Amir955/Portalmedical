import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export type ModePaiement = 'especes' | 'cheque' | 'carte' | 'virement';

export interface Paiement {
  id: number;
  rendezVousId: number | null;
  patientId: number | null;
  patientNom: string | null;
  montant: number;
  mode: ModePaiement;
  datePaiement: string;
  note: string | null;
  encaissePar: string | null;
  numeroRecu: string;
}

export interface ResumePaiements {
  total: number;
  nombre: number;
  parMode: { [mode: string]: number };
}

export interface StatMois {
  cle: string;
  libelle: string;
  rendezVous: number;
  honores: number;
  absents: number;
  annules: number;
  demandesEnLigne: number;
  nouveauxPatients: number;
  chiffreAffaires: number;
}

export interface Statistiques {
  mois: StatMois[];
  totaux: {
    rendezVous: number;
    honores: number;
    absents: number;
    nouveauxPatients: number;
    chiffreAffaires: number;
    tauxAbsence: number | null;
  };
}

export const MODES_PAIEMENT: { value: ModePaiement; label: string; icon: string }[] = [
  { value: 'especes', label: 'Espèces', icon: 'payments' },
  { value: 'cheque', label: 'Chèque', icon: 'edit_note' },
  { value: 'carte', label: 'Carte', icon: 'credit_card' },
  { value: 'virement', label: 'Virement', icon: 'account_balance' }
];

/** Payments, receipts and practice statistics. */
@Injectable({
  providedIn: 'root'
})
export class PaiementService {
  private readonly url = 'http://localhost:8085/paiements';

  constructor(private http: HttpClient) {
  }

  /** "45,000 DT" (Tunisian dinar, 3 decimals). */
  static format(amount: number): string {
    return (amount || 0).toLocaleString('fr-FR', { minimumFractionDigits: 3, maximumFractionDigits: 3 }) + ' DT';
  }

  static modeLabel(mode: string): string {
    const found = MODES_PAIEMENT.find((m) => m.value === mode);
    return found ? found.label : 'Espèces';
  }

  getTarif(): Observable<{ tarif: number | null }> {
    return this.http.get<{ tarif: number | null }>(this.url + '/tarif');
  }

  encaisser(rendezVousId: number, montant: number, mode: ModePaiement, note: string): Observable<Paiement> {
    return this.http.post<Paiement>(this.url + '/rendezvous/' + rendezVousId, { montant, mode, note });
  }

  list(du: string, au: string): Observable<Paiement[]> {
    return this.http.get<Paiement[]>(this.url, { params: { du, au } });
  }

  resume(du: string, au: string): Observable<ResumePaiements> {
    return this.http.get<ResumePaiements>(this.url + '/resume', { params: { du, au } });
  }

  recu(id: number): Observable<Blob> {
    return this.http.get(this.url + '/' + id + '/recu', { responseType: 'blob' });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(this.url + '/' + id);
  }

  statistiques(mois: number): Observable<Statistiques> {
    return this.http.get<Statistiques>('http://localhost:8085/statistiques', { params: { mois: String(mois) } });
  }

  /** Opens a PDF blob in a tab opened beforehand (inside the click, so it is not blocked). */
  static openPdf(tab: Window | null, blob: Blob): void {
    const url = URL.createObjectURL(blob);
    if (tab) {
      tab.location.href = url;
    } else {
      window.location.href = url;
    }
    setTimeout(() => URL.revokeObjectURL(url), 60000);
  }
}
