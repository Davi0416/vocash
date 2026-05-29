import { Injectable, signal } from '@angular/core';

export interface ToastState {
  message: string;
  visible: boolean;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  readonly state = signal<ToastState>({ message: '', visible: false });
  private timer: ReturnType<typeof setTimeout> | null = null;

  show(message: string): void {
    if (this.timer) clearTimeout(this.timer);
    this.state.set({ message, visible: true });
    this.timer = setTimeout(() => this.hide(), 4200);
  }

  hide(): void {
    this.state.update(s => ({ ...s, visible: false }));
    this.timer = null;
  }
}
