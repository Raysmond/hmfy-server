import { Moment } from 'moment';

export const enum ParkMsgType {
  IN = 'IN',
  OUT = 'OUT'
}

export interface IParkMsg {
  id?: number;
  parkid?: string;
  service?: string;
  createTime?: Moment;
  body?: string;
  type?: ParkMsgType;
}

export class ParkMsg implements IParkMsg {
  constructor(
    public id?: number,
    public parkid?: string,
    public service?: string,
    public createTime?: Moment,
    public body?: string,
    public type?: ParkMsgType
  ) {}
}
