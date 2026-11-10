import { HttpEventType } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { Subscription } from 'rxjs';
import { Patient } from 'src/models/Patient';
import { RendezVousEntity } from 'src/models/RendezVous';
import { TokenService } from '../Authentification/token.service';
import { AjoutpatientComponent } from '../gestion-des-patients/ajoutpatient/ajoutpatient.component';
import { DossierMedical, DossierService, HistoriqueItem, PatientDocument } from '../services/dossier.service';
import { PatientService } from '../services/patient.service';

type Filter = 'tout' | 'rdv' | 'fiche' | 'ordonnance' | 'certificat' | 'lettre';

interface TimelineEntry {
  kind: 'rdv' | HistoriqueItem['type'];
  date: Date | null;
  title: string;
  detail: string;
  badge?: string;
  badgeTone?: 'ok' | 'wait' | 'off' | 'next';
  link?: string;
}

/** One patient's file: identity, medical summary, history and documents. */
@Component({
  selector: 'app-patient-file',
  templateUrl: './patient-file.component.html',
  styleUrls: ['./patient-file.component.css']
})
export class PatientFileComponent implements OnInit, OnDestroy {
  readonly isDoctor = this.tokenService.getUserRole() === 'Medecin';
  readonly bloodGroups = ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'];
  readonly categories = [
    { value: 'analyse', label: 'Analyse' },
    { value: 'imagerie', label: 'Imagerie' },
    { value: 'courrier', label: 'Courrier' },
    { value: 'ordonnance', label: 'Ordonnance' },
    { value: 'autre', label: 'Autre' }
  ];

  patientId = 0;
  patient: Patient | null = null;
  notFound = false;

  dossier: DossierMedical = {};
  editingDossier = false;
  draft: DossierMedical = {};
  savingDossier = false;
  dossierError = '';

  appointments: RendezVousEntity[] = [];
  records: HistoriqueItem[] = [];
  filter: Filter = 'tout';

  documents: PatientDocument[] = [];
  uploadCategory = 'analyse';
  uploadProgress: number | null = null;
  uploadError = '';
  toDelete: PatientDocument | null = null;

  private routeSub: Subscription | null = null;

  constructor(private route: ActivatedRoute,
              private router: Router,
              private tokenService: TokenService,
              private patientService: PatientService,
              private dossierService: DossierService,
              private dialog: MatDialog,
              private toastr: ToastrService) {
  }

  ngOnInit(): void {
    this.routeSub = this.route.paramMap.subscribe((params) => {
      this.patientId = Number(params.get('id'));
      this.load();
    });
  }

  ngOnDestroy(): void {
    if (this.routeSub) {
      this.routeSub.unsubscribe();
    }
  }

  // ------------------------------------------------------------------ loading

  load(): void {
    this.notFound = false;
    this.patientService.getbyid(this.patientId).subscribe(
      (patient) => this.patient = patient,
      () => this.notFound = true
    );
    this.dossierService.getAppointments(this.patientId).subscribe((list) => this.appointments = list || []);
    if (this.isDoctor) {
      this.dossierService.getDossier(this.patientId).subscribe((dossier) => this.dossier = dossier || {});
      this.dossierService.getHistorique(this.patientId).subscribe((items) => this.records = items || []);
      this.loadDocuments();
    }
  }

  loadDocuments(): void {
    this.dossierService.getDocuments(this.patientId).subscribe((docs) => this.documents = docs || []);
  }

  // ------------------------------------------------------------------ identity

  get fullName(): string {
    return this.patient ? `${this.patient.prenom || ''} ${this.patient.nom || ''}`.trim() || 'Patient' : '';
  }

  get initials(): string {
    return this.patient
      ? ((this.patient.prenom || '').charAt(0) + (this.patient.nom || '').charAt(0)).toUpperCase() || '?'
      : '';
  }

  get age(): number | null {
    if (!this.patient || !this.patient.age) {
      return null;
    }
    const birth = new Date(this.patient.age);
    if (isNaN(birth.getTime())) {
      return null;
    }
    const now = new Date();
    let years = now.getFullYear() - birth.getFullYear();
    if (now.getMonth() < birth.getMonth() || (now.getMonth() === birth.getMonth() && now.getDate() < birth.getDate())) {
      years--;
    }
    return years >= 0 ? years : null;
  }

