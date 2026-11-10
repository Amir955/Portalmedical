import { Component, HostListener, OnDestroy, OnInit } from '@angular/core';
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
  /** Top bar slides away while scrolling down and comes back when scrolling up. */
  topHidden = false;
  private lastScrollY = 0;
  /** Icons-only sidebar on large screens (remembered in this browser). */
  collapsed = false;
  /** Pending appointment requests for the bell, most recently received first. */
  pending: RendezVousEntity[] = [];
  private readIds = new Set<number>();
  /** Appointments cancelled by patients in the last 7 days. */
  cancelled: RendezVousEntity[] = [];
  private cancelReadIds = new Set<number>();
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
    this.collapsed = this.readCollapsed();
    this.applySidebarWidth();
    this.subscriptions = [
      this.notifications.pending$.subscribe((list) => this.pending = list),
      this.notifications.read$.subscribe((ids) => this.readIds = ids),
      this.notifications.cancelled$.subscribe((list) => this.cancelled = list),
      this.notifications.cancelRead$.subscribe((ids) => this.cancelReadIds = ids)
    ];
    this.notifications.start();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((subscription) => subscription.unsubscribe());
  }

  get unreadCount(): number {
    return this.pending.filter((rdv) => !this.readIds.has(rdv.idapt)).length + this.unreadCancelled;
  }

  get unreadCancelled(): number {
    return this.cancelled.filter((rdv) => !this.cancelReadIds.has(rdv.idapt)).length;
  }

  isCancelUnread(rdv: RendezVousEntity): boolean {
    return !this.cancelReadIds.has(rdv.idapt);
  }

  cancelledName(rdv: RendezVousEntity): string {
    return DemandesNotificationService.patientName(rdv);
  }

  /** A cancellation counts as read once opened; the calendar shows the freed slot. */
  openCancelled(rdv: RendezVousEntity): void {
    this.notifications.markCancelRead(rdv);
    this.router.navigateByUrl('/calender');
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
    if (this.collapsed) {
      this.toggleCollapsed();
      this.consultationOpen = true;
      return;
    }
    this.consultationOpen = !this.consultationOpen;
  }

  @HostListener('window:scroll')
  onScroll(): void {
    const y = window.pageYOffset || document.documentElement.scrollTop || 0;
    if (this.menuOpen || y < 100) {
      this.topHidden = false;
    } else if (y > this.lastScrollY + 6) {
      this.topHidden = true;
    } else if (y < this.lastScrollY - 6) {
      this.topHidden = false;
    }
    this.lastScrollY = y;
  }

  toggleCollapsed(): void {
    this.collapsed = !this.collapsed;
    try {
      localStorage.setItem('pm-sidebar-collapsed', this.collapsed ? '1' : '0');
    } catch (e) {
      // Storage unavailable: the choice lasts for this page only.
    }
    this.applySidebarWidth();
  }

  private readCollapsed(): boolean {
    try {
      return localStorage.getItem('pm-sidebar-collapsed') === '1';
    } catch (e) {
      return false;
    }
  }

  /** The page content is offset by this variable (styles.css, "App layout"). */
  private applySidebarWidth(): void {
    document.documentElement.style.setProperty('--pm-sidebar-width', this.collapsed ? '84px' : '264px');
  }

  logout() {
    this.notifications.stop();
    this.tokenService.remove();
    this.router.navigateByUrl('/login');
  }

}
