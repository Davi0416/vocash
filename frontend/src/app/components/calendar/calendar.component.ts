import { Component, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ExpenseService } from '../../services/expense.service';
import { SettingsService } from '../../services/settings.service';
import { MONTHS_FULL } from '../../models/expense.model';

export interface CalCell {
  day: number;
  empty: boolean;
  isFechamento: boolean;
  isVencimento: boolean;
  isToday: boolean;
  hasGasto: boolean;
  badge: string;
}

const fmt = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const DOW = ['dom', 'seg', 'ter', 'qua', 'qui', 'sex', 'sáb'];

@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './calendar.component.html',
  styleUrls: ['./calendar.component.scss'],
})
export class CalendarComponent {
  viewYear  = signal(new Date().getFullYear());
  viewMonth = signal(new Date().getMonth());

  dow = DOW;

  title = computed(() =>
    `${MONTHS_FULL[this.viewMonth()]} ${this.viewYear()}`
  );

  invoiceFormatted = computed(() =>
    fmt.format(this.expenseService.invoiceTotal(this.viewYear(), this.viewMonth()))
  );

  invoiceMeta = computed(() => {
    const s = this.settingsService.settings();
    return `fecha dia ${s.fechamento} · vence dia ${s.vencimento}`;
  });

  cells = computed<CalCell[]>(() => {
    const y = this.viewYear(), m = this.viewMonth();
    const { fechamento, vencimento } = this.settingsService.settings();
    const gastoDays = this.expenseService.gastoDays(y, m);
    const today = new Date();
    const isThisMonth = today.getFullYear() === y && today.getMonth() === m;

    const firstDow = new Date(y, m, 1).getDay();
    const daysInMonth = new Date(y, m + 1, 0).getDate();

    const cells: CalCell[] = [];

    // empty leading cells
    for (let i = 0; i < firstDow; i++) {
      cells.push({ day: 0, empty: true, isFechamento: false, isVencimento: false, isToday: false, hasGasto: false, badge: '' });
    }

    for (let day = 1; day <= daysInMonth; day++) {
      const isFechamento = day === fechamento;
      const isVencimento = day === vencimento;
      const isToday = isThisMonth && today.getDate() === day;
      cells.push({
        day,
        empty: false,
        isFechamento,
        isVencimento,
        isToday: isToday && !isFechamento && !isVencimento,
        hasGasto: gastoDays.has(day),
        badge: isFechamento ? 'fecha' : isVencimento ? 'vence' : '',
      });
    }
    return cells;
  });

  constructor(
    private expenseService: ExpenseService,
    public settingsService: SettingsService,
  ) {}

  prev(): void {
    if (this.viewMonth() === 0) {
      this.viewMonth.set(11);
      this.viewYear.update(y => y - 1);
    } else {
      this.viewMonth.update(m => m - 1);
    }
  }

  next(): void {
    if (this.viewMonth() === 11) {
      this.viewMonth.set(0);
      this.viewYear.update(y => y + 1);
    } else {
      this.viewMonth.update(m => m + 1);
    }
  }
}