  get address(): string {
    if (!this.patient) {
      return '';
    }
    return [this.patient.ville, this.patient.codepostal, this.patient.gouvernerat as any, this.patient.pays]
      .filter((part) => !!part).join(', ');
  }

  get nextAppointment(): RendezVousEntity | null {
    const now = Date.now();
    const upcoming = this.appointments
      .filter((rdv) => this.stateOf(rdv) === 'confirmed' && new Date(rdv.dateOfApt).getTime() >= now)
      .sort((a, b) => new Date(a.dateOfApt).getTime() - new Date(b.dateOfApt).getTime());
    return upcoming[0] || null;
  }

  phoneLink(phone: string): string {
    return 'tel:' + String(phone || '').replace(/[^+0-9]/g, '');
  }

  editPatient(): void {
    if (!this.patient) {
      return;
    }
    this.dialog.open(AjoutpatientComponent, {
      width: '760px',
      maxWidth: '94vw',
      panelClass: 'pm-dialog',
      autoFocus: false,
      data: { patient: this.patient }
    }).afterClosed().subscribe((saved) => {
      if (saved) {
        this.patientService.getbyid(this.patientId).subscribe((patient) => this.patient = patient);
      }
    });
  }

  newAppointment(): void {
    this.router.navigate(['/calender'], { queryParams: { nouveau: 1, patient: this.patientId } });
  }

  // ------------------------------------------------------------------ medical summary

  startEditDossier(): void {
    this.draft = { ...this.dossier };
    this.dossierError = '';
    this.editingDossier = true;
  }

  cancelEditDossier(): void {
    this.editingDossier = false;
  }

  saveDossier(): void {
    if (this.savingDossier) {
      return;
    }
    this.savingDossier = true;
    this.dossierService.saveDossier(this.patientId, this.draft).subscribe(
      (saved) => {
        this.savingDossier = false;
        this.dossier = saved;
        this.editingDossier = false;
        this.toastr.success('Les informations médicales sont à jour.', 'Dossier enregistré');
      },
      () => {
        this.savingDossier = false;
        this.dossierError = 'Les informations n’ont pas pu être enregistrées. Réessayez dans un instant.';
      }
    );
  }

  get allergyList(): string[] {
    return (this.dossier.allergies || '').split(/[\n,;]+/).map((a) => a.trim()).filter((a) => !!a);
  }

  // ------------------------------------------------------------------ timeline

  get timeline(): TimelineEntry[] {
    const entries: TimelineEntry[] = [];
    const now = Date.now();

    if (this.filter === 'tout' || this.filter === 'rdv') {
      this.appointments.forEach((rdv) => {
        const date = rdv.dateOfApt ? new Date(rdv.dateOfApt) : null;
        const state = this.stateOf(rdv);
        const future = !!date && date.getTime() >= now;
        const labels: { [key: string]: [string, TimelineEntry['badgeTone']] } = {
          confirmed: [future ? 'À venir' : 'Honoré', future ? 'next' : 'ok'],
          waiting: ['Demande en attente', 'wait'],
          refused: ['Refusé', 'off'],
          cancelled: ['Annulé par le patient', 'off']
        };
        const [badge, tone] = labels[state] || [state, 'wait'];
        entries.push({
          kind: 'rdv',
          date,
          title: 'Rendez-vous',
          detail: rdv.user ? `Dr ${rdv.user.prenom || ''} ${rdv.user.nom || ''}`.trim() : '',
          badge,
          badgeTone: tone
        });
      });
    }

    const links: { [key: string]: string } = {
      'fiche': '/fichespatients',
      'ordonnance': '/ordonnances',
      'certificat-medical': '/certificatmedical',
      'certificat-aptitude': '/certificatmedicalaptitude',
      'lettre': '/lettreaunconfrere'
    };
    this.records
      .filter((item) => this.filter === 'tout'
        || item.type === this.filter
        || (this.filter === 'certificat' && item.type.indexOf('certificat') === 0))
      .forEach((item) => entries.push({
        kind: item.type,
        date: item.date ? new Date(item.date) : null,
        title: item.titre,
        detail: item.detail || '',
        link: links[item.type]
      }));

    return entries.sort((a, b) => (b.date ? b.date.getTime() : 0) - (a.date ? a.date.getTime() : 0));
  }

