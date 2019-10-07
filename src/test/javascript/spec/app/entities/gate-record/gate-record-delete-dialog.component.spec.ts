/* tslint:disable max-line-length */
import { ComponentFixture, TestBed, inject, fakeAsync, tick } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable, of } from 'rxjs';
import { JhiEventManager } from 'ng-jhipster';

import { ShieldTestModule } from '../../../test.module';
import { GateRecordDeleteDialogComponent } from 'app/entities/gate-record/gate-record-delete-dialog.component';
import { GateRecordService } from 'app/entities/gate-record/gate-record.service';

describe('Component Tests', () => {
  describe('GateRecord Management Delete Component', () => {
    let comp: GateRecordDeleteDialogComponent;
    let fixture: ComponentFixture<GateRecordDeleteDialogComponent>;
    let service: GateRecordService;
    let mockEventManager: any;
    let mockActiveModal: any;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [ShieldTestModule],
        declarations: [GateRecordDeleteDialogComponent]
      })
        .overrideTemplate(GateRecordDeleteDialogComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(GateRecordDeleteDialogComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(GateRecordService);
      mockEventManager = fixture.debugElement.injector.get(JhiEventManager);
      mockActiveModal = fixture.debugElement.injector.get(NgbActiveModal);
    });

    describe('confirmDelete', () => {
      it('Should call delete service on confirmDelete', inject(
        [],
        fakeAsync(() => {
          // GIVEN
          spyOn(service, 'delete').and.returnValue(of({}));

          // WHEN
          comp.confirmDelete(123);
          tick();

          // THEN
          expect(service.delete).toHaveBeenCalledWith(123);
          expect(mockActiveModal.dismissSpy).toHaveBeenCalled();
          expect(mockEventManager.broadcastSpy).toHaveBeenCalled();
        })
      ));
    });
  });
});
