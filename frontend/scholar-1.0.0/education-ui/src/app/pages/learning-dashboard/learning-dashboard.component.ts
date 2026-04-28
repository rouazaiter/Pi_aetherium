import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FileService } from '../../core/services/file.service';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-learning-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './learning-dashboard.component.html',
  styleUrls: ['./learning-dashboard.component.scss']
})
export class LearningDashboardComponent implements OnInit {
  stats: any = {
    totalFiles: 0,
    analyzedFiles: 0,
    totalQuizzes: 0,
    averageScore: 0,
    recentResults: []
  };
  userId: number = 0;
  isLoading: boolean = true;
  aiAnalysis: string = '';
  isAnalyzing: boolean = false;

  constructor(private fileService: FileService) {}

  ngOnInit(): void {
    const auth = JSON.parse(localStorage.getItem('education_platform_auth') || '{}');
    this.userId = auth.userId;
    this.loadStats();
    this.runAiAnalysis();
  }

  runAiAnalysis() {
    this.isAnalyzing = true;
    this.fileService.analyzePerformance(this.userId).subscribe({
      next: (data) => {
        this.aiAnalysis = data.analysis;
        this.isAnalyzing = false;
      },
      error: () => {
        this.isAnalyzing = false;
      }
    });
  }

  loadStats() {
    this.isLoading = true;
    this.fileService.getUserStats(this.userId).subscribe({
      next: (data) => {
        this.stats = data;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  getScoreColor(score: number, total: number): string {
    const percentage = (score / total) * 100;
    if (percentage >= 80) return 'text-success';
    if (percentage >= 50) return 'text-warning';
    return 'text-danger';
  }

  getCompletionRate(): number {
    if (this.stats.totalFiles === 0) return 0;
    return Math.round((this.stats.analyzedFiles / this.stats.totalFiles) * 100);
  }

  formatAnalysis(text: string): string {
    if (!text) return '';
    return text
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\n/g, '<br>');
  }
}
