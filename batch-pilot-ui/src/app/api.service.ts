import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ExecutionDetail, ExecutionSummary, JobSummary } from './models';

/**
 * Talks to the batch-pilot REST API. URLs are relative to the document base
 * href (/batch-pilot/), so requests land on /batch-pilot/api/**.
 */
@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(private readonly http: HttpClient) {}

  listJobs(): Observable<JobSummary[]> {
    return this.http.get<JobSummary[]>('api/jobs');
  }

  listExecutions(jobName: string): Observable<ExecutionSummary[]> {
    return this.http.get<ExecutionSummary[]>(`api/jobs/${encodeURIComponent(jobName)}/executions`);
  }

  getExecution(executionId: number | string): Observable<ExecutionDetail> {
    return this.http.get<ExecutionDetail>(`api/executions/${executionId}`);
  }
}
