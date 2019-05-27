/* tslint:disable max-line-length */
import { ComponentFixture, TestBed, inject, fakeAsync, tick } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable, of } from 'rxjs';
import { JhiEventManager } from 'ng-jhipster';

import { ShieldTestModule } from '../../../test.module';
import { WxMaUserDeleteDialogComponent } from 'app/entities/wx-ma-user/wx-ma-user-delete-dialog.component';
import { WxMaUserService } from 'app/entities/wx-ma-user/wx-ma-user.service';

describe('Component Tests', () => {
  describe('WxMaUser Management Delete Component', () => {
    let comp: WxMaUserDeleteDialogComponent;
    let fixture: ComponentFixture<WxMaUserDeleteDialogComponent>;
    let service: WxMaUserService;
    let mockEventManager: any;
    let mockActiveModal: any;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [ShieldTestModule],
        declarations: [WxMaUserDeleteDialogComponent]
      })
        .overrideTemplate(WxMaUserDeleteDialogComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(WxMaUserDeleteDialogComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(WxMaUserService);
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
