import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../services/toast.service';
import { ExpenseService } from '../../services/expense.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast" [class.show]="toastService.state().visible">
      <span>{{ toastService.state().message }}</span>
      <button (click)="undo()">Desfazer</button>
    </div>
  `,
  styles: [`
    .toast {
      position: fixed; left: 50%; bottom: 24px;
      transform: translateX(-50%) translateY(140%);
      background: var(--surface-3); border: 1px solid var(--border);
      border-radius: 13px; padding: 12px 14px 12px 18px;
      display: flex; align-items: center; gap: 16px;
      box-shadow: var(--shadow); z-index: 60;
      transition: transform .35s cubic-bezier(.2,.8,.2,1);
      font-size: 14px;
    }
    .toast.show { transform: translateX(-50%) translateY(0); }
    button { color: var(--accent); font-weight: 700; font-size: 13.5px; }
  `],
})
export class ToastComponent {
  constructor(
    public toastService: ToastService,
    private expenseService: ExpenseService,
  ) {}

  undo(): void {
    this.expenseService.undo();
    this.toastService.hide();
  }
}
