import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PreLoadingComponent } from './pre-loading/pre-loading.component';
import { PageNotFoundComponent } from './page-not-found/page-not-found.component';
import { BreadcrumbsComponent } from './breadcrumbs/breadcrumbs.component';
import { SpinnerComponent } from './spinner/spinner.component';


@NgModule({
  declarations: [
    PreLoadingComponent,
    PageNotFoundComponent,
    BreadcrumbsComponent,
    SpinnerComponent
  ],
  exports: [
    PreLoadingComponent,
    BreadcrumbsComponent,
    SpinnerComponent
  ],
  imports: [
    CommonModule
  ]
})
export class SharedModule { }
