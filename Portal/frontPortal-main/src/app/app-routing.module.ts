import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { CalendrierComponent } from './legacy/calendrier/calendrier.component';
import { CertificatmedicalComponent } from './legacy/certificatmedical/certificatmedical.component';
import { CertificatmedicalaptitudeComponent } from './legacy/certificatmedicalaptitude/certificatmedicalaptitude.component';
import { ConsultationComponent } from './medical/consultation/consultation.component';
import { FichesPatientsComponent } from './legacy/fiches-patients/fiches-patients.component';
import { GestionDesPatientsComponent } from './cabinet/gestion-des-patients/gestion-des-patients.component';
import { HomeComponent } from './public/auth/login/home.component';
import { LettreAUnCofrereComponent } from './legacy/lettre-a-un-cofrere/lettre-a-un-cofrere.component';
import { OrdonnancesComponent } from './legacy/ordonnances/ordonnances.component';
import { PageAdminComponent } from './legacy/page-admin/page-admin.component';
import { RendezVousComponent } from './legacy/rendez-vous/rendez-vous.component';
import { StatistiquesComponent } from './legacy/statistiques/statistiques.component';
import {ResetPasswordComponent} from "./public/auth/reset-password/reset-password.component";
import { LandingComponent } from './public/landing/landing.component';
import {DemandeRendezVousComponent} from "./public/demande-rendez-vous/demande-rendez-vous.component";
import {CalenderComponent} from "./cabinet/calender/calender.component";
import {ListeRendezVousComponent} from "./cabinet/liste-rendez-vous/liste-rendez-vous.component";
import { InscriptionComponent } from './public/auth/inscription/inscription.component';
import { AuthGuard } from './core/auth/guards/auth.guard';
import { DoctorGuard } from './core/auth/guards/doctor.guard';
import { ProfileComponent } from './cabinet/profile/profile.component';
import { DashboardComponent } from './cabinet/dashboard/dashboard.component';
import { AnnulationComponent } from './public/annulation/annulation.component';
import { PatientFileComponent } from './cabinet/patient-file/patient-file.component';
import { SalleAttenteComponent } from './cabinet/salle-attente/salle-attente.component';
import { CaisseComponent } from './cabinet/caisse/caisse.component';
import { StatsCabinetComponent } from './cabinet/stats-cabinet/stats-cabinet.component';

/**
 * URLs: lowercase, French, words separated by hyphens.
 * The old addresses redirect to the new ones (links already sent by e-mail keep working).
 */
const routes: Routes = [
  // Public
  { path: '', component: LandingComponent },
  { path: 'prendre-rendez-vous', component: DemandeRendezVousComponent },
  { path: 'annuler/:token', component: AnnulationComponent },
  { path: 'connexion', component: HomeComponent },
  { path: 'inscription', component: InscriptionComponent },
  { path: 'reinitialiser-mot-de-passe/:token', component: ResetPasswordComponent },

  // Practice (doctor and secretary)
  { path: 'tableau-de-bord', component: DashboardComponent, canActivate: [AuthGuard] },
  { path: 'agenda', component: CalenderComponent, canActivate: [AuthGuard] },
  { path: 'demandes', component: ListeRendezVousComponent, canActivate: [AuthGuard] },
  { path: 'salle-attente', component: SalleAttenteComponent, canActivate: [AuthGuard] },
  { path: 'patients', component: GestionDesPatientsComponent, canActivate: [AuthGuard] },
  { path: 'patients/:id', component: PatientFileComponent, canActivate: [AuthGuard] },
  { path: 'caisse', component: CaisseComponent, canActivate: [AuthGuard] },
  { path: 'statistiques', component: StatsCabinetComponent, canActivate: [AuthGuard] },
  { path: 'parametres', component: ProfileComponent, canActivate: [AuthGuard] },

  // Medical (doctor only)
  { path: 'consultation', component: ConsultationComponent, canActivate: [AuthGuard, DoctorGuard], data: { section: 'fiche' } },
  { path: 'consultation/fiches-patients', component: ConsultationComponent, canActivate: [AuthGuard, DoctorGuard], data: { section: 'fiche' } },
  { path: 'consultation/ordonnances', component: ConsultationComponent, canActivate: [AuthGuard, DoctorGuard], data: { section: 'ordonnance' } },
  { path: 'consultation/certificat-medical', component: ConsultationComponent, canActivate: [AuthGuard, DoctorGuard], data: { section: 'certificat-medical' } },
  { path: 'consultation/certificat-aptitude', component: ConsultationComponent, canActivate: [AuthGuard, DoctorGuard], data: { section: 'certificat-aptitude' } },
  { path: 'consultation/lettre-confrere', component: ConsultationComponent, canActivate: [AuthGuard, DoctorGuard], data: { section: 'lettre' } },

  // Old administration screens (kept, no longer linked)
  { path: 'ancien-admin', component: PageAdminComponent, canActivate: [AuthGuard], children: [
    { path: 'calendrier', component: CalendrierComponent, canActivate: [AuthGuard] },
    { path: 'fiches-patients', component: ConsultationComponent, canActivate: [AuthGuard, DoctorGuard], data: { section: 'fiche' } },
    { path: 'ordonnances', component: ConsultationComponent, canActivate: [AuthGuard, DoctorGuard], data: { section: 'ordonnance' } },
    { path: 'patients', component: GestionDesPatientsComponent, canActivate: [AuthGuard] },
    { path: 'statistiques', component: StatistiquesComponent, canActivate: [AuthGuard] },
    { path: 'rendez-vous', component: RendezVousComponent, canActivate: [AuthGuard] },
    { path: 'certificat-medical', component: ConsultationComponent, canActivate: [AuthGuard, DoctorGuard], data: { section: 'certificat-medical' } },
    { path: 'certificat-aptitude', component: ConsultationComponent, canActivate: [AuthGuard, DoctorGuard], data: { section: 'certificat-aptitude' } },
    { path: 'lettre-confrere', component: ConsultationComponent, canActivate: [AuthGuard, DoctorGuard], data: { section: 'lettre' } }
  ] },

  // Old addresses → new ones
  { path: 'demandeRDV', redirectTo: 'prendre-rendez-vous' },
  { path: 'login', redirectTo: 'connexion' },
  { path: 'resetPassword/:token', redirectTo: 'reinitialiser-mot-de-passe/:token' },
  { path: 'calender', redirectTo: 'agenda' },
  { path: 'listeRDV', redirectTo: 'demandes' },
  { path: 'gestiondespatients', redirectTo: 'patients' },
  { path: 'profil', redirectTo: 'parametres' },
  { path: 'fichespatients', redirectTo: 'consultation/fiches-patients' },
  { path: 'ordonnances', redirectTo: 'consultation/ordonnances' },
  { path: 'certificatmedical', redirectTo: 'consultation/certificat-medical' },
  { path: 'certificatmedicalaptitude', redirectTo: 'consultation/certificat-aptitude' },
  { path: 'lettreaunconfrere', redirectTo: 'consultation/lettre-confrere' },
  { path: 'pageadmin', redirectTo: 'ancien-admin' },

  { path: '**', redirectTo: '' }
];
@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
