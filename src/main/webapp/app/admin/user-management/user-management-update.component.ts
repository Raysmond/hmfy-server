import { Component, OnInit } from '@angular/core';
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { JhiLanguageHelper, User, UserService, AccountService } from 'app/core';

import { IRegion } from 'app/shared/model/region.model';
import { RegionService } from 'app/entities/region';
import { JhiAlertService } from 'ng-jhipster';
import { filter, map } from 'rxjs/operators';

@Component({
  selector: 'jhi-user-mgmt-update',
  templateUrl: './user-management-update.component.html'
})
export class UserMgmtUpdateComponent implements OnInit {
  user: User;
  languages: any[];
  authorities: any[];
  isSaving: boolean;
  account: any;

  regions: IRegion[];

  editForm = this.fb.group({
    id: [null],
    login: [
      '',
      [
        Validators.required,
        Validators.minLength(1),
        Validators.maxLength(50)
        // Validators.pattern('^[_.@A-Za-z0-9-]*')
      ]
    ],
    firstName: ['', [Validators.required, Validators.maxLength(20)]],
    lastName: ['', [Validators.maxLength(50)]],
    // email: ['', [Validators.minLength(5), Validators.maxLength(254), Validators.email]],
    email: [null],
    activated: [true],
    langKey: [],
    authorities: [],
    regionId: [null],
    rawPassword: [
      '',
      [
        // Validators.required,
        Validators.minLength(6),
        Validators.maxLength(20)
        // Validators.pattern('^[_.@A-Za-z0-9-]*')
      ]
    ],
    truckNumber: [null],
    company: ['', [Validators.required, Validators.maxLength(50)]],
    carCompany: [null],
    phone: ['', [Validators.required, Validators.minLength(11), Validators.maxLength(11)]],
    memo: ['', [Validators.maxLength(50)]],
    carCapacity: [null]
  });

  editFormDriver = this.fb.group({
    id: [null],
    login: [
      '',
      [
        Validators.required,
        Validators.minLength(1),
        Validators.maxLength(50)
        // Validators.pattern('^[_.@A-Za-z0-9-]*')
      ]
    ],
    firstName: ['', [Validators.required, Validators.maxLength(20)]],
    lastName: ['', [Validators.maxLength(50)]],
    // email: ['', [Validators.minLength(5), Validators.maxLength(254), Validators.email]],
    email: [null],
    activated: [true],
    langKey: [],
    authorities: [],
    regionId: [null],
    rawPassword: [
      '',
      [
        // Validators.required,
        Validators.minLength(6),
        Validators.maxLength(20)
        // Validators.pattern('^[_.@A-Za-z0-9-]*')
      ]
    ],
    truckNumber: ['', [Validators.required, Validators.maxLength(10)]],
    company: ['', [Validators.required, Validators.maxLength(50)]],
    carCompany: ['', [Validators.required, Validators.maxLength(50)]],
    phone: ['', [Validators.required, Validators.minLength(11), Validators.maxLength(11)]],
    memo: ['', [Validators.maxLength(50)]],
    carCapacity: [
      '',
      [
        Validators.required,
        Validators.minLength(1),
        Validators.maxLength(50)
        // Validators.pattern('^[_.@A-Za-z0-9-]*')
      ]
    ]
  });

