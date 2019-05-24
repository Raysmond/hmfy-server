import { Component, OnInit } from '@angular/core';
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import * as moment from 'moment';
import { DATE_TIME_FORMAT } from 'app/shared/constants/input.constants';
import { JhiAlertService } from 'ng-jhipster';
import { IAppointment, Appointment } from 'app/shared/model/appointment.model';
import { AppointmentService } from './appointment.service';
import { IRegion } from 'app/shared/model/region.model';
import { RegionService } from 'app/entities/region';
import { IUser, UserService } from 'app/core';

@Component({
  selector: 'jhi-appointment-update',
  templateUrl: './appointment-update.component.html'
})
export class AppointmentUpdateComponent implements OnInit {
  appointment: IAppointment;
  isSaving: boolean;

  regions: IRegion[];

  users: IUser[];

  editForm = this.fb.group({
    id: [],
    licensePlateNumber: [null, [Validators.required]],
    driver: [null, [Validators.required]],
    number: [null, [Validators.required]],
    valid: [null, [Validators.required]],
    status: [null, [Validators.required]],
    queueNumber: [],
    vip: [null, [Validators.required]],
    createTime: [],
    updateTime: [],
    startTime: [],
    enterTime: [],
    leaveTime: [],
    regionId: [null, Validators.required],
    userId: [null, Validators.required]
  });

  constructor(
    protected jhiAlertService: JhiAlertService,
    protected appointmentService: AppointmentService,
    protected regionService: RegionService,
    protected userService: UserService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ appointment }) => {
      this.updateForm(appointment);
      this.appointment = appointment;
    });
    this.regionService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<IRegion[]>) => mayBeOk.ok),
        map((response: HttpResponse<IRegion[]>) => response.body)
      )
      .subscribe((res: IRegion[]) => (this.regions = res), (res: HttpErrorResponse) => this.onError(res.message));
    this.userService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<IUser[]>) => mayBeOk.ok),
        map((response: HttpResponse<IUser[]>) => response.body)
      )
      .subscribe((res: IUser[]) => (this.users = res), (res: HttpErrorResponse) => this.onError(res.message));
  }

  updateForm(appointment: IAppointment) {
    this.editForm.patchValue({
      id: appointment.id,
      licensePlateNumber: appointment.licensePlateNumber,
      driver: appointment.driver,
      number: appointment.number,
      valid: appointment.valid,
      status: appointment.status,
      queueNumber: appointment.queueNumber,
      vip: appointment.vip,
      createTime: appointment.createTime != null ? appointment.createTime.format(DATE_TIME_FORMAT) : null,
      updateTime: appointment.updateTime != null ? appointment.updateTime.format(DATE_TIME_FORMAT) : null,
      startTime: appointment.startTime != null ? appointment.startTime.format(DATE_TIME_FORMAT) : null,
      enterTime: appointment.enterTime != null ? appointment.enterTime.format(DATE_TIME_FORMAT) : null,
      leaveTime: appointment.leaveTime != null ? appointment.leaveTime.format(DATE_TIME_FORMAT) : null,
      regionId: appointment.regionId,
      userId: appointment.userId
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const appointment = this.createFromForm();
    if (appointment.id !== undefined) {
      this.subscribeToSaveResponse(this.appointmentService.update(appointment));
    } else {
      this.subscribeToSaveResponse(this.appointmentService.create(appointment));
    }
  }

  private createFromForm(): IAppointment {
    const entity = {
      ...new Appointment(),
      id: this.editForm.get(['id']).value,
      licensePlateNumber: this.editForm.get(['licensePlateNumber']).value,
      driver: this.editForm.get(['driver']).value,
      number: this.editForm.get(['number']).value,
      valid: this.editForm.get(['valid']).value,
      status: this.editForm.get(['status']).value,
      queueNumber: this.editForm.get(['queueNumber']).value,
      vip: this.editForm.get(['vip']).value,
      createTime:
        this.editForm.get(['createTime']).value != null ? moment(this.editForm.get(['createTime']).value, DATE_TIME_FORMAT) : undefined,
      updateTime:
        this.editForm.get(['updateTime']).value != null ? moment(this.editForm.get(['updateTime']).value, DATE_TIME_FORMAT) : undefined,
      startTime:
        this.editForm.get(['startTime']).value != null ? moment(this.editForm.get(['startTime']).value, DATE_TIME_FORMAT) : undefined,
      enterTime:
        this.editForm.get(['enterTime']).value != null ? moment(this.editForm.get(['enterTime']).value, DATE_TIME_FORMAT) : undefined,
      leaveTime:
        this.editForm.get(['leaveTime']).value != null ? moment(this.editForm.get(['leaveTime']).value, DATE_TIME_FORMAT) : undefined,
      regionId: this.editForm.get(['regionId']).value,
      userId: this.editForm.get(['userId']).value
    };
    return entity;
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IAppointment>>) {
    result.subscribe((res: HttpResponse<IAppointment>) => this.onSaveSuccess(), (res: HttpErrorResponse) => this.onSaveError());
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

  trackRegionById(index: number, item: IRegion) {
    return item.id;
  }

  trackUserById(index: number, item: IUser) {
    return item.id;
  }
}
