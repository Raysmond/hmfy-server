import { Component, OnInit } from '@angular/core';
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import * as moment from 'moment';
import { DATE_TIME_FORMAT } from 'app/shared/constants/input.constants';
import { JhiAlertService } from 'ng-jhipster';
import { IShipPlan, ShipPlan } from 'app/shared/model/ship-plan.model';
import { ShipPlanService } from './ship-plan.service';
import { IUser, UserService } from 'app/core';

@Component({
  selector: 'jhi-ship-plan-update',
  templateUrl: './ship-plan-update.component.html'
})
export class ShipPlanUpdateComponent implements OnInit {
  isSaving: boolean;

  users: IUser[];

  editForm = this.fb.group({
    id: [],
    company: [],
    applyId: [null, [Validators.required]],
    applyNumber: [],
    truckNumber: [null, [Validators.required]],
    auditStatus: [null, [Validators.required]],
    productName: [null, [Validators.required]],
    deliverPosition: [null, [Validators.required]],
    gateTime: [],
    leaveTime: [],
    deliverTime: [],
    allowInTime: [],
    createTime: [null, [Validators.required]],
    updateTime: [null, [Validators.required]],
    userId: []
  });

  constructor(
    protected jhiAlertService: JhiAlertService,
    protected shipPlanService: ShipPlanService,
    protected userService: UserService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ shipPlan }) => {
      this.updateForm(shipPlan);
    });
    this.userService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<IUser[]>) => mayBeOk.ok),
        map((response: HttpResponse<IUser[]>) => response.body)
      )
      .subscribe((res: IUser[]) => (this.users = res), (res: HttpErrorResponse) => this.onError(res.message));
  }

  updateForm(shipPlan: IShipPlan) {
    this.editForm.patchValue({
      id: shipPlan.id,
      company: shipPlan.company,
      applyId: shipPlan.applyId,
      applyNumber: shipPlan.applyNumber,
      truckNumber: shipPlan.truckNumber,
      auditStatus: shipPlan.auditStatus,
      productName: shipPlan.productName,
      deliverPosition: shipPlan.deliverPosition,
      gateTime: shipPlan.gateTime != null ? shipPlan.gateTime.format(DATE_TIME_FORMAT) : null,
      leaveTime: shipPlan.leaveTime != null ? shipPlan.leaveTime.format(DATE_TIME_FORMAT) : null,
      deliverTime: shipPlan.deliverTime != null ? shipPlan.deliverTime.format(DATE_TIME_FORMAT) : null,
      allowInTime: shipPlan.allowInTime != null ? shipPlan.allowInTime.format(DATE_TIME_FORMAT) : null,
      createTime: shipPlan.createTime != null ? shipPlan.createTime.format(DATE_TIME_FORMAT) : null,
      updateTime: shipPlan.updateTime != null ? shipPlan.updateTime.format(DATE_TIME_FORMAT) : null,
      userId: shipPlan.userId
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const shipPlan = this.createFromForm();
    if (shipPlan.id !== undefined) {
      this.subscribeToSaveResponse(this.shipPlanService.update(shipPlan));
    } else {
      this.subscribeToSaveResponse(this.shipPlanService.create(shipPlan));
    }
  }

  private createFromForm(): IShipPlan {
    const entity = {
      ...new ShipPlan(),
      id: this.editForm.get(['id']).value,
      company: this.editForm.get(['company']).value,
      applyId: this.editForm.get(['applyId']).value,
      applyNumber: this.editForm.get(['applyNumber']).value,
      truckNumber: this.editForm.get(['truckNumber']).value,
      auditStatus: this.editForm.get(['auditStatus']).value,
      productName: this.editForm.get(['productName']).value,
      deliverPosition: this.editForm.get(['deliverPosition']).value,
      gateTime: this.editForm.get(['gateTime']).value != null ? moment(this.editForm.get(['gateTime']).value, DATE_TIME_FORMAT) : undefined,
      leaveTime:
        this.editForm.get(['leaveTime']).value != null ? moment(this.editForm.get(['leaveTime']).value, DATE_TIME_FORMAT) : undefined,
      deliverTime:
        this.editForm.get(['deliverTime']).value != null ? moment(this.editForm.get(['deliverTime']).value, DATE_TIME_FORMAT) : undefined,
      allowInTime:
        this.editForm.get(['allowInTime']).value != null ? moment(this.editForm.get(['allowInTime']).value, DATE_TIME_FORMAT) : undefined,
      createTime:
        this.editForm.get(['createTime']).value != null ? moment(this.editForm.get(['createTime']).value, DATE_TIME_FORMAT) : undefined,
      updateTime:
        this.editForm.get(['updateTime']).value != null ? moment(this.editForm.get(['updateTime']).value, DATE_TIME_FORMAT) : undefined,
      userId: this.editForm.get(['userId']).value
    };
    return entity;
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IShipPlan>>) {
    result.subscribe((res: HttpResponse<IShipPlan>) => this.onSaveSuccess(), (res: HttpErrorResponse) => this.onSaveError());
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
