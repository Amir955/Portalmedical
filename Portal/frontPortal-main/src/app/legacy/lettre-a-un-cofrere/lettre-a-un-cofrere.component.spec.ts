import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LettreAUnCofrereComponent } from './lettre-a-un-cofrere.component';

describe('LettreAUnCofrereComponent', () => {
  let component: LettreAUnCofrereComponent;
  let fixture: ComponentFixture<LettreAUnCofrereComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LettreAUnCofrereComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LettreAUnCofrereComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
