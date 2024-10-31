import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListmanagerComponent } from './listmanager.component';

describe('ListmanagerComponent', () => {
  let component: ListmanagerComponent;
  let fixture: ComponentFixture<ListmanagerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ListmanagerComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListmanagerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
