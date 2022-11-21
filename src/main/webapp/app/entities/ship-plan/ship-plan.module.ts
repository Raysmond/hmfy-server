import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';
import { JhiLanguageService } from 'ng-jhipster';
import { JhiLanguageHelper } from 'app/core';

import { ShieldSharedModule } from 'app/shared';
import {
  ShipPlanComponent,
  ShipPlanWarningComponent,
  ShipPlanDetailComponent,
  ShipPlanUpdateComponent,
  ShipPlanDeletePopupComponent,
  ShipPlanDeleteDialogComponent,
  shipPlanRoute,
  shipPlanPopupRoute
} from './';

const ENTITY_STATES = [...shipPlanRoute, ...shipPlanPopupRoute];

@NgModule({
  imports: [ShieldSharedModule, RouterModule.forChild(ENTITY_STATES)],
  declarations: [
    ShipPlanComponent,
    ShipPlanWarningComponent,
    ShipPlanDetailComponent,
    ShipPlanUpdateComponent,
    ShipPlanDeleteDialogComponent,
    ShipPlanDeletePopupComponent
  ],
  entryComponents: [ShipPlanComponent, ShipPlanWarningComponent, ShipPlanUpdateComponent, ShipPlanDeleteDialogComponent, ShipPlanDeletePopupComponent],
  providers: [{ provide: JhiLanguageService, useClass: JhiLanguageService }],
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class ShieldShipPlanModule {
  constructor(private languageService: JhiLanguageService, private languageHelper: JhiLanguageHelper) {
    this.languageHelper.language.subscribe((languageKey: string) => {
      if (languageKey !== undefined) {
        this.languageService.changeLanguage(languageKey);
      }
    });
  }
}
