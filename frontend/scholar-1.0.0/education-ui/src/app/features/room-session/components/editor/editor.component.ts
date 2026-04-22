import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CodeWorkspacePanelComponent } from '../code-workspace-panel/code-workspace-panel.component';
import { WhiteboardPanelComponent } from '../whiteboard-panel/whiteboard-panel.component';

@Component({
  selector: 'app-editor',
  standalone: true,
  host: {
    class: 'flex-1 min-w-0 flex'
  },
  imports: [CommonModule, CodeWorkspacePanelComponent, WhiteboardPanelComponent],
  template: `
    <main class="w-full h-full min-h-0 flex flex-col min-w-0 bg-white dark:bg-slate-900" layout-main>
      <app-code-workspace-panel class="flex-1 min-h-0 block" *ngIf="activeTab === 'code'" [roomId]="roomId"></app-code-workspace-panel>
      <app-whiteboard-panel class="flex-1 min-h-0 block" *ngIf="activeTab === 'whiteboard'" [roomId]="roomId"></app-whiteboard-panel>
    </main>
  `
})
export class EditorComponent {
  @Input() roomId = 0;
  @Input() isHost = false;
  @Input() recordingActive = false;
  @Input() recordingDuration = '0:00';
  @Input() activeTab: 'code' | 'whiteboard' = 'code';

  @Output() activeTabChange = new EventEmitter<'code' | 'whiteboard'>();

  switchTab(tab: 'code' | 'whiteboard'): void {
    this.activeTab = tab;
    this.activeTabChange.emit(tab);
  }
}



