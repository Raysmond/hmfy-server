/* tslint:disable max-line-length */
import { TestBed, getTestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { of } from 'rxjs';
import { take, map } from 'rxjs/operators';
import * as moment from 'moment';
import { DATE_TIME_FORMAT } from 'app/shared/constants/input.constants';
import { ShipPlanService } from 'app/entities/ship-plan/ship-plan.service';
import { IShipPlan, ShipPlan } from 'app/shared/model/ship-plan.model';

describe('Service Tests', () => {
  describe('ShipPlan Service', () => {
    let injector: TestBed;
    let service: ShipPlanService;
    let httpMock: HttpTestingController;
    let elemDefault: IShipPlan;
    let expectedResult;
    let currentDate: moment.Moment;
    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule]
      });
      expectedResult = {};
      injector = getTestBed();
      service = injector.get(ShipPlanService);
      httpMock = injector.get(HttpTestingController);
      currentDate = moment();

      elemDefault = new ShipPlan(
        0,
        'AAAAAAA',
        0,
        'AAAAAAA',
        'AAAAAAA',
        0,
        'AAAAAAA',
        'AAAAAAA',
        false,
        currentDate,
        currentDate,
        currentDate,
        currentDate,
        currentDate,
        currentDate,
        currentDate,
        currentDate,
        currentDate,
        false,
        false,
        0,
        'AAAAAAA',
        false
      );
    });

    describe('Service methods', () => {
      it('should find an element', async () => {
        const returnedFromService = Object.assign(
          {
            gateTime: currentDate.format(DATE_TIME_FORMAT),
            leaveTime: currentDate.format(DATE_TIME_FORMAT),
            deliverTime: currentDate.format(DATE_TIME_FORMAT),
            allowInTime: currentDate.format(DATE_TIME_FORMAT),
            loadingStartTime: currentDate.format(DATE_TIME_FORMAT),
            loadingEndTime: currentDate.format(DATE_TIME_FORMAT),
            createTime: currentDate.format(DATE_TIME_FORMAT),
            updateTime: currentDate.format(DATE_TIME_FORMAT),
            syncTime: currentDate.format(DATE_TIME_FORMAT)
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

      it('should create a ShipPlan', async () => {
        const returnedFromService = Object.assign(
          {
            id: 0,
            gateTime: currentDate.format(DATE_TIME_FORMAT),
            leaveTime: currentDate.format(DATE_TIME_FORMAT),
            deliverTime: currentDate.format(DATE_TIME_FORMAT),
            allowInTime: currentDate.format(DATE_TIME_FORMAT),
            loadingStartTime: currentDate.format(DATE_TIME_FORMAT),
            loadingEndTime: currentDate.format(DATE_TIME_FORMAT),
            createTime: currentDate.format(DATE_TIME_FORMAT),
            updateTime: currentDate.format(DATE_TIME_FORMAT),
            syncTime: currentDate.format(DATE_TIME_FORMAT)
          },
          elemDefault
        );
        const expected = Object.assign(
          {
            gateTime: currentDate,
            leaveTime: currentDate,
            deliverTime: currentDate,
            allowInTime: currentDate,
            loadingStartTime: currentDate,
            loadingEndTime: currentDate,
            createTime: currentDate,
            updateTime: currentDate,
            syncTime: currentDate
          },
          returnedFromService
        );
        service
          .create(new ShipPlan(null))
          .pipe(take(1))
          .subscribe(resp => (expectedResult = resp));
        const req = httpMock.expectOne({ method: 'POST' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject({ body: expected });
      });

      it('should update a ShipPlan', async () => {
        const returnedFromService = Object.assign(
          {
            company: 'BBBBBB',
            applyId: 1,
            applyNumber: 'BBBBBB',
            truckNumber: 'BBBBBB',
            auditStatus: 1,
            productName: 'BBBBBB',
            deliverPosition: 'BBBBBB',
            valid: true,
            gateTime: currentDate.format(DATE_TIME_FORMAT),
            leaveTime: currentDate.format(DATE_TIME_FORMAT),
            deliverTime: currentDate.format(DATE_TIME_FORMAT),
            allowInTime: currentDate.format(DATE_TIME_FORMAT),
            loadingStartTime: currentDate.format(DATE_TIME_FORMAT),
            loadingEndTime: currentDate.format(DATE_TIME_FORMAT),
            createTime: currentDate.format(DATE_TIME_FORMAT),
            updateTime: currentDate.format(DATE_TIME_FORMAT),
            syncTime: currentDate.format(DATE_TIME_FORMAT),
            tareAlert: true,
            leaveAlert: true,
            netWeight: 1,
            weigherNo: 'BBBBBB',
            vip: true
          },
          elemDefault
        );

        const expected = Object.assign(
          {
            gateTime: currentDate,
            leaveTime: currentDate,
            deliverTime: currentDate,
            allowInTime: currentDate,
            loadingStartTime: currentDate,
            loadingEndTime: currentDate,
            createTime: currentDate,
            updateTime: currentDate,
            syncTime: currentDate
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

      it('should return a list of ShipPlan', async () => {
        const returnedFromService = Object.assign(
          {
            company: 'BBBBBB',
            applyId: 1,
            applyNumber: 'BBBBBB',
            truckNumber: 'BBBBBB',
            auditStatus: 1,
            productName: 'BBBBBB',
            deliverPosition: 'BBBBBB',
            valid: true,
            gateTime: currentDate.format(DATE_TIME_FORMAT),
            leaveTime: currentDate.format(DATE_TIME_FORMAT),
            deliverTime: currentDate.format(DATE_TIME_FORMAT),
            allowInTime: currentDate.format(DATE_TIME_FORMAT),
            loadingStartTime: currentDate.format(DATE_TIME_FORMAT),
            loadingEndTime: currentDate.format(DATE_TIME_FORMAT),
            createTime: currentDate.format(DATE_TIME_FORMAT),
            updateTime: currentDate.format(DATE_TIME_FORMAT),
            syncTime: currentDate.format(DATE_TIME_FORMAT),
            tareAlert: true,
            leaveAlert: true,
            netWeight: 1,
            weigherNo: 'BBBBBB',
            vip: true
          },
          elemDefault
        );
        const expected = Object.assign(
          {
            gateTime: currentDate,
            leaveTime: currentDate,
            deliverTime: currentDate,
            allowInTime: currentDate,
            loadingStartTime: currentDate,
            loadingEndTime: currentDate,
            createTime: currentDate,
            updateTime: currentDate,
            syncTime: currentDate
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

      it('should delete a ShipPlan', async () => {
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
