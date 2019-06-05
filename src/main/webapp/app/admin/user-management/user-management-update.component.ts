import { Component, OnInit } from '@angular/core';
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { JhiLanguageHelper, User, UserService } from 'app/core';

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
    firstName: ['', [Validators.maxLength(50)]],
    lastName: ['', [Validators.maxLength(50)]],
    email: ['', [Validators.minLength(5), Validators.maxLength(254), Validators.email]],
    activated: [true],
    langKey: [],
    authorities: [],
    regionId: [null]
  });

  constructor(
    private languageHelper: JhiLanguageHelper,
    private userService: UserService,
    protected regionService: RegionService,
    protected jhiAlertService: JhiAlertService,
    private route: ActivatedRoute,
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
      regionId: user.regionId
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

  private updateUser(user: User): void {
    user.login = this.editForm.get(['login']).value;
    user.firstName = this.editForm.get(['firstName']).value;
    user.lastName = this.editForm.get(['lastName']).value;
    user.email = this.editForm.get(['email']).value;
    user.activated = this.editForm.get(['activated']).value;
    user.langKey = this.editForm.get(['langKey']).value;
    user.authorities = this.editForm.get(['authorities']).value;
    user.regionId = this.editForm.get(['regionId']).value;
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
