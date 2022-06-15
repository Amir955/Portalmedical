import { NumberFormatStyle } from "@angular/common";
import { Gouvernerats } from "./Gouvernerats";
import { Role } from "./Role";
import { Sexe } from "./Sexe";

export class UserEntity{
    id:number;
    nom:string;
    prenom:string;
    age:Date;
    cin:string;
   email:string;
   password:string;
   role :Role;
   creationDate : Date;
   ville :string;
   codepostal:number;
   gouvernerat:Gouvernerats;
   pays:string;
   sexe:Sexe;
   numtelephone:number ;
  }