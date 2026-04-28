import { EmitEventType } from "../../../plugin/types";
import { BaseMsgMethod } from "../base";
import { IworkId } from "../../types";
import { FontStyleType, FontWeightType } from "../../../component/textEditor";
export type SetFontEmtData = {
    workIds: IworkId[];
    bold?: FontWeightType;
    underline?: boolean;
    lineThrough?: boolean;
    italic?: FontStyleType;
    fontSize?: number;
    viewId: string;
};
export declare class SetFontStyleMethod extends BaseMsgMethod {
    protected lastEmtData?: unknown;
    readonly emitEventType: EmitEventType;
    private timerId?;
    private setTextStyle;
    collect(data: SetFontEmtData): Promise<void>;
}
