import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

@Component({
  selector: 'app-code-workspace-panel',
  standalone: true,
  host: {
    class: 'block h-full min-h-0 flex-1'
  },
  imports: [CommonModule],
  template: `
    <section class="h-full flex flex-col bg-white dark:bg-slate-900">
      <div class="h-10 bg-slate-50 dark:bg-slate-950 border-b border-slate-200 dark:border-slate-800 flex items-center justify-between px-4">
        <div class="flex items-center gap-2 text-xs text-slate-500 dark:text-slate-300 uppercase tracking-wider font-semibold">
          <span class="material-symbols-outlined text-sm">code</span>
          External Workspace API
        </div>
        <div class="flex items-center gap-2">
          <button type="button" class="text-xs text-primary font-semibold hover:opacity-80" (click)="reloadFrame()">Reload</button>
          <a [href]="ideUrl" target="_blank" rel="noopener" class="text-xs text-slate-500 dark:text-slate-300 hover:text-primary">Open new tab</a>
        </div>
      </div>

      <div class="flex-1 min-h-0">
        <iframe
          title="External Code Workspace"
          class="w-full h-full border-0 bg-white dark:bg-slate-900"
          [src]="safeIdeUrl"
          loading="lazy"
          referrerpolicy="no-referrer">
        </iframe>
      </div>
    </section>
  `
})
export class CodeWorkspacePanelComponent implements OnInit {
  @Input() roomId = 0;
  ideUrl = 'http://localhost:3000';
  safeIdeUrl!: SafeResourceUrl;

  constructor(
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    this.safeIdeUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.ideUrl);
  }

  reloadFrame(): void {
    this.safeIdeUrl = this.sanitizer.bypassSecurityTrustResourceUrl(`${this.ideUrl}?t=${Date.now()}`);
  }
}



