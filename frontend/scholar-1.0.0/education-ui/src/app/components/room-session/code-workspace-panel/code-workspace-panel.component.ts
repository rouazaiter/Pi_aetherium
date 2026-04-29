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
  templateUrl: './code-workspace-panel.component.html',
  styleUrls: ['./code-workspace-panel.component.scss']
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



