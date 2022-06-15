import { Component, Inject, OnInit } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ToastrService } from 'ngx-toastr';
import { AccountService } from 'src/app/services/account.service';
import { Gouvernerats } from 'src/models/Gouvernerats';
import { UserEntity } from 'src/models/userEntity';

@Component({
  selector: 'app-updatepatient',
  templateUrl: './updatepatient.component.html',
  styleUrls: ['./updatepatient.component.css']
})
export class UpdatepatientComponent implements OnInit {


  email = new FormControl('', [Validators.required, Validators.email]);
  hide = true;
  close=false;
  message: File;
  photo: File;
  password=new FormControl(null, [Validators.required, Validators.minLength(8)])
  base64Data: Int8Array;
  retrievedImage: string;
  constructor(private accountService:AccountService,
              private toast:ToastrService,
              @Inject(MAT_DIALOG_DATA) public user: UserEntity) {}


  ngOnInit(): void {
  }

  getErrorPasswordMessage() {
    if (this.password.hasError('required')) {
      return 'Vous devez entrer une valeur';
    }

    return this.password.hasError('password') ? 'Not password valid' : '';
  }

  getErrorMessage() {
    if (this.email.hasError('required')) {
      return 'Vous devez entrer une valeur';
    }

    return this.email.hasError('email') ? 'Not a valid email' : '';
  }

  onSelectFile(event: any) {
    if (event.target.files.length > 0) {
      this.photo = event.target.files[0];
      this.message = this.photo;
      const reader = new FileReader();
      reader.onload = () => {
        this.retrievedImage = reader.result as string;
      }
      reader.readAsDataURL(this.photo)

    }
  }
  updateUser() {

  
      this.accountService.updateUserWithoutImage(this.user).subscribe(res => {
          this.toast.success('Data update successfully ', 'UPDATE', {
            timeOut: 3000,
            positionClass: 'toast-bottom-right'
          });
          this.close=true;
        }

      )

  }
  
}
