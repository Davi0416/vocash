import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

/** Shape do erro retornado pelo GlobalExceptionHandler. */
export interface ApiError {
  codigo: string;
  mensagem: string;
}

/** Extrai a mensagem amigável de um HttpErrorResponse. */
export function extractErrorMessage(err: HttpErrorResponse, fallback = 'Erro inesperado. Tente novamente.'): string {
  try {
    const body: ApiError = typeof err.error === 'string' ? JSON.parse(err.error) : err.error;
    if (body?.mensagem) return body.mensagem;
  } catch { /* ignore */ }
  if (err.status === 0) return 'Sem conexão com o servidor. Verifique se o backend está rodando.';
  if (err.status === 413) return 'O áudio enviado é muito grande. Limite: 25 MB.';
  return fallback;
}

/** Shape do Gasto retornado pela API Spring Boot. */
export interface BackendGasto {
  id: number;
  valor: number;
  categoria: string;
  descricao: string | null;
  local: string | null;
  data: string; // YYYY-MM-DD
  parcelas: number;
}

export interface GastoRequest {
  valor: number;
  categoria: string;
  descricao: string;
  local: string;
  data: string;
  parcelas: number;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(private http: HttpClient) {}

  /** Envia áudio para o pipeline Whisper → LLM → tool. */
  processarAudio(blob: Blob, filename: string): Observable<string> {
    const form = new FormData();
    form.append('audio', blob, filename);
    return this.http.post('/api/v1/assistente/processar', form, { responseType: 'text' });
  }

  /** Envia texto diretamente ao LLM → tool (sem Whisper). */
  processarTexto(texto: string): Observable<string> {
    const form = new FormData();
    form.append('texto', texto);
    return this.http.post('/api/v1/assistente/processar-texto', form, { responseType: 'text' });
  }

  /** Lista todos os gastos persistidos no PostgreSQL. */
  getGastos(): Observable<BackendGasto[]> {
    return this.http.get<BackendGasto[]>('/api/v1/gastos');
  }

  /** Cria um gasto manualmente (sem LLM). */
  criarGasto(req: GastoRequest): Observable<BackendGasto> {
    return this.http.post<BackendGasto>('/api/v1/gastos', req);
  }

  /** Atualiza um gasto existente. */
  atualizarGasto(id: number, req: GastoRequest): Observable<BackendGasto> {
    return this.http.put<BackendGasto>(`/api/v1/gastos/${id}`, req);
  }

  /** Remove um gasto pelo id. */
  deletarGasto(id: number): Observable<void> {
    return this.http.delete<void>(`/api/v1/gastos/${id}`);
  }
}
