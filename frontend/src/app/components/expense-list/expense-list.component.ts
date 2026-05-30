import { Component, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ExpenseService, shortDate, todayISOExport } from '../../services/expense.service';
import { Expense, CategoryKey, CATS, CAT_KEYS } from '../../models/expense.model';
import { ToastService } from '../../services/toast.service';

const NEW_ID = '__new__';
const fmt = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

@Component({
  selector: 'app-expense-list',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, FormsModule],
  templateUrl: './expense-list.component.html',
  styleUrls: ['./expense-list.component.scss'],
})
export class ExpenseListComponent {
  editingId = signal<string | null>(null);
  editDraft: Partial<Expense> = {};
  isNew = false;

  catKeys = CAT_KEYS;
  cats = CATS;

  constructor(
    public expenseService: ExpenseService,
    private toastService: ToastService,
  ) {}



  catColor(cat: CategoryKey): string { return CATS[cat].color; }
  catLabel(cat: CategoryKey): string { return CATS[cat].label; }
  catIcon(cat: CategoryKey): string  { return CATS[cat].icon; }
  shortDate(iso: string): string     { return shortDate(iso); }
  fmt(v: number): string             { return fmt.format(v); }
  perInstallment(e: Expense): string { return fmt.format((e.value || 0) / (e.installments || 1)); }

  /** Abre o formulário de criação inline (sem tocar no backend ainda). */
  addManual(): void {
    this.isNew = true;
    this.editingId.set(NEW_ID);
    this.editDraft = { name: '', value: 0, cat: 'outros', installments: 1, date: todayISOExport() };
  }

  startEdit(e: Expense): void {
    this.isNew = false;
    this.editingId.set(e.id);
    this.editDraft = { ...e };
  }

  cancelEdit(): void {
    this.editingId.set(null);
    this.editDraft = {};
    this.isNew = false;
  }

  saveEdit(): void {
    const id = this.editingId();
    if (!id) return;

    const payload: Omit<Expense, 'id'> = {
      name:         (this.editDraft.name || '').trim() || 'Gasto',
      value:        parseFloat(String(this.editDraft.value)) || 0,
      installments: Math.max(1, Math.min(360, parseInt(String(this.editDraft.installments)) || 1)),
      date:         this.editDraft.date || todayISOExport(),
      cat:          (this.editDraft.cat as CategoryKey) || 'outros',
    };

    if (this.isNew) {
      this.expenseService.add(payload);
    } else {
      this.expenseService.update({ id, ...payload });
    }

    this.editingId.set(null);
    this.editDraft = {};
    this.isNew = false;
  }

  delete(id: string): void {
    this.expenseService.remove(id);
    this.toastService.show('Item excluído');
  }

  trackById(_: number, e: Expense): string { return e.id; }
}
