import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { RendezVousEntity } from 'src/models/RendezVous';
import { TokenService } from '../Authentification/token.service';

@Injectable({
  providedIn: 'root'
})
export class RendezVousService {
  private url='http://localhost:8085/rendezvous/';

  constructor(private Token: TokenService,private http :HttpClient) { }
  
  
  get(idapt: string):Observable<RendezVousEntity>{
    return this.http.get<RendezVousEntity>(this.url + idapt);
  }
  ajouterApt(info:RendezVousEntity):Observable<RendezVousEntity>{
    return  this.http.post<RendezVousEntity>(this.url,info);    
   }
   updateApt(formData: FormData):Observable<RendezVousEntity>{
    return this.http.put<RendezVousEntity>(this.url,formData)
}
deleterdv(id :number){
  return this.http.delete(this.url+id);
}
}
