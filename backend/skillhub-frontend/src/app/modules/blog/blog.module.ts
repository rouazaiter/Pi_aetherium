import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { BlogComponent } from './blog.component';

const routes: Routes = [{ path: '', component: BlogComponent }];

@NgModule({
  declarations: [BlogComponent],
  imports: [SharedModule, RouterModule.forChild(routes)]
})
export class BlogModule {}
