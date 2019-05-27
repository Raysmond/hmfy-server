import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import * as moment from 'moment';
import { DATE_FORMAT } from 'app/shared/constants/input.constants';
import { map } from 'rxjs/operators';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared';
import { IWxMaUser } from 'app/shared/model/wx-ma-user.model';

type EntityResponseType = HttpResponse<IWxMaUser>;
type EntityArrayResponseType = HttpResponse<IWxMaUser[]>;

@Injectable({ providedIn: 'root' })
export class WxMaUserService {
  public resourceUrl = SERVER_API_URL + 'api/wx-ma-users';

  constructor(protected http: HttpClient) {}

  create(wxMaUser: IWxMaUser): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(wxMaUser);
    return this.http
      .post<IWxMaUser>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(wxMaUser: IWxMaUser): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(wxMaUser);
    return this.http
      .put<IWxMaUser>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<IWxMaUser>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<IWxMaUser[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<any>> {
    return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  protected convertDateFromClient(wxMaUser: IWxMaUser): IWxMaUser {
    const copy: IWxMaUser = Object.assign({}, wxMaUser, {
      createTime: wxMaUser.createTime != null && wxMaUser.createTime.isValid() ? wxMaUser.createTime.toJSON() : null,
      updateTime: wxMaUser.updateTime != null && wxMaUser.updateTime.isValid() ? wxMaUser.updateTime.toJSON() : null
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
      res.body.forEach((wxMaUser: IWxMaUser) => {
        wxMaUser.createTime = wxMaUser.createTime != null ? moment(wxMaUser.createTime) : null;
        wxMaUser.updateTime = wxMaUser.updateTime != null ? moment(wxMaUser.updateTime) : null;
      });
    }
    return res;
  }
}
