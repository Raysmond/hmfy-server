import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ShieldSharedModule } from 'app/shared';
import { LoginPageComponent } from 'app/login_page/login_page.component';
import { LOGIN_PAGE_ROUTE } from 'app/login_page/login_page.route';
// import { LOGIN_PAGE_ROUTE, LoginPageComponent } from './';

@NgModule({
  imports: [ShieldSharedModule, RouterModule.forChild([LOGIN_PAGE_ROUTE])],
  declarations: [LoginPageComponent],
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class ShieldLoginPageModule {}
