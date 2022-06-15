import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class TokenService {


  set(data: any) {
    localStorage.setItem('token', data.token);
    localStorage.setItem('id', data.id);

  }

  handle(data: any) {
    this.set(data);
  }

  getToken() {
    return localStorage.getItem('token');
  }

  getId() {
    return localStorage.getItem('id');
  }

  remove() {
    localStorage.removeItem('token');
    localStorage.removeItem('id');
  }

  decode(payload: any) {
    return JSON.parse(atob(payload));
  }

  payload(token: string) {
    const payload = token.split('.')[1];
    return this.decode(payload);
  }
getUserName(){
    return this.payload(this.getToken()).name;
}


  getExprirationDate(token: string){
   const date = new Date(0);
   date.setUTCSeconds(this.payload(token).exp);
   return date ;
  }
  getUserRole(){
    return this.payload(this.getToken()).role;
  }

  isValid() {
    const token = this.getToken();
    const id = this.getId();

    if (token) {

      const payload = this.payload(token);
      if (payload) {
        return id == payload.id;
      }
    }
    return false;
  }

  getInfos() {

    const token = this.getToken();

    if (token) {
      const payload = this.payload(token);
      return payload ? payload : null;
    }

    return null;
  }
  loggedIn() {
    return !!this.getToken();
  }
}