import { EmitEventType } from "../../../plugin/types";
import { BaseMsgMethodForWorker } from "../baseForWorker";
import { IWorkerMessage } from "../../types";
export declare class CopyNodeMethodForWorker extends BaseMsgMethodForWorker {
    readonly emitEventType: EmitEventType;
    consume(data: IWorkerMessage): boolean | undefined;
    consumeForLocalWorker(data: IWorkerMessage): Promise<void>;
}
