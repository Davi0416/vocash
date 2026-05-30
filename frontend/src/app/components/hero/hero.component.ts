import { Component, Input, Output, EventEmitter, signal, OnDestroy, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ExpenseService } from '../../services/expense.service';
import { ApiService, extractErrorMessage } from '../../services/api.service';
import { SettingsService } from '../../services/settings.service';
import { HttpErrorResponse } from '@angular/common/http';
import { MONTHS_FULL } from '../../models/expense.model';

const EXAMPLES = [
  'Gastei 50 reais no mercado',
  'Notebook 3600 em 12x',
  'Uber de 23,90',
  'Tênis 599 parcelado em 6 vezes',
  'Conta de luz 142',
  'Cinema 60 reais',
];

const fmt = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

/** RMS abaixo disso = silêncio (0–1). Ajuste se o ambiente for muito ruidoso. */
const SILENCE_RMS_THRESHOLD = 0.012;
/** Silêncio contínuo por este tempo (ms) dispara o envio automático. */
const SILENCE_DURATION_MS = 1800;
/** Período mínimo de gravação antes de detectar silêncio (evita disparo imediato). */
const MIN_RECORDING_MS = 900;

@Component({
  selector: 'app-hero',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, FormsModule],
  templateUrl: './hero.component.html',
  styleUrls: ['./hero.component.scss'],
})
export class HeroComponent implements OnDestroy {
  @Input() listening = false;
  @Output() listeningChange = new EventEmitter<boolean>();

  phrase      = signal('');
  hint        = signal('Toque e diga seu gasto');
  processing  = signal(false);
  examples    = EXAMPLES;

  private mediaRecorder: MediaRecorder | null = null;
  private audioChunks: Blob[] = [];

  // silence detection
  private audioContext: AudioContext | null = null;
  private analyser: AnalyserNode | null = null;
  private vadInterval: ReturnType<typeof setInterval> | null = null;
  private silenceTimer: ReturnType<typeof setTimeout> | null = null;
  private recordingStart = 0;

  constructor(
    public expenseService: ExpenseService,
    private apiService: ApiService,
    public settingsService: SettingsService,
  ) {}

  ngOnDestroy(): void {
    this.cleanup();
  }

  /* ── getters ─────────────────────────────────────────────────── */

  get totalParts(): { integer: string; cents: string } {
    const v = fmt.format(this.expenseService.total());
    const [int, cents] = v.split(',');
    return { integer: int, cents: `,${cents ?? '00'}` };
  }

  get currentMonth(): string { return MONTHS_FULL[new Date().getMonth()]; }

  get monthInvoice(): string {
    const now = new Date();
    return fmt.format(this.expenseService.invoiceTotal(now.getFullYear(), now.getMonth()));
  }

  get count(): number { return this.expenseService.expenses().length; }

  /* ── mic toggle ──────────────────────────────────────────────── */

  async toggleMic(): Promise<void> {
    if (this.processing()) return;
    if (!this.listening) {
      await this.startListening();
    } else {
      if (this.phrase().trim()) {
        this.sendText(this.phrase().trim());
      } else {
        this.stopRecordingAndSend();
      }
    }
  }

