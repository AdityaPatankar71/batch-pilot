import { Component } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'bp-root',
  standalone: true,
  imports: [RouterOutlet, MatToolbarModule, MatIconModule],
  template: `
    <mat-toolbar color="primary">
      <mat-icon>flight_takeoff</mat-icon>
      <span class="bp-toolbar-title" (click)="home()">&nbsp;batch-pilot</span>
      <span class="bp-spacer"></span>
      <span class="bp-muted" style="font-size: 13px;">Spring Batch console</span>
    </mat-toolbar>
    <div class="bp-container">
      <router-outlet></router-outlet>
    </div>
  `,
})
export class AppComponent {
  constructor(private readonly router: Router) {}

  home(): void {
    this.router.navigate(['/']);
  }
}
