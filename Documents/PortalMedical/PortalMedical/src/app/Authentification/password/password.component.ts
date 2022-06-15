import { Component, OnInit } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { AuthentificationService } from '../authentification.service';

@Component({
  selector: 'app-password',
  templateUrl: './password.component.html',
  styleUrls: ['./password.component.css']
})
export class PasswordComponent{

  email = new FormControl('', [Validators.required, Validators.email]);
  constructor( private authService: AuthentificationService,
               private toast: ToastrService) { }


  getErrorEmailMessage() {
    if (this.email.hasError('required')) {
      return 'Vous devez entrer une valeur';
    }

    return this.email.hasError('email') ? 'E-mail non validé' : '';
  }
  sendEmail(){
    this.authService.forgotPassword(this.email.value).subscribe(() =>
      this.toast.success('E-mail envoyer')
    , () => {
      this.toast.success('E-mail envoyer');
    });
  }
}