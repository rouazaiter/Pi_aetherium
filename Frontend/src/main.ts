import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';

// SockJS expects a Node-like global in some bundles; provide a browser-safe shim.
if (!(globalThis as any).global) {
  (globalThis as any).global = globalThis;
}

platformBrowserDynamic().bootstrapModule(AppModule, {
  ngZoneEventCoalescing: true
})
  .catch(err => console.error(err));
