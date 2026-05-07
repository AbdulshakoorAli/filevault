import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Clipboard, ClipboardModule } from '@angular/cdk/clipboard';
import { DocumentService } from '../../services/document.service';

export interface ShareLinkDialogData {
  blobName: string;
  fileName: string;
  downloadUrl: string;
  expiresAt: string;
}

@Component({
  selector: 'app-share-link-dialog',
  standalone: true,
  imports: [
    CommonModule, FormsModule,
    MatDialogModule, MatButtonModule, MatIconModule,
    MatFormFieldModule, MatInputModule, MatSnackBarModule,
    MatTabsModule, MatProgressSpinnerModule, MatTooltipModule,
    ClipboardModule
  ],
  template: `
    <h2 mat-dialog-title class="share-title">
      <mat-icon class="title-icon">share</mat-icon>
      Share "{{ data.fileName }}"
    </h2>

    <mat-dialog-content>
      <mat-tab-group animationDuration="300ms">
        <!-- Copy Link tab -->
        <mat-tab>
          <ng-template mat-tab-label>
            <mat-icon>link</mat-icon>&nbsp;Copy Link
          </ng-template>
          <div class="tab-body">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Download Link</mat-label>
              <input matInput [value]="data.downloadUrl" readonly>
              <button mat-icon-button matSuffix (click)="copyLink()" matTooltip="Copy">
                <mat-icon>content_copy</mat-icon>
              </button>
            </mat-form-field>

            <p class="expiry-row">
              <mat-icon>schedule</mat-icon>
              Expires: {{ formatDate(data.expiresAt) }}
            </p>

            <div class="actions-right">
              <button mat-raised-button color="primary" (click)="copyLink()">
                <mat-icon>content_copy</mat-icon>
                Copy Link
              </button>
            </div>
          </div>
        </mat-tab>

        <!-- Email tab -->
        <mat-tab>
          <ng-template mat-tab-label>
            <mat-icon>email</mat-icon>&nbsp;Email
          </ng-template>
          <div class="tab-body">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Recipient Email</mat-label>
              <mat-icon matPrefix>email</mat-icon>
              <input matInput type="email" [(ngModel)]="recipientEmail"
                     placeholder="user&#64;example.com" [disabled]="isSending">
            </mat-form-field>

            <p class="info-row">
              <mat-icon>info_outline</mat-icon>
              Recipient gets a secure download link valid for 24 hours.
            </p>

            <div class="actions-right">
              <button mat-raised-button color="primary"
                      (click)="sendEmail()"
                      [disabled]="!isValidEmail() || isSending">
                <mat-spinner *ngIf="isSending" diameter="18"></mat-spinner>
                <mat-icon *ngIf="!isSending">send</mat-icon>
                {{ isSending ? 'Sending…' : 'Send Email' }}
              </button>
            </div>

            <div *ngIf="emailSent" class="success-banner">
              <mat-icon>check_circle</mat-icon>
              Email sent successfully!
            </div>
          </div>
        </mat-tab>
      </mat-tab-group>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button (click)="close()" class="close-btn">Close</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .share-title {
      display: flex;
      align-items: center;
      gap: 10px;
      font-weight: 600;
      color: var(--text);
    }

    .title-icon { color: var(--accent-light); }

    .tab-body { padding: 20px 0 4px; }
    .full-width { width: 100%; }

    .expiry-row, .info-row {
      display: flex;
      align-items: flex-start;
      gap: 8px;
      color: var(--text-muted);
      font-size: 13px;
      margin: 4px 0 12px;

      mat-icon {
        font-size: 16px;
        width: 16px;
        height: 16px;
        flex-shrink: 0;
        margin-top: 2px;
      }
    }

    .actions-right {
      display: flex;
      justify-content: flex-end;
      margin-top: 8px;

      button {
        display: inline-flex;
        align-items: center;
        gap: 8px;
      }

      mat-spinner { margin-right: 4px; }
    }

    .success-banner {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-top: 16px;
      padding: 12px 16px;
      border-radius: var(--radius-sm);
      background: rgba(16, 185, 129, 0.12);
      border: 1px solid rgba(16, 185, 129, 0.25);
      color: var(--success);
      font-weight: 500;
      animation: fadeInUp 0.3s ease both;

      mat-icon { color: var(--success); }
    }

    .close-btn { color: var(--text-muted) !important; }

    mat-dialog-content { min-width: min(460px, 92vw); }

    ::ng-deep .mat-mdc-tab-body-wrapper { padding-top: 4px; }
  `]
})
export class ShareLinkDialogComponent {
  recipientEmail = '';
  isSending = false;
  emailSent = false;

  constructor(
    public dialogRef: MatDialogRef<ShareLinkDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ShareLinkDialogData,
    private clipboard: Clipboard,
    private snackBar: MatSnackBar,
    private documentService: DocumentService
  ) {}

  copyLink(): void {
    this.clipboard.copy(this.data.downloadUrl);
    this.snackBar.open('Link copied!', 'Close', { duration: 2000 });
  }

  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleString();
  }

  isValidEmail(): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.recipientEmail);
  }

  sendEmail(): void {
    if (!this.isValidEmail()) {
      this.snackBar.open('Enter a valid email', 'Close', { duration: 3000 });
      return;
    }
    this.isSending = true;
    this.emailSent = false;

    this.documentService.shareViaEmail(this.data.blobName, {
      recipientEmail: this.recipientEmail,
      expiryHours: 24
    }).subscribe({
      next: () => {
        this.isSending = false;
        this.emailSent = true;
        this.snackBar.open(`Email sent to ${this.recipientEmail}`, 'Close', { duration: 3000 });
      },
      error: (error) => {
        this.isSending = false;
        console.error('Email error:', error);
        this.snackBar.open('Failed to send email', 'Close', { duration: 5000 });
      }
    });
  }

  close(): void {
    this.dialogRef.close();
  }
}
