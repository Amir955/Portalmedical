import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator, MatPaginatorIntl } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { Patient } from 'src/models/Patient';
import { PatientService } from '../../core/services/patient.service';
import { AjoutpatientComponent, PatientFormData } from './ajoutpatient/ajoutpatient.component';

/** "Patients": search, add, edit and remove the practice's patients. */
@Component({
  selector: 'app-gestion-des-patients',
  templateUrl: './gestion-des-patients.component.html',
  styleUrls: ['./gestion-des-patients.component.css']
})
export class GestionDesPatientsComponent implements OnInit, AfterViewInit {
  readonly displayedColumns = ['patient', 'infos', 'numtelephone', 'creationDate', 'actions'];
  dataSource = new MatTableDataSource<Patient>([]);
  loading = true;
  search = '';
  toDelete: Patient | null = null;
  deleting = false;

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;

  constructor(private patientService: PatientService,
              private toast: ToastrService,
              private dialog: MatDialog,
              private route: ActivatedRoute,
              private router: Router,
              paginatorIntl: MatPaginatorIntl) {
    paginatorIntl.itemsPerPageLabel = 'Patients par page';
    paginatorIntl.nextPageLabel = 'Page suivante';
    paginatorIntl.previousPageLabel = 'Page précédente';
    paginatorIntl.firstPageLabel = 'Première page';
    paginatorIntl.lastPageLabel = 'Dernière page';
    paginatorIntl.getRangeLabel = (page, size, length) => {
      if (!length) {
        return '0 sur 0';
      }
      const start = page * size + 1;
      return `${start} – ${Math.min(start + size - 1, length)} sur ${length}`;
    };

    // Search across name, e-mail, phone and CIN.
    this.dataSource.filterPredicate = (patient, filter) =>
      [patient.nom, patient.prenom, patient.email, patient.numtelephone, patient.cin]
        .some((field) => String(field || '').toLowerCase().indexOf(filter) !== -1);
    this.dataSource.sortingDataAccessor = (patient, column) => {
      if (column === 'patient') {
        return `${patient.nom || ''} ${patient.prenom || ''}`.toLowerCase();
      }
      if (column === 'creationDate') {
        return patient.creationDate ? new Date(patient.creationDate).getTime() : 0;
      }
      return (patient as any)[column] || '';
    };
  }

  ngOnInit(): void {
    // Search typed in the top bar arrives as ?q=.
    this.route.queryParamMap.subscribe((params) => {
      this.search = params.get('q') || '';
      this.applyFilter();
    });
    this.load();
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  get count(): number {
    return this.dataSource.data.length;
  }

  load(): void {
    this.loading = true;
    this.patientService.getAllpatient().subscribe(
      (patients) => {
        this.dataSource.data = patients || [];
        this.loading = false;
      },
      () => {
        this.loading = false;
        this.toast.error('La liste des patients n’a pas pu être chargée.', 'Erreur');
      }
    );
  }

  applyFilter(): void {
    this.dataSource.filter = this.search.trim().toLowerCase();
    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  clearSearch(): void {
    this.search = '';
    this.applyFilter();
  }

  initials(patient: Patient): string {
    return ((patient.prenom || '').charAt(0) + (patient.nom || '').charAt(0)).toUpperCase() || '?';
  }

  age(patient: Patient): number | null {
    if (!patient.age) {
      return null;
    }
    const birth = new Date(patient.age);
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

  phoneLink(phone: string): string {
    return 'tel:' + String(phone || '').replace(/[^+0-9]/g, '');
  }

  add(): void {
    this.openForm({});
  }

  /** Opens the patient's file. */
  open(patient: Patient): void {
    this.router.navigate(['/patients', patient.id]);
  }

  edit(patient: Patient): void {
    this.openForm({ patient });
  }

  private openForm(data: PatientFormData): void {
    this.dialog.open(AjoutpatientComponent, {
      width: '760px',
      maxWidth: '94vw',
      panelClass: 'pm-dialog',
      autoFocus: false,
      data
    }).afterClosed().subscribe((saved) => {
      if (saved) {
        this.load();
      }
    });
  }

  askDelete(patient: Patient): void {
    this.toDelete = patient;
  }

  confirmDelete(): void {
    const patient = this.toDelete;
    if (!patient || this.deleting) {
      return;
    }
    this.deleting = true;
    this.patientService.supprimerPatient(patient.id).subscribe(
      () => {
        this.deleting = false;
        this.toDelete = null;
        this.toast.success(`${patient.prenom || ''} ${patient.nom || ''} a été retiré de la liste.`.trim(), 'Patient supprimé');
        this.load();
      },
      () => {
        this.deleting = false;
        this.toDelete = null;
        this.toast.error('Ce patient a des rendez-vous ou des documents liés et ne peut pas être supprimé.', 'Suppression impossible');
      }
    );
  }
}
