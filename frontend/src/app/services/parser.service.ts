import { Injectable } from '@angular/core';
import { CategoryKey, CATS } from '../models/expense.model';

const KEYWORDS: Record<CategoryKey, string[]> = {
  mercado:     ['mercado','supermercado','feira','hortifruti','padaria','pão','pao','açougue','acougue','compras','atacad','extra','carrefour'],
  alimentacao: ['restaurante','lanche','ifood','comida','almoço','almoco','jantar','café','cafe','pizza','pizzaria','hambúrguer','hamburguer','burguer','bar','cerveja','açaí','acai','sorvete','padoca','mc','burger','sushi','marmita','delivery'],
  transporte:  ['uber','99','táxi','taxi','ônibus','onibus','metrô','metro','gasolina','combustível','combustivel','etanol','álcool','alcool','estacionamento','passagem','bilhete','pedágio','pedagio','posto','corrida','brt'],
  lazer:       ['cinema','show','jogo','netflix','spotify','festa','viagem','viajem','lazer','parque','balada','ingresso','streaming','disney','hbo','prime','game','steam','livro','passeio'],
  contas:      ['conta','luz','água','agua','internet','telefone','celular','aluguel','energia','boleto','fatura','cartão','cartao','assinatura','condomínio','condominio','gás','gas','tv','wifi'],
  saude:       ['farmácia','farmacia','remédio','remedio','médico','medico','academia','consulta','dentista','exame','plano','psicólogo','psicologo','terapia','drogaria','vitamina','gym'],
  outros:      [],
};

const NUM_WORDS: Record<string, number> = {
  'um':1,'uma':1,'dois':2,'duas':2,'três':3,'tres':3,'quatro':4,'cinco':5,'seis':6,
  'sete':7,'oito':8,'nove':9,'dez':10,'onze':11,'doze':12,'quinze':15,'vinte':20,
  'trinta':30,'quarenta':40,'cinquenta':50,'sessenta':60,'setenta':70,'oitenta':80,
  'noventa':90,'cem':100,'cento':100,'duzentos':200,'trezentos':300,'quinhentos':500,'mil':1000,
};

const STOP = new Set(['reais','real','conto','contos','pila','pilas','mangos','mango',
  'gastei','gasto','paguei','comprei','de','no','na','em','com','pra','para','do','da',
  'nos','nas','um','uma','r$','r','foi','hoje','ontem','uns','umas','aquele','aquela',
  'esse','essa','o','a','parcelado','parcelada','parcelas','parcela','vezes','vez','x','cada']);

export interface ParsedExpense {
  name: string;
  value: number;
  cat: CategoryKey;
  installments: number;
  date: string;
}

function isoLocal(d: Date): string {
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
}

@Injectable({ providedIn: 'root' })
export class ParserService {
  parse(raw: string): ParsedExpense {
    const text = ' ' + raw.toLowerCase().trim() + ' ';

    // parcelas
    let installments = 1, perInstallment = false;
    const instMatch = text.match(/(\d+)\s*x\b/)
      ?? text.match(/(?:em\s+)?(\d+)\s*(?:vezes|parcelas?)\b/)
      ?? text.match(/parcelad\w*\s+(?:em\s+)?(\d+)/);
    if (instMatch) {
      const n = parseInt(instMatch[1]);
      if (n >= 2 && n <= 360) installments = n;
    }
    if (/(?:\d+\s*x|vezes|parcelas?)\s+de\b/.test(text) ||
        /\bde\s+[\d.,]+\s*cada\b/.test(text) || /\bcada\b/.test(text)) {
      perInstallment = true;
    }

    // data
    const dt = new Date();
    let dayMatched: string | null = null;
    if (/\banteontem\b/.test(text)) dt.setDate(dt.getDate() - 2);
    else if (/\bontem\b/.test(text)) dt.setDate(dt.getDate() - 1);
    else {
      const dm = text.match(/\bdia\s+(\d{1,2})\b/);
      if (dm) { const dd = parseInt(dm[1]); if (dd >= 1 && dd <= 31) { dt.setDate(dd); dayMatched = dm[0]; } }
    }
    const date = isoLocal(dt);

    // valor
    let cleaned = raw.replace(/r\$\s*/gi, '');
    if (dayMatched) cleaned = cleaned.replace(/\bdia\s+\d{1,2}\b/i, ' ');
    if (installments > 1) {
      cleaned = cleaned
        .replace(new RegExp(`\\b${installments}\\s*x\\b`, 'i'), ' ')
        .replace(new RegExp(`\\b${installments}\\s*(?:vezes|parcelas?)\\b`, 'i'), ' ')
        .replace(new RegExp(`parcelad\\w*\\s+(?:em\\s+)?${installments}`, 'i'), ' ');
    }
    let value = 0;
    const m = cleaned.match(/(\d{1,3}(?:\.\d{3})+|\d+)([.,]\d{1,2})?/);
    if (m) {
      value = parseFloat((m[1].replace(/\./g, '') + (m[2] ? m[2].replace(',', '.') : '')).replace(',', '.'));
    }
    if (!value) {
      let sum = 0, found = false;
      for (const w in NUM_WORDS) {
        if (new RegExp(`\\b${w}\\b`).test(text)) { sum += NUM_WORDS[w]; found = true; }
      }
      if (found) value = sum;
    }
    if (perInstallment && installments > 1) value = value * installments;

    // categoria
    let cat: CategoryKey = 'outros', matchedWord: string | null = null;
    outer: for (const c of Object.keys(KEYWORDS) as CategoryKey[]) {
      for (const kw of KEYWORDS[c]) {
        if (new RegExp(`(?:^|[^\\wà-ÿ])${kw.replace(/[.*+?^${}()|[\]\\]/g,'\\$&')}(?:[^\\wà-ÿ]|$)`, 'i').test(text)) {
          cat = c; matchedWord = kw; break outer;
        }
      }
    }

    // nome
    let name: string | null = null;
    const pm = text.match(/\b(?:no|na|em|com|pra|para|do|da|nos|nas)\s+([a-zà-ú]+(?:\s+[a-zà-ú]+)?)/);
    if (pm) {
      const cand = pm[1].split(/\s+/).filter(w => !STOP.has(w) && !/^\d/.test(w) && !/^\d+x$/i.test(w));
      if (cand.length) name = cand.slice(0, 2).join(' ');
    }
    if (!name && matchedWord) name = matchedWord;
    if (!name) {
      const words = raw.toLowerCase().replace(/r\$/g, '').split(/\s+/)
        .filter(w => w && !STOP.has(w) && !/^[\d.,]+$/.test(w) && !/^\d+x$/i.test(w));
      name = words[0] || CATS[cat].label.toLowerCase();
    }
    name = name.charAt(0).toUpperCase() + name.slice(1);

    return { name, value: value || 0, cat, installments, date };
  }
}
