import { Component, OnInit } from '@angular/core';
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import * as moment from 'moment';
import { DATE_TIME_FORMAT } from 'app/shared/constants/input.constants';
import { IRegion, Region } from 'app/shared/model/region.model';
import { RegionService } from './region.service';

@Component({
  selector: 'jhi-region-update',
  templateUrl: './region-update.component.html'
})
export class RegionUpdateComponent implements OnInit {
  region: IRegion;
  isSaving: boolean;

  editForm = this.fb.group({
    id: [],
    name: [null, [Validators.required]],
    quota: [null, [Validators.required, Validators.min(0)]],
    vipQuota: [null, [Validators.required, Validators.min(0)]],
    startTime: [],
    endTime: [],
    days: [],
    open: [],
    validTime: [null, [Validators.required, Validators.min(0)]],
    queueQuota: [null, [Validators.required, Validators.min(0)]],
    queueValidTime: [null, [Validators.required, Validators.min(0)]],
    createTime: [],
    updateTime: []
  });

  constructor(protected regionService: RegionService, protected activatedRoute: ActivatedRoute, private fb: FormBuilder) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ region }) => {
      this.updateForm(region);
      this.region = region;
    });
  }

  updateForm(region: IRegion) {
    this.editForm.patchValue({
      id: region.id,
      name: region.name,
      quota: region.quota,
      vipQuota: region.vipQuota,
      startTime: region.startTime,
      endTime: region.endTime,
      days: region.days,
      open: region.open,
      validTime: region.validTime,
      queueQuota: region.queueQuota,
      queueValidTime: region.queueValidTime,
      createTime: region.createTime != null ? region.createTime.format(DATE_TIME_FORMAT) : null,
      updateTime: region.updateTime != null ? region.updateTime.format(DATE_TIME_FORMAT) : null
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const region = this.createFromForm();
    if (region.id !== undefined) {
      this.subscribeToSaveResponse(this.regionService.update(region));
    } else {
      this.subscribeToSaveResponse(this.regionService.create(region));
    }
  }

  private createFromForm(): IRegion {
    const entity = {
      ...new Region(),
      id: this.editForm.get(['id']).value,
      name: this.editForm.get(['name']).value,
      quota: this.editForm.get(['quota']).value,
      vipQuota: this.editForm.get(['vipQuota']).value,
      startTime: this.editForm.get(['startTime']).value,
      endTime: this.editForm.get(['endTime']).value,
      days: this.editForm.get(['days']).value,
      open: this.editForm.get(['open']).value,
      validTime: this.editForm.get(['validTime']).value,
      queueQuota: this.editForm.get(['queueQuota']).value,
      queueValidTime: this.editForm.get(['queueValidTime']).value,
      createTime:
        this.editForm.get(['createTime']).value != null ? moment(this.editForm.get(['createTime']).value, DATE_TIME_FORMAT) : undefined,
      updateTime:
        this.editForm.get(['updateTime']).value != null ? moment(this.editForm.get(['updateTime']).value, DATE_TIME_FORMAT) : undefined
    };
    return entity;
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IRegion>>) {
    result.subscribe((res: HttpResponse<IRegion>) => this.onSaveSuccess(), (res: HttpErrorResponse) => this.onSaveError());
  }

  protected onSaveSuccess() {
    this.isSaving = false;
    this.previousState();
  }

  protected onSaveError() {
    this.isSaving = false;
  }
}
