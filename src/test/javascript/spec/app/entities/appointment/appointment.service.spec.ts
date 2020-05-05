/* tslint:disable max-line-length */
import {TestBed, getTestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {of} from 'rxjs';
import {take, map} from 'rxjs/operators';
import * as moment from 'moment';
import {DATE_TIME_FORMAT} from 'app/shared/constants/input.constants';
import {AppointmentService} from 'app/entities/appointment/appointment.service';
import {IAppointment, Appointment, AppointmentStatus} from 'app/shared/model/appointment.model';

describe('Service Tests', () => {
  describe('Appointment Service', () => {
    let injector: TestBed;
    let service: AppointmentService;
    let httpMock: HttpTestingController;
    let elemDefault: IAppointment;
    let expectedResult;
    let currentDate: moment.Moment;
    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule]
      });
      expectedResult = {};
      injector = getTestBed();
      service = injector.get(AppointmentService);
      httpMock = injector.get(HttpTestingController);
      currentDate = moment();

      elemDefault = new Appointment(
        0,
        'AAAAAAA',
        'AAAAAAA',
        0,
        'BBBBBB',
        0,
        false,
        AppointmentStatus.CREATE,
        0,
        false,
        currentDate,
        currentDate,
        currentDate,
        currentDate,
        currentDate,
        currentDate
      );
    });

    describe('Service methods', () => {
      it('should find an element', async () => {
        const returnedFromService = Object.assign(
          {
            createTime: currentDate.format(DATE_TIME_FORMAT),
            updateTime: currentDate.format(DATE_TIME_FORMAT),
            startTime: currentDate.format(DATE_TIME_FORMAT),
            enterTime: currentDate.format(DATE_TIME_FORMAT),
            leaveTime: currentDate.format(DATE_TIME_FORMAT),
            expireTime: currentDate.format(DATE_TIME_FORMAT)
          },
          elemDefault
        );
        service
          .find(123)
          .pipe(take(1))
          .subscribe(resp => (expectedResult = resp));

        const req = httpMock.expectOne({method: 'GET'});
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject({body: elemDefault});
      });

      it('should create a Appointment', async () => {
        const returnedFromService = Object.assign(
          {
            id: 0,
            createTime: currentDate.format(DATE_TIME_FORMAT),
            updateTime: currentDate.format(DATE_TIME_FORMAT),
            startTime: currentDate.format(DATE_TIME_FORMAT),
            enterTime: currentDate.format(DATE_TIME_FORMAT),
            leaveTime: currentDate.format(DATE_TIME_FORMAT),
            expireTime: currentDate.format(DATE_TIME_FORMAT)
          },
          elemDefault
        );
        const expected = Object.assign(
          {
            createTime: currentDate,
            updateTime: currentDate,
            startTime: currentDate,
            enterTime: currentDate,
            leaveTime: currentDate,
            expireTime: currentDate
          },
          returnedFromService
        );
        service
          .create(new Appointment(null))
          .pipe(take(1))
          .subscribe(resp => (expectedResult = resp));
        const req = httpMock.expectOne({method: 'POST'});
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject({body: expected});
      });

      it('should update a Appointment', async () => {
        const returnedFromService = Object.assign(
          {
            licensePlateNumber: 'BBBBBB',
            driver: 'BBBBBB',
            applyId: 1,
            applyNumber: 'BBBBBB',
            number: 1,
            valid: true,
            status: 'BBBBBB',
            queueNumber: 1,
            vip: true,
            createTime: currentDate.format(DATE_TIME_FORMAT),
            updateTime: currentDate.format(DATE_TIME_FORMAT),
            startTime: currentDate.format(DATE_TIME_FORMAT),
            enterTime: currentDate.format(DATE_TIME_FORMAT),
            leaveTime: currentDate.format(DATE_TIME_FORMAT),
            expireTime: currentDate.format(DATE_TIME_FORMAT)
          },
          elemDefault
        );

        const expected = Object.assign(
          {
            createTime: currentDate,
            updateTime: currentDate,
            startTime: currentDate,
            enterTime: currentDate,
            leaveTime: currentDate,
            expireTime: currentDate
          },
          returnedFromService
        );
        service
          .update(expected)
          .pipe(take(1))
          .subscribe(resp => (expectedResult = resp));
        const req = httpMock.expectOne({method: 'PUT'});
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject({body: expected});
      });

      it('should return a list of Appointment', async () => {
        const returnedFromService = Object.assign(
          {
            licensePlateNumber: 'BBBBBB',
            driver: 'BBBBBB',
            applyId: 1,
            number: 1,
            valid: true,
            status: 'BBBBBB',
            queueNumber: 1,
            vip: true,
            createTime: currentDate.format(DATE_TIME_FORMAT),
            updateTime: currentDate.format(DATE_TIME_FORMAT),
            startTime: currentDate.format(DATE_TIME_FORMAT),
            enterTime: currentDate.format(DATE_TIME_FORMAT),
            leaveTime: currentDate.format(DATE_TIME_FORMAT),
            expireTime: currentDate.format(DATE_TIME_FORMAT)
          },
          elemDefault
        );
        const expected = Object.assign(
          {
            createTime: currentDate,
            updateTime: currentDate,
            startTime: currentDate,
            enterTime: currentDate,
            leaveTime: currentDate,
            expireTime: currentDate
          },
          returnedFromService
        );
        service
          .query(expected)
          .pipe(
            take(1),
            map(resp => resp.body)
          )
          .subscribe(body => (expectedResult = body));
        const req = httpMock.expectOne({method: 'GET'});
        req.flush([returnedFromService]);
        httpMock.verify();
        expect(expectedResult).toContainEqual(expected);
      });

      it('should delete a Appointment', async () => {
        const rxPromise = service.delete(123).subscribe(resp => (expectedResult = resp.ok));

        const req = httpMock.expectOne({method: 'DELETE'});
        req.flush({status: 200});
        expect(expectedResult);
      });
    });

    afterEach(() => {
      httpMock.verify();
    });
  });
});
