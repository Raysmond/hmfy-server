/* tslint:disable max-line-length */
import { TestBed, getTestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { of } from 'rxjs';
import { take, map } from 'rxjs/operators';
import * as moment from 'moment';
import { DATE_TIME_FORMAT } from 'app/shared/constants/input.constants';
import { WxMaUserService } from 'app/entities/wx-ma-user/wx-ma-user.service';
import { IWxMaUser, WxMaUser } from 'app/shared/model/wx-ma-user.model';

describe('Service Tests', () => {
  describe('WxMaUser Service', () => {
    let injector: TestBed;
    let service: WxMaUserService;
    let httpMock: HttpTestingController;
    let elemDefault: IWxMaUser;
    let expectedResult;
    let currentDate: moment.Moment;
    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule]
      });
      expectedResult = {};
      injector = getTestBed();
      service = injector.get(WxMaUserService);
      httpMock = injector.get(HttpTestingController);
      currentDate = moment();

      elemDefault = new WxMaUser(
        0,
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        currentDate,
        currentDate,
        'AAAAAAA',
        'AAAAAAA'
      );
    });

    describe('Service methods', () => {
      it('should find an element', async () => {
        const returnedFromService = Object.assign(
          {
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

      it('should create a WxMaUser', async () => {
        const returnedFromService = Object.assign(
          {
            id: 0,
            createTime: currentDate.format(DATE_TIME_FORMAT),
            updateTime: currentDate.format(DATE_TIME_FORMAT)
          },
          elemDefault
        );
        const expected = Object.assign(
          {
            createTime: currentDate,
            updateTime: currentDate
          },
          returnedFromService
        );
        service
          .create(new WxMaUser(null))
          .pipe(take(1))
          .subscribe(resp => (expectedResult = resp));
        const req = httpMock.expectOne({ method: 'POST' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject({ body: expected });
      });

      it('should update a WxMaUser', async () => {
        const returnedFromService = Object.assign(
          {
            openId: 'BBBBBB',
            nickName: 'BBBBBB',
            gender: 'BBBBBB',
            language: 'BBBBBB',
            city: 'BBBBBB',
            province: 'BBBBBB',
            country: 'BBBBBB',
            avatarUrl: 'BBBBBB',
            unionId: 'BBBBBB',
            watermark: 'BBBBBB',
            createTime: currentDate.format(DATE_TIME_FORMAT),
            updateTime: currentDate.format(DATE_TIME_FORMAT),
            phone: 'BBBBBB',
            appId: 'BBBBBB'
          },
          elemDefault
        );

        const expected = Object.assign(
          {
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

      it('should return a list of WxMaUser', async () => {
        const returnedFromService = Object.assign(
          {
            openId: 'BBBBBB',
            nickName: 'BBBBBB',
            gender: 'BBBBBB',
            language: 'BBBBBB',
            city: 'BBBBBB',
            province: 'BBBBBB',
            country: 'BBBBBB',
            avatarUrl: 'BBBBBB',
            unionId: 'BBBBBB',
            watermark: 'BBBBBB',
            createTime: currentDate.format(DATE_TIME_FORMAT),
            updateTime: currentDate.format(DATE_TIME_FORMAT),
            phone: 'BBBBBB',
            appId: 'BBBBBB'
          },
          elemDefault
        );
        const expected = Object.assign(
          {
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

      it('should delete a WxMaUser', async () => {
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
