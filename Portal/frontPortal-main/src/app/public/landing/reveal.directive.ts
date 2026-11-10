import { AfterViewInit, Directive, ElementRef, Input, OnDestroy, Renderer2 } from '@angular/core';

/**
 * Fades an element in when it scrolls into view: <div lpReveal [lpRevealDelay]="i * 80">.
 * The styles (.lp-reveal / .is-visible) live in landing-shared.css.
 */
@Directive({
  selector: '[lpReveal]'
})
export class RevealDirective implements AfterViewInit, OnDestroy {
  /** Delay before this element appears, in milliseconds (for staggered lists). */
  @Input() lpRevealDelay = 0;

  private observer: IntersectionObserver | null = null;

  constructor(private element: ElementRef<HTMLElement>, private renderer: Renderer2) {
  }

  ngAfterViewInit(): void {
    const el = this.element.nativeElement;
    this.renderer.addClass(el, 'lp-reveal');
    if (this.lpRevealDelay) {
      this.renderer.setStyle(el, 'transition-delay', this.lpRevealDelay + 'ms');
    }
    if (typeof IntersectionObserver === 'undefined') {
      this.renderer.addClass(el, 'is-visible');
      return;
    }
    this.observer = new IntersectionObserver((entries) => {
      if (entries.some((entry) => entry.isIntersecting)) {
        this.renderer.addClass(el, 'is-visible');
        this.disconnect();
      }
    }, { threshold: 0.15, rootMargin: '0px 0px -40px 0px' });
    this.observer.observe(el);
  }

  ngOnDestroy(): void {
    this.disconnect();
  }

  private disconnect(): void {
    if (this.observer) {
      this.observer.disconnect();
      this.observer = null;
    }
  }
}
