import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ResetPassword } from 'src/models/ResetPassword';

@Injectable({
  providedIn: 'root'
})
export class AuthentificationService {
  
  constructor(private http: HttpClient) { }

  public login(auth: {email: string, password: string}): Observable <any> {
    return this.http.post<any>(`http://localhost:8085/users/login`, auth);
  }
  public forgotPassword(email: string): Observable<any> {
    return this.http.post<any>('http://localhost:8085/user/sendEmail', email);
  }
  public resetPassword(resetPassword: ResetPassword): Observable<string>{
    return this.http.post<string>('http://localhost:8085/user/resetPassword', resetPassword) ;
  }
}