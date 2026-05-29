import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SettingsService } from './services/settings.service';
import { HeaderComponent } from './components/header/header.component';
import { HeroComponent } from './components/hero/hero.component';
import { ExpenseListComponent } from './components/expense-list/expense-list.component';
import { CalendarComponent } from './components/calendar/calendar.component';
import { SummaryComponent } from './components/summary/summary.component';
import { SettingsPanelComponent } from './components/settings-panel/settings-panel.component';
import { ToastComponent } from './components/toast/toast.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    HeaderComponent,
    HeroComponent,
    ExpenseListComponent,
    CalendarComponent,
    SummaryComponent,
    SettingsPanelComponent,
    ToastComponent,
  ],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit {
  listening = signal(false);
  settingsOpen = signal(false);

  constructor(public settingsService: SettingsService) {}

  ngOnInit(): void {
    const s = this.settingsService.settings();
    document.body.dataset['theme'] = s.theme;
    document.body.dataset['accent'] = s.accent;
  }

  onListeningChange(v: boolean): void {
    this.listening.set(v);
  }

  openSettings(): void {
    this.settingsOpen.set(true);
  }

  closeSettings(): void {
    this.settingsOpen.set(false);
  }
}
