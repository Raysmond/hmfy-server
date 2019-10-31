import { Moment } from 'moment';

export const enum RecordType {
  IN = 'IN',
  OUT = 'OUT'
}

export interface IGateRecord {
  id?: number;
  recordType?: RecordType;
  truckNumber?: string;
  recordTime?: Moment;
  data?: any;
  rid?: string;
  createTime?: Moment;
  regionId?: number;
  dataMd5?: string;
  modifyTime?: Moment;
}

export class GateRecord implements IGateRecord {
  constructor(
    public id?: number,
    public recordType?: RecordType,
    public truckNumber?: string,
    public recordTime?: Moment,
    public data?: any,
    public rid?: string,
    public createTime?: Moment,
    public regionId?: number,
    public dataMd5?: string,
    public modifyTime?: Moment
  ) {}
}
