import { Component, Inject, OnInit } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AccountService } from 'src/app/services/account.service';
import { UserEntity } from 'src/models/userEntity';

@Component({
  selector: 'app-ajoutpatient',
  templateUrl: './ajoutpatient.component.html',
  styleUrls: ['./ajoutpatient.component.css']
})
export class AjoutpatientComponent implements OnInit {
  email = new FormControl('', [Validators.required, Validators.email]);
  role = new FormControl('');
  user: UserEntity = new UserEntity();
  hide = true;



  constructor(private accountService: AccountService,
    private toast: ToastrService,
    private route: Router
    
  ) {
  }
  ngOnInit(): void {
  }









addUser() {


  this.accountService.addUserWithoutImage1(this.user)
    .subscribe(res => {
      this.toast.success('User registered successfully!', '', {
        timeOut: 3000,
        positionClass: 'toast-bottom-right'
      });
    },
      error => {
        this.toast.error(error.error.message, 'something went wrong!!', {
          timeOut: 3000,
          positionClass: 'toast-bottom-left'
        })
      });

}
  
}



