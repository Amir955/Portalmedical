import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { CalendrierComponent } from './calendrier/calendrier.component';
import { FichesPatientsComponent } from './fiches-patients/fiches-patients.component';
import { GestionDesPatientsComponent } from './gestion-des-patients/gestion-des-patients.component';
import { HomeComponent } from './home/home.component';
import { OrdonnancesComponent } from './ordonnances/ordonnances.component';
import { PageAdminComponent } from './page-admin/page-admin.component';
import { StatistiquesComponent } from './statistiques/statistiques.component';

const routes: Routes = [
  { path: '', component: HomeComponent },
  
    {path: 'pageadmin', component: PageAdminComponent, children: [
      { path: 'calendrier', component: CalendrierComponent },
      { path: 'fichespatients', component: FichesPatientsComponent },
      { path: 'ordonnances', component: OrdonnancesComponent },
      { path: 'gestiondespatients', component: GestionDesPatientsComponent },
      { path: 'statistiques', component: StatistiquesComponent }

    ]},

];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
