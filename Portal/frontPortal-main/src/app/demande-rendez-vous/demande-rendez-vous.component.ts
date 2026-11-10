import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { UserEntity } from '../../models/userEntity';
import { AccountService } from '../services/account.service';
import { RendezVousService } from '../services/rendez-vous.service';

@Component({
  selector: 'app-demande-rendez-vous',
  templateUrl: './demande-rendez-vous.component.html',
  styleUrls: ['./demande-rendez-vous.component.css']
})
export class DemandeRendezVousComponent implements OnInit {
  ajoutRDVform: FormGroup = new FormGroup({});
  medecins: UserEntity[] = [];
  filteredMedecins: UserEntity[] = [];
  /** Taken times per "doctorId|yyyy-MM-dd", loaded from the public endpoint. */
  private takenSlots: { [key: string]: string[] } = {};
  calendarDate: Date = this.getStartOfDay(new Date());

  heures = [
    '08:30', '09:00', '09:30', '10:00', '10:30',
    '11:00', '11:30', '12:00', '14:00', '14:30',
    '15:00', '15:30', '16:00', '16:30', '17:00'
  ];

  specialtyOptions: string[] = [];
  locationOptions: string[] = [];

  selectedDoctor: UserEntity | null = null;
  filterQuery = '';
  selectedSpecialty = 'all';
  selectedLocation = 'all';
  submitting = false;
  /** Shown above the submit button until the form is edited. */
  formError = '';
  formErrorTitle = '';
  readonly meetingDuration = '15 min';
  readonly timezoneLabel = this.buildTimezoneLabel();

  readonly minDate = this.getTodayString();
  readonly minCalendarDate = this.getStartOfDay(new Date());

  constructor(
    private fb: FormBuilder,
    private userService: AccountService,
    private rdvService: RendezVousService,
    private toastr: ToastrService
  ) { }

  ngOnInit(): void {
    this.ajoutRDVform = this.fb.group({
      patientNom: ['', [Validators.required, Validators.maxLength(60)]],
      patientPrenom: ['', [Validators.required, Validators.maxLength(60)]],
      patientDateNaissance: ['', Validators.required],
      patientTelephone: ['', [Validators.required, Validators.pattern(/^\+?[0-9 ()-]{8,20}$/)]],
      patientEmail: ['', [Validators.required, Validators.email]],
      user: ['', Validators.required],
      date: [this.minDate, Validators.required],
      heure: ['', Validators.required]
    });

    const dateControl = this.ajoutRDVform.get('date');
    if (dateControl) {
      dateControl.valueChanges.subscribe(() => {
        this.syncCalendarDate();
        this.loadTakenSlots(this.filteredMedecins);
        this.syncSelectedSlot();
      });
    }

    this.ajoutRDVform.valueChanges.subscribe(() => this.formError = '');
    this.getAllMedecin();
  }

  private setFormError(message: string, title = 'Demande non envoyée'): void {
    this.formError = message;
    this.formErrorTitle = title;
  }

  get selectedDate(): string {
    const control = this.ajoutRDVform.get('date');
    return control ? (control.value || '') : '';
  }

  get selectedSlot(): string {
    const control = this.ajoutRDVform.get('heure');
    return control ? (control.value || '') : '';
  }

  get selectedMonthLabel(): string {
    const label = this.calendarDate.toLocaleDateString('fr-FR', {
      month: 'long',
      year: 'numeric'
    });

    return label.charAt(0).toUpperCase() + label.slice(1);
  }

  getAllMedecin(): void {
    this.userService.getuserByRole('Medecin').subscribe((data) => {
      this.medecins = (data || []).slice().sort((a, b) =>
        this.getDoctorDisplayName(a).localeCompare(this.getDoctorDisplayName(b))
      );
      this.specialtyOptions = this.buildUniqueOptions(this.medecins.map((doctor) => this.getDoctorSpecialty(doctor)));
      this.locationOptions = this.buildUniqueOptions(this.medecins.map((doctor) => this.getDoctorLocation(doctor)));
      this.applyFilters();
    });
  }

  /** Fetches the taken times of these doctors for the selected date (cached per day). */
  loadTakenSlots(doctors: UserEntity[], force = false): void {
    const date = this.selectedDate;
    if (!date) {
      return;
    }

    (doctors || []).forEach((doctor) => {
      const key = this.slotKey(doctor.id, date);
      if (!force && this.takenSlots[key]) {
        return;
      }
      this.rdvService.getTakenSlots(doctor.id, date).subscribe(
        (slots) => {
          this.takenSlots[key] = slots || [];
          this.syncSelectedSlot();
        },
        () => {
          this.takenSlots[key] = this.takenSlots[key] || [];
        }
      );
    });
  }

