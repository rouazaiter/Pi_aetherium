import { EmitEventType } from "../../../plugin/types";
import { BaseMsgMethod } from "../base";
import { IworkId } from "../../types";
export type SetLockEmitData = {
    workIds: IworkId[];
    isLocked: boolean;
    viewId: string;
};
export declare class SetLockMethod extends BaseMsgMethod {
    protected lastEmtData?: unknown;
    readonly emitEventType: EmitEventType;
    collect(data: SetLockEmitData): void;
}
