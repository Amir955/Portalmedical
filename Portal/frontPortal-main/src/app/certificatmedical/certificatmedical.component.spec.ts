import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CertificatmedicalComponent } from './certificatmedical.component';

describe('CertificatmedicalComponent', () => {
  let component: CertificatmedicalComponent;
  let fixture: ComponentFixture<CertificatmedicalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CertificatmedicalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CertificatmedicalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
