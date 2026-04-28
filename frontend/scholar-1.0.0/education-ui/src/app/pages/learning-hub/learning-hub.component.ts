import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { FileService } from '../../core/services/file.service';
import { PdfService } from '../../core/services/pdf.service';

interface QuizQuestion {
  question: string;
  options: string[];
  answer: string;
  explanation: string;
}

@Component({
  selector: 'app-learning-hub',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './learning-hub.component.html',
  styleUrls: ['./learning-hub.component.scss']
})
export class LearningHubComponent implements OnInit {
  userId: number = 0;
  files: any[] = [];
  filteredFiles: any[] = [];
  searchKeyword: string = '';
  selectedFile: any = null;

  // Quiz State
  quizQuestions: QuizQuestion[] = [];
  currentQuestionIndex: number = 0;
  selectedOption: string | null = null;
  isAnswered: boolean = false;
  score: number = 0;
  quizFinished: boolean = false;
  isLoading: boolean = false;

  constructor(
    public fileService: FileService,
    private pdfService: PdfService
  ) {}

  ngOnInit(): void {
    const auth = JSON.parse(localStorage.getItem('education_platform_auth') || '{}');
    this.userId = auth.userId;
    this.loadFiles();
  }

  downloadStudyGuide() {
    if (!this.selectedFile) return;

    // Pour le Learning Hub, on veut le résumé ET le quizz
    this.fileService.getSummary(this.selectedFile.id).subscribe({
      next: (summaryTxt) => {
        // Simple parse du texte brut en lignes
        const lines = summaryTxt.split('\n').filter(l => l.trim()).map(l => ({ text: l }));
        
        this.pdfService.generateStudyGuide(
          this.selectedFile.name,
          lines,
          [], // keywords optionnels ici
          [],
          this.quizQuestions
        );
      }
    });
  }

  loadFiles() {
    this.fileService.getFiles(this.userId).subscribe(data => {
      // On ne garde que les vidéos/audios qui peuvent avoir un transcript
      this.files = data.filter(f => f.type?.startsWith('video') || f.type?.startsWith('audio'));
      this.filteredFiles = [...this.files];
    });
  }

  onSearch() {
    const kw = this.searchKeyword.toLowerCase().trim();
    if (!kw) {
      this.filteredFiles = [...this.files];
    } else {
      this.filteredFiles = this.files.filter(f => f.name.toLowerCase().includes(kw));
    }
  }

  selectFile(file: any) {
    this.selectedFile = file;
    this.startNewQuiz(file.id);
  }

  startNewQuiz(fileId: number) {
    this.isLoading = true;
    this.quizQuestions = [];
    this.currentQuestionIndex = 0;
    this.score = 0;
    this.quizFinished = false;
    this.isAnswered = false;
    this.selectedOption = null;

    this.fileService.getQuiz(fileId).subscribe({
      next: (data) => {
        // Shuffle options for each question
        this.quizQuestions = data.map(q => {
          return {
            ...q,
            options: this.shuffleArray([...q.options])
          };
        });
        this.isLoading = false;
      },
      error: (err) => {
        console.error(err);
        this.isLoading = false;
        alert("Impossible de générer le quizz. Assurez-vous d'avoir généré le résumé IA pour cette vidéo d'abord.");
      }
    });
  }

  submitAnswer() {
    if (!this.selectedOption || this.isAnswered) return;

    this.isAnswered = true;
    if (this.selectedOption === this.quizQuestions[this.currentQuestionIndex].answer) {
      this.score++;
    }
  }

  nextQuestion() {
    if (this.currentQuestionIndex < this.quizQuestions.length - 1) {
      this.currentQuestionIndex++;
      this.selectedOption = null;
      this.isAnswered = false;
    } else {
      this.quizFinished = true;
      this.saveFinalResult();
    }
  }

  saveFinalResult() {
    const auth = JSON.parse(localStorage.getItem('education_platform_auth') || '{}');
    const result = {
      userId: auth.userId,
      fileId: this.selectedFile.id,
      fileName: this.selectedFile.name,
      score: this.score,
      totalQuestions: this.quizQuestions.length
    };
    this.fileService.saveQuizResult(result).subscribe();
  }

  restart() {
    this.selectedFile = null;
    this.quizQuestions = [];
  }

  shuffleArray(array: any[]) {
    for (let i = array.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [array[i], array[j]] = [array[j], array[i]];
    }
    return array;
  }

  get progress(): number {
    if (this.quizQuestions.length === 0) return 0;
    return ((this.currentQuestionIndex + 1) / this.quizQuestions.length) * 100;
  }
}
