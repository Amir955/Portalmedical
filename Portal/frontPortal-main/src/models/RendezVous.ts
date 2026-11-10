import { Days } from "./Days";
import { StateApt } from "./StateApt";
import { Temprdv } from "./Temprdv";
import { UserEntity } from "./userEntity";
import {Patient} from "./Patient";

export class RendezVousEntity{
    idapt:number;
    dateOfApt:Date;
    user:UserEntity;
    days:Days;
    stateApt:StateApt;
  /** State as sent by the backend ("waiting", "confirmed", "refused"). */
  stateapt?: string;
  patientEmail:string;
  patient:Patient;
  patientNom?: string;
  patientPrenom?: string;
  patientTelephone?: string;
  /** yyyy-MM-dd when sent, ISO date when read back. */
  patientDateNaissance?: string;

}
