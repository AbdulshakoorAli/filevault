import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DocumentAnalysisResult } from '../models/document-analysis.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DocumentIntelligenceService {
  private readonly apiUrl = environment.documentsApiUrl;

  constructor(private http: HttpClient) {}

  analyzeDocument(blobName: string, jobDescription?: string): Observable<DocumentAnalysisResult> {
    return this.http.post<DocumentAnalysisResult>(
      `${this.apiUrl}/${encodeURIComponent(blobName)}/analyze`,
      { jobDescription: jobDescription ?? '' }
    );
  }

  getAnalysis(blobName: string): Observable<DocumentAnalysisResult> {
    return this.http.get<DocumentAnalysisResult>(
      `${this.apiUrl}/${encodeURIComponent(blobName)}/analysis`
    );
  }
}