import { Injectable, signal, computed } from '@angular/core';
import { ApiService, BackendGasto, GastoRequest } from './api.service';
import { Expense, CategoryKey, CAT_KEYS } from '../models/expense.model';
import { SettingsService } from './settings.service';

function toExpense(g: BackendGasto): Expense {
  return {
    id: String(g.id),
    name: g.descricao || g.categoria,
    value: g.valor,
    cat: (g.categoria as CategoryKey) || 'outros',
    installments: g.parcelas || 1,
    date: g.data,
  };
}

function isoLocal(d: Date): string {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

export function todayISOExport(): string {
  return isoLocal(new Date());
}

export function parseDate(iso: string): Date {
  const d = iso ? new Date(iso + 'T00:00:00') : new Date();
  return isNaN(d.getTime()) ? new Date() : d;
}

export function shortDate(iso: string): string {
  const MONTHS_SHORT = ['jan','fev','mar','abr','mai','jun','jul','ago','set','out','nov','dez'];
  const d = parseDate(iso);
  return `${d.getDate()} ${MONTHS_SHORT[d.getMonth()]}`;
}

@Injectable({ providedIn: 'root' })
export class ExpenseService {
  readonly expenses  = signal<Expense[]>([]);
  readonly loading   = signal(false);

  readonly total = computed(() =>
    this.expenses().reduce((sum, e) => sum + (e.value || 0), 0)
  );

  readonly byCat = computed(() => {
    const map: Partial<Record<CategoryKey, number>> = {};
    this.expenses().forEach(e => {
      map[e.cat] = (map[e.cat] || 0) + (e.value || 0);
    });
    return map;
  });

  private lastDeletedId: number | null = null;
  private lastDeletedExpense: Expense | null = null;

  constructor(
    private api: ApiService,
    private settingsService: SettingsService,
  ) {
    this.refresh();
  }

  /** Recarrega a lista do backend. */
  refresh(): void {
    this.loading.set(true);
    this.api.getGastos().subscribe({
      next: gastos => {
        this.expenses.set(gastos.map(toExpense));
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  /** Cria um gasto manualmente e atualiza a lista. */
  add(partial: Omit<Expense, 'id'>): void {
    const req: GastoRequest = {
      valor:     partial.value,
      categoria: partial.cat,
      descricao: partial.name,
      local:     '',
      data:      partial.date,
      parcelas:  partial.installments,
    };
    this.api.criarGasto(req).subscribe(() => this.refresh());
  }

  /** Atualiza um gasto e recarrega. */
  update(e: Expense): void {
    const req: GastoRequest = {
      valor:     e.value,
      categoria: e.cat,
      descricao: e.name,
      local:     '',
      data:      e.date,
      parcelas:  e.installments,
    };
    this.api.atualizarGasto(Number(e.id), req).subscribe(() => this.refresh());
  }

  /** Remove um gasto do backend e atualiza a lista. */
  remove(id: string): void {
    const numId = Number(id);
    this.lastDeletedId = numId;
    this.lastDeletedExpense = this.expenses().find(e => e.id === id) ?? null;
    this.expenses.update(list => list.filter(e => e.id !== id));
    this.api.deletarGasto(numId).subscribe();
  }

  /** Desfaz o último delete recriando o gasto no backend. */
  undo(): void {
    if (!this.lastDeletedExpense) return;
    const e = this.lastDeletedExpense;
    const req: GastoRequest = {
      valor: e.value, categoria: e.cat, descricao: e.name,
      local: '', data: e.date, parcelas: e.installments,
    };
    this.api.criarGasto(req).subscribe(() => this.refresh());
    this.lastDeletedId = null;
    this.lastDeletedExpense = null;
  }

  hasUndo(): boolean { return this.lastDeletedExpense !== null; }
  clearUndo(): void  { this.lastDeletedId = null; this.lastDeletedExpense = null; }

  /** Fatura do mês baseada nas parcelas. */
  invoiceTotal(year: number, month: number): number {
    const idx = year * 12 + month;
    const fechamento = this.settingsService.settings().fechamento;
    let sum = 0;
    this.expenses().forEach(e => {
      const d = parseDate(e.date);
      let y = d.getFullYear(), m = d.getMonth();
      if (d.getDate() > fechamento) { m++; if (m > 11) { m = 0; y++; } }
      const base = y * 12 + m;
      const k = idx - base;
      const n = e.installments || 1;
      if (k >= 0 && k < n) sum += (e.value || 0) / n;
    });
    return sum;
  }

  gastoDays(year: number, month: number): Set<number> {
    const set = new Set<number>();
    this.expenses().forEach(e => {
      const d = parseDate(e.date);
      if (d.getFullYear() === year && d.getMonth() === month) set.add(d.getDate());
    });
    return set;
  }
}
