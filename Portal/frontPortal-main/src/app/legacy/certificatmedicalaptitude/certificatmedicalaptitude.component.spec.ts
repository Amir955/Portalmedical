import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { CertificatmedicalaptitudeComponent } from './certificatmedicalaptitude.component';

describe('CertificatmedicalaptitudeComponent', () => {
  let component: CertificatmedicalaptitudeComponent;
  let fixture: ComponentFixture<CertificatmedicalaptitudeComponent>;

  beforeEach(waitForAsync(() => {
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
