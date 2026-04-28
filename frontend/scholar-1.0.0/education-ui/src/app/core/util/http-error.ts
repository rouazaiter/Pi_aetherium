import { HttpErrorResponse } from '@angular/common/http';
import type { ApiErrorBody } from '../models/api.models';

const MSG_404_API =
  'Réponse 404 : la ressource est introuvable. Vérifiez que le backend Spring est lancé (port 8089) et que vous exécutez la dernière version du backend. Test rapide : http://localhost:8089/api/auth/health (JSON {"status":"ok",...}).';

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
      if (err.url?.includes('/api/subscriptions/') && err.url?.includes('/invoice')) {
        return "Facture introuvable pour cet abonnement. Redémarrez le backend (version à jour) puis réessayez.";
      }
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
      return 'L’application n’a pas reçu de réponse de l’API Spring (serveur arrêté, mauvais port, ou pare-feu). Démarrez le backend, vérifiez server.port=8089, puis testez http://localhost:8089/api/auth/health dans le navigateur.';
    }
  }
  return fallback;
}
