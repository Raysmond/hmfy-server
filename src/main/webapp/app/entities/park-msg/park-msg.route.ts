import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes } from '@angular/router';
import { JhiPaginationUtil, JhiResolvePagingParams } from 'ng-jhipster';
import { UserRouteAccessService } from 'app/core';
import { Observable, of } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { ParkMsg } from 'app/shared/model/park-msg.model';
import { ParkMsgService } from './park-msg.service';
import { ParkMsgComponent } from './park-msg.component';
import { ParkMsgDetailComponent } from './park-msg-detail.component';
import { ParkMsgUpdateComponent } from './park-msg-update.component';
import { ParkMsgDeletePopupComponent } from './park-msg-delete-dialog.component';
import { IParkMsg } from 'app/shared/model/park-msg.model';

@Injectable({ providedIn: 'root' })
export class ParkMsgResolve implements Resolve<IParkMsg> {
  constructor(private service: ParkMsgService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<IParkMsg> {
    const id = route.params['id'] ? route.params['id'] : null;
    if (id) {
      return this.service.find(id).pipe(
        filter((response: HttpResponse<ParkMsg>) => response.ok),
        map((parkMsg: HttpResponse<ParkMsg>) => parkMsg.body)
      );
    }
    return of(new ParkMsg());
  }
}

export const parkMsgRoute: Routes = [
  {
    path: '',
    component: ParkMsgComponent,
    resolve: {
      pagingParams: JhiResolvePagingParams
    },
    data: {
      authorities: ['ROLE_USER'],
      defaultSort: 'id,asc',
      pageTitle: 'shieldApp.parkMsg.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/view',
    component: ParkMsgDetailComponent,
    resolve: {
      parkMsg: ParkMsgResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'shieldApp.parkMsg.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'new',
    component: ParkMsgUpdateComponent,
    resolve: {
      parkMsg: ParkMsgResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'shieldApp.parkMsg.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/edit',
    component: ParkMsgUpdateComponent,
    resolve: {
      parkMsg: ParkMsgResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'shieldApp.parkMsg.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const parkMsgPopupRoute: Routes = [
  {
    path: ':id/delete',
    component: ParkMsgDeletePopupComponent,
    resolve: {
      parkMsg: ParkMsgResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'shieldApp.parkMsg.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
