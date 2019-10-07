import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes } from '@angular/router';
import { JhiPaginationUtil, JhiResolvePagingParams } from 'ng-jhipster';
import { UserRouteAccessService } from 'app/core';
import { Observable, of } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { GateRecord } from 'app/shared/model/gate-record.model';
import { GateRecordService } from './gate-record.service';
import { GateRecordComponent } from './gate-record.component';
import { GateRecordDetailComponent } from './gate-record-detail.component';
import { GateRecordUpdateComponent } from './gate-record-update.component';
import { GateRecordDeletePopupComponent } from './gate-record-delete-dialog.component';
import { IGateRecord } from 'app/shared/model/gate-record.model';

@Injectable({ providedIn: 'root' })
export class GateRecordResolve implements Resolve<IGateRecord> {
  constructor(private service: GateRecordService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<IGateRecord> {
    const id = route.params['id'] ? route.params['id'] : null;
    if (id) {
      return this.service.find(id).pipe(
        filter((response: HttpResponse<GateRecord>) => response.ok),
        map((gateRecord: HttpResponse<GateRecord>) => gateRecord.body)
      );
    }
    return of(new GateRecord());
  }
}

export const gateRecordRoute: Routes = [
  {
    path: '',
    component: GateRecordComponent,
    resolve: {
      pagingParams: JhiResolvePagingParams
    },
    data: {
      authorities: ['ROLE_USER'],
      defaultSort: 'id,asc',
      pageTitle: 'shieldApp.gateRecord.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/view',
    component: GateRecordDetailComponent,
    resolve: {
      gateRecord: GateRecordResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'shieldApp.gateRecord.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'new',
    component: GateRecordUpdateComponent,
    resolve: {
      gateRecord: GateRecordResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'shieldApp.gateRecord.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/edit',
    component: GateRecordUpdateComponent,
    resolve: {
      gateRecord: GateRecordResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'shieldApp.gateRecord.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const gateRecordPopupRoute: Routes = [
  {
    path: ':id/delete',
    component: GateRecordDeletePopupComponent,
    resolve: {
      gateRecord: GateRecordResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'shieldApp.gateRecord.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
