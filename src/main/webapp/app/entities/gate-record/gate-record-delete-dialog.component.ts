import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IGateRecord } from 'app/shared/model/gate-record.model';
import { GateRecordService } from './gate-record.service';

@Component({
  selector: 'jhi-gate-record-delete-dialog',
  templateUrl: './gate-record-delete-dialog.component.html'
})
export class GateRecordDeleteDialogComponent {
  gateRecord: IGateRecord;

  constructor(
    protected gateRecordService: GateRecordService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager
  ) {}

  clear() {
    this.activeModal.dismiss('cancel');
  }

  confirmDelete(id: number) {
    this.gateRecordService.delete(id).subscribe(response => {
      this.eventManager.broadcast({
        name: 'gateRecordListModification',
        content: 'Deleted an gateRecord'
      });
      this.activeModal.dismiss(true);
    });
  }
}

@Component({
  selector: 'jhi-gate-record-delete-popup',
  template: ''
})
export class GateRecordDeletePopupComponent implements OnInit, OnDestroy {
  protected ngbModalRef: NgbModalRef;

  constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ gateRecord }) => {
      setTimeout(() => {
        this.ngbModalRef = this.modalService.open(GateRecordDeleteDialogComponent as Component, { size: 'lg', backdrop: 'static' });
        this.ngbModalRef.componentInstance.gateRecord = gateRecord;
        this.ngbModalRef.result.then(
          result => {
            this.router.navigate(['/gate-record', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          },
          reason => {
            this.router.navigate(['/gate-record', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          }
        );
      }, 0);
    });
  }

  ngOnDestroy() {
    this.ngbModalRef = null;
  }
}
