import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import * as moment from 'moment';
import { DATE_FORMAT } from 'app/shared/constants/input.constants';
import { map } from 'rxjs/operators';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared';
import { IAppointment } from 'app/shared/model/appointment.model';

type EntityResponseType = HttpResponse<IAppointment>;
type EntityArrayResponseType = HttpResponse<IAppointment[]>;

@Injectable({ providedIn: 'root' })
export class AppointmentService {
  public resourceUrl = SERVER_API_URL + 'api/appointments';

  constructor(protected http: HttpClient) {}

  create(appointment: IAppointment): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(appointment);
    return this.http
      .post<IAppointment>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(appointment: IAppointment): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(appointment);
    return this.http
      .put<IAppointment>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<IAppointment>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<IAppointment[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<any>> {
    return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  protected convertDateFromClient(appointment: IAppointment): IAppointment {
    const copy: IAppointment = Object.assign({}, appointment, {
      createTime: appointment.createTime != null && appointment.createTime.isValid() ? appointment.createTime.toJSON() : null,
      updateTime: appointment.updateTime != null && appointment.updateTime.isValid() ? appointment.updateTime.toJSON() : null,
      startTime: appointment.startTime != null && appointment.startTime.isValid() ? appointment.startTime.toJSON() : null,
      enterTime: appointment.enterTime != null && appointment.enterTime.isValid() ? appointment.enterTime.toJSON() : null,
      leaveTime: appointment.leaveTime != null && appointment.leaveTime.isValid() ? appointment.leaveTime.toJSON() : null,
      expireTime: appointment.expireTime != null && appointment.expireTime.isValid() ? appointment.expireTime.toJSON() : null
    });
    return copy;
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.createTime = res.body.createTime != null ? moment(res.body.createTime) : null;
      res.body.updateTime = res.body.updateTime != null ? moment(res.body.updateTime) : null;
      res.body.startTime = res.body.startTime != null ? moment(res.body.startTime) : null;
      res.body.enterTime = res.body.enterTime != null ? moment(res.body.enterTime) : null;
      res.body.leaveTime = res.body.leaveTime != null ? moment(res.body.leaveTime) : null;
      res.body.expireTime = res.body.expireTime != null ? moment(res.body.expireTime) : null;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((appointment: IAppointment) => {
        appointment.createTime = appointment.createTime != null ? moment(appointment.createTime) : null;
        appointment.updateTime = appointment.updateTime != null ? moment(appointment.updateTime) : null;
        appointment.startTime = appointment.startTime != null ? moment(appointment.startTime) : null;
        appointment.enterTime = appointment.enterTime != null ? moment(appointment.enterTime) : null;
        appointment.leaveTime = appointment.leaveTime != null ? moment(appointment.leaveTime) : null;
        appointment.expireTime = appointment.expireTime != null ? moment(appointment.expireTime) : null;
      });
    }
    return res;
  }
}
