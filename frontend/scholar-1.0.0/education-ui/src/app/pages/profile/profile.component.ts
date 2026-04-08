import { HttpErrorResponse } from '@angular/common/http';
import { Component, HostListener, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { animate, query, sequence, stagger, style, transition, trigger } from '@angular/animations';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../core/services/auth.service';
import { ProfileService } from '../../core/services/profile.service';
import { WelcomeDialogService } from '../../core/services/welcome-dialog.service';
import {
  PRESET_AVATAR_IDS,
  presetAvatarDataUrl,
  presetAvatarPickerSrc,
  presetAvatarStorageKey,
  presetIdFromStored,
  resolvePresetProfilePicture,
} from '../../core/data/preset-avatars';
import { messageFromHttpError } from '../../core/util/http-error';

const PRESET_LABELS: Record<string, string> = {
  '1': 'Violet',
  '2': 'Corail',
  '3': 'Océan',
  '4': 'Forêt',
  '5': 'Soleil',
  '6': 'Nuit',
  '7': 'Ardoise',
  '8': 'Rose',
};

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss',
  animations: [
    trigger('profileBackdrop', [
      transition(':enter', [
        style({ opacity: 0 }),
        animate('900ms ease-out', style({ opacity: 1 })),
      ]),
    ]),
    trigger('verifyLift', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(40px) scale(0.94)' }),
        animate('780ms cubic-bezier(0.22, 1, 0.36, 1)', style({ opacity: 1, transform: 'none' })),
      ]),
    ]),
    trigger('pageLayout', [
      transition(':enter', [
        sequence([
          query(
            '.profile-hero',
            [
              style({ opacity: 0, transform: 'translateY(24px)' }),
              stagger(0, [
                animate('640ms cubic-bezier(0.22, 1, 0.36, 1)', style({ opacity: 1, transform: 'none' })),
              ]),
            ],
            { optional: true },
          ),
          query(
            '.profile-card',
            [
              style({ opacity: 0, transform: 'translateY(26px)' }),
              stagger(140, [
                animate('560ms cubic-bezier(0.22, 1, 0.36, 1)', style({ opacity: 1, transform: 'none' })),
              ]),
            ],
            { optional: true },
          ),
          query(
            '.profile-field',
            [
              style({ opacity: 0, transform: 'translateY(16px)' }),
              stagger(48, [
                animate('380ms cubic-bezier(0.22, 1, 0.36, 1)', style({ opacity: 1, transform: 'none' })),
              ]),
            ],
            { optional: true },
          ),
        ]),
      ]),
    ]),
    trigger('alertReveal', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(-10px) scale(0.98)' }),
        animate('380ms cubic-bezier(0.22, 1, 0.36, 1)', style({ opacity: 1, transform: 'none' })),
      ]),
    ]),
  ],
})
export class ProfileComponent implements OnInit, OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly profileApi = inject(ProfileService);
  private readonly welcomeDialog = inject(WelcomeDialogService);
  private readonly auth = inject(AuthService);

  readonly form = this.fb.nonNullable.group({
    firstName: [''],
    lastName: [''],
    interestsText: [''],
    description: [''],
    profilePicture: [''],
    recuperationEmail: ['', Validators.email],
  });

  readonly verifyForm = this.fb.nonNullable.group({
    code: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]],
  });

  readonly saveAck = signal(false);

  loadError = '';
  saveError = '';
  loading = false;
  saving = false;

  verificationRequired = false;
  sendingCode = false;
  verifying = false;
  verifyError = '';
  codeHint = '';

  /** Pour l’avatar : éviter les schémas dangereux. */
  avatarBroken = false;

  /** Aperçu local avant réponse serveur (object URL). */
  photoPreviewUrl: string | null = null;
  photoUploading = false;
  photoError = '';

  private readonly maxPhotoBytes = 3 * 1024 * 1024;

  /** Carte photo retournée : face arrière = grille d’avatars SkillHub. */
  readonly avatarPickerOpen = signal(false);
  presetSaving = false;

  /** IDs dont le PNG local a échoué → affichage SVG intégré à la place. */
  private readonly presetAssetLoadFailed = signal<ReadonlySet<string>>(new Set());

  readonly presetAvatars: { id: string; label: string }[] = PRESET_AVATAR_IDS.map((id) => ({
    id,
    label: PRESET_LABELS[id] ?? id,
  }));

  /** Grille presets : PNG si configuré et dispo, sinon repli sur data-URL SVG. */
  presetGridImgSrc(id: string): string {
    if (this.presetAssetLoadFailed().has(id)) {
      return presetAvatarDataUrl(id);
    }
    return presetAvatarPickerSrc(id);
  }

  onPresetGridImgError(id: string): void {
    this.presetAssetLoadFailed.update((s) => new Set(s).add(id));
  }

  ngOnInit(): void {
    this.loadProfile();
  }

  ngOnDestroy(): void {
    this.revokePhotoPreview();
  }

  @HostListener('document:keydown', ['$event'])
  onDocumentKeydown(ev: KeyboardEvent): void {
    if (ev.key === 'Escape' && this.avatarPickerOpen()) {
      this.avatarPickerOpen.set(false);
    }
  }

  maskedEmail(): string {
    const email = this.auth.auth()?.email?.trim();
    if (!email || !email.includes('@')) {
      return 'votre adresse e-mail';
    }
    const [local, domain] = email.split('@');
    const vis = local.length <= 2 ? `${local[0] ?? ''}••` : `${local.slice(0, 2)}•••`;
    return `${vis}@${domain}`;
  }

  displayName(): string {
    const fn = this.form.controls.firstName.value?.trim();
    const ln = this.form.controls.lastName.value?.trim();
    if (fn || ln) {
      return [fn, ln].filter(Boolean).join(' ');
    }
    return this.auth.auth()?.username ?? 'Membre';
  }

  initials(): string {
    const fn = this.form.controls.firstName.value?.trim();
    const ln = this.form.controls.lastName.value?.trim();
    if (fn && ln) {
      return (fn[0] + ln[0]).toUpperCase();
    }
    if (fn) {
      return fn.slice(0, 2).toUpperCase();
    }
    const u = this.auth.auth()?.username;
    return u ? u.slice(0, 2).toUpperCase() : 'SH';
  }

  /**
   * URL affichable pour la photo : fichier hébergé par l’API (`/api/files/...`),
   * lien https externe, ou aperçu local pendant l’upload.
   */
  resolvedProfilePictureUrl(): string {
    if (this.photoPreviewUrl) {
      return this.photoPreviewUrl;
    }
    const u = this.form.controls.profilePicture.value?.trim() ?? '';
    if (!u) {
      return '';
    }
    const presetUrl = resolvePresetProfilePicture(u);
    if (presetUrl) {
      return presetUrl;
    }
    if (/^https?:\/\//i.test(u)) {
      return u;
    }
    if (u.startsWith('/api/')) {
      const base = (environment.apiUrl ?? '').trim().replace(/\/$/, '');
      return base ? `${base}${u}` : u;
    }
    return '';
  }

  toggleAvatarPicker(): void {
    this.avatarPickerOpen.update((v) => !v);
  }

  closeAvatarPicker(): void {
    this.avatarPickerOpen.set(false);
  }

  isPresetSelected(id: string): boolean {
    return presetIdFromStored(this.form.controls.profilePicture.value) === id;
  }

  selectPresetAvatar(id: string): void {
    this.form.patchValue({ profilePicture: presetAvatarStorageKey(id) });
    this.avatarBroken = false;
    this.closeAvatarPicker();
    this.persistAvatarToServer();
  }

  private persistAvatarToServer(): void {
    const v = this.form.getRawValue();
    const interests = v.interestsText
      .split(',')
      .map((s) => s.trim())
      .filter(Boolean);
    this.presetSaving = true;
    this.profileApi
      .updateMe({
        firstName: v.firstName || null,
        lastName: v.lastName || null,
        interests,
        description: v.description || null,
        profilePicture: v.profilePicture || null,
        recuperationEmail: v.recuperationEmail || null,
      })
      .subscribe({
        next: (p) => {
          this.presetSaving = false;
          this.auth.patchSessionProfilePicture(p.profilePicture ?? null);
          this.saveAck.set(true);
          window.setTimeout(() => this.saveAck.set(false), 2800);
        },
        error: (err) => {
          this.presetSaving = false;
          if (this.tryHandleProfileVerificationRequired(err)) {
            return;
          }
          this.saveError = messageFromHttpError(err, 'Enregistrement de l’avatar impossible.');
        },
      });
  }

  onPhotoSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    this.photoError = '';
    if (!file) {
      return;
    }
    if (!file.type.startsWith('image/')) {
      this.photoError = 'Choisissez une image (JPEG, PNG, WebP ou GIF).';
      return;
    }
    if (file.size > this.maxPhotoBytes) {
      this.photoError = 'Image trop volumineuse (maximum 3 Mo).';
      return;
    }
    this.revokePhotoPreview();
    this.photoPreviewUrl = URL.createObjectURL(file);
    this.avatarBroken = false;
    this.photoUploading = true;
    this.profileApi.uploadProfilePhoto(file).subscribe({
      next: (p) => {
        this.photoUploading = false;
        this.form.patchValue({ profilePicture: p.profilePicture ?? '' });
        this.auth.patchSessionProfilePicture(p.profilePicture ?? null);
        this.revokePhotoPreview();
        this.saveAck.set(true);
        window.setTimeout(() => this.saveAck.set(false), 2800);
      },
      error: (err) => {
        this.photoUploading = false;
        this.revokePhotoPreview();
        if (this.tryHandleProfileVerificationRequired(err)) {
          return;
        }
        this.photoError = messageFromHttpError(err, 'Envoi de la photo impossible.');
      },
    });
  }

  private revokePhotoPreview(): void {
    if (this.photoPreviewUrl) {
      URL.revokeObjectURL(this.photoPreviewUrl);
      this.photoPreviewUrl = null;
    }
  }

  resendCode(): void {
    this.requestSendCode();
  }

  requestSendCode(): void {
    this.verifyError = '';
    this.sendingCode = true;
    this.profileApi.sendProfileVerificationCode().subscribe({
      next: (r) => {
        this.sendingCode = false;
        this.codeHint = r.message;
      },
      error: (err) => {
        this.sendingCode = false;
        this.verifyError = messageFromHttpError(err, 'Envoi du code impossible.');
      },
    });
  }

  submitVerification(): void {
    this.verifyError = '';
    if (this.verifyForm.invalid) {
      this.verifyForm.markAllAsTouched();
      return;
    }
    this.verifying = true;
    const code = this.verifyForm.controls.code.value.trim();
    this.profileApi.verifyProfileAccessCode(code).subscribe({
      next: () => {
        this.verifying = false;
        this.verificationRequired = false;
        this.verifyForm.reset();
        this.loadProfile();
      },
      error: (err) => {
        this.verifying = false;
        this.verifyError = messageFromHttpError(err, 'Vérification impossible.');
      },
    });
  }

  private loadProfile(): void {
    this.loading = true;
    this.loadError = '';
    this.avatarBroken = false;
    this.profileApi.getMe().subscribe({
      next: (p) => {
        this.loading = false;
        this.form.patchValue({
          firstName: p.firstName ?? '',
          lastName: p.lastName ?? '',
          interestsText: (p.interests ?? []).join(', '),
          description: p.description ?? '',
          profilePicture: p.profilePicture ?? '',
          recuperationEmail: p.recuperationEmail ?? '',
        });
        this.auth.patchSessionProfilePicture(p.profilePicture ?? null);
        this.welcomeDialog.tryShowPendingWelcome();
      },
      error: (err) => {
        this.loading = false;
        if (err instanceof HttpErrorResponse && err.status === 403) {
          const code = (err.error as { error?: string } | null)?.error;
          if (code === 'PROFILE_VERIFICATION_REQUIRED') {
            this.verificationRequired = true;
            this.requestSendCode();
            return;
          }
        }
        this.loadError = messageFromHttpError(err, 'Impossible de charger le profil.');
      },
    });
  }

  /**
   * Session « profil » expirée (ou nouvelle connexion) : le serveur renvoie 403 avec ce code.
   * On rouvre l’écran code e-mail au lieu d’afficher l’identifiant technique.
   */
  private tryHandleProfileVerificationRequired(err: unknown): boolean {
    if (!(err instanceof HttpErrorResponse) || err.status !== 403) {
      return false;
    }
    const code = (err.error as { error?: string } | null)?.error;
    if (code !== 'PROFILE_VERIFICATION_REQUIRED') {
      return false;
    }
    this.verificationRequired = true;
    this.saveError = '';
    this.photoError = '';
    this.loadError = '';
    this.requestSendCode();
    return true;
  }

  save(): void {
    this.saveError = '';
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const interests = v.interestsText
      .split(',')
      .map((s) => s.trim())
      .filter(Boolean);
    this.saving = true;
    this.profileApi
      .updateMe({
        firstName: v.firstName || null,
        lastName: v.lastName || null,
        interests,
        description: v.description || null,
        profilePicture: v.profilePicture || null,
        recuperationEmail: v.recuperationEmail || null,
      })
      .subscribe({
        next: (p) => {
          this.saving = false;
          this.auth.patchSessionProfilePicture(p.profilePicture ?? null);
          this.saveAck.set(true);
          window.setTimeout(() => this.saveAck.set(false), 2800);
        },
        error: (err) => {
          this.saving = false;
          if (this.tryHandleProfileVerificationRequired(err)) {
            return;
          }
          this.saveError = messageFromHttpError(err, 'Enregistrement impossible.');
        },
      });
  }
}
