import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RendezVousComponent } from './rendez-vous.component';

describe('RendezVousComponent', () => {
  let component: RendezVousComponent;
  let fixture: ComponentFixture<RendezVousComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RendezVousComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RendezVousComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
