import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import * as moment from 'moment';
import { DATE_FORMAT } from 'app/shared/constants/input.constants';
import { map } from 'rxjs/operators';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared';
import { IGateRecord } from 'app/shared/model/gate-record.model';

type EntityResponseType = HttpResponse<IGateRecord>;
type EntityArrayResponseType = HttpResponse<IGateRecord[]>;

@Injectable({ providedIn: 'root' })
export class GateRecordService {
  public resourceUrl = SERVER_API_URL + 'api/gate-records';

  constructor(protected http: HttpClient) {}

  create(gateRecord: IGateRecord): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(gateRecord);
    return this.http
      .post<IGateRecord>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(gateRecord: IGateRecord): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(gateRecord);
    return this.http
      .put<IGateRecord>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<IGateRecord>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<IGateRecord[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<any>> {
    return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  protected convertDateFromClient(gateRecord: IGateRecord): IGateRecord {
    const copy: IGateRecord = Object.assign({}, gateRecord, {
      recordTime: gateRecord.recordTime != null && gateRecord.recordTime.isValid() ? gateRecord.recordTime.toJSON() : null,
      createTime: gateRecord.createTime != null && gateRecord.createTime.isValid() ? gateRecord.createTime.toJSON() : null,
      modifyTime: gateRecord.modifyTime != null && gateRecord.modifyTime.isValid() ? gateRecord.modifyTime.toJSON() : null
    });
    return copy;
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.recordTime = res.body.recordTime != null ? moment(res.body.recordTime) : null;
      res.body.createTime = res.body.createTime != null ? moment(res.body.createTime) : null;
      res.body.modifyTime = res.body.modifyTime != null ? moment(res.body.modifyTime) : null;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((gateRecord: IGateRecord) => {
        gateRecord.recordTime = gateRecord.recordTime != null ? moment(gateRecord.recordTime) : null;
        gateRecord.createTime = gateRecord.createTime != null ? moment(gateRecord.createTime) : null;
        gateRecord.modifyTime = gateRecord.modifyTime != null ? moment(gateRecord.modifyTime) : null;
      });
    }
    return res;
  }
}
