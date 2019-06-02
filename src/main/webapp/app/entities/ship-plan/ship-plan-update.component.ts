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
  shipPlan: IShipPlan;
  isSaving: boolean;

  users: IUser[];

  editForm = this.fb.group({
    id: [],
    company: [],
    demandedAmount: [null, [Validators.required, Validators.min(0)]],
    finishAmount: [],
    remainAmount: [],
    availableAmount: [],
    shipMethond: [],
    shipNumber: [],
    endTime: [],
    createTime: [],
    updateTime: [],
    licensePlateNumber: [null, [Validators.required]],
    driver: [],
    phone: [],
    userId: [],
    toUserId: []
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
      this.shipPlan = shipPlan;
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
      demandedAmount: shipPlan.demandedAmount,
      finishAmount: shipPlan.finishAmount,
      remainAmount: shipPlan.remainAmount,
      availableAmount: shipPlan.availableAmount,
      shipMethond: shipPlan.shipMethond,
      shipNumber: shipPlan.shipNumber,
      endTime: shipPlan.endTime != null ? shipPlan.endTime.format(DATE_TIME_FORMAT) : null,
      createTime: shipPlan.createTime != null ? shipPlan.createTime.format(DATE_TIME_FORMAT) : null,
      updateTime: shipPlan.updateTime != null ? shipPlan.updateTime.format(DATE_TIME_FORMAT) : null,
      licensePlateNumber: shipPlan.licensePlateNumber,
      driver: shipPlan.driver,
      phone: shipPlan.phone,
      userId: shipPlan.userId,
      toUserId: shipPlan.toUserId
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
      demandedAmount: this.editForm.get(['demandedAmount']).value,
      finishAmount: this.editForm.get(['finishAmount']).value,
      remainAmount: this.editForm.get(['remainAmount']).value,
      availableAmount: this.editForm.get(['availableAmount']).value,
      shipMethond: this.editForm.get(['shipMethond']).value,
      shipNumber: this.editForm.get(['shipNumber']).value,
      endTime: this.editForm.get(['endTime']).value != null ? moment(this.editForm.get(['endTime']).value, DATE_TIME_FORMAT) : undefined,
      createTime:
        this.editForm.get(['createTime']).value != null ? moment(this.editForm.get(['createTime']).value, DATE_TIME_FORMAT) : undefined,
      updateTime:
        this.editForm.get(['updateTime']).value != null ? moment(this.editForm.get(['updateTime']).value, DATE_TIME_FORMAT) : undefined,
      licensePlateNumber: this.editForm.get(['licensePlateNumber']).value,
      driver: this.editForm.get(['driver']).value,
      phone: this.editForm.get(['phone']).value,
      userId: this.editForm.get(['userId']).value,
      toUserId: this.editForm.get(['toUserId']).value
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
