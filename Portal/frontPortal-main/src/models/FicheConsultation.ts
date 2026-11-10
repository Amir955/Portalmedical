import { Patient } from "./Patient";
import { UserEntity } from "./userEntity";

export class FicheConsultation {
  idFiche: number;
  datedecreation: string | Date;
  matriculecnam: string;
  causedeconsultation: string;
  examen: string;
  suivie: string;
  patient: Patient;
  user: UserEntity;
}
