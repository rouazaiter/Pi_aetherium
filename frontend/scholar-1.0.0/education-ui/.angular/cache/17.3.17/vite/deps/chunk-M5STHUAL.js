import {
  __name
} from "./chunk-6HLTO7YV.js";
import {
  __async
} from "./chunk-WME3VOFU.js";

// node_modules/@mermaid-js/parser/dist/mermaid-parser.core.mjs
var parsers = {};
var initializers = {
  info: __name(() => __async(void 0, null, function* () {
    const { createInfoServices: createInfoServices2 } = yield import("./info-OMHHGYJF-KCIOBLHU.js");
    const parser = createInfoServices2().Info.parser.LangiumParser;
    parsers.info = parser;
  }), "info"),
  packet: __name(() => __async(void 0, null, function* () {
    const { createPacketServices: createPacketServices2 } = yield import("./packet-4T2RLAQJ-KXQDP4O2.js");
    const parser = createPacketServices2().Packet.parser.LangiumParser;
    parsers.packet = parser;
  }), "packet"),
  pie: __name(() => __async(void 0, null, function* () {
    const { createPieServices: createPieServices2 } = yield import("./pie-ZZUOXDRM-6IAE46DJ.js");
    const parser = createPieServices2().Pie.parser.LangiumParser;
    parsers.pie = parser;
  }), "pie"),
  treeView: __name(() => __async(void 0, null, function* () {
    const { createTreeViewServices: createTreeViewServices2 } = yield import("./treeView-SZITEDCU-GYY5WHD6.js");
    const parser = createTreeViewServices2().TreeView.parser.LangiumParser;
    parsers.treeView = parser;
  }), "treeView"),
  architecture: __name(() => __async(void 0, null, function* () {
    const { createArchitectureServices: createArchitectureServices2 } = yield import("./architecture-YZFGNWBL-YCZHBGZB.js");
    const parser = createArchitectureServices2().Architecture.parser.LangiumParser;
    parsers.architecture = parser;
  }), "architecture"),
  gitGraph: __name(() => __async(void 0, null, function* () {
    const { createGitGraphServices: createGitGraphServices2 } = yield import("./gitGraph-7Q5UKJZL-RMQUB6UQ.js");
    const parser = createGitGraphServices2().GitGraph.parser.LangiumParser;
    parsers.gitGraph = parser;
  }), "gitGraph"),
  radar: __name(() => __async(void 0, null, function* () {
    const { createRadarServices: createRadarServices2 } = yield import("./radar-PYXPWWZC-4NN26A5J.js");
    const parser = createRadarServices2().Radar.parser.LangiumParser;
    parsers.radar = parser;
  }), "radar"),
  treemap: __name(() => __async(void 0, null, function* () {
    const { createTreemapServices: createTreemapServices2 } = yield import("./treemap-W4RFUUIX-M5ZWUZXH.js");
    const parser = createTreemapServices2().Treemap.parser.LangiumParser;
    parsers.treemap = parser;
  }), "treemap"),
  wardley: __name(() => __async(void 0, null, function* () {
    const { createWardleyServices: createWardleyServices2 } = yield import("./wardley-RL74JXVD-2UPRA2GS.js");
    const parser = createWardleyServices2().Wardley.parser.LangiumParser;
    parsers.wardley = parser;
  }), "wardley")
};
function parse(diagramType, text) {
  return __async(this, null, function* () {
    const initializer = initializers[diagramType];
    if (!initializer) {
      throw new Error(`Unknown diagram type: ${diagramType}`);
    }
    if (!parsers[diagramType]) {
      yield initializer();
    }
    const parser = parsers[diagramType];
    const result = parser.parse(text);
    if (result.lexerErrors.length > 0 || result.parserErrors.length > 0) {
      throw new MermaidParseError(result);
    }
    return result.value;
  });
}
__name(parse, "parse");
var MermaidParseError = class extends Error {
  constructor(result) {
    const lexerErrors = result.lexerErrors.map((err) => {
      const line = err.line !== void 0 && !isNaN(err.line) ? err.line : "?";
      const column = err.column !== void 0 && !isNaN(err.column) ? err.column : "?";
      return `Lexer error on line ${line}, column ${column}: ${err.message}`;
    }).join("\n");
    const parserErrors = result.parserErrors.map((err) => {
      const line = err.token.startLine !== void 0 && !isNaN(err.token.startLine) ? err.token.startLine : "?";
      const column = err.token.startColumn !== void 0 && !isNaN(err.token.startColumn) ? err.token.startColumn : "?";
      return `Parse error on line ${line}, column ${column}: ${err.message}`;
    }).join("\n");
    super(`Parsing failed: ${lexerErrors} ${parserErrors}`);
    this.result = result;
  }
  static {
    __name(this, "MermaidParseError");
  }
};

export {
  parse
};
//# sourceMappingURL=chunk-M5STHUAL.js.map
