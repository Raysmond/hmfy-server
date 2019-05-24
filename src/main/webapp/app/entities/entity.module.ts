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
