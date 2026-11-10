import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {UserEntity} from "../../../models/userEntity";
import {Patient} from "../../../models/Patient";
import {RendezVousEntity} from "../../../models/RendezVous";

@Injectable({
  providedIn: 'root'
})
export class PatientService {
  private url = 'http://localhost:8085/patient/';
  constructor(private http: HttpClient) { }

  getAllpatient():Observable<Patient[]>{
    return  this.http.get<Patient[]>(this.url+'getall')
  }
  getbyid(id: number):Observable<Patient>{
    return  this.http.get<Patient>(this.url+'getbyid/'+id)
  }
  ajoutPatient(patient:Patient):Observable<Patient>{
    return this.http.post<Patient>(this.url+'ajout',patient)
  }
  modifierPatient(patient:Patient):Observable<Patient>{
    return this.http.put<Patient>(this.url+'update',patient)
  }
  supprimerPatient(id:number):Observable<Patient>{
    return this.http.delete<Patient>(this.url+'delete/'+ id)
  }
}
