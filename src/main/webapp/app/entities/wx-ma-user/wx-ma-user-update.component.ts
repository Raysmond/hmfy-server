import { Component, OnInit } from '@angular/core';
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import * as moment from 'moment';
import { DATE_TIME_FORMAT } from 'app/shared/constants/input.constants';
import { JhiAlertService } from 'ng-jhipster';
import { IWxMaUser, WxMaUser } from 'app/shared/model/wx-ma-user.model';
import { WxMaUserService } from './wx-ma-user.service';
import { IUser, UserService } from 'app/core';

@Component({
  selector: 'jhi-wx-ma-user-update',
  templateUrl: './wx-ma-user-update.component.html'
})
export class WxMaUserUpdateComponent implements OnInit {
  wxMaUser: IWxMaUser;
  isSaving: boolean;

  users: IUser[];

  editForm = this.fb.group({
    id: [],
    openId: [null, [Validators.required]],
    nickName: [],
    gender: [],
    language: [],
    city: [],
    province: [],
    country: [],
    avatarUrl: [],
    unionId: [],
    watermark: [],
    createTime: [null, [Validators.required]],
    updateTime: [null, [Validators.required]],
    phone: [],
    userId: [null, Validators.required]
  });

  constructor(
    protected jhiAlertService: JhiAlertService,
    protected wxMaUserService: WxMaUserService,
    protected userService: UserService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ wxMaUser }) => {
      this.updateForm(wxMaUser);
      this.wxMaUser = wxMaUser;
    });
    this.userService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<IUser[]>) => mayBeOk.ok),
        map((response: HttpResponse<IUser[]>) => response.body)
      )
      .subscribe((res: IUser[]) => (this.users = res), (res: HttpErrorResponse) => this.onError(res.message));
  }

  updateForm(wxMaUser: IWxMaUser) {
    this.editForm.patchValue({
      id: wxMaUser.id,
      openId: wxMaUser.openId,
      nickName: wxMaUser.nickName,
      gender: wxMaUser.gender,
      language: wxMaUser.language,
      city: wxMaUser.city,
      province: wxMaUser.province,
      country: wxMaUser.country,
      avatarUrl: wxMaUser.avatarUrl,
      unionId: wxMaUser.unionId,
      watermark: wxMaUser.watermark,
      createTime: wxMaUser.createTime != null ? wxMaUser.createTime.format(DATE_TIME_FORMAT) : null,
      updateTime: wxMaUser.updateTime != null ? wxMaUser.updateTime.format(DATE_TIME_FORMAT) : null,
      phone: wxMaUser.phone,
      userId: wxMaUser.userId
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const wxMaUser = this.createFromForm();
    if (wxMaUser.id !== undefined) {
      this.subscribeToSaveResponse(this.wxMaUserService.update(wxMaUser));
    } else {
      this.subscribeToSaveResponse(this.wxMaUserService.create(wxMaUser));
    }
  }

  private createFromForm(): IWxMaUser {
    const entity = {
      ...new WxMaUser(),
      id: this.editForm.get(['id']).value,
      openId: this.editForm.get(['openId']).value,
      nickName: this.editForm.get(['nickName']).value,
      gender: this.editForm.get(['gender']).value,
      language: this.editForm.get(['language']).value,
      city: this.editForm.get(['city']).value,
      province: this.editForm.get(['province']).value,
      country: this.editForm.get(['country']).value,
      avatarUrl: this.editForm.get(['avatarUrl']).value,
      unionId: this.editForm.get(['unionId']).value,
      watermark: this.editForm.get(['watermark']).value,
      createTime:
        this.editForm.get(['createTime']).value != null ? moment(this.editForm.get(['createTime']).value, DATE_TIME_FORMAT) : undefined,
      updateTime:
        this.editForm.get(['updateTime']).value != null ? moment(this.editForm.get(['updateTime']).value, DATE_TIME_FORMAT) : undefined,
      phone: this.editForm.get(['phone']).value,
      userId: this.editForm.get(['userId']).value
    };
    return entity;
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IWxMaUser>>) {
    result.subscribe((res: HttpResponse<IWxMaUser>) => this.onSaveSuccess(), (res: HttpErrorResponse) => this.onSaveError());
  }

  protected onSaveSuccess() {
    this.isSaving = false;
    this.previousState();
  }

  protected onSaveError() {
    this.isSaving = false;
  }
  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }

  trackUserById(index: number, item: IUser) {
    return item.id;
  }
}
