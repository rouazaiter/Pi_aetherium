import { Routes } from '@angular/router';
import { RoomListComponent } from './features/room-session/components/room-list/room-list.component';
import { RoomSessionComponent } from './features/room-session/components/room-session/room-session.component';

export const routes: Routes = [
  { path: '', redirectTo: '/rooms', pathMatch: 'full' },
  { path: 'rooms', component: RoomListComponent },
  { path: 'rooms/:roomId', component: RoomSessionComponent }
];