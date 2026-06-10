import { Component, OnInit } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';

const THEME_KEY = 'bp-theme';

@Component({
  selector: 'bp-root',
  standalone: true,
  imports: [RouterOutlet, MatToolbarModule, MatIconModule, MatButtonModule, MatTooltipModule],
  template: `
    <mat-toolbar color="primary">
      <mat-icon>flight_takeoff</mat-icon>
      <span class="bp-toolbar-title" (click)="go('/')">&nbsp;batch-pilot</span>
      <span class="bp-spacer"></span>
      <button mat-button (click)="go('/')"><mat-icon>list</mat-icon> Jobs</button>
      <button mat-button (click)="go('/audit')"><mat-icon>history</mat-icon> Audit</button>
      <button
        mat-icon-button
        (click)="toggleTheme()"
        [matTooltip]="dark ? 'Switch to light mode' : 'Switch to dark mode'"
        attr.aria-label="{{ dark ? 'Switch to light mode' : 'Switch to dark mode' }}"
      >
        <mat-icon>{{ dark ? 'light_mode' : 'dark_mode' }}</mat-icon>
      </button>
    </mat-toolbar>
    <div class="bp-container">
      <router-outlet></router-outlet>
    </div>
  `,
})
export class AppComponent implements OnInit {
  dark = false;

  constructor(private readonly router: Router) {}

  ngOnInit(): void {
    const stored = localStorage.getItem(THEME_KEY);
    const prefersDark =
      typeof matchMedia === 'function' && matchMedia('(prefers-color-scheme: dark)').matches;
    this.dark = stored ? stored === 'dark' : prefersDark;
    this.applyTheme();
  }

  toggleTheme(): void {
    this.dark = !this.dark;
    localStorage.setItem(THEME_KEY, this.dark ? 'dark' : 'light');
    this.applyTheme();
  }

  private applyTheme(): void {
    document.documentElement.classList.toggle('bp-dark', this.dark);
  }

  go(path: string): void {
    this.router.navigate([path]);
  }
}
