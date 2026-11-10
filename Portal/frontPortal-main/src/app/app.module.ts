import { BrowserModule } from '@angular/platform-browser';
import { LOCALE_ID, NgModule } from '@angular/core';
import { registerLocaleData } from '@angular/common';
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import {MatInputModule} from '@angular/material/input';
import {MatSidenavModule} from "@angular/material/sidenav";
import {MatIconModule} from "@angular/material/icon";
import {MatExpansionModule} from '@angular/material/expansion';
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatDatepickerModule} from '@angular/material/datepicker';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { PasswordComponent } from './Authentification/password/password.component';
import { AuthShellComponent } from './Authentification/auth-shell/auth-shell.component';
import { JwtInterceptorService } from './Authentification/jwt-interceptor.service';
import { AuthGuard } from './Authentification/guards/auth.guard';
import { HomeComponent } from './home/home.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {MAT_FORM_FIELD_DEFAULT_OPTIONS, MatFormFieldModule} from "@angular/material/form-field";
import { ResetPasswordComponent } from './Authentification/reset-password/reset-password.component';
import { MatDialogModule } from '@angular/material/dialog';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {MatCardModule} from "@angular/material/card";
import { ToastrModule } from 'ngx-toastr';
import { PageAdminComponent } from './page-admin/page-admin.component';
import {MatMenuModule} from "@angular/material/menu";
import { MatTooltipModule } from '@angular/material/tooltip';
import { CalendrierComponent } from './calendrier/calendrier.component';
import { FichesPatientsComponent } from './fiches-patients/fiches-patients.component';
import { OrdonnancesComponent } from './ordonnances/ordonnances.component';
import { GestionDesPatientsComponent } from './gestion-des-patients/gestion-des-patients.component';
import { StatistiquesComponent } from './statistiques/statistiques.component';
import {MatTableModule} from "@angular/material/table";
import {MatPaginatorIntl, MatPaginatorModule} from "@angular/material/paginator";
import { AjoutpatientComponent } from './gestion-des-patients/ajoutpatient/ajoutpatient.component';
import { DateAdapter, MAT_DATE_LOCALE, MatNativeDateModule } from '@angular/material/core';
import {MatRadioModule} from '@angular/material/radio';
import { RendezVousComponent } from './rendez-vous/rendez-vous.component';
import {MatSelectModule} from '@angular/material/select';
import {MatDividerModule} from '@angular/material/divider';
import { CertificatmedicalComponent } from './certificatmedical/certificatmedical.component';
import { LettreAUnCofrereComponent } from './lettre-a-un-cofrere/lettre-a-un-cofrere.component';
import { CertificatmedicalaptitudeComponent } from './certificatmedicalaptitude/certificatmedicalaptitude.component';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { library } from '@fortawesome/fontawesome-svg-core';
import { faSquare, faCheckSquare } from '@fortawesome/free-solid-svg-icons';
import { LandingComponent } from './landing/landing.component';
import { LandingHeaderComponent } from './landing/sections/landing-header/landing-header.component';
import { LandingHeroComponent } from './landing/sections/landing-hero/landing-hero.component';
import { LandingHowItWorksComponent } from './landing/sections/landing-how-it-works/landing-how-it-works.component';
import { LandingDoctorsComponent } from './landing/sections/landing-doctors/landing-doctors.component';
import { LandingProComponent } from './landing/sections/landing-pro/landing-pro.component';
import { LandingFaqComponent } from './landing/sections/landing-faq/landing-faq.component';
import { LandingFooterComponent } from './landing/sections/landing-footer/landing-footer.component';
import { DemandeRendezVousComponent } from './demande-rendez-vous/demande-rendez-vous.component';
import { CalenderComponent } from './calender/calender.component';
import {FullCalendarModule} from "@fullcalendar/angular";
import dayGridPlugin from '@fullcalendar/daygrid'; // a plugin!
import interactionPlugin from '@fullcalendar/interaction';
import {DialogModule} from 'primeng/dialog';
import {ButtonModule} from "primeng";
import { ConsultationComponent } from './consultation/consultation.component';
import { ListeRendezVousComponent } from './liste-rendez-vous/liste-rendez-vous.component';
import { NavbarComponent } from './navbar/navbar.component';
import { InscriptionComponent } from './inscription/inscription.component';
import { SecretariatComponent } from './profile/secretariat/secretariat.component';
import { PlanningSettingsComponent } from './profile/planning-settings/planning-settings.component';
import localeFr from '@angular/common/locales/fr';
import { PortalDateAdapter } from './portal-date-adapter';
import { ProfileComponent } from './profile/profile.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { AnnulationComponent } from './annulation/annulation.component';
import { PatientFileComponent } from './patient-file/patient-file.component';
import { SalleAttenteComponent } from './salle-attente/salle-attente.component';
import { CaisseComponent } from './caisse/caisse.component';
import { StatsCabinetComponent } from './stats-cabinet/stats-cabinet.component';

