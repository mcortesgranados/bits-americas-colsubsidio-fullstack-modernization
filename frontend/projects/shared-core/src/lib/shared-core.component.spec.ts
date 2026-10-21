import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SharedCoreComponent } from './shared-core.component';

describe('SharedCoreComponent', () => {
  let component: SharedCoreComponent;
  let fixture: ComponentFixture<SharedCoreComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SharedCoreComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SharedCoreComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
