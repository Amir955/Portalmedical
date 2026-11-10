import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { Subscription } from 'rxjs';
import { Patient } from 'src/models/Patient';
import { FicheConsultation } from 'src/models/FicheConsultation';
import { ConsultationService, DocumentPdfType } from '../../core/services/consultation.service';
import { PatientService } from '../../core/services/patient.service';
import {
  ConsultationSectionKey,
  ConsultationWorkspaceService
} from '../../core/services/consultation-workspace.service';

interface ConsultationSection {
  key: ConsultationSectionKey;
  label: string;
  subtitle: string;
}

interface OrdonnanceLine {
  medicament: string;
  dosage: string;
  forme: string;
  voie: string;
  frequence: string;
  duree: string;
  instructions: string;
}

@Component({
  selector: 'app-consultation',
  templateUrl: './consultation.component.html',
  styleUrls: ['./consultation.component.css']
})
export class ConsultationComponent implements OnInit, OnDestroy {
  private readonly subscriptions = new Subscription();

  sections: ConsultationSection[] = [
    {
      key: 'fiche',
      label: 'Fiche patient',
      subtitle: 'Historique et nouvelle consultation'
    },
    {
      key: 'ordonnance',
      label: 'Ordonnance',
      subtitle: 'Prescription et aperçu imprimable'
    },
    {
      key: 'certificat-medical',
      label: 'Certificat médical',
      subtitle: 'Repos et justification'
    },
    {
      key: 'certificat-aptitude',
      label: "Certificat d'aptitude",
      subtitle: 'Aptitude et observation'
    },
    {
      key: 'lettre',
      label: 'Lettre à un confrère',
      subtitle: 'Synthèse et orientation'
    }
  ];

  searchTerm = '';
  patients: Patient[] = [];
  filteredPatients: Patient[] = [];
  selectedPatient: Patient | null = null;
  history: FicheConsultation[] = [];
  loadingPatients = false;
  loadingHistory = false;
  saving = false;
  activeSection: ConsultationSectionKey = 'fiche';
  todayLabel = this.formatDate(new Date());
  ficheForm: FormGroup;

  ordonnanceContext = {
    diagnostic: '',
    antecedents: '',
    allergies: '',
    consigne: '',
    suivi: ''
  };

  ordonnanceLines: OrdonnanceLine[] = [this.createOrdonnanceLine()];

  certificateMedical = {
    repos: '',
    note: ''
  };

  certificateAptitude = {
    profession: '',
    etatSante: '',
    note: ''
  };

  generatingPdf = false;

  letterDraft = {
    recipient: '',
    subject: '',
    resume: '',
    request: ''
  };

  ordonnanceTips = [
    'Afficher l’identité du patient et la date automatiquement.',
    'Écrire un médicament par ligne avec dose, voie, fréquence et durée.',
    'Ajouter des consignes claires et lisibles pour le patient.',
    'Prévoir un aperçu imprimable avant validation.'
  ];

  constructor(
    private patientService: PatientService,
    private consultationService: ConsultationService,
    private workspace: ConsultationWorkspaceService,
    private route: ActivatedRoute,
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
    this.subscriptions.add(
      this.route.data.subscribe((data) => {
        const section = data['section'] as ConsultationSectionKey | undefined;
        if (section) {
          this.setActiveSection(section);
        }
      })
    );

    this.subscriptions.add(
      this.workspace.activeSection$.subscribe((section) => {
        this.activeSection = section;
      })
    );

    this.subscriptions.add(
      this.workspace.selectedPatient$.subscribe((patient) => {
        this.selectedPatient = patient;
        if (patient) {
          this.ficheForm.patchValue({
            matriculecnam: patient.cin || ''
          });
          this.loadHistory(patient.id);
          this.syncTemplatesWithPatient(patient);
        } else {
          this.history = [];
        }
      })
    );

    this.loadPatients();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
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
    this.workspace.setSelectedPatient(patient);
  }

  clearPatientSelection(): void {
    this.workspace.setSelectedPatient(null);
    this.setActiveSection('fiche');
    this.ficheForm.reset({ matriculecnam: '' });
    this.history = [];
  }

