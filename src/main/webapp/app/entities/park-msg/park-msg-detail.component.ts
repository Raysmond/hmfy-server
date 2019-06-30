import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IParkMsg } from 'app/shared/model/park-msg.model';

@Component({
  selector: 'jhi-park-msg-detail',
  templateUrl: './park-msg-detail.component.html'
})
export class ParkMsgDetailComponent implements OnInit {
  parkMsg: IParkMsg;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ parkMsg }) => {
      this.parkMsg = parkMsg;
    });
  }

  previousState() {
    window.history.back();
  }
}
