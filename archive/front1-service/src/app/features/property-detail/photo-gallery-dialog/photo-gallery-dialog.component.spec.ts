import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PhotoGalleryDialogComponent } from './photo-gallery-dialog.component';

describe('PhotoGalleryDialogComponent', () => {
  let component: PhotoGalleryDialogComponent;
  let fixture: ComponentFixture<PhotoGalleryDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PhotoGalleryDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PhotoGalleryDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
