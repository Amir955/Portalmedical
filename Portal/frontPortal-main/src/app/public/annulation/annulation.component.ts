import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

interface CancelView {
  dateOfApt: string;
  etat: string;
  passe: boolean;
  medecin?: string;
  telephone?: string;
}

type CancelState = 'loading' | 'invalid' | 'ready' | 'cancelled' | 'closed';

/** Public page behind the "Annuler mon rendez-vous" link of the patient's e-mails. */
@Component({
  selector: 'app-annulation',
  templateUrl: './annulation.component.html'
})
export class AnnulationComponent implements OnInit {
  private readonly url = 'http://localhost:8085/rendezvous/annulation/';

  state: CancelState = 'loading';
  view: CancelView | null = null;
  cancelling = false;
  error = '';
  /** True when the cancellation happened on this page (vs. an already cancelled link). */
  justCancelled = false;

  private token = '';

  constructor(private route: ActivatedRoute,
              private http: HttpClient) {
  }

  ngOnInit(): void {
    this.token = this.route.snapshot.paramMap.get('token') || '';
    if (!this.token) {
      this.state = 'invalid';
      return;
    }
    this.http.get<CancelView>(this.url + encodeURIComponent(this.token)).subscribe(
      (view) => this.show(view),
      () => this.state = 'invalid'
    );
  }

  cancel(): void {
    if (this.cancelling) {
      return;
    }
    this.cancelling = true;
    this.error = '';
    this.http.post<CancelView>(this.url + encodeURIComponent(this.token), {}).subscribe(
      (view) => {
        this.cancelling = false;
        this.justCancelled = true;
        this.show(view);
      },
      (err) => {
        this.cancelling = false;
        if (err && err.status === 409) {
          this.state = 'closed';
        } else {
          this.error = 'L’annulation n’a pas pu être enregistrée. Réessayez dans un instant ou appelez le cabinet.';
        }
      }
    );
  }

  phoneLink(phone: string): string {
    return 'tel:' + String(phone || '').replace(/[^+0-9]/g, '');
  }

  private show(view: CancelView): void {
    this.view = view;
    if (view.etat === 'cancelled') {
      this.state = 'cancelled';
    } else if (view.passe || (view.etat !== 'confirmed' && view.etat !== 'waiting')) {
      this.state = 'closed';
    } else {
      this.state = 'ready';
    }
  }
}
