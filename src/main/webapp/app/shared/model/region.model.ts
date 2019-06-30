import { Moment } from 'moment';

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
  parkId?: string;
  validTime?: number;
  queueQuota?: number;
  queueValidTime?: number;
  createTime?: Moment;
  updateTime?: Moment;
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
    public parkId?: string,
    public validTime?: number,
    public queueQuota?: number,
    public queueValidTime?: number,
    public createTime?: Moment,
    public updateTime?: Moment
  ) {
    this.open = this.open || false;
    this.autoAppointment = this.autoAppointment || false;
  }
}
