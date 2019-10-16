import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import * as moment from 'moment';
import { DATE_FORMAT } from 'app/shared/constants/input.constants';
import { map } from 'rxjs/operators';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared';
import { IShipPlan } from 'app/shared/model/ship-plan.model';

type EntityResponseType = HttpResponse<IShipPlan>;
type EntityArrayResponseType = HttpResponse<IShipPlan[]>;

@Injectable({ providedIn: 'root' })
export class ShipPlanService {
  public resourceUrl = SERVER_API_URL + 'api/ship-plans';

  constructor(protected http: HttpClient) {}

  create(shipPlan: IShipPlan): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(shipPlan);
    return this.http
      .post<IShipPlan>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(shipPlan: IShipPlan): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(shipPlan);
    return this.http
      .put<IShipPlan>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<IShipPlan>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<IShipPlan[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<any>> {
    return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  protected convertDateFromClient(shipPlan: IShipPlan): IShipPlan {
    const copy: IShipPlan = Object.assign({}, shipPlan, {
      gateTime: shipPlan.gateTime != null && shipPlan.gateTime.isValid() ? shipPlan.gateTime.toJSON() : null,
      leaveTime: shipPlan.leaveTime != null && shipPlan.leaveTime.isValid() ? shipPlan.leaveTime.toJSON() : null,
      deliverTime: shipPlan.deliverTime != null && shipPlan.deliverTime.isValid() ? shipPlan.deliverTime.toJSON() : null,
      allowInTime: shipPlan.allowInTime != null && shipPlan.allowInTime.isValid() ? shipPlan.allowInTime.toJSON() : null,
      loadingStartTime:
        shipPlan.loadingStartTime != null && shipPlan.loadingStartTime.isValid() ? shipPlan.loadingStartTime.toJSON() : null,
      loadingEndTime: shipPlan.loadingEndTime != null && shipPlan.loadingEndTime.isValid() ? shipPlan.loadingEndTime.toJSON() : null,
      createTime: shipPlan.createTime != null && shipPlan.createTime.isValid() ? shipPlan.createTime.toJSON() : null,
      updateTime: shipPlan.updateTime != null && shipPlan.updateTime.isValid() ? shipPlan.updateTime.toJSON() : null,
      syncTime: shipPlan.syncTime != null && shipPlan.syncTime.isValid() ? shipPlan.syncTime.toJSON() : null
    });
    return copy;
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.gateTime = res.body.gateTime != null ? moment(res.body.gateTime) : null;
      res.body.leaveTime = res.body.leaveTime != null ? moment(res.body.leaveTime) : null;
      res.body.deliverTime = res.body.deliverTime != null ? moment(res.body.deliverTime) : null;
      res.body.allowInTime = res.body.allowInTime != null ? moment(res.body.allowInTime) : null;
      res.body.loadingStartTime = res.body.loadingStartTime != null ? moment(res.body.loadingStartTime) : null;
      res.body.loadingEndTime = res.body.loadingEndTime != null ? moment(res.body.loadingEndTime) : null;
      res.body.createTime = res.body.createTime != null ? moment(res.body.createTime) : null;
      res.body.updateTime = res.body.updateTime != null ? moment(res.body.updateTime) : null;
      res.body.syncTime = res.body.syncTime != null ? moment(res.body.syncTime) : null;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((shipPlan: IShipPlan) => {
        shipPlan.gateTime = shipPlan.gateTime != null ? moment(shipPlan.gateTime) : null;
        shipPlan.leaveTime = shipPlan.leaveTime != null ? moment(shipPlan.leaveTime) : null;
        shipPlan.deliverTime = shipPlan.deliverTime != null ? moment(shipPlan.deliverTime) : null;
        shipPlan.allowInTime = shipPlan.allowInTime != null ? moment(shipPlan.allowInTime) : null;
        shipPlan.loadingStartTime = shipPlan.loadingStartTime != null ? moment(shipPlan.loadingStartTime) : null;
        shipPlan.loadingEndTime = shipPlan.loadingEndTime != null ? moment(shipPlan.loadingEndTime) : null;
        shipPlan.createTime = shipPlan.createTime != null ? moment(shipPlan.createTime) : null;
        shipPlan.updateTime = shipPlan.updateTime != null ? moment(shipPlan.updateTime) : null;
        shipPlan.syncTime = shipPlan.syncTime != null ? moment(shipPlan.syncTime) : null;
      });
    }
    return res;
  }
}
