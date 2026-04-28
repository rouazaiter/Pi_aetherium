import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { CoreModule } from './core/core.module';
import { SharedModule } from './shared/shared.module';
import { ToastComponent } from './shared/components/toast/toast.component';

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    AppRoutingModule,
    CoreModule,
    SharedModule,
    ToastComponent   // standalone component — imported directly so AppComponent can use <app-toast>
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
