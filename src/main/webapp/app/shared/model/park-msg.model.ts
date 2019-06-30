import { Moment } from 'moment';

export interface IParkMsg {
  id?: number;
  parkid?: string;
  service?: string;
  createTime?: Moment;
  body?: string;
}

export class ParkMsg implements IParkMsg {
  constructor(public id?: number, public parkid?: string, public service?: string, public createTime?: Moment, public body?: string) {}
}
