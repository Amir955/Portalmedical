import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FicheConsultation } from '../../../models/FicheConsultation';

@Injectable({
  providedIn: 'root'
})
export class ConsultationService {
  private url = 'http://localhost:8085/consultation/fiches/';
  private ordonnanceUrl = 'http://localhost:8085/consultation/ordonnances/';
  private documentsUrl = 'http://localhost:8085/consultation/documents/';

  constructor(private http: HttpClient) {}

  getFichesByPatientId(patientId: number): Observable<FicheConsultation[]> {
    return this.http.get<FicheConsultation[]>(this.url + 'patient/' + patientId);
  }

  createFiche(patientId: number, fiche: Partial<FicheConsultation>): Observable<FicheConsultation> {
    return this.http.post<FicheConsultation>(this.url + patientId, fiche);
  }

  downloadOrdonnancePdf(patientId: number, payload: unknown): Observable<Blob> {
    return this.http.post(this.ordonnanceUrl + 'patient/' + patientId + '/pdf', payload, {
      responseType: 'blob'
    });
  }

  /** Certificates and letters share one endpoint; type is the document key. */
  downloadDocumentPdf(patientId: number, type: DocumentPdfType, payload: unknown): Observable<Blob> {
    return this.http.post(this.documentsUrl + 'patient/' + patientId + '/' + type + '/pdf', payload, {
      responseType: 'blob'
    });
  }
}

export type DocumentPdfType = 'certificat-medical' | 'certificat-aptitude' | 'lettre';
