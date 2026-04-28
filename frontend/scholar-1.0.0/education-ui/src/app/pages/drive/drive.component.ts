import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { FileService } from '../../core/services/file.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-drive',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './drive.component.html',
  styleUrls: ['./drive.component.scss']
})
export class DriveComponent implements OnInit {

  files: any[] = [];
  userId!: number;

  // ================= VIDEO =================
  selectedVideo: any = null;
  @ViewChild('videoPlayer') videoPlayer!: ElementRef<HTMLVideoElement>;

  // ================= SUMMARY =================
  isTranscribing = false;

  summaryLines: {
    start: string;
    end: string;
    seconds: number;
    title: string;
    description: string;
  }[] = [];

  transcriptInterval: any = null;

  keyword: string = '';

  // --- AI Tutor Chat State ---
  activeTab: 'summary' | 'chat' | 'report' = 'summary';
  chatMessages: {role: 'user'|'ai', content: string}[] = [];
  chatInput: string = '';
  isChatting: boolean = false;

  // --- Session Report State ---
  sessionReport: any = null;

  // --- Global Drive Assistant State ---
  isGlobalChatOpen: boolean = false;
  globalChatMessages: {role: 'user'|'ai', content: string}[] = [];
  globalChatInput: string = '';
  isGlobalChatting: boolean = false;

  constructor(public fileService: FileService) {}

  ngOnInit(): void {
    const auth = JSON.parse(localStorage.getItem('education_platform_auth') || '{}');
    this.userId = auth.userId;
    this.loadFiles();
  }

  // ================= FILES =================
  // loadFiles() {
  //   this.fileService.getFiles(this.userId).subscribe(data => {
  //     this.files = data;
  //   });
  // }

