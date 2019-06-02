import { Moment } from 'moment';

export interface ICar {
  id?: number;
  licensePlateNumber?: string;
  driver?: string;
  phone?: string;
  createTime?: Moment;
  updateTime?: Moment;
  userLogin?: string;
  userId?: number;
}

export class Car implements ICar {
  constructor(
    public id?: number,
    public licensePlateNumber?: string,
    public driver?: string,
    public phone?: string,
    public createTime?: Moment,
    public updateTime?: Moment,
    public userLogin?: string,
    public userId?: number
  ) {}
}
