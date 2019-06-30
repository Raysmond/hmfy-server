import { Component, OnInit } from '@angular/core';
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import * as moment from 'moment';
import { DATE_TIME_FORMAT } from 'app/shared/constants/input.constants';
import { IParkMsg, ParkMsg } from 'app/shared/model/park-msg.model';
import { ParkMsgService } from './park-msg.service';

@Component({
  selector: 'jhi-park-msg-update',
  templateUrl: './park-msg-update.component.html'
})
export class ParkMsgUpdateComponent implements OnInit {
  isSaving: boolean;

  editForm = this.fb.group({
    id: [],
    parkid: [null, [Validators.required, Validators.maxLength(64)]],
    service: [null, [Validators.required, Validators.maxLength(64)]],
    createTime: [null, [Validators.required]],
    body: [null, [Validators.required, Validators.maxLength(4096)]],
    type: []
  });

  constructor(protected parkMsgService: ParkMsgService, protected activatedRoute: ActivatedRoute, private fb: FormBuilder) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ parkMsg }) => {
      this.updateForm(parkMsg);
    });
  }

  updateForm(parkMsg: IParkMsg) {
    this.editForm.patchValue({
      id: parkMsg.id,
      parkid: parkMsg.parkid,
      service: parkMsg.service,
      createTime: parkMsg.createTime != null ? parkMsg.createTime.format(DATE_TIME_FORMAT) : null,
      body: parkMsg.body,
      type: parkMsg.type
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const parkMsg = this.createFromForm();
    if (parkMsg.id !== undefined) {
      this.subscribeToSaveResponse(this.parkMsgService.update(parkMsg));
    } else {
      this.subscribeToSaveResponse(this.parkMsgService.create(parkMsg));
    }
  }

  private createFromForm(): IParkMsg {
    const entity = {
      ...new ParkMsg(),
      id: this.editForm.get(['id']).value,
      parkid: this.editForm.get(['parkid']).value,
      service: this.editForm.get(['service']).value,
      createTime:
        this.editForm.get(['createTime']).value != null ? moment(this.editForm.get(['createTime']).value, DATE_TIME_FORMAT) : undefined,
      body: this.editForm.get(['body']).value,
      type: this.editForm.get(['type']).value
    };
    return entity;
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IParkMsg>>) {
    result.subscribe((res: HttpResponse<IParkMsg>) => this.onSaveSuccess(), (res: HttpErrorResponse) => this.onSaveError());
  }

  protected onSaveSuccess() {
    this.isSaving = false;
    this.previousState();
  }

  protected onSaveError() {
    this.isSaving = false;
  }
}
