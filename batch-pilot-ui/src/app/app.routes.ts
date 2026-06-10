import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./jobs/jobs-list.component').then((m) => m.JobsListComponent),
  },
  {
    path: 'jobs/:jobName',
    loadComponent: () =>
      import('./executions/executions-list.component').then((m) => m.ExecutionsListComponent),
  },
  {
    path: 'executions/:executionId',
    loadComponent: () =>
      import('./executions/execution-detail.component').then((m) => m.ExecutionDetailComponent),
  },
  { path: '**', redirectTo: '' },
];
