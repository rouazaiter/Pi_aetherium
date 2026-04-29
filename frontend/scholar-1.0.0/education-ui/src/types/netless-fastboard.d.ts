declare module '@netless/fastboard' {
  export function createFastboard(options: any): Promise<any>;
  export function mount(app: any, div: HTMLElement): any;
}

declare module '@netless/fastboard/dist/index.mjs' {
  export function createFastboard(options: any): Promise<any>;
  export function mount(app: any, div: HTMLElement): any;
}


