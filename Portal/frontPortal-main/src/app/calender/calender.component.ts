import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CalendarOptions } from '@fullcalendar/angular';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { Patient } from '../../models/Patient';
import { RendezVousEntity } from '../../models/RendezVous';
import { UserEntity } from '../../models/userEntity';
import { AccountService } from '../services/account.service';
import { DemandesNotificationService } from '../services/demandes-notification.service';
import { PatientService } from '../services/patient.service';
import { PlanningService } from '../services/planning.service';
import { RendezVousService } from '../services/rendez-vous.service';

type PatientMode = 'existing' | 'new';

/** Practice calendar: confirmed appointments, add / move / cancel. */
@Component({
  selector: 'app-calender',
  templateUrl: './calender.component.html',
  styleUrls: ['./calender.component.css']
})
export class CalenderComponent implements OnInit {
  /** The doctor's opening-hour slots for the date chosen in a dialog. */
  daySlots: string[] = [];
  /** Why that day has no slot (day off, not a working day), or ''. */
  closedLabel = '';

  calendarOptions: CalendarOptions = this.buildCalendarOptions();
  appointments: RendezVousEntity[] = [];
  doctors: UserEntity[] = [];
  patients: Patient[] = [];

  /** Taken times (HH:mm) of the doctor/date being edited in a dialog. */
  takenSlots: string[] = [];

  // Add dialog
  addOpen = false;
  patientMode: PatientMode = 'existing';
  addForm: FormGroup;
  adding = false;
  addError = '';

  // Edit dialog
  editOpen = false;
  editing: RendezVousEntity | null = null;
  editForm: FormGroup;
  saving = false;
  editError = '';
  confirmCancel = false;

