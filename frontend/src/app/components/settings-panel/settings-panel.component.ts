import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SettingsService } from '../../services/settings.service';
import { AppSettings } from '../../models/expense.model';

@Component({
  selector: 'app-settings-panel',
  standalone: true,
  imports: [CommonModule, FormsModule],
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

  setFechamento(val: string): void {
    const n = Math.max(1, Math.min(31, parseInt(val) || 1));
    this.settingsService.update({ fechamento: n });
  }

  setVencimento(val: string): void {
    const n = Math.max(1, Math.min(31, parseInt(val) || 1));
    this.settingsService.update({ vencimento: n });
  }
}
