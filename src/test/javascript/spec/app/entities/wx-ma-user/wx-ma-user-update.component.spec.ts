/* tslint:disable max-line-length */
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { Observable, of } from 'rxjs';

import { ShieldTestModule } from '../../../test.module';
import { WxMaUserUpdateComponent } from 'app/entities/wx-ma-user/wx-ma-user-update.component';
import { WxMaUserService } from 'app/entities/wx-ma-user/wx-ma-user.service';
import { WxMaUser } from 'app/shared/model/wx-ma-user.model';

describe('Component Tests', () => {
  describe('WxMaUser Management Update Component', () => {
    let comp: WxMaUserUpdateComponent;
    let fixture: ComponentFixture<WxMaUserUpdateComponent>;
    let service: WxMaUserService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [ShieldTestModule],
        declarations: [WxMaUserUpdateComponent],
        providers: [FormBuilder]
      })
        .overrideTemplate(WxMaUserUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(WxMaUserUpdateComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(WxMaUserService);
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', fakeAsync(() => {
        // GIVEN
        const entity = new WxMaUser(123);
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
        const entity = new WxMaUser();
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
