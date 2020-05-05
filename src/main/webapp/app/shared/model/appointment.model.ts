import { Moment } from 'moment';

export const enum AppointmentStatus {
  CREATE = 'CREATE',
  WAIT = 'WAIT',
  START = 'START',
  ENTER = 'ENTER',
  LEAVE = 'LEAVE'
}

export interface IAppointment {
  id?: number;
  licensePlateNumber?: string;
  driver?: string;
  applyId?: number;
  applyNumber?: string;
  number?: number;
  valid?: boolean;
  status?: AppointmentStatus;
  queueNumber?: number;
  vip?: boolean;
  createTime?: Moment;
  updateTime?: Moment;
  startTime?: Moment;
  enterTime?: Moment;
  leaveTime?: Moment;
  expireTime?: Moment;
  regionName?: string;
  regionId?: number;
  userLogin?: string;
  userId?: number;
}

export class Appointment implements IAppointment {
  constructor(
    public id?: number,
    public licensePlateNumber?: string,
    public driver?: string,
    public applyId?: number,
    public applyNumber?: string,
    public number?: number,
    public valid?: boolean,
    public status?: AppointmentStatus,
    public queueNumber?: number,
    public vip?: boolean,
    public createTime?: Moment,
    public updateTime?: Moment,
    public startTime?: Moment,
    public enterTime?: Moment,
    public leaveTime?: Moment,
    public expireTime?: Moment,
    public regionName?: string,
    public regionId?: number,
    public userLogin?: string,
    public userId?: number
  ) {
    this.valid = this.valid || false;
    this.vip = this.vip || false;
  }
}
