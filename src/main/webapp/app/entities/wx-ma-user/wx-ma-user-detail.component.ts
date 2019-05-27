import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IWxMaUser } from 'app/shared/model/wx-ma-user.model';

@Component({
  selector: 'jhi-wx-ma-user-detail',
  templateUrl: './wx-ma-user-detail.component.html'
})
export class WxMaUserDetailComponent implements OnInit {
  wxMaUser: IWxMaUser;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ wxMaUser }) => {
      this.wxMaUser = wxMaUser;
    });
  }

  previousState() {
    window.history.back();
  }
}
