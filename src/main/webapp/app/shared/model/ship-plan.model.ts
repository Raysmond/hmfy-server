import { Moment } from 'moment';

export interface IShipPlan {
  id?: number;
  company?: string;
  applyId?: number;
  applyNumber?: string;
  truckNumber?: string;
  auditStatus?: number;
  productName?: string;
  deliverPosition?: string;
  valid?: boolean;
  gateTime?: Moment;
  leaveTime?: Moment;
  deliverTime?: Moment;
  allowInTime?: Moment;
  createTime?: Moment;
  updateTime?: Moment;
  syncTime?: Moment;
  userLogin?: string;
  userId?: number;
}

export class ShipPlan implements IShipPlan {
  constructor(
    public id?: number,
    public company?: string,
    public applyId?: number,
    public applyNumber?: string,
    public truckNumber?: string,
    public auditStatus?: number,
    public productName?: string,
    public deliverPosition?: string,
    public valid?: boolean,
    public gateTime?: Moment,
    public leaveTime?: Moment,
    public deliverTime?: Moment,
    public allowInTime?: Moment,
    public createTime?: Moment,
    public updateTime?: Moment,
    public syncTime?: Moment,
    public userLogin?: string,
    public userId?: number
  ) {
    this.valid = this.valid || false;
  }
}
