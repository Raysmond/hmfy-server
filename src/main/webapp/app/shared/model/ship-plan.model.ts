import { Moment } from 'moment';

export interface IShipPlan {
  id?: number;
  company?: string;
  applyId?: number;
  applyNumber?: string;
  appointmentNumber?: string;
  truckNumber?: string;
  auditStatus?: number;
  productName?: string;
  deliverPosition?: string;
  valid?: boolean;
  gateTime?: Moment;
  leaveTime?: Moment;
  deliverTime?: Moment;
  allowInTime?: Moment;
  loadingStartTime?: Moment;
  loadingEndTime?: Moment;
  createTime?: Moment;
  updateTime?: Moment;
  syncTime?: Moment;
  tareAlert?: boolean;
  leaveAlert?: boolean;
  netWeight?: number;
  weigherNo?: string;
  vip?: boolean;
  userLogin?: string;
  userId?: number;
}

export class ShipPlan implements IShipPlan {
  constructor(
    public id?: number,
    public company?: string,
    public applyId?: number,
    public applyNumber?: string,
    public appointmentNumber?: string,
    public truckNumber?: string,
    public auditStatus?: number,
    public productName?: string,
    public deliverPosition?: string,
    public valid?: boolean,
    public gateTime?: Moment,
    public leaveTime?: Moment,
    public deliverTime?: Moment,
    public allowInTime?: Moment,
    public loadingStartTime?: Moment,
    public loadingEndTime?: Moment,
    public createTime?: Moment,
    public updateTime?: Moment,
    public syncTime?: Moment,
    public tareAlert?: boolean,
    public leaveAlert?: boolean,
    public netWeight?: number,
    public weigherNo?: string,
    public vip?: boolean,
    public userLogin?: string,
    public userId?: number
  ) {
    this.valid = this.valid || false;
    this.tareAlert = this.tareAlert || false;
    this.leaveAlert = this.leaveAlert || false;
    this.vip = this.vip || false;
  }
}
