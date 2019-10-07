/* tslint:disable max-line-length */
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { ShieldTestModule } from '../../../test.module';
import { GateRecordDetailComponent } from 'app/entities/gate-record/gate-record-detail.component';
import { GateRecord } from 'app/shared/model/gate-record.model';

describe('Component Tests', () => {
  describe('GateRecord Management Detail Component', () => {
    let comp: GateRecordDetailComponent;
    let fixture: ComponentFixture<GateRecordDetailComponent>;
    const route = ({ data: of({ gateRecord: new GateRecord(123) }) } as any) as ActivatedRoute;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [ShieldTestModule],
        declarations: [GateRecordDetailComponent],
        providers: [{ provide: ActivatedRoute, useValue: route }]
      })
        .overrideTemplate(GateRecordDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(GateRecordDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should call load all on init', () => {
        // GIVEN

        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.gateRecord).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
