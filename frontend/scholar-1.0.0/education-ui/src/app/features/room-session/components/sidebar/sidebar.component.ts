import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule],
  template: `
    <aside class="bg-white border-r border-slate-200 w-[280px] flex flex-col h-full py-4 space-y-1 shrink-0">
      <div class="px-6 mb-6">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 bg-primary-container rounded-lg flex items-center justify-center text-on-primary">
            <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">group_work</span>
          </div>
          <div class="flex flex-col">
            <span class="text-base font-black tracking-tight text-[#464EB8]">Enterprise Workspace</span>
            <span class="text-[11px] text-slate-500 font-medium">Collaborative Team</span>
          </div>
        </div>
      </div>
      <nav class="flex-1 px-3 space-y-1">
        <a class="flex items-center gap-3 px-3 py-2 text-slate-600 hover:bg-slate-200/50 transition-all duration-150 ease-in-out group" href="javascript:void(0)">
          <span class="material-symbols-outlined text-[20px]">notifications</span>
          <span class="font-body-md">Activity</span>
        </a>
        <ul class="space-y-1">
          <li>
            <button type="button" (click)="select('code')" [ngClass]="buttonClasses('code')">
              <span class="material-symbols-outlined text-[20px]">code</span>
              <span class="font-body-md">IDE</span>
            </button>
          </li>
          <li>
            <button type="button" (click)="select('whiteboard')" [ngClass]="buttonClasses('whiteboard')">
              <span class="material-symbols-outlined text-[20px]">gesture</span>
              <span class="font-body-md">Whiteboard</span>
            </button>
          </li>
          <li>
            <a class="flex items-center gap-3 px-3 py-2 text-slate-600 hover:bg-slate-200/50 transition-all duration-150 ease-in-out group" href="javascript:void(0)">
              <span class="material-symbols-outlined text-[20px]">cloud</span>
              <span class="font-body-md">Drive</span>
            </a>
          </li>
        </ul>
      </nav>
      <div class="px-3 pt-4 border-t border-slate-200 space-y-1">
        <a class="flex items-center gap-3 px-3 py-2 text-slate-600 hover:bg-slate-200/50 transition-all duration-150 ease-in-out group" href="javascript:void(0)">
          <span class="material-symbols-outlined text-[20px]">chat</span>
          <span class="font-body-md">Chat</span>
        </a>
        <a class="flex items-center gap-3 px-3 py-2 text-slate-600 hover:bg-slate-200/50 transition-all duration-150 ease-in-out group" href="javascript:void(0)">
          <span class="material-symbols-outlined text-[20px]">grid_view</span>
          <span class="font-body-md">Apps</span>
        </a>
        <a class="flex items-center gap-3 px-3 py-2 text-slate-600 hover:bg-slate-200/50 transition-all duration-150 ease-in-out group" href="javascript:void(0)">
          <span class="material-symbols-outlined text-[20px]">help_outline</span>
          <span class="font-body-md">Help</span>
        </a>
      </div>
    </aside>
  `
})
export class SidebarComponent {
  @Input() activeSection: 'code' | 'whiteboard' = 'code';
  @Output() activeSectionChange = new EventEmitter<'code' | 'whiteboard'>();

  select(section: 'code' | 'whiteboard'): void {
    this.activeSectionChange.emit(section);
  }

  buttonClasses(section: 'code' | 'whiteboard'): string {
    const base = 'w-full flex items-center gap-3 px-3 py-2 transition-all duration-150 ease-in-out';
    if (this.activeSection === section) {
      return `${base} bg-white text-[#464EB8] font-medium border-l-4 border-[#464EB8] rounded-r-md`;
    }
    return `${base} text-slate-600 hover:bg-slate-200/50 rounded-lg`;
  }
}

