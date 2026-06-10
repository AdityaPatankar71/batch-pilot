import { Component, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { ApiService } from '../api.service';
import { JobSummary } from '../models';
import { DurationPipe } from '../duration.pipe';

@Component({
  selector: 'bp-jobs-list',
  standalone: true,
  imports: [DatePipe, DurationPipe, MatTableModule, MatProgressSpinnerModule, MatIconModule, MatCardModule],
  template: `
    <h2>Jobs</h2>

    @if (loading) {
      <mat-spinner diameter="40"></mat-spinner>
    } @else if (error) {
      <mat-card class="bp-row-gap"><mat-card-content>
        <mat-icon color="warn">error_outline</mat-icon> Failed to load jobs: {{ error }}
      </mat-card-content></mat-card>
    } @else if (jobs.length === 0) {
      <mat-card><mat-card-content class="bp-muted">No registered jobs found.</mat-card-content></mat-card>
    } @else {
      <table mat-table [dataSource]="jobs" class="mat-elevation-z1">
        <ng-container matColumnDef="name">
          <th mat-header-cell *matHeaderCellDef>Job</th>
          <td mat-cell *matCellDef="let job">{{ job.name }}</td>
        </ng-container>

        <ng-container matColumnDef="status">
          <th mat-header-cell *matHeaderCellDef>Last status</th>
          <td mat-cell *matCellDef="let job">
            @if (job.lastExecution) {
              <span [class]="'status-' + job.lastExecution.status">{{ job.lastExecution.status }}</span>
            } @else {
              <span class="bp-muted">never run</span>
            }
          </td>
        </ng-container>

        <ng-container matColumnDef="lastRun">
          <th mat-header-cell *matHeaderCellDef>Last run</th>
          <td mat-cell *matCellDef="let job">
            {{ job.lastExecution?.startTime ? (job.lastExecution.startTime | date: 'medium') : '—' }}
          </td>
        </ng-container>

        <ng-container matColumnDef="duration">
          <th mat-header-cell *matHeaderCellDef>Duration</th>
          <td mat-cell *matCellDef="let job">{{ job.lastExecution?.durationMs | duration }}</td>
        </ng-container>

        <ng-container matColumnDef="instances">
          <th mat-header-cell *matHeaderCellDef>Instances</th>
          <td mat-cell *matCellDef="let job">{{ job.instanceCount }}</td>
        </ng-container>

        <tr mat-header-row *matHeaderRowDef="columns"></tr>
        <tr mat-row *matRowDef="let job; columns: columns" class="bp-clickable" (click)="open(job)"></tr>
      </table>
    }
  `,
})
export class JobsListComponent implements OnInit {
  columns = ['name', 'status', 'lastRun', 'duration', 'instances'];
  jobs: JobSummary[] = [];
  loading = true;
  error: string | null = null;

  constructor(private readonly api: ApiService, private readonly router: Router) {}

  ngOnInit(): void {
    this.api.listJobs().subscribe({
      next: (jobs) => {
        this.jobs = jobs;
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.message ?? 'unknown error';
        this.loading = false;
      },
    });
  }

  open(job: JobSummary): void {
    this.router.navigate(['/jobs', job.name]);
  }
}
