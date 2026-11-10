import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {RendezVousEntity} from 'src/models/RendezVous';
import {TokenService} from '../auth/token.service';

@Injectable({
  providedIn: 'root'
})
export class RendezVousService {
  private url = 'http://localhost:8085/rendezvous/';

  constructor(private Token: TokenService, private http: HttpClient) {
  }


  getbyid(idapt: number): Observable<RendezVousEntity> {
    return this.http.get<RendezVousEntity>(this.url+'getById/' + idapt);
  }

  ajouterApt(rdv: RendezVousEntity): Observable<RendezVousEntity> {
    return this.http.post<RendezVousEntity>(this.url+'ajouterApt', rdv);
  }

  updateApt(rdv: RendezVousEntity): Observable<RendezVousEntity> {
    return this.http.put<RendezVousEntity>(this.url+'modifier', rdv)
  }

  deleterdv(id: number) {
    return this.http.delete(this.url + id);
  }

  demandeRDV(rdv: RendezVousEntity): Observable<{ idapt: number }>{
    return this.http.post<{ idapt: number }>(this.url+'demandeRDV',rdv);
  }

  /** Public: times (HH:mm) already taken for a doctor on a day (yyyy-MM-dd). */
  getTakenSlots(medecinId: number, date: string): Observable<string[]> {
    return this.http.get<string[]>(this.url + 'creneauxPris/' + medecinId, { params: { date } });
  }

  /** Waiting room: arrive, en_consultation, termine, absent, or 'prevu' to reset. */
  setStatut(id: number, statut: string): Observable<RendezVousEntity> {
    return this.http.put<RendezVousEntity>(this.url + id + '/statut', { statut });
  }

  /** Confirmed appointments for the calendar, same scope as getDemandes. */
  getAgenda(): Observable<RendezVousEntity[]> {
    return this.http.get<RendezVousEntity[]>(this.url + 'agenda');
  }

  /** Pending requests for the signed-in user (a doctor only gets their own). */
  /** Appointments the patients cancelled in the last 7 days, newest first. */
  getAnnulations(): Observable<RendezVousEntity[]> {
    return this.http.get<RendezVousEntity[]>(this.url + 'annulations');
  }

  getDemandes(): Observable<RendezVousEntity[]> {
    return this.http.get<RendezVousEntity[]>(this.url + 'demandes');
  }

  getAllrdv():Observable<RendezVousEntity[]>{
    return this.http.get<RendezVousEntity[]>(this.url)
  }
  getrdvByState(state : string):Observable<RendezVousEntity[]>{
    return this.http.get<RendezVousEntity[]>(this.url+'getByState/'+state)
}
refuserRdv(rdv:RendezVousEntity): Observable<RendezVousEntity>{
  return this.http.put<RendezVousEntity>(this.url+'refuser', rdv)
}
  confirmerRdv(rdv:RendezVousEntity): Observable<RendezVousEntity>{
    return this.http.put<RendezVousEntity>(this.url+'confirmer', rdv)
  }

}
