import { Component, OnInit } from '@angular/core';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { HttpClient, HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';

import { LoginModalService, AccountService, Account } from 'app/core';
import { HomeService } from './home.service';
import { IRegionStat } from './home.model';
import { AppointmentService } from 'app/entities/appointment';
import { IAppointment } from 'app/shared/model/appointment.model';

@Component({
  selector: 'jhi-home',
  templateUrl: './home.component.html',
  styleUrls: ['home.scss']
})
export class HomeComponent implements OnInit {
  account: Account;
  modalRef: NgbModalRef;
  appointments: IAppointment[];

  public resourceUrl = SERVER_API_URL + 'api/admin-dashboard';

  constructor(
    private accountService: AccountService,
    private loginModalService: LoginModalService,
    protected appointmentService: AppointmentService,
    private eventManager: JhiEventManager,
    private homeService: HomeService,
    private http: HttpClient
  ) {}

  loadLatestAppointments() {
    const filterParams = {
      page: 0,
      size: 4,
      sort: ['id,desc']
    };

    this.appointmentService.query(filterParams).subscribe(
      (res: HttpResponse<IAppointment[]>) => {
        // console.log(res.body);
        this.appointments = res.body;
      },
      (res: HttpErrorResponse) => {
        console.log(res.message);
      }
    );
  }

  ngOnInit() {
    this.accountService.identity().then((account: Account) => {
      this.account = account;
    });
    this.registerAuthenticationSuccess();

    this.http.get(`${this.resourceUrl}/region-stats`).subscribe(
      (res: HttpResponse<any>) => {
        console.log(res);
      },
      (res: HttpErrorResponse) => {
        console.log(res.message);
      }
    );

    this.loadLatestAppointments();
  }

  registerAuthenticationSuccess() {
    this.eventManager.subscribe('authenticationSuccess', message => {
      this.accountService.identity().then(account => {
        this.account = account;
      });
    });
  }

  isAuthenticated() {
    return this.accountService.isAuthenticated();
  }

  login() {
    this.modalRef = this.loginModalService.open();
  }

  trackId(index: number, item: IAppointment) {
    return item.id;
  }
}
