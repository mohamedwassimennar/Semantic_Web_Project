import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListCollectionEventComponent } from './list-collection-event.component';

describe('ListCollectionEventComponent', () => {
  let component: ListCollectionEventComponent;
  let fixture: ComponentFixture<ListCollectionEventComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ListCollectionEventComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListCollectionEventComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
