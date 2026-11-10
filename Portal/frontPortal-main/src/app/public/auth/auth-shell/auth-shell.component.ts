import { Component, Input } from '@angular/core';

/**
 * Split layout shared by the account pages (login, sign-up, new password):
 * brand panel on the left, the page's form projected on the right.
 * Styles live in styles.css under "Account pages".
 */
@Component({
  selector: 'app-auth-shell',
  templateUrl: './auth-shell.component.html'
})
export class AuthShellComponent {
  @Input() eyebrow = 'Portail médical';
  @Input() heading = 'Votre santé, accessible simplement';
  @Input() text = 'Rendez-vous, consultations et documents médicaux réunis dans un espace sécurisé.';
  /** Wider form column, used by the sign-up form. */
  @Input() wide = false;

  readonly year = new Date().getFullYear();

  readonly points = [
    { icon: 'event_available', label: 'Rendez-vous en ligne, confirmés par e-mail' },
    { icon: 'folder_shared', label: 'Dossiers, ordonnances et certificats au même endroit' },
    { icon: 'verified_user', label: 'Accès sécurisé et données protégées' }
  ];
}
