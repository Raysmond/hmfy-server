import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse, HttpHeaders, HttpResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { FormBuilder, Validators } from '@angular/forms';
import { JhiEventManager, JhiParseLinks, JhiAlertService } from 'ng-jhipster';

import { IShipPlan } from 'app/shared/model/ship-plan.model';
import { AccountService } from 'app/core';

import { ITEMS_PER_PAGE } from 'app/shared';
import { ShipPlanService } from './ship-plan.service';
import { IRegion } from 'app/shared/model/region.model';
import { RegionService } from 'app/entities/region';

const beginDate = new Date();
beginDate.setDate(beginDate.getDate() - 7);

@Component({
  selector: 'jhi-ship-plan',
  templateUrl: './ship-plan-warning.component.html'
})

export class ShipPlanWarningComponent implements OnInit, OnDestroy {
  currentAccount: any;
  shipPlans: IShipPlan[];
  error: any;
  success: any;
  eventSubscriber: Subscription;
  routeData: any;
  links: any;
  totalItems: any;
  itemsPerPage: any;
  page: any;
  predicate: any;
  previousPage: any;
  reverse: any;

  searchForm = this.fb.group({
    truckNumber: null,
    auditStatus: null,
    deliverPosition: null,
    deliverTimeBegin: this.formatDate(beginDate),
    deliverTimeEnd: this.formatDate(new Date()),
    warningPlan: null
  });

  regions: IRegion[];

  constructor(
    protected shipPlanService: ShipPlanService,
    protected parseLinks: JhiParseLinks,
    protected jhiAlertService: JhiAlertService,
    protected accountService: AccountService,
    protected activatedRoute: ActivatedRoute,
    protected router: Router,
    protected eventManager: JhiEventManager,
    protected regionService: RegionService,
    private fb: FormBuilder
  ) {
    this.itemsPerPage = ITEMS_PER_PAGE;
    this.routeData = this.activatedRoute.data.subscribe(data => {
      this.page = data.pagingParams.page;
      this.previousPage = data.pagingParams.page;
      this.reverse = data.pagingParams.ascending;
      this.predicate = data.pagingParams.predicate;
    });
  }

  loadAll() {
    let filterParams = {
      page: this.page - 1,
      size: this.itemsPerPage,
      sort: this.sort(),
      warningPlan: true,
      'excludeDeliverPosition': '宝龙' // 特殊要求：排查宝龙
    };
    console.log(this.searchForm);
    if (this.searchForm.get(['auditStatus']).value) {
      filterParams['auditStatus.equals'] = this.searchForm.get(['auditStatus']).value;
    }
    if (this.searchForm.get(['truckNumber']).value) {
      filterParams['truckNumber.contains'] = this.searchForm.get(['truckNumber']).value;
    }
    if (this.searchForm.get(['deliverPosition']).value) {
      filterParams['deliverPosition.equals'] = this.searchForm.get(['deliverPosition']).value;
    }
    if (this.searchForm.get(['deliverTimeBegin']).value) {
      filterParams['deliverTimeBegin'] = this.searchForm.get(['deliverTimeBegin']).value;
    }
    if (this.searchForm.get(['deliverTimeEnd']).value) {
      filterParams['deliverTimeEnd'] = this.searchForm.get(['deliverTimeEnd']).value;
    }
    // if (this.searchForm.get(['warningPlan']).value) {
    //   filterParams['warningPlan'] = this.searchForm.get(['warningPlan']).value;
    // }
    this.shipPlanService
      .query(filterParams)
      .subscribe(
        (res: HttpResponse<IShipPlan[]>) => this.paginateShipPlans(res.body, res.headers),
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  formatDate(d) {
    return d.getFullYear() + '-' + this.appendLeadingZeroes(d.getMonth() + 1) + '-' + this.appendLeadingZeroes(d.getDate());
  }

  appendLeadingZeroes(n) {
    if (n <= 9) {
      return '0' + n;
    }
    return n;
  }

  loadPage(page: number) {
    if (page !== this.previousPage) {
      this.previousPage = page;
      this.transition();
    }
  }

  search() {
    this.page = 1;
    this.loadAll();
  }

  transition() {
    this.router.navigate(['/ship-plan'], {
      queryParams: {
        page: this.page,
        size: this.itemsPerPage,
        sort: this.predicate + ',' + (this.reverse ? 'asc' : 'desc')
      }
    });
    this.loadAll();
  }

  clear() {
    this.page = 0;
    this.router.navigate([
      '/ship-plan',
      {
        page: this.page,
        sort: this.predicate + ',' + (this.reverse ? 'asc' : 'desc')
      }
    ]);
    this.loadAll();
  }

  ngOnInit() {
    this.loadAll();
    this.accountService.identity().then(account => {
      this.currentAccount = account;
    });
    this.registerChangeInShipPlans();

    this.regionService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<IRegion[]>) => mayBeOk.ok),
        map((response: HttpResponse<IRegion[]>) => response.body)
      )
      .subscribe((res: IRegion[]) => (this.regions = res), (res: HttpErrorResponse) => this.onError(res.message));
  }

  ngOnDestroy() {
    this.eventManager.destroy(this.eventSubscriber);
  }

  trackId(index: number, item: IShipPlan) {
    return item.id;
  }

  registerChangeInShipPlans() {
    this.eventSubscriber = this.eventManager.subscribe('shipPlanListModification', response => this.loadAll());
  }

  sort() {
    const result = [this.predicate + ',' + (this.reverse ? 'asc' : 'desc')];
    if (this.predicate !== 'id') {
      result.push('id');
    }
    return result;
  }

  protected paginateShipPlans(data: IShipPlan[], headers: HttpHeaders) {
    this.links = this.parseLinks.parse(headers.get('link'));
    this.totalItems = parseInt(headers.get('X-Total-Count'), 10);
    this.shipPlans = data;
  }

  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }

  trackRegionById(index: number, item: IRegion) {
    return item.id;
  }
}
