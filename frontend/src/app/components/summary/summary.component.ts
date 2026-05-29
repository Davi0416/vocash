import { Component, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ExpenseService } from '../../services/expense.service';
import { SettingsService } from '../../services/settings.service';
import { CATS, CAT_KEYS, CategoryKey } from '../../models/expense.model';

const fmt = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const R = 80;
const C = 2 * Math.PI * R;

export interface DonutSegment {
  cat: CategoryKey;
  label: string;
  color: string;
  value: number;
  pct: number;
  dasharray: string;
  dashoffset: string;
}

@Component({
  selector: 'app-summary',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './summary.component.html',
  styleUrls: ['./summary.component.scss'],
})
export class SummaryComponent {
  showChart = computed(() => this.settingsService.settings().showChart);
  total     = computed(() => this.expenseService.total());

  segments = computed<DonutSegment[]>(() => {
    const total = this.total();
    if (total <= 0) return [];
    const byCat = this.expenseService.byCat();
    const rows = CAT_KEYS
      .filter(k => (byCat[k] ?? 0) > 0)
      .map(k => ({ k, v: byCat[k] ?? 0 }))
      .sort((a, b) => b.v - a.v);

    let offset = 0;
    return rows.map(({ k, v }) => {
      const frac = v / total;
      const len  = frac * C;
      const seg: DonutSegment = {
        cat: k,
        label: CATS[k].label,
        color: CATS[k].color,
        value: v,
        pct: Math.round(frac * 100),
        dasharray: `${len} ${C - len}`,
        dashoffset: String(-offset),
      };
      offset += len;
      return seg;
    });
  });

  trackColor = C;  // expose constant for template

  constructor(
    public expenseService: ExpenseService,
    public settingsService: SettingsService,
  ) {}

  fmt(v: number): string { return fmt.format(v); }
}
