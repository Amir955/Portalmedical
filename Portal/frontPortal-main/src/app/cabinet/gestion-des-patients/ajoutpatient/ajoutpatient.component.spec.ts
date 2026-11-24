import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { AjoutpatientComponent } from './ajoutpatient.component';

describe('AjoutpatientComponent', () => {
  let component: AjoutpatientComponent;
  let fixture: ComponentFixture<AjoutpatientComponent>;

  beforeEach(waitForAsync(() => {
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
