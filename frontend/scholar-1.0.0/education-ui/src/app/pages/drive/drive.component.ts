import { Component, OnInit } from '@angular/core';
import { FileService } from '../../core/services/file.service';
import { CommonModule } from '@angular/common';
import { FileModel } from '../../core/models/file.model';
import { FormsModule } from '@angular/forms';
@Component({
  selector: 'app-drive',
  standalone: true,
  imports: [CommonModule,FormsModule],

  templateUrl: './drive.component.html',
  styleUrls: ['./drive.component.scss']
})
export class DriveComponent implements OnInit {

files: FileModel[] = [];
  userId!: number;// 🔥 temporaire (après JWT)

  used = 0;
  total = 50; // GB

  constructor(public fileService: FileService) {}

  ngOnInit(): void { 
      const auth = JSON.parse(localStorage.getItem('education_platform_auth') || '{}');

  this.userId = auth.userId;

  this.loadFiles();
  }

  loadFiles() {
    this.fileService.getFiles(this.userId).subscribe(data => {
      this.files = data;

      // 💡 calcul quota simple
      this.used = data.reduce((sum, f) => sum + f.size, 0) / (1024 * 1024 * 1024);
    });
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];

    if (file) {
      this.fileService.upload(file, this.userId).subscribe(() => {
        this.loadFiles();
      });
    }
  }

  delete(id: number) {
    this.fileService.delete(id).subscribe(() => {
      this.loadFiles();
    });
  }

  rename(file: any) {
    const newName = prompt("New name:", file.name);

    if (newName) {
      this.fileService.rename(file.id, newName).subscribe(() => {
        this.loadFiles();
      });
    }
  }

  download(file: any) {
  this.fileService.download(file.id).subscribe((blob: Blob) => {

    const url = window.URL.createObjectURL(blob);

    const a = document.createElement('a');
    a.href = url;
    a.download = file.name;

    a.click();

    window.URL.revokeObjectURL(url);
  });
}
// summaryText: string = '';
// selectedFileId: number | null = null;
// summarize(file: any) {

//   this.selectedFileId = file.id;

//   this.fileService.summarize(file.id).subscribe(res => {

//     this.summaryText = res.summary;

//   });
// }


summaryText: string = '';
isLoading = false;

summarize(file: any) {

  this.isLoading = true;
  this.summaryText = '';

  // 1. start job
  this.fileService.startAiSummary(file.id).subscribe(jobId => {

    this.jobId = jobId;

    // 2. polling
    this.interval = setInterval(() => {

      this.fileService.getAiSummaryStatus(jobId).subscribe(res => {

        if (res.status === 'DONE') {

          this.summaryText = res.result;
          this.isLoading = false;

          clearInterval(this.interval);
        }

        if (res.status === 'ERROR') {

          this.summaryText = "Erreur AI";
          this.isLoading = false;

          clearInterval(this.interval);
        }

      });

    }, 2000); // check every 2 sec

  });

}

jobId: number | null = null;

interval: any;

keyword: string = '';

onSearch() {
  this.fileService.searchFiles(this.userId, this.keyword)
    .subscribe({
      next: (data) => {
        this.files = data;
      },
      error: (err) => {
        console.error(err);
      }
    });
}

}