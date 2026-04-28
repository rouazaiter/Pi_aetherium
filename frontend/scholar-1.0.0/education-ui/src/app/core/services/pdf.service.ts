import { Injectable } from '@angular/core';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

@Injectable({
  providedIn: 'root'
})
export class PdfService {

  constructor() { }

  generateStudyGuide(fileName: string, summary: any[], keywords: string[], usage: string[], quiz?: any[]) {
    const doc = new jsPDF();
    const pageWidth = doc.internal.pageSize.getWidth();

    // --- HEADER ---
    doc.setFillColor(122, 106, 216); // Purple brand color
    doc.rect(0, 0, pageWidth, 40, 'F');
    
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(22);
    doc.setFont('helvetica', 'bold');
    doc.text('SKILLHUB - GUIDE D\'ETUDE', 20, 25);
    
    doc.setFontSize(10);
    doc.text('Généré par Intelligence Artificielle', pageWidth - 70, 25);

    // --- TITLE ---
    doc.setTextColor(44, 62, 80);
    doc.setFontSize(18);
    doc.text(fileName, 20, 55);
    doc.setLineWidth(0.5);
    doc.line(20, 58, 100, 58);

    let currentY = 70;

    // --- SUMMARY SECTION ---
    doc.setFontSize(14);
    doc.setFont('helvetica', 'bold');
    doc.text('1. Résumé de la Session', 20, currentY);
    currentY += 10;
    
    doc.setFontSize(11);
    doc.setFont('helvetica', 'normal');
    
    summary.forEach(line => {
      // Extraction intelligente du texte selon le format (objet ou string)
      let textToPrint = "";
      if (typeof line === 'string') {
        textToPrint = line;
      } else if (line.title && line.description) {
        textToPrint = `${line.start} - ${line.end} : ${line.title}\n${line.description}`;
      } else if (line.text) {
        textToPrint = line.text;
      } else {
        textToPrint = JSON.stringify(line);
      }

      const splitText = doc.splitTextToSize(textToPrint, pageWidth - 40);
      doc.text(splitText, 20, currentY);
      currentY += (splitText.length * 7) + 2; // +2 for spacing between blocks
      
      if (currentY > 270) {
        doc.addPage();
        currentY = 20;
      }
    });

    // --- KEYWORDS ---
    currentY += 10;
    doc.setFontSize(14);
    doc.setFont('helvetica', 'bold');
    doc.text('2. Mots-clés & Concepts', 20, currentY);
    currentY += 8;
    
    doc.setFontSize(11);
    doc.setFont('helvetica', 'normal');
    const kwText = keywords.join(' • ');
    const splitKw = doc.splitTextToSize(kwText, pageWidth - 40);
    doc.text(splitKw, 20, currentY);
    currentY += (splitKw.length * 7);

    // --- QUIZ SECTION (if available) ---
    if (quiz && quiz.length > 0) {
      if (currentY > 230) {
        doc.addPage();
        currentY = 20;
      }
      
      currentY += 10;
      doc.setFontSize(14);
      doc.setFont('helvetica', 'bold');
      doc.text('3. Quizz de Révision', 20, currentY);
      currentY += 5;

      const tableData = quiz.map((q, index) => [
        `Q${index + 1}`,
        q.question,
        q.answer
      ]);

      autoTable(doc, {
        startY: currentY,
        head: [['#', 'Question', 'Réponse Correcte']],
        body: tableData,
        theme: 'striped',
        headStyles: { fillColor: [122, 106, 216] },
        styles: { fontSize: 9, cellPadding: 5 }
      });
    }

    // --- FOOTER ---
    const pageCount = (doc as any).internal.getNumberOfPages();
    for (let i = 1; i <= pageCount; i++) {
      doc.setPage(i);
      doc.setFontSize(10);
      doc.setTextColor(150);
      doc.text(`Page ${i} sur ${pageCount}`, pageWidth / 2, 285, { align: 'center' });
      doc.text('SkillHub Platform - Votre assistant d\'apprentissage IA', 20, 285);
    }

    doc.save(`Guide_Etude_${fileName.replace(/\s+/g, '_')}.pdf`);
  }
}
