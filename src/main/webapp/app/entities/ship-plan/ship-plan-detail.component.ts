import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IShipPlan } from 'app/shared/model/ship-plan.model';

@Component({
  selector: 'jhi-ship-plan-detail',
  templateUrl: './ship-plan-detail.component.html'
})
export class ShipPlanDetailComponent implements OnInit {
  shipPlan: IShipPlan;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ shipPlan }) => {
      this.shipPlan = shipPlan;
    });
  }

  previousState() {
    window.history.back();
  }
}
