import { Injectable } from '@angular/core';

export type UrgencyLevel = 'NONE' | 'LOW' | 'HIGH' | 'CRITICAL';

export interface UrgencyResult {
  level: UrgencyLevel;
  matchedKeywords: string[];
}

const CRITICAL = [
  'production down','prod down','system down','server down','database down',
  'data loss','data breach','security breach','hacked','compromised',
  'critical error','fatal error','application crashed','app crashed',
  '500 error','error 500','http 500','internal server error',
  'out of memory','oom','memory leak','deadlock',
  'urgent','emergency','critical','immediately','asap',
  'panne','urgence','critique'
];

const HIGH = [
  'not working','broken','bug','crash','exception','error',
  'failed','failure','cannot connect','connection refused','timeout',
  '403','404','401','null pointer','nullpointerexception',
  'stack overflow','infinite loop','performance issue','slow',
  'help needed','stuck','blocked','cannot deploy','deployment failed',
  'ne fonctionne pas','erreur','problème','bloqué'
];

@Injectable({ providedIn: 'root' })
export class UrgencyService {

  detect(title: string, content: string): UrgencyResult {
    const combined = `${title} ${content}`.toLowerCase();
    const matched: string[] = [];

    for (const kw of CRITICAL) {
      if (combined.includes(kw)) matched.push(kw);
    }
    if (matched.length) return { level: 'CRITICAL', matchedKeywords: matched };

    for (const kw of HIGH) {
      if (combined.includes(kw)) matched.push(kw);
    }
    if (matched.length) return { level: 'HIGH', matchedKeywords: matched };

    return { level: 'NONE', matchedKeywords: [] };
  }
}
