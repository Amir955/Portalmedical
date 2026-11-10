import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Patient } from 'src/models/Patient';

export type ConsultationSectionKey =
  | 'fiche'
  | 'ordonnance'
  | 'certificat-medical'
  | 'certificat-aptitude'
  | 'lettre';

@Injectable({
  providedIn: 'root'
})
export class ConsultationWorkspaceService {
  private readonly selectedPatientSubject = new BehaviorSubject<Patient | null>(null);
  readonly selectedPatient$ = this.selectedPatientSubject.asObservable();

  private readonly activeSectionSubject = new BehaviorSubject<ConsultationSectionKey>('fiche');
  readonly activeSection$ = this.activeSectionSubject.asObservable();

  get selectedPatientSnapshot(): Patient | null {
    return this.selectedPatientSubject.value;
  }

  get activeSectionSnapshot(): ConsultationSectionKey {
    return this.activeSectionSubject.value;
  }

  setSelectedPatient(patient: Patient | null): void {
    this.selectedPatientSubject.next(patient);
  }

  setActiveSection(section: ConsultationSectionKey): void {
    this.activeSectionSubject.next(section);
  }
}
