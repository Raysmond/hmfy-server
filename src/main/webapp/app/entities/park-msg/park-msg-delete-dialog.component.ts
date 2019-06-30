import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IParkMsg } from 'app/shared/model/park-msg.model';
import { ParkMsgService } from './park-msg.service';

@Component({
  selector: 'jhi-park-msg-delete-dialog',
  templateUrl: './park-msg-delete-dialog.component.html'
})
export class ParkMsgDeleteDialogComponent {
  parkMsg: IParkMsg;

  constructor(protected parkMsgService: ParkMsgService, public activeModal: NgbActiveModal, protected eventManager: JhiEventManager) {}

  clear() {
    this.activeModal.dismiss('cancel');
  }

  confirmDelete(id: number) {
    this.parkMsgService.delete(id).subscribe(response => {
      this.eventManager.broadcast({
        name: 'parkMsgListModification',
        content: 'Deleted an parkMsg'
      });
      this.activeModal.dismiss(true);
    });
  }
}

@Component({
  selector: 'jhi-park-msg-delete-popup',
  template: ''
})
export class ParkMsgDeletePopupComponent implements OnInit, OnDestroy {
  protected ngbModalRef: NgbModalRef;

  constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ parkMsg }) => {
      setTimeout(() => {
        this.ngbModalRef = this.modalService.open(ParkMsgDeleteDialogComponent as Component, { size: 'lg', backdrop: 'static' });
        this.ngbModalRef.componentInstance.parkMsg = parkMsg;
        this.ngbModalRef.result.then(
          result => {
            this.router.navigate(['/park-msg', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          },
          reason => {
            this.router.navigate(['/park-msg', { outlets: { popup: null } }]);
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
