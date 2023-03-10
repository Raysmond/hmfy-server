import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { FormBuilder, Validators } from '@angular/forms';

import { ActivatedRoute, Router } from '@angular/router';
import { JhiEventManager, JhiParseLinks, JhiAlertService } from 'ng-jhipster';

import { ITEMS_PER_PAGE } from 'app/shared';
import { AccountService, UserService, User } from 'app/core';
import { UserMgmtDeleteDialogComponent } from './user-management-delete-dialog.component';
import { IRegion } from 'app/shared/model/region.model';

@Component({
  selector: 'jhi-user-mgmt',
  templateUrl: './user-management.component.html'
})
export class UserMgmtComponent implements OnInit, OnDestroy {
  currentAccount: any;
  users: User[];
  error: any;
  success: any;
  routeData: any;
  links: any;
  totalItems: any;
  itemsPerPage: any;
  page: any;
  predicate: any;
  previousPage: any;
  reverse: any;

  searchForm = this.fb.group({
    query: null
  });

  constructor(
    private userService: UserService,
    private alertService: JhiAlertService,
    private accountService: AccountService,
    private parseLinks: JhiParseLinks,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private eventManager: JhiEventManager,
    private modalService: NgbModal,
    private fb: FormBuilder
  ) {
    this.itemsPerPage = ITEMS_PER_PAGE;
    this.routeData = this.activatedRoute.data.subscribe(data => {
      this.page = data['pagingParams'].page;
      this.previousPage = data['pagingParams'].page;
      this.reverse = data['pagingParams'].decending;
      this.predicate = data['pagingParams'].predicate;
    });
  }

  canRegionAdminEdit(user) {
    if (this.accountService.hasAnyAuthority(['ROLE_ADMIN'])) {
      return false;
    }
    if (this.accountService.hasAnyAuthority(['ROLE_REGION_ADMIN'])) {
      if (user.authorities.includes('ROLE_ADMIN') || user.authorities.includes('ROLE_REGION_ADMIN')) {
        return false;
      }
    }
    return true;
  }

  isRegionAdmin() {
    if (this.accountService.hasAnyAuthority(['ROLE_ADMIN'])) {
      return false;
    }
    if (this.accountService.hasAnyAuthority(['ROLE_REGION_ADMIN'])) {
      return true;
    }
    return true;
  }

  ngOnInit() {
    this.accountService.identity().then(account => {
      this.currentAccount = account;
      this.loadAll();
      this.registerChangeInUsers();
    });
  }

  ngOnDestroy() {
    this.routeData.unsubscribe();
  }

  registerChangeInUsers() {
    this.eventManager.subscribe('userListModification', response => this.loadAll());
  }

  setActive(user, isActivated) {
    user.activated = isActivated;

    this.userService.update(user).subscribe(response => {
      if (response.status === 200) {
        this.error = null;
        this.success = 'OK';
        this.loadAll();
      } else {
        this.success = null;
        this.error = 'ERROR';
      }
    });
  }

  loadAll() {
    let filterParams = {
      page: this.page - 1,
      size: this.itemsPerPage,
      sort: this.sort()
    };
    if (this.searchForm.get(['query']).value) {
      filterParams['query'] = this.searchForm.get(['query']).value;
    }
    this.userService
      .query(filterParams)
      .subscribe((res: HttpResponse<User[]>) => this.onSuccess(res.body, res.headers), (res: HttpResponse<any>) => this.onError(res.body));
  }

  search() {
    this.loadAll();
  }

  trackIdentity(index, item: User) {
    return item.id;
  }

  sort() {
    const result = [this.predicate + ',' + (this.reverse ? 'asc' : 'desc')];
    if (this.predicate !== 'id') {
      result.push('id');
    }
    return result;
  }

  loadPage(page: number) {
    if (page !== this.previousPage) {
      this.previousPage = page;
      this.transition();
    }
  }

  transition() {
    this.router.navigate(['/admin/user-management'], {
      queryParams: {
        page: this.page,
        sort: this.predicate + ',' + (this.reverse ? 'asc' : 'desc')
      }
    });
    this.loadAll();
  }

  deleteUser(user: User) {
    const modalRef = this.modalService.open(UserMgmtDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.user = user;
    modalRef.result.then(
      result => {
        // Left blank intentionally, nothing to do here
      },
      reason => {
        // Left blank intentionally, nothing to do here
      }
    );
  }

  private onSuccess(data, headers) {
    this.links = this.parseLinks.parse(headers.get('link'));
    this.totalItems = headers.get('X-Total-Count');
    this.users = data;
  }

  private onError(error) {
    this.alertService.error(error.error, error.message, null);
  }

  trackRegionById(index: number, item: IRegion) {
    return item.id;
  }
}
