import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { RouterModule, Routes } from '@angular/router';
import { ShowcaseComponent } from './showcase.component';

const routes: Routes = [
  { path: '', component: ShowcaseComponent }
];

@NgModule({
    imports: [
        CommonModule,
        HttpClientModule,
        RouterModule.forChild(routes),
        ShowcaseComponent
    ]
})
export class ShowcaseModule {}
