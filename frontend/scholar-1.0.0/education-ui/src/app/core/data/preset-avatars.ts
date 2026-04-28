/**
 * Avatars SkillHub — stockage profil : `skillhub-preset:{id}` (ou ancien `/avatars/preset-{id}.svg`).
 *
 * Affichage :
 * - `USE_CUSTOM_PRESET_FACE_IMAGES === false` (défaut) : SVG intégrés (data-URL), aucun fichier requis.
 * - `true` : images `src/assets/avatars/1.png` … `8.png` (voir LISEZMOI.txt dans ce dossier).
 */
/** `true` = affiche `src/assets/avatars/1.png` … `8.png` (repli SVG sur erreur de chargement côté grille profil). */
export const USE_CUSTOM_PRESET_FACE_IMAGES = true;

function assetAvatarPath(id: string): string {
  return `assets/avatars/${id}.png`;
}

const SVG: Record<string, string> = {
  '1':
    '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><linearGradient id="a" x1="0" y1="0" x2="1" y2="1"><stop offset="0%" stop-color="#7a6ad8"/><stop offset="100%" stop-color="#5b52c7"/></linearGradient></defs><rect width="100" height="100" rx="22" fill="url(#a)"/><circle cx="50" cy="38" r="16" fill="#fff" opacity=".92"/><ellipse cx="50" cy="78" rx="30" ry="20" fill="#fff" opacity=".88"/></svg>',
  '2':
    '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><linearGradient id="b" x1="0" y1="1" x2="1" y2="0"><stop offset="0%" stop-color="#f97316"/><stop offset="100%" stop-color="#fb7185"/></linearGradient></defs><rect width="100" height="100" rx="22" fill="url(#b)"/><circle cx="50" cy="38" r="16" fill="#fff" opacity=".9"/><path d="M20 78 Q50 58 80 78 L80 88 Q50 72 20 88 Z" fill="#fff" opacity=".85"/></svg>',
  '3':
    '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><linearGradient id="c" x1="0" y1="0" x2="1" y2="1"><stop offset="0%" stop-color="#0ea5e9"/><stop offset="100%" stop-color="#6366f1"/></linearGradient></defs><rect width="100" height="100" rx="22" fill="url(#c)"/><circle cx="50" cy="38" r="16" fill="#fff" opacity=".9"/><ellipse cx="50" cy="76" rx="28" ry="18" fill="#fff" opacity=".82"/></svg>',
  '4':
    '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><linearGradient id="d" x1="0" y1="0" x2="1" y2="1"><stop offset="0%" stop-color="#22c55e"/><stop offset="100%" stop-color="#059669"/></linearGradient></defs><rect width="100" height="100" rx="22" fill="url(#d)"/><circle cx="50" cy="38" r="16" fill="#fff" opacity=".9"/><ellipse cx="50" cy="78" rx="30" ry="19" fill="#fff" opacity=".85"/></svg>',
  '5':
    '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><linearGradient id="e" x1="0" y1="0" x2="1" y2="1"><stop offset="0%" stop-color="#eab308"/><stop offset="100%" stop-color="#f59e0b"/></linearGradient></defs><rect width="100" height="100" rx="22" fill="url(#e)"/><circle cx="50" cy="38" r="16" fill="#fff" opacity=".92"/><ellipse cx="50" cy="77" rx="29" ry="19" fill="#fff" opacity=".86"/></svg>',
  '6':
    '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><linearGradient id="f" x1="0" y1="1" x2="1" y2="0"><stop offset="0%" stop-color="#4c1d95"/><stop offset="100%" stop-color="#7c3aed"/></linearGradient></defs><rect width="100" height="100" rx="22" fill="url(#f)"/><circle cx="50" cy="38" r="16" fill="#fff" opacity=".88"/><ellipse cx="50" cy="78" rx="28" ry="18" fill="#fff" opacity=".8"/></svg>',
  '7':
    '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1"><stop offset="0%" stop-color="#475569"/><stop offset="100%" stop-color="#1e293b"/></linearGradient></defs><rect width="100" height="100" rx="22" fill="url(#g)"/><circle cx="50" cy="38" r="16" fill="#fff" opacity=".9"/><ellipse cx="50" cy="77" rx="30" ry="19" fill="#fff" opacity=".84"/></svg>',
  '8':
    '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><linearGradient id="h" x1="0" y1="0" x2="1" y2="1"><stop offset="0%" stop-color="#ec4899"/><stop offset="100%" stop-color="#a855f7"/></linearGradient></defs><rect width="100" height="100" rx="22" fill="url(#h)"/><circle cx="50" cy="38" r="16" fill="#fff" opacity=".92"/><ellipse cx="50" cy="78" rx="28" ry="18" fill="#fff" opacity=".86"/></svg>',
};

function toDataUrl(svg: string): string {
  return 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(svg);
}

const DATA_BY_ID: Record<string, string> = Object.fromEntries(
  Object.entries(SVG).map(([id, xml]) => [id, toDataUrl(xml)]),
);

export const PRESET_AVATAR_IDS = ['1', '2', '3', '4', '5', '6', '7', '8'] as const;

export function presetAvatarStorageKey(id: string): string {
  return `skillhub-preset:${id}`;
}

export function presetAvatarDataUrl(id: string): string {
  return DATA_BY_ID[id] ?? '';
}

/** Source pour la grille du sélecteur et l’aperçu (PNG local ou SVG intégré). */
export function presetAvatarPickerSrc(id: string): string {
  if (USE_CUSTOM_PRESET_FACE_IMAGES) {
    return assetAvatarPath(id);
  }
  return presetAvatarDataUrl(id);
}

/** Affiche une photo profil si c’est un preset SkillHub (nouvelle clé ou ancien chemin fichier). */
export function resolvePresetProfilePicture(stored: string | null | undefined): string {
  const v = stored?.trim() ?? '';
  if (!v) {
    return '';
  }
  const mNew = /^skillhub-preset:([1-8])$/.exec(v);
  if (mNew) {
    const id = mNew[1];
    if (USE_CUSTOM_PRESET_FACE_IMAGES) {
      return assetAvatarPath(id);
    }
    return presetAvatarDataUrl(id);
  }
  const mOld = /^\/avatars\/preset-([1-8])\.svg$/.exec(v);
  if (mOld) {
    const id = mOld[1];
    if (USE_CUSTOM_PRESET_FACE_IMAGES) {
      return assetAvatarPath(id);
    }
    return presetAvatarDataUrl(id);
  }
  return '';
}

export function isStoredPresetAvatar(stored: string | null | undefined): boolean {
  const v = stored?.trim() ?? '';
  return /^skillhub-preset:[1-8]$/.test(v) || /^\/avatars\/preset-[1-8]\.svg$/.test(v);
}

export function presetIdFromStored(stored: string | null | undefined): string | null {
  const v = stored?.trim() ?? '';
  let m = /^skillhub-preset:([1-8])$/.exec(v);
  if (m) {
    return m[1];
  }
  m = /^\/avatars\/preset-([1-8])\.svg$/.exec(v);
  return m ? m[1] : null;
}