  private async startListening(): Promise<void> {
    this.listeningChange.emit(true);

    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      this.audioChunks = [];
      this.recordingStart = Date.now();

      const mimeType = MediaRecorder.isTypeSupported('audio/webm;codecs=opus')
        ? 'audio/webm;codecs=opus'
        : 'audio/webm';

      this.mediaRecorder = new MediaRecorder(stream, { mimeType });
      this.mediaRecorder.ondataavailable = e => { if (e.data.size > 0) this.audioChunks.push(e.data); };
      this.mediaRecorder.start(250);

      this.hint.set('Ouvindo... fale seu gasto');
      this.startVAD(stream);
    } catch {
      this.hint.set('Sem microfone — digite o gasto e envie');
    }
  }

  /* ── Voice Activity Detection (Web Audio API) ────────────────── */

  private startVAD(stream: MediaStream): void {
    try {
      this.audioContext = new AudioContext();
      const source = this.audioContext.createMediaStreamSource(stream);
      this.analyser = this.audioContext.createAnalyser();
      this.analyser.fftSize = 512;
      source.connect(this.analyser);

      const buffer = new Uint8Array(this.analyser.frequencyBinCount);

      this.vadInterval = setInterval(() => {
        if (!this.analyser || !this.listening) return;

        // Ignora primeiros MIN_RECORDING_MS para não disparar imediatamente
        if (Date.now() - this.recordingStart < MIN_RECORDING_MS) return;

        // Calcula RMS do domínio do tempo
        this.analyser.getByteTimeDomainData(buffer);
        let sum = 0;
        for (let i = 0; i < buffer.length; i++) {
          const norm = (buffer[i] - 128) / 128;
          sum += norm * norm;
        }
        const rms = Math.sqrt(sum / buffer.length);

        if (rms < SILENCE_RMS_THRESHOLD) {
          // Inicia contagem se ainda não iniciou
          if (!this.silenceTimer) {
            this.silenceTimer = setTimeout(() => {
              this.stopRecordingAndSend();
            }, SILENCE_DURATION_MS);
            // Dica visual
            this.hint.set('Silêncio detectado — enviando...');
          }
        } else {
          // Voz detectada: cancela contagem e restaura dica
          if (this.silenceTimer) {
            clearTimeout(this.silenceTimer);
            this.silenceTimer = null;
            this.hint.set('Ouvindo... fale seu gasto');
          }
        }
      }, 80); // checa a cada 80 ms
    } catch {
      // Web Audio não disponível — sem detecção automática
    }
  }

  private stopVAD(): void {
    if (this.vadInterval)  { clearInterval(this.vadInterval);   this.vadInterval = null; }
    if (this.silenceTimer) { clearTimeout(this.silenceTimer);   this.silenceTimer = null; }
    if (this.audioContext) { this.audioContext.close();          this.audioContext = null; }
    this.analyser = null;
  }

  /* ── stop & send ─────────────────────────────────────────────── */

  private stopRecordingAndSend(): void {
    // Para VAD antes de tudo para evitar chamadas duplas
    this.stopVAD();

    // Bloqueia novo toque ANTES do onstop (fecha a janela de corrida)
    this.processing.set(true);

    // Atualiza UI imediatamente
    this.listeningChange.emit(false);
    this.hint.set('✦ Processando áudio...');

    if (this.mediaRecorder && this.mediaRecorder.state !== 'inactive') {
      this.mediaRecorder.onstop = () => {
        const blob = new Blob(this.audioChunks, { type: 'audio/webm' });
        try { this.mediaRecorder?.stream.getTracks().forEach(t => t.stop()); } catch { /* ignore */ }
        this.mediaRecorder = null;
        this.sendAudio(blob);
      };
      this.mediaRecorder.stop();
    } else {
      this.processing.set(false);
      this.hint.set('Toque e diga seu gasto');
    }
  }

  private sendAudio(blob: Blob): void {
    // processing já foi setado true em stopRecordingAndSend()
    this.apiService.processarAudio(blob, 'gasto.webm').subscribe({
      next: resposta => {
        this.processing.set(false);
        this.hint.set(resposta);
        this.expenseService.refresh();
        setTimeout(() => this.hint.set('Toque e diga seu gasto'), 4000);
      },
      error: (err: HttpErrorResponse) => {
        this.processing.set(false);
        this.hint.set(extractErrorMessage(err, 'Erro ao processar áudio. Tente novamente.'));
        setTimeout(() => this.hint.set('Toque e diga seu gasto'), 5000);
      },
    });
  }

  /* ── text path ───────────────────────────────────────────────── */

  send(): void   { const r = this.phrase().trim(); if (r) this.sendText(r); }
  onEnter(): void { const r = this.phrase().trim(); if (r) this.sendText(r); }

  useExample(text: string): void {
    if (!this.listening) this.listeningChange.emit(true);
    this.phrase.set(text);
  }

  private sendText(raw: string): void {
    this.phrase.set('');
    this.stopVAD();
    this.cleanupStream();
    this.listeningChange.emit(false);
    this.processing.set(true);
    this.hint.set('✦ Interpretando...');

    this.apiService.processarTexto(raw).subscribe({
      next: resposta => {
        this.processing.set(false);
        this.hint.set(resposta);
        this.expenseService.refresh();
        setTimeout(() => this.hint.set('Toque e diga seu gasto'), 4000);
      },
      error: (err: HttpErrorResponse) => {
        this.processing.set(false);
        this.hint.set(extractErrorMessage(err, 'Erro ao processar. Tente novamente.'));
        setTimeout(() => this.hint.set('Toque e diga seu gasto'), 5000);
      },
    });
  }

  /* ── cleanup ─────────────────────────────────────────────────── */

  private cleanupStream(): void {
    if (this.mediaRecorder) {
      if (this.mediaRecorder.state !== 'inactive') {
        try { this.mediaRecorder.stop(); } catch { /* ignore */ }
      }
      try { this.mediaRecorder.stream.getTracks().forEach(t => t.stop()); } catch { /* ignore */ }
      this.mediaRecorder = null;
    }
  }

  private cleanup(): void {
    this.stopVAD();
    this.cleanupStream();
  }
}
