/* tslint:disable max-line-length */
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { ShieldTestModule } from '../../../test.module';
import { PlanDetailComponent } from 'app/entities/plan/plan-detail.component';
import { Plan } from 'app/shared/model/plan.model';

describe('Component Tests', () => {
  describe('Plan Management Detail Component', () => {
    let comp: PlanDetailComponent;
    let fixture: ComponentFixture<PlanDetailComponent>;
    const route = ({ data: of({ plan: new Plan(123) }) } as any) as ActivatedRoute;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [ShieldTestModule],
        declarations: [PlanDetailComponent],
        providers: [{ provide: ActivatedRoute, useValue: route }]
      })
        .overrideTemplate(PlanDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(PlanDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should call load all on init', () => {
        // GIVEN

        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.plan).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
