import {
  selectSvgElement
} from "./chunk-D3F7RQB3.js";
import {
  parse
} from "./chunk-M5STHUAL.js";
import "./chunk-GF277QKG.js";
import "./chunk-6TXF5SFS.js";
import "./chunk-D3KTUBJH.js";
import "./chunk-D2HH6CAQ.js";
import "./chunk-JH3ODH7B.js";
import "./chunk-5LOJHV3O.js";
import "./chunk-7ERPKTMT.js";
import "./chunk-GIA23Y76.js";
import "./chunk-VJ4R27KS.js";
import "./chunk-6HLTO7YV.js";
import {
  configureSvgSize
} from "./chunk-R6F3TVG2.js";
import {
  __name,
  log
} from "./chunk-XLVYR3CC.js";
import "./chunk-AE4KXJIE.js";
import "./chunk-PSVLW5TH.js";
import "./chunk-W7TIXABP.js";
import "./chunk-T366FHQI.js";
import {
  __async
} from "./chunk-WME3VOFU.js";

// node_modules/mermaid/dist/chunks/mermaid.core/infoDiagram-42DDH7IO.mjs
var parser = {
  parse: __name((input) => __async(void 0, null, function* () {
    const ast = yield parse("info", input);
    log.debug(ast);
  }), "parse")
};
var DEFAULT_INFO_DB = {
  version: "11.14.0" + (true ? "" : "-tiny")
};
var getVersion = __name(() => DEFAULT_INFO_DB.version, "getVersion");
var db = {
  getVersion
};
var draw = __name((text, id, version) => {
  log.debug("rendering info diagram\n" + text);
  const svg = selectSvgElement(id);
  configureSvgSize(svg, 100, 400, true);
  const group = svg.append("g");
  group.append("text").attr("x", 100).attr("y", 40).attr("class", "version").attr("font-size", 32).style("text-anchor", "middle").text(`v${version}`);
}, "draw");
var renderer = { draw };
var diagram = {
  parser,
  db,
  renderer
};
export {
  diagram
};
//# sourceMappingURL=infoDiagram-42DDH7IO-EX6PRIEB.js.map
