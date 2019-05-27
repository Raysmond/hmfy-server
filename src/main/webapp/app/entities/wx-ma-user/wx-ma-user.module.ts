import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';
import { JhiLanguageService } from 'ng-jhipster';
import { JhiLanguageHelper } from 'app/core';

import { ShieldSharedModule } from 'app/shared';
import {
  WxMaUserComponent,
  WxMaUserDetailComponent,
  WxMaUserUpdateComponent,
  WxMaUserDeletePopupComponent,
  WxMaUserDeleteDialogComponent,
  wxMaUserRoute,
  wxMaUserPopupRoute
} from './';

const ENTITY_STATES = [...wxMaUserRoute, ...wxMaUserPopupRoute];

@NgModule({
  imports: [ShieldSharedModule, RouterModule.forChild(ENTITY_STATES)],
  declarations: [
    WxMaUserComponent,
    WxMaUserDetailComponent,
    WxMaUserUpdateComponent,
    WxMaUserDeleteDialogComponent,
    WxMaUserDeletePopupComponent
  ],
  entryComponents: [WxMaUserComponent, WxMaUserUpdateComponent, WxMaUserDeleteDialogComponent, WxMaUserDeletePopupComponent],
  providers: [{ provide: JhiLanguageService, useClass: JhiLanguageService }],
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class ShieldWxMaUserModule {
  constructor(private languageService: JhiLanguageService, private languageHelper: JhiLanguageHelper) {
    this.languageHelper.language.subscribe((languageKey: string) => {
      if (languageKey !== undefined) {
        this.languageService.changeLanguage(languageKey);
      }
    });
  }
}
