import { Moment } from 'moment';

export interface IShipPlan {
  id?: number;
  company?: string;
  applyId?: number;
  applyNumber?: string;
  truckNumber?: string;
  auditStatus?: number;
  gateTime?: Moment;
  leaveTime?: Moment;
  deliverTime?: Moment;
  allowInTime?: Moment;
  createTime?: Moment;
  updateTime?: Moment;
  userLogin?: string;
  userId?: number;
  toUserLogin?: string;
  toUserId?: number;
}

export class ShipPlan implements IShipPlan {
  constructor(
    public id?: number,
    public company?: string,
    public applyId?: number,
    public applyNumber?: string,
    public truckNumber?: string,
    public auditStatus?: number,
    public gateTime?: Moment,
    public leaveTime?: Moment,
    public deliverTime?: Moment,
    public allowInTime?: Moment,
    public createTime?: Moment,
    public updateTime?: Moment,
    public userLogin?: string,
    public userId?: number,
    public toUserLogin?: string,
    public toUserId?: number
  ) {}
}
