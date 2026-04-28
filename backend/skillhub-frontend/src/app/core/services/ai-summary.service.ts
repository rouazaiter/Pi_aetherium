import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

export interface DiscussionSummary {
  summary: string;
  keySteps: string[];
  codeSnippet: string;
}

export interface FailureExplanation {
  rootCause: string;
  commonCauses: string[];
  prevention: string[];
  analogy: string;
}

export interface WhyItWorksExplanation {
  explanation: string;
  concepts: string[];
  analogy: string;
}

@Injectable({ providedIn: 'root' })
export class AiSummaryService {

  private readonly AI_PROXY = 'http://localhost:8080/api/ai/ask';

  private postCache = new Map<number, string>();
  private discCache = new Map<number, DiscussionSummary>();
  private failureCache = new Map<number, FailureExplanation>();
  private whyCache = new Map<number, WhyItWorksExplanation>();

  constructor(private http: HttpClient) {}

  private get headers(): HttpHeaders {
    return new HttpHeaders({ 'Content-Type': 'application/json' });
  }

  private gemini(prompt: string): Observable<string> {
    return this.http.post<any>(this.AI_PROXY, { prompt }, { headers: this.headers }).pipe(
      map(res => res?.text?.trim() || ''),
      catchError(err => {
        console.error('[Gemini] error:', err?.error || err?.message);
        return of('');
      })
    );
  }

  // ── Post hover summary ────────────────────────────────────────────────────
  getSummary(postId: number, content: string): Observable<string> {
    if (this.postCache.has(postId)) return of(this.postCache.get(postId)!);
    const prompt = `Summarize this in 2 sentences: "${content.substring(0, 800)}"`;
    return this.gemini(prompt).pipe(
      map(s => {
        const result = s || this.extractiveFallback(content);
        this.postCache.set(postId, result);
        return result;
      })
    );
  }

  // ── Discussion solution summary ───────────────────────────────────────────
  getDiscussionSummary(discId: number, theme: string, messages: string[]): Observable<DiscussionSummary> {
    if (this.discCache.has(discId)) return of(this.discCache.get(discId)!);
    const conversation = messages.slice(0, 20).join('\n').substring(0, 2000);
    const prompt = `Analyze this discussion and provide a structured summary.

Topic: "${theme}"
Messages:
${conversation}

Respond in this EXACT format:
SUMMARY: <2-3 sentence summary of problem and solution>
STEPS:
- <step 1>
- <step 2>
CODE: <relevant code snippet or none>`;

    return this.gemini(prompt).pipe(
      map(text => {
        const result = text ? this.parseDiscussionResponse(text, theme, messages) : this.fallbackDiscussionSummary(theme, messages);
        this.discCache.set(discId, result);
        return result;
      })
    );
  }

  // ── "Why This Failed" explanation ─────────────────────────────────────────
  getFailureExplanation(postId: number, title: string, content: string): Observable<FailureExplanation> {
    if (this.failureCache.has(postId)) return of(this.failureCache.get(postId)!);
    const input = `${title}\n${content}`.substring(0, 1500);
    const prompt = `You are a senior software engineer explaining errors to beginners.

Error report: "${input}"

Respond in this EXACT format:
ROOT_CAUSE: <one clear sentence>
COMMON_CAUSES:
- <cause 1>
- <cause 2>
- <cause 3>
PREVENTION:
- <tip 1>
- <tip 2>
ANALOGY: <one simple real-world analogy>`;

    return this.gemini(prompt).pipe(
      map(text => {
        const result = text ? this.parseFailureResponse(text, title) : this.fallbackFailureExplanation(title);
        this.failureCache.set(postId, result);
        return result;
      })
    );
  }

  // ── Follow-up chat ────────────────────────────────────────────────────────
  askFollowUp(question: string, articleQuestion: string, articleAnswer: string, history: {role: string, text: string}[]): Observable<string> {
    const userHistory = history.filter(h => h.role === 'user').slice(-3).map(h => `- ${h.text}`).join('\n');
    const prompt = `You are a technical assistant helping a developer understand a solution.

KB Article:
Q: ${articleQuestion}
A: ${articleAnswer.substring(0, 600)}

${userHistory ? `Previous questions:\n${userHistory}\n` : ''}
New question: "${question}"

Answer directly and concisely in 2-4 sentences.`;

    return this.gemini(prompt).pipe(
      map(text => text || this.localAnswer(question, articleAnswer)),
      catchError(() => of('Could not get a response. Please try again.'))
    );
  }

  // ── "Why This Works" KB explainer ─────────────────────────────────────────
  getWhyItWorks(articleId: number, question: string, answer: string): Observable<WhyItWorksExplanation> {
    if (this.whyCache.has(articleId)) return of(this.whyCache.get(articleId)!);
    const prompt = `You are a senior developer explaining a solution to a junior developer.

Question: "${question}"
Solution: "${answer.substring(0, 800)}"

Respond in this EXACT format:
EXPLANATION: <2-3 sentences explaining why this fix works>
CONCEPTS:
- <concept 1>
- <concept 2>
- <concept 3>
ANALOGY: <one simple real-world analogy>`;

    return this.gemini(prompt).pipe(
      map(text => {
        const result = text ? this.parseWhyItWorks(text, question) : this.fallbackWhyItWorks(question);
        this.whyCache.set(articleId, result);
        return result;
      })
    );
  }

