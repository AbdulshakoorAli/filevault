import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { DocumentIntelligenceService } from '../../services/document-intelligence.service';
import { DocumentAnalysisResult } from '../../models/document-analysis.model';

export interface DocumentAnalysisDialogData {
  blobName: string;
  fileName: string;
}

@Component({
  selector: 'app-document-analysis-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './document-analysis-dialog.component.html',
  styleUrls: ['./document-analysis-dialog.component.scss']
})
export class DocumentAnalysisDialogComponent implements OnInit {
  analysis: DocumentAnalysisResult | null = null;
  keyValueRows: { key: string; value: string }[] = [];
  isLoading = false;
  isAnalyzing = false;
  errorMessage = '';

  constructor(
    public dialogRef: MatDialogRef<DocumentAnalysisDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DocumentAnalysisDialogData,
    private intelligenceService: DocumentIntelligenceService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadExistingAnalysis();
  }

  close(): void {
    this.dialogRef.close();
  }

  analyze(): void {
    this.isAnalyzing = true;
    this.errorMessage = '';
    this.intelligenceService.analyzeDocument(this.data.blobName).subscribe({
      next: (result) => {
        this.applyResult(result);
        this.isAnalyzing = false;
        this.snackBar.open('Analysis complete', 'Close', { duration: 2500 });
      },
      error: (err) => {
        this.isAnalyzing = false;
        const msg =
          err?.error?.message ||
          err?.error?.error ||
          err?.message ||
          'Analysis failed. Check Azure Document Intelligence configuration and try again.';
        this.errorMessage = typeof msg === 'string' ? msg : 'Analysis failed.';
        this.snackBar.open(this.errorMessage, 'Close', { duration: 6000 });
      }
    });
  }

  private loadExistingAnalysis(): void {
    this.isLoading = true;
    this.intelligenceService.getAnalysis(this.data.blobName).subscribe({
      next: (result) => {
        this.applyResult(result);
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  private applyResult(result: DocumentAnalysisResult): void {
    this.analysis = result;
    const kv = result.keyValuePairs ?? {};
    this.keyValueRows = Object.entries(kv).map(([key, value]) => ({ key, value }));
  }
}