  count(filter: Filter): number {
    if (filter === 'rdv') {
      return this.appointments.length;
    }
    if (filter === 'tout') {
      return this.appointments.length + this.records.length;
    }
    return this.records.filter((item) => item.type === filter
      || (filter === 'certificat' && item.type.indexOf('certificat') === 0)).length;
  }

  icon(kind: TimelineEntry['kind']): string {
    const icons: { [key: string]: string } = {
      'rdv': 'event',
      'fiche': 'stethoscope',
      'ordonnance': 'medication',
      'certificat-medical': 'assignment',
      'certificat-aptitude': 'verified',
      'lettre': 'forward_to_inbox'
    };
    return icons[kind] || 'description';
  }

  private stateOf(rdv: RendezVousEntity): string {
    return String((rdv as any).stateapt || '');
  }

  // ------------------------------------------------------------------ documents

  onFileChosen(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files && input.files[0];
    input.value = '';
    if (!file) {
      return;
    }
    this.uploadError = '';
    if (file.size > 10 * 1024 * 1024) {
      this.uploadError = 'Ce fichier dépasse 10 Mo.';
      return;
    }
    if (!/^(application\/pdf|image\/(jpeg|png|gif|webp))$/.test(file.type)) {
      this.uploadError = 'Seuls les PDF et les images (JPG, PNG, GIF, WebP) sont acceptés.';
      return;
    }
    this.uploadProgress = 0;
    this.dossierService.uploadDocument(this.patientId, file, this.uploadCategory).subscribe(
      (event) => {
        if (event.type === HttpEventType.UploadProgress && event.total) {
          this.uploadProgress = Math.round((event.loaded / event.total) * 100);
        } else if (event.type === HttpEventType.Response) {
          this.uploadProgress = null;
          this.toastr.success(`${file.name} a été ajouté au dossier.`, 'Document ajouté');
          this.loadDocuments();
        }
      },
      (error) => {
        this.uploadProgress = null;
        this.uploadError = error && error.status === 413
          ? 'Ce fichier dépasse 10 Mo.'
          : error && error.status === 415
            ? 'Seuls les PDF et les images sont acceptés.'
            : 'Le document n’a pas pu être ajouté. Réessayez dans un instant.';
      }
    );
  }

  openDocument(doc: PatientDocument): void {
    // Open the tab now (still inside the click), then load the file into it.
    const tab = window.open('', '_blank');
    this.dossierService.downloadDocument(this.patientId, doc.id).subscribe(
      (blob) => {
        const url = URL.createObjectURL(blob);
        if (tab) {
          tab.location.href = url;
        } else {
          window.location.href = url;
        }
        setTimeout(() => URL.revokeObjectURL(url), 60000);
      },
      () => {
        if (tab) {
          tab.close();
        }
        this.toastr.error('Le document n’a pas pu être ouvert.', 'Erreur');
      }
    );
  }

  confirmDeleteDocument(): void {
    const doc = this.toDelete;
    if (!doc) {
      return;
    }
    this.toDelete = null;
    this.dossierService.deleteDocument(this.patientId, doc.id).subscribe(
      () => {
        this.documents = this.documents.filter((item) => item.id !== doc.id);
        this.toastr.success(`${doc.nom} a été retiré du dossier.`, 'Document supprimé');
      },
      () => this.toastr.error('Le document n’a pas pu être supprimé.', 'Erreur')
    );
  }

  categoryLabel(value: string): string {
    const found = this.categories.find((c) => c.value === value);
    return found ? found.label : 'Autre';
  }

  size(bytes: number): string {
    if (bytes >= 1024 * 1024) {
      return (bytes / (1024 * 1024)).toFixed(1).replace('.', ',') + ' Mo';
    }
    return Math.max(1, Math.round(bytes / 1024)) + ' Ko';
  }

  isImage(doc: PatientDocument): boolean {
    return doc.typeMime.indexOf('image/') === 0;
  }
}
