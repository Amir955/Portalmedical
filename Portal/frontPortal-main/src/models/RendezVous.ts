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
  /** Day-of-visit status: null (planned), arrive, en_consultation, termine, absent. */
  statutVisite?: 'arrive' | 'en_consultation' | 'termine' | 'absent' | null;
  heureArrivee?: string | null;
  /** When the patient cancelled with their link. */
  dateAnnulation?: string | null;

}
