import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import * as moment from 'moment';
import { DATE_FORMAT } from 'app/shared/constants/input.constants';
import { map } from 'rxjs/operators';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared';
import { IPlan } from 'app/shared/model/plan.model';

type EntityResponseType = HttpResponse<IPlan>;
type EntityArrayResponseType = HttpResponse<IPlan[]>;

@Injectable({ providedIn: 'root' })
export class PlanService {
  public resourceUrl = SERVER_API_URL + 'api/plans';

  constructor(protected http: HttpClient) {}

  create(plan: IPlan): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(plan);
    return this.http
      .post<IPlan>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(plan: IPlan): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(plan);
    return this.http
      .put<IPlan>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<IPlan>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<IPlan[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<any>> {
    return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  protected convertDateFromClient(plan: IPlan): IPlan {
    const copy: IPlan = Object.assign({}, plan, {
      workDay: plan.workDay != null && plan.workDay.isValid() ? plan.workDay.format(DATE_FORMAT) : null,
      loadingStartTime: plan.loadingStartTime != null && plan.loadingStartTime.isValid() ? plan.loadingStartTime.toJSON() : null,
      loadingEndTime: plan.loadingEndTime != null && plan.loadingEndTime.isValid() ? plan.loadingEndTime.toJSON() : null,
      createTime: plan.createTime != null && plan.createTime.isValid() ? plan.createTime.toJSON() : null,
      updateTime: plan.updateTime != null && plan.updateTime.isValid() ? plan.updateTime.toJSON() : null
    });
    return copy;
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.workDay = res.body.workDay != null ? moment(res.body.workDay) : null;
      res.body.loadingStartTime = res.body.loadingStartTime != null ? moment(res.body.loadingStartTime) : null;
      res.body.loadingEndTime = res.body.loadingEndTime != null ? moment(res.body.loadingEndTime) : null;
      res.body.createTime = res.body.createTime != null ? moment(res.body.createTime) : null;
      res.body.updateTime = res.body.updateTime != null ? moment(res.body.updateTime) : null;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((plan: IPlan) => {
        plan.workDay = plan.workDay != null ? moment(plan.workDay) : null;
        plan.loadingStartTime = plan.loadingStartTime != null ? moment(plan.loadingStartTime) : null;
        plan.loadingEndTime = plan.loadingEndTime != null ? moment(plan.loadingEndTime) : null;
        plan.createTime = plan.createTime != null ? moment(plan.createTime) : null;
        plan.updateTime = plan.updateTime != null ? moment(plan.updateTime) : null;
      });
    }
    return res;
  }
}
