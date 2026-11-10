import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GestionDesPatientsComponent } from './gestion-des-patients.component';

describe('GestionDesPatientsComponent', () => {
  let component: GestionDesPatientsComponent;
  let fixture: ComponentFixture<GestionDesPatientsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GestionDesPatientsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GestionDesPatientsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