  constructor(private fb: FormBuilder,
              private rdvService: RendezVousService,
              private userService: AccountService,
              private patientService: PatientService,
              private planningService: PlanningService,
              private notifications: DemandesNotificationService,
              private toastr: ToastrService,
              private route: ActivatedRoute,
              private router: Router) {
    this.addForm = this.fb.group({
      patientId: [null],
      nom: [''],
      prenom: [''],
      email: [''],
      telephone: [''],
      doctorId: [null, Validators.required],
      date: ['', Validators.required],
      slot: ['', Validators.required]
    });
    this.editForm = this.fb.group({
      doctorId: [null, Validators.required],
      date: ['', Validators.required],
      slot: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadAppointments();
    // From the dashboard's "Nouveau rendez-vous": open the dialog once the doctors are known.
    const openNew = this.route.snapshot.queryParamMap.get('nouveau') === '1';
    this.userService.getuserByRole('Medecin').subscribe((doctors) => {
      this.doctors = doctors || [];
      if (openNew) {
        this.openAdd();
        // From a patient file: the patient is already chosen.
        const patientId = Number(this.route.snapshot.queryParamMap.get('patient'));
        if (patientId) {
          this.addForm.patchValue({ patientId });
        }
        this.router.navigate([], { relativeTo: this.route, queryParams: {}, replaceUrl: true });
      }
    });
    this.loadPatients();

    // Slot availability follows the chosen doctor and date.
    ['doctorId', 'date'].forEach((name) => {
      this.addForm.get(name).valueChanges.subscribe(() => this.refreshTakenSlots(this.addForm));
      this.editForm.get(name).valueChanges.subscribe(() => this.refreshTakenSlots(this.editForm));
    });
    this.addForm.valueChanges.subscribe(() => this.addError = '');
    this.editForm.valueChanges.subscribe(() => this.editError = '');
  }

  // ------------------------------------------------------------------ data

  loadAppointments(): void {
    this.rdvService.getAgenda().subscribe((list) => {
      this.appointments = list || [];
      this.calendarOptions = {
        ...this.calendarOptions,
        events: this.appointments.map((rdv) => ({
          id: String(rdv.idapt),
          title: this.patientName(rdv),
          start: new Date(rdv.dateOfApt)
        }))
      };
    });
  }

  loadPatients(): void {
    this.patientService.getAllpatient().subscribe((patients) => {
      this.patients = (patients || []).slice().sort((a, b) =>
        `${a.nom} ${a.prenom}`.localeCompare(`${b.nom} ${b.prenom}`));
    });
  }

  get todayCount(): number {
    const today = this.toDateValue(new Date());
    return this.appointments.filter((rdv) => this.toDateValue(new Date(rdv.dateOfApt)) === today).length;
  }

  get weekCount(): number {
    const start = new Date();
    start.setHours(0, 0, 0, 0);
    start.setDate(start.getDate() - ((start.getDay() + 6) % 7));
    const end = new Date(start);
    end.setDate(end.getDate() + 7);
    return this.appointments.filter((rdv) => {
      const when = new Date(rdv.dateOfApt).getTime();
      return when >= start.getTime() && when < end.getTime();
    }).length;
  }

  patientName(rdv: RendezVousEntity): string {
    const prenom = rdv.patient ? rdv.patient.prenom : rdv.patientPrenom;
    const nom = rdv.patient ? rdv.patient.nom : rdv.patientNom;
    return [prenom, nom].filter((part) => !!part).join(' ') || rdv.patientEmail || 'Patient';
  }

  patientPhone(rdv: RendezVousEntity): string {
    return (rdv.patient && rdv.patient.numtelephone) || rdv.patientTelephone || '';
  }

  doctorName(doctor: UserEntity): string {
    return `Dr ${doctor.prenom || ''} ${doctor.nom || ''}`.trim();
  }

  // ------------------------------------------------------------------ slots

  isSlotDisabled(form: FormGroup, slot: string): boolean {
    return this.slotStatus(form, slot) !== '';
  }

  /** "Réservé" / "Passé" for a slot that cannot be chosen, otherwise empty. */
  slotStatus(form: FormGroup, slot: string): string {
    const date = form.get('date').value;
    if (this.takenSlots.indexOf(slot) !== -1 && !this.isOwnSlot(form, slot)) {
      return 'Réservé';
    }
    if (date && new Date(`${date}T${slot}:00`).getTime() < Date.now()) {
      return 'Passé';
    }
    return '';
  }

  pickSlot(form: FormGroup, slot: string): void {
    if (!this.isSlotDisabled(form, slot)) {
      form.patchValue({ slot });
    }
  }

  /** The appointment being edited keeps its own slot available. */
  private isOwnSlot(form: FormGroup, slot: string): boolean {
    if (form !== this.editForm || !this.editing) {
      return false;
    }
    const when = new Date(this.editing.dateOfApt);
    return !!this.editing.user && this.editing.user.id === form.get('doctorId').value
      && this.toDateValue(when) === form.get('date').value
      && this.toTimeValue(when) === slot;
  }

  private refreshTakenSlots(form: FormGroup): void {
    const doctorId = form.get('doctorId').value;
    const date = form.get('date').value;
    this.takenSlots = [];
    this.daySlots = [];
    this.closedLabel = '';
    if (!doctorId || !date) {
      return;
    }
    this.planningService.getCreneaux(doctorId, date).subscribe((creneaux) => {
      const slots = creneaux.slots.slice();
      // An appointment being moved keeps its current time in the list.
      const own = form === this.editForm && this.editing ? this.toTimeValue(new Date(this.editing.dateOfApt)) : '';
      if (own && this.isOwnSlot(form, own) && slots.indexOf(own) === -1) {
        slots.push(own);
        slots.sort();
      }
      this.daySlots = slots;
      this.closedLabel = slots.length ? '' : PlanningService.closedLabel(creneaux);
    });
    this.rdvService.getTakenSlots(doctorId, date).subscribe((slots) => {
      this.takenSlots = slots || [];
      const chosen = form.get('slot').value;
      if (chosen && this.isSlotDisabled(form, chosen)) {
        form.patchValue({ slot: '' }, { emitEvent: false });
      }
    });
  }

  // ------------------------------------------------------------------ add

  openAdd(date?: Date): void {
    this.addError = '';
    this.adding = false;
    this.patientMode = 'existing';
    this.addForm.reset({
      patientId: null, nom: '', prenom: '', email: '', telephone: '',
      doctorId: this.doctors.length === 1 ? this.doctors[0].id : null,
      date: this.toDateValue(date || new Date()),
      slot: ''
    });
    this.refreshTakenSlots(this.addForm);
    this.addOpen = true;
  }

  setPatientMode(mode: PatientMode): void {
    this.patientMode = mode;
    this.addError = '';
  }

  submitAdd(): void {
    if (this.adding) {
      return;
    }
    const value = this.addForm.value;
    const missing = this.patientMode === 'existing'
      ? !value.patientId
      : !value.nom.trim() || !value.prenom.trim() || !/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(value.email.trim());
    if (missing) {
      this.addError = this.patientMode === 'existing'
        ? 'Choisissez un patient dans la liste.'
        : 'Renseignez le nom, le prénom et une adresse e-mail valide.';
      return;
    }
    if (this.addForm.invalid) {
      this.addError = 'Choisissez le médecin, la date et un créneau libre.';
      return;
    }

    this.adding = true;
    if (this.patientMode === 'existing') {
      const patient = this.patients.find((p) => p.id === value.patientId);
      this.createAppointment(patient);
    } else {
      const patient: any = {
        nom: value.nom.trim(),
        prenom: value.prenom.trim(),
        email: value.email.trim(),
        numtelephone: value.telephone.trim()
      };
      this.patientService.ajoutPatient(patient).subscribe(
        (created) => {
          this.loadPatients();
          this.createAppointment(created);
        },
        (error) => {
          this.adding = false;
          this.addError = error && error.status === 409
            ? 'Ce patient existe déjà : choisissez-le dans « Patient existant ».'
            : 'Le patient n’a pas pu être créé. Réessayez dans un instant.';
        }
      );
    }
  }

  private createAppointment(patient: Patient): void {
    const value = this.addForm.value;
    const rdv: any = {
      patientEmail: patient ? patient.email : '',
      patient,
      user: { id: value.doctorId },
      dateOfApt: new Date(`${value.date}T${value.slot}:00`)
    };
    this.rdvService.ajouterApt(rdv).subscribe(
      () => {
        this.adding = false;
        this.addOpen = false;
        this.toastr.success(`Rendez-vous ajouté le ${this.formatWhen(rdv.dateOfApt)}.`, 'Rendez-vous ajouté');
        this.loadAppointments();
      },
      (error) => {
        this.adding = false;
        if (error && error.status === 409) {
          this.addError = 'Ce créneau vient d’être pris. Choisissez une autre heure.';
          this.refreshTakenSlots(this.addForm);
        } else {
          this.addError = 'Le rendez-vous n’a pas pu être ajouté. Réessayez dans un instant.';
        }
      }
    );
  }

  // ------------------------------------------------------------------ edit

  openEdit(id: number): void {
    this.rdvService.getbyid(id).subscribe((rdv) => {
      this.editing = rdv;
      this.editError = '';
      this.confirmCancel = false;
      const when = new Date(rdv.dateOfApt);
      this.editForm.reset({
        doctorId: rdv.user ? rdv.user.id : null,
        date: this.toDateValue(when),
        slot: this.toTimeValue(when)
      });
      this.refreshTakenSlots(this.editForm);
      this.editOpen = true;
    });
  }

  submitEdit(): void {
    if (!this.editing || this.saving) {
      return;
    }
    if (this.editForm.invalid) {
      this.editError = 'Choisissez le médecin, la date et un créneau libre.';
      return;
    }
    const value = this.editForm.value;
    const updated: any = {
      ...this.editing,
      user: { id: value.doctorId },
      dateOfApt: new Date(`${value.date}T${value.slot}:00`)
    };
    this.saving = true;
    this.rdvService.updateApt(updated).subscribe(
      () => {
        this.saving = false;
        this.editOpen = false;
        this.toastr.success(`Le rendez-vous est fixé au ${this.formatWhen(updated.dateOfApt)}.`, 'Rendez-vous modifié');
        this.loadAppointments();
      },
      (error) => {
        this.saving = false;
        this.editError = error && error.status === 409
          ? 'Le médecin a déjà un rendez-vous à cette heure-là.'
          : 'Le rendez-vous n’a pas pu être modifié. Réessayez dans un instant.';
      }
    );
  }

  cancelAppointment(): void {
    if (!this.editing || this.saving) {
      return;
    }
    this.saving = true;
    this.rdvService.refuserRdv(this.editing).subscribe(
      () => {
        this.saving = false;
        this.editOpen = false;
        this.toastr.success('Le patient a été prévenu par e-mail.', 'Rendez-vous annulé');
        this.loadAppointments();
        this.notifications.refresh();
      },
      () => {
        this.saving = false;
        this.confirmCancel = false;
        this.editError = 'Le rendez-vous n’a pas pu être annulé. Réessayez dans un instant.';
      }
    );
  }

  // ------------------------------------------------------------------ helpers

  private formatWhen(date: Date): string {
    return date.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long' })
      + ' à ' + this.toTimeValue(date);
  }

  private toDateValue(date: Date): string {
    return [date.getFullYear(), this.pad(date.getMonth() + 1), this.pad(date.getDate())].join('-');
  }

  private toTimeValue(date: Date): string {
    return this.pad(date.getHours()) + ':' + this.pad(date.getMinutes());
  }

  private pad(value: number): string {
    return value < 10 ? '0' + value : String(value);
  }

  private buildCalendarOptions(): CalendarOptions {
    return {
      headerToolbar: {
        left: 'prev,next today',
        center: 'title',
        right: 'dayGridMonth,dayGridWeek'
      },
      buttonText: { today: 'Aujourd’hui', month: 'Mois', week: 'Semaine' },
      locale: 'fr',
      firstDay: 1,
      initialView: 'dayGridMonth',
      selectable: true,
      editable: false,
      height: 'auto',
      dayMaxEventRows: 3,
      displayEventTime: true,
      eventTimeFormat: { hour: '2-digit', minute: '2-digit', hour12: false },
      events: [],
      dateClick: (info) => this.openAdd(info.date),
      eventClick: (info) => this.openEdit(Number(info.event.id))
    };
  }
}
