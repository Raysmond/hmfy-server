/* tslint:disable max-line-length */
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { Observable, of } from 'rxjs';

import { ShieldTestModule } from '../../../test.module';
import { GateRecordUpdateComponent } from 'app/entities/gate-record/gate-record-update.component';
import { GateRecordService } from 'app/entities/gate-record/gate-record.service';
import { GateRecord } from 'app/shared/model/gate-record.model';

describe('Component Tests', () => {
  describe('GateRecord Management Update Component', () => {
    let comp: GateRecordUpdateComponent;
    let fixture: ComponentFixture<GateRecordUpdateComponent>;
    let service: GateRecordService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [ShieldTestModule],
        declarations: [GateRecordUpdateComponent],
        providers: [FormBuilder]
      })
        .overrideTemplate(GateRecordUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(GateRecordUpdateComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(GateRecordService);
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', fakeAsync(() => {
        // GIVEN
        const entity = new GateRecord(123);
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
        const entity = new GateRecord();
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
