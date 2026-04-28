export declare function transformToNormalData(str: string): any;
export declare function transformToSerializableData(data: unknown): string;
export declare function normalDataToUint8Array(data: unknown): Uint8Array;
export declare function uint8ArrayToNormalData(data: Uint8Array): unknown;
export declare const plainObjectKeys: <T>(o: T) => Array<Extract<keyof T, string>>;
