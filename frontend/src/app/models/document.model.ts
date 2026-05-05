export interface Document {
  blobName: string;
  fileName: string;
  contentType: string;
  fileSize: number;
  uploadedAt: string;
}

export interface UploadResponse {
  message: string;
  document: Document;
}

export interface ShareLinkResponse {
  downloadUrl: string;
  expiresAt: string;
}

export interface EmailResponse {
  messageId: string;
  status: string;
  sentAt: string;
}

export interface ShareViaEmailRequest {
  recipientEmail: string;
  expiryHours: number;
}
