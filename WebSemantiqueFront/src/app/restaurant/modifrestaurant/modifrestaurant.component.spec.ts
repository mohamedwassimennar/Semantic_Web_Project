import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModifrestaurantComponent } from './modifrestaurant.component';

describe('ModifrestaurantComponent', () => {
  let component: ModifrestaurantComponent;
  let fixture: ComponentFixture<ModifrestaurantComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ModifrestaurantComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModifrestaurantComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
