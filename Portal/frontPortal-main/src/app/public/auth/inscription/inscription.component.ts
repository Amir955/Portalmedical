import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AccountService } from '../../../core/services/account.service';

/** Staff sign-up page (médecin or secrétaire). */
@Component({
  selector: 'app-inscription',
  templateUrl: './inscription.component.html',
  styleUrls: ['./inscription.component.css']
})
export class InscriptionComponent {
  readonly minLength = 8;
  readonly today = new Date().toISOString().slice(0, 10);

  inscriptionForm: FormGroup;
  showPassword = false;
  submitting = false;
  /** Shown inside the form until the user edits it. */
  formError = '';

  constructor(
    private formBuilder: FormBuilder,
    private accountService: AccountService,
    private toastr: ToastrService,
    private router: Router
  ) {
    this.inscriptionForm = this.formBuilder.group({
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      age: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(this.minLength)]],
      // Public sign-up is for doctors; secretaries are created by their doctor.
      role: ['Medecin']
    });
    this.inscriptionForm.valueChanges.subscribe(() => this.formError = '');
  }

  get passwordValue(): string {
    return this.inscriptionForm.get('password').value || '';
  }

  get hasMinLength(): boolean {
    return this.passwordValue.length >= this.minLength;
  }

  get hasLetterAndDigit(): boolean {
    return /[A-Za-zÀ-ÿ]/.test(this.passwordValue) && /\d/.test(this.passwordValue);
  }

  showError(name: string): boolean {
    const control = this.inscriptionForm.get(name);
    return control.invalid && (control.dirty || control.touched);
  }

  getErrorEmailMessage(): string {
    const email = this.inscriptionForm.get('email');
    if (email.hasError('required')) {
      return 'Saisissez votre adresse e-mail';
    }
    return email.hasError('email') ? 'Adresse e-mail invalide' : '';
  }

  getErrorPasswordMessage(): string {
    const password = this.inscriptionForm.get('password');
    if (password.hasError('required')) {
      return 'Choisissez un mot de passe';
    }
    return password.hasError('minlength') ? `Au moins ${this.minLength} caractères` : '';
  }

  registerUser(): void {
    if (this.inscriptionForm.invalid || this.submitting) {
      this.inscriptionForm.markAllAsTouched();
      return;
    }

    const value = this.inscriptionForm.value;
    const user = {
      ...value,
      nom: value.nom.trim(),
      prenom: value.prenom.trim(),
      email: value.email.trim()
    };

    this.submitting = true;
    this.formError = '';
    this.accountService.addUserWithoutImage(user).subscribe(
      () => {
        this.submitting = false;
        this.toastr.success('Vous pouvez maintenant vous connecter.', 'Compte créé');
        this.router.navigateByUrl('/connexion');
      },
      error => {
        this.submitting = false;
        if (error && error.status === 409) {
          this.inscriptionForm.get('email').setErrors({ taken: true });
          this.formError = 'Cette adresse e-mail est déjà utilisée. Connectez-vous ou utilisez une autre adresse.';
        } else if (error && error.status === 400) {
          this.formError = 'Vérifiez les informations saisies.';
        } else {
          this.formError = 'Le compte n’a pas pu être créé. Réessayez dans un instant.';
        }
      }
    );
  }
}