  applyFilters(): void {
    const query = this.normalize(this.filterQuery);

    this.filteredMedecins = this.medecins.filter((doctor) => {
      const specialty = this.getDoctorSpecialty(doctor);
      const location = this.getDoctorLocation(doctor);
      const haystack = this.normalize([
        doctor.nom,
        doctor.prenom,
        doctor.email,
        doctor.cin,
        doctor.numtelephone,
        doctor.ville,
        doctor.gouvernerat,
        specialty
      ].join(' '));

      const matchesQuery = !query || haystack.indexOf(query) !== -1;
      const matchesSpecialty = this.selectedSpecialty === 'all' || specialty === this.selectedSpecialty;
      const matchesLocation = this.selectedLocation === 'all' || location === this.selectedLocation;

      return matchesQuery && matchesSpecialty && matchesLocation;
    });

    if (this.selectedDoctor && !this.filteredMedecins.some((doctor) => doctor.id === this.selectedDoctor.id)) {
      this.clearSelectedDoctor();
    }

    this.loadTakenSlots(this.filteredMedecins);
  }

  resetFilters(): void {
    this.filterQuery = '';
    this.selectedSpecialty = 'all';
    this.selectedLocation = 'all';
    this.applyFilters();
  }

  selectDoctor(doctor: UserEntity): void {
    this.selectedDoctor = doctor;
    this.ajoutRDVform.patchValue({
      user: doctor.id,
      heure: ''
    });
    this.loadTakenSlots([doctor], true);
  }

  clearSelectedDoctor(): void {
    this.selectedDoctor = null;
    this.ajoutRDVform.patchValue({
      user: '',
      heure: ''
    });
  }

  selectSlot(slot: string): void {
    if (!this.selectedDoctor || this.isSlotUnavailableForDoctor(this.selectedDoctor, this.selectedDate, slot)) {
      return;
    }

    this.ajoutRDVform.patchValue({ heure: slot });
  }

  onCalendarDateChange(date: Date | null): void {
    if (!date) {
      return;
    }

    this.calendarDate = this.getStartOfDay(date);
    this.ajoutRDVform.patchValue({
      date: this.toDateInputValue(this.calendarDate),
      heure: ''
    });
  }

  ajoutRdv(): void {
    if (!this.selectedDoctor) {
      this.setFormError('Choisissez d’abord un médecin.');
      return;
    }

    if (this.ajoutRDVform.invalid) {
      this.ajoutRDVform.markAllAsTouched();
      this.setFormError('Complétez vos coordonnées et choisissez un créneau.', 'Demande incomplète');
      return;
    }

    if (this.isSlotUnavailableForDoctor(this.selectedDoctor, this.selectedDate, this.selectedSlot)) {
      this.setFormError('Ce créneau vient d’être pris. Choisissez une autre heure.', 'Créneau indisponible');
      return;
    }

    const appointmentDate = new Date(this.selectedDate + 'T' + this.selectedSlot + ':00');
    const formValue = this.ajoutRDVform.value;
    const payload: any = {
      patientNom: formValue.patientNom.trim(),
      patientPrenom: formValue.patientPrenom.trim(),
      patientDateNaissance: formValue.patientDateNaissance,
      patientTelephone: formValue.patientTelephone.trim(),
      patientEmail: formValue.patientEmail.trim(),
      user: { id: this.selectedDoctor.id },
      dateOfApt: appointmentDate
    };
    const doctor = this.selectedDoctor;

    this.submitting = true;
    this.formError = '';
    this.rdvService.demandeRDV(payload).subscribe(
      () => {
        this.submitting = false;
        this.toastr.success(
          'Votre demande a été envoyée. Vous recevrez la réponse du médecin par e-mail.',
          'Demande envoyée',
          { timeOut: 9000 }
        );
        this.ajoutRDVform.patchValue({ heure: '' });
        this.loadTakenSlots([doctor], true);
      },
      (error) => {
        this.submitting = false;
        if (error && error.status === 409) {
          this.setFormError('Ce créneau vient d’être réservé. Choisissez une autre heure.', 'Créneau indisponible');
          this.ajoutRDVform.patchValue({ heure: '' }, { emitEvent: false });
          this.loadTakenSlots([doctor], true);
        } else if (error && error.status === 400) {
          this.setFormError('Vérifiez vos coordonnées et le créneau choisi.', 'Demande incomplète');
        } else {
          this.setFormError('Impossible d’envoyer la demande pour le moment. Réessayez dans un instant.');
        }
      }
    );
  }

  getDoctorDisplayName(doctor: UserEntity): string {
    return ((doctor && doctor.nom) || '') + ' ' + ((doctor && doctor.prenom) || '');
  }

  getDoctorInitials(doctor: UserEntity): string {
    const first = doctor && doctor.nom ? doctor.nom.charAt(0) : 'M';
    const second = doctor && doctor.prenom ? doctor.prenom.charAt(0) : 'D';
    return (first + second).toUpperCase();
  }

  getDoctorSpecialty(doctor: UserEntity): string {
    const specialty = doctor.specialite || (doctor as any).speciality || (doctor as any).specialiteMedecin;
    return specialty || 'Medecine generale';
  }

  getDoctorLocation(doctor: UserEntity): string {
    return doctor.ville || (doctor.gouvernerat as any) || 'Localisation non renseignee';
  }