  constructor(
    private languageHelper: JhiLanguageHelper,
    private userService: UserService,
    protected regionService: RegionService,
    protected jhiAlertService: JhiAlertService,
    private route: ActivatedRoute,
    private accountService: AccountService,
    private router: Router,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.isSaving = false;
    this.route.data.subscribe(({ user }) => {
      this.user = user.body ? user.body : user;
      this.updateForm(this.user);
    });
    this.authorities = [];
    this.userService.authorities().subscribe(authorities => {
      this.authorities = authorities;
    });
    this.languageHelper.getAll().then(languages => {
      this.languages = languages;
    });

    this.regionService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<IRegion[]>) => mayBeOk.ok),
        map((response: HttpResponse<IRegion[]>) => response.body)
      )
      .subscribe((res: IRegion[]) => (this.regions = res), (res: HttpErrorResponse) => this.onError(res.message));

    this.accountService.identity().then(res => {
      this.account = res;

      if (this.account.authorities.indexOf('ROLE_REGION_ADMIN') >= 0) {
        this.user.authorities = ['ROLE_APPOINTMENT'];
        this.editForm.patchValue({
          authorities: this.user.authorities
        });
        this.editFormDriver.patchValue({
          authorities: this.user.authorities
        });
      }
    });
  }

  private updateForm(user: User): void {
    this.editForm.patchValue({
      id: user.id,
      login: user.login,
      firstName: user.firstName,
      lastName: user.lastName,
      email: user.email,
      activated: user.activated,
      langKey: user.langKey,
      authorities: user.authorities,
      regionId: user.regionId,
      truckNumber: user.truckNumber,
      company: user.company,
      memo: user.memo,
      phone: user.phone,
      carCapacity: user.carCapacity,
      carCompany: user.carCompany
    });

    this.editFormDriver.patchValue({
      id: user.id,
      login: user.login,
      firstName: user.firstName,
      lastName: user.lastName,
      email: user.email,
      activated: user.activated,
      langKey: user.langKey,
      authorities: user.authorities,
      regionId: user.regionId,
      truckNumber: user.truckNumber,
      company: user.company,
      memo: user.memo,
      phone: user.phone,
      carCapacity: user.carCapacity,
      carCompany: user.carCompany
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    this.updateUser(this.user);
    if (this.user.id !== null) {
      this.userService.update(this.user).subscribe(response => this.onSaveSuccess(response), () => this.onSaveError());
    } else {
      this.userService.create(this.user).subscribe(response => this.onSaveSuccess(response), () => this.onSaveError());
    }
  }

  isDriver() {
    let authorities = this.editForm.get(['authorities']).value;
    if (!authorities) {
      return false;
    }
    return authorities.indexOf('ROLE_APPOINTMENT') >= 0;
  }

  isRegionAdmin() {
    let authorities = this.editForm.get(['authorities']).value;
    if (!authorities) {
      return false;
    }
    return authorities.indexOf('ROLE_REGION_ADMIN') >= 0;
  }

  isCurrentUserAdmin() {
    if (!this.account || !this.account.authorities) {
      return false;
    }
    let authorities = this.account.authorities;
    if (!authorities) {
      return false;
    }
    return authorities.indexOf('ROLE_ADMIN') >= 0;
  }

  private updateUser(user: User): void {
    if (this.isCurrentUserAdmin()) {
      user.login = this.editForm.get(['login']).value;
      user.firstName = this.editForm.get(['firstName']).value;
      user.lastName = this.editForm.get(['lastName']).value;
      user.email = this.editForm.get(['email']).value;
      user.activated = this.editForm.get(['activated']).value;
      user.langKey = this.editForm.get(['langKey']).value;
      user.authorities = this.editForm.get(['authorities']).value;
      user.regionId = this.editForm.get(['regionId']).value;
      user.truckNumber = this.editForm.get(['truckNumber']).value;
      user.rawPassword = this.editForm.get(['rawPassword']).value;
      user.company = this.editForm.get(['company']).value;
      user.carCompany = this.editForm.get(['carCompany']).value;
      user.carCapacity = this.editForm.get(['carCapacity']).value;
      user.memo = this.editForm.get(['memo']).value;
      user.phone = this.editForm.get(['phone']).value;
    } else {
      user.login = this.editFormDriver.get(['login']).value;
      user.firstName = this.editFormDriver.get(['firstName']).value;
      user.lastName = this.editFormDriver.get(['lastName']).value;
      user.email = this.editFormDriver.get(['email']).value;
      user.activated = this.editFormDriver.get(['activated']).value;
      user.langKey = this.editFormDriver.get(['langKey']).value;
      user.authorities = this.editFormDriver.get(['authorities']).value;
      user.regionId = this.editFormDriver.get(['regionId']).value;
      user.truckNumber = this.editFormDriver.get(['truckNumber']).value;
      user.rawPassword = this.editFormDriver.get(['rawPassword']).value;
      user.company = this.editFormDriver.get(['company']).value;
      user.carCompany = this.editFormDriver.get(['carCompany']).value;
      user.carCapacity = this.editFormDriver.get(['carCapacity']).value;
      user.memo = this.editFormDriver.get(['memo']).value;
      user.phone = this.editFormDriver.get(['phone']).value;
    }
  }

  private onSaveSuccess(result) {
    this.isSaving = false;
    this.previousState();
  }

  private onSaveError() {
    this.isSaving = false;
  }

  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }

  trackRegionById(index: number, item: IRegion) {
    return item.id;
  }
}
