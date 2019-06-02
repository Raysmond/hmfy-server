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
      endTime: shipPlan.endTime != null && shipPlan.endTime.isValid() ? shipPlan.endTime.toJSON() : null,
      createTime: shipPlan.createTime != null && shipPlan.createTime.isValid() ? shipPlan.createTime.toJSON() : null,
      updateTime: shipPlan.updateTime != null && shipPlan.updateTime.isValid() ? shipPlan.updateTime.toJSON() : null
    });
    return copy;
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.endTime = res.body.endTime != null ? moment(res.body.endTime) : null;
      res.body.createTime = res.body.createTime != null ? moment(res.body.createTime) : null;
      res.body.updateTime = res.body.updateTime != null ? moment(res.body.updateTime) : null;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((shipPlan: IShipPlan) => {
        shipPlan.endTime = shipPlan.endTime != null ? moment(shipPlan.endTime) : null;
        shipPlan.createTime = shipPlan.createTime != null ? moment(shipPlan.createTime) : null;
        shipPlan.updateTime = shipPlan.updateTime != null ? moment(shipPlan.updateTime) : null;
      });
    }
    return res;
  }
}
