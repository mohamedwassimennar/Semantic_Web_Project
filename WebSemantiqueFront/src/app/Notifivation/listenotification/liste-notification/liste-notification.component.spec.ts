import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListeNotificationComponent } from './liste-notification.component';

describe('ListeNotificationComponent', () => {
  let component: ListeNotificationComponent;
  let fixture: ComponentFixture<ListeNotificationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ListeNotificationComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListeNotificationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
