import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListfoodComponent } from './listfood.component';

describe('ListfoodComponent', () => {
  let component: ListfoodComponent;
  let fixture: ComponentFixture<ListfoodComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ListfoodComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListfoodComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
