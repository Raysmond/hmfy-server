export interface IUser {
  id?: any;
  login?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  activated?: boolean;
  langKey?: string;
  authorities?: any[];
  createdBy?: string;
  createdDate?: Date;
  lastModifiedBy?: string;
  lastModifiedDate?: Date;
  password?: string;
  userInfo?: any;
  regionId?: any;
  regionName?: string;
  rawPassword?: string;
  truckNumber?: string;
  company?: string;
  carCapacity?: any;
  carCompany?: string;
  memo?: string;
  phone?: string;
}

export class User implements IUser {
  constructor(
    public id?: any,
    public login?: string,
    public firstName?: string,
    public lastName?: string,
    public email?: string,
    public activated?: boolean,
    public langKey?: string,
    public authorities?: any[],
    public createdBy?: string,
    public createdDate?: Date,
    public lastModifiedBy?: string,
    public lastModifiedDate?: Date,
    public password?: string,
    public userInfo?: any,
    public regionId?: any,
    public regionName?: string,
    public truckNumber?: string,
    public rawPassword?: string,
    public company?: string,
    public carCompany?: string,
    public carCapacity?: string,
    public memo?: string,
    public phone?: string
  ) {
    this.id = id ? id : null;
    this.login = login ? login : null;
    this.firstName = firstName ? firstName : null;
    this.lastName = lastName ? lastName : null;
    this.email = email ? email : null;
    this.activated = activated ? activated : false;
    this.langKey = langKey ? langKey : null;
    this.authorities = authorities ? authorities : null;
    this.createdBy = createdBy ? createdBy : null;
    this.createdDate = createdDate ? createdDate : null;
    this.lastModifiedBy = lastModifiedBy ? lastModifiedBy : null;
    this.lastModifiedDate = lastModifiedDate ? lastModifiedDate : null;
    this.password = password ? password : null;
    this.userInfo = userInfo ? userInfo : null;
    this.regionId = regionId ? regionId : null;
    this.regionName = regionName ? regionName : null;
    this.truckNumber = truckNumber ? truckNumber : null;
    this.rawPassword = rawPassword ? rawPassword : null;
    this.company = company ? company : null;
    this.carCapacity = carCapacity ? carCapacity : null;
    this.carCompany = carCompany ? carCompany : null;
    this.memo = memo ? memo : null;
    this.phone = phone ? phone : null;
  }
}
