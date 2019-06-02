/* tslint:disable max-line-length */
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { Observable, of } from 'rxjs';

import { ShieldTestModule } from '../../../test.module';
import { ShipPlanUpdateComponent } from 'app/entities/ship-plan/ship-plan-update.component';
import { ShipPlanService } from 'app/entities/ship-plan/ship-plan.service';
import { ShipPlan } from 'app/shared/model/ship-plan.model';

describe('Component Tests', () => {
  describe('ShipPlan Management Update Component', () => {
    let comp: ShipPlanUpdateComponent;
    let fixture: ComponentFixture<ShipPlanUpdateComponent>;
    let service: ShipPlanService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [ShieldTestModule],
        declarations: [ShipPlanUpdateComponent],
        providers: [FormBuilder]
      })
        .overrideTemplate(ShipPlanUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(ShipPlanUpdateComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(ShipPlanService);
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', fakeAsync(() => {
        // GIVEN
        const entity = new ShipPlan(123);
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
        const entity = new ShipPlan();
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
