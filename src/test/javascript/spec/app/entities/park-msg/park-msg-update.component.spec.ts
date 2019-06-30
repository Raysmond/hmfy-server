/* tslint:disable max-line-length */
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { Observable, of } from 'rxjs';

import { ShieldTestModule } from '../../../test.module';
import { ParkMsgUpdateComponent } from 'app/entities/park-msg/park-msg-update.component';
import { ParkMsgService } from 'app/entities/park-msg/park-msg.service';
import { ParkMsg } from 'app/shared/model/park-msg.model';

describe('Component Tests', () => {
  describe('ParkMsg Management Update Component', () => {
    let comp: ParkMsgUpdateComponent;
    let fixture: ComponentFixture<ParkMsgUpdateComponent>;
    let service: ParkMsgService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [ShieldTestModule],
        declarations: [ParkMsgUpdateComponent],
        providers: [FormBuilder]
      })
        .overrideTemplate(ParkMsgUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(ParkMsgUpdateComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(ParkMsgService);
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', fakeAsync(() => {
        // GIVEN
        const entity = new ParkMsg(123);
        spyOn(service, 'update').and.returnValue(of(new HttpResponse({ body: entity })));
        comp.updateForm(entity);
        // WHEN
        comp.save();
        tick(); // simulate async

        // THEN
        expect(service.update).toHaveBeenCalledWith(entity);
        expect(comp.isSaving).toEqual(false);
      }));

      it('Should call create service on save for new entity', fakeAsync(() => {
        // GIVEN
        const entity = new ParkMsg();
        spyOn(service, 'create').and.returnValue(of(new HttpResponse({ body: entity })));
        comp.updateForm(entity);
        // WHEN
        comp.save();
        tick(); // simulate async

        // THEN
        expect(service.create).toHaveBeenCalledWith(entity);
        expect(comp.isSaving).toEqual(false);
      }));
    });
  });
});
