import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IShipPlan } from 'app/shared/model/ship-plan.model';
import { ShipPlanService } from './ship-plan.service';

@Component({
  selector: 'jhi-ship-plan-delete-dialog',
  templateUrl: './ship-plan-delete-dialog.component.html'
})
export class ShipPlanDeleteDialogComponent {
  shipPlan: IShipPlan;

  constructor(protected shipPlanService: ShipPlanService, public activeModal: NgbActiveModal, protected eventManager: JhiEventManager) {}

  clear() {
    this.activeModal.dismiss('cancel');
  }

  confirmDelete(id: number) {
    this.shipPlanService.delete(id).subscribe(response => {
      this.eventManager.broadcast({
        name: 'shipPlanListModification',
        content: 'Deleted an shipPlan'
      });
      this.activeModal.dismiss(true);
    });
  }
}

@Component({
  selector: 'jhi-ship-plan-delete-popup',
  template: ''
})
export class ShipPlanDeletePopupComponent implements OnInit, OnDestroy {
  protected ngbModalRef: NgbModalRef;

  constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ shipPlan }) => {
      setTimeout(() => {
        this.ngbModalRef = this.modalService.open(ShipPlanDeleteDialogComponent as Component, { size: 'lg', backdrop: 'static' });
        this.ngbModalRef.componentInstance.shipPlan = shipPlan;
        this.ngbModalRef.result.then(
          result => {
            this.router.navigate(['/ship-plan', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          },
          reason => {
            this.router.navigate(['/ship-plan', { outlets: { popup: null } }]);
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
