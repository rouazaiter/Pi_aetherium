import { Component } from '@angular/core';

@Component({
  selector: 'app-app-layout',
  standalone: true,
  template: `
    <div class="bg-background text-on-surface font-body-md antialiased overflow-hidden h-screen flex flex-col">
      <ng-content select="app-topbar"></ng-content>
      <div class="flex flex-1 pt-16 h-full overflow-hidden">
        <ng-content select="app-sidebar"></ng-content>
        <ng-content select="[layout-main]"></ng-content>
        <ng-content select="[layout-right]"></ng-content>
      </div>
    </div>
  `
})
export class AppLayoutComponent {}


