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
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSnackBarModule,
    MatTabsModule,
    MatProgressSpinnerModule,
    ClipboardModule
  ],
  template: `
    <h2 mat-dialog-title>Share "{{ data.fileName }}"</h2>
    <mat-dialog-content>
      <mat-tab-group>
        <mat-tab label="Copy Link">
          <div class="tab-content">
            <mat-form-field appearance="outline" class="link-field">
              <mat-label>Download Link</mat-label>
              <input matInput [value]="data.downloadUrl" readonly>
              <button mat-icon-button matSuffix (click)="copyLink()" matTooltip="Copy link">
                <mat-icon>content_copy</mat-icon>
              </button>
            </mat-form-field>

            <p class="expiry-info">
              <mat-icon>schedule</mat-icon>
              <span>Link expires: {{ formatDate(data.expiresAt) }}</span>
            </p>

            <div class="actions">
              <button mat-raised-button color="primary" (click)="copyLink()">
                <mat-icon>content_copy</mat-icon>
                Copy Link
              </button>
            </div>
          </div>
        </mat-tab>

        <mat-tab label="Send via Email">
          <div class="tab-content">
            <mat-form-field appearance="outline" class="email-field">
              <mat-label>Recipient Email</mat-label>
              <input matInput 
                     type="email" 
                     [(ngModel)]="recipientEmail" 
                     placeholder="Enter email address"
                     [disabled]="isSending">
              <mat-icon matPrefix>email</mat-icon>
            </mat-form-field>

            <p class="info-text">
              <mat-icon>info</mat-icon>
              <span>The recipient will receive an email with a secure download link that expires in 24 hours.</span>
            </p>

            <div class="actions">
              <button mat-raised-button 
                      color="primary" 
                      (click)="sendEmail()"
                      [disabled]="!isValidEmail() || isSending">
                <mat-spinner *ngIf="isSending" diameter="20"></mat-spinner>
                <mat-icon *ngIf="!isSending">send</mat-icon>
                {{ isSending ? 'Sending...' : 'Send Email' }}
              </button>
            </div>

            <div *ngIf="emailSent" class="success-message">
              <mat-icon>check_circle</mat-icon>
              <span>Email sent successfully!</span>
            </div>
          </div>
        </mat-tab>
      </mat-tab-group>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="close()">Close</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .tab-content {
      padding: 20px 0;
    }

    .link-field, .email-field {
      width: 100%;
    }

    .expiry-info, .info-text {
      display: flex;
      align-items: flex-start;
      gap: 8px;
      color: #666;
      margin-top: 8px;
      font-size: 14px;

      mat-icon {
        font-size: 18px;
        width: 18px;
        height: 18px;
        flex-shrink: 0;
      }
    }

    .actions {
      margin-top: 16px;
      display: flex;
      justify-content: flex-end;

      button {
        display: flex;
        align-items: center;
        gap: 8px;
      }

      mat-spinner {
        margin-right: 4px;
      }
    }

    .success-message {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #4caf50;
      margin-top: 16px;
      padding: 12px;
      background: #e8f5e9;
      border-radius: 4px;

      mat-icon {
        color: #4caf50;
      }
    }

    mat-dialog-content {
      min-width: 450px;
    }

    ::ng-deep .mat-mdc-tab-body-wrapper {
      padding-top: 8px;
    }
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
    this.snackBar.open('Link copied to clipboard!', 'Close', {
      duration: 2000
    });
  }

  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleString();
  }

  isValidEmail(): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(this.recipientEmail);
  }

  sendEmail(): void {
    if (!this.isValidEmail()) {
      this.snackBar.open('Please enter a valid email address', 'Close', {
        duration: 3000
      });
      return;
    }

    this.isSending = true;
    this.emailSent = false;

    this.documentService.shareViaEmail(this.data.blobName, {
      recipientEmail: this.recipientEmail,
      expiryHours: 24
    }).subscribe({
      next: (response) => {
        this.isSending = false;
        this.emailSent = true;
        this.snackBar.open(`Email sent to ${this.recipientEmail}`, 'Close', {
          duration: 3000
        });
      },
      error: (error) => {
        this.isSending = false;
        console.error('Error sending email:', error);
        this.snackBar.open('Failed to send email. Please try again.', 'Close', {
          duration: 5000
        });
      }
    });
  }

  close(): void {
    this.dialogRef.close();
  }
}
