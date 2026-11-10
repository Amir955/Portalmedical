import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { TokenService } from '../token.service';

/** Medical records (consultations, ordonnances, certificats…) are reserved to doctors. */
@Injectable({
  providedIn: 'root'
})
export class DoctorGuard implements CanActivate {

  constructor(private tokenService: TokenService,
              private toastr: ToastrService,
              private router: Router) {
  }

  canActivate(): boolean | UrlTree {
    if (this.tokenService.getUserRole() === 'Medecin') {
      return true;
    }
    this.toastr.info('Les consultations et documents médicaux sont réservés au médecin.', 'Accès réservé');
    return this.router.parseUrl('/tableau-de-bord');
  }
}
