import { Route } from '@angular/router';
import { LoginPageComponent } from 'app/login_page/login_page.component';

// import { LoginPageComponent } from './';

export const LOGIN_PAGE_ROUTE: Route = {
  path: 'login',
  component: LoginPageComponent,
  data: {
    authorities: [],
    pageTitle: 'login.title'
  }
};
