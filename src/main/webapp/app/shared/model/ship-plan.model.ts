import { Moment } from 'moment';

export const enum ShipMethod {
  LAND = 'LAND',
  AIR = 'AIR',
  WATER = 'WATER'
}

export interface IShipPlan {
  id?: number;
  company?: string;
  demandedAmount?: number;
  finishAmount?: number;
  remainAmount?: number;
  availableAmount?: number;
  shipMethond?: ShipMethod;
  shipNumber?: string;
  endTime?: Moment;
  createTime?: Moment;
  updateTime?: Moment;
  licensePlateNumber?: string;
  driver?: string;
  phone?: string;
  userLogin?: string;
  userId?: number;
  toUserLogin?: string;
  toUserId?: number;
}

export class ShipPlan implements IShipPlan {
  constructor(
    public id?: number,
    public company?: string,
    public demandedAmount?: number,
    public finishAmount?: number,
    public remainAmount?: number,
    public availableAmount?: number,
    public shipMethond?: ShipMethod,
    public shipNumber?: string,
    public endTime?: Moment,
    public createTime?: Moment,
    public updateTime?: Moment,
    public licensePlateNumber?: string,
    public driver?: string,
    public phone?: string,
    public userLogin?: string,
    public userId?: number,
    public toUserLogin?: string,
    public toUserId?: number
  ) {}
}
