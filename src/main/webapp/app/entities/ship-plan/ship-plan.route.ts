import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes } from '@angular/router';
import { JhiPaginationUtil, JhiResolvePagingParams } from 'ng-jhipster';
import { UserRouteAccessService } from 'app/core';
import { Observable, of } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { ShipPlan } from 'app/shared/model/ship-plan.model';
import { ShipPlanService } from './ship-plan.service';
import { ShipPlanComponent } from './ship-plan.component';
import { ShipPlanDetailComponent } from './ship-plan-detail.component';
import { ShipPlanUpdateComponent } from './ship-plan-update.component';
import { ShipPlanDeletePopupComponent } from './ship-plan-delete-dialog.component';
import { IShipPlan } from 'app/shared/model/ship-plan.model';

@Injectable({ providedIn: 'root' })
export class ShipPlanResolve implements Resolve<IShipPlan> {
  constructor(private service: ShipPlanService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<IShipPlan> {
    const id = route.params['id'] ? route.params['id'] : null;
    if (id) {
      return this.service.find(id).pipe(
        filter((response: HttpResponse<ShipPlan>) => response.ok),
        map((shipPlan: HttpResponse<ShipPlan>) => shipPlan.body)
      );
    }
    return of(new ShipPlan());
  }
}

export const shipPlanRoute: Routes = [
  {
    path: '',
    component: ShipPlanComponent,
    resolve: {
      pagingParams: JhiResolvePagingParams
    },
    data: {
      authorities: ['ROLE_USER'],
      defaultSort: 'id,asc',
      pageTitle: 'shieldApp.shipPlan.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/view',
    component: ShipPlanDetailComponent,
    resolve: {
      shipPlan: ShipPlanResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'shieldApp.shipPlan.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'new',
    component: ShipPlanUpdateComponent,
    resolve: {
      shipPlan: ShipPlanResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'shieldApp.shipPlan.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/edit',
    component: ShipPlanUpdateComponent,
    resolve: {
      shipPlan: ShipPlanResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'shieldApp.shipPlan.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const shipPlanPopupRoute: Routes = [
  {
    path: ':id/delete',
    component: ShipPlanDeletePopupComponent,
    resolve: {
      shipPlan: ShipPlanResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'shieldApp.shipPlan.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
