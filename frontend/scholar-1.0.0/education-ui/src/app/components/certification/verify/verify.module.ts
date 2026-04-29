import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { RouterModule, Routes } from '@angular/router';
import { VerifyComponent } from './verify.component';

const routes: Routes = [
  { path: '', component: VerifyComponent }
];

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        HttpClientModule,
        RouterModule.forChild(routes),
        VerifyComponent
    ]
})
export class VerifyModule {}