  getDoctorAvailabilityLabel(doctor: UserEntity): string {
    const available = this.getAvailableSlotsForDoctor(doctor, this.selectedDate).length;

    if (available === 0) {
      return 'Journee chargee';
    }

    if (available <= 3) {
      return 'Peu de créneaux';
    }

    return 'Créneaux visibles';
  }

  getPreviewSlots(doctor: UserEntity): string[] {
    return this.getAvailableSlotsForDoctor(doctor, this.selectedDate).slice(0, 4);
  }

  getSelectedDoctorAvailableSlots(): string[] {
    if (!this.selectedDoctor) {
      return [];
    }

    return this.getAvailableSlotsForDoctor(this.selectedDoctor, this.selectedDate);
  }

  getMeetingLocation(doctor: UserEntity | null): string {
    if (!doctor) {
      return 'A definir';
    }

    const location = this.getDoctorLocation(doctor);
    return location === 'Localisation non renseignee' ? 'Cabinet ou confirmation par email' : location;
  }

  getAvailableSlotsForDoctor(doctor: UserEntity, dateValue: string): string[] {
    return this.heures.filter((slot) => !this.isSlotUnavailableForDoctor(doctor, dateValue, slot));
  }

  isSlotUnavailableForDoctor(doctor: UserEntity, dateValue: string, slot: string): boolean {
    if (!doctor || !dateValue || !slot) {
      return false;
    }

    return this.isPastSlot(dateValue, slot) || this.isSlotTaken(doctor, dateValue, slot);
  }

  isSlotTaken(doctor: UserEntity, dateValue: string, slot: string): boolean {
    const taken = doctor ? this.takenSlots[this.slotKey(doctor.id, dateValue)] : null;
    return !!taken && taken.indexOf(slot) !== -1;
  }

  /** "Réservé" or "Passé" for a slot that cannot be picked, otherwise empty. */
  getSlotStatus(slot: string): string {
    if (!this.selectedDoctor) {
      return '';
    }
    if (this.isSlotTaken(this.selectedDoctor, this.selectedDate, slot)) {
      return 'Réservé';
    }
    return this.isPastSlot(this.selectedDate, slot) ? 'Passé' : '';
  }

  formatDateLabel(dateValue: string): string {
    if (!dateValue) {
      return 'Date non choisie';
    }

    const date = new Date(dateValue + 'T00:00:00');
    return date.toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: 'long',
      year: 'numeric'
    });
  }

  hasError(controlName: string, errorName?: string): boolean {
    const control = this.ajoutRDVform.get(controlName);

    if (!control) {
      return false;
    }

    if (!errorName) {
      return control.invalid && (control.dirty || control.touched);
    }

    return control.hasError(errorName) && (control.dirty || control.touched);
  }

  private syncSelectedSlot(): void {
    if (!this.selectedDoctor || !this.selectedSlot) {
      return;
    }

    if (this.isSlotUnavailableForDoctor(this.selectedDoctor, this.selectedDate, this.selectedSlot)) {
      this.ajoutRDVform.patchValue({ heure: '' }, { emitEvent: false });
    }
  }

  private syncCalendarDate(): void {
    if (!this.selectedDate) {
      return;
    }

    const nextDate = new Date(this.selectedDate + 'T00:00:00');
    if (!isNaN(nextDate.getTime())) {
      this.calendarDate = nextDate;
    }
  }

  private slotKey(doctorId: number, dateValue: string): string {
    return doctorId + '|' + dateValue;
  }

  private buildUniqueOptions(values: string[]): string[] {
    const normalized = values.filter((value) => !!value);
    return Array.from(new Set(normalized)).sort((a, b) => a.localeCompare(b));
  }

  private normalize(value: any): string {
    return String(value || '').trim().toLowerCase();
  }

  private isPastSlot(dateValue: string, slot: string): boolean {
    const slotDate = new Date(dateValue + 'T' + slot + ':00');
    if (isNaN(slotDate.getTime())) {
      return false;
    }

    return slotDate.getTime() < new Date().getTime();
  }

  private toDateInputValue(date: Date): string {
    return [
      date.getFullYear(),
      this.pad(date.getMonth() + 1),
      this.pad(date.getDate())
    ].join('-');
  }

  private pad(value: number): string {
    return value < 10 ? '0' + value : String(value);
  }

  private getStartOfDay(date: Date): Date {
    return new Date(date.getFullYear(), date.getMonth(), date.getDate());
  }

  private getTodayString(): string {
    return this.toDateInputValue(new Date());
  }

  private buildTimezoneLabel(): string {
    const now = new Date();
    const offsetMinutes = -now.getTimezoneOffset();
    const sign = offsetMinutes >= 0 ? '+' : '-';
    const abs = Math.abs(offsetMinutes);
    const hours = this.pad(Math.floor(abs / 60));
    const minutes = this.pad(abs % 60);

    return 'UTC ' + sign + hours + ':' + minutes + ' Heure locale';
  }
}
