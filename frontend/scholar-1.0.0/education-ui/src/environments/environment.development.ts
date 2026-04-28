export const environment = {
  production: false,
  /**
   * Laisser vide en dev avec `ng serve` : les appels vont sur `/api/...` (même origine que le front)
   * et `proxy.conf.json` redirige vers Spring (http://localhost:8089). Évite les 404 si le navigateur
   * n’atteint pas directement le port 8089.
   *
   * Si vous servez le front autrement (sans proxy), mettez l’URL du backend, ex. :
   * `http://localhost:8089` — doit être identique à `server.port` dans `application.properties`.
   */
  apiUrl: 'http://localhost:8089',
  /**
   * Google Sign-In : ID client OAuth « Application Web » (console.cloud.google.com → Identifiants).
   * Origine JS autorisée : http://localhost:4200 — même ID dans Spring : app.oauth.google-client-id
   * Tant que vide : la ligne « Sign up with Google » est un décoratif NON CLIQUABLE jusqu’à configuration.
   */
  googleClientId: '679411521835-7q64ktvpt7anrfg1hmumjv5bsf2oe1nq.apps.googleusercontent.com',
  /** Facebook (optionnel) — developers.facebook.com + Spring app.oauth.facebook-app-id / secret */
  facebookAppId: '1947309899485311',
  giphyApiKey: 'Wlf22ErX4VH9dJysQ9MPUCopfvV3YlKw',
};
