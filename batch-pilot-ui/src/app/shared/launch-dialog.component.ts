import { Component, Inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { LaunchParamInput, ParamType } from '../models';

export interface LaunchDialogData {
  jobName: string;
}

@Component({
  selector: 'bp-launch-dialog',
  standalone: true,
  imports: [
    FormsModule, MatDialogModule, MatButtonModule, MatIconModule,
    MatFormFieldModule, MatInputModule, MatSelectModule, MatCheckboxModule,
  ],
  template: `
    <h2 mat-dialog-title>Launch {{ data.jobName }}</h2>
    <mat-dialog-content>
      <p class="bp-muted">Add typed parameters. Identifying parameters define the job instance.</p>
      @for (p of params; track $index) {
        <div class="bp-param-row">
          <mat-form-field appearance="outline" class="bp-name">
            <mat-label>Name</mat-label>
            <input matInput [(ngModel)]="p.name" [name]="'name' + $index" />
          </mat-form-field>
          <mat-form-field appearance="outline" class="bp-type">
            <mat-label>Type</mat-label>
            <mat-select [(ngModel)]="p.type" [name]="'type' + $index">
              @for (t of types; track t) {
                <mat-option [value]="t">{{ t }}</mat-option>
              }
            </mat-select>
          </mat-form-field>
          <mat-form-field appearance="outline" class="bp-value">
            <mat-label>Value</mat-label>
            <input matInput [(ngModel)]="p.value" [name]="'value' + $index"
                   [placeholder]="p.type === 'DATE' ? 'yyyy-MM-dd' : ''" />
          </mat-form-field>
          <mat-checkbox [(ngModel)]="p.identifying" [name]="'id' + $index">ID</mat-checkbox>
          <button mat-icon-button color="warn" (click)="remove($index)" aria-label="Remove">
            <mat-icon>delete</mat-icon>
          </button>
        </div>
      }
      <button mat-stroked-button (click)="add()"><mat-icon>add</mat-icon> Add parameter</button>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button [mat-dialog-close]="null">Cancel</button>
      <button mat-raised-button color="primary" (click)="launch()">Launch</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .bp-param-row { display: flex; gap: 8px; align-items: center; }
    .bp-name { width: 180px; }
    .bp-type { width: 120px; }
    .bp-value { flex: 1 1 auto; min-width: 160px; }
  `],
})
export class LaunchDialogComponent {
  readonly types: ParamType[] = ['STRING', 'LONG', 'DOUBLE', 'DATE'];
  params: LaunchParamInput[] = [{ name: '', value: '', type: 'STRING', identifying: true }];

  constructor(
    public dialogRef: MatDialogRef<LaunchDialogComponent, LaunchParamInput[] | null>,
    @Inject(MAT_DIALOG_DATA) public data: LaunchDialogData,
  ) {}

  add(): void {
    this.params.push({ name: '', value: '', type: 'STRING', identifying: false });
  }

  remove(index: number): void {
    this.params.splice(index, 1);
  }

  launch(): void {
    const cleaned = this.params.filter((p) => p.name.trim().length > 0);
    this.dialogRef.close(cleaned);
  }
}
