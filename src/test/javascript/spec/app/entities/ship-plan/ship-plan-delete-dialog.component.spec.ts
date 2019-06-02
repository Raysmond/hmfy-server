/* tslint:disable max-line-length */
import { ComponentFixture, TestBed, inject, fakeAsync, tick } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable, of } from 'rxjs';
import { JhiEventManager } from 'ng-jhipster';

import { ShieldTestModule } from '../../../test.module';
import { ShipPlanDeleteDialogComponent } from 'app/entities/ship-plan/ship-plan-delete-dialog.component';
import { ShipPlanService } from 'app/entities/ship-plan/ship-plan.service';

describe('Component Tests', () => {
  describe('ShipPlan Management Delete Component', () => {
    let comp: ShipPlanDeleteDialogComponent;
    let fixture: ComponentFixture<ShipPlanDeleteDialogComponent>;
    let service: ShipPlanService;
    let mockEventManager: any;
    let mockActiveModal: any;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [ShieldTestModule],
        declarations: [ShipPlanDeleteDialogComponent]
      })
        .overrideTemplate(ShipPlanDeleteDialogComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(ShipPlanDeleteDialogComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(ShipPlanService);
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
