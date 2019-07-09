import { Moment } from 'moment';

export interface IWxMaUser {
  id?: number;
  openId?: string;
  nickName?: string;
  gender?: string;
  language?: string;
  city?: string;
  province?: string;
  country?: string;
  avatarUrl?: string;
  unionId?: string;
  watermark?: string;
  createTime?: Moment;
  updateTime?: Moment;
  phone?: string;
  appId?: string;
  userLogin?: string;
  userId?: number;
}

export class WxMaUser implements IWxMaUser {
  constructor(
    public id?: number,
    public openId?: string,
    public nickName?: string,
    public gender?: string,
    public language?: string,
    public city?: string,
    public province?: string,
    public country?: string,
    public avatarUrl?: string,
    public unionId?: string,
    public watermark?: string,
    public createTime?: Moment,
    public updateTime?: Moment,
    public phone?: string,
    public appId?: string,
    public userLogin?: string,
    public userId?: number
  ) {}
}
