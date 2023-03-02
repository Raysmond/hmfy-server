import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';
import { JhiLanguageService } from 'ng-jhipster';
import { JhiLanguageHelper } from 'app/core';

import { ShieldSharedModule } from 'app/shared';
import {
  PlanComponent,
  PlanDetailComponent,
  PlanUpdateComponent,
  PlanDeletePopupComponent,
  PlanDeleteDialogComponent,
  planRoute,
  planPopupRoute
} from './';

const ENTITY_STATES = [...planRoute, ...planPopupRoute];

@NgModule({
  imports: [ShieldSharedModule, RouterModule.forChild(ENTITY_STATES)],
  declarations: [PlanComponent, PlanDetailComponent, PlanUpdateComponent, PlanDeleteDialogComponent, PlanDeletePopupComponent],
  entryComponents: [PlanComponent, PlanUpdateComponent, PlanDeleteDialogComponent, PlanDeletePopupComponent],
  providers: [{ provide: JhiLanguageService, useClass: JhiLanguageService }],
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class ShieldPlanModule {
  constructor(private languageService: JhiLanguageService, private languageHelper: JhiLanguageHelper) {
    this.languageHelper.language.subscribe((languageKey: string) => {
      if (languageKey !== undefined) {
        this.languageService.changeLanguage(languageKey);
      }
    });
  }
}
