export interface ExtractedTable {
  rowCount: number;
  columnCount: number;
  cells: string[];
}

export interface DocumentAnalysisResult {
  blobName: string;
  fileName: string;
  extractedText: string;
  keyValuePairs: Record<string, string>;
  extractedTables: ExtractedTable[];
  pageCount: number;
  analyzedAt: string;
}
