import { Component, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiService } from '../api.service';
import { ActionsCapabilities, ExecutionDetail } from '../models';
import { DurationPipe } from '../duration.pipe';
import { ConfirmDialogComponent } from '../shared/confirm-dialog.component';

@Component({
  selector: 'bp-execution-detail',
  standalone: true,
  imports: [
    DatePipe, DurationPipe, MatTableModule, MatProgressSpinnerModule, MatIconModule,
    MatCardModule, MatButtonModule, MatChipsModule, MatExpansionModule,
    MatDialogModule, MatSnackBarModule,
  ],
  template: `
    <button mat-button (click)="back()"><mat-icon>arrow_back</mat-icon> Back</button>

    @if (loading) {
      <mat-spinner diameter="40"></mat-spinner>
    } @else if (error) {
      <mat-card class="bp-row-gap"><mat-card-content>
        <mat-icon color="warn">error_outline</mat-icon> Failed to load execution: {{ error }}
      </mat-card-content></mat-card>
    } @else if (detail) {
      <h2>
        {{ detail.jobName }} — execution #{{ detail.executionId }}
        <span [class]="'status-' + detail.status">&nbsp;{{ detail.status }}</span>
      </h2>

      <div class="bp-row-gap">
        @if (canRestart()) {
          <button mat-raised-button color="primary" [disabled]="busy" (click)="restart()">
            <mat-icon>replay</mat-icon> Restart
          </button>
        }
        @if (canStop()) {
          <button mat-raised-button color="warn" [disabled]="busy" (click)="stop()">
            <mat-icon>stop</mat-icon> Stop
          </button>
        }
      </div>

      <mat-card class="bp-row-gap"><mat-card-content>
        <div><strong>Instance:</strong> {{ detail.instanceId }}</div>
        <div><strong>Exit code:</strong> {{ detail.exitCode || '—' }}</div>
        <div><strong>Started:</strong> {{ detail.startTime ? (detail.startTime | date: 'medium') : '—' }}</div>
        <div><strong>Ended:</strong> {{ detail.endTime ? (detail.endTime | date: 'medium') : '—' }}</div>
        <div><strong>Duration:</strong> {{ detail.durationMs | duration }}</div>
        <div class="bp-row-gap">
          <strong>Parameters:</strong>
          @if (detail.parameters.length === 0) {
            <span class="bp-muted">none</span>
          } @else {
            <mat-chip-set>
              @for (p of detail.parameters; track p.name) {
                <mat-chip>{{ p.name }}={{ p.value }}{{ p.identifying ? ' *' : '' }}</mat-chip>
              }
            </mat-chip-set>
          }
        </div>
      </mat-card-content></mat-card>

      <h3>Steps</h3>
      <table mat-table [dataSource]="detail.steps" class="mat-elevation-z1 bp-row-gap">
        <ng-container matColumnDef="stepName">
          <th mat-header-cell *matHeaderCellDef>Step</th>
          <td mat-cell *matCellDef="let s">{{ s.stepName }}</td>
        </ng-container>
        <ng-container matColumnDef="status">
          <th mat-header-cell *matHeaderCellDef>Status</th>
          <td mat-cell *matCellDef="let s"><span [class]="'status-' + s.status">{{ s.status }}</span></td>
        </ng-container>
        <ng-container matColumnDef="read">
          <th mat-header-cell *matHeaderCellDef>Read</th>
          <td mat-cell *matCellDef="let s">{{ s.readCount }}</td>
        </ng-container>
        <ng-container matColumnDef="write">
          <th mat-header-cell *matHeaderCellDef>Write</th>
          <td mat-cell *matCellDef="let s">{{ s.writeCount }}</td>
        </ng-container>
        <ng-container matColumnDef="skip">
          <th mat-header-cell *matHeaderCellDef>Skip (r/w/p)</th>
          <td mat-cell *matCellDef="let s">{{ s.readSkipCount }}/{{ s.writeSkipCount }}/{{ s.processSkipCount }}</td>
        </ng-container>
        <ng-container matColumnDef="commit">
          <th mat-header-cell *matHeaderCellDef>Commit</th>
          <td mat-cell *matCellDef="let s">{{ s.commitCount }}</td>
        </ng-container>
        <ng-container matColumnDef="exit">
          <th mat-header-cell *matHeaderCellDef>Exit</th>
          <td mat-cell *matCellDef="let s">{{ s.exitCode }}</td>
        </ng-container>
        <tr mat-header-row *matHeaderRowDef="stepColumns"></tr>
        <tr mat-row *matRowDef="let s; columns: stepColumns"></tr>
      </table>

      @if (failurePanels().length > 0) {
        <h3>Failures</h3>
        @for (panel of failurePanels(); track $index) {
          <mat-expansion-panel class="bp-row-gap" [expanded]="$first">
            <mat-expansion-panel-header>
              <mat-panel-title><span class="status-FAILED">{{ panel.label }}</span></mat-panel-title>
              <mat-panel-description>{{ firstLine(panel.trace) }}</mat-panel-description>
            </mat-expansion-panel-header>
            <pre class="bp-stacktrace">{{ panel.trace }}</pre>
          </mat-expansion-panel>
        }
      }
    }
  `,
})
export class ExecutionDetailComponent implements OnInit {
  stepColumns = ['stepName', 'status', 'read', 'write', 'skip', 'commit', 'exit'];
  detail: ExecutionDetail | null = null;
  loading = true;
  busy = false;
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
    this.api.getCapabilities().subscribe({
      next: (caps) => (this.capabilities = caps),
      error: () => undefined,
    });
    this.load();
  }

  private load(): void {
    this.loading = true;
    const id = this.route.snapshot.paramMap.get('executionId') ?? '';
    this.api.getExecution(id).subscribe({
      next: (detail) => {
        this.detail = detail;
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.status === 404 ? 'execution not found' : (err?.message ?? 'unknown error');
        this.loading = false;
      },
    });
  }

  canRestart(): boolean {
    return this.capabilities.restart && ['FAILED', 'STOPPED'].includes(this.detail?.status ?? '');
  }

  canStop(): boolean {
    return this.capabilities.stop && ['STARTED', 'STARTING'].includes(this.detail?.status ?? '');
  }

  restart(): void {
    if (!this.detail) {
      return;
    }
    const execId = this.detail.executionId;
    this.confirm('Restart execution', `Restart execution #${execId}?`, 'Restart', 'primary', () => {
      this.busy = true;
      this.api.restart(execId).subscribe({
        next: (res) => {
          this.busy = false;
          this.snackBar.open(`Restarted as execution #${res.newExecutionId}`, 'OK', { duration: 4000 });
          this.router.navigate(['/executions', res.newExecutionId]).then(() => this.load());
        },
        error: (err) => this.fail(err),
      });
    });
  }

  stop(): void {
    if (!this.detail) {
      return;
    }
    const execId = this.detail.executionId;
    this.confirm('Stop execution', `Request stop of execution #${execId}?`, 'Stop', 'warn', () => {
      this.busy = true;
      this.api.stop(execId).subscribe({
        next: () => {
          this.busy = false;
          this.snackBar.open('Stop requested', 'OK', { duration: 4000 });
          this.load();
        },
        error: (err) => this.fail(err),
      });
    });
  }

  private confirm(
    title: string,
    message: string,
    confirmLabel: string,
    confirmColor: 'primary' | 'warn',
    onConfirm: () => void,
  ): void {
    this.dialog
      .open(ConfirmDialogComponent, { data: { title, message, confirmLabel, confirmColor } })
      .afterClosed()
      .subscribe((ok) => {
        if (ok) {
          onConfirm();
        }
      });
  }

  private fail(err: { error?: { detail?: string }; message?: string }): void {
    this.busy = false;
    const detail = err?.error?.detail ?? err?.message ?? 'action failed';
    this.snackBar.open(detail, 'Dismiss', { duration: 6000 });
  }

  /**
   * Collects failure stack traces for display. Spring Batch only persists the
   * trace in {@code exitDescription} (the live {@code failureExceptions} are
   * transient and empty on JobExplorer reads), so we prefer exitDescription and
   * fall back to any failureExceptions, de-duplicating identical traces.
   */
  failurePanels(): { label: string; trace: string }[] {
    if (!this.detail) {
      return [];
    }
    const panels: { label: string; trace: string }[] = [];
    const seen = new Set<string>();
    const add = (label: string, trace: string | null | undefined) => {
      if (!trace) {
        return;
      }
      if (seen.has(trace)) {
        return;
      }
      seen.add(trace);
      panels.push({ label, trace });
    };

    for (const s of this.detail.steps) {
      for (const t of s.failureExceptions) {
        add(s.stepName, t);
      }
      if (s.status === 'FAILED') {
        add(s.stepName, s.exitDescription);
      }
    }
    for (const t of this.detail.failureExceptions) {
      add('job', t);
    }
    if (this.detail.status === 'FAILED') {
      add('job', this.detail.exitDescription);
    }
    return panels;
  }

  firstLine(trace: string): string {
    return trace.split('\n')[0];
  }

  back(): void {
    if (this.detail?.jobName) {
      this.router.navigate(['/jobs', this.detail.jobName]);
    } else {
      this.router.navigate(['/']);
    }
  }
}
