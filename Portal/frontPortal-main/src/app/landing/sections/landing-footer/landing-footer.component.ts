import { Component } from '@angular/core';
import { scrollToSection } from '../../landing.data';

/** Footer: links and emergency number. */
@Component({
  selector: 'app-landing-footer',
  templateUrl: './landing-footer.component.html',
  styleUrls: ['../../landing-shared.css', './landing-footer.component.css']
})
export class LandingFooterComponent {
  readonly year = new Date().getFullYear();
  readonly scrollTo = scrollToSection;
}
