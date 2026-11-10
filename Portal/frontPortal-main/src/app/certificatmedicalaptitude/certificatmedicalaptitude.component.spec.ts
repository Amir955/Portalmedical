import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CertificatmedicalaptitudeComponent } from './certificatmedicalaptitude.component';

describe('CertificatmedicalaptitudeComponent', () => {
  let component: CertificatmedicalaptitudeComponent;
  let fixture: ComponentFixture<CertificatmedicalaptitudeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CertificatmedicalaptitudeComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CertificatmedicalaptitudeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
