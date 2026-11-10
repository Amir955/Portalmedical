import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { Patient } from 'src/models/Patient';
import { FicheConsultation } from 'src/models/FicheConsultation';
import { ConsultationService } from '../services/consultation.service';
import { PatientService } from '../services/patient.service';

@Component({
  selector: 'app-fiches-patients',
  templateUrl: './fiches-patients.component.html',
  styleUrls: ['./fiches-patients.component.css']
})
export class FichesPatientsComponent implements OnInit {
  searchTerm = '';
  patients: Patient[] = [];
  filteredPatients: Patient[] = [];
  selectedPatient: Patient | null = null;
  history: FicheConsultation[] = [];
  loadingPatients = false;
  loadingHistory = false;
  saving = false;
  ficheForm: FormGroup;

  constructor(
    private patientService: PatientService,
    private consultationService: ConsultationService,
    private fb: FormBuilder,
    private toast: ToastrService
  ) {
    this.ficheForm = this.fb.group({
      matriculecnam: [''],
      causedeconsultation: ['', Validators.required],
      examen: [''],
      suivie: ['']
    });
  }

  ngOnInit(): void {
    this.loadPatients();
  }

  loadPatients(): void {
    this.loadingPatients = true;
    this.patientService.getAllpatient().subscribe(
      (data) => {
        this.patients = data || [];
        this.applyPatientFilter();
        this.loadingPatients = false;
      },
      () => {
        this.loadingPatients = false;
        this.toast.error('Impossible de charger les patients');
      }
    );
  }

  applyPatientFilter(): void {
    const term = this.normalize(this.searchTerm);
    if (!term) {
      this.filteredPatients = [...this.patients];
      return;
    }

    this.filteredPatients = this.patients.filter((patient) =>
      this.matchesPatient(patient, term)
    );
  }

  selectPatient(patient: Patient): void {
    this.selectedPatient = patient;
    this.ficheForm.patchValue({
      matriculecnam: patient.cin || ''
    });
    this.loadHistory(patient.id);
  }

  loadHistory(patientId: number): void {
    this.loadingHistory = true;
    this.consultationService.getFichesByPatientId(patientId).subscribe(
      (data) => {
        this.history = this.sortHistory(data || []);
        this.loadingHistory = false;
      },
      () => {
        this.loadingHistory = false;
        this.toast.error('Impossible de charger l’historique');
      }
    );
  }

  saveFiche(): void {
    if (!this.selectedPatient || this.ficheForm.invalid) {
      this.ficheForm.markAllAsTouched();
      return;
    }

    this.saving = true;
    this.consultationService.createFiche(this.selectedPatient.id, this.ficheForm.value).subscribe(
      () => {
        this.saving = false;
        this.toast.success('Fiche enregistrée');
        this.ficheForm.patchValue({
          causedeconsultation: '',
          examen: '',
          suivie: ''
        });
        this.loadHistory(this.selectedPatient!.id);
      },
      () => {
        this.saving = false;
        this.toast.error('Enregistrement impossible');
      }
    );
  }

  getPatientLabel(patient: Patient): string {
    const parts = [patient.nom, patient.prenom].filter(Boolean);
    return parts.join(' ');
  }

  getPatientMeta(patient: Patient): string {
    const items = [
      patient.email,
      patient.numtelephone,
      this.formatDate(patient.age),
      patient.cin
    ].filter(Boolean);
    return items.join(' | ');
  }

  getConsultationTitle(index: number): string {
    return `Consultation ${index + 1}`;
  }

  getConsultationDate(item: FicheConsultation): string {
    return this.formatDate(item.datedecreation);
  }

  getConsultationSummary(item: FicheConsultation): string {
    const blocks = [item.causedeconsultation, item.examen, item.suivie].filter((value) => !!value);
    return blocks.length ? blocks.join(' - ') : 'Aucune note';
  }

  private sortHistory(items: FicheConsultation[]): FicheConsultation[] {
    return [...items].sort((a, b) => {
      const dateA = new Date(a.datedecreation || 0).getTime();
      const dateB = new Date(b.datedecreation || 0).getTime();
      if (dateA !== dateB) {
        return dateA - dateB;
      }

      return (a.idFiche || 0) - (b.idFiche || 0);
    });
  }

  private matchesPatient(patient: Patient, term: string): boolean {
    const searchable = [
      patient.nom,
      patient.prenom,
      patient.email,
      patient.numtelephone,
      patient.cin,
      patient.ville,
      patient.pays,
      patient.codepostal,
      patient.gouvernerat,
      patient.sexe,
      this.formatDate(patient.age),
      this.formatDate(patient.creationDate)
    ]
      .map((value) => this.normalize(value))
      .join(' ');

    return searchable.includes(term);
  }

  private normalize(value: unknown): string {
    return String(value ?? '')
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .trim();
  }

  formatDate(value: unknown): string {
    if (!value) {
      return '';
    }

    const date = new Date(value as string | number | Date);
    if (Number.isNaN(date.getTime())) {
      return String(value);
    }

    return new Intl.DateTimeFormat('fr-FR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    }).format(date);
  }
}
