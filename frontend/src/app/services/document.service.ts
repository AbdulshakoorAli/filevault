import { Injectable } from '@angular/core';
import { HttpClient, HttpEvent, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Document, UploadResponse, ShareLinkResponse, EmailResponse, ShareViaEmailRequest } from '../models/document.model';

@Injectable({
  providedIn: 'root'
})
export class DocumentService {
  private apiUrl = 'http://localhost:8080/api/documents';

  constructor(private http: HttpClient) {}

  uploadDocument(file: File): Observable<HttpEvent<UploadResponse>> {
    const formData = new FormData();
    formData.append('file', file);

    const req = new HttpRequest('POST', `${this.apiUrl}/upload`, formData, {
      reportProgress: true
    });

    return this.http.request<UploadResponse>(req);
  }

  getDocuments(): Observable<Document[]> {
    return this.http.get<Document[]>(this.apiUrl);
  }

  getDownloadLink(blobName: string, expiryHours: number = 24): Observable<ShareLinkResponse> {
    return this.http.get<ShareLinkResponse>(
      `${this.apiUrl}/${encodeURIComponent(blobName)}/download-link`,
      { params: { expiryHours: expiryHours.toString() } }
    );
  }

  deleteDocument(blobName: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${encodeURIComponent(blobName)}`);
  }

  shareViaEmail(blobName: string, request: ShareViaEmailRequest): Observable<EmailResponse> {
    return this.http.post<EmailResponse>(
      `${this.apiUrl}/${encodeURIComponent(blobName)}/share-via-email`,
      request
    );
  }
}
