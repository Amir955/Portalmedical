import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { PageAdminComponent } from './page-admin.component';

describe('PageAdminComponent', () => {
  let component: PageAdminComponent;
  let fixture: ComponentFixture<PageAdminComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ PageAdminComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PageAdminComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
