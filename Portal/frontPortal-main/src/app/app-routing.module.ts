import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { CalendrierComponent } from './calendrier/calendrier.component';
import { CertificatmedicalComponent } from './certificatmedical/certificatmedical.component';
import { CertificatmedicalaptitudeComponent } from './certificatmedicalaptitude/certificatmedicalaptitude.component';
import { ConsultationComponent } from './consultation/consultation.component';
import { FichesPatientsComponent } from './fiches-patients/fiches-patients.component';
import { GestionDesPatientsComponent } from './gestion-des-patients/gestion-des-patients.component';
import { HomeComponent } from './home/home.component';
import { LettreAUnCofrereComponent } from './lettre-a-un-cofrere/lettre-a-un-cofrere.component';
import { OrdonnancesComponent } from './ordonnances/ordonnances.component';
import { PageAdminComponent } from './page-admin/page-admin.component';
import { RendezVousComponent } from './rendez-vous/rendez-vous.component';
import { StatistiquesComponent } from './statistiques/statistiques.component';
import {ResetPasswordComponent} from "./Authentification/reset-password/reset-password.component";
import {AcceuilComponent} from "./acceuil/acceuil.component";
import {DemandeRendezVousComponent} from "./demande-rendez-vous/demande-rendez-vous.component";
import {CalenderComponent} from "./calender/calender.component";
import {ListeRendezVousComponent} from "./liste-rendez-vous/liste-rendez-vous.component";
import { InscriptionComponent } from './inscription/inscription.component';
import { AuthGuard } from './Authentification/guards/auth.guard';
import { DoctorGuard } from './Authentification/guards/doctor.guard';
import { ProfileComponent } from './profile/profile.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { AnnulationComponent } from './annulation/annulation.component';
import { PatientFileComponent } from './patient-file/patient-file.component';

const routes: Routes = [
  { path: '', component: AcceuilComponent},
  { path: 'login', component: HomeComponent},
  { path: 'inscription', component: InscriptionComponent },
  { path: 'demandeRDV', component: DemandeRendezVousComponent},
  { path: 'annuler/:token', component: AnnulationComponent },
  { path: 'patients/:id', component: PatientFileComponent, canActivate: [AuthGuard] },
  { path: 'tableau-de-bord', component: DashboardComponent, canActivate: [AuthGuard] },
  {path:'calender', component: CalenderComponent, canActivate: [AuthGuard]},
  {path:'listeRDV', component: ListeRendezVousComponent, canActivate: [AuthGuard]},
  { path: 'profil', component: ProfileComponent, canActivate: [AuthGuard] },
  { path: 'consultation', component: ConsultationComponent, canActivate: [AuthGuard, DoctorGuard], data: { section: 'fiche' } },
  { path: 'fichespatients', component: ConsultationComponent, canActivate: [AuthGuard, DoctorGuard], data: { section: 'fiche' } },
  { path: 'ordonnances', component: ConsultationComponent, canActivate: [AuthGuard, DoctorGuard], data: { section: 'ordonnance' } },
  { path: 'certificatmedical', component: ConsultationComponent, canActivate: [AuthGuard, DoctorGuard], data: { section: 'certificat-medical' } },
  { path: 'certificatmedicalaptitude', component: ConsultationComponent, canActivate: [AuthGuard, DoctorGuard], data: { section: 'certificat-aptitude' } },
  { path: 'lettreaunconfrere', component: ConsultationComponent, canActivate: [AuthGuard, DoctorGuard], data: { section: 'lettre' } },
  {path:'resetPassword/:token', component: ResetPasswordComponent},
  { path: 'gestiondespatients', component: GestionDesPatientsComponent, canActivate: [AuthGuard] },

   {path: 'pageadmin', component: PageAdminComponent, canActivate: [AuthGuard], children: [
      { path: 'calendrier', component: CalendrierComponent, canActivate: [AuthGuard] },
      { path: 'fichespatients', component: ConsultationComponent, canActivate: [AuthGuard, DoctorGuard], data: { section: 'fiche' } },
      { path: 'ordonnances', component: ConsultationComponent, canActivate: [AuthGuard, DoctorGuard], data: { section: 'ordonnance' } },
      { path: 'gestiondespatients', component: GestionDesPatientsComponent, canActivate: [AuthGuard] },
      { path: 'statistiques', component: StatistiquesComponent, canActivate: [AuthGuard] },
      { path: 'Rendez-Vous', component: RendezVousComponent, canActivate: [AuthGuard]},
      {path: 'certificatmedical', component: ConsultationComponent, canActivate: [AuthGuard, DoctorGuard], data: { section: 'certificat-medical' }},
      {path: 'Certificatmedicalaptitude', component: ConsultationComponent, canActivate: [AuthGuard, DoctorGuard], data: { section: 'certificat-aptitude' }},
      {path: 'lettreaunconfrere', component: ConsultationComponent, canActivate: [AuthGuard, DoctorGuard], data: { section: 'lettre' }}

    ]},

];
@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
