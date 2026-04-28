import {
  parseFontSize
} from "./chunk-24IDZ4D5.js";
import {
  defaultConfig_default,
  getConfig2
} from "./chunk-R6F3TVG2.js";
import {
  __name
} from "./chunk-XLVYR3CC.js";
import {
  __async
} from "./chunk-WME3VOFU.js";

// node_modules/mermaid/dist/chunks/mermaid.core/chunk-ZZ45TVLE.mjs
var getSubGraphTitleMargins = __name(({
  flowchart
}) => {
  const subGraphTitleTopMargin = flowchart?.subGraphTitleMargin?.top ?? 0;
  const subGraphTitleBottomMargin = flowchart?.subGraphTitleMargin?.bottom ?? 0;
  const subGraphTitleTotalMargin = subGraphTitleTopMargin + subGraphTitleBottomMargin;
  return {
    subGraphTitleTopMargin,
    subGraphTitleBottomMargin,
    subGraphTitleTotalMargin
  };
}, "getSubGraphTitleMargins");
function configureLabelImages(container, labelText) {
  return __async(this, null, function* () {
    const images = container.getElementsByTagName("img");
    if (!images || images.length === 0) {
      return;
    }
    const noImgText = labelText.replace(/<img[^>]*>/g, "").trim() === "";
    yield Promise.all(
      [...images].map(
        (img) => new Promise((res) => {
          function setupImage() {
            img.style.display = "flex";
            img.style.flexDirection = "column";
            if (noImgText) {
              const bodyFontSize = getConfig2().fontSize ? getConfig2().fontSize : window.getComputedStyle(document.body).fontSize;
              const enlargingFactor = 5;
              const [parsedBodyFontSize = defaultConfig_default.fontSize] = parseFontSize(bodyFontSize);
              const width = parsedBodyFontSize * enlargingFactor + "px";
              img.style.minWidth = width;
              img.style.maxWidth = width;
            } else {
              img.style.width = "100%";
            }
            res(img);
          }
          __name(setupImage, "setupImage");
          setTimeout(() => {
            if (img.complete) {
              setupImage();
            }
          });
          img.addEventListener("error", setupImage);
          img.addEventListener("load", setupImage);
        })
      )
    );
  });
}
__name(configureLabelImages, "configureLabelImages");

export {
  getSubGraphTitleMargins,
  configureLabelImages
};
//# sourceMappingURL=chunk-PGOY7VLQ.js.map
