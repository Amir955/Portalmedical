import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { ResetPassword } from 'src/models/ResetPassword';
import { AuthentificationService } from '../authentification.service';
import { TokenService } from '../token.service';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent implements OnInit {
  password = new FormControl(null, [Validators.required, Validators.minLength(8)]);
  confirmPassword = new FormControl(null, [Validators.required, Validators.minLength(8)]);
  hide = true;
  show = true;

  constructor(private route: ActivatedRoute,
              private router: Router,
              private toast: ToastrService,
              private authentificationService: AuthentificationService,
              private tokenService: TokenService) {
  }

  token: string;
  resetpassword: ResetPassword = new ResetPassword();
  passwordForm: FormGroup = new FormGroup({
    password : this.password,
    confirmPassword: this.confirmPassword
  }, );

  ngOnInit(): void {
    this.token = this.route.snapshot.paramMap.get('token');
    this.checkToken();
  }

  checkToken() {
    if (this.tokenService.getExprirationDate(this.token) < new Date()) {
      window.alert('URL Expired');
      window.close();

    }
  }

  getErrorConfirmPasswordMessage() {
    if (this.confirmPassword.hasError('required')) {
      return 'You must enter a value';
    }
    return this.confirmPassword.hasError('password') ? 'Not password valid' : '';
  }

  getErrorPasswordMessage() {
    if (this.password.hasError('required')) {
      return 'You must enter a value';
    }

    return this.password.hasError('password') ? 'Not password valid' : '';
  }

  confirm() {

    this.resetpassword.token = this.token;
    this.resetpassword.password = this.password.value;
    this.authentificationService.resetPassword(this.resetpassword).subscribe(next => {
      this.toast.success('password reset');
      setTimeout(() => {
          this.router.navigateByUrl('/');
        }
        , 3000);

    });
  }
}
