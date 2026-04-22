declare module 'white-web-sdk' {
  export class WhiteWebSdk {
    constructor(config?: any);
    joinRoom(config: any): Promise<any>;
  }

  const WhiteWebSdkDefault: typeof WhiteWebSdk;
  export default WhiteWebSdkDefault;
}

