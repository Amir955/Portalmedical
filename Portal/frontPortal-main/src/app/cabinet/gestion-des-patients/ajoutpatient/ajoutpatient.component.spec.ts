import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AjoutpatientComponent } from './ajoutpatient.component';

describe('AjoutpatientComponent', () => {
  let component: AjoutpatientComponent;
  let fixture: ComponentFixture<AjoutpatientComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AjoutpatientComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AjoutpatientComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
