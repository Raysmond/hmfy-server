export interface IRegionStat {
  day?: string;
  data?: any;
}

export class RegionStat implements IRegionStat {
  constructor(public day?: string, public data?: any) {
    this.day = day ? day : null;
    this.data = data ? data : null;
  }
}
