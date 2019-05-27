import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IWxMaUser } from 'app/shared/model/wx-ma-user.model';
import { WxMaUserService } from './wx-ma-user.service';

@Component({
  selector: 'jhi-wx-ma-user-delete-dialog',
  templateUrl: './wx-ma-user-delete-dialog.component.html'
})
export class WxMaUserDeleteDialogComponent {
  wxMaUser: IWxMaUser;

  constructor(protected wxMaUserService: WxMaUserService, public activeModal: NgbActiveModal, protected eventManager: JhiEventManager) {}

  clear() {
    this.activeModal.dismiss('cancel');
  }

  confirmDelete(id: number) {
    this.wxMaUserService.delete(id).subscribe(response => {
      this.eventManager.broadcast({
        name: 'wxMaUserListModification',
        content: 'Deleted an wxMaUser'
      });
      this.activeModal.dismiss(true);
    });
  }
}

@Component({
  selector: 'jhi-wx-ma-user-delete-popup',
  template: ''
})
export class WxMaUserDeletePopupComponent implements OnInit, OnDestroy {
  protected ngbModalRef: NgbModalRef;

  constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ wxMaUser }) => {
      setTimeout(() => {
        this.ngbModalRef = this.modalService.open(WxMaUserDeleteDialogComponent as Component, { size: 'lg', backdrop: 'static' });
        this.ngbModalRef.componentInstance.wxMaUser = wxMaUser;
        this.ngbModalRef.result.then(
          result => {
            this.router.navigate(['/wx-ma-user', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          },
          reason => {
            this.router.navigate(['/wx-ma-user', { outlets: { popup: null } }]);
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
