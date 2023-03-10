/* tslint:disable max-line-length */
import { TestBed, getTestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { of } from 'rxjs';
import { take, map } from 'rxjs/operators';
import * as moment from 'moment';
import { DATE_TIME_FORMAT } from 'app/shared/constants/input.constants';
import { ParkMsgService } from 'app/entities/park-msg/park-msg.service';
import { IParkMsg, ParkMsg, ParkMsgType } from 'app/shared/model/park-msg.model';

describe('Service Tests', () => {
  describe('ParkMsg Service', () => {
    let injector: TestBed;
    let service: ParkMsgService;
    let httpMock: HttpTestingController;
    let elemDefault: IParkMsg;
    let expectedResult;
    let currentDate: moment.Moment;
    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule]
      });
      expectedResult = {};
      injector = getTestBed();
      service = injector.get(ParkMsgService);
      httpMock = injector.get(HttpTestingController);
      currentDate = moment();

      elemDefault = new ParkMsg(0, 'AAAAAAA', 'AAAAAAA', 'AAAAAAA', currentDate, currentDate, 'AAAAAAA', ParkMsgType.IN, 0);
    });

    describe('Service methods', () => {
      it('should find an element', async () => {
        const returnedFromService = Object.assign(
          {
            createTime: currentDate.format(DATE_TIME_FORMAT),
            sendTime: currentDate.format(DATE_TIME_FORMAT)
          },
          elemDefault
        );
        service
          .find(123)
          .pipe(take(1))
          .subscribe(resp => (expectedResult = resp));

        const req = httpMock.expectOne({ method: 'GET' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject({ body: elemDefault });
      });

      it('should create a ParkMsg', async () => {
        const returnedFromService = Object.assign(
          {
            id: 0,
            createTime: currentDate.format(DATE_TIME_FORMAT),
            sendTime: currentDate.format(DATE_TIME_FORMAT)
          },
          elemDefault
        );
        const expected = Object.assign(
          {
            createTime: currentDate,
            sendTime: currentDate
          },
          returnedFromService
        );
        service
          .create(new ParkMsg(null))
          .pipe(take(1))
          .subscribe(resp => (expectedResult = resp));
        const req = httpMock.expectOne({ method: 'POST' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject({ body: expected });
      });

      it('should update a ParkMsg', async () => {
        const returnedFromService = Object.assign(
          {
            parkid: 'BBBBBB',
            service: 'BBBBBB',
            truckNumber: 'BBBBBB',
            createTime: currentDate.format(DATE_TIME_FORMAT),
            sendTime: currentDate.format(DATE_TIME_FORMAT),
            body: 'BBBBBB',
            type: 'BBBBBB',
            sendTimes: 1
          },
          elemDefault
        );

        const expected = Object.assign(
          {
            createTime: currentDate,
            sendTime: currentDate
          },
          returnedFromService
        );
        service
          .update(expected)
          .pipe(take(1))
          .subscribe(resp => (expectedResult = resp));
        const req = httpMock.expectOne({ method: 'PUT' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject({ body: expected });
      });

      it('should return a list of ParkMsg', async () => {
        const returnedFromService = Object.assign(
          {
            parkid: 'BBBBBB',
            service: 'BBBBBB',
            truckNumber: 'BBBBBB',
            createTime: currentDate.format(DATE_TIME_FORMAT),
            sendTime: currentDate.format(DATE_TIME_FORMAT),
            body: 'BBBBBB',
            type: 'BBBBBB',
            sendTimes: 1
          },
          elemDefault
        );
        const expected = Object.assign(
          {
            createTime: currentDate,
            sendTime: currentDate
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
        const req = httpMock.expectOne({ method: 'GET' });
        req.flush([returnedFromService]);
        httpMock.verify();
        expect(expectedResult).toContainEqual(expected);
      });

      it('should delete a ParkMsg', async () => {
        const rxPromise = service.delete(123).subscribe(resp => (expectedResult = resp.ok));

        const req = httpMock.expectOne({ method: 'DELETE' });
        req.flush({ status: 200 });
        expect(expectedResult);
      });
    });

    afterEach(() => {
      httpMock.verify();
    });
  });
});
