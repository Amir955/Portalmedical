import { HttpClient, HttpEvent } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { RendezVousEntity } from 'src/models/RendezVous';

/** Medical summary of a patient (doctors only). */
export interface DossierMedical {
  patientId?: number;
  groupeSanguin?: string | null;
  allergies?: string | null;
  antecedents?: string | null;
  traitements?: string | null;
  notes?: string | null;
  dateMaj?: string | null;
  majPar?: string | null;
}

/** One of the doctor's records for the patient (consultation, ordonnance…). */
export interface HistoriqueItem {
  type: 'fiche' | 'ordonnance' | 'certificat-medical' | 'certificat-aptitude' | 'lettre';
  id: number;
  date: string | null;
  titre: string;
  detail: string | null;
}

export interface PatientDocument {
  id: number;
  nom: string;
  typeMime: string;
  taille: number;
  categorie: string;
  dateAjout: string;
}

/** The patient file: medical summary, history, documents and appointments. */
@Injectable({
  providedIn: 'root'
})
export class DossierService {
  private readonly base = 'http://localhost:8085/consultation/patients/';

  constructor(private http: HttpClient) {
  }

  getDossier(patientId: number): Observable<DossierMedical> {
    return this.http.get<DossierMedical>(this.base + patientId + '/dossier');
  }

  saveDossier(patientId: number, dossier: DossierMedical): Observable<DossierMedical> {
    return this.http.put<DossierMedical>(this.base + patientId + '/dossier', dossier);
  }

  getHistorique(patientId: number): Observable<HistoriqueItem[]> {
    return this.http.get<HistoriqueItem[]>(this.base + patientId + '/historique');
  }

  getDocuments(patientId: number): Observable<PatientDocument[]> {
    return this.http.get<PatientDocument[]>(this.base + patientId + '/documents');
  }

  uploadDocument(patientId: number, file: File, categorie: string): Observable<HttpEvent<PatientDocument>> {
    const form = new FormData();
    form.append('fichier', file);
    form.append('categorie', categorie);
    return this.http.post<PatientDocument>(this.base + patientId + '/documents', form, {
      reportProgress: true,
      observe: 'events'
    });
  }

  /** The file itself (sent with the session token, then shown from a local URL). */
  downloadDocument(patientId: number, documentId: number): Observable<Blob> {
    return this.http.get(this.base + patientId + '/documents/' + documentId + '/fichier', { responseType: 'blob' });
  }

  deleteDocument(patientId: number, documentId: number): Observable<void> {
    return this.http.delete<void>(this.base + patientId + '/documents/' + documentId);
  }

  /** The patient's appointments in the practice (also for secretaries). */
  getAppointments(patientId: number): Observable<RendezVousEntity[]> {
    return this.http.get<RendezVousEntity[]>('http://localhost:8085/rendezvous/patient/' + patientId);
  }
}
