import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_REGION_ADMIN_URL, SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IUser } from './user.model';
import { AccountService } from 'app/core';

@Injectable({ providedIn: 'root' })
export class UserService {
  public resourceUrl = SERVER_API_URL + 'api/users';

  public regionAdminResourceUrl = SERVER_API_REGION_ADMIN_URL + 'api/users';

  constructor(protected http: HttpClient, private accountService: AccountService) {}

  getResourceUrl() {
    if (this.accountService.hasAnyAuthority(['ROLE_ADMIN'])) {
      return this.resourceUrl;
    } else {
      return this.regionAdminResourceUrl;
    }
  }

  create(user: IUser): Observable<HttpResponse<IUser>> {
    return this.http.post<IUser>(this.getResourceUrl(), user, { observe: 'response' });
  }

  update(user: IUser): Observable<HttpResponse<IUser>> {
    return this.http.put<IUser>(this.getResourceUrl(), user, { observe: 'response' });
  }

  find(login: string): Observable<HttpResponse<IUser>> {
    return this.http.get<IUser>(`${this.getResourceUrl()}/${login}`, { observe: 'response' });
  }

  query(req?: any): Observable<HttpResponse<IUser[]>> {
    const options = createRequestOption(req);
    return this.http.get<IUser[]>(this.getResourceUrl(), { params: options, observe: 'response' });
  }

  delete(login: string): Observable<HttpResponse<any>> {
    return this.http.delete(`${this.getResourceUrl()}/${login}`, { observe: 'response' });
  }

  authorities(): Observable<string[]> {
    return this.http.get<string[]>(this.getResourceUrl() + '/authorities');
  }
}
