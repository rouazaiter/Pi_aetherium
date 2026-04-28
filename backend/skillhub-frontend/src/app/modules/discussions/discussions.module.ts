import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { DiscussionsComponent } from './discussions.component';

const routes: Routes = [{ path: '', component: DiscussionsComponent }];

@NgModule({
  declarations: [DiscussionsComponent],
  imports: [SharedModule, RouterModule.forChild(routes)]
})
export class DiscussionsModule {}
