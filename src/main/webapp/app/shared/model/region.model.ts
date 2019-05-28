import { Moment } from 'moment';

export interface IRegion {
  id?: number;
  name?: string;
  quota?: number;
  startTime?: string;
  endTime?: string;
  days?: string;
  open?: boolean;
  createTime?: Moment;
  updateTime?: Moment;
  validTime?: number;
}

export class Region implements IRegion {
  constructor(
    public id?: number,
    public name?: string,
    public quota?: number,
    public startTime?: string,
    public endTime?: string,
    public days?: string,
    public open?: boolean,
    public createTime?: Moment,
    public updateTime?: Moment,
    public validTime?: number
  ) {
    this.open = this.open || false;
  }
}
