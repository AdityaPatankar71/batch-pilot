import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ActionsCapabilities,
  AuditEvent,
  ExecutionDetail,
  ExecutionSummary,
  JobSummary,
  LaunchParamInput,
} from './models';

/**
 * Talks to the batch-pilot REST API. URLs are relative to the document base
 * href (/batch-pilot/), so requests land on /batch-pilot/api/**. CSRF tokens
 * are attached automatically by Angular's XSRF interceptor (XSRF-TOKEN cookie
 * -> X-XSRF-TOKEN header), matching the server's CookieCsrfTokenRepository.
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

  // --- M1 actions ----------------------------------------------------------

  getCapabilities(): Observable<ActionsCapabilities> {
    return this.http.get<ActionsCapabilities>('api/actions');
  }

  restart(executionId: number): Observable<{ newExecutionId: number }> {
    return this.http.post<{ newExecutionId: number }>(`api/executions/${executionId}/restart`, {});
  }

  stop(executionId: number): Observable<{ stopRequested: boolean }> {
    return this.http.post<{ stopRequested: boolean }>(`api/executions/${executionId}/stop`, {});
  }

  launch(jobName: string, parameters: LaunchParamInput[]): Observable<{ executionId: number }> {
    return this.http.post<{ executionId: number }>(
      `api/jobs/${encodeURIComponent(jobName)}/launch`,
      { parameters },
    );
  }

  getAudit(limit = 100): Observable<AuditEvent[]> {
    return this.http.get<AuditEvent[]>(`api/audit?limit=${limit}`);
  }
}
