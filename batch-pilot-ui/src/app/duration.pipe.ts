import { Pipe, PipeTransform } from '@angular/core';

/** Formats a millisecond duration as a compact human string (e.g. 1.5s, 2m 03s). */
@Pipe({ name: 'duration', standalone: true })
export class DurationPipe implements PipeTransform {
  transform(ms: number | null | undefined): string {
    if (ms === null || ms === undefined) {
      return '—';
    }
    if (ms < 1000) {
      return `${ms}ms`;
    }
    const totalSeconds = ms / 1000;
    if (totalSeconds < 60) {
      return `${totalSeconds.toFixed(totalSeconds < 10 ? 1 : 0)}s`;
    }
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = Math.round(totalSeconds % 60);
    return `${minutes}m ${seconds.toString().padStart(2, '0')}s`;
  }
}
