import { Component } from '@angular/core';
import { scrollToSection } from '../../landing.data';

/** Top bar: brand, section links and the professional sign-in. */
@Component({
  selector: 'app-landing-header',
  templateUrl: './landing-header.component.html',
  styleUrls: ['../../landing-shared.css', './landing-header.component.css']
})
export class LandingHeaderComponent {
  readonly scrollTo = scrollToSection;
}
