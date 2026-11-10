import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DemandeRendezVousComponent } from './demande-rendez-vous.component';

describe('DemandeRendezVousComponent', () => {
  let component: DemandeRendezVousComponent;
  let fixture: ComponentFixture<DemandeRendezVousComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DemandeRendezVousComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DemandeRendezVousComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
