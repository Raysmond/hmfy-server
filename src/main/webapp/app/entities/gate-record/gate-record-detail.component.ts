import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { JhiDataUtils } from 'ng-jhipster';

import { IGateRecord } from 'app/shared/model/gate-record.model';

@Component({
  selector: 'jhi-gate-record-detail',
  templateUrl: './gate-record-detail.component.html'
})
export class GateRecordDetailComponent implements OnInit {
  gateRecord: IGateRecord;

  constructor(protected dataUtils: JhiDataUtils, protected activatedRoute: ActivatedRoute) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ gateRecord }) => {
      this.gateRecord = gateRecord;
    });
  }

  byteSize(field) {
    return this.dataUtils.byteSize(field);
  }

  openFile(contentType, field) {
    return this.dataUtils.openFile(contentType, field);
  }
  previousState() {
    window.history.back();
  }
}
