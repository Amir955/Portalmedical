import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ResetPassword } from 'src/models/ResetPassword';
import { AuthentificationService } from '../authentification.service';
import { TokenService } from '../token.service';

/** Group validator: the confirmation must match the new password. */
export const passwordsMatch: ValidatorFn = (group: AbstractControl): ValidationErrors | null => {
  const password = group.get('password');
  const confirm = group.get('confirmPassword');
  if (!password || !confirm || !confirm.value) {
    return null;
  }
  return password.value === confirm.value ? null : { mismatch: true };
};

type ResetState = 'form' | 'expired' | 'done';

/** "Nouveau mot de passe" page opened from the link in the reset e-mail. */
@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent implements OnInit {
  readonly minLength = 8;

  password = new FormControl('', [Validators.required, Validators.minLength(this.minLength)]);
  confirmPassword = new FormControl('', [Validators.required]);
  passwordForm = new FormGroup({
    password: this.password,
    confirmPassword: this.confirmPassword
  }, { validators: passwordsMatch });

  hidePassword = true;
  hideConfirm = true;
  saving = false;
  state: ResetState = 'form';
  /** Shown inside the form until the user edits it. */
  formError = '';

  private token: string | null = null;

  constructor(private route: ActivatedRoute,
              private authService: AuthentificationService,
              private tokenService: TokenService) {
  }

  ngOnInit(): void {
    this.token = this.route.snapshot.paramMap.get('token');
    if (!this.token || this.tokenService.isTokenExpired(this.token)) {
      this.state = 'expired';
    }
    this.passwordForm.valueChanges.subscribe(() => this.formError = '');
  }

  get hasMinLength(): boolean {
    return (this.password.value || '').length >= this.minLength;
  }

  get hasLetterAndDigit(): boolean {
    const value: string = this.password.value || '';
    return /[A-Za-zÀ-ÿ]/.test(value) && /\d/.test(value);
  }

  get matches(): boolean {
    return !!this.confirmPassword.value && this.password.value === this.confirmPassword.value;
  }

  get showMismatch(): boolean {
    return this.passwordForm.hasError('mismatch') && (this.confirmPassword.dirty || this.confirmPassword.touched);
  }

  getErrorPasswordMessage(): string {
    if (this.password.hasError('required')) {
      return 'Saisissez un nouveau mot de passe';
    }
    return this.password.hasError('minlength') ? `Au moins ${this.minLength} caractères` : '';
  }

  confirm(): void {
    if (this.passwordForm.invalid || this.saving) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    const request = new ResetPassword();
    request.token = this.token as string;
    request.password = this.password.value;

    this.saving = true;
    this.authService.resetPassword(request).subscribe(
      () => {
        this.saving = false;
        this.state = 'done';
      },
      error => {
        this.saving = false;
        if (error && error.status === 400) {
          this.state = 'expired';
        } else {
          this.formError = 'Le mot de passe n’a pas pu être enregistré. Réessayez dans un instant.';
        }
      }
    );
  }
}
