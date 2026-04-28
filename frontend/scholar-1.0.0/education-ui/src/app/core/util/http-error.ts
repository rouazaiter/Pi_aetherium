import { HttpErrorResponse } from '@angular/common/http';
import type { ApiErrorBody } from '../models/api.models';

const MSG_404_API =
  'Réponse 404 : la requête n’atteint pas le bon service. 1) Démarrez le backend Spring (port 8081, voir application.properties). 2) Ouvrez dans le navigateur : http://localhost:8081/api/auth/health — vous devez voir JSON {"status":"ok",...}. Sinon, recompilez (Maven Rebuild) et relancez l’appli. 3) Vérifiez que environment.apiUrl pointe vers ce même hôte/port.';

/** Codes d’erreur API → message lisible (évite d’afficher des identifiants techniques). */
const KNOWN_API_ERRORS: Record<string, string> = {
  PROFILE_VERIFICATION_REQUIRED:
    'Vérification requise : saisissez le code à 6 chiffres reçu par e-mail pour accéder au profil. La vérification expire après un délai ; reconnectez-vous si besoin.',
};

function humanizeApiError(raw: string): string {
  const t = raw?.trim() ?? '';
  return KNOWN_API_ERRORS[t] ?? t;
}

export function messageFromHttpError(err: unknown, fallback: string): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 404) {
      return MSG_404_API;
    }
    const body = err.error as ApiErrorBody | string | null;
    if (body && typeof body === 'object') {
      if (body.error) {
        return humanizeApiError(body.error);
      }
      if (body.errors) {
        return Object.values(body.errors).join(' · ');
      }
    }
    if (typeof body === 'string' && body.trim()) {
      return body;
    }
    if (err.status === 0) {
      return 'Après Google, l’application appelle votre API Spring (POST /api/auth/social) sur http://localhost:8081 — le navigateur n’a pas reçu de réponse (serveur arrêté, mauvais port, ou pare-feu). Démarrez le backend (Maven / IntelliJ), vérifiez server.port=8081, puis testez http://localhost:8081/api/auth/health dans le navigateur. Ouvrez le site en http://localhost:4200 (même machine).';
    }
  }
  return fallback;
}
