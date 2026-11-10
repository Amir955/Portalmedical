/** Static content of the landing page (texts only, no logic). */

export interface LandingItem {
  icon: string;
  title: string;
  text: string;
}

export interface FaqItem {
  question: string;
  answer: string;
}

export const PATIENT_STEPS: LandingItem[] = [
  { icon: 'person_search', title: 'Choisissez un médecin', text: 'Recherchez par nom, spécialité ou ville.' },
  { icon: 'event_available', title: 'Choisissez un créneau', text: 'Seules les heures réellement libres sont proposées.' },
  { icon: 'mark_email_read', title: 'Recevez la confirmation', text: 'Le cabinet confirme par e-mail, avec un lien pour annuler si besoin.' }
];

export const PRO_FEATURES: LandingItem[] = [
  { icon: 'calendar_month', title: 'Agenda et horaires', text: 'Horaires, séance unique, congés : les patients ne voient que vos créneaux libres.' },
  { icon: 'chair', title: 'Salle d’attente', text: 'Arrivées, consultation en cours, patients absents, en temps réel.' },
  { icon: 'folder_shared', title: 'Dossiers patients', text: 'Antécédents, historique des visites et documents au même endroit.' },
  { icon: 'description', title: 'Ordonnances et certificats', text: 'Documents PDF prêts à imprimer, à votre en-tête.' },
  { icon: 'point_of_sale', title: 'Caisse et reçus', text: 'Encaissements, reçus PDF et export pour la comptabilité.' },
  { icon: 'insights', title: 'Statistiques', text: 'Rendez-vous, absences, nouveaux patients et chiffre d’affaires.' },
  { icon: 'support_agent', title: 'Secrétariat', text: 'Ajoutez votre secrétaire : elle gère l’agenda de votre cabinet.' },
  { icon: 'notifications_active', title: 'Rappels et alertes', text: 'Rappel envoyé la veille, alerte à chaque demande ou annulation.' }
];

export const FAQ: FaqItem[] = [
  { question: 'Dois-je créer un compte pour prendre rendez-vous ?',
    answer: 'Non. Choisissez un médecin et un créneau, puis indiquez vos coordonnées. Le cabinet vous répond par e-mail.' },
  { question: 'Comment annuler mon rendez-vous ?',
    answer: 'L’e-mail de confirmation contient un lien d’annulation. Le créneau est aussitôt libéré et le cabinet est prévenu.' },
  { question: 'Mon rendez-vous est-il confirmé tout de suite ?',
    answer: 'Votre demande est envoyée au cabinet, qui la confirme. Vous recevez un e-mail dès qu’elle est acceptée, puis un rappel la veille.' },
  { question: 'Qui peut voir mes informations ?',
    answer: 'Uniquement le cabinet du médecin choisi (le médecin et sa secrétaire). Les échanges passent par une connexion sécurisée.' },
  { question: 'Je suis médecin, comment rejoindre PortalMedical ?',
    answer: 'Créez votre cabinet depuis l’espace professionnel, réglez vos horaires, puis ajoutez votre secrétaire si vous en avez une.' }
];

/** Smooth scroll to a section of the page by its id. */
export function scrollToSection(id: string): void {
  const target = document.getElementById(id);
  if (target) {
    target.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }
}

/** Specialty shown for a doctor (same default as the booking page). */
export function doctorSpecialty(doctor: { specialite?: string }): string {
  return doctor.specialite || 'Médecine générale';
}

export function doctorLocation(doctor: { ville?: string; gouvernerat?: any }): string {
  return doctor.ville || doctor.gouvernerat || '';
}
