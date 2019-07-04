import { Moment } from 'moment';

export const enum ParkMsgType {
  IN = 'IN',
  OUT = 'OUT'
}

export interface IParkMsg {
  id?: number;
  parkid?: string;
  service?: string;
  truckNumber?: string;
  createTime?: Moment;
  sendTime?: Moment;
  body?: string;
  type?: ParkMsgType;
  sendTimes?: number;
}

export class ParkMsg implements IParkMsg {
  constructor(
    public id?: number,
    public parkid?: string,
    public service?: string,
    public truckNumber?: string,
    public createTime?: Moment,
    public sendTime?: Moment,
    public body?: string,
    public type?: ParkMsgType,
    public sendTimes?: number
  ) {}
}
