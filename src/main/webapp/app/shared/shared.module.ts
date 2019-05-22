import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ShieldSharedLibsModule, ShieldSharedCommonModule, JhiLoginModalComponent, HasAnyAuthorityDirective } from './';

@NgModule({
  imports: [ShieldSharedLibsModule, ShieldSharedCommonModule],
  declarations: [JhiLoginModalComponent, HasAnyAuthorityDirective],
  entryComponents: [JhiLoginModalComponent],
  exports: [ShieldSharedCommonModule, JhiLoginModalComponent, HasAnyAuthorityDirective],
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class ShieldSharedModule {
  static forRoot() {
    return {
      ngModule: ShieldSharedModule
    };
  }
}
