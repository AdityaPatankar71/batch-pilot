import { Component, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiService } from '../api.service';
import { ActionsCapabilities, ExecutionSummary } from '../models';
import { DurationPipe } from '../duration.pipe';
import { LaunchDialogComponent } from '../shared/launch-dialog.component';

@Component({
  selector: 'bp-executions-list',
  standalone: true,
  imports: [
    DatePipe, DurationPipe, MatTableModule, MatProgressSpinnerModule,
    MatIconModule, MatCardModule, MatButtonModule, MatChipsModule,
    MatDialogModule, MatSnackBarModule,
  ],
  template: `
    <button mat-button (click)="back()"><mat-icon>arrow_back</mat-icon> Jobs</button>
    <h2>
      Executions — {{ jobName }}
      @if (capabilities.launch) {
        <button mat-raised-button color="primary" style="margin-left: 12px;" (click)="launch()">
          <mat-icon>play_arrow</mat-icon> Launch
        </button>
      }
    </h2>

    @if (loading) {
      <mat-spinner diameter="40"></mat-spinner>
    } @else if (error) {
      <mat-card class="bp-row-gap"><mat-card-content>
        <mat-icon color="warn">error_outline</mat-icon> Failed to load executions: {{ error }}
      </mat-card-content></mat-card>
    } @else if (executions.length === 0) {
      <div class="bp-empty">
        <mat-icon>history</mat-icon>
        <div>No executions for this job yet.</div>
      </div>
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
  capabilities: ActionsCapabilities = { restart: false, stop: false, launch: false };

  constructor(
    private readonly api: ApiService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.jobName = this.route.snapshot.paramMap.get('jobName') ?? '';
    this.api.getCapabilities().subscribe({
      next: (caps) => (this.capabilities = caps),
      error: () => undefined,
    });
    this.load();
  }

  private load(): void {
    this.loading = true;
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

  launch(): void {
    this.dialog
      .open(LaunchDialogComponent, { data: { jobName: this.jobName }, width: '720px' })
      .afterClosed()
      .subscribe((params) => {
        if (!params) {
          return;
        }
        this.api.launch(this.jobName, params).subscribe({
          next: (res) => {
            this.snackBar.open(`Launched as execution #${res.executionId}`, 'OK', { duration: 4000 });
            this.load();
          },
          error: (err) => {
            const detail = err?.error?.detail ?? err?.message ?? 'launch failed';
            this.snackBar.open(detail, 'Dismiss', { duration: 6000 });
          },
        });
      });
  }

  open(e: ExecutionSummary): void {
    this.router.navigate(['/executions', e.executionId]);
  }

  back(): void {
    this.router.navigate(['/']);
  }
}