  setActiveSection(section: ConsultationSectionKey, syncWorkspace = true): void {
    this.activeSection = section;
    if (syncWorkspace) {
      this.workspace.setActiveSection(section);
    }
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

  addOrdonnanceLine(): void {
    this.ordonnanceLines.push(this.createOrdonnanceLine());
  }

  removeOrdonnanceLine(index: number): void {
    if (this.ordonnanceLines.length === 1) {
      return;
    }

    this.ordonnanceLines.splice(index, 1);
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

  getOrdonnancePreview(): string[] {
    if (!this.selectedPatient) {
      return [];
    }

    return this.ordonnanceLines
      .map((line) => {
        const parts = [
          line.medicament,
          line.dosage,
          line.forme,
          line.voie,
          line.frequence,
          line.duree,
          line.instructions
        ].filter((value) => !!value);
        return parts.join(' • ');
      })
      .filter((value) => !!value);
  }

  downloadOrdonnancePdf(): void {
    if (!this.selectedPatient) {
      this.toast.info('Choisissez un patient avant de générer le PDF');
      return;
    }

    const payload = {
      diagnostic: this.ordonnanceContext.diagnostic,
      antecedents: this.ordonnanceContext.antecedents,
      allergies: this.ordonnanceContext.allergies,
      consigne: this.ordonnanceContext.consigne,
      suivi: this.ordonnanceContext.suivi,
      lines: this.ordonnanceLines
    };

    this.generatingPdf = true;
    this.consultationService.downloadOrdonnancePdf(this.selectedPatient.id, payload).subscribe(
      (blob) => {
        this.generatingPdf = false;
        this.saveBlob(blob, this.getFileName('ordonnance'));
        this.toast.success("PDF de l'ordonnance généré");
      },
      () => {
        this.generatingPdf = false;
        this.toast.error("Impossible de générer le PDF de l'ordonnance");
      }
    );
  }

  downloadDocumentPdf(type: DocumentPdfType): void {
    if (!this.selectedPatient) {
      this.toast.info('Choisissez un patient avant de générer le PDF');
      return;
    }

    const documents: Record<DocumentPdfType, { payload: unknown; file: string; label: string }> = {
      'certificat-medical': {
        payload: { repos: this.certificateMedical.repos, note: this.certificateMedical.note },
        file: 'certificat-medical',
        label: 'du certificat médical'
      },
      'certificat-aptitude': {
        payload: {
          profession: this.certificateAptitude.profession,
          etatSante: this.certificateAptitude.etatSante,
          note: this.certificateAptitude.note
        },
        file: 'certificat-aptitude',
        label: "du certificat d'aptitude"
      },
      lettre: {
        payload: {
          recipient: this.letterDraft.recipient,
          subject: this.letterDraft.subject,
          resume: this.letterDraft.resume,
          request: this.letterDraft.request
        },
        file: 'lettre-confrere',
        label: 'de la lettre'
      }
    };
    const doc = documents[type];

    this.generatingPdf = true;
    this.consultationService.downloadDocumentPdf(this.selectedPatient.id, type, doc.payload).subscribe(
      (blob) => {
        this.generatingPdf = false;
        this.saveBlob(blob, this.getFileName(doc.file));
        this.toast.success(`PDF ${doc.label} généré`);
      },
      () => {
        this.generatingPdf = false;
        this.toast.error(`Impossible de générer le PDF ${doc.label}`);
      }
    );
  }

  private saveBlob(blob: Blob, fileName: string): void {
    const downloadUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(downloadUrl);
  }

  private createOrdonnanceLine(): OrdonnanceLine {
    return {
      medicament: '',
      dosage: '',
      forme: '',
      voie: '',
      frequence: '',
      duree: '',
      instructions: ''
    };
  }

  private getFileName(prefix: string): string {
    if (!this.selectedPatient) {
      return `${prefix}.pdf`;
    }

    const patientName = this.normalize(this.getPatientLabel(this.selectedPatient))
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-+|-+$/g, '');

    return `${prefix}-${patientName || 'patient'}.pdf`;
  }

  private syncTemplatesWithPatient(patient: Patient): void {
    this.ordonnanceContext = {
      diagnostic: '',
      antecedents: '',
      allergies: '',
      consigne: '',
      suivi: ''
    };
    this.certificateMedical = {
      repos: '',
      note: ''
    };
    this.certificateAptitude = {
      profession: '',
      etatSante: '',
      note: ''
    };
    this.letterDraft = {
      recipient: '',
      subject: '',
      resume: '',
      request: ''
    };
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
