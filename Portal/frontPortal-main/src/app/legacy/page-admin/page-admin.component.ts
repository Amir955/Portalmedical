import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { UserEntity } from 'src/models/userEntity';
import { ResetPasswordComponent } from '../../public/auth/reset-password/reset-password.component';
import { TokenService } from '../../core/auth/token.service';
import { AccountService } from '../../core/services/account.service';
import { LoaderService } from '../../core/services/loader.service';

@Component({
  selector: 'app-page-admin',
  templateUrl: './page-admin.component.html',
  styleUrls: ['./page-admin.component.css']
})
export class PageAdminComponent implements OnInit {
  private defaultImage: string = 'assets/img/logo.png';
  public imageUrl: string ;
  panelOpenState = false;
 user: UserEntity;
  selectedFile: File;
  retrievedImage: any;
  base64Data: any;
  initOpts = {
    renderer: 'svg',
    width: 300,
    height: 300
  };
  lang: any;
  constructor(private tokenService: TokenService,
              private router: Router,
              public LoadService: LoaderService,
              private accountService: AccountService,
              private dialog: MatDialog) {
  }

  ngOnInit(): void {
    this.getUserById();
    this.lang= localStorage.getItem('lang') || 'en' ;

  }


  public onError(): void {
    this.retrievedImage = this.defaultImage;
   
  }

   getUserById() {
     this.accountService.getUserByEmail(this.tokenService.getId()).subscribe(data => {
        this.user = data;
       }
     );
   }

redirectTools(){
  this.router.navigateByUrl('/ancien-admin/fiches-patients');
}

redirectLogs(){
  this.router.navigateByUrl('/ancien-admin/ordonnances');
}
redirectDashboard(){
  this.router.navigateByUrl('/ancien-admin/calendrier');
}

redirectFaq(){
  this.router.navigateByUrl('/ancien-admin/patients');
}
redirectStats(){
  this.router.navigateByUrl('/ancien-admin/statistiques');
}

redirectRdv(){
  this.router.navigateByUrl('/ancien-admin/rendez-vous');
}
redirectCertificats(){
  this.router.navigateByUrl('/ancien-admin/certificat-medical');
}
redirectCertificatsmedicalaptitude(){
  this.router.navigateByUrl('/ancien-admin/certificat-aptitude');
}
redirectlettreaunconfrere(){
  this.router.navigateByUrl('/ancien-admin/lettre-confrere');
}
logout() {
  this.tokenService.remove();
  this.router.navigateByUrl('/');
}

openDialogPassword() {
  this.dialog.open(ResetPasswordComponent, {
    height: '40%',
    width: '60%'
  });
}
changeLang(lang){
  localStorage.setItem('lang', lang);
  window.location.reload();
    }
}