  // ── Parsers ───────────────────────────────────────────────────────────────
  private parseWhyItWorks(text: string, question: string): WhyItWorksExplanation {
    const expMatch = text.match(/EXPLANATION:\s*(.+?)(?=CONCEPTS:|$)/s);
    const conceptsMatch = text.match(/CONCEPTS:\s*((?:- .+\n?)+)/s);
    const analogyMatch = text.match(/ANALOGY:\s*(.+?)$/s);
    const parseList = (raw: string) => raw.split('\n').map(s => s.replace(/^-\s*/, '').trim()).filter(Boolean).slice(0, 4);
    return {
      explanation: expMatch?.[1]?.trim() || this.fallbackWhyItWorks(question).explanation,
      concepts: conceptsMatch?.[1] ? parseList(conceptsMatch[1]) : this.fallbackWhyItWorks(question).concepts,
      analogy: analogyMatch?.[1]?.trim() || this.fallbackWhyItWorks(question).analogy
    };
  }

  private fallbackWhyItWorks(question: string): WhyItWorksExplanation {
    return {
      explanation: `This solution works because it addresses the root cause of "${question}".`,
      concepts: ['Root cause analysis', 'Configuration management', 'Debugging methodology'],
      analogy: 'Like fixing a leaky pipe at the source rather than mopping the floor repeatedly.'
    };
  }

  private parseFailureResponse(text: string, title: string): FailureExplanation {
    const rootMatch = text.match(/ROOT_CAUSE:\s*(.+?)(?=COMMON_CAUSES:|$)/s);
    const causesMatch = text.match(/COMMON_CAUSES:\s*((?:- .+\n?)+)/s);
    const preventMatch = text.match(/PREVENTION:\s*((?:- .+\n?)+)/s);
    const analogyMatch = text.match(/ANALOGY:\s*(.+?)$/s);
    const parseList = (raw: string) => raw.split('\n').map(s => s.replace(/^-\s*/, '').trim()).filter(Boolean).slice(0, 4);
    return {
      rootCause: rootMatch?.[1]?.trim() || `The error in "${title}" was caused by a system or configuration issue.`,
      commonCauses: causesMatch?.[1] ? parseList(causesMatch[1]) : ['Misconfiguration', 'Missing dependency', 'Network issue'],
      prevention: preventMatch?.[1] ? parseList(preventMatch[1]) : ['Check logs carefully', 'Validate configuration'],
      analogy: analogyMatch?.[1]?.trim() || 'Like trying to open a locked door without the right key.'
    };
  }

  private fallbackFailureExplanation(title: string): FailureExplanation {
    return {
      rootCause: `The error "${title}" typically occurs due to a misconfiguration or missing resource.`,
      commonCauses: ['Incorrect configuration', 'Missing dependency or service', 'Network or permission issue'],
      prevention: ['Always check application logs', 'Validate environment variables', 'Test in a staging environment first'],
      analogy: 'Like trying to call someone whose phone is off.'
    };
  }

  private parseDiscussionResponse(text: string, theme: string, messages: string[]): DiscussionSummary {
    const summaryMatch = text.match(/SUMMARY:\s*(.+?)(?=STEPS:|$)/s);
    const stepsMatch = text.match(/STEPS:\s*((?:- .+\n?)+)/s);
    const codeMatch = text.match(/CODE:\s*([\s\S]+?)$/s);
    const summary = summaryMatch?.[1]?.trim() || this.fallbackDiscussionSummary(theme, messages).summary;
    const stepsRaw = stepsMatch?.[1] || '';
    const keySteps = stepsRaw.split('\n').map((s: string) => s.replace(/^-\s*/, '').trim()).filter(Boolean).slice(0, 4);
    const codeSnippet = codeMatch?.[1]?.trim() === 'none' ? '' : (codeMatch?.[1]?.trim() || '');
    return { summary, keySteps: keySteps.length ? keySteps : this.fallbackDiscussionSummary(theme, messages).keySteps, codeSnippet };
  }

  private fallbackDiscussionSummary(theme: string, messages: string[]): DiscussionSummary {
    const sentences = messages.join(' ').match(/[^.!?]+[.!?]+/g) || [];
    return {
      summary: sentences.slice(0, 2).join(' ') || `Discussion about: ${theme}`,
      keySteps: messages.slice(0, 3).map(m => m.substring(0, 80)),
      codeSnippet: ''
    };
  }

  private localAnswer(question: string, answer: string): string {
    const q = question.toLowerCase();
    if (q.includes('why')) return `This works because: ${answer.substring(0, 200)}`;
    if (q.includes('how')) return `Here is how it works: ${answer.substring(0, 200)}`;
    return answer.substring(0, 200) + (answer.length > 200 ? '...' : '');
  }

  extractiveFallback(content: string): string {
    const sentences = content.match(/[^.!?]+[.!?]+/g) || [];
    return sentences.slice(0, 2).join(' ').trim() || content.substring(0, 150) + '...';
  }
}
