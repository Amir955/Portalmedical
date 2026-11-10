import { Component, Input, OnChanges } from '@angular/core';
import { Router } from '@angular/router';
import { UserEntity } from '../../../../../models/userEntity';
import { doctorLocation, doctorSpecialty, scrollToSection } from '../../landing.data';

/** Hero for patients, with the doctor search (opens the booking page with these filters). */
@Component({
  selector: 'app-landing-hero',
  templateUrl: './landing-hero.component.html',
  styleUrls: ['../../landing-shared.css', './landing-hero.component.css']
})
export class LandingHeroComponent implements OnChanges {
  @Input() doctors: UserEntity[] = [];

  readonly scrollTo = scrollToSection;
  query = '';
  specialite = '';
  ville = '';
  specialites: string[] = [];
  villes: string[] = [];

  constructor(private router: Router) {
  }

  ngOnChanges(): void {
    this.specialites = this.unique((this.doctors || []).map(doctorSpecialty));
    this.villes = this.unique((this.doctors || []).map(doctorLocation));
  }

  search(): void {
    const queryParams: { [key: string]: string } = {};
    if (this.query.trim()) {
      queryParams.q = this.query.trim();
    }
    if (this.specialite) {
      queryParams.specialite = this.specialite;
    }
    if (this.ville) {
      queryParams.ville = this.ville;
    }
    this.router.navigate(['/prendre-rendez-vous'], { queryParams });
  }

  private unique(values: string[]): string[] {
    return Array.from(new Set(values.filter((v) => !!v))).sort((a, b) => a.localeCompare(b));
  }
}
