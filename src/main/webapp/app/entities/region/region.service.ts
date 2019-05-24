import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import * as moment from 'moment';
import { DATE_FORMAT } from 'app/shared/constants/input.constants';
import { map } from 'rxjs/operators';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared';
import { IRegion } from 'app/shared/model/region.model';

type EntityResponseType = HttpResponse<IRegion>;
type EntityArrayResponseType = HttpResponse<IRegion[]>;

@Injectable({ providedIn: 'root' })
export class RegionService {
  public resourceUrl = SERVER_API_URL + 'api/regions';

  constructor(protected http: HttpClient) {}

  create(region: IRegion): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(region);
    return this.http
      .post<IRegion>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(region: IRegion): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(region);
    return this.http
      .put<IRegion>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<IRegion>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<IRegion[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<any>> {
    return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  protected convertDateFromClient(region: IRegion): IRegion {
    const copy: IRegion = Object.assign({}, region, {
      createTime: region.createTime != null && region.createTime.isValid() ? region.createTime.toJSON() : null,
      updateTime: region.updateTime != null && region.updateTime.isValid() ? region.updateTime.toJSON() : null
    });
    return copy;
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.createTime = res.body.createTime != null ? moment(res.body.createTime) : null;
      res.body.updateTime = res.body.updateTime != null ? moment(res.body.updateTime) : null;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((region: IRegion) => {
        region.createTime = region.createTime != null ? moment(region.createTime) : null;
        region.updateTime = region.updateTime != null ? moment(region.updateTime) : null;
      });
    }
    return res;
  }
}
