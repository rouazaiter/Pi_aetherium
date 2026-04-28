import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { KnowledgeBaseComponent } from './knowledge-base.component';

const routes: Routes = [{ path: '', component: KnowledgeBaseComponent }];

@NgModule({
  declarations: [KnowledgeBaseComponent],
  imports: [SharedModule, RouterModule.forChild(routes)]
})
export class KnowledgeBaseModule {}
