import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

export interface ConfirmOptions {
  title: string;
  message: string;
  confirmLabel?: string;   // default: 'Confirm'
  cancelLabel?: string;    // default: 'Cancel'
  variant?: 'danger' | 'warning' | 'info';  // controls button + icon colour
}

interface ConfirmRequest extends ConfirmOptions {
  resolve: (value: boolean) => void;
}

@Injectable({ providedIn: 'root' })
export class ConfirmModalService {
  private _request$ = new Subject<ConfirmRequest>();
  readonly request$ = this._request$.asObservable();

  /** Opens the modal and returns a Promise<boolean>. */
  confirm(options: ConfirmOptions): Promise<boolean> {
    return new Promise(resolve =>
      this._request$.next({ ...options, resolve })
    );
  }

  /** Shorthand for a standard delete confirmation. */
  delete(itemName: string): Promise<boolean> {
    return this.confirm({
      title: 'Confirm Deletion',
      message: `Are you sure you want to delete "${itemName}"? This action cannot be undone.`,
      confirmLabel: 'Delete',
      variant: 'danger'
    });
  }
}
