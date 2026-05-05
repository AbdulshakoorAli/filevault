import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: '/documents', pathMatch: 'full' },
  { 
    path: 'documents', 
    loadComponent: () => import('./components/document-list/document-list.component')
      .then(m => m.DocumentListComponent) 
  }
];
