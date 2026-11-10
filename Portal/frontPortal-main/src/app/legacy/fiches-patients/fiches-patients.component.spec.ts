import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FichesPatientsComponent } from './fiches-patients.component';

describe('FichesPatientsComponent', () => {
  let component: FichesPatientsComponent;
  let fixture: ComponentFixture<FichesPatientsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FichesPatientsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FichesPatientsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
