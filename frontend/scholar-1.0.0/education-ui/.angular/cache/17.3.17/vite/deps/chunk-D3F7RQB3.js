import {
  getConfig2
} from "./chunk-R6F3TVG2.js";
import {
  __name
} from "./chunk-XLVYR3CC.js";
import {
  select_default
} from "./chunk-AE4KXJIE.js";

// node_modules/mermaid/dist/chunks/mermaid.core/chunk-426QAEUC.mjs
var selectSvgElement = __name((id) => {
  const { securityLevel } = getConfig2();
  let root = select_default("body");
  if (securityLevel === "sandbox") {
    const sandboxElement = select_default(`#i${id}`);
    const doc = sandboxElement.node()?.contentDocument ?? document;
    root = select_default(doc.body);
  }
  const svg = root.select(`#${id}`);
  return svg;
}, "selectSvgElement");

export {
  selectSvgElement
};
//# sourceMappingURL=chunk-D3F7RQB3.js.map
