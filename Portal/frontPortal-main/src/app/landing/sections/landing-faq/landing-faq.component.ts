import { Component } from '@angular/core';
import { FAQ } from '../../landing.data';

/** Frequently asked questions (one answer open at a time). */
@Component({
  selector: 'app-landing-faq',
  templateUrl: './landing-faq.component.html',
  styleUrls: ['../../landing-shared.css', './landing-faq.component.css']
})
export class LandingFaqComponent {
  readonly items = FAQ;
  open: number | null = 0;

  toggle(index: number): void {
    this.open = this.open === index ? null : index;
  }
}
