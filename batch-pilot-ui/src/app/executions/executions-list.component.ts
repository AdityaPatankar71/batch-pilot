import { Component, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { ApiService } from '../api.service';
import { ExecutionSummary } from '../models';
import { DurationPipe } from '../duration.pipe';

@Component({
  selector: 'bp-executions-list',
  standalone: true,
  imports: [
    DatePipe, DurationPipe, MatTableModule, MatProgressSpinnerModule,
    MatIconModule, MatCardModule, MatButtonModule, MatChipsModule,
  ],
  template: `
    <button mat-button (click)="back()"><mat-icon>arrow_back</mat-icon> Jobs</button>
    <h2>Executions — {{ jobName }}</h2>

    @if (loading) {
      <mat-spinner diameter="40"></mat-spinner>
    } @else if (error) {
      <mat-card class="bp-row-gap"><mat-card-content>
        <mat-icon color="warn">error_outline</mat-icon> Failed to load executions: {{ error }}
      </mat-card-content></mat-card>
    } @else if (executions.length === 0) {
      <mat-card><mat-card-content class="bp-muted">No executions for this job yet.</mat-card-content></mat-card>
    } @else {
      <table mat-table [dataSource]="executions" class="mat-elevation-z1">
        <ng-container matColumnDef="id">
          <th mat-header-cell *matHeaderCellDef>Execution</th>
          <td mat-cell *matCellDef="let e">#{{ e.executionId }}</td>
        </ng-container>

        <ng-container matColumnDef="status">
          <th mat-header-cell *matHeaderCellDef>Status</th>
          <td mat-cell *matCellDef="let e"><span [class]="'status-' + e.status">{{ e.status }}</span></td>
        </ng-container>

        <ng-container matColumnDef="start">
          <th mat-header-cell *matHeaderCellDef>Started</th>
          <td mat-cell *matCellDef="let e">{{ e.startTime ? (e.startTime | date: 'medium') : '—' }}</td>
        </ng-container>

        <ng-container matColumnDef="duration">
          <th mat-header-cell *matHeaderCellDef>Duration</th>
          <td mat-cell *matCellDef="let e">{{ e.durationMs | duration }}</td>
        </ng-container>

        <ng-container matColumnDef="params">
          <th mat-header-cell *matHeaderCellDef>Parameters</th>
          <td mat-cell *matCellDef="let e">
            @if (e.parameters.length === 0) {
              <span class="bp-muted">—</span>
            } @else {
              <mat-chip-set>
                @for (p of e.parameters; track p.name) {
                  <mat-chip>{{ p.name }}={{ p.value }}</mat-chip>
                }
              </mat-chip-set>
            }
          </td>
        </ng-container>

        <tr mat-header-row *matHeaderRowDef="columns"></tr>
        <tr mat-row *matRowDef="let e; columns: columns" class="bp-clickable" (click)="open(e)"></tr>
      </table>
    }
  `,
})
export class ExecutionsListComponent implements OnInit {
  columns = ['id', 'status', 'start', 'duration', 'params'];
  jobName = '';
  executions: ExecutionSummary[] = [];
  loading = true;
  error: string | null = null;

  constructor(
    private readonly api: ApiService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.jobName = this.route.snapshot.paramMap.get('jobName') ?? '';
    this.api.listExecutions(this.jobName).subscribe({
      next: (executions) => {
        this.executions = executions;
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.message ?? 'unknown error';
        this.loading = false;
      },
    });
  }

  open(e: ExecutionSummary): void {
    this.router.navigate(['/executions', e.executionId]);
  }

  back(): void {
    this.router.navigate(['/']);
  }
}
