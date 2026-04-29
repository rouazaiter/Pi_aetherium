import { Injectable } from '@angular/core';

// Comprehensive bad word list — English + French + Arabic + leet variants
const BAD_WORDS = [
  // English
  'fuck','fck','fuuck','fvck','f u c k',
  'shit','sh1t','sht',
  'bitch','b1tch',
  'asshole','a55hole',
  'bastard','b4stard',
  'damn','crap',
  'dick','d1ck',
  'pussy','cock','c0ck',
  'whore','wh0re',
  'slut','sl*t',
  'nigger','n1gger',
  'faggot','f4ggot',
  'retard','ret4rd',
  'idiot','moron',
  'kys','kill yourself',
  'rape','r4pe',
  'terrorist','nazi','n4zi',
  'porn','p0rn',
  // French
  'merde','putain','connard','salope','enculé','bordel','con','nique',
  'pute','batard','fdp','tg',
  // Arabic
  'كلب','حمار','عاهرة','كس','زب','لعنة','ابن الكلب','يلعن',
  // Spanish
  'puta','mierda','cabron','pendejo','chinga',
  // German
  'scheisse','scheiße','arschloch',
  // Italian
  'cazzo','vaffanculo','stronzo'
];

// Leet-speak normalization
function normalize(text: string): string {
  return text
    .toLowerCase()
    .replace(/@/g, 'a').replace(/4/g, 'a')
    .replace(/3/g, 'e')
    .replace(/1/g, 'i').replace(/!/g, 'i')
    .replace(/0/g, 'o')
    .replace(/5/g, 's').replace(/\$/g, 's')
    .replace(/7/g, 't')
    .replace(/\*/g, '').replace(/\./g, '')
    .replace(/(.)\1{2,}/g, '$1$1'); // remove repeated chars: fuuuck → fuuck
}

export interface ModerationResult {
  flagged: boolean;
  detectedWords: string[];
}

@Injectable({ providedIn: 'root' })
export class ModerationService {

  check(text: string): ModerationResult {
    if (!text?.trim()) return { flagged: false, detectedWords: [] };

    const normalized = normalize(text);
    const detected: string[] = [];

    for (const word of BAD_WORDS) {
      const normalizedWord = normalize(word);
      if (normalized.includes(normalizedWord)) {
        detected.push(word);
      }
    }

    return { flagged: detected.length > 0, detectedWords: [...new Set(detected)] };
  }

  // Returns text with bad words replaced by asterisks (for display)
  censor(text: string): string {
    let result = text;
    for (const word of BAD_WORDS) {
      const regex = new RegExp(word.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'gi');
      result = result.replace(regex, '*'.repeat(word.length));
    }
    return result;
  }
}
