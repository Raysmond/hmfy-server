import { Component, OnInit } from '@angular/core';
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import * as moment from 'moment';
import { DATE_TIME_FORMAT } from 'app/shared/constants/input.constants';
import { IPlan, Plan } from 'app/shared/model/plan.model';
import { PlanService } from './plan.service';

@Component({
  selector: 'jhi-plan-update',
  templateUrl: './plan-update.component.html'
})
export class PlanUpdateComponent implements OnInit {
  isSaving: boolean;
  workDayDp: any;

  editForm = this.fb.group({
    id: [],
    planNumber: [null, [Validators.required]],
    location: [],
    workDay: [null, [Validators.required]],
    stockName: [],
    loadingStartTime: [],
    loadingEndTime: [],
    weightSum: [null, [Validators.required]],
    operator: [],
    operation: [],
    opPosition: [],
    channel: [],
    comment: [],
    createTime: [],
    updateTime: []
  });

  constructor(protected planService: PlanService, protected activatedRoute: ActivatedRoute, private fb: FormBuilder) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ plan }) => {
      this.updateForm(plan);
    });
  }

  updateForm(plan: IPlan) {
    this.editForm.patchValue({
      id: plan.id,
      planNumber: plan.planNumber,
      location: plan.location,
      workDay: plan.workDay,
      stockName: plan.stockName,
      loadingStartTime: plan.loadingStartTime != null ? plan.loadingStartTime.format(DATE_TIME_FORMAT) : null,
      loadingEndTime: plan.loadingEndTime != null ? plan.loadingEndTime.format(DATE_TIME_FORMAT) : null,
      weightSum: plan.weightSum,
      operator: plan.operator,
      operation: plan.operation,
      opPosition: plan.opPosition,
      channel: plan.channel,
      comment: plan.comment,
      createTime: plan.createTime != null ? plan.createTime.format(DATE_TIME_FORMAT) : null,
      updateTime: plan.updateTime != null ? plan.updateTime.format(DATE_TIME_FORMAT) : null
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const plan = this.createFromForm();
    if (plan.id !== undefined) {
      this.subscribeToSaveResponse(this.planService.update(plan));
    } else {
      this.subscribeToSaveResponse(this.planService.create(plan));
    }
  }

  private createFromForm(): IPlan {
    const entity = {
      ...new Plan(),
      id: this.editForm.get(['id']).value,
      planNumber: this.editForm.get(['planNumber']).value,
      location: this.editForm.get(['location']).value,
      workDay: this.editForm.get(['workDay']).value,
      stockName: this.editForm.get(['stockName']).value,
      loadingStartTime:
        this.editForm.get(['loadingStartTime']).value != null
          ? moment(this.editForm.get(['loadingStartTime']).value, DATE_TIME_FORMAT)
          : undefined,
      loadingEndTime:
        this.editForm.get(['loadingEndTime']).value != null
          ? moment(this.editForm.get(['loadingEndTime']).value, DATE_TIME_FORMAT)
          : undefined,
      weightSum: this.editForm.get(['weightSum']).value,
      operator: this.editForm.get(['operator']).value,
      operation: this.editForm.get(['operation']).value,
      opPosition: this.editForm.get(['opPosition']).value,
      channel: this.editForm.get(['channel']).value,
      comment: this.editForm.get(['comment']).value,
      createTime:
        this.editForm.get(['createTime']).value != null ? moment(this.editForm.get(['createTime']).value, DATE_TIME_FORMAT) : undefined,
      updateTime:
        this.editForm.get(['updateTime']).value != null ? moment(this.editForm.get(['updateTime']).value, DATE_TIME_FORMAT) : undefined
    };
    return entity;
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IPlan>>) {
    result.subscribe((res: HttpResponse<IPlan>) => this.onSaveSuccess(), (res: HttpErrorResponse) => this.onSaveError());
  }

  protected onSaveSuccess() {
    this.isSaving = false;
    this.previousState();
  }

  protected onSaveError() {
    this.isSaving = false;
  }
}
