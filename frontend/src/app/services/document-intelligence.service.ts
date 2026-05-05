import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DocumentAnalysisResult } from '../models/document-analysis.model';

@Injectable({
  providedIn: 'root'
})
export class DocumentIntelligenceService {
  private readonly apiUrl = 'http://localhost:8080/api/documents';

  constructor(private http: HttpClient) {}

  analyzeDocument(blobName: string): Observable<DocumentAnalysisResult> {
    return this.http.post<DocumentAnalysisResult>(
      `${this.apiUrl}/${encodeURIComponent(blobName)}/analyze`,
      {}
    );
  }

  getAnalysis(blobName: string): Observable<DocumentAnalysisResult> {
    return this.http.get<DocumentAnalysisResult>(
      `${this.apiUrl}/${encodeURIComponent(blobName)}/analysis`
    );
  }
}
