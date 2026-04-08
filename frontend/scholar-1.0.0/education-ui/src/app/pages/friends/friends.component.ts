import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import type { FriendResponse } from '../../core/models/api.models';
import { FriendService } from '../../core/services/friend.service';
import { messageFromHttpError } from '../../core/util/http-error';

@Component({
  selector: 'app-friends',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './friends.component.html',
  styleUrl: './friends.component.scss',
})
export class FriendsComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(FriendService);

  readonly addForm = this.fb.nonNullable.group({
    username: ['', Validators.required],
  });

  list: FriendResponse[] = [];
  loadError = '';
  addError = '';
  loading = false;
  adding = false;

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.loading = true;
    this.loadError = '';
    this.api.list().subscribe({
      next: (rows) => {
        this.loading = false;
        this.list = rows;
      },
      error: (err) => {
        this.loading = false;
        this.loadError = messageFromHttpError(err, 'Impossible de charger la liste d’amis.');
      },
    });
  }

  add(): void {
    this.addError = '';
    if (this.addForm.invalid) {
      this.addForm.markAllAsTouched();
      return;
    }
    this.adding = true;
    this.api.add({ username: this.addForm.getRawValue().username.trim() }).subscribe({
      next: () => {
        this.adding = false;
        this.addForm.reset();
        this.refresh();
      },
      error: (err) => {
        this.adding = false;
        this.addError = messageFromHttpError(err, 'Ajout impossible.');
      },
    });
  }

  remove(id: number): void {
    this.api.remove(id).subscribe({
      next: () => this.refresh(),
      error: (err) => {
        this.loadError = messageFromHttpError(err, 'Suppression impossible.');
      },
    });
  }
}