  // ================= OPEN VIDEO =================
  openFile(file: any) {
    if (file.type?.startsWith('video') || file.type?.startsWith('audio')) {
      this.selectedVideo = file;
      this.summaryLines = [];
      this.loadSessionReport(file.id);
      this.loadSummary(file.id);
      
      // Fix: Force scroll to top to see the player
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  loadSummary(fileId: number) {
    this.fileService.getSummary(fileId).subscribe({
      next: (txt: string) => {
        this.parseSummary(this.cleanText(txt));
      },
      error: () => {
        this.summaryLines = []; // File doesn't have a summary yet
      }
    });
  }

  closeVideo() {
    this.selectedVideo = null;
    this.summaryLines = [];
    this.chatMessages = [];
    this.sessionReport = null;
    this.activeTab = 'summary';

    if (this.transcriptInterval) {
      clearInterval(this.transcriptInterval);
    }
  }

  // ================= TRANSCRIBE =================
  transcribe(file: any) {

    this.isTranscribing = true;
    this.summaryLines = [];

    this.fileService.startTranscription(file.id).subscribe({
      next: (jobId) => {

        this.transcriptInterval = setInterval(() => {

          this.fileService.getTranscriptionStatus(jobId).subscribe(res => {

            if (res.status === 'DONE') {

              const result = res.result;

              // 🔥 CASE 1: file path
              if (result && result.includes('.txt')) {

                this.fileService.getSummaryFile(result).subscribe(text => {
                  this.parseSummary(this.cleanText(text));
                  this.isTranscribing = false;
                  this.loadSessionReport(file.id);
                });

              } 
              // 🔥 CASE 2: direct text
              else {
                this.parseSummary(this.cleanText(result));
                this.isTranscribing = false;
              }

              clearInterval(this.transcriptInterval);
            }

            if (res.status === 'ERROR') {
              this.isTranscribing = false;
              clearInterval(this.transcriptInterval);
            }

          });

        }, 2000);

      }
    });
  }

  // ================= CLEAN TEXT =================
  cleanText(text: string): string {
    if (!text) return '';

    return text
      .replace(/\\n/g, '\n')
      .replace(/\r/g, '')
      .replace(/\t/g, ' ');
  }

  // ================= PARSE SUMMARY (ROBUST) =================
parseSummary(text: string) {

  const lines = text.split('\n');

  const result: any[] = [];

  let current: any = null;

  let mode: 'timeline' | 'keywords' | 'usage' = 'timeline';

  this.summaryMeta = {
    keywords: [],
    usage: []
  };

  for (let rawLine of lines) {

    const line = rawLine.trim();
    if (!line) continue;

    // 🔥 detect keywords section
    if (line.toLowerCase().includes('keywords')) {
      mode = 'keywords';
      continue;
    }

    // 🔥 detect usage section
    if (line.toLowerCase().includes('usage')) {
      mode = 'usage';
      continue;
    }

    // 🛡️ RECOVERY: if we see a timestamp anywhere in the line, force switch back to timeline
    const timestampMatch = line.match(/(\d+:\d+)\s*[\-–—→]/);
    if (timestampMatch) {
      mode = 'timeline';
    }

    // ================= TIMELINE =================
    if (mode === 'timeline') {

      // Flexible regex: detects "0:00 - 0:10", "0:00 -> 0:10", "[0:00 - 0:10]", "0:00 → Titre", etc.
      const match = line.match(
        /(\d+:\d+)\s*[\-–—→\s]*(\d+:\d+)?\s*[\-–—→:>]?\s*(.*)$/
      );

      if (match) {

        if (current) result.push(current);

        const start = match[1];
        const end = match[2] || '';
        let title = match[3] || '';

        // Clean title if it starts with unwanted symbols
        title = title.replace(/^[\-–—→:>\]\s]+/, '').trim();

        const [m, s] = start.split(':').map(Number);
        const seconds = m * 60 + s;

        current = {
          start,
          end,
          seconds,
          title,
          description: ''
        };

      } else {

        if (current) {
          current.description += (current.description ? ' ' : '') + line;
        }
      }
    }

    // ================= KEYWORDS =================
    else if (mode === 'keywords') {

      if (line.startsWith('-')) {
        this.summaryMeta.keywords.push(
          line.replace('-', '').trim()
        );
      }
    }

    // ================= USAGE =================
    else if (mode === 'usage') {

      if (line.match(/^\d+\./)) {
        this.summaryMeta.usage.push(line);
      }
    }
  }

  if (current) result.push(current);

  this.summaryLines = result;
}

  // ================= SEEK VIDEO =================
  seekTo(seconds: number) {
    if (this.videoPlayer?.nativeElement) {
      this.videoPlayer.nativeElement.currentTime = seconds;
      this.videoPlayer.nativeElement.play();
    }
  }

  // ================= AI TUTOR CHAT & REPORT =================
  switchTab(tab: 'summary' | 'chat' | 'report') {
    this.activeTab = tab;
  }

  loadSessionReport(fileId: number) {
    this.fileService.getReport(fileId).subscribe({
      next: (data) => {
        this.sessionReport = data;
      },
      error: () => {
        this.sessionReport = null;
      }
    });
  }

  sendChatMessage() {
    if (!this.chatInput.trim() || !this.selectedVideo) return;

    const question = this.chatInput.trim();
    this.chatMessages.push({ role: 'user', content: question });
    this.chatInput = '';
    this.isChatting = true;

    // Call AI Tutor Endpoint
    this.fileService.chatWithVideo(this.selectedVideo.id, question).subscribe({
      next: (res) => {
        this.chatMessages.push({ role: 'ai', content: res.answer });
        this.isChatting = false;
      },
      error: (err) => {
        const errMsg = err.error?.error || "Désolé, une erreur est survenue avec le Tuteur IA.";
        this.chatMessages.push({ role: 'ai', content: errMsg });
        this.isChatting = false;
      }
    });
  }

  // ================= GLOBAL DRIVE ASSISTANT =================
  toggleGlobalChat() {
    this.isGlobalChatOpen = !this.isGlobalChatOpen;
  }

  sendGlobalChatMessage() {
    if (!this.globalChatInput.trim()) return;

    const question = this.globalChatInput.trim();
    this.globalChatMessages.push({ role: 'user', content: question });
    this.globalChatInput = '';
    this.isGlobalChatting = true;

    this.fileService.globalChat(this.userId, question).subscribe({
      next: (res) => {
        this.globalChatMessages.push({ role: 'ai', content: res.answer });
        this.isGlobalChatting = false;
      },
      error: (err) => {
        const errMsg = err.error?.error || "Erreur de l'Assistant Global. Avez-vous généré des bilans de session ?";
        this.globalChatMessages.push({ role: 'ai', content: errMsg });
        this.isGlobalChatting = false;
      }
    });
  }

  // Permet de transformer le Markdown basique (Gras, Titres) en HTML sécurisé
  formatChatMessage(text: string): string {
    if (!text) return '';
    let formatted = text
      // Gras : **texte** -> <strong>texte</strong>
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      // Titre 3 : ### Titre -> <h3>Titre</h3>
      .replace(/### (.*)/g, '<h3 style="margin-top:10px; margin-bottom:5px; font-size:16px;">$1</h3>')
      // Échapper les balises HTML de base pour la sécurité (optionnel mais recommandé)
      .replace(/<script>/g, '&lt;script&gt;');
    return formatted;
  }

  // ================= SEARCH =================
  onSearch() {
    this.fileService.searchFiles(this.userId, this.keyword)
      .subscribe(data => this.files = data);
  }

  summaryMeta: {
  keywords: string[];
  usage: string[];
} = {
  keywords: [],
  usage: []
};




  used = 0;

  total = 50; // GB

  onFileSelected(event: any) {
    const file = event.target.files[0];

    if (file) {
      this.fileService.upload(file, this.userId).subscribe(() => {
        this.loadFiles();
      });
    }
  }

    loadFiles() {
    this.fileService.getFiles(this.userId).subscribe(data => {
      this.files = data;

      // 💡 calcul quota simple
      this.used = data.reduce((sum, f) => sum + f.size, 0) / (1024 * 1024 * 1024);
    });
  }


  downloadFile(file: any) {
    // Use direct anchor link — bypasses CORS restrictions on blob responses
    const url = this.fileService.getFileUrl(file.id);
    const a = document.createElement('a');
    a.href = url;
    a.download = file.name || 'download';
    a.target = '_self';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }

renameFile(file: any) {
  const newName = prompt('New name', file.name);

  if (!newName) return;

  this.fileService.rename(file.id, newName)
    .subscribe(() => this.loadFiles());
}


deleteFile(file: any) {

  const confirmDelete = confirm('Delete this file ?');

  if (!confirmDelete) return;

  this.fileService.delete(file.id)
    .subscribe(() => this.loadFiles());
}

// Fallback si le thumbnail n'existe pas (anciens fichiers uploadés avant la feature)
onThumbError(event: Event) {
  const img = event.target as HTMLImageElement;
  img.onerror = null; // évite boucle infinie

  // encodeURIComponent supporte Unicode (btoa plante avec ▶)
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="480" height="270" viewBox="0 0 480 270">
    <rect width="480" height="270" fill="#1a1a2e"/>
    <circle cx="240" cy="120" r="45" fill="rgba(122,106,216,0.85)"/>
    <polygon points="228,100 228,140 268,120" fill="white"/>
    <text x="240" y="195" font-size="16" text-anchor="middle" fill="#666" font-family="sans-serif">No preview</text>
  </svg>`;

  img.src = 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svg);
}


}