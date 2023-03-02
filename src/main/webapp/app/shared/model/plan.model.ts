import { Moment } from 'moment';

export interface IPlan {
  id?: number;
  planNumber?: string;
  location?: string;
  workDay?: Moment;
  stockName?: string;
  loadingStartTime?: Moment;
  loadingEndTime?: Moment;
  weightSum?: number;
  operator?: string;
  operation?: string;
  opPosition?: string;
  channel?: string;
  comment?: string;
  createTime?: Moment;
  updateTime?: Moment;
}

export class Plan implements IPlan {
  constructor(
    public id?: number,
    public planNumber?: string,
    public location?: string,
    public workDay?: Moment,
    public stockName?: string,
    public loadingStartTime?: Moment,
    public loadingEndTime?: Moment,
    public weightSum?: number,
    public operator?: string,
    public operation?: string,
    public opPosition?: string,
    public channel?: string,
    public comment?: string,
    public createTime?: Moment,
    public updateTime?: Moment
  ) {}
}
