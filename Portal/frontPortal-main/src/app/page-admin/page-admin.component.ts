import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { UserEntity } from 'src/models/userEntity';
import { ResetPasswordComponent } from '../Authentification/reset-password/reset-password.component';
import { TokenService } from '../Authentification/token.service';
import { AccountService } from '../services/account.service';
import { LoaderService } from '../services/loader.service';

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
  this.router.navigateByUrl('/pageadmin/FichesPatients');
}

redirectLogs(){
  this.router.navigateByUrl('/pageadmin/ordonnances');
}
redirectDashboard(){
  this.router.navigateByUrl('/pageadmin/calendrier');
}

redirectFaq(){
  this.router.navigateByUrl('/pageadmin/gestiondespatients');
}
redirectStats(){
  this.router.navigateByUrl('/pageadmin/statistiques');
}

redirectRdv(){
  this.router.navigateByUrl('/pageadmin/RendezVous');
}
redirectCertificats(){
  this.router.navigateByUrl('/pageadmin/certificatmedical');
}
redirectCertificatsmedicalaptitude(){
  this.router.navigateByUrl('/pageadmin/Certificatmedicalaptitude');
}
redirectlettreaunconfrere(){
  this.router.navigateByUrl('/pageadmin/lettreaunconfrere');
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
