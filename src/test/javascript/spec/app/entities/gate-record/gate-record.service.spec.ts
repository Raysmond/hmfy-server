/* tslint:disable max-line-length */
import { TestBed, getTestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { of } from 'rxjs';
import { take, map } from 'rxjs/operators';
import * as moment from 'moment';
import { DATE_TIME_FORMAT } from 'app/shared/constants/input.constants';
import { GateRecordService } from 'app/entities/gate-record/gate-record.service';
import { IGateRecord, GateRecord, RecordType } from 'app/shared/model/gate-record.model';

describe('Service Tests', () => {
  describe('GateRecord Service', () => {
    let injector: TestBed;
    let service: GateRecordService;
    let httpMock: HttpTestingController;
    let elemDefault: IGateRecord;
    let expectedResult;
    let currentDate: moment.Moment;
    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule]
      });
      expectedResult = {};
      injector = getTestBed();
      service = injector.get(GateRecordService);
      httpMock = injector.get(HttpTestingController);
      currentDate = moment();

      elemDefault = new GateRecord(0, RecordType.IN, 'AAAAAAA', currentDate, 'AAAAAAA', 'AAAAAAA', currentDate, 0, 'AAAAAAA', currentDate);
    });

    describe('Service methods', () => {
      it('should find an element', async () => {
        const returnedFromService = Object.assign(
          {
            recordTime: currentDate.format(DATE_TIME_FORMAT),
            createTime: currentDate.format(DATE_TIME_FORMAT),
            modifyTime: currentDate.format(DATE_TIME_FORMAT)
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

      it('should create a GateRecord', async () => {
        const returnedFromService = Object.assign(
          {
            id: 0,
            recordTime: currentDate.format(DATE_TIME_FORMAT),
            createTime: currentDate.format(DATE_TIME_FORMAT),
            modifyTime: currentDate.format(DATE_TIME_FORMAT)
          },
          elemDefault
        );
        const expected = Object.assign(
          {
            recordTime: currentDate,
            createTime: currentDate,
            modifyTime: currentDate
          },
          returnedFromService
        );
        service
          .create(new GateRecord(null))
          .pipe(take(1))
          .subscribe(resp => (expectedResult = resp));
        const req = httpMock.expectOne({ method: 'POST' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject({ body: expected });
      });

      it('should update a GateRecord', async () => {
        const returnedFromService = Object.assign(
          {
            recordType: 'BBBBBB',
            truckNumber: 'BBBBBB',
            recordTime: currentDate.format(DATE_TIME_FORMAT),
            data: 'BBBBBB',
            rid: 'BBBBBB',
            createTime: currentDate.format(DATE_TIME_FORMAT),
            regionId: 1,
            dataMd5: 'BBBBBB',
            modifyTime: currentDate.format(DATE_TIME_FORMAT)
          },
          elemDefault
        );

        const expected = Object.assign(
          {
            recordTime: currentDate,
            createTime: currentDate,
            modifyTime: currentDate
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

      it('should return a list of GateRecord', async () => {
        const returnedFromService = Object.assign(
          {
            recordType: 'BBBBBB',
            truckNumber: 'BBBBBB',
            recordTime: currentDate.format(DATE_TIME_FORMAT),
            data: 'BBBBBB',
            rid: 'BBBBBB',
            createTime: currentDate.format(DATE_TIME_FORMAT),
            regionId: 1,
            dataMd5: 'BBBBBB',
            modifyTime: currentDate.format(DATE_TIME_FORMAT)
          },
          elemDefault
        );
        const expected = Object.assign(
          {
            recordTime: currentDate,
            createTime: currentDate,
            modifyTime: currentDate
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

      it('should delete a GateRecord', async () => {
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
