import {
  __async
} from "./chunk-WME3VOFU.js";

// node_modules/@netless/appliance-plugin/dist/svgToImageLoader-mXH53h-l.mjs
var t = null;
var r = null;
function n() {
  return __async(this, null, function* () {
    return t || r || (r = (() => __async(this, null, function* () {
      try {
        const e = yield import("./dom-to-image-2D4YOW7L.js");
        return t = e.default || e, t;
      } catch (e) {
        throw r = null, e;
      }
    }))(), r);
  });
}
function o() {
  return t;
}
export {
  o as getSvgToImageLib,
  n as loadSvgToImageLib
};
//# sourceMappingURL=svgToImageLoader-mXH53h-l-JJZM4WIL.js.map
