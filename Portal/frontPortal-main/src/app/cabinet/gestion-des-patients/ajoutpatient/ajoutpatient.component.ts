import { Component, Inject, Optional } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ToastrService } from 'ngx-toastr';
import { PatientService } from 'src/app/core/services/patient.service';
import { GOUVERNORATS } from 'src/models/gouvernorats';
import { Patient } from 'src/models/Patient';

export interface PatientFormData {
  /** Present when editing an existing patient. */
  patient?: Patient;
}

/** Add or edit a patient (one form for both). Closes with `true` once saved. */
@Component({
  selector: 'app-ajoutpatient',
  templateUrl: './ajoutpatient.component.html',
  styleUrls: ['./ajoutpatient.component.css']
})
export class AjoutpatientComponent {
  readonly gouvernorats = GOUVERNORATS;
  readonly today = new Date();
  readonly editing: Patient | null;

  form: FormGroup;
  saving = false;
  formError = '';

  constructor(private fb: FormBuilder,
              private patientService: PatientService,
              private toast: ToastrService,
              private dialogRef: MatDialogRef<AjoutpatientComponent>,
              @Optional() @Inject(MAT_DIALOG_DATA) data: PatientFormData | null) {
    this.editing = data && data.patient ? data.patient : null;
    const p: any = this.editing || {};
    this.form = this.fb.group({
      prenom: [p.prenom || '', [Validators.required, Validators.maxLength(60)]],
      nom: [p.nom || '', [Validators.required, Validators.maxLength(60)]],
      sexe: [p.sexe || null],
      age: [p.age ? new Date(p.age) : null],
      cin: [p.cin || '', Validators.pattern(/^[0-9]{8}$/)],
      email: [p.email || '', [Validators.required, Validators.email]],
      numtelephone: [p.numtelephone || '', Validators.pattern(/^\+?[0-9 ()-]{8,20}$/)],
      pays: [p.pays || 'Tunisie'],
      gouvernerat: [p.gouvernerat || null],
      ville: [p.ville || ''],
      codepostal: [p.codepostal || null, Validators.pattern(/^[0-9]{4}$/)]
    });
    this.form.valueChanges.subscribe(() => this.formError = '');
  }

  get title(): string {
    return this.editing ? 'Modifier le patient' : 'Nouveau patient';
  }

  showError(name: string): boolean {
    const control = this.form.get(name);
    return control.invalid && (control.dirty || control.touched);
  }

  save(): void {
    if (this.saving) {
      return;
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.formError = 'Vérifiez les champs signalés en rouge.';
      return;
    }

    const value = this.form.value;
    const patient: any = {
      ...(this.editing || {}),
      ...value,
      prenom: value.prenom.trim(),
      nom: value.nom.trim(),
      email: value.email.trim(),
      numtelephone: (value.numtelephone || '').trim(),
      cin: (value.cin || '').trim(),
      ville: (value.ville || '').trim()
    };

    this.saving = true;
    const request = this.editing
      ? this.patientService.modifierPatient(patient)
      : this.patientService.ajoutPatient(patient);
    request.subscribe(
      () => {
        this.saving = false;
        this.toast.success(
          this.editing ? 'Les informations du patient sont à jour.' : `${patient.prenom} ${patient.nom} a été ajouté.`,
          this.editing ? 'Patient modifié' : 'Patient ajouté');
        this.dialogRef.close(true);
      },
      (error) => {
        this.saving = false;
        this.formError = error && error.status === 409
          ? 'Un patient avec ce nom et cette adresse e-mail existe déjà.'
          : 'Le patient n’a pas pu être enregistré. Réessayez dans un instant.';
      }
    );
  }

  close(): void {
    this.dialogRef.close(false);
  }
}
