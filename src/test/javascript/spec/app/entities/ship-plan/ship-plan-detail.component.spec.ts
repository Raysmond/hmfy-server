/* tslint:disable max-line-length */
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { ShieldTestModule } from '../../../test.module';
import { ShipPlanDetailComponent } from 'app/entities/ship-plan/ship-plan-detail.component';
import { ShipPlan } from 'app/shared/model/ship-plan.model';

describe('Component Tests', () => {
  describe('ShipPlan Management Detail Component', () => {
    let comp: ShipPlanDetailComponent;
    let fixture: ComponentFixture<ShipPlanDetailComponent>;
    const route = ({ data: of({ shipPlan: new ShipPlan(123) }) } as any) as ActivatedRoute;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [ShieldTestModule],
        declarations: [ShipPlanDetailComponent],
        providers: [{ provide: ActivatedRoute, useValue: route }]
      })
        .overrideTemplate(ShipPlanDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(ShipPlanDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should call load all on init', () => {
        // GIVEN

        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.shipPlan).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
