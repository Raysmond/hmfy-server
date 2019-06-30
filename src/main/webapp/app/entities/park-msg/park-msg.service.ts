import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import * as moment from 'moment';
import { DATE_FORMAT } from 'app/shared/constants/input.constants';
import { map } from 'rxjs/operators';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared';
import { IParkMsg } from 'app/shared/model/park-msg.model';

type EntityResponseType = HttpResponse<IParkMsg>;
type EntityArrayResponseType = HttpResponse<IParkMsg[]>;

@Injectable({ providedIn: 'root' })
export class ParkMsgService {
  public resourceUrl = SERVER_API_URL + 'api/park-msgs';

  constructor(protected http: HttpClient) {}

  create(parkMsg: IParkMsg): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(parkMsg);
    return this.http
      .post<IParkMsg>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(parkMsg: IParkMsg): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(parkMsg);
    return this.http
      .put<IParkMsg>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<IParkMsg>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<IParkMsg[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<any>> {
    return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  protected convertDateFromClient(parkMsg: IParkMsg): IParkMsg {
    const copy: IParkMsg = Object.assign({}, parkMsg, {
      createTime: parkMsg.createTime != null && parkMsg.createTime.isValid() ? parkMsg.createTime.toJSON() : null
    });
    return copy;
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.createTime = res.body.createTime != null ? moment(res.body.createTime) : null;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((parkMsg: IParkMsg) => {
        parkMsg.createTime = parkMsg.createTime != null ? moment(parkMsg.createTime) : null;
      });
    }
    return res;
  }
}
