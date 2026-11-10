import { Component } from '@angular/core';
import { PATIENT_STEPS } from '../../landing.data';

/** The three steps of a booking, for patients. */
@Component({
  selector: 'app-landing-how-it-works',
  templateUrl: './landing-how-it-works.component.html',
  styleUrls: ['../../landing-shared.css', './landing-how-it-works.component.css']
})
export class LandingHowItWorksComponent {
  readonly steps = PATIENT_STEPS;
}
