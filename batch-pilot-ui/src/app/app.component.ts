import { Component } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'bp-root',
  standalone: true,
  imports: [RouterOutlet, MatToolbarModule, MatIconModule, MatButtonModule],
  template: `
    <mat-toolbar color="primary">
      <mat-icon>flight_takeoff</mat-icon>
      <span class="bp-toolbar-title" (click)="go('/')">&nbsp;batch-pilot</span>
      <span class="bp-spacer"></span>
      <button mat-button (click)="go('/')"><mat-icon>list</mat-icon> Jobs</button>
      <button mat-button (click)="go('/audit')"><mat-icon>history</mat-icon> Audit</button>
    </mat-toolbar>
    <div class="bp-container">
      <router-outlet></router-outlet>
    </div>
  `,
})
export class AppComponent {
  constructor(private readonly router: Router) {}

  go(path: string): void {
    this.router.navigate([path]);
  }
}
