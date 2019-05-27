import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes } from '@angular/router';
import { JhiPaginationUtil, JhiResolvePagingParams } from 'ng-jhipster';
import { UserRouteAccessService } from 'app/core';
import { Observable, of } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { WxMaUser } from 'app/shared/model/wx-ma-user.model';
import { WxMaUserService } from './wx-ma-user.service';
import { WxMaUserComponent } from './wx-ma-user.component';
import { WxMaUserDetailComponent } from './wx-ma-user-detail.component';
import { WxMaUserUpdateComponent } from './wx-ma-user-update.component';
import { WxMaUserDeletePopupComponent } from './wx-ma-user-delete-dialog.component';
import { IWxMaUser } from 'app/shared/model/wx-ma-user.model';

@Injectable({ providedIn: 'root' })
export class WxMaUserResolve implements Resolve<IWxMaUser> {
  constructor(private service: WxMaUserService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<IWxMaUser> {
    const id = route.params['id'] ? route.params['id'] : null;
    if (id) {
      return this.service.find(id).pipe(
        filter((response: HttpResponse<WxMaUser>) => response.ok),
        map((wxMaUser: HttpResponse<WxMaUser>) => wxMaUser.body)
      );
    }
    return of(new WxMaUser());
  }
}

export const wxMaUserRoute: Routes = [
  {
    path: '',
    component: WxMaUserComponent,
    resolve: {
      pagingParams: JhiResolvePagingParams
    },
    data: {
      authorities: ['ROLE_USER'],
      defaultSort: 'id,asc',
      pageTitle: 'shieldApp.wxMaUser.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/view',
    component: WxMaUserDetailComponent,
    resolve: {
      wxMaUser: WxMaUserResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'shieldApp.wxMaUser.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'new',
    component: WxMaUserUpdateComponent,
    resolve: {
      wxMaUser: WxMaUserResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'shieldApp.wxMaUser.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/edit',
    component: WxMaUserUpdateComponent,
    resolve: {
      wxMaUser: WxMaUserResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'shieldApp.wxMaUser.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const wxMaUserPopupRoute: Routes = [
  {
    path: ':id/delete',
    component: WxMaUserDeletePopupComponent,
    resolve: {
      wxMaUser: WxMaUserResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'shieldApp.wxMaUser.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
