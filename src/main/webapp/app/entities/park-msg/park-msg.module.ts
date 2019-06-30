import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';
import { JhiLanguageService } from 'ng-jhipster';
import { JhiLanguageHelper } from 'app/core';

import { ShieldSharedModule } from 'app/shared';
import {
  ParkMsgComponent,
  ParkMsgDetailComponent,
  ParkMsgUpdateComponent,
  ParkMsgDeletePopupComponent,
  ParkMsgDeleteDialogComponent,
  parkMsgRoute,
  parkMsgPopupRoute
} from './';

const ENTITY_STATES = [...parkMsgRoute, ...parkMsgPopupRoute];

@NgModule({
  imports: [ShieldSharedModule, RouterModule.forChild(ENTITY_STATES)],
  declarations: [
    ParkMsgComponent,
    ParkMsgDetailComponent,
    ParkMsgUpdateComponent,
    ParkMsgDeleteDialogComponent,
    ParkMsgDeletePopupComponent
  ],
  entryComponents: [ParkMsgComponent, ParkMsgUpdateComponent, ParkMsgDeleteDialogComponent, ParkMsgDeletePopupComponent],
  providers: [{ provide: JhiLanguageService, useClass: JhiLanguageService }],
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class ShieldParkMsgModule {
  constructor(private languageService: JhiLanguageService, private languageHelper: JhiLanguageHelper) {
    this.languageHelper.language.subscribe((languageKey: string) => {
      if (languageKey !== undefined) {
        this.languageService.changeLanguage(languageKey);
      }
    });
  }
}
