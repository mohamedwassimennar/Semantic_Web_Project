import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListefeedbackComponent } from './listefeedback.component';

describe('ListefeedbackComponent', () => {
  let component: ListefeedbackComponent;
  let fixture: ComponentFixture<ListefeedbackComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ListefeedbackComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListefeedbackComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
