import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';
import { JhiLanguageService } from 'ng-jhipster';
import { JhiLanguageHelper } from 'app/core';

import { ShieldSharedModule } from 'app/shared';
import {
  GateRecordComponent,
  GateRecordDetailComponent,
  GateRecordUpdateComponent,
  GateRecordDeletePopupComponent,
  GateRecordDeleteDialogComponent,
  gateRecordRoute,
  gateRecordPopupRoute
} from './';

const ENTITY_STATES = [...gateRecordRoute, ...gateRecordPopupRoute];

@NgModule({
  imports: [ShieldSharedModule, RouterModule.forChild(ENTITY_STATES)],
  declarations: [
    GateRecordComponent,
    GateRecordDetailComponent,
    GateRecordUpdateComponent,
    GateRecordDeleteDialogComponent,
    GateRecordDeletePopupComponent
  ],
  entryComponents: [GateRecordComponent, GateRecordUpdateComponent, GateRecordDeleteDialogComponent, GateRecordDeletePopupComponent],
  providers: [{ provide: JhiLanguageService, useClass: JhiLanguageService }],
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class ShieldGateRecordModule {
  constructor(private languageService: JhiLanguageService, private languageHelper: JhiLanguageHelper) {
    this.languageHelper.language.subscribe((languageKey: string) => {
      if (languageKey !== undefined) {
        this.languageService.changeLanguage(languageKey);
      }
    });
  }
}
