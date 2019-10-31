import { Component, OnInit } from '@angular/core';
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import * as moment from 'moment';
import { DATE_TIME_FORMAT } from 'app/shared/constants/input.constants';
import { JhiAlertService, JhiDataUtils } from 'ng-jhipster';
import { IGateRecord, GateRecord } from 'app/shared/model/gate-record.model';
import { GateRecordService } from './gate-record.service';

@Component({
  selector: 'jhi-gate-record-update',
  templateUrl: './gate-record-update.component.html'
})
export class GateRecordUpdateComponent implements OnInit {
  isSaving: boolean;

  editForm = this.fb.group({
    id: [],
    recordType: [null, [Validators.required]],
    truckNumber: [null, [Validators.required]],
    recordTime: [null, [Validators.required]],
    data: [],
    rid: [null, [Validators.required]],
    createTime: [null, [Validators.required]],
    regionId: [null, [Validators.required]],
    dataMd5: [],
    modifyTime: []
  });

  constructor(
    protected dataUtils: JhiDataUtils,
    protected jhiAlertService: JhiAlertService,
    protected gateRecordService: GateRecordService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ gateRecord }) => {
      this.updateForm(gateRecord);
    });
  }

  updateForm(gateRecord: IGateRecord) {
    this.editForm.patchValue({
      id: gateRecord.id,
      recordType: gateRecord.recordType,
      truckNumber: gateRecord.truckNumber,
      recordTime: gateRecord.recordTime != null ? gateRecord.recordTime.format(DATE_TIME_FORMAT) : null,
      data: gateRecord.data,
      rid: gateRecord.rid,
      createTime: gateRecord.createTime != null ? gateRecord.createTime.format(DATE_TIME_FORMAT) : null,
      regionId: gateRecord.regionId,
      dataMd5: gateRecord.dataMd5,
      modifyTime: gateRecord.modifyTime != null ? gateRecord.modifyTime.format(DATE_TIME_FORMAT) : null
    });
  }

  byteSize(field) {
    return this.dataUtils.byteSize(field);
  }

  openFile(contentType, field) {
    return this.dataUtils.openFile(contentType, field);
  }

  setFileData(event, field: string, isImage) {
    return new Promise((resolve, reject) => {
      if (event && event.target && event.target.files && event.target.files[0]) {
        const file = event.target.files[0];
        if (isImage && !/^image\//.test(file.type)) {
          reject(`File was expected to be an image but was found to be ${file.type}`);
        } else {
          const filedContentType: string = field + 'ContentType';
          this.dataUtils.toBase64(file, base64Data => {
            this.editForm.patchValue({
              [field]: base64Data,
              [filedContentType]: file.type
            });
          });
        }
      } else {
        reject(`Base64 data was not set as file could not be extracted from passed parameter: ${event}`);
      }
    }).then(
      () => console.log('blob added'), // sucess
      this.onError
    );
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const gateRecord = this.createFromForm();
    if (gateRecord.id !== undefined) {
      this.subscribeToSaveResponse(this.gateRecordService.update(gateRecord));
    } else {
      this.subscribeToSaveResponse(this.gateRecordService.create(gateRecord));
    }
  }

  private createFromForm(): IGateRecord {
    const entity = {
      ...new GateRecord(),
      id: this.editForm.get(['id']).value,
      recordType: this.editForm.get(['recordType']).value,
      truckNumber: this.editForm.get(['truckNumber']).value,
      recordTime:
        this.editForm.get(['recordTime']).value != null ? moment(this.editForm.get(['recordTime']).value, DATE_TIME_FORMAT) : undefined,
      data: this.editForm.get(['data']).value,
      rid: this.editForm.get(['rid']).value,
      createTime:
        this.editForm.get(['createTime']).value != null ? moment(this.editForm.get(['createTime']).value, DATE_TIME_FORMAT) : undefined,
      regionId: this.editForm.get(['regionId']).value,
      dataMd5: this.editForm.get(['dataMd5']).value,
      modifyTime:
        this.editForm.get(['modifyTime']).value != null ? moment(this.editForm.get(['modifyTime']).value, DATE_TIME_FORMAT) : undefined
    };
    return entity;
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IGateRecord>>) {
    result.subscribe((res: HttpResponse<IGateRecord>) => this.onSaveSuccess(), (res: HttpErrorResponse) => this.onSaveError());
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
}
