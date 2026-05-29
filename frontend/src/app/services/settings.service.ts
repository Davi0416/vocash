import { Injectable, signal, effect } from '@angular/core';
import { AppSettings } from '../models/expense.model';

const STORAGE_KEY = 'falo.settings';

const DEFAULTS: AppSettings = {
  theme: 'light',
  accent: 'indigo',
  showChart: true,
  fechamento: 28,
  vencimento: 8,
};

@Injectable({ providedIn: 'root' })
export class SettingsService {
  readonly settings = signal<AppSettings>(this.load());

  constructor() {
    effect(() => {
      const s = this.settings();
      localStorage.setItem(STORAGE_KEY, JSON.stringify(s));
      document.body.dataset['theme'] = s.theme;
      document.body.dataset['accent'] = s.accent;
    });
  }

  update(partial: Partial<AppSettings>): void {
    this.settings.update(s => ({ ...s, ...partial }));
  }

  private load(): AppSettings {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (raw) {
        const parsed = JSON.parse(raw) as Partial<AppSettings>;
        const s: AppSettings = { ...DEFAULTS, ...parsed };
        if (!['light','dark'].includes(s.theme)) s.theme = 'light';
        if (!['indigo','esmeralda','coral','violeta'].includes(s.accent)) s.accent = 'indigo';
        s.fechamento = Math.max(1, Math.min(31, s.fechamento || 28));
        s.vencimento = Math.max(1, Math.min(31, s.vencimento || 8));
        return s;
      }
    } catch { /* ignore */ }
    return { ...DEFAULTS };
  }
}