registerLocaleData(localeFr);


FullCalendarModule.registerPlugins([ // register FullCalendar plugins
  dayGridPlugin,
  interactionPlugin
]);

@NgModule({
  declarations: [
    AppComponent,
    PasswordComponent,
    AuthShellComponent,
    HomeComponent,
    ResetPasswordComponent,
    PageAdminComponent,
    CalendrierComponent,
    FichesPatientsComponent,
    OrdonnancesComponent,
    GestionDesPatientsComponent,
    StatistiquesComponent,
    AjoutpatientComponent,
    RendezVousComponent,
    CertificatmedicalComponent,
    LettreAUnCofrereComponent,
    CertificatmedicalaptitudeComponent,
    LandingComponent,
    LandingHeaderComponent,
    LandingHeroComponent,
    LandingHowItWorksComponent,
    LandingDoctorsComponent,
    LandingProComponent,
    LandingFaqComponent,
    LandingFooterComponent,
    DemandeRendezVousComponent,
    CalenderComponent,
    ConsultationComponent,
    ListeRendezVousComponent,
    NavbarComponent,
    InscriptionComponent,
    SecretariatComponent,
    PlanningSettingsComponent,
    ProfileComponent,
    DashboardComponent,
    AnnulationComponent,
    PatientFileComponent,
    SalleAttenteComponent,
    CaisseComponent,
    StatsCabinetComponent

  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    BrowserAnimationsModule,
    MatDialogModule,
    MatFormFieldModule,
    FormsModule,
    MatCardModule,
    ReactiveFormsModule,
    // One look and one place for every toast (style in styles.css, "Toasts").
    ToastrModule.forRoot({
      positionClass: 'toast-top-right',
      timeOut: 5000,
      extendedTimeOut: 2000,
      closeButton: true,
      progressBar: true,
      newestOnTop: true,
      preventDuplicates: true,
      maxOpened: 4,
      autoDismiss: true
    }),
    MatInputModule,
    MatSidenavModule,
    MatIconModule,
    MatExpansionModule,
    MatMenuModule,
    MatTooltipModule,
    MatToolbarModule,
    MatTableModule,
    MatPaginatorModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatRadioModule,
    MatSelectModule,
    MatDividerModule,
    FontAwesomeModule,
    FullCalendarModule,
    DialogModule,
    ButtonModule,

  ],
  providers: [
  {provide: LOCALE_ID, useValue: 'fr-FR'},
  {provide: MAT_DATE_LOCALE, useValue: 'fr-FR'},
  {provide: DateAdapter, useClass: PortalDateAdapter},
  {provide: MAT_FORM_FIELD_DEFAULT_OPTIONS, useValue: {appearance: 'fill'}},
  {
    provide: HTTP_INTERCEPTORS,
    useClass: JwtInterceptorService,
    multi: true
  },AuthGuard,
  MatPaginatorIntl
],
  bootstrap: [AppComponent]
})
export class AppModule { }
