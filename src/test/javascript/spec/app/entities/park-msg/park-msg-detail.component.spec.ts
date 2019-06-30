/* tslint:disable max-line-length */
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { ShieldTestModule } from '../../../test.module';
import { ParkMsgDetailComponent } from 'app/entities/park-msg/park-msg-detail.component';
import { ParkMsg } from 'app/shared/model/park-msg.model';

describe('Component Tests', () => {
  describe('ParkMsg Management Detail Component', () => {
    let comp: ParkMsgDetailComponent;
    let fixture: ComponentFixture<ParkMsgDetailComponent>;
    const route = ({ data: of({ parkMsg: new ParkMsg(123) }) } as any) as ActivatedRoute;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [ShieldTestModule],
        declarations: [ParkMsgDetailComponent],
        providers: [{ provide: ActivatedRoute, useValue: route }]
      })
        .overrideTemplate(ParkMsgDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(ParkMsgDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should call load all on init', () => {
        // GIVEN

        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.parkMsg).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
