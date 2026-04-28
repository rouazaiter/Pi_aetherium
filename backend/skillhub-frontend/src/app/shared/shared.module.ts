import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from './components/navbar/navbar.component';
import { FooterComponent } from './components/footer/footer.component';
import { MicButtonComponent } from './components/mic-button/mic-button.component';
import { PostTooltipDirective } from './directives/post-tooltip.directive';
import { ToastComponent } from './components/toast/toast.component';

@NgModule({
  declarations: [NavbarComponent, FooterComponent, PostTooltipDirective],
  imports: [CommonModule, RouterModule, FormsModule, MicButtonComponent, ToastComponent],
  exports: [
    NavbarComponent, FooterComponent, MicButtonComponent,
    PostTooltipDirective, ToastComponent,
    CommonModule, FormsModule, RouterModule
  ]
})
export class SharedModule {}
