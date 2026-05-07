import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export interface ConfirmDialogData {
  title: string;
  message: string;
  confirmText: string;
  cancelText: string;
}

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <h2 mat-dialog-title class="confirm-title">
      <mat-icon class="warn-icon">warning_amber</mat-icon>
      {{ data.title }}
    </h2>
    <mat-dialog-content>
      <p class="confirm-msg">{{ data.message }}</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()" class="cancel-btn">{{ data.cancelText }}</button>
      <button mat-raised-button color="warn" (click)="onConfirm()" class="confirm-btn">
        <mat-icon>delete_outline</mat-icon>
        {{ data.confirmText }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .confirm-title {
      display: flex;
      align-items: center;
      gap: 10px;
      font-weight: 600;
      color: var(--text);
    }

    .warn-icon {
      color: var(--danger);
    }

    .confirm-msg {
      margin: 0;
      color: var(--text-muted);
      font-size: 14px;
      line-height: 1.6;
    }

    mat-dialog-content {
      min-width: 320px;
    }

    .cancel-btn {
      color: var(--text-muted) !important;
    }

    .confirm-btn {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      background: var(--danger) !important;
      border-radius: var(--radius-sm) !important;

      &:hover {
        box-shadow: 0 0 20px rgba(239, 68, 68, 0.3);
      }
    }
  `]
})
export class ConfirmDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ConfirmDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ConfirmDialogData
  ) {}

  onCancel(): void {
    this.dialogRef.close(false);
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }
}
