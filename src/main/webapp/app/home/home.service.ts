import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IRegionStat } from './home.model';

@Injectable({ providedIn: 'root' })
export class HomeService {
  public regionStatUrl = SERVER_API_URL + 'api/dashboard/region-stats';

  constructor(private http: HttpClient) {}

  getRegionStats(): Observable<HttpResponse<IRegionStat>> {
    return this.http.get<IRegionStat>(this.regionStatUrl, { observe: 'response' });
  }
}
