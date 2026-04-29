import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
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

