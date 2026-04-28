import type { HotKeys, BaseSubWorkModuleProps } from "../plugin/types";
import { BaseApplianceManager } from "../plugin/baseApplianceManager";
import type EventEmitter2 from "eventemitter2";
import { Collector } from "../collector";
import { MasterControlForWorker } from "../core/mainEngine";
export type HotKeyChecker = (event: HotKeyEvent, kind: KeyboardKind) => boolean;
export type HotKeyEvent = {
    readonly nativeEvent?: KeyboardEvent;
    readonly kind: "KeyDown" | "KeyUp";
    readonly key: string;
    readonly altKey: boolean;
    readonly ctrlKey: boolean;
    readonly shiftKey: boolean;
};
export declare enum KeyboardKind {
    Mac = "mac",
    Windows = "windows"
}
export type HotKeyCheckerNode = {
    readonly kind: keyof HotKeys;
    readonly checker: HotKeyChecker;
};
export interface HotkeyManager {
    readonly internalMsgEmitter: EventEmitter2;
    readonly control: BaseApplianceManager;
    readonly roomHotkeyCheckers: HotKeyCheckerNode[];
    onActiveHotkey(hotKey: keyof HotKeys): void;
    onSelfActiveHotkey(hotKey: keyof HotKeys): void;
    colloctHotkey(e: KeyboardEvent): void;
}
export declare class HotkeyManagerImpl implements HotkeyManager {
    internalMsgEmitter: EventEmitter2;
    control: BaseApplianceManager;
    roomHotkeyCheckers: HotKeyCheckerNode[];
    private tmpCopyStore;
    private tmpCopyCoordInfo?;
    constructor(props: BaseSubWorkModuleProps);
    get isUseSelf(): boolean;
    get isSelector(): boolean;
    get collector(): Collector | undefined;
    get mainEngine(): MasterControlForWorker | undefined;
    get keyboardKind(): KeyboardKind;
    private getEventKey;
    onActiveHotkey(hotKey: keyof HotKeys): void;
    colloctHotkey(e: KeyboardEvent): void;
    onSelfActiveHotkey(hotKey: keyof HotKeys): void;
    private checkHotkey;
    private copySelectorToTemp;
    private pasteTempToFocusView;
    private setMemberState;
}
