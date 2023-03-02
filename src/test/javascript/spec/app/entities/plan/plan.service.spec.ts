/* tslint:disable max-line-length */
import { TestBed, getTestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { of } from 'rxjs';
import { take, map } from 'rxjs/operators';
import * as moment from 'moment';
import { DATE_FORMAT, DATE_TIME_FORMAT } from 'app/shared/constants/input.constants';
import { PlanService } from 'app/entities/plan/plan.service';
import { IPlan, Plan } from 'app/shared/model/plan.model';

describe('Service Tests', () => {
  describe('Plan Service', () => {
    let injector: TestBed;
    let service: PlanService;
    let httpMock: HttpTestingController;
    let elemDefault: IPlan;
    let expectedResult;
    let currentDate: moment.Moment;
    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule]
      });
      expectedResult = {};
      injector = getTestBed();
      service = injector.get(PlanService);
      httpMock = injector.get(HttpTestingController);
      currentDate = moment();

      elemDefault = new Plan(
        0,
        'AAAAAAA',
        'AAAAAAA',
        currentDate,
        'AAAAAAA',
        currentDate,
        currentDate,
        0,
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        currentDate,
        currentDate
      );
    });

    describe('Service methods', () => {
      it('should find an element', async () => {
        const returnedFromService = Object.assign(
          {
            workDay: currentDate.format(DATE_FORMAT),
            loadingStartTime: currentDate.format(DATE_TIME_FORMAT),
            loadingEndTime: currentDate.format(DATE_TIME_FORMAT),
            createTime: currentDate.format(DATE_TIME_FORMAT),
            updateTime: currentDate.format(DATE_TIME_FORMAT)
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

      it('should create a Plan', async () => {
        const returnedFromService = Object.assign(
          {
            id: 0,
            workDay: currentDate.format(DATE_FORMAT),
            loadingStartTime: currentDate.format(DATE_TIME_FORMAT),
            loadingEndTime: currentDate.format(DATE_TIME_FORMAT),
            createTime: currentDate.format(DATE_TIME_FORMAT),
            updateTime: currentDate.format(DATE_TIME_FORMAT)
          },
          elemDefault
        );
        const expected = Object.assign(
          {
            workDay: currentDate,
            loadingStartTime: currentDate,
            loadingEndTime: currentDate,
            createTime: currentDate,
            updateTime: currentDate
          },
          returnedFromService
        );
        service
          .create(new Plan(null))
          .pipe(take(1))
          .subscribe(resp => (expectedResult = resp));
        const req = httpMock.expectOne({ method: 'POST' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject({ body: expected });
      });

      it('should update a Plan', async () => {
        const returnedFromService = Object.assign(
          {
            planNumber: 'BBBBBB',
            location: 'BBBBBB',
            workDay: currentDate.format(DATE_FORMAT),
            stockName: 'BBBBBB',
            loadingStartTime: currentDate.format(DATE_TIME_FORMAT),
            loadingEndTime: currentDate.format(DATE_TIME_FORMAT),
            weightSum: 1,
            operator: 'BBBBBB',
            operation: 'BBBBBB',
            opPosition: 'BBBBBB',
            channel: 'BBBBBB',
            comment: 'BBBBBB',
            createTime: currentDate.format(DATE_TIME_FORMAT),
            updateTime: currentDate.format(DATE_TIME_FORMAT)
          },
          elemDefault
        );

        const expected = Object.assign(
          {
            workDay: currentDate,
            loadingStartTime: currentDate,
            loadingEndTime: currentDate,
            createTime: currentDate,
            updateTime: currentDate
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

      it('should return a list of Plan', async () => {
        const returnedFromService = Object.assign(
          {
            planNumber: 'BBBBBB',
            location: 'BBBBBB',
            workDay: currentDate.format(DATE_FORMAT),
            stockName: 'BBBBBB',
            loadingStartTime: currentDate.format(DATE_TIME_FORMAT),
            loadingEndTime: currentDate.format(DATE_TIME_FORMAT),
            weightSum: 1,
            operator: 'BBBBBB',
            operation: 'BBBBBB',
            opPosition: 'BBBBBB',
            channel: 'BBBBBB',
            comment: 'BBBBBB',
            createTime: currentDate.format(DATE_TIME_FORMAT),
            updateTime: currentDate.format(DATE_TIME_FORMAT)
          },
          elemDefault
        );
        const expected = Object.assign(
          {
            workDay: currentDate,
            loadingStartTime: currentDate,
            loadingEndTime: currentDate,
            createTime: currentDate,
            updateTime: currentDate
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

      it('should delete a Plan', async () => {
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
