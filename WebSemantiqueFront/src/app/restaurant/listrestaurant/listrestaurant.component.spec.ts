import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListrestaurantComponent } from './listrestaurant.component';

describe('ListrestaurantComponent', () => {
  let component: ListrestaurantComponent;
  let fixture: ComponentFixture<ListrestaurantComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ListrestaurantComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListrestaurantComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
