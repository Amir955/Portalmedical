import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { TokenService } from '../Authentification/token.service';
import { AccountService } from '../services/account.service';
import { UserEntity } from 'src/models/userEntity';
import { GOUVERNORATS } from 'src/models/gouvernorats';

type ProfileSection = 'infos' | 'password' | 'horaires' | 'secretariat';

/** "Mon compte": personal details, password, and (doctors) their secretaries. */
@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  user: UserEntity | null = null;
  editableUser: UserEntity = new UserEntity();
  loading = true;

  section: ProfileSection = 'infos';
  /** Doctors manage their secretaries from this page. */
  isDoctor = false;

  savingInfos = false;
  infosError = '';

  readonly minPasswordLength = 8;
  newPassword = '';
  confirmPassword = '';
  hideNew = true;
  hideConfirm = true;
  savingPassword = false;
  passwordError = '';

  readonly gouvernerats = GOUVERNORATS;

  readonly genres: string[] = ['Homme', 'Femme'];

  constructor(
    private tokenService: TokenService,
    private accountService: AccountService,
    private toastr: ToastrService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  sections: { value: ProfileSection; label: string; hint: string; icon: string }[] = [];

  private buildSections(): void {
    this.sections = [
      { value: 'infos', label: 'Mes informations', hint: 'Identité et coordonnées', icon: 'badge' },
      { value: 'password', label: 'Mot de passe', hint: 'Changer votre mot de passe', icon: 'lock' },
      { value: 'horaires', label: 'Horaires du cabinet', hint: 'Jours, heures et congés', icon: 'schedule' }
    ];
    if (this.isDoctor) {
      this.sections.push({ value: 'secretariat', label: 'Mon secrétariat', hint: 'Comptes de vos secrétaires', icon: 'support_agent' });
    }
  }

  get roleLabel(): string {
    const role = String(this.editableUser.role || this.tokenService.getUserRole() || '');
    return role === 'Medecin' ? 'Médecin' : role === 'Secretaire' ? 'Secrétaire' : role;
  }

  ngOnInit(): void {
    this.isDoctor = this.tokenService.getUserRole() === 'Medecin';
    this.buildSections();
    // Follow ?section= (also when the sidebar links change it while on this page).
    this.route.queryParamMap.subscribe((params) => {
      const requested = params.get('section') as ProfileSection;
      this.section = requested === 'password' || requested === 'horaires' || (requested === 'secretariat' && this.isDoctor)
        ? requested : 'infos';
    });
    this.loadProfile();
  }

  selectSection(section: ProfileSection): void {
    this.section = section;
    this.infosError = '';
    this.passwordError = '';
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { section: section === 'infos' ? null : section },
      replaceUrl: true
    });
  }

  loadProfile(): void {
    const rawId = this.tokenService.getId();
    const userId = rawId ? Number(rawId) : NaN;

    if (!rawId || Number.isNaN(userId)) {
      this.tokenService.handleExpiredSession('Votre session est invalide. Reconnectez-vous pour continuer.');
      return;
    }

    this.loading = true;
    this.accountService.getById(userId).subscribe(
      (user) => {
        this.user = this.sanitizeUser(user);
        this.editableUser = { ...this.user };
        this.loading = false;
      },
      () => {
        this.loading = false;
        this.toastr.error('Votre profil n’a pas pu être chargé. Rechargez la page.', 'Profil');
      }
    );
  }

  getInitials(): string {
    const first = (this.editableUser.prenom || '').trim().charAt(0);
    const last = (this.editableUser.nom || '').trim().charAt(0);
    return `${first}${last}`.toUpperCase() || 'PM';
  }

  saveInfos(): void {
    if (!this.user || this.savingInfos) {
      return;
    }
    if (!(this.editableUser.nom || '').trim() || !(this.editableUser.prenom || '').trim()) {
      this.infosError = 'Le nom et le prénom sont obligatoires.';
      return;
    }

    this.infosError = '';
    this.savingInfos = true;
    // An empty password means "unchanged" for the server.
    this.save({ ...this.user, ...this.editableUser, password: '' }).subscribe(
      (updated) => {
        this.savingInfos = false;
        this.applyUpdate(updated);
        this.toastr.success('Vos informations sont à jour.', 'Profil enregistré', { timeOut: 3000 });
      },
      () => {
        this.savingInfos = false;
        this.infosError = 'Vos modifications n’ont pas pu être enregistrées. Réessayez dans un instant.';
      }
    );
  }

  get hasMinLength(): boolean {
    return this.newPassword.length >= this.minPasswordLength;
  }

  get hasLetterAndDigit(): boolean {
    return /[A-Za-zÀ-ÿ]/.test(this.newPassword) && /\d/.test(this.newPassword);
  }

  get passwordsMatch(): boolean {
    return !!this.confirmPassword && this.newPassword === this.confirmPassword;
  }

  savePassword(): void {
    if (!this.user || this.savingPassword) {
      return;
    }
    if (!this.hasMinLength) {
      this.passwordError = `Le nouveau mot de passe doit contenir au moins ${this.minPasswordLength} caractères.`;
      return;
    }
    if (!this.passwordsMatch) {
      this.passwordError = 'La confirmation ne correspond pas au nouveau mot de passe.';
      return;
    }

    this.passwordError = '';
    this.savingPassword = true;
    this.save({ ...this.user, password: this.newPassword }).subscribe(
      (updated) => {
        this.savingPassword = false;
        this.applyUpdate(updated);
        this.newPassword = '';
        this.confirmPassword = '';
        this.toastr.success('Utilisez-le dès votre prochaine connexion.', 'Mot de passe modifié', { timeOut: 4000 });
      },
      () => {
        this.savingPassword = false;
        this.passwordError = 'Le mot de passe n’a pas pu être modifié. Réessayez dans un instant.';
      }
    );
  }

  clearPasswordError(): void {
    this.passwordError = '';
  }

  goToDashboard(): void {
    this.router.navigateByUrl('/calender');
  }

  private save(payload: UserEntity) {
    return this.accountService.updateUserWithoutImage(payload);
  }

  private applyUpdate(updated: UserEntity): void {
    this.user = this.sanitizeUser(updated);
    this.editableUser = { ...this.user };
  }

  private sanitizeUser(user: UserEntity): UserEntity {
    return {
      ...user,
      password: ''
    };
  }
}
