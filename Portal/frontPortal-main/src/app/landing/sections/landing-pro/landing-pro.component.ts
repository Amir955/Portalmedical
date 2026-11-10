import { Component } from '@angular/core';
import { PRO_FEATURES } from '../../landing.data';

/** "Vous êtes médecin ?": what the practice gets, and how to join. */
@Component({
  selector: 'app-landing-pro',
  templateUrl: './landing-pro.component.html',
  styleUrls: ['../../landing-shared.css', './landing-pro.component.css']
})
export class LandingProComponent {
  readonly features = PRO_FEATURES;
}
