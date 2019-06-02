import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';

@NgModule({
  imports: [
    RouterModule.forChild([
      {
        path: 'region',
        loadChildren: './region/region.module#ShieldRegionModule'
      },
      {
        path: 'region',
        loadChildren: './region/region.module#ShieldRegionModule'
      },
      {
        path: 'region',
        loadChildren: './region/region.module#ShieldRegionModule'
      },
      {
        path: 'region',
        loadChildren: './region/region.module#ShieldRegionModule'
      },
      {
        path: 'car',
        loadChildren: './car/car.module#ShieldCarModule'
      },
      {
        path: 'appointment',
        loadChildren: './appointment/appointment.module#ShieldAppointmentModule'
      },
      {
        path: 'region',
        loadChildren: './region/region.module#ShieldRegionModule'
      },
      {
        path: 'appointment',
        loadChildren: './appointment/appointment.module#ShieldAppointmentModule'
      },
      {
        path: 'appointment',
        loadChildren: './appointment/appointment.module#ShieldAppointmentModule'
      },
      {
        path: 'wx-ma-user',
        loadChildren: './wx-ma-user/wx-ma-user.module#ShieldWxMaUserModule'
      },
      {
        path: 'wx-ma-user',
        loadChildren: './wx-ma-user/wx-ma-user.module#ShieldWxMaUserModule'
      },
      {
        path: 'wx-ma-user',
        loadChildren: './wx-ma-user/wx-ma-user.module#ShieldWxMaUserModule'
      },
      {
        path: 'wx-ma-user',
        loadChildren: './wx-ma-user/wx-ma-user.module#ShieldWxMaUserModule'
      },
      {
        path: 'region',
        loadChildren: './region/region.module#ShieldRegionModule'
      },
      {
        path: 'ship-plan',
        loadChildren: './ship-plan/ship-plan.module#ShieldShipPlanModule'
      },
      {
        path: 'region',
        loadChildren: './region/region.module#ShieldRegionModule'
      },
      {
        path: 'appointment',
        loadChildren: './appointment/appointment.module#ShieldAppointmentModule'
      },
      {
        path: 'ship-plan',
        loadChildren: './ship-plan/ship-plan.module#ShieldShipPlanModule'
      },
      {
        path: 'car',
        loadChildren: './car/car.module#ShieldCarModule'
      },
      {
        path: 'car',
        loadChildren: './car/car.module#ShieldCarModule'
      }
      /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
    ])
  ],
  declarations: [],
  entryComponents: [],
  providers: [],
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class ShieldEntityModule {}
