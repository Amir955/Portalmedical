import { Component, Inject, Optional } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { AuthentificationService } from '../authentification.service';

/** "Mot de passe oublié" dialog opened from the login page. */
@Component({
  selector: 'app-password',
  templateUrl: './password.component.html',
  styleUrls: ['./password.component.css']
})
export class PasswordComponent {

  email = new FormControl('', [Validators.required, Validators.email]);
  sending = false;
  sent = false;
  /** Shown inside the dialog until the address is edited. */
  formError = '';

  constructor(private authService: AuthentificationService,
              private dialogRef: MatDialogRef<PasswordComponent>,
              @Optional() @Inject(MAT_DIALOG_DATA) data: { email?: string } | null) {
    if (data && data.email) {
      this.email.setValue(data.email);
    }
    this.email.valueChanges.subscribe(() => this.formError = '');
  }

  getErrorEmailMessage(): string {
    if (this.email.hasError('required')) {
      return 'Saisissez votre adresse e-mail';
    }
    return this.email.hasError('email') ? 'Adresse e-mail invalide' : '';
  }

  sendEmail(): void {
    if (this.email.invalid || this.sending) {
      this.email.markAsTouched();
      return;
    }

    this.sending = true;
    this.formError = '';
    // The server answers the same way whether or not the account exists.
    this.authService.forgotPassword(this.email.value.trim()).subscribe(
      () => {
        this.sending = false;
        this.sent = true;
      },
      error => {
        this.sending = false;
        this.formError = error && error.status === 503
          ? 'Le service d’envoi d’e-mails est indisponible pour le moment. Réessayez plus tard ou contactez le cabinet.'
          : 'Le lien n’a pas pu être envoyé. Réessayez dans un instant.';
      }
    );
  }

  close(): void {
    this.dialogRef.close();
  }
}
