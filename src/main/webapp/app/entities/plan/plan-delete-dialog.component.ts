import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IPlan } from 'app/shared/model/plan.model';
import { PlanService } from './plan.service';

@Component({
  selector: 'jhi-plan-delete-dialog',
  templateUrl: './plan-delete-dialog.component.html'
})
export class PlanDeleteDialogComponent {
  plan: IPlan;

  constructor(protected planService: PlanService, public activeModal: NgbActiveModal, protected eventManager: JhiEventManager) {}

  clear() {
    this.activeModal.dismiss('cancel');
  }

  confirmDelete(id: number) {
    this.planService.delete(id).subscribe(response => {
      this.eventManager.broadcast({
        name: 'planListModification',
        content: 'Deleted an plan'
      });
      this.activeModal.dismiss(true);
    });
  }
}

@Component({
  selector: 'jhi-plan-delete-popup',
  template: ''
})
export class PlanDeletePopupComponent implements OnInit, OnDestroy {
  protected ngbModalRef: NgbModalRef;

  constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ plan }) => {
      setTimeout(() => {
        this.ngbModalRef = this.modalService.open(PlanDeleteDialogComponent as Component, { size: 'lg', backdrop: 'static' });
        this.ngbModalRef.componentInstance.plan = plan;
        this.ngbModalRef.result.then(
          result => {
            this.router.navigate(['/plan', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          },
          reason => {
            this.router.navigate(['/plan', { outlets: { popup: null } }]);
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
