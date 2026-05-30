import { Component, Input, Output, EventEmitter, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SettingsService } from '../../services/settings.service';
import { AppSettings } from '../../models/expense.model';

const MONTHS_SHORT = ['jan','fev','mar','abr','mai','jun','jul','ago','set','out','nov','dez'];

@Component({
  selector: 'app-settings-panel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './settings-panel.component.html',
  styleUrls: ['./settings-panel.component.scss'],
})
export class SettingsPanelComponent {
  @Input() open = false;
  @Output() close = new EventEmitter<void>();

  accents: AppSettings['accent'][] = ['indigo', 'esmeralda', 'coral', 'violeta'];
  accentColors: Record<AppSettings['accent'], string> = {
    indigo:    '#5B5BD6',
    esmeralda: '#0E9F6E',
    coral:     '#F0603A',
    violeta:   '#8B5CF6',
  };

  /** Dias disponíveis para seleção (1–28 para ser seguro em todos os meses). */
  readonly days = Array.from({ length: 28 }, (_, i) => i + 1);

  /** Preview: "Gastos de 25/abr → 25/mai  ·  vence 5/jun" */
  readonly invoicePreview = computed(() => {
    const { fechamento, vencimento } = this.settingsService.settings();
    const today = new Date();
    let closeYear = today.getFullYear();
    let closeMonth = today.getMonth();

    // Se já passamos do fechamento, a próxima fatura fecha no mês seguinte
    if (today.getDate() >= fechamento) {
      closeMonth++;
      if (closeMonth > 11) { closeMonth = 0; closeYear++; }
    }

    // Fatura abre no fechamento do mês anterior
    const openMonth = closeMonth === 0 ? 11 : closeMonth - 1;
    const openYear  = closeMonth === 0 ? closeYear - 1 : closeYear;

    // Vencimento: se vencimento < fechamento, é no mesmo mês do fechamento; senão, mês seguinte
    let dueMonth = vencimento <= fechamento ? closeMonth : closeMonth + 1;
    let dueYear  = closeYear;
    if (dueMonth > 11) { dueMonth = 0; dueYear++; }

    return `${fechamento}/${MONTHS_SHORT[openMonth]} → ${fechamento}/${MONTHS_SHORT[closeMonth]}  ·  vence ${vencimento}/${MONTHS_SHORT[dueMonth]}`;
  });

  constructor(public settingsService: SettingsService) {}

  get s() { return this.settingsService.settings(); }

  setTheme(theme: AppSettings['theme']): void {
    this.settingsService.update({ theme });
  }

  setAccent(accent: AppSettings['accent']): void {
    this.settingsService.update({ accent });
  }

  toggleChart(): void {
    this.settingsService.update({ showChart: !this.s.showChart });
  }

  setFechamento(day: number): void {
    this.settingsService.update({ fechamento: day });
  }

  setVencimento(day: number): void {
    this.settingsService.update({ vencimento: day });
  }
}
