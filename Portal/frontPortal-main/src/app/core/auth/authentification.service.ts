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
  /** The server answers with plain text ("email send"), not JSON. */
  public forgotPassword(email: string): Observable<string> {
    return this.http.post('http://localhost:8085/user/sendEmail', email, { responseType: 'text' });
  }
  /** Empty body on success. */
  public resetPassword(resetPassword: ResetPassword): Observable<string>{
    return this.http.post('http://localhost:8085/user/resetPassword', resetPassword, { responseType: 'text' });
  }
}