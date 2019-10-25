import { Moment } from 'moment';

export const enum ParkingConnectMethod {
  TCP = 'TCP',
  DATABASE = 'DATABASE'
}

export interface IRegion {
  id?: number;
  name?: string;
  quota?: number;
  vipQuota?: number;
  startTime?: string;
  endTime?: string;
  days?: string;
  open?: boolean;
  autoAppointment?: boolean;
  parkingConnectMethod?: ParkingConnectMethod;
  parkId?: string;
  validTime?: number;
  queueQuota?: number;
  queueValidTime?: number;
  createTime?: Moment;
  updateTime?: Moment;
  loadAlertTime?: number;
  leaveAlertTime?: number;
}

export class Region implements IRegion {
  constructor(
    public id?: number,
    public name?: string,
    public quota?: number,
    public vipQuota?: number,
    public startTime?: string,
    public endTime?: string,
    public days?: string,
    public open?: boolean,
    public autoAppointment?: boolean,
    public parkingConnectMethod?: ParkingConnectMethod,
    public parkId?: string,
    public validTime?: number,
    public queueQuota?: number,
    public queueValidTime?: number,
    public createTime?: Moment,
    public updateTime?: Moment,
    public loadAlertTime?: number,
    public leaveAlertTime?: number
  ) {
    this.open = this.open || false;
    this.autoAppointment = this.autoAppointment || false;
  }
}
