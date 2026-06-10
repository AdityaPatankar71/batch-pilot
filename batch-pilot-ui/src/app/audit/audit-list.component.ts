import { Component, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { ApiService } from '../api.service';
import { AuditEvent } from '../models';

@Component({
  selector: 'bp-audit-list',
  standalone: true,
  imports: [DatePipe, MatTableModule, MatProgressSpinnerModule, MatIconModule, MatCardModule],
  template: `
    <h2>Action audit log</h2>

    @if (loading) {
      <mat-spinner diameter="40"></mat-spinner>
    } @else if (error) {
      <mat-card class="bp-row-gap"><mat-card-content>
        <mat-icon color="warn">error_outline</mat-icon> Failed to load audit log: {{ error }}
      </mat-card-content></mat-card>
    } @else if (events.length === 0) {
      <div class="bp-empty">
        <mat-icon>fact_check</mat-icon>
        <div>No actions recorded yet.</div>
      </div>
    } @else {
      <table mat-table [dataSource]="events" class="mat-elevation-z1">
        <ng-container matColumnDef="time">
          <th mat-header-cell *matHeaderCellDef>When</th>
          <td mat-cell *matCellDef="let e">{{ e.eventTime ? (e.eventTime | date: 'medium') : '—' }}</td>
        </ng-container>
        <ng-container matColumnDef="principal">
          <th mat-header-cell *matHeaderCellDef>Who</th>
          <td mat-cell *matCellDef="let e">{{ e.principal }}</td>
        </ng-container>
        <ng-container matColumnDef="action">
          <th mat-header-cell *matHeaderCellDef>Action</th>
          <td mat-cell *matCellDef="let e">{{ e.action }}</td>
        </ng-container>
        <ng-container matColumnDef="target">
          <th mat-header-cell *matHeaderCellDef>Target</th>
          <td mat-cell *matCellDef="let e">{{ e.target }}</td>
        </ng-container>
        <ng-container matColumnDef="result">
          <th mat-header-cell *matHeaderCellDef>Result</th>
          <td mat-cell *matCellDef="let e">
            <span [class]="e.result === 'SUCCESS' ? 'status-COMPLETED' : 'status-FAILED'">{{ e.result }}</span>
          </td>
        </ng-container>
        <ng-container matColumnDef="message">
          <th mat-header-cell *matHeaderCellDef>Detail</th>
          <td mat-cell *matCellDef="let e" class="bp-muted">{{ e.message }}</td>
        </ng-container>
        <tr mat-header-row *matHeaderRowDef="columns"></tr>
        <tr mat-row *matRowDef="let e; columns: columns"></tr>
      </table>
    }
  `,
})
export class AuditListComponent implements OnInit {
  columns = ['time', 'principal', 'action', 'target', 'result', 'message'];
  events: AuditEvent[] = [];
  loading = true;
  error: string | null = null;

  constructor(private readonly api: ApiService) {}

  ngOnInit(): void {
    this.api.getAudit().subscribe({
      next: (events) => {
        this.events = events;
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.message ?? 'unknown error';
        this.loading = false;
      },
    });
  }
}
