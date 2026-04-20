import { Component, OnInit } from '@angular/core';
import { FileService } from '../../core/services/file.service';
import { CommonModule } from '@angular/common';
import { FileModel } from '../../core/models/file.model';
@Component({
  selector: 'app-drive',
  standalone: true,
  imports: [CommonModule],
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
summaryText: string = '';
selectedFileId: number | null = null;
summarize(file: any) {

  this.selectedFileId = file.id;

  this.fileService.summarize(file.id).subscribe(res => {

    this.summaryText = res.summary;

  });
}
}