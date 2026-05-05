import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DocumentService } from '../../services/document.service';
import { Document } from '../../models/document.model';
import { DocumentUploadComponent } from '../document-upload/document-upload.component';
import { ShareLinkDialogComponent, ShareLinkDialogData } from '../share-link-dialog/share-link-dialog.component';
import { ConfirmDialogComponent, ConfirmDialogData } from '../confirm-dialog/confirm-dialog.component';
import {
  DocumentAnalysisDialogComponent,
  DocumentAnalysisDialogData
} from '../document-analysis-dialog/document-analysis-dialog.component';

@Component({
  selector: 'app-document-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDialogModule,
    MatTooltipModule,
    DocumentUploadComponent
  ],
  templateUrl: './document-list.component.html',
  styleUrls: ['./document-list.component.scss']
})
export class DocumentListComponent implements OnInit {
  documents: Document[] = [];
  displayedColumns = ['fileName', 'contentType', 'fileSize', 'uploadedAt', 'actions'];
  isLoading = true;

  constructor(
    private documentService: DocumentService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadDocuments();
  }

  loadDocuments(): void {
    this.isLoading = true;
    this.documentService.getDocuments().subscribe({
      next: (docs) => {
        this.documents = docs;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading documents:', error);
        this.isLoading = false;
        this.snackBar.open('Failed to load documents', 'Close', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  onDocumentUploaded(document: Document): void {
    this.documents = [document, ...this.documents];
  }

  openDocumentAnalysis(doc: Document): void {
    this.dialog.open(DocumentAnalysisDialogComponent, {
      width: '640px',
      maxWidth: '95vw',
      data: {
        blobName: doc.blobName,
        fileName: doc.fileName
      } as DocumentAnalysisDialogData
    });
  }

  getShareLink(doc: Document): void {
    this.documentService.getDownloadLink(doc.blobName).subscribe({
      next: (response) => {
        this.dialog.open(ShareLinkDialogComponent, {
          data: {
            blobName: doc.blobName,
            fileName: doc.fileName,
            downloadUrl: response.downloadUrl,
            expiresAt: response.expiresAt
          } as ShareLinkDialogData
        });
      },
      error: (error) => {
        console.error('Error getting share link:', error);
        this.snackBar.open('Failed to generate share link', 'Close', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  confirmDelete(doc: Document): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Delete Document',
        message: `Are you sure you want to delete "${doc.fileName}"?`,
        confirmText: 'Delete',
        cancelText: 'Cancel'
      } as ConfirmDialogData
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.deleteDocument(doc);
      }
    });
  }

  private deleteDocument(doc: Document): void {
    this.documentService.deleteDocument(doc.blobName).subscribe({
      next: () => {
        this.documents = this.documents.filter(d => d.blobName !== doc.blobName);
        this.snackBar.open('Document deleted successfully', 'Close', {
          duration: 3000,
          panelClass: ['success-snackbar']
        });
      },
      error: (error) => {
        console.error('Error deleting document:', error);
        this.snackBar.open('Failed to delete document', 'Close', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getFileIcon(contentType: string): string {
    if (contentType?.includes('pdf')) return 'picture_as_pdf';
    if (contentType?.includes('image')) return 'image';
    if (contentType?.includes('word') || contentType?.includes('document')) return 'description';
    if (contentType?.includes('excel') || contentType?.includes('spreadsheet')) return 'table_chart';
    if (contentType?.includes('powerpoint') || contentType?.includes('presentation')) return 'slideshow';
    return 'insert_drive_file';
  }
}
