import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { UserEntity } from 'src/models/userEntity';
import { AuthentificationService } from '../../../core/auth/authentification.service';
import { PasswordComponent } from '../password/password.component';
import { TokenService } from '../../../core/auth/token.service';
import { AccountService } from '../../../core/services/account.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  user: UserEntity | undefined;

  email = new FormControl('', [Validators.required, Validators.email]);
  password = new FormControl('', [Validators.required, Validators.minLength(4)]);
  hide = true;

  loginForm = new FormGroup({
    email: this.email,
    password: this.password

  });
  constructor(private authService: AuthentificationService,
              private token: TokenService,
              private account: AccountService,
              private toastr: ToastrService,
              private router: Router,
              private dialog: MatDialog) { }

  submitting = false;
  /** Shown inside the form until the user edits it. */
  formError = '';

  ngOnInit(): void {
    this.email.reset();
    this.password.reset();
    this.loginForm.valueChanges.subscribe(() => this.formError = '');
  }

  openDialog() {
    this.dialog.open(PasswordComponent, {
      width: '440px',
      maxWidth: '92vw',
      panelClass: 'auth-dialog',
      autoFocus: true,
      data: { email: this.email.valid ? this.email.value : '' }
    });
  }

  getErrorEmailMessage() {
    if (this.email.hasError('required')) {
      return 'Saisissez votre adresse e-mail';
    }
    return this.email.hasError('email') ? 'Adresse e-mail invalide' : '';
  }

  getErrorPasswordMessage() {
    if (this.password.hasError('required')) {
      return 'Saisissez votre mot de passe';
    }
    return this.password.hasError('minlength') ? 'Mot de passe trop court' : '';
  }

  signIn() {
    if (this.loginForm.invalid || this.submitting) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.submitting = true;
    this.authService.login(this.loginForm.value).subscribe(
      res => {
        this.submitting = false;
        this.handleResponse(res);
      },
      (error) => {
        this.submitting = false;
        this.formError = error && (error.status === 0 || error.status >= 500)
          ? 'Le serveur ne répond pas. Réessayez dans un instant.'
          : 'Adresse e-mail ou mot de passe incorrect.';
      });
  }


  handleResponse(data: any) {
    this.token.handle(data);

    this.account.changeAuthStatus(true);
    const name = this.token.getUserName();
    this.toastr.success(name ? `Bienvenue, ${name}.` : 'Bienvenue.', 'Connexion réussie', { timeOut: 3000 });

    // Doctors and secretaries both start on the dashboard.
    const role = this.token.getUserRole();
    this.router.navigateByUrl(role === 'Medecin' || role === 'Secretaire' ? '/tableau-de-bord' : '/');

  }

}
