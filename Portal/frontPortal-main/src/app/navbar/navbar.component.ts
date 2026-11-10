import { Component, OnDestroy, OnInit } from '@angular/core';
import {TokenService} from "../Authentification/token.service";
import {Router} from "@angular/router";
import { Subscription } from 'rxjs';
import { AccountService } from '../services/account.service';
import { DemandesNotificationService } from '../services/demandes-notification.service';
import { RendezVousEntity } from 'src/models/RendezVous';
import { UserEntity } from 'src/models/userEntity';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit, OnDestroy {
  user: UserEntity | null = null;
  /** Medical records (Consultation menu) are reserved to doctors. */
  readonly isDoctor = this.tokenService.getUserRole() === 'Medecin';
  readonly roleLabel = this.isDoctor ? 'Médecin' : this.tokenService.getUserRole() === 'Secretaire' ? 'Secrétaire' : '';
  /** Sidebar drawer on small screens. */
  menuOpen = false;
  /** Consultation sub-menu in the sidebar (open on its own pages). */
  consultationOpen = false;
  /** Pending appointment requests for the bell, most recently received first. */
  pending: RendezVousEntity[] = [];
  private readIds = new Set<number>();
  private subscriptions: Subscription[] = [];
  consultationLinks = [
    { label: 'Vue générale', route: '/consultation' },
    { label: 'Fiches patients', route: '/fichespatients' },
    { label: 'Ordonnances', route: '/ordonnances' },
    { label: 'Certificats médicaux', route: '/certificatmedical' },
    { label: "Certificats d'aptitude", route: '/certificatmedicalaptitude' },
    { label: 'Lettre à un confrère', route: '/lettreaunconfrere' }
  ];

  constructor(private tokenService: TokenService,
              private accountService: AccountService,
              private notifications: DemandesNotificationService,
              private router: Router) { }

  ngOnInit(): void {
    this.loadUser();
    this.consultationOpen = this.consultationActive;
    this.subscriptions = [
      this.notifications.pending$.subscribe((list) => this.pending = list),
      this.notifications.read$.subscribe((ids) => this.readIds = ids)
    ];
    this.notifications.start();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((subscription) => subscription.unsubscribe());
  }

  get unreadCount(): number {
    return this.pending.filter((rdv) => !this.readIds.has(rdv.idapt)).length;
  }

  isUnread(rdv: RendezVousEntity): boolean {
    return !this.readIds.has(rdv.idapt);
  }

  requestName(rdv: RendezVousEntity): string {
    return [rdv.patientPrenom, rdv.patientNom].filter((part) => !!part).join(' ') || rdv.patientEmail;
  }

  /** Opens the list from one request: that request counts as read. */
  openRequest(rdv: RendezVousEntity): void {
    this.notifications.markRead(rdv);
    this.openRequests();
  }

  markAllRead(event: Event): void {
    // Keep the menu open so the dots disappear in place.
    event.stopPropagation();
    this.notifications.markAllRead();
  }

  openRequests(): void {
    this.router.navigateByUrl('/listeRDV');
  }

  loadUser(): void {
    const rawId = this.tokenService.getId();
    const userId = rawId ? Number(rawId) : NaN;

    if (!rawId || Number.isNaN(userId)) {
      return;
    }

    this.accountService.getById(userId).subscribe({
      next: (user) => this.user = user,
      error: () => this.user = null
    });
  }

  getUserInitials(): string {
    const first = this.user?.prenom?.trim()?.charAt(0) || '';
    const last = this.user?.nom?.trim()?.charAt(0) || '';
    return `${first}${last}`.toUpperCase() || 'PM';
  }

  openProfile(): void {
    this.router.navigateByUrl('/profil');
  }

  /** Highlights "Consultation" on any of its pages. */
  get consultationActive(): boolean {
    const path = this.router.url.split('?')[0];
    return this.consultationLinks.some((link) => path === link.route);
  }

  get displayName(): string {
    return this.user ? `${this.user.prenom || ''} ${this.user.nom || ''}`.trim() : '';
  }

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
  }

  closeMenu(): void {
    this.menuOpen = false;
  }

  toggleConsultation(): void {
    this.consultationOpen = !this.consultationOpen;
  }

  logout() {
    this.notifications.stop();
    this.tokenService.remove();
    this.router.navigateByUrl('/login');
  }

}
