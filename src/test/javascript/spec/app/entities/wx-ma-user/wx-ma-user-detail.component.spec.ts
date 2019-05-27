/* tslint:disable max-line-length */
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { ShieldTestModule } from '../../../test.module';
import { WxMaUserDetailComponent } from 'app/entities/wx-ma-user/wx-ma-user-detail.component';
import { WxMaUser } from 'app/shared/model/wx-ma-user.model';

describe('Component Tests', () => {
  describe('WxMaUser Management Detail Component', () => {
    let comp: WxMaUserDetailComponent;
    let fixture: ComponentFixture<WxMaUserDetailComponent>;
    const route = ({ data: of({ wxMaUser: new WxMaUser(123) }) } as any) as ActivatedRoute;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [ShieldTestModule],
        declarations: [WxMaUserDetailComponent],
        providers: [{ provide: ActivatedRoute, useValue: route }]
      })
        .overrideTemplate(WxMaUserDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(WxMaUserDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should call load all on init', () => {
        // GIVEN

        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.wxMaUser).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
