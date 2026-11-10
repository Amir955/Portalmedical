import { Gouvernerats } from "./Gouvernerats";
import { Sexe } from "./Sexe";

export class Patient{
  id :number;
  nom:string;
  prenom:string;
  email:string;
  numtelephone:string;
  age:Date;
  cin:string;
 password:string;
 creationDate : Date;
 ville :string;
 codepostal:number;
 gouvernerat:Gouvernerats;
 pays:string;
 sexe:Sexe;

}
