import {
  p
} from "./chunk-QDHQLVUH.js";
import {
  $,
  $n,
  AI,
  Ag,
  Ai,
  At,
  BI,
  CI,
  DI,
  EI,
  Et,
  FI,
  Fe,
  Gn,
  Gt,
  Jc,
  LI,
  NI,
  OI,
  Oa,
  PI,
  PM,
  Pn,
  R,
  RI,
  Rt,
  SI,
  Se,
  Su,
  T,
  U,
  UI,
  VI,
  W,
  WI,
  Wt,
  Xi,
  YI,
  Zt,
  _spritejs$Ellipse,
  _spritejs$Group,
  _spritejs$Label,
  _spritejs$Layer,
  _spritejs$Path,
  _spritejs$Polyline,
  _spritejs$Rect,
  _spritejs$Scene,
  _spritejs$Sprite,
  _t,
  ar,
  bI,
  cn,
  fg,
  jI,
  jr,
  kI,
  me,
  nt,
  ot,
  pe,
  q,
  require_clipper,
  require_lib,
  require_lineclip,
  require_lz_string,
  st,
  un,
  vI,
  ve,
  x,
  xI,
  yt,
  zI
} from "./chunk-35EV6RPG.js";
import "./chunk-M62YKLN3.js";
import {
  require_lodash,
  require_react,
  require_react_dom,
  require_white_web_sdk
} from "./chunk-LV5RZA64.js";
import "./chunk-SMZDIAJL.js";
import {
  __async,
  __objRest,
  __spreadProps,
  __spreadValues,
  __superGet,
  __toESM
} from "./chunk-WME3VOFU.js";

// node_modules/@netless/appliance-plugin/dist/index-BZIAL5Sa.mjs
var import_clipper_lib = __toESM(require_clipper(), 1);
var import_lineclip = __toESM(require_lineclip(), 1);
var import_lz_string = __toESM(require_lz_string(), 1);
var import_xss = __toESM(require_lib(), 1);
var import_lodash = __toESM(require_lodash(), 1);
var import_white_web_sdk = __toESM(require_white_web_sdk(), 1);
var import_react_dom = __toESM(require_react_dom(), 1);
var import_react = __toESM(require_react(), 1);
var _e = Object.defineProperty;
var Ve = (T2, i, t) => i in T2 ? _e(T2, i, { enumerable: true, configurable: true, writable: true, value: t }) : T2[i] = t;
var y = (T2, i, t) => Ve(T2, typeof i != "symbol" ? i + "" : i, t);
var ws = Rt;
var gs = yt;
var ks = "[object Number]";
function Ss(T2) {
  return typeof T2 == "number" || gs(T2) && ws(T2) == ks;
}
var Ts = Ss;
var H = Pn(Ts);
function mt(T2, i = true) {
  const t = T2.length;
  if (t < 2)
    return "";
  let e = T2[0], s = T2[1];
  if (t === 2)
    return `M${BI(e)}L${BI(s)}`;
  let o = "";
  for (let r = 2, n = t - 1; r < n; r++)
    e = T2[r], s = T2[r + 1], o += WI(e, s);
  return i ? `M${WI(T2[0], T2[1])}Q${BI(T2[1])}${WI(
    T2[1],
    T2[2]
  )}T${o}${WI(T2[t - 1], T2[0])}${WI(T2[0], T2[1])}Z` : `M${BI(T2[0])}Q${BI(T2[1])}${WI(T2[1], T2[2])}${T2.length > 3 ? "T" : ""}${o}L${BI(T2[t - 1])}`;
}
var et = class et2 {
  constructor(i) {
    y(this, "maxImageWidth", Se.pencilEraser.maxImageWidth);
    y(this, "maxImageHeight", Se.pencilEraser.maxImageHeight);
    y(this, "syncUnitTime", Se.syncOpt.interval);
    y(this, "vNodes");
    y(this, "drawLayer");
    y(this, "fullLayer");
    y(this, "workId");
    y(this, "isDelete", false);
    const { vNodes: t, fullLayer: e, drawLayer: s, workId: o, toolsOpt: r } = i;
    this.vNodes = t, this.fullLayer = e, this.drawLayer = s, this.workId = o, this.syncUnitTime = r.syncUnitTime || this.syncUnitTime;
  }
  get baseConsumeResult() {
    return {
      workId: this.workId,
      toolsType: this.toolsType,
      opt: this.workOptions
    };
  }
  filterSamePoints(i, t = 0.01) {
    return i.reduce((e, s) => {
      const o = e[e.length - 1];
      return (s && !o || s && o && !s.isNear(o, t)) && e.push(s), e;
    }, []);
  }
  /** 设置工作id */
  setWorkId(i) {
    this.workId = i;
  }
  getWorkId() {
    return this.workId;
  }
  /** 获取工作选项配置 */
  getWorkOptions() {
    return this.workOptions;
  }
  /** 设置工作选项配置 */
  setWorkOptions(i) {
    var s, o, r;
    this.workOptions = i, this.syncUnitTime = i.syncUnitTime || this.syncUnitTime;
    const t = (s = this.workId) == null ? void 0 : s.toString(), e = t && ((o = this.vNodes) == null ? void 0 : o.get(t)) || void 0;
    t && e && (e.opt = i, (r = this.vNodes) == null || r.setInfo(t, e));
  }
  /** 更新服务端同步配置,返回绘制结果 */
  updataOptService(i) {
    var s, o;
    let t;
    const e = (s = this.workId) == null ? void 0 : s.toString();
    if (e && i) {
      const r = this.fullLayer.getElementsByName(e) || this.drawLayer && this.drawLayer.getElementsByName(e) || [];
      if (r.length !== 1)
        return;
      const n = r[0], { pos: a, zIndex: c, scale: l, angle: h, translate: p2 } = i, d = {};
      H(c) && (d.zIndex = c), a && (d.pos = [a[0], a[1]]), l && (d.scale = l), h && (d.rotate = h), p2 && (d.translate = p2), n.attr(d);
      const u = n == null ? void 0 : n.getBoundingClientRect();
      return u && (t = SI(t, {
        x: Math.floor(u.x - et2.SafeBorderPadding),
        y: Math.floor(u.y - et2.SafeBorderPadding),
        w: Math.floor(u.width + et2.SafeBorderPadding * 2),
        h: Math.floor(u.height + et2.SafeBorderPadding * 2)
      })), (o = this.vNodes) == null || o.setInfo(e, {
        rect: t,
        centerPos: a
      }), t;
    }
  }
  drawEraserlines(i, t) {
    const { group: e, eraserlines: s, pos: o, layer: r } = i, n = r.parent;
    if (t)
      try {
        const c = (r.renderer.glRenderer || r.renderer.canvasRenderer).options.displayRatio, l = e.getBoundingClientRect(), h = l.width * c * r.worldScaling[0], p2 = l.height * c * r.worldScaling[1];
        let d = 1;
        (h > this.maxImageWidth || p2 > this.maxImageHeight) && (d = Math.min(
          this.maxImageWidth / h,
          this.maxImageHeight / p2
        ));
        let u = r.getAttribute("scale");
        u = [u[0] * d, u[1] * d];
        const f = r.parent.parent, m = r.getAttribute("translate"), g = this.createVmRenderNode(
          `${this.workId}_bitMapLayer`,
          f,
          {
            offscreen: n.offscreen,
            width: n.width,
            height: n.height,
            contextType: "2d",
            autoRender: false,
            bufferSize: 500
          },
          u,
          m
        ), I = e.cloneNode(true);
        if (!s || !s.length)
          return;
        this.addEraserlines(I, s, o, false), g.appendChild(I);
        const S = g.parent;
        S.render();
        const v = g.renderer.canvasRenderer, L = v.context;
        if (!L)
          return;
        const C = v.options.displayRatio, N = I.getBoundingClientRect(), R2 = {
          x: N.x,
          y: N.y,
          w: N.width,
          h: N.height
        }, P = {
          x: 0,
          y: 0,
          w: f.width,
          h: f.height
        };
        if (FI(R2, P) === Gt.outside) {
          Q(I, S), f.removeChild(S), this.isDelete = true;
          return;
        }
        const D = un(R2);
        D.x = Math.floor(Math.max(R2.x, 0)), D.y = Math.floor(Math.max(R2.y, 0)), D.w = Math.min(R2.x + R2.w, f.width) - D.x, D.h = Math.min(R2.y + R2.h, f.height) - D.y;
        const B = this.getGroupRect(D, C), F = B.x, U2 = B.y, V = B.w, Z = B.h;
        let K = L.getImageData(F, U2, V, Z);
        if (this.isTransparentRectByCanvas(K)) {
          Q(I, S), f.removeChild(S), this.isDelete = true, K = null;
          return;
        }
        Q(e, n);
        const gt = this.createSpriteNode({
          imageData: K,
          safariRect: D,
          originRect: R2,
          worldScaling: g.worldScaling
        });
        gt && e.append(gt), Q(I, S), f.removeChild(S), n.deleteTexture(K), K = null;
      } catch (a) {
        console.error("[BaseShapeTool] drawEraserlines error:", a);
        return;
      }
  }
  createVmRenderNode(i, t, e, s, o) {
    const { width: r, height: n } = e, a = `bitMap-${i}`, c = t.layer(a, e), l = new _spritejs$Group({
      anchor: [0.5, 0.5],
      pos: [r * 0.5, n * 0.5],
      size: [r, n],
      name: "viewport",
      id: i
    });
    return l.setAttribute("scale", s), l.setAttribute("translate", o), c.append(l), l;
  }
  getGroupRect(i, t) {
    const e = Math.floor(i.x * t), s = Math.floor(i.y * t), o = Math.floor(i.w * t), r = Math.floor(i.h * t);
    return {
      x: e,
      y: s,
      w: o,
      h: r
    };
  }
  addEraserlines(i, t, e, s = true) {
    for (const o of t) {
      const { thickness: r, op: n } = o;
      for (const a of n) {
        const c = a.map((u, f) => f % 2 ? u - e[1] : u - e[0]), l = this.computEraserPoints(c, r), h = mt(l, true), p2 = {
          pos: [0, 0],
          d: h,
          fillColor: "rgba(0,0,0,1)"
        }, d = new _spritejs$Path(p2);
        d.addEventListener("beforerender", ({ detail: u }) => {
          const f = u.context;
          s ? f.blendFuncSeparate(f.ZERO, f.ZERO, f.ZERO, f.ZERO) : f.globalCompositeOperation = "destination-out";
        }), d.addEventListener("afterrender", ({ detail: u }) => {
          const f = u.context;
          s ? f.blendFuncSeparate(
            f.SRC_ALPHA,
            f.ONE_MINUS_SRC_ALPHA,
            f.ONE,
            f.ONE_MINUS_SRC_ALPHA
          ) : f.globalCompositeOperation = "source-over";
        }), i.append(d);
      }
    }
  }
  isTransparentRectByCanvas(i) {
    let t = true;
    for (let e = 0; e < i.data.length; e += 4)
      if (i.data[e + 3] !== 0) {
        t = false;
        break;
      }
    return t;
  }
  createSpriteNode(i) {
    const { imageData: t, worldScaling: e, safariRect: s, originRect: o } = i;
    let r;
    if (typeof OffscreenCanvas == "function") {
      r = new OffscreenCanvas(t.width, t.height);
      const l = r.getContext("2d");
      l && l.putImageData(t, 0, 0);
    } else if (typeof document < "u") {
      r = document.createElement("canvas"), r.width = t.width, r.height = t.height;
      const l = r.getContext("2d");
      l && l.putImageData(t, 0, 0);
    }
    if (!r)
      return console.warn("Failed to create imageEraserBitmap Sprite"), null;
    const n = [
      Math.floor(o.w / e[0]),
      Math.floor(o.h / e[1])
    ], a = [
      Math.floor((s.x - o.x) / e[0]),
      Math.floor((s.y - o.y) / e[1]),
      Math.floor(s.w / e[0]),
      Math.floor(s.h / e[1])
    ], c = {
      name: "eraserTexture",
      anchor: [0.5, 0.5],
      pos: [0, 0],
      size: n,
      texture: r,
      textureRect: a
    };
    return new _spritejs$Sprite(c);
  }
  computEraserPoints(i, t) {
    const e = Math.ceil(t / 2);
    return i.length === 2 ? this.computDot(i, e) : this.computLine(i, e);
  }
  computDot(i, t) {
    const e = new ve(i[0], i[1]);
    return ve.GetDotStroke(e, t, 8);
  }
  computLine(i, t) {
    const e = [], s = [];
    let o, r;
    for (let n = 0; n < i.length; n += 2) {
      const a = new ve(i[n], i[n + 1]);
      let c;
      if (n == i.length - 2) {
        const l = new ve(i[n - 2], i[n - 1]);
        c = U.Sub(a, l).uni(), o = a;
      } else {
        n === 0 && (r = a);
        const l = new ve(i[n + 2], i[n + 3]);
        c = U.Sub(l, a).uni();
      }
      if (c) {
        const l = U.Per(c).mul(t);
        e.push(ve.Sub(a, l)), s.push(ve.Add(a, l));
      }
    }
    if (o && r) {
      const n = ve.GetSemicircleStroke(
        o,
        e[e.length - 1],
        -1,
        8
      ), a = ve.GetSemicircleStroke(
        r,
        s[0],
        -1,
        8
      );
      return e.concat(n, s.reverse(), a);
    }
    return [];
  }
  replace(i, t, e) {
    var o;
    if (!t) {
      e && i.append(e);
      return;
    }
    const s = i.getElementsByName(t);
    if (s.length)
      for (const r of s)
        e ? oo(e, r, i) : (r.remove(), q2(r, i.parent));
    else e && i.append(e);
    this.fullLayer !== this.drawLayer && (this.fullLayer === i ? (o = this.drawLayer) == null || o.getElementsByName(t).forEach((n) => {
      var a;
      n.remove(), q2(n, (a = this.drawLayer) == null ? void 0 : a.parent);
    }) : this.fullLayer.getElementsByName(t).forEach((n) => {
      n.remove(), q2(n, this.fullLayer.parent);
    }));
  }
  removeDrawCountNodes(i, t) {
    const e = [];
    i.getElementsByName(this.workId).forEach((s) => {
      s.id && t && Number(s.id) < t && e.push(s);
    });
    for (const s of e)
      s.remove(), q2(s, i.parent);
  }
  static updateNodeOpt(i) {
    var I;
    const { node: t, opt: e, vNodes: s, willSerializeData: o, targetNode: r } = i, {
      zIndex: n,
      translate: a,
      angle: c,
      originPoint: l,
      scenePoint: h,
      scale: p2,
      pointMap: d,
      thickness: u
    } = e;
    let f;
    const m = r && un(r) || s.get(t.name);
    if (!m) return;
    H(n) && (t.setAttribute("zIndex", n), m.opt.zIndex = n);
    const g = t.parent;
    if (g) {
      if (l && p2 && r) {
        const S = [m.op[0], m.op[1]];
        EI(m.op, h, p2, a);
        const v = [m.op[0], m.op[1]], L = [
          v[0] - S[0],
          v[1] - S[1]
        ];
        if (m.centerPos = [
          m.centerPos[0] + L[0],
          m.centerPos[1] + L[1]
        ], m.opt.translate = void 0, m.opt.scale = void 0, m.opt.eraserlines)
          for (let C = 0; C < m.opt.eraserlines.length; C++) {
            const { op: N, thickness: R2 } = m.opt.eraserlines[C];
            m.opt.eraserlines[C].thickness = Math.round(
              R2 * Math.max(p2[0], p2[1])
            );
            for (let P = 0; P < N.length; P++)
              LI(N[P], h, p2, a);
          }
      } else if (a)
        if (t.setAttribute("translate", a), m.opt.translate = a, r) {
          const S = [
            a[0] * g.worldScaling[0],
            a[1] * g.worldScaling[1]
          ];
          f = jI(m.rect, S), m.rect = f;
        } else {
          const S = et2.getRectFromLayer(g, t.name);
          m.rect = S || m.rect;
        }
      else if (H(c))
        if (t.setAttribute("rotate", c), m.opt.rotate = c, r)
          f = DI(m.rect, c), m.rect = f;
        else {
          const S = et2.getRectFromLayer(g, t.name);
          m.rect = S || m.rect;
        }
      if (d) {
        const S = d.get(t.name);
        if (S)
          for (let v = 0, L = 0; v < m.op.length; v += 3, L++)
            m.op[v] = S[L][0], m.op[v + 1] = S[L][1];
      }
      if (u && ((I = m == null ? void 0 : m.opt) != null && I.thickness) && (m.opt.thickness = u), o && !(l && p2 && r)) {
        if (a) {
          const S = m.op.map((v, L) => {
            const C = L % 3;
            return C === 0 ? v + a[0] : C === 1 ? v + a[1] : v;
          });
          if (m.op = S, m.centerPos = [
            m.centerPos[0] + a[0],
            m.centerPos[1] + a[1]
          ], m != null && m.opt && (m.opt.translate = void 0), m.opt.eraserlines)
            for (let v = 0; v < m.opt.eraserlines.length; v++) {
              const { op: L } = m.opt.eraserlines[v];
              for (let C = 0; C < L.length; C++) {
                const N = L[C].map((R2, P) => P % 2 ? R2 + a[1] : R2 + a[0]);
                m.opt.eraserlines[v].op[C] = N;
              }
            }
        } else if (H(c)) {
          const S = m.op;
          if (bI(S, m.centerPos, c), m.op = S, m != null && m.opt && (m.opt.rotate = void 0), m.opt.eraserlines)
            for (let v = 0; v < m.opt.eraserlines.length; v++) {
              const { op: L } = m.opt.eraserlines[v];
              for (let C = 0; C < L.length; C++)
                AI(L[C], m.centerPos, c);
            }
        }
      }
      m && s.setInfo(t.name, m);
    }
  }
  static getCenterPos(i, t) {
    const { worldPosition: e, worldScaling: s } = t;
    return [
      (i.x + i.w / 2 - e[0]) / s[0],
      (i.y + i.h / 2 - e[1]) / s[1]
    ];
  }
  static getRectFromLayer(i, t) {
    const e = i.getElementsByName(t)[0];
    if (e) {
      const s = e.getBoundingClientRect();
      return {
        x: Math.floor(s.x - et2.SafeBorderPadding),
        y: Math.floor(s.y - et2.SafeBorderPadding),
        w: Math.floor(s.width + et2.SafeBorderPadding * 2),
        h: Math.floor(s.height + et2.SafeBorderPadding * 2)
      };
    }
  }
  static isWillRefresh(i) {
    const { toolsType: t, opt: e, node: s, updateOpt: o, willSerializeData: r } = i;
    return !!(r && (o.angle || o.translate) || o.thickness && e.thickness && e.thickness !== o.thickness || o.strokeType && e.strokeType && e.strokeType !== o.strokeType || o.originPoint && o.scenePoint && o.scale || o.pointMap && o.pointMap.has(s.name) || t === T.Text && (o.fontSize || o.translate || o.textInfos && o.textInfos.get(s.name)) || t === T.BackgroundSVG && (o.translate || o.scale) || t === T.Image && (o.angle || o.translate || o.scale || o.strokeColor && e.type === Zt.Iconify) || t === o.toolsType && o.willRefresh || e.eraserlines && e.eraserlines.length && (o.strokeColor || o.fillColor));
  }
};
y(et, "SafeBorderPadding", 10);
var b = et;
var re = Object.freeze([
  Object.freeze({ width: 18, height: 26 }),
  Object.freeze({ width: 26, height: 34 }),
  Object.freeze({ width: 34, height: 50 }),
  Object.freeze({ width: 48, height: 74 })
]);
function Re(T2, i, t = 0.01) {
  return Math.abs(T2[0] - i[0]) < t && Math.abs(T2[1] - i[1]) < t;
}
function vs(T2, i = 0.01) {
  if (T2.length === 0) return [];
  const t = [[T2[0][0], T2[0][1]]];
  for (let e = 1; e < T2.length; e++)
    Re([T2[e][0], T2[e][1]], t[t.length - 1], i) || t.push([T2[e][0], T2[e][1]]);
  return t;
}
function Is(T2, i = 0.01) {
  if (T2.length <= 2) return T2;
  const t = T2[0], e = T2[T2.length - 1];
  return Re(t, e, i) ? T2.slice(0, -1) : T2;
}
function Ps(T2) {
  const i = T2.reduce((e, s) => e + s[0], 0) / T2.length, t = T2.reduce((e, s) => e + s[1], 0) / T2.length;
  return [i, t];
}
function me2(T2, i) {
  return Math.atan2(i[1] - T2[1], i[0] - T2[0]);
}
function Ws(T2, i = 0.01) {
  if (T2.length <= 2) return T2;
  const t = [];
  for (const s of T2)
    t.some(
      (r) => Math.abs(r[0] - s[0]) < i && Math.abs(r[1] - s[1]) < i
    ) || t.push([s[0], s[1]]);
  const e = Ps(t);
  return t.sort((s, o) => {
    const r = me2(e, s), n = me2(e, o);
    return r - n;
  });
}
function Ls(T2, i) {
  const t = i[0].x < i[i.length - 1].x;
  return T2.map((s) => {
    const r = s.map((n) => {
      let a = -1, c = 1 / 0, l = 0;
      for (let h = 0; h < i.length; h++) {
        const p2 = i[h], d = Math.sqrt(
          Math.pow(p2.x - n[0], 2) + Math.pow(p2.y - n[1], 2)
        );
        d < c && (c = d, a = h, l = p2.z);
      }
      return { point: n, index: a, bestZ: l };
    }).sort((n, a) => n.index - a.index).map((n) => [...n.point, n.bestZ]);
    return t && r[0][0] > r[r.length - 1][0] || !t && r[0][0] < r[r.length - 1][0] ? r.reverse() : r;
  }).sort((s, o) => {
    const r = s[0][0], n = o[0][0];
    return t ? r - n : n - r;
  });
}
function Cs(T2, i, t = 0.01) {
  const s = T2.map((h) => [h.x, h.y]), o = (h, p2) => h.map(([d, u]) => ({
    X: Math.round(d * p2),
    Y: Math.round(u * p2)
  })), r = new import_clipper_lib.default.ClipperOffset();
  r.AddPath(
    o(s, 1e3),
    import_clipper_lib.default.JoinType.jtRound,
    import_clipper_lib.default.EndType.etOpenButt
  );
  const n = [];
  r.Execute(n, 1);
  const a = new import_clipper_lib.default.Clipper();
  a.AddPaths(n, import_clipper_lib.default.PolyType.ptSubject, true), i.forEach((h) => {
    a.AddPath(
      o(h, 1e3),
      import_clipper_lib.default.PolyType.ptClip,
      true
    );
  });
  const c = new import_clipper_lib.default.Paths();
  a.Execute(
    import_clipper_lib.default.ClipType.ctDifference,
    c,
    import_clipper_lib.default.PolyFillType.pftNonZero,
    import_clipper_lib.default.PolyFillType.pftNonZero
  );
  const l = c.map(
    (h) => Is(
      Ws(
        vs(
          h.map(
            (p2) => [p2.X / 1e3, p2.Y / 1e3]
          ),
          t
        ),
        t
      ),
      t
    )
  ).filter((h) => h.length >= 2);
  return Ls(l, T2);
}
var Ne = class extends b {
  constructor(t) {
    super(t);
    y(this, "canRotate", true);
    y(this, "scaleType", Fe.all);
    y(this, "toolsType", T.Pencil);
    y(this, "syncTimestamp");
    y(this, "syncIndex", 0);
    y(this, "tmpPoints", []);
    y(this, "MAX_REPEAR", 10);
    y(this, "uniThickness");
    y(this, "workOptions");
    y(this, "centerPos", [0, 0]);
    this.workOptions = t.toolsOpt, this.uniThickness = this.MAX_REPEAR / this.workOptions.thickness / 10, this.syncTimestamp = 0;
  }
  /** 批量合并消费本地数据,返回绘制结果 */
  combineConsume() {
    var n;
    const t = (n = this.workId) == null ? void 0 : n.toString();
    if (this.tmpPoints.length < 2)
      return {
        type: R.None
      };
    const e = this.transformDataAll(true), s = {
      name: t
    };
    let o;
    const r = this.drawLayer || this.fullLayer;
    return e.length && (o = this.draw({ attrs: s, tasks: e, replaceId: t, layer: r })), {
      rect: o,
      type: R.DrawWork,
      dataType: q.Local
    };
  }
  setWorkOptions(t) {
    super.setWorkOptions(t), this.syncTimestamp = Date.now();
  }
  consume(t) {
    const {
      data: e,
      isFullWork: s,
      isSubWorker: o,
      isMainThread: r,
      drawCount: n,
      removeDrawCount: a,
      isSimpleWorker: c
    } = t, { workId: l, syncUnitTime: h } = e;
    h && (this.syncUnitTime = h);
    const { tasks: p2, effects: d, consumeIndex: u } = this.transformData(e, false);
    this.syncIndex = Math.min(
      this.syncIndex,
      u,
      Math.max(0, this.tmpPoints.length - 2)
    );
    const f = {
      name: l,
      id: H(n) && n.toString() || void 0
    };
    let m, g = false;
    const I = this.syncIndex;
    if (this.syncTimestamp === 0 && (this.syncTimestamp = Date.now()), p2.length && (p2[0].taskId - this.syncTimestamp > this.syncUnitTime && (g = true, this.syncTimestamp = p2[0].taskId, this.syncIndex = this.tmpPoints.length), o || r || c)) {
      const v = s ? this.fullLayer : this.drawLayer || this.fullLayer;
      m = this.draw({ attrs: f, tasks: p2, effects: d, layer: v, removeDrawCount: a });
    }
    if (c) {
      const v = [];
      return this.tmpPoints.slice(I).forEach((L) => {
        v.push(L.x, L.y, this.computRadius(L.z, this.workOptions.thickness));
      }), __spreadProps(__spreadValues({}, this.baseConsumeResult), {
        type: R.DrawWork,
        dataType: q.Local,
        op: g ? v : void 0,
        index: g ? I * 3 : void 0,
        rect: m,
        updateNodeOpt: {
          useAnimation: true
        }
      });
    }
    if (o)
      return u > 10 && this.tmpPoints.splice(0, u - 10), {
        rect: m,
        type: R.DrawWork,
        dataType: q.Local
      };
    const S = [];
    return this.tmpPoints.slice(I).forEach((v) => {
      S.push(v.x, v.y, this.computRadius(v.z, this.workOptions.thickness));
    }), __spreadProps(__spreadValues({}, this.baseConsumeResult), {
      type: R.DrawWork,
      dataType: q.Local,
      rect: m,
      op: g ? S : void 0,
      index: g ? I * 3 : void 0,
      updateNodeOpt: {
        useAnimation: true
      }
    });
  }
  consumeAll(t) {
    var l;
    const e = this.workId;
    if (t.data) {
      const { op: h, workState: p2 } = t.data;
      h != null && h.length && p2 === x.Done && this.workOptions.strokeType === me.Stroke && this.updateTempPointsWithPressureWhenDone(h);
    }
    const s = this.transformDataAll(true), o = {
      name: e
    };
    let r;
    const n = this.fullLayer;
    if (s.length && (r = this.draw({ attrs: o, tasks: s, replaceId: e, layer: n })), this.tmpPoints.length < 2)
      return this.replace(n, e), {
        type: R.RemoveNode,
        removeIds: [e],
        rect: r
      };
    const a = [];
    this.tmpPoints.map((h) => {
      a.push(h.x, h.y, h.z);
    }), this.syncTimestamp = 0, delete this.workOptions.syncUnitTime;
    const c = _t(a);
    return (l = this.vNodes) == null || l.setInfo(e, {
      rect: r,
      op: a,
      opt: this.workOptions,
      toolsType: this.toolsType,
      scaleType: this.scaleType,
      canRotate: this.canRotate,
      centerPos: r && b.getCenterPos(r, n)
    }), __spreadProps(__spreadValues({}, this.baseConsumeResult), {
      rect: r,
      type: R.FullWork,
      dataType: q.Local,
      ops: c,
      updateNodeOpt: {
        pos: this.centerPos,
        useAnimation: true
      }
    });
  }
  clearTmpPoints() {
    this.tmpPoints.length = 0, this.syncTimestamp = 0, this.syncIndex = 0;
  }
  consumeService(t) {
    var h, p2;
    const {
      op: e,
      isFullWork: s,
      replaceId: o,
      workState: r = x.Done
    } = t;
    this.tmpPoints.length = 0;
    for (let d = 0; d < e.length; d += 3) {
      const u = new ve(e[d], e[d + 1], e[d + 2]);
      if (this.tmpPoints.length > 0) {
        const f = this.tmpPoints[this.tmpPoints.length - 1], m = U.Sub(u, f).uni();
        u.setv(m);
      }
      this.tmpPoints.push(u);
    }
    if (this.tmpPoints.length < 2)
      return;
    const n = this.transformDataAll(true), a = (h = this.workId) == null ? void 0 : h.toString(), c = {
      name: a
    };
    let l;
    if (a && n.length) {
      const d = s ? this.fullLayer : this.drawLayer || this.fullLayer;
      l = this.draw({
        attrs: c,
        tasks: n,
        replaceId: o,
        layer: d,
        isDrawEraserlines: r === x.Done
      }), (p2 = this.vNodes) == null || p2.setInfo(a, {
        rect: l,
        op: e,
        opt: this.workOptions,
        toolsType: this.toolsType,
        scaleType: this.scaleType,
        canRotate: this.canRotate,
        centerPos: l && b.getCenterPos(l, d)
      });
    }
    return l;
  }
  computPencilPoints(t) {
    const e = [], { op: s, eraserPolylines: o, eraserThickness: r } = t;
    this.tmpPoints.length = 0;
    for (let a = 0; a < s.length; a += 3) {
      const c = new ve(s[a], s[a + 1], s[a + 2]);
      if (this.tmpPoints.length > 0) {
        const l = this.tmpPoints[this.tmpPoints.length - 1], h = U.Sub(c, l).uni();
        c.setv(h);
      }
      this.tmpPoints.push(c);
    }
    if (this.tmpPoints.length < 2)
      return;
    const n = this.transformDataAll(true);
    if (n.length)
      for (let a = 0; a < n.length; a++) {
        const { pos: c, points: l } = n[a], h = l.map((u) => u.point.addXY(c[0], c[1])), p2 = [];
        for (const u of o) {
          const f = [];
          for (let g = 0; g < u.length; g += 2) {
            const I = new ve(u[g], u[g + 1]);
            if (f.length > 0) {
              const S = f[f.length - 1].point, v = U.Sub(I, S).uni();
              I.setv(v);
            }
            f.push({
              point: I,
              radius: r
            });
          }
          const { ps: m } = this.computStroke(f, false);
          p2.push(m.map((g) => g.XY));
        }
        Cs(
          h,
          p2,
          0.01
        ).forEach((u) => {
          e.push(u.map((f) => [f[0], f[1], f[2]]).flat(1));
        });
      }
    return e;
  }
  transformDataAll(t = true) {
    return this.getTaskPoints(
      this.tmpPoints,
      t && this.workOptions.thickness || void 0
    );
  }
  draw(t) {
    const {
      attrs: e,
      tasks: s,
      replaceId: o,
      effects: r,
      layer: n,
      removeDrawCount: a,
      isDrawEraserlines: c = true
    } = t, {
      strokeColor: l,
      strokeType: h,
      thickness: p2,
      zIndex: d,
      scale: u,
      rotate: f,
      translate: m,
      eraserlines: g,
      lineCap: I,
      lineDash: S
    } = this.workOptions;
    r != null && r.size && (r.forEach((P) => {
      var M;
      (M = n.getElementById(P + "")) == null || M.remove();
    }), r.clear()), a && this.removeDrawCountNodes(n, a);
    let v;
    const L = [], C = n.worldPosition, N = n.worldScaling;
    for (let P = 0; P < s.length; P++) {
      const { pos: M, points: D } = s[P], { ps: B, rect: F } = this.computDrawPoints(D);
      let U2;
      const V = D.length === 1;
      h === me.Stroke || V ? U2 = mt(B, true) : U2 = mt(B, false);
      const Z = {
        pos: M,
        d: U2,
        fillColor: h === me.Stroke || V ? l : void 0,
        lineDash: V ? void 0 : h === me.Dotted ? [
          S && S[0] || 1,
          (S && S[1] || 2) * p2
        ] : h === me.LongDotted ? [
          (S && S[0] || 1) * p2,
          (S && S[1] || 2) * p2
        ] : void 0,
        strokeColor: l,
        lineCap: V ? void 0 : I,
        lineWidth: h === me.Stroke || V ? 0 : p2
      };
      v = SI(v, {
        x: Math.floor(
          (F.x + M[0]) * N[0] + C[0] - b.SafeBorderPadding
        ),
        y: Math.floor(
          (F.y + M[1]) * N[1] + C[1] - b.SafeBorderPadding
        ),
        w: Math.floor(
          F.w * N[0] + 2 * b.SafeBorderPadding
        ),
        h: Math.floor(
          F.h * N[1] + 2 * b.SafeBorderPadding
        )
      }), L.push(Z);
    }
    u && (e.scale = u), f && (e.rotate = f), m && (e.translate = m);
    const R2 = new _spritejs$Group();
    if (v) {
      this.centerPos = b.getCenterPos(v, n);
      const P = h === me.Stroke && !g;
      R2.attr(__spreadProps(__spreadValues({}, e), {
        normalize: true,
        anchor: [0.5, 0.5],
        bgcolor: P ? l : void 0,
        pos: this.centerPos,
        size: [
          (v.w - 2 * b.SafeBorderPadding) / N[0],
          (v.h - 2 * b.SafeBorderPadding) / N[1]
        ],
        zIndex: d
      }));
      const M = L.map((D) => (D.pos = [
        D.pos[0] - this.centerPos[0],
        D.pos[1] - this.centerPos[1]
      ], new _spritejs$Path(D)));
      R2.append(...M), P && R2.seal(), g && (this.scaleType = Fe.proportional, this.drawEraserlines(
        {
          group: R2,
          eraserlines: g,
          pos: this.centerPos,
          layer: n
        },
        c
      )), this.replace(n, o, R2);
    }
    if (u || f || m) {
      const P = R2 == null ? void 0 : R2.getBoundingClientRect();
      P && (v = {
        x: Math.floor(P.x - b.SafeBorderPadding),
        y: Math.floor(P.y - b.SafeBorderPadding),
        w: Math.floor(P.width + b.SafeBorderPadding * 2),
        h: Math.floor(P.height + b.SafeBorderPadding * 2)
      });
    }
    return this.isDelete && R2.setAttribute("opacity", 0), v;
  }
  computDrawPoints(t) {
    return this.workOptions.strokeType === me.Stroke || t.length === 1 ? this.computStroke(t) : this.computNomal(t);
  }
  computNomal(t) {
    let e = this.workOptions.thickness;
    const s = t.map((o) => (e = Math.max(e, o.radius), o.point));
    return { ps: s, rect: jr(s, e) };
  }
  computStroke(t, e = true) {
    return t.length === 1 ? this.computDotStroke(t[0]) : this.computLineStroke(t, e);
  }
  computLineStroke(t, e = true) {
    const s = [], o = [];
    for (let a = 0; a < t.length; a++) {
      const { point: c, radius: l } = t[a];
      let h = c.v;
      a === 0 && t.length > 1 && (h = t[a + 1].point.v);
      const p2 = U.Per(h).mul(l);
      s.push(ve.Sub(c, p2)), o.push(ve.Add(c, p2));
    }
    const r = t[t.length - 1];
    if (e) {
      const a = ve.GetSemicircleStroke(
        r.point,
        s[s.length - 1],
        -1,
        8
      ), c = ve.GetSemicircleStroke(
        t[0].point,
        o[0],
        -1,
        8
      ), l = s.concat(a, o.reverse(), c);
      return { ps: l, rect: jr(l) };
    }
    const n = s.concat(o.reverse());
    return { ps: n, rect: jr(n) };
  }
  computDotStroke(t) {
    const { point: e, radius: s } = t, o = {
      x: e.x - s,
      y: e.y - s,
      w: s * 2,
      h: s * 2
    };
    return { ps: ve.GetDotStroke(e, s, 8), rect: o };
  }
  transformData(t, e) {
    const { op: s, workState: o } = t;
    let r = this.tmpPoints.length - 1, n = [];
    if (s != null && s.length && o) {
      const { strokeType: a, thickness: c } = this.workOptions, l = /* @__PURE__ */ new Set();
      r = a === me.Stroke ? this.updateTempPointsWithPressure(s, c, l) : this.updateTempPoints(s, c, l);
      const h = e ? this.tmpPoints : this.tmpPoints.slice(r);
      return n = this.getTaskPoints(h, c), { tasks: n, effects: l, consumeIndex: r };
    }
    return { tasks: n, consumeIndex: r };
  }
  /** 压力渐变公式 */
  computRadius(t, e) {
    return t * 0.03 * e + e * 0.5;
  }
  getMinZ(t, e) {
    return ((e || Math.max(1, Math.floor(t * 0.3))) - t * 0.5) * 100 / t / 3;
  }
  getTaskPoints(t, e) {
    var h;
    const s = [];
    if (t.length === 0)
      return [];
    let o = 0, r = t[0].x, n = t[0].y, a = [r, n], c = [], l = t[0].t;
    for (; o < t.length; ) {
      const p2 = t[o], d = p2.x - r, u = p2.y - n, f = p2.z, m = e ? this.computRadius(f, e) : f;
      if (c.push({
        point: new ve(d, u, f, t[o].v),
        radius: m
      }), o > 0 && o < t.length - 1) {
        const g = t[o].getAngleByPoints(
          t[o - 1],
          t[o + 1]
        );
        if (g < 60 || g > 300) {
          const I = (h = c.pop()) == null ? void 0 : h.point.clone();
          I && s.push({
            taskId: l,
            pos: a,
            points: [
              ...c,
              {
                point: I,
                radius: m
              }
            ]
          }), r = t[o].x, n = t[o].y, a = [r, n];
          const S = p2.x - r, v = p2.y - n;
          c = [
            {
              point: new ve(S, v, f),
              radius: m
            }
          ], l = Date.now();
        }
      }
      o++;
    }
    return s.push({
      taskId: l,
      pos: a,
      points: c
    }), s;
  }
  updateTempPointsWithPressure(t, e, s) {
    const o = Date.now(), r = this.tmpPoints.length;
    let n = r;
    for (let c = 0; c < t.length; c += 2) {
      n = Math.min(n, r);
      const l = this.tmpPoints.length, h = new ve(
        t[c],
        t[c + 1]
      );
      if (l === 0) {
        this.tmpPoints.push(h);
        continue;
      }
      const p2 = l - 1, d = this.tmpPoints[p2], u = U.Sub(h, d).uni();
      if (h.isNear(d, e)) {
        if (d.z < this.MAX_REPEAR) {
          if (d.setz(Math.min(d.z + 1, this.MAX_REPEAR)), n = Math.min(n, p2), l > 1) {
            let g = l - 1;
            for (; g > 0; ) {
              const I = this.tmpPoints[g].distance(
                this.tmpPoints[g - 1]
              ), S = Math.max(
                this.tmpPoints[g].z - this.uniThickness * I,
                0
              );
              if (this.tmpPoints[g - 1].z >= S)
                break;
              this.tmpPoints[g - 1].setz(S), n = Math.min(n, g - 1), g--;
            }
          }
        } else
          n = 1 / 0;
        continue;
      }
      h.setv(u);
      const f = h.distance(d), m = Math.max(d.z - this.uniThickness * f, 0);
      l > 1 && U.Equals(u, d.v, 0.02) && (m > 0 || d.z <= 0) && (s && d.t && s.add(d.t), this.tmpPoints.pop(), n = Math.min(p2, n)), h.setz(m), this.tmpPoints.push(h);
    }
    if (n === 1 / 0)
      return this.tmpPoints.length;
    let a = r;
    if (n === r) {
      a = Math.max(a - 1, 0);
      const c = this.tmpPoints[a].t;
      c && (s == null || s.add(c));
    } else {
      let c = r - 1;
      for (a = n; c >= 0; ) {
        const l = this.tmpPoints[c].t;
        if (l && (s == null || s.add(l), c <= n)) {
          a = c, c = -1;
          break;
        }
        c--;
      }
    }
    return this.tmpPoints[a].setT(o), a;
  }
  updateTempPoints(t, e, s) {
    var c;
    const o = Date.now(), r = this.tmpPoints.length;
    let n = r;
    for (let l = 0; l < t.length; l += 2) {
      const h = this.tmpPoints.length, p2 = new ve(
        t[l],
        t[l + 1]
      );
      if (h === 0) {
        this.tmpPoints.push(p2);
        continue;
      }
      const d = h - 1, u = this.tmpPoints[d], f = U.Sub(p2, u).uni();
      if (p2.isNear(u, e / 2)) {
        n = Math.min(d, n);
        continue;
      }
      U.Equals(f, u.v, 0.02) && (s && u.t && s.add(u.t), this.tmpPoints.pop(), n = Math.min(d, n)), p2.setv(f), this.tmpPoints.push(p2);
    }
    let a = r;
    if (n === r) {
      a = Math.max(a - 1, 0);
      const l = this.tmpPoints[a].t;
      l && (s == null || s.add(l));
    } else {
      let l = Math.min(r - 1, n);
      for (a = n; l >= 0; ) {
        const h = (c = this.tmpPoints[l]) == null ? void 0 : c.t;
        if (h && (s == null || s.add(h), l <= n)) {
          a = l, l = -1;
          break;
        }
        l--;
      }
    }
    return this.tmpPoints[a].setT(o), a;
  }
  updateTempPointsWithPressureWhenDone(t) {
    const { thickness: e } = this.workOptions, s = t.length, o = this.getMinZ(e);
    for (let r = 0; r < s; r += 2) {
      const n = this.tmpPoints.length, a = new ve(
        t[r],
        t[r + 1]
      );
      if (n === 0) {
        this.tmpPoints.push(a);
        continue;
      }
      const c = n - 1, l = this.tmpPoints[c], h = U.Sub(a, l).uni(), p2 = a.distance(l);
      if (n > 1 && l.z === o)
        break;
      if (a.isNear(l, e / 2)) {
        if (s < 3 && l.z < this.MAX_REPEAR && (l.setz(Math.min(l.z + 1, this.MAX_REPEAR)), n > 1)) {
          let u = n - 1;
          for (; u > 0; ) {
            const f = this.tmpPoints[u].distance(
              this.tmpPoints[u - 1]
            ), m = Math.max(
              this.tmpPoints[u].z - this.uniThickness * f,
              -e / 4
            );
            if (this.tmpPoints[u - 1].z >= m)
              break;
            this.tmpPoints[u - 1].setz(m), u--;
          }
        }
        continue;
      }
      a.setv(h);
      const d = Math.max(l.z - this.uniThickness * p2, o);
      n > 1 && U.Equals(h, l.v, 0.02) && l.z <= 0 && this.tmpPoints.pop(), a.setz(d), this.tmpPoints.push(a);
    }
  }
  static updateNodeOpt(t) {
    var h, p2;
    const { node: e, opt: s, vNodes: o } = t, { strokeColor: r, strokeType: n, lineCap: a, lineDash: c } = s, l = o.get(e.name);
    return r && (e.tagName === "GROUP" ? so(e) ? e.setAttribute("bgcolor", r) : e.children.forEach((d) => {
      d.setAttribute("strokeColor", r), d.getAttribute("fillColor") && d.setAttribute("fillColor", r);
    }) : (e.setAttribute("strokeColor", r), e.setAttribute("fillColor", r)), (h = l == null ? void 0 : l.opt) != null && h.strokeColor && (l.opt.strokeColor = r)), n && (l != null && l.opt) && ((p2 = l.opt) != null && p2.strokeType) && (l.opt.strokeType = n), a && (l != null && l.opt) && (l.opt.lineCap = a), c && (l != null && l.opt) && (l.opt.lineDash = c), l && o.setInfo(e.name, l), b.updateNodeOpt(t);
  }
};
var xe = class extends b {
  constructor(t) {
    super(t);
    y(this, "toolsType", T.LaserPen);
    y(this, "canRotate", false);
    y(this, "scaleType", Fe.none);
    y(this, "syncTimestamp");
    y(this, "syncIndex", 0);
    y(this, "tmpPoints", []);
    y(this, "workOptions");
    y(this, "consumeIndex", 0);
    this.workOptions = t.toolsOpt, this.syncTimestamp = 0;
  }
  combineConsume() {
  }
  setWorkOptions(t) {
    super.setWorkOptions(t), this.syncTimestamp = Date.now();
  }
  consume(t) {
    const { data: e, isSubWorker: s } = t, { workId: o, op: r, syncUnitTime: n } = e;
    if ((r == null ? void 0 : r.length) === 0)
      return { type: R.None };
    if (n && (this.syncUnitTime = n), this.updateTempPoints(r || []), this.consumeIndex > this.tmpPoints.length - 4)
      return { type: R.None };
    const { strokeColor: a, thickness: c, strokeType: l, lineDash: h, lineCap: p2 } = this.workOptions, d = jr(this.tmpPoints, c);
    let u = false;
    const f = this.syncIndex, m = this.tmpPoints.slice(this.consumeIndex);
    this.consumeIndex = this.tmpPoints.length - 1, this.syncTimestamp === 0 && (this.syncTimestamp = Date.now());
    const g = {
      name: o == null ? void 0 : o.toString(),
      opacity: 1,
      lineDash: l === me.Dotted ? [
        h && h[0] || 1,
        (h && h[1] || 2) * c
      ] : l === me.LongDotted ? [
        (h && h[0] || 1) * c,
        (h && h[1] || 2) * c
      ] : void 0,
      strokeColor: a,
      lineCap: p2,
      lineWidth: c,
      anchor: [0.5, 0.5]
    }, I = this.getTaskPoints(m);
    if (I.length) {
      const v = Date.now();
      v - this.syncTimestamp > this.syncUnitTime && (u = true, this.syncTimestamp = v, this.syncIndex = this.tmpPoints.length), s && this.draw({
        attrs: g,
        tasks: I,
        isDot: false,
        layer: this.drawLayer || this.fullLayer
      });
    }
    const S = [];
    return this.tmpPoints.slice(f).forEach((v) => {
      S.push(v.x, v.y);
    }), __spreadValues({
      rect: {
        x: d.x * this.fullLayer.worldScaling[0] + this.fullLayer.worldPosition[0],
        y: d.y * this.fullLayer.worldScaling[1] + this.fullLayer.worldPosition[1],
        w: d.w * this.fullLayer.worldScaling[0],
        h: d.h * this.fullLayer.worldScaling[1]
      },
      type: R.DrawWork,
      dataType: q.Local,
      op: u ? S : void 0,
      index: u ? f * 2 : void 0
    }, this.baseConsumeResult);
  }
  consumeAll() {
    var r;
    const t = (r = this.workId) == null ? void 0 : r.toString();
    let e;
    if (this.tmpPoints.length - 1 > this.consumeIndex) {
      let n = this.tmpPoints.slice(this.consumeIndex);
      const a = n.length === 1, { strokeColor: c, thickness: l, strokeType: h } = this.workOptions;
      if (a) {
        const u = this.computDotStroke({
          point: n[0],
          radius: l / 2
        });
        n = u.ps, e = u.rect;
      } else
        e = jr(this.tmpPoints, l);
      const p2 = {
        name: t == null ? void 0 : t.toString(),
        fillColor: a ? c : void 0,
        opacity: 1,
        lineDash: h === me.Dotted && !a ? [1, l * 2] : h === me.LongDotted && !a ? [l, l * 2] : void 0,
        strokeColor: c,
        lineCap: a ? void 0 : "round",
        lineWidth: a ? 0 : l,
        anchor: [0.5, 0.5]
      }, d = this.getTaskPoints(n);
      d.length && this.draw({
        attrs: p2,
        tasks: d,
        isDot: a,
        layer: this.drawLayer || this.fullLayer
      });
    }
    const s = [];
    this.tmpPoints.forEach((n) => {
      s.push(n.x, n.y);
    });
    const o = _t(s);
    return __spreadValues({
      rect: e && {
        x: e.x * this.fullLayer.worldScaling[0] + this.fullLayer.worldPosition[0],
        y: e.y * this.fullLayer.worldScaling[1] + this.fullLayer.worldPosition[1],
        w: e.w * this.fullLayer.worldScaling[0],
        h: e.h * this.fullLayer.worldScaling[1]
      },
      type: R.FullWork,
      dataType: q.Local,
      ops: o,
      index: this.syncIndex * 2
    }, this.baseConsumeResult);
  }
  clearTmpPoints() {
    this.tmpPoints.length = 0, this.syncTimestamp = 0, this.syncIndex = 0;
  }
  consumeService(t) {
    var f;
    const { op: e, replaceId: s, isFullWork: o } = t, { strokeColor: r, thickness: n, strokeType: a } = this.workOptions;
    if (!e.length) {
      const m = jr(this.tmpPoints, n);
      return {
        x: m.x * this.fullLayer.worldScaling[0] + this.fullLayer.worldPosition[0],
        y: m.y * this.fullLayer.worldScaling[1] + this.fullLayer.worldPosition[1],
        w: m.w * this.fullLayer.worldScaling[0],
        h: m.h * this.fullLayer.worldScaling[1]
      };
    }
    const c = Math.max(0, this.tmpPoints.length - 1);
    this.updateTempPoints(e || []);
    let l, h = this.tmpPoints.slice(c);
    const p2 = h.length === 1;
    if (p2) {
      const m = this.computDotStroke({
        point: h[0],
        radius: n / 2
      });
      h = m.ps, l = m.rect;
    } else
      l = jr(this.tmpPoints, n);
    const d = {
      name: (f = this.workId) == null ? void 0 : f.toString(),
      fillColor: p2 ? r : void 0,
      opacity: 1,
      lineDash: a === me.Dotted && !p2 ? [1, n * 2] : a === me.LongDotted && !p2 ? [n, n * 2] : void 0,
      strokeColor: r,
      lineCap: p2 ? void 0 : "round",
      lineWidth: p2 ? 0 : n,
      anchor: [0.5, 0.5]
    }, u = this.getTaskPoints(h);
    if (u.length) {
      const m = o ? this.fullLayer : this.drawLayer || this.fullLayer;
      this.draw({ attrs: d, tasks: u, isDot: p2, replaceId: s, layer: m });
    }
    return {
      x: l.x * this.fullLayer.worldScaling[0] + this.fullLayer.worldPosition[0],
      y: l.y * this.fullLayer.worldScaling[1] + this.fullLayer.worldPosition[1],
      w: l.w * this.fullLayer.worldScaling[0],
      h: l.h * this.fullLayer.worldScaling[1]
    };
  }
  computDotStroke(t) {
    const { point: e, radius: s } = t, o = {
      x: e.x - s,
      y: e.y - s,
      w: s * 2,
      h: s * 2
    };
    return { ps: ve.GetDotStroke(e, s, 8), rect: o };
  }
  updateTempPoints(t) {
    const e = this.tmpPoints.length;
    for (let s = 0; s < t.length; s += 2) {
      if (e) {
        const o = this.tmpPoints.slice(-1)[0];
        o && o.x === t[s] && o.y === t[s + 1] && this.tmpPoints.pop();
      }
      this.tmpPoints.push(
        new ve(t[s], t[s + 1])
      );
    }
  }
  draw(t) {
    return __async(this, null, function* () {
      const { attrs: e, tasks: s, isDot: o, layer: r } = t, { duration: n } = this.workOptions;
      for (const a of s) {
        const c = new _spritejs$Path(), { pos: l, points: h } = a;
        let p2;
        o ? p2 = mt(h, true) : p2 = mt(h, false), c.attr(__spreadProps(__spreadValues({}, e), {
          pos: l,
          d: p2
        })), r.appendChild(c), c.transition(n).attr({
          scale: o ? [0.1, 0.1] : [1, 1],
          lineWidth: o ? 0 : 1
        }).then(() => {
          c.remove();
        });
      }
    });
  }
  getTaskPoints(t) {
    var c;
    const e = [];
    if (t.length === 0)
      return [];
    let s = 0, o = t[0].x, r = t[0].y, n = [o, r], a = [];
    for (; s < t.length; ) {
      const l = t[s], h = l.x - o, p2 = l.y - r;
      if (a.push(new ve(h, p2)), s > 0 && s < t.length - 1) {
        const d = t[s].getAngleByPoints(
          t[s - 1],
          t[s + 1]
        );
        if (d < 60 || d > 300) {
          const u = (c = a.pop()) == null ? void 0 : c.clone();
          u && e.push({
            pos: n,
            points: [...a, u]
          }), o = t[s].x, r = t[s].y, n = [o, r];
          const f = l.x - o, m = l.y - r;
          a = [new ve(f, m)];
        }
      }
      s++;
    }
    return e.push({
      pos: n,
      points: a
    }), e;
  }
  removeLocal() {
  }
  removeService(t) {
    let e;
    const s = [];
    return this.fullLayer.getElementsByName(t).forEach((o) => {
      if (o.name === t) {
        const r = o.getBoundingClientRect();
        e = SI(e, {
          x: r.x,
          y: r.y,
          w: r.width,
          h: r.height
        }), s.push(o);
      }
    }), s.length && s.forEach((o) => o.remove()), e;
  }
};
var vt = class vt2 extends b {
  constructor(t, e) {
    super(t);
    y(this, "canRotate", false);
    y(this, "scaleType", Fe.none);
    y(this, "toolsType", T.Eraser);
    y(this, "serviceWork");
    y(this, "tmpPoints", []);
    y(this, "workOptions");
    y(this, "worldPosition");
    y(this, "worldScaling");
    y(this, "eraserRect");
    y(this, "eraserPolyline");
    this.serviceWork = e, this.workOptions = t.toolsOpt, this.worldPosition = this.fullLayer.worldPosition, this.worldScaling = this.fullLayer.worldScaling;
  }
  combineConsume() {
  }
  consumeService() {
  }
  setWorkOptions(t) {
    super.setWorkOptions(t);
  }
  createEraserRect(t) {
    const e = t[0] * this.worldScaling[0] + this.worldPosition[0], s = t[1] * this.worldScaling[1] + this.worldPosition[1], { width: o, height: r } = vt2.eraserSizes[this.workOptions.thickness];
    this.eraserRect = {
      x: e - o * 0.5,
      y: s - r * 0.5,
      w: o,
      h: r
    }, this.eraserPolyline = [
      this.eraserRect.x,
      this.eraserRect.y,
      this.eraserRect.x + this.eraserRect.w,
      this.eraserRect.y + this.eraserRect.h
    ];
  }
  computRectCenterPoints() {
    const t = this.tmpPoints.slice(-2);
    if (this.tmpPoints.length === 4) {
      const e = new U(this.tmpPoints[0], this.tmpPoints[1]), s = new U(this.tmpPoints[2], this.tmpPoints[3]), o = U.Sub(s, e).uni(), r = U.Dist(e, s), { width: n, height: a } = vt2.eraserSizes[this.workOptions.thickness], c = Math.min(n, a), l = Math.round(r / c);
      if (l > 1) {
        const h = [];
        for (let p2 = 0; p2 < l; p2++) {
          const d = U.Mul(o, p2 * c);
          h.push(this.tmpPoints[0] + d.x, this.tmpPoints[1] + d.y);
        }
        return h.concat(t);
      }
    }
    return t;
  }
  isNear(t, e) {
    const s = new U(t[0], t[1]), o = new U(e[0], e[1]), { width: r, height: n } = vt2.eraserSizes[this.workOptions.thickness];
    return U.Dist(s, o) < Math.hypot(r, n) * 0.5;
  }
  remove(t) {
    const { curNodeMap: e, removeIds: s } = t;
    let o;
    for (const r of e.values())
      if (r.rect && this.eraserRect && this.eraserPolyline && kI(this.eraserRect, r.rect)) {
        const { op: n } = r, a = [], c = [];
        for (let h = 0; h < n.length; h += 3) {
          const p2 = new U(
            n[h] * this.worldScaling[0] + this.worldPosition[0],
            n[h + 1] * this.worldScaling[1] + this.worldPosition[1],
            n[h + 2]
          );
          c.push(p2), a.push(new ve(p2.x, p2.y));
        }
        const l = a.length && jr(a) || r.rect;
        kI(l, this.eraserRect) && (c.length > 1 ? import_lineclip.default.polyline(
          c.map((p2) => p2.XY),
          this.eraserPolyline
        ).length && s.add(r.name) : s.add(r.name), o = SI(o, r.rect || l));
      }
    return s.forEach((r) => {
      var a;
      const n = this.fullLayer.getElementsByName(r);
      n[0] && (n[0].remove(), q2(n[0], this.fullLayer.parent), (a = this.vNodes) == null || a.delete(r));
    }), o && (o.x -= b.SafeBorderPadding, o.y -= b.SafeBorderPadding, o.w += b.SafeBorderPadding * 2, o.h += b.SafeBorderPadding * 2), o;
  }
  consume(t) {
    const { op: e, disableEraseImage: s, disableEraseText: o } = t.data;
    if (!e || e.length === 0)
      return __spreadValues({
        type: R.None
      }, this.baseConsumeResult);
    const r = this.tmpPoints.length;
    if (r > 1 && this.isNear(
      [e[0], e[1]],
      [this.tmpPoints[r - 2], this.tmpPoints[r - 1]]
    ))
      return __spreadValues({
        type: R.None
      }, this.baseConsumeResult);
    r < 3 ? this.tmpPoints.push(e[0], e[1]) : this.tmpPoints.splice(2, 2, e[0], e[1]);
    const n = this.computRectCenterPoints();
    let a;
    const c = /* @__PURE__ */ new Set();
    if (!this.vNodes)
      return __spreadValues({
        type: R.None
      }, this.baseConsumeResult);
    const l = this.getCanEraserNodeMap(
      this.vNodes.getCanEraserNodes(this.vNodes.curNodeMap, {
        disableEraseImage: s,
        disableEraseText: o
      })
    );
    for (let h = 0; h < n.length - 1; h += 2) {
      this.createEraserRect(n.slice(h, h + 2));
      const p2 = this.remove({ curNodeMap: l, removeIds: c });
      a = SI(a, p2);
    }
    return a && c.size ? {
      type: R.RemoveNode,
      rect: a,
      removeIds: [...c]
    } : __spreadValues({
      type: R.None
    }, this.baseConsumeResult);
  }
  consumeAll(t) {
    return this.consume(t);
  }
  clearTmpPoints() {
    this.tmpPoints.length = 0;
  }
  getCanEraserNodeMap(t) {
    var e;
    if (this.serviceWork) {
      const s = new Map(
        t
      ), o = this.serviceWork.selectorWorkShapes, r = this.serviceWork.workShapes;
      for (const n of o.values())
        if ((e = n.selectIds) != null && e.length)
          for (const a of n.selectIds)
            s.delete(a);
      for (const n of r.keys())
        s.delete(n);
      return s;
    }
    return t;
  }
};
y(vt, "eraserSizes", re);
var Ut = vt;
var ye = Su;
var bs = 1;
var Rs = Object.prototype;
var Ns = Rs.hasOwnProperty;
function xs(T2, i, t, e, s, o) {
  var r = t & bs, n = ye(T2), a = n.length, c = ye(i), l = c.length;
  if (a != l && !r)
    return false;
  for (var h = a; h--; ) {
    var p2 = n[h];
    if (!(r ? p2 in i : Ns.call(i, p2)))
      return false;
  }
  var d = o.get(T2), u = o.get(i);
  if (d && u)
    return d == i && u == T2;
  var f = true;
  o.set(T2, i), o.set(i, T2);
  for (var m = r; ++h < a; ) {
    p2 = n[h];
    var g = T2[p2], I = i[p2];
    if (e)
      var S = r ? e(I, g, p2, i, T2, o) : e(g, I, p2, T2, i, o);
    if (!(S === void 0 ? g === I || s(g, I, t, e, o) : S)) {
      f = false;
      break;
    }
    m || (m = p2 == "constructor");
  }
  if (f && !m) {
    var v = T2.constructor, L = i.constructor;
    v != L && "constructor" in T2 && "constructor" in i && !(typeof v == "function" && v instanceof v && typeof L == "function" && L instanceof L) && (f = false);
  }
  return o.delete(T2), o.delete(i), f;
}
var Ds = xs;
var jt = Oa;
var Ms = Ag;
var Os = CI;
var As = Ds;
var we = Gn;
var ge = cn;
var ke = ar;
var Es = Jc;
var Bs = 1;
var Se2 = "[object Arguments]";
var Te = "[object Array]";
var Ft = "[object Object]";
var Fs = Object.prototype;
var ve2 = Fs.hasOwnProperty;
function zs(T2, i, t, e, s, o) {
  var r = ge(T2), n = ge(i), a = r ? Te : we(T2), c = n ? Te : we(i);
  a = a == Se2 ? Ft : a, c = c == Se2 ? Ft : c;
  var l = a == Ft, h = c == Ft, p2 = a == c;
  if (p2 && ke(T2)) {
    if (!ke(i))
      return false;
    r = true, l = false;
  }
  if (p2 && !l)
    return o || (o = new jt()), r || Es(T2) ? Ms(T2, i, t, e, s, o) : Os(T2, i, a, t, e, s, o);
  if (!(t & Bs)) {
    var d = l && ve2.call(T2, "__wrapped__"), u = h && ve2.call(i, "__wrapped__");
    if (d || u) {
      var f = d ? T2.value() : T2, m = u ? i.value() : i;
      return o || (o = new jt()), s(f, m, t, e, o);
    }
  }
  return p2 ? (o || (o = new jt()), As(T2, i, t, e, s, o)) : false;
}
var Us = zs;
var Gs = Us;
var Ie = yt;
function De(T2, i, t, e, s) {
  return T2 === i ? true : T2 == null || i == null || !Ie(T2) && !Ie(i) ? T2 !== T2 && i !== i : Gs(T2, i, t, e, De, s);
}
var Xs = De;
var Ys = Xs;
function $s(T2, i) {
  return Ys(T2, i);
}
var _s = $s;
var lt = Pn(_s);
var _t2 = class _t3 extends b {
  constructor(t) {
    super(t);
    y(this, "toolsType", T.Selector);
    y(this, "tmpPoints", []);
    y(this, "subTmpPoints", []);
    y(this, "workOptions");
    y(this, "vNodes");
    y(this, "selectIds");
    y(this, "selectorColor");
    y(this, "strokeColor");
    y(this, "fillColor");
    y(this, "oldSelectRect");
    y(this, "oldSubSelectRect");
    y(this, "canRotate", false);
    y(this, "canTextEdit", false);
    y(this, "canLock", false);
    y(this, "scaleType", Fe.all);
    y(this, "toolsTypes");
    y(this, "shapeOpt");
    y(this, "textOpt");
    y(this, "isLocked");
    y(this, "thickness");
    y(this, "strokeType");
    y(this, "useStroke");
    this.workOptions = t.toolsOpt, this.vNodes = t.vNodes;
  }
  computSelector(t = true) {
    const e = jr(this.tmpPoints);
    if (e.w === 0 || e.h === 0)
      return {
        selectIds: [],
        intersectRect: void 0,
        subNodeMap: /* @__PURE__ */ new Map()
      };
    const { rectRange: s, nodeRange: o } = this.vNodes.getRectIntersectRange(
      e,
      t
    );
    return {
      selectIds: [...o.keys()],
      intersectRect: s,
      subNodeMap: o
    };
  }
  updateTempPoints(t) {
    const e = this.tmpPoints.length, s = t.length;
    if (s > 1) {
      const o = new ve(
        t[s - 2] * this.fullLayer.worldScaling[0] + this.fullLayer.worldPosition[0],
        t[s - 1] * this.fullLayer.worldScaling[0] + this.fullLayer.worldPosition[1]
      );
      e === 2 ? this.tmpPoints.splice(1, 1, o) : this.tmpPoints.push(o);
    }
  }
  drawSelector(t) {
    const { drawRect: e, subNodeMap: s, selectorId: o, layer: r, isService: n } = t, a = new _spritejs$Group({
      pos: [e.x, e.y],
      anchor: [0, 0],
      size: [e.w, e.h],
      id: o,
      name: o,
      zIndex: 9999
    }), c = [];
    if (n) {
      const l = new _spritejs$Rect({
        normalize: true,
        pos: [e.w / 2, e.h / 2],
        lineWidth: 1,
        strokeColor: this.selectorColor || this.workOptions.strokeColor,
        width: e.w,
        height: e.h,
        name: _t3.selectorBorderId
      });
      c.push(l);
    }
    s.forEach((l, h) => {
      const p2 = [
        l.rect.x + l.rect.w / 2 - e.x,
        l.rect.y + l.rect.h / 2 - e.y
      ], d = new _spritejs$Rect({
        normalize: true,
        pos: p2,
        lineWidth: 1,
        strokeColor: s.size > 1 ? this.selectorColor || this.workOptions.strokeColor : void 0,
        width: l.rect.w,
        height: l.rect.h,
        id: `selector-${h}`,
        name: `selector-${h}`
      });
      c.push(d);
    }), c && a.append(...c), (r == null ? void 0 : r.parent).appendChild(a);
  }
  draw(t, e, s, o = false) {
    var a, c;
    const { intersectRect: r, subNodeMap: n } = s;
    (c = (a = e.parent) == null ? void 0 : a.getElementById(t)) == null || c.remove(), r && this.drawSelector({
      drawRect: r,
      subNodeMap: n,
      selectorId: t,
      layer: e,
      isService: o
    });
  }
  getSelecteorInfo(t) {
    this.scaleType = Fe.all, this.canRotate = false, this.textOpt = void 0, this.strokeColor = void 0, this.fillColor = void 0, this.canTextEdit = false, this.canLock = false, this.isLocked = false, this.toolsTypes = void 0, this.shapeOpt = void 0, this.thickness = void 0, this.strokeType = void 0, this.useStroke = false;
    const e = /* @__PURE__ */ new Set();
    let s, o = true;
    for (const r of t.values()) {
      const { opt: n, canRotate: a, scaleType: c, toolsType: l } = r;
      this.selectorColor = this.workOptions.strokeColor, n.strokeColor && (this.strokeColor = n.strokeColor), n.fillColor && (this.fillColor = n.fillColor), n.textOpt && (this.textOpt = n.textOpt), n.thickness && (this.thickness = n.thickness), l !== T.Pencil && (o = false), n.strokeType && (this.strokeType = n.strokeType), l === T.SpeechBalloon && (e.add(l), this.shapeOpt || (this.shapeOpt = {}), this.shapeOpt.placement = n.placement), l === T.Polygon && (e.add(l), this.shapeOpt || (this.shapeOpt = {}), this.shapeOpt.vertices = n.vertices), l === T.Star && (e.add(l), this.shapeOpt || (this.shapeOpt = {}), this.shapeOpt.vertices = n.vertices, this.shapeOpt.innerRatio = n.innerRatio, this.shapeOpt.innerVerticeStep = n.innerVerticeStep), l === T.Text && (this.textOpt = n), t.size === 1 && (this.textOpt && (this.canTextEdit = true), this.canRotate = a, this.scaleType = c), (l === T.Image || l === T.BackgroundSVG) && (s = r), (c === Fe.proportional && this.scaleType !== Fe.none || c === Fe.none) && (this.scaleType = c);
    }
    o && (this.useStroke = true), e.size && (this.toolsTypes = [...e]), s && (t.size === 1 ? (this.canLock = true, s.opt.locked && (this.isLocked = true, this.scaleType = Fe.none, this.canRotate = false, this.textOpt = void 0, this.fillColor = void 0, this.selectorColor = "rgb(177,177,177)", this.strokeColor = void 0, this.canTextEdit = false, this.thickness = void 0, this.strokeType = void 0, this.useStroke = void 0)) : t.size > 1 && !s.opt.locked && (this.canLock = false, this.canRotate = false));
  }
  getChildrenPoints() {
    var t, e;
    if (this.scaleType === Fe.both && ((t = this.selectIds) == null ? void 0 : t.length) === 1) {
      const s = this.selectIds[0], o = (e = this.vNodes.get(s)) == null ? void 0 : e.op;
      if (o) {
        const r = [];
        for (let n = 0; n < o.length; n += 3)
          r.push([o[n], o[n + 1]]);
        return r;
      }
    }
  }
  consume(t) {
    if (t.isSubWorker)
      return this.subWorkerConsume(t);
    let e = __spreadValues({
      type: R.Select,
      dataType: q.Local
    }, this.baseConsumeResult);
    if (t.isSimpleWorker) {
      const l = this.subWorkerConsume(t);
      e.subRect = l.rect;
    }
    const { op: s, workState: o } = t.data;
    let r = this.oldSelectRect;
    if (o === x.Start && (r = this.unSelectedAllIds()), !(s != null && s.length) || !this.vNodes.curNodeMap.size)
      return t.isSimpleWorker ? e : { type: R.None };
    this.updateTempPoints(s);
    const n = this.computSelector();
    if (this.selectIds && lt(this.selectIds, n.selectIds))
      return t.isSimpleWorker ? e : { type: R.None };
    this.selectIds = n.selectIds;
    const a = n.intersectRect;
    this.getSelecteorInfo(n.subNodeMap), this.draw($, this.fullLayer, n), this.oldSelectRect = a;
    const c = this.getChildrenPoints();
    return e = __spreadProps(__spreadValues({}, e), {
      rect: SI(a, r),
      selectIds: n.selectIds,
      selectRect: a,
      selectorColor: this.selectorColor,
      strokeColor: this.strokeColor,
      fillColor: this.fillColor,
      textOpt: this.textOpt,
      canTextEdit: this.canTextEdit,
      canRotate: this.canRotate,
      canLock: this.canLock,
      scaleType: this.scaleType,
      willSyncService: true,
      points: c,
      isLocked: this.isLocked,
      toolsTypes: this.toolsTypes,
      shapeOpt: this.shapeOpt,
      thickness: this.thickness,
      useStroke: this.useStroke,
      strokeType: this.strokeType
    }), e;
  }
  consumeAll() {
    var e, s;
    let t = this.oldSelectRect;
    if (!((e = this.selectIds) != null && e.length) && this.tmpPoints[0] && this.selectSingleTool(this.tmpPoints[0].XY, $, false), (s = this.selectIds) != null && s.length && (t = this.selectedByIds(this.selectIds)), t) {
      const o = this.getChildrenPoints();
      return __spreadValues({
        type: R.Select,
        dataType: q.Local,
        rect: this.oldSelectRect,
        selectIds: this.selectIds,
        selectorColor: this.selectorColor,
        selectRect: this.oldSelectRect,
        strokeColor: this.strokeColor,
        fillColor: this.fillColor,
        textOpt: this.textOpt,
        canTextEdit: this.canTextEdit,
        canRotate: this.canRotate,
        canLock: this.canLock,
        scaleType: this.scaleType,
        willSyncService: true,
        points: o,
        isLocked: this.isLocked,
        toolsTypes: this.toolsTypes,
        shapeOpt: this.shapeOpt,
        thickness: this.thickness,
        useStroke: this.useStroke,
        strokeType: this.strokeType
      }, this.baseConsumeResult);
    }
    return { type: R.None };
  }
  consumeService() {
  }
  updateTempPointsForSubWorker(t) {
    const e = t.slice(-2), s = new ve(e[0], e[1]);
    if (this.subTmpPoints[0].isNear(s, 1))
      return false;
    if (this.subTmpPoints.length === 2) {
      if (s.isNear(this.subTmpPoints[1], 1))
        return false;
      this.subTmpPoints[1] = s;
    } else
      this.subTmpPoints.push(s);
    return true;
  }
  computDrawPoints(t) {
    const { thickness: e } = this.workOptions, s = [];
    for (const n of t)
      s.push(new U(...n));
    const o = jr(s, e), r = [
      o.x + o.w / 2,
      o.y + o.h / 2
    ];
    return {
      rect: o,
      pos: r,
      points: s.map((n) => n.XY).flat(1)
    };
  }
  drawForSubWorker(t) {
    const { workId: e, layer: s, ps: o } = t, { strokeColor: r, scale: n, rotate: a, translate: c } = this.workOptions, l = s.worldPosition, h = s.worldScaling, { points: p2, rect: d, pos: u } = this.computDrawPoints(o), f = 1 / s.worldScaling[0], m = r && Wt(r) || [0, 0, 0, 0], g = {
      close: true,
      normalize: true,
      points: p2,
      lineWidth: f,
      fillColor: At(m[0], m[1], m[2], 0.1),
      strokeColor: At(m[0], m[1], m[2], 1),
      lineJoin: "round",
      lineCap: "round"
    };
    let I = {
      x: Math.floor(
        d.x * h[0] + l[0] - b.SafeBorderPadding
      ),
      y: Math.floor(
        d.y * h[1] + l[1] - b.SafeBorderPadding
      ),
      w: Math.floor(
        d.w * h[0] + 2 * b.SafeBorderPadding
      ),
      h: Math.floor(
        d.h * h[0] + 2 * b.SafeBorderPadding
      )
    };
    const S = new _spritejs$Group({
      name: e,
      id: e,
      pos: u,
      anchor: [0.5, 0.5],
      size: [d.w, d.h],
      scale: n,
      rotate: a,
      translate: c
    }), v = new _spritejs$Polyline(__spreadProps(__spreadValues({}, g), {
      pos: [0, 0]
    }));
    if (S.appendChild(v), this.replace(s, e, S), n || a || c) {
      const L = S.getBoundingClientRect();
      I = {
        x: Math.floor(L.x - b.SafeBorderPadding),
        y: Math.floor(L.y - b.SafeBorderPadding),
        w: Math.floor(L.width + 2 * b.SafeBorderPadding),
        h: Math.floor(L.height + 2 * b.SafeBorderPadding)
      };
    }
    return I;
  }
  transformData(t) {
    const e = jr(t);
    return [
      [e.x, e.y, 0],
      [e.x + e.w, e.y, 0],
      [e.x + e.w, e.y + e.h, 0],
      [e.x, e.y + e.h, 0]
    ];
  }
  subWorkerConsume(t) {
    const { data: e, isFullWork: s } = t, { op: o, syncUnitTime: r } = e;
    r && (this.syncUnitTime = r);
    const n = o == null ? void 0 : o.length;
    if (!n || n < 2)
      return { type: R.None, rect: void 0 };
    let a;
    if (this.subTmpPoints.length === 0 ? (this.subTmpPoints = [new ve(o[0], o[1])], a = false) : a = this.updateTempPointsForSubWorker(o), !a)
      return { type: R.None, rect: void 0 };
    const c = this.transformData(this.subTmpPoints), l = s ? this.fullLayer : this.drawLayer || this.fullLayer, h = this.drawForSubWorker({
      ps: c,
      workId: this.workId,
      layer: l
    }), p2 = SI(h, this.oldSubSelectRect);
    return this.oldSubSelectRect = h, __spreadValues({
      rect: p2,
      type: R.DrawWork,
      dataType: q.Local
    }, this.baseConsumeResult);
  }
  subWorkerConsumeAll(t) {
    const { isFullWork: e } = t, s = e ? this.fullLayer : this.drawLayer || this.fullLayer;
    return this.replace(s, this.workId), __spreadValues({
      rect: this.oldSelectRect,
      type: R.FullWork,
      dataType: q.Local
    }, this.baseConsumeResult);
  }
  clearSubTmpPoints() {
    this.subTmpPoints.length = 0;
  }
  clearTmpPoints() {
    this.tmpPoints.length = 0, this.clearSubTmpPoints();
  }
  clearSubSelectData() {
    this.oldSubSelectRect = void 0;
  }
  clearSelectData() {
    this.selectIds = void 0, this.oldSelectRect = void 0, this.clearSubSelectData();
  }
  selectSingleTool(t, e = $, s = false) {
    if (t.length === 2) {
      const o = t[0], r = t[1];
      let n;
      const { nodeRange: a } = this.vNodes.getRectIntersectRange(
        { x: o, y: r, w: 0, h: 0 },
        false
      ), c = [...a.values()].sort(
        (l, h) => (h.opt.zIndex || 0) - (l.opt.zIndex || 0)
      );
      for (const l of c) {
        const h = this.fullLayer.getElementsByName(
          l.name
        );
        if (Ge(h).find((d) => d.isPointCollision(o, r))) {
          n = l;
          break;
        }
      }
      if (n) {
        const l = n.name;
        if (!lt(this.oldSelectRect, n.rect)) {
          const h = /* @__PURE__ */ new Map([[l, n]]);
          this.getSelecteorInfo(h), this.draw(
            e,
            this.fullLayer,
            {
              intersectRect: n.rect,
              subNodeMap: h,
              selectIds: this.selectIds || []
            },
            s
          );
        }
        this.selectIds = [l], this.oldSelectRect = n.rect;
      }
    }
  }
  unSelectedAllIds() {
    let t;
    for (const [e, s] of this.vNodes.curNodeMap.entries())
      s.isSelected && (t = SI(t, s.rect), this.vNodes.unSelected(e));
    return t;
  }
  unSelectedByIds(t) {
    let e;
    for (const s of t) {
      const o = this.vNodes.get(s);
      o && o.isSelected && (e = SI(e, o.rect), this.vNodes.unSelected(s));
    }
    return e;
  }
  selectedByIds(t) {
    let e;
    for (const s of t) {
      const o = this.vNodes.get(s);
      o && (e = SI(e, o.rect), this.vNodes.selected(s));
    }
    return e;
  }
  getSelectorRect(t, e) {
    var n;
    let s;
    const o = (n = t.parent) == null ? void 0 : n.getElementById(
      e
    ), r = o == null ? void 0 : o.getBoundingClientRect();
    return r && (s = SI(s, {
      x: Math.floor(r.x),
      y: Math.floor(r.y),
      w: Math.floor(r.width + 1),
      h: Math.floor(r.height + 1)
    })), s;
  }
  isCanFillColor(t) {
    return t === T.Ellipse || t === T.Triangle || t === T.Rectangle || t === T.Polygon || t === T.Star || t === T.SpeechBalloon;
  }
  updateSelector(t) {
    return __async(this, null, function* () {
      const {
        updateSelectorOpt: e,
        selectIds: s,
        vNodes: o,
        willSerializeData: r,
        worker: n,
        offset: a
      } = t, c = this.fullLayer;
      if (!c)
        return;
      let l;
      const h = /* @__PURE__ */ new Map(), { originPoint: p2, workState: d, angle: u, translate: f, dir: m, scale: g } = e;
      a && (f ? e.translate = [
        f[0] + a[0],
        f[1] + a[1]
      ] : e.translate = a);
      let I;
      if (p2 || f || H(u)) {
        if (d === x.Start && s)
          return o.setTargetAssignKeys(s), {
            type: R.Select,
            dataType: q.Local,
            selectRect: this.oldSelectRect,
            rect: this.oldSelectRect
          };
        if (I = o.getLastTarget(), !I)
          return;
      }
      if (s)
        for (const L of s) {
          const C = o.get(L);
          if (C) {
            const { toolsType: N, opt: R2 } = C, P = (c == null ? void 0 : c.getElementsByName(L))[0];
            if (P) {
              const M = __spreadValues({}, e);
              let D;
              const B = b.isWillRefresh({
                toolsType: N,
                opt: R2,
                updateOpt: M,
                vNodes: o,
                node: P,
                willSerializeData: r
              });
              if (N) {
                D = I == null ? void 0 : I.get(L);
                const F = Ue(N);
                if (F == null || F.updateNodeOpt({
                  node: P,
                  opt: M,
                  vNodes: o,
                  willSerializeData: r,
                  targetNode: D
                }), C && n && B) {
                  const U2 = n.createWorkShapeNode({
                    workId: L,
                    toolsType: N,
                    toolsOpt: C.opt
                  });
                  U2 == null || U2.setWorkId(L);
                  let V;
                  if (N === T.BackgroundSVG)
                    V = U2.consumeService({
                      isFullWork: true,
                      replaceId: L
                    });
                  else if (N === T.Image)
                    V = yield U2.consumeServiceAsync({
                      isFullWork: true,
                      replaceId: L,
                      worker: n
                    });
                  else if (N === T.Text)
                    V = yield U2.consumeServiceAsync({
                      isFullWork: true,
                      replaceId: L,
                      isDrawLabel: true
                    });
                  else
                    try {
                      V = U2 == null ? void 0 : U2.consumeService({
                        op: C.op,
                        isFullWork: true,
                        replaceId: L,
                        workState: d
                      });
                    } catch (Z) {
                      console.error("consumeService error", Z);
                      continue;
                    }
                  V && (C.rect = V);
                }
                C && (h.set(L, C), l = SI(l, C.rect));
              }
            }
          }
        }
      I && d === x.Done && (o.deleteLastTarget(), I = void 0);
      const S = l;
      if (p2 && f && g && m && S && !a) {
        const L = [
          [S.x, S.y],
          [S.x + S.w, S.y],
          [S.x + S.w, S.y + S.h],
          [S.x, S.y + S.h]
        ];
        let C;
        switch (m) {
          case "top":
          case "topLeft":
          case "left":
            f[0] > 0 && f[1] > 0 ? C = L[0] : f[0] > 0 ? C = L[3] : f[1] > 0 ? C = L[1] : C = L[2];
            break;
          case "topRight":
            f[0] < 0 && f[1] > 0 ? C = L[1] : f[0] < 0 ? C = L[2] : f[1] > 0 ? C = L[0] : C = L[3];
            break;
          case "right":
          case "bottomRight":
          case "bottom":
            f[0] < 0 && f[1] < 0 ? C = L[2] : f[0] < 0 ? C = L[1] : f[1] < 0 ? C = L[3] : C = L[0];
            break;
          case "bottomLeft":
            f[0] > 0 && f[1] < 0 ? C = L[3] : f[0] > 0 ? C = L[0] : f[1] < 0 ? C = L[2] : C = L[1];
            break;
        }
        const N = C && [
          p2[0] - C[0],
          p2[1] - C[1]
        ] || [0, 0];
        if (!lt(N, [0, 0]))
          return yield this.updateSelector(__spreadProps(__spreadValues({}, t), {
            updateSelectorOpt: { workState: d },
            offset: N
          }));
      }
      this.getSelecteorInfo(h), this.draw($, c, {
        selectIds: s || [],
        subNodeMap: h,
        intersectRect: S
      });
      const v = SI(this.oldSelectRect, S);
      return this.oldSelectRect = S, {
        type: R.Select,
        dataType: q.Local,
        selectRect: S,
        renderRect: l,
        rect: SI(v, S),
        selectIds: s
      };
    });
  }
  blurSelector() {
    const t = this.unSelectedAllIds();
    return {
      type: R.Select,
      dataType: q.Local,
      rect: t,
      selectIds: [],
      willSyncService: true
    };
  }
  getRightServiceId(t) {
    return t.replace(st, "-");
  }
  selectServiceNode(t, e, s) {
    const { selectIds: o } = e, r = this.getRightServiceId(t), n = this.getSelectorRect(
      this.fullLayer,
      r
    );
    let a;
    const c = /* @__PURE__ */ new Map();
    return o == null || o.forEach((l) => {
      const h = this.vNodes.get(l);
      h && (a = SI(a, h.rect), c.set(l, h));
    }), this.getSelecteorInfo(c), this.draw(
      r,
      this.fullLayer,
      {
        intersectRect: a,
        selectIds: o || [],
        subNodeMap: c
      },
      s
    ), SI(a, n);
  }
  reRenderSelector() {
    var s;
    let t;
    const e = /* @__PURE__ */ new Map();
    return (s = this.selectIds) == null || s.forEach((o) => {
      const r = this.vNodes.get(o);
      r && (t = SI(t, r.rect), e.set(o, r));
    }, this), this.getSelecteorInfo(e), this.draw($, this.fullLayer, {
      intersectRect: t,
      subNodeMap: e,
      selectIds: this.selectIds || []
    }), this.oldSelectRect = t, t;
  }
  updateSelectIds(t) {
    var r;
    let e;
    const s = (r = this.selectIds) == null ? void 0 : r.filter(
      (n) => !t.includes(n)
    );
    if (s != null && s.length && (e = this.unSelectedByIds(s)), t.length) {
      const n = this.selectedByIds(t);
      e = SI(e, n);
    }
    this.selectIds = t;
    const o = this.reRenderSelector();
    return {
      bgRect: e,
      selectRect: o
    };
  }
  cursorHover(t) {
    var r, n;
    const e = this.oldSelectRect;
    this.selectIds = [];
    const s = (r = this.workId) == null ? void 0 : r.toString(), o = [
      t[0] * this.fullLayer.worldScaling[0] + this.fullLayer.worldPosition[0],
      t[1] * this.fullLayer.worldScaling[0] + this.fullLayer.worldPosition[1]
    ];
    if (this.selectSingleTool(o, s, true), this.oldSelectRect && !lt(e, this.oldSelectRect))
      return {
        type: R.CursorHover,
        dataType: q.Local,
        rect: SI(e, this.oldSelectRect),
        selectorColor: this.selectorColor,
        willSyncService: false
      };
    if ((n = this.selectIds) != null && n.length || (this.oldSelectRect = void 0), e && !this.oldSelectRect)
      return this.cursorBlur(), {
        type: R.CursorHover,
        dataType: q.Local,
        rect: e,
        selectorColor: this.selectorColor,
        willSyncService: false
      };
  }
  cursorBlur() {
    var e, s;
    this.selectIds = [];
    const t = (e = this.workId) == null ? void 0 : e.toString();
    ((s = this.fullLayer) == null ? void 0 : s.parent).children.forEach((o) => {
      o.name === t && o.remove();
    });
  }
};
y(_t2, "selectorBorderId", "selector-border");
var Gt2 = _t2;
var Me = class extends b {
  constructor(t) {
    super(t);
    y(this, "canRotate", false);
    y(this, "scaleType", Fe.both);
    y(this, "toolsType", T.Arrow);
    y(this, "tmpPoints", []);
    y(this, "workOptions");
    y(this, "oldRect");
    y(this, "arrowTipWidth");
    y(this, "syncTimestamp");
    this.workOptions = t.toolsOpt, this.arrowTipWidth = this.workOptions.thickness * 4, this.syncTimestamp = 0, this.syncUnitTime = 50;
  }
  consume(t) {
    const {
      data: e,
      isFullWork: s,
      isSubWorker: o,
      isMainThread: r,
      smoothSync: n,
      isSimpleWorker: a
    } = t, c = this.workId, { op: l, syncUnitTime: h } = e;
    h && (this.syncUnitTime = h);
    const p2 = l == null ? void 0 : l.length;
    if (!p2 || p2 < 2)
      return { type: R.None };
    let d;
    if (this.tmpPoints.length === 0 ? (this.tmpPoints = [new ve(l[0], l[1])], d = false) : d = this.updateTempPoints(l), !d)
      return { type: R.None };
    let u;
    if (o || r || a) {
      const m = s ? this.fullLayer : this.drawLayer || this.fullLayer;
      u = this.draw({ workId: c, layer: m });
    }
    if (a) {
      const m = SI(u, this.oldRect);
      this.oldRect = u;
      const g = __spreadValues({
        rect: m,
        type: R.DrawWork,
        dataType: q.Local
      }, this.baseConsumeResult);
      if (n) {
        const I = Date.now();
        I - this.syncTimestamp > this.syncUnitTime && (this.syncTimestamp = I, g.op = this.tmpPoints.map((S) => [...S.XY, 0]).flat(1), g.index = 0, g.isSync = true);
      }
      return g;
    }
    if (!o && n) {
      const m = Date.now();
      return m - this.syncTimestamp > this.syncUnitTime ? (this.syncTimestamp = m, __spreadProps(__spreadValues({}, this.baseConsumeResult), {
        type: R.DrawWork,
        dataType: q.Local,
        op: this.tmpPoints.map((g) => [...g.XY, 0]).flat(1),
        isSync: true,
        index: 0
      })) : { type: R.None };
    }
    const f = SI(u, this.oldRect);
    return this.oldRect = u, __spreadProps(__spreadValues({
      rect: f
    }, this.baseConsumeResult), {
      type: R.DrawWork,
      dataType: q.Local
    });
  }
  consumeAll() {
    var n;
    const t = this.workId;
    if (this.tmpPoints.length < 2)
      return {
        type: R.RemoveNode,
        removeIds: [t]
      };
    const e = this.fullLayer, s = this.draw({ workId: t, layer: e });
    this.oldRect = s;
    const o = this.tmpPoints.map((a) => [...a.XY, 0]).flat(1), r = _t(o);
    return (n = this.vNodes) == null || n.setInfo(t, {
      rect: s,
      op: o,
      opt: this.workOptions,
      toolsType: this.toolsType,
      canRotate: this.canRotate,
      scaleType: this.scaleType,
      centerPos: b.getCenterPos(s, e)
    }), __spreadProps(__spreadValues({
      rect: s
    }, this.baseConsumeResult), {
      type: R.FullWork,
      dataType: q.Local,
      ops: r,
      isSync: true,
      updateNodeOpt: {
        useAnimation: true
      }
    });
  }
  draw(t) {
    const { workId: e, layer: s, isDrawEraserlines: o } = t, {
      strokeColor: r,
      thickness: n,
      zIndex: a,
      scale: c,
      rotate: l,
      translate: h,
      strokeType: p2,
      eraserlines: d,
      lineDash: u,
      lineCap: f
    } = this.workOptions, m = s.worldPosition, g = s.worldScaling, { points: I, pos: S, rect: v, isTriangle: L, trianglePoints: C, trianglePos: N } = this.computDrawPoints(n), R2 = [
      v.x + v.w / 2,
      v.y + v.h / 2
    ], P = {
      pos: R2,
      name: e,
      id: e,
      zIndex: a,
      anchor: [0.5, 0.5],
      size: [v.w, v.h]
    };
    c && (P.scale = c), l && (P.rotate = l), h && (P.translate = h);
    const M = new _spritejs$Group(P), D = {
      points: C,
      pos: [N[0] - R2[0], N[1] - R2[1]],
      fillColor: r,
      strokeColor: r,
      lineWidth: 0,
      normalize: false
    }, B = new _spritejs$Polyline(D);
    if (M.append(B), !L && I && S) {
      const U2 = {
        points: I,
        pos: [S[0] - R2[0], S[1] - R2[1]],
        fillColor: r,
        strokeColor: r,
        lineDash: p2 === me.Dotted ? [
          u && u[0] || 1,
          (u && u[1] || 2) * n
        ] : p2 === me.LongDotted ? [
          (u && u[0] || 1) * n,
          (u && u[1] || 2) * n
        ] : void 0,
        lineCap: p2 === me.Normal ? void 0 : f,
        lineWidth: n,
        normalize: false
      }, V = new _spritejs$Polyline(U2);
      M.append(V);
    }
    d && (this.scaleType = Fe.proportional, this.drawEraserlines(
      {
        group: M,
        eraserlines: d,
        pos: R2,
        layer: s
      },
      o
    )), this.replace(s, e, M);
    let F = {
      x: Math.floor(
        v.x * g[0] + m[0] - b.SafeBorderPadding
      ),
      y: Math.floor(
        v.y * g[1] + m[1] - b.SafeBorderPadding
      ),
      w: Math.floor(
        v.w * g[0] + 2 * b.SafeBorderPadding
      ),
      h: Math.floor(
        v.h * g[1] + 2 * b.SafeBorderPadding
      )
    };
    if (c || l || h) {
      const U2 = M.getBoundingClientRect();
      F = {
        x: Math.floor(U2.x - b.SafeBorderPadding),
        y: Math.floor(U2.y - b.SafeBorderPadding),
        w: Math.floor(U2.width + b.SafeBorderPadding * 2),
        h: Math.floor(U2.height + b.SafeBorderPadding * 2)
      };
    }
    return this.isDelete && M.setAttribute("opacity", 0), F;
  }
  computDrawPoints(t) {
    return this.tmpPoints[1].distance(this.tmpPoints[0]) > this.arrowTipWidth ? this.computFullArrowPoints(t) : this.computTrianglePoints();
  }
  computFullArrowPoints(t) {
    const e = U.Sub(this.tmpPoints[1], this.tmpPoints[0]).uni(), s = U.Per(e).mul(t / 2), o = ve.Sub(
      this.tmpPoints[0],
      s
    ), r = ve.Add(
      this.tmpPoints[0],
      s
    ), n = U.Mul(e, this.arrowTipWidth), a = U.Sub(this.tmpPoints[1], n), c = ve.Sub(a, s), l = ve.Add(a, s), h = U.Per(e).mul(t * 1.5), p2 = ve.Sub(a, h), d = ve.Add(a, h), u = [this.tmpPoints[0], a], f = [p2, this.tmpPoints[1], d], m = [
      o,
      r,
      ...f,
      c,
      l
    ];
    return {
      trianglePoints: f.map((g) => ve.Sub(g, this.tmpPoints[1]).XY).flat(1),
      trianglePos: this.tmpPoints[1].XY,
      points: u.map((g) => ve.Sub(g, this.tmpPoints[0]).XY).flat(1),
      rect: jr(m),
      isTriangle: false,
      pos: this.tmpPoints[0].XY
    };
  }
  computTrianglePoints() {
    const t = U.Sub(this.tmpPoints[1], this.tmpPoints[0]).uni(), e = this.tmpPoints[1].distance(this.tmpPoints[0]), s = U.Per(t).mul(
      Math.floor(e * 3 / 8)
    ), o = ve.Sub(this.tmpPoints[0], s), r = ve.Add(
      this.tmpPoints[0],
      s
    ), n = [o, this.tmpPoints[1], r];
    return {
      trianglePoints: n.map((a) => ve.Sub(a, this.tmpPoints[1]).XY).flat(1),
      trianglePos: this.tmpPoints[1].XY,
      rect: jr(n),
      isTriangle: true
    };
  }
  updateTempPoints(t) {
    const e = t.slice(-2), s = new ve(e[0], e[1]), o = this.tmpPoints[0], { thickness: r } = this.workOptions;
    if (o.isNear(s, r))
      return false;
    if (this.tmpPoints.length === 2) {
      if (s.isNear(this.tmpPoints[1], 1))
        return false;
      this.tmpPoints[1] = s;
    } else
      this.tmpPoints.push(s);
    return true;
  }
  consumeService(t) {
    var c, l;
    const { op: e, isFullWork: s, workState: o = x.Done } = t, r = (c = this.workId) == null ? void 0 : c.toString();
    if (!r)
      return;
    this.tmpPoints.length = 0;
    for (let h = 0; h < e.length; h += 3)
      this.tmpPoints.push(new ve(e[h], e[h + 1], e[h + 2]));
    const n = s ? this.fullLayer : this.drawLayer || this.fullLayer, a = this.draw({
      workId: r,
      layer: n,
      isDrawEraserlines: o === x.Done
    });
    return this.oldRect = a, (l = this.vNodes) == null || l.setInfo(r, {
      rect: a,
      op: e,
      opt: this.workOptions,
      toolsType: this.toolsType,
      canRotate: this.canRotate,
      scaleType: this.scaleType,
      centerPos: b.getCenterPos(a, n)
    }), a;
  }
  clearTmpPoints() {
    this.tmpPoints.length = 0;
  }
  static updateNodeOpt(t) {
    var c, l;
    const { node: e, opt: s, vNodes: o } = t, { strokeColor: r, strokeType: n } = s, a = o.get(e.name);
    return r && (e.tagName === "GROUP" ? e.children.forEach((h) => {
      h.setAttribute("strokeColor", r), h.getAttribute("fillColor") && h.setAttribute("fillColor", r);
    }) : (e.setAttribute("strokeColor", r), e.setAttribute("fillColor", r)), (c = a == null ? void 0 : a.opt) != null && c.strokeColor && (a.opt.strokeColor = r)), n && (a != null && a.opt) && ((l = a.opt) != null && l.strokeType) && (a.opt.strokeType = n), a && o.setInfo(e.name, a), b.updateNodeOpt(t);
  }
};
var Oe = class extends b {
  constructor(t) {
    super(t);
    y(this, "canRotate", false);
    y(this, "scaleType", Fe.all);
    y(this, "toolsType", T.Ellipse);
    y(this, "tmpPoints", []);
    y(this, "workOptions");
    y(this, "oldRect");
    y(this, "syncTimestamp");
    this.workOptions = t.toolsOpt, this.syncTimestamp = 0, this.syncUnitTime = 50;
  }
  consume(t) {
    const {
      data: e,
      isFullWork: s,
      isSubWorker: o,
      isMainThread: r,
      smoothSync: n,
      isSimpleWorker: a
    } = t, c = this.workId, { op: l, syncUnitTime: h } = e;
    h && (this.syncUnitTime = h);
    const p2 = l == null ? void 0 : l.length;
    if (!p2 || p2 < 2)
      return { type: R.None };
    let d;
    if (this.tmpPoints.length === 0 ? (this.tmpPoints = [new ve(l[0], l[1])], d = false) : d = this.updateTempPoints(l), !d)
      return { type: R.None };
    let u;
    if (o || r || a) {
      const f = s ? this.fullLayer : this.drawLayer || this.fullLayer;
      u = this.draw({ workId: c, layer: f, isDrawing: true });
      const m = f.parent.parent, g = {
        x: 0,
        y: 0,
        w: Math.floor(m.width),
        h: Math.floor(m.height)
      }, I = __spreadValues({
        type: R.DrawWork,
        dataType: q.Local
      }, this.baseConsumeResult);
      if (FI(u, g) !== Gt.outside) {
        const v = SI(u, this.oldRect);
        if (this.oldRect = u, I.rect = v, !a)
          return I;
      }
      if (a) {
        const v = Date.now();
        return v - this.syncTimestamp > this.syncUnitTime && (this.syncTimestamp = v, I.op = this.tmpPoints.map((L) => [...L.XY, 0]).flat(1), I.index = 0, I.isSync = true), I;
      }
      return { type: R.None };
    }
    if (!o && !r && !a && n) {
      const f = Date.now();
      return f - this.syncTimestamp > this.syncUnitTime ? (this.syncTimestamp = f, __spreadProps(__spreadValues({
        type: R.DrawWork,
        dataType: q.Local
      }, this.baseConsumeResult), {
        op: this.tmpPoints.map((m) => [...m.XY, 0]).flat(1),
        isSync: true,
        index: 0
      })) : { type: R.None };
    }
    return { type: R.None };
  }
  consumeAll() {
    var n;
    const t = this.workId;
    if (this.tmpPoints.length < 2)
      return {
        type: R.RemoveNode,
        removeIds: [t]
      };
    const e = this.fullLayer, s = this.draw({
      workId: t,
      layer: e,
      isDrawing: false
    });
    this.oldRect = s;
    const o = this.tmpPoints.map((a) => [...a.XY, 0]).flat(1), r = _t(o);
    return (n = this.vNodes) == null || n.setInfo(t, {
      rect: s,
      op: o,
      opt: this.workOptions,
      toolsType: this.toolsType,
      canRotate: this.canRotate,
      scaleType: this.scaleType,
      centerPos: s && b.getCenterPos(s, e)
    }), __spreadProps(__spreadValues({
      rect: s,
      type: R.FullWork,
      dataType: q.Local
    }, this.baseConsumeResult), {
      ops: r,
      isSync: true,
      updateNodeOpt: {
        useAnimation: true
      }
    });
  }
  draw(t) {
    const { workId: e, layer: s, isDrawing: o, isDrawEraserlines: r } = t, {
      strokeColor: n,
      fillColor: a,
      thickness: c,
      zIndex: l,
      scale: h,
      rotate: p2,
      translate: d,
      strokeType: u,
      eraserlines: f,
      lineDash: m,
      lineCap: g
    } = this.workOptions, I = s.worldScaling, { radius: S, rect: v, pos: L } = this.computDrawPoints(c), C = {
      closeType: "normal",
      radius: S,
      lineWidth: c,
      fillColor: a !== "transparent" && a || void 0,
      strokeColor: n,
      normalize: true,
      lineCap: g,
      lineDash: u === me.Dotted ? [
        m && m[0] || 1,
        (m && m[1] || 2) * c
      ] : u === me.LongDotted ? [
        (m && m[0] || 1) * c,
        (m && m[1] || 2) * c
      ] : void 0
    }, N = {
      name: e,
      id: e,
      zIndex: l,
      pos: L,
      anchor: [0.5, 0.5],
      size: [v.w, v.h]
    };
    h && (N.scale = h), p2 && (N.rotate = p2), d && (N.translate = d);
    const R2 = new _spritejs$Group(N);
    if (o) {
      const D = new _spritejs$Path({
        d: "M-4,0H4M0,-4V4",
        normalize: true,
        pos: [0, 0],
        strokeColor: n,
        lineWidth: 1,
        scale: [1 / I[0], 1 / I[1]]
      });
      R2.append(D);
    }
    const P = new _spritejs$Ellipse(__spreadProps(__spreadValues({}, C), {
      pos: [0, 0]
    }));
    R2.append(P), f && (this.scaleType = Fe.proportional, this.drawEraserlines(
      {
        group: R2,
        eraserlines: f,
        pos: L,
        layer: s
      },
      r
    )), this.replace(s, e, R2);
    const M = R2.getBoundingClientRect();
    return this.isDelete && R2.setAttribute("opacity", 0), {
      x: Math.floor(M.x - b.SafeBorderPadding),
      y: Math.floor(M.y - b.SafeBorderPadding),
      w: Math.floor(M.width + b.SafeBorderPadding * 2),
      h: Math.floor(M.height + b.SafeBorderPadding * 2)
    };
  }
  computDrawPoints(t) {
    const e = jr(this.tmpPoints), s = jr(this.tmpPoints, t), o = [
      Math.floor(e.x + e.w / 2),
      Math.floor(e.y + e.h / 2)
    ];
    return {
      rect: s,
      pos: o,
      radius: [Math.floor(e.w / 2), Math.floor(e.h / 2)]
    };
  }
  updateTempPoints(t) {
    const e = t.slice(-2), s = new ve(e[0], e[1]), o = this.tmpPoints[0], { thickness: r } = this.workOptions;
    if (o.isNear(s, r))
      return false;
    if (this.tmpPoints.length === 2) {
      if (s.isNear(this.tmpPoints[1], 1))
        return false;
      this.tmpPoints[1] = s;
    } else
      this.tmpPoints.push(s);
    return true;
  }
  consumeService(t) {
    var c, l;
    const { op: e, isFullWork: s, workState: o = x.Done } = t, r = (c = this.workId) == null ? void 0 : c.toString();
    if (!r)
      return;
    this.tmpPoints.length = 0;
    for (let h = 0; h < e.length; h += 3)
      this.tmpPoints.push(new ve(e[h], e[h + 1], e[h + 2]));
    const n = s ? this.fullLayer : this.drawLayer || this.fullLayer, a = this.draw({
      workId: r,
      layer: n,
      isDrawing: false,
      isDrawEraserlines: o === x.Done
    });
    return this.oldRect = a, (l = this.vNodes) == null || l.setInfo(r, {
      rect: a,
      op: e,
      opt: this.workOptions,
      toolsType: this.toolsType,
      canRotate: this.canRotate,
      scaleType: this.scaleType,
      centerPos: b.getCenterPos(a, n)
    }), a;
  }
  clearTmpPoints() {
    this.tmpPoints.length = 0;
  }
  static updateNodeOpt(t) {
    var h, p2, d;
    const { node: e, opt: s, vNodes: o } = t, { strokeColor: r, fillColor: n, strokeType: a } = s, c = o.get(e.name);
    let l = e;
    return e.tagName === "GROUP" && (l = e.children[0]), r && (l.setAttribute("strokeColor", r), (h = c == null ? void 0 : c.opt) != null && h.strokeColor && (c.opt.strokeColor = r)), n && (n === "transparent" ? l.setAttribute("fillColor", "rgba(0,0,0,0)") : l.setAttribute("fillColor", n), (p2 = c == null ? void 0 : c.opt) != null && p2.fillColor && (c.opt.fillColor = n)), a && (c != null && c.opt) && ((d = c.opt) != null && d.strokeType) && (c.opt.strokeType = a), c && o.setInfo(e.name, c), b.updateNodeOpt(t);
  }
};
var Ae = class extends b {
  constructor(t) {
    super(t);
    y(this, "canRotate", true);
    y(this, "scaleType", Fe.all);
    y(this, "toolsType", T.Rectangle);
    y(this, "tmpPoints", []);
    y(this, "workOptions");
    y(this, "oldRect");
    y(this, "syncTimestamp");
    this.workOptions = t.toolsOpt, this.syncTimestamp = 0, this.syncUnitTime = 50;
  }
  transformData() {
    const t = jr(this.tmpPoints);
    return [
      [t.x, t.y, 0],
      [t.x + t.w, t.y, 0],
      [t.x + t.w, t.y + t.h, 0],
      [t.x, t.y + t.h, 0]
    ];
  }
  computDrawPoints(t) {
    const { thickness: e } = this.workOptions, s = [];
    for (const n of t)
      s.push(new U(...n));
    const o = jr(s, e), r = [
      o.x + o.w / 2,
      o.y + o.h / 2
    ];
    return {
      rect: o,
      pos: r,
      points: s.map((n) => n.XY).flat(1)
    };
  }
  consume(t) {
    const {
      data: e,
      isFullWork: s,
      isSubWorker: o,
      isMainThread: r,
      smoothSync: n,
      isSimpleWorker: a
    } = t, c = this.workId, { op: l } = e, h = l == null ? void 0 : l.length;
    if (!h || h < 2)
      return { type: R.None };
    let p2;
    if (this.tmpPoints.length === 0 ? (this.tmpPoints = [new ve(l[0], l[1])], p2 = false) : p2 = this.updateTempPoints(l), !p2)
      return { type: R.None };
    const d = this.transformData();
    let u;
    if (o || r || a) {
      const m = s ? this.fullLayer : this.drawLayer || this.fullLayer;
      u = this.draw({ ps: d, workId: c, layer: m, isDrawing: true });
    }
    if (a) {
      const m = SI(u, this.oldRect);
      this.oldRect = u;
      const g = __spreadValues({
        rect: m,
        type: R.DrawWork,
        dataType: q.Local
      }, this.baseConsumeResult);
      if (n) {
        const I = Date.now();
        I - this.syncTimestamp > this.syncUnitTime && (this.syncTimestamp = I, g.op = d.flat(1), g.index = 0, g.isSync = true);
      }
      return g;
    }
    if (!o && n) {
      const m = Date.now();
      return m - this.syncTimestamp > this.syncUnitTime ? (this.syncTimestamp = m, __spreadValues({
        type: R.DrawWork,
        dataType: q.Local,
        op: d.flat(1),
        isSync: true,
        index: 0
      }, this.baseConsumeResult)) : { type: R.None };
    }
    const f = SI(u, this.oldRect);
    return this.oldRect = u, __spreadValues({
      rect: f,
      type: R.DrawWork,
      dataType: q.Local
    }, this.baseConsumeResult);
  }
  consumeAll() {
    var a;
    const t = this.workId;
    if (this.tmpPoints.length < 2)
      return {
        type: R.RemoveNode,
        removeIds: [t]
      };
    const e = this.transformData(), s = this.fullLayer, o = this.draw({
      ps: e,
      workId: t,
      layer: s,
      isDrawing: false
    });
    this.oldRect = o;
    const r = e.flat(1), n = _t(r);
    return (a = this.vNodes) == null || a.setInfo(t, {
      rect: o,
      op: r,
      opt: this.workOptions,
      toolsType: this.toolsType,
      scaleType: this.scaleType,
      canRotate: this.canRotate,
      centerPos: o && b.getCenterPos(o, s)
    }), __spreadProps(__spreadValues({
      rect: o,
      type: R.FullWork,
      dataType: q.Local,
      ops: n,
      isSync: true
    }, this.baseConsumeResult), {
      updateNodeOpt: {
        useAnimation: true
      }
    });
  }
  draw(t) {
    const {
      workId: e,
      layer: s,
      isDrawing: o,
      ps: r,
      replaceId: n,
      isDrawEraserlines: a = true
    } = t, {
      strokeColor: c,
      fillColor: l,
      thickness: h,
      zIndex: p2,
      scale: d,
      rotate: u,
      translate: f,
      strokeType: m,
      eraserlines: g,
      lineCap: I,
      lineDash: S
    } = this.workOptions, v = s.worldPosition, L = s.worldScaling, { points: C, rect: N, pos: R2 } = this.computDrawPoints(r), P = {
      close: true,
      normalize: true,
      points: C,
      lineWidth: h,
      fillColor: l !== "transparent" && l || void 0,
      strokeColor: c,
      lineCap: I,
      lineDash: m === me.Dotted ? [
        S && S[0] || 1,
        (S && S[1] || 2) * h
      ] : m === me.LongDotted ? [
        (S && S[0] || 1) * h,
        (S && S[1] || 2) * h
      ] : void 0
    };
    let M = {
      x: Math.floor(
        N.x * L[0] + v[0] - b.SafeBorderPadding
      ),
      y: Math.floor(
        N.y * L[1] + v[1] - b.SafeBorderPadding
      ),
      w: Math.floor(
        N.w * L[0] + 2 * b.SafeBorderPadding
      ),
      h: Math.floor(
        N.h * L[0] + 2 * b.SafeBorderPadding
      )
    };
    const D = new _spritejs$Group({
      name: e,
      id: e,
      zIndex: p2,
      pos: R2,
      anchor: [0.5, 0.5],
      size: [N.w, N.h],
      scale: d,
      rotate: u,
      translate: f
    }), B = new _spritejs$Polyline(__spreadProps(__spreadValues({}, P), {
      pos: [0, 0]
    }));
    if (D.appendChild(B), o) {
      const F = new _spritejs$Path({
        d: "M-4,0H4M0,-4V4",
        normalize: true,
        pos: [0, 0],
        strokeColor: c,
        lineWidth: 1,
        scale: [1 / L[0], 1 / L[1]]
      });
      D.appendChild(F);
    }
    if (g && (this.scaleType = Fe.proportional, this.drawEraserlines(
      {
        group: D,
        eraserlines: g,
        pos: R2,
        layer: s
      },
      a
    )), this.replace(s, n || e, D), d || u || f) {
      const F = D.getBoundingClientRect();
      M = {
        x: Math.floor(F.x - b.SafeBorderPadding),
        y: Math.floor(F.y - b.SafeBorderPadding),
        w: Math.floor(F.width + 2 * b.SafeBorderPadding),
        h: Math.floor(F.height + 2 * b.SafeBorderPadding)
      };
    }
    return this.isDelete && D.setAttribute("opacity", 0), M;
  }
  updateTempPoints(t) {
    const e = t.slice(-2), s = new ve(e[0], e[1]), o = this.tmpPoints[0], { thickness: r } = this.workOptions;
    if (o.isNear(s, r))
      return false;
    if (this.tmpPoints.length === 2) {
      if (s.isNear(this.tmpPoints[1], 1))
        return false;
      this.tmpPoints[1] = s;
    } else
      this.tmpPoints.push(s);
    return true;
  }
  consumeService(t) {
    var h, p2;
    const {
      op: e,
      isFullWork: s,
      replaceId: o,
      workState: r = x.Done
    } = t, n = (h = this.workId) == null ? void 0 : h.toString();
    if (!n)
      return;
    const a = [];
    for (let d = 0; d < e.length; d += 3)
      a.push([e[d], e[d + 1], e[d + 2]]);
    const c = s ? this.fullLayer : this.drawLayer || this.fullLayer, l = this.draw({
      ps: a,
      workId: n,
      layer: c,
      isDrawing: false,
      replaceId: o,
      isDrawEraserlines: r === x.Done
    });
    return this.oldRect = l, (p2 = this.vNodes) == null || p2.setInfo(n, {
      rect: l,
      op: e,
      opt: this.workOptions,
      toolsType: this.toolsType,
      scaleType: this.scaleType,
      canRotate: this.canRotate,
      centerPos: l && b.getCenterPos(l, c)
    }), l;
  }
  clearTmpPoints() {
    this.tmpPoints.length = 0;
  }
  static updateNodeOpt(t) {
    var h, p2, d;
    const { node: e, opt: s, vNodes: o } = t, { strokeColor: r, fillColor: n, strokeType: a } = s, c = o.get(e.name);
    let l = e;
    return e.tagName === "GROUP" && (l = e.children[0]), r && (l.setAttribute("strokeColor", r), (h = c == null ? void 0 : c.opt) != null && h.strokeColor && (c.opt.strokeColor = r)), n && (n === "transparent" ? l.setAttribute("fillColor", "rgba(0,0,0,0)") : l.setAttribute("fillColor", n), (p2 = c == null ? void 0 : c.opt) != null && p2.fillColor && (c.opt.fillColor = n)), a && (c != null && c.opt) && ((d = c.opt) != null && d.strokeType) && (c.opt.strokeType = a), c && o.setInfo(e.name, c), b.updateNodeOpt(t);
  }
};
var Ee = class extends b {
  constructor(t) {
    super(t);
    y(this, "canRotate", false);
    y(this, "scaleType", Fe.all);
    y(this, "toolsType", T.Star);
    y(this, "tmpPoints", []);
    y(this, "workOptions");
    y(this, "oldRect");
    y(this, "syncTimestamp");
    this.workOptions = t.toolsOpt, this.syncTimestamp = 0, this.syncUnitTime = 50;
  }
  consume(t) {
    const {
      data: e,
      isFullWork: s,
      isSubWorker: o,
      isMainThread: r,
      smoothSync: n,
      isSimpleWorker: a
    } = t, c = this.workId, { op: l, syncUnitTime: h } = e, p2 = l == null ? void 0 : l.length;
    if (!p2 || p2 < 2)
      return { type: R.None };
    h && (this.syncUnitTime = h);
    let d;
    if (this.tmpPoints.length === 0 ? (this.tmpPoints = [new ve(l[0], l[1])], d = false) : d = this.updateTempPoints(l), !d)
      return { type: R.None };
    let u;
    if (o || r || a) {
      const m = s ? this.fullLayer : this.drawLayer || this.fullLayer;
      u = this.draw({ workId: c, layer: m, isDrawing: true });
    }
    if (a) {
      const m = SI(u, this.oldRect);
      this.oldRect = u;
      const g = __spreadValues({
        rect: m,
        type: R.DrawWork,
        dataType: q.Local
      }, this.baseConsumeResult);
      if (n) {
        const I = Date.now();
        I - this.syncTimestamp > this.syncUnitTime && (this.syncTimestamp = I, g.op = this.tmpPoints.map((S) => [...S.XY, 0]).flat(1), g.index = 0, g.isSync = true);
      }
      return g;
    }
    if (!o && n) {
      const m = Date.now();
      return m - this.syncTimestamp > this.syncUnitTime ? (this.syncTimestamp = m, __spreadValues({
        type: R.DrawWork,
        dataType: q.Local,
        op: this.tmpPoints.map((g) => [...g.XY, 0]).flat(1),
        isSync: true,
        index: 0
      }, this.baseConsumeResult)) : { type: R.None };
    }
    const f = SI(u, this.oldRect);
    return this.oldRect = u, __spreadValues({
      rect: f,
      type: R.DrawWork,
      dataType: q.Local
    }, this.baseConsumeResult);
  }
  consumeAll() {
    var n;
    const t = this.workId;
    if (this.tmpPoints.length < 2)
      return {
        type: R.RemoveNode,
        removeIds: [t]
      };
    const e = this.fullLayer, s = this.draw({
      workId: t,
      layer: e,
      isDrawing: false
    });
    this.oldRect = s;
    const o = this.tmpPoints.map((a) => [...a.XY, 0]).flat(1), r = _t(o);
    return (n = this.vNodes) == null || n.setInfo(t, {
      rect: s,
      op: o,
      opt: this.workOptions,
      toolsType: this.toolsType,
      scaleType: this.scaleType,
      canRotate: this.canRotate,
      centerPos: s && b.getCenterPos(s, e)
    }), __spreadProps(__spreadValues({
      rect: s,
      type: R.FullWork,
      dataType: q.Local,
      ops: r,
      isSync: true
    }, this.baseConsumeResult), {
      updateNodeOpt: {
        useAnimation: true
      }
    });
  }
  draw(t) {
    const { workId: e, layer: s, isDrawing: o, isDrawEraserlines: r = true } = t, {
      strokeColor: n,
      fillColor: a,
      thickness: c,
      zIndex: l,
      vertices: h,
      innerVerticeStep: p2,
      innerRatio: d,
      scale: u,
      rotate: f,
      translate: m,
      strokeType: g,
      eraserlines: I,
      lineDash: S,
      lineCap: v
    } = this.workOptions, L = s.worldScaling, { rect: C, pos: N, points: R2 } = this.computDrawPoints(
      c,
      h,
      p2,
      d
    ), P = {
      close: true,
      points: R2,
      lineWidth: c,
      fillColor: a !== "transparent" && a || void 0,
      strokeColor: n,
      normalize: true,
      lineCap: v,
      lineDash: g === me.Dotted ? [
        S && S[0] || 1,
        (S && S[1] || 2) * c
      ] : g === me.LongDotted ? [
        (S && S[0] || 1) * c,
        (S && S[1] || 2) * c
      ] : void 0
    }, M = {
      name: e,
      id: e,
      zIndex: l,
      pos: N,
      anchor: [0.5, 0.5],
      size: [C.w, C.h]
    };
    u && (M.scale = u), f && (M.rotate = f), m && (M.translate = m);
    const D = new _spritejs$Group(M);
    if (o) {
      const U2 = new _spritejs$Path({
        d: "M-4,0H4M0,-4V4",
        normalize: true,
        pos: [0, 0],
        strokeColor: n,
        lineWidth: 1,
        scale: [1 / L[0], 1 / L[1]]
      });
      D.append(U2);
    }
    const B = new _spritejs$Polyline(__spreadProps(__spreadValues({}, P), {
      pos: [0, 0]
    }));
    D.append(B), I && (this.scaleType = Fe.proportional, this.drawEraserlines(
      {
        group: D,
        eraserlines: I,
        pos: N,
        layer: s
      },
      r
    )), this.replace(s, e, D);
    const F = D.getBoundingClientRect();
    return this.isDelete && D.setAttribute("opacity", 0), {
      x: Math.floor(F.x - b.SafeBorderPadding),
      y: Math.floor(F.y - b.SafeBorderPadding),
      w: Math.floor(F.width + b.SafeBorderPadding * 2),
      h: Math.floor(F.height + b.SafeBorderPadding * 2)
    };
  }
  computDrawPoints(t, e, s, o) {
    const r = jr(this.tmpPoints), n = [
      Math.floor(r.x + r.w / 2),
      Math.floor(r.y + r.h / 2)
    ], a = PI(r.w, r.h), c = Math.floor(Math.min(r.w, r.h) / 2), l = o * c, h = [], p2 = 2 * Math.PI / e;
    for (let u = 0; u < e; u++) {
      const f = u * p2 - 0.5 * Math.PI;
      let m, g;
      u % s === 1 ? (m = l * a[0] * Math.cos(f), g = l * a[1] * Math.sin(f)) : (m = c * a[0] * Math.cos(f), g = c * a[1] * Math.sin(f), h.push(m, g)), h.push(m, g);
    }
    return {
      rect: jr(this.tmpPoints, t),
      pos: n,
      points: h
    };
  }
  updateTempPoints(t) {
    const e = t.slice(-2), s = new ve(e[0], e[1]), o = this.tmpPoints[0], { thickness: r } = this.workOptions;
    if (o.isNear(s, r) || ve.Sub(o, s).XY.includes(0))
      return false;
    if (this.tmpPoints.length === 2) {
      if (s.isNear(this.tmpPoints[1], 1))
        return false;
      this.tmpPoints[1] = s;
    } else
      this.tmpPoints.push(s);
    return true;
  }
  consumeService(t) {
    var c, l;
    const { op: e, isFullWork: s, workState: o = x.Done } = t, r = (c = this.workId) == null ? void 0 : c.toString();
    if (!r)
      return;
    this.tmpPoints.length = 0;
    for (let h = 0; h < e.length; h += 3)
      this.tmpPoints.push(new ve(e[h], e[h + 1], e[h + 2]));
    const n = s ? this.fullLayer : this.drawLayer || this.fullLayer, a = this.draw({
      workId: r,
      layer: n,
      isDrawing: false,
      isDrawEraserlines: o === x.Done
    });
    return this.oldRect = a, (l = this.vNodes) == null || l.setInfo(r, {
      rect: a,
      op: e,
      opt: this.workOptions,
      toolsType: this.toolsType,
      scaleType: this.scaleType,
      canRotate: this.canRotate,
      centerPos: a && b.getCenterPos(a, n)
    }), a;
  }
  clearTmpPoints() {
    this.tmpPoints.length = 0;
  }
  static updateNodeOpt(t) {
    var m;
    const { node: e, opt: s, vNodes: o } = t, {
      strokeColor: r,
      fillColor: n,
      toolsType: a,
      vertices: c,
      innerVerticeStep: l,
      innerRatio: h,
      strokeType: p2
    } = s, d = o.get(e.name), u = d == null ? void 0 : d.opt;
    let f = e;
    return e.tagName === "GROUP" && (f = e.children[0]), r && (f.setAttribute("strokeColor", r), u != null && u.strokeColor && (u.strokeColor = r)), n && (n === "transparent" ? f.setAttribute("fillColor", "rgba(0,0,0,0)") : f.setAttribute("fillColor", n), u != null && u.fillColor && (u.fillColor = n)), a === T.Star && (c && (u.vertices = c), l && (u.innerVerticeStep = l), h && (u.innerRatio = h)), p2 && (d != null && d.opt) && ((m = d.opt) != null && m.strokeType) && (d.opt.strokeType = p2), d && o.setInfo(e.name, __spreadProps(__spreadValues({}, d), { opt: u })), b.updateNodeOpt(t);
  }
};
var Be = class extends b {
  constructor(t) {
    super(t);
    y(this, "canRotate", false);
    y(this, "scaleType", Fe.all);
    y(this, "toolsType", T.Polygon);
    y(this, "tmpPoints", []);
    y(this, "workOptions");
    y(this, "oldRect");
    y(this, "syncTimestamp");
    this.workOptions = t.toolsOpt, this.syncTimestamp = 0, this.syncUnitTime = 50;
  }
  consume(t) {
    const {
      data: e,
      isFullWork: s,
      isSubWorker: o,
      isMainThread: r,
      smoothSync: n,
      isSimpleWorker: a
    } = t, { op: c, syncUnitTime: l } = e;
    l && (this.syncUnitTime = l);
    const h = this.workId, p2 = c == null ? void 0 : c.length;
    if (!p2 || p2 < 2)
      return { type: R.None };
    let d;
    if (this.tmpPoints.length === 0 ? (this.tmpPoints = [new ve(c[0], c[1])], d = false) : d = this.updateTempPoints(c), !d)
      return { type: R.None };
    let u;
    if (o || r || a) {
      const m = s ? this.fullLayer : this.drawLayer || this.fullLayer;
      u = this.draw({ workId: h, layer: m, isDrawing: true });
    }
    if (a) {
      const m = SI(u, this.oldRect);
      this.oldRect = u;
      const g = __spreadValues({
        type: R.DrawWork,
        rect: m,
        dataType: q.Local
      }, this.baseConsumeResult);
      if (n) {
        const I = Date.now();
        I - this.syncTimestamp > this.syncUnitTime && (this.syncTimestamp = I, g.op = this.tmpPoints.map((S) => [...S.XY, 0]).flat(1), g.index = 0, g.isSync = true);
      }
      return g;
    }
    if (!o && n) {
      const m = Date.now();
      return m - this.syncTimestamp > this.syncUnitTime ? (this.syncTimestamp = m, __spreadValues({
        type: R.DrawWork,
        dataType: q.Local,
        op: this.tmpPoints.map((g) => [...g.XY, 0]).flat(1),
        isSync: true,
        index: 0
      }, this.baseConsumeResult)) : { type: R.None };
    }
    const f = SI(u, this.oldRect);
    return this.oldRect = u, __spreadValues({
      rect: f,
      type: R.DrawWork,
      dataType: q.Local
    }, this.baseConsumeResult);
  }
  consumeAll() {
    var n;
    const t = this.workId;
    if (this.tmpPoints.length < 2)
      return {
        type: R.RemoveNode,
        removeIds: [t]
      };
    const e = this.fullLayer, s = this.draw({
      workId: t,
      layer: e,
      isDrawing: false
    });
    this.oldRect = s;
    const o = this.tmpPoints.map((a) => [...a.XY, 0]).flat(1), r = _t(o);
    return (n = this.vNodes) == null || n.setInfo(t, {
      rect: s,
      op: o,
      opt: this.workOptions,
      toolsType: this.toolsType,
      scaleType: this.scaleType,
      canRotate: this.canRotate,
      centerPos: s && b.getCenterPos(s, e)
    }), __spreadProps(__spreadValues({
      rect: s,
      type: R.FullWork,
      dataType: q.Local,
      ops: r,
      isSync: true
    }, this.baseConsumeResult), {
      updateNodeOpt: {
        useAnimation: true
      }
    });
  }
  draw(t) {
    const { workId: e, layer: s, isDrawing: o, isDrawEraserlines: r = true } = t, {
      strokeColor: n,
      fillColor: a,
      thickness: c,
      zIndex: l,
      vertices: h,
      scale: p2,
      rotate: d,
      translate: u,
      strokeType: f,
      eraserlines: m,
      lineCap: g,
      lineDash: I
    } = this.workOptions, S = s.worldScaling, { rect: v, pos: L, points: C } = this.computDrawPoints(c, h), N = {
      close: true,
      points: C,
      lineWidth: c,
      fillColor: a !== "transparent" && a || void 0,
      strokeColor: n,
      normalize: true,
      lineCap: g,
      lineDash: f === me.Dotted ? [
        I && I[0] || 1,
        (I && I[1] || 2) * c
      ] : f === me.LongDotted ? [
        (I && I[0] || 1) * c,
        (I && I[1] || 2) * c
      ] : void 0
    }, R2 = {
      name: e,
      id: e,
      zIndex: l,
      pos: L,
      anchor: [0.5, 0.5],
      size: [v.w, v.h]
    };
    p2 && (R2.scale = p2), d && (R2.rotate = d), u && (R2.translate = u);
    const P = new _spritejs$Group(R2);
    if (o) {
      const B = new _spritejs$Path({
        d: "M-4,0H4M0,-4V4",
        normalize: true,
        pos: [0, 0],
        strokeColor: n,
        lineWidth: 1,
        scale: [1 / S[0], 1 / S[1]]
      });
      P.append(B);
    }
    const M = new _spritejs$Polyline(__spreadProps(__spreadValues({}, N), {
      pos: [0, 0]
    }));
    P.append(M), m && (this.scaleType = Fe.proportional, this.drawEraserlines(
      {
        group: P,
        eraserlines: m,
        pos: L,
        layer: s
      },
      r
    )), this.replace(s, e, P);
    const D = P.getBoundingClientRect();
    return this.isDelete && P.setAttribute("opacity", 0), {
      x: Math.floor(D.x - b.SafeBorderPadding),
      y: Math.floor(D.y - b.SafeBorderPadding),
      w: Math.floor(D.width + b.SafeBorderPadding * 2),
      h: Math.floor(D.height + b.SafeBorderPadding * 2)
    };
  }
  computDrawPoints(t, e) {
    const s = jr(this.tmpPoints), o = [
      Math.floor(s.x + s.w / 2),
      Math.floor(s.y + s.h / 2)
    ], r = PI(s.w, s.h), n = Math.floor(Math.min(s.w, s.h) / 2), a = [], c = 2 * Math.PI / e;
    for (let h = 0; h < e; h++) {
      const p2 = h * c - 0.5 * Math.PI, d = n * r[0] * Math.cos(p2), u = n * r[1] * Math.sin(p2);
      a.push(d, u);
    }
    return {
      rect: jr(this.tmpPoints, t),
      pos: o,
      points: a
    };
  }
  updateTempPoints(t) {
    const e = t.slice(-2), s = new ve(e[0], e[1]), o = this.tmpPoints[0], { thickness: r } = this.workOptions;
    if (o.isNear(s, r) || ve.Sub(o, s).XY.includes(0))
      return false;
    if (this.tmpPoints.length === 2) {
      if (s.isNear(this.tmpPoints[1], 1))
        return false;
      this.tmpPoints[1] = s;
    } else
      this.tmpPoints.push(s);
    return true;
  }
  consumeService(t) {
    var c, l;
    const { op: e, isFullWork: s, workState: o = x.Done } = t, r = (c = this.workId) == null ? void 0 : c.toString();
    if (!r)
      return;
    this.tmpPoints.length = 0;
    for (let h = 0; h < e.length; h += 3)
      this.tmpPoints.push(new ve(e[h], e[h + 1], e[h + 2]));
    const n = s ? this.fullLayer : this.drawLayer || this.fullLayer, a = this.draw({
      workId: r,
      layer: n,
      isDrawing: false,
      isDrawEraserlines: o === x.Done
    });
    return this.oldRect = a, (l = this.vNodes) == null || l.setInfo(r, {
      rect: a,
      op: e,
      opt: this.workOptions,
      toolsType: this.toolsType,
      scaleType: this.scaleType,
      canRotate: this.canRotate,
      centerPos: a && b.getCenterPos(a, n)
    }), a;
  }
  clearTmpPoints() {
    this.tmpPoints.length = 0;
  }
  static updateNodeOpt(t) {
    var u;
    const { node: e, opt: s, vNodes: o } = t, { strokeColor: r, fillColor: n, toolsType: a, vertices: c, strokeType: l } = s, h = o.get(e.name), p2 = h == null ? void 0 : h.opt;
    let d = e;
    return e.tagName === "GROUP" && (d = e.children[0]), r && (d.setAttribute("strokeColor", r), p2 != null && p2.strokeColor && (p2.strokeColor = r)), n && (n === "transparent" ? d.setAttribute("fillColor", "rgba(0,0,0,0)") : d.setAttribute("fillColor", n), p2 != null && p2.fillColor && (p2.fillColor = n)), a === T.Polygon && c && (p2.vertices = c), l && (h != null && h.opt) && ((u = h.opt) != null && u.strokeType) && (h.opt.strokeType = l), h && o.setInfo(e.name, __spreadProps(__spreadValues({}, h), { opt: p2 })), b.updateNodeOpt(t);
  }
};
var ot2 = class _ot {
  static bezier(i, t) {
    const e = [];
    for (let s = 0; s < t.length; s += 4) {
      const o = t[s], r = t[s + 1], n = t[s + 2], a = t[s + 3];
      o && r && n && a ? e.push(..._ot.getBezierPoints(i, o, r, n, a)) : o && r && n ? e.push(..._ot.getBezierPoints(i, o, r, n)) : o && r ? e.push(..._ot.getBezierPoints(i, o, r)) : o && e.push(o);
    }
    return e;
  }
  /**
   * @desc 获取点，这里可以设置点的个数
   * @param {number} num 点个数
   * @param {Vec2d} p1 点坐标
   * @param {Vec2d} p2 点坐标
   * @param {Vec2d} p3 点坐标
   * @param {Vec2d} p4 点坐标
   * 如果参数是 num, p1, p2 为一阶贝塞尔
   * 如果参数是 num, p1, c1, p2 为二阶贝塞尔
   * 如果参数是 num, p1, c1, c2, p2 为三阶贝塞尔
   */
  static getBezierPoints(i = 10, t, e, s, o) {
    let r = null;
    const n = [];
    !s && !o ? r = _ot.oneBezier : s && !o ? r = _ot.twoBezier : s && o && (r = _ot.threeBezier);
    for (let a = 0; a < i; a++)
      r && n.push(r(a / i, t, e, s, o));
    return o ? n.push(o) : s && n.push(s), n;
  }
  /**
   * @desc 一阶贝塞尔
   * @param {number} t 当前百分比
   * @param {Vec2d} p1 起点坐标
   * @param {Vec2d} p2 终点坐标
   */
  static oneBezier(i, t, e) {
    const s = t.x + (e.x - t.x) * i, o = t.y + (e.y - t.y) * i;
    return new U(s, o);
  }
  /**
   * @desc 二阶贝塞尔
   * @param {number} t 当前百分比
   * @param {Array} p1 起点坐标
   * @param {Array} p2 终点坐标
   * @param {Array} cp 控制点
   */
  static twoBezier(i, t, e, s) {
    const o = (1 - i) * (1 - i) * t.x + 2 * i * (1 - i) * e.x + i * i * s.x, r = (1 - i) * (1 - i) * t.y + 2 * i * (1 - i) * e.y + i * i * s.y;
    return new U(o, r);
  }
  /**
   * @desc 三阶贝塞尔
   * @param {number} t 当前百分比
   * @param {Array} p1 起点坐标
   * @param {Array} p2 终点坐标
   * @param {Array} cp1 控制点1
   * @param {Array} cp2 控制点2
   */
  static threeBezier(i, t, e, s, o) {
    const r = t.x * (1 - i) * (1 - i) * (1 - i) + 3 * e.x * i * (1 - i) * (1 - i) + 3 * s.x * i * i * (1 - i) + o.x * i * i * i, n = t.y * (1 - i) * (1 - i) * (1 - i) + 3 * e.y * i * (1 - i) * (1 - i) + 3 * s.y * i * i * (1 - i) + o.y * i * i * i;
    return new U(r, n);
  }
};
var Fe2 = class extends b {
  constructor(t) {
    super(t);
    y(this, "canRotate", false);
    y(this, "scaleType", Fe.all);
    y(this, "toolsType", T.SpeechBalloon);
    y(this, "ratio", 0.8);
    y(this, "tmpPoints", []);
    y(this, "workOptions");
    y(this, "oldRect");
    y(this, "syncTimestamp");
    this.workOptions = t.toolsOpt, this.syncTimestamp = 0, this.syncUnitTime = 50;
  }
  consume(t) {
    var f;
    const {
      data: e,
      isFullWork: s,
      isSubWorker: o,
      isMainThread: r,
      smoothSync: n,
      isSimpleWorker: a
    } = t, c = (f = e == null ? void 0 : e.workId) == null ? void 0 : f.toString();
    if (!c)
      return { type: R.None };
    const { op: l } = e, h = l == null ? void 0 : l.length;
    if (!h || h < 2)
      return { type: R.None };
    let p2;
    if (this.tmpPoints.length === 0 ? (this.tmpPoints = [new ve(l[0], l[1])], p2 = false) : p2 = this.updateTempPoints(l), !p2)
      return { type: R.None };
    let d;
    if (o || r || a) {
      const m = s ? this.fullLayer : this.drawLayer || this.fullLayer;
      d = this.draw({ workId: c, layer: m, isDrawing: true });
    }
    if (a) {
      const m = SI(d, this.oldRect);
      this.oldRect = d;
      const g = __spreadValues({
        rect: m,
        type: R.DrawWork,
        dataType: q.Local
      }, this.baseConsumeResult);
      if (n) {
        const I = Date.now();
        I - this.syncTimestamp > this.syncUnitTime && (this.syncTimestamp = I, g.op = this.tmpPoints.map((S) => [...S.XY, 0]).flat(1), g.index = 0, g.isSync = true);
      }
      return g;
    }
    if (!o && n) {
      const m = Date.now();
      return m - this.syncTimestamp > this.syncUnitTime ? (this.syncTimestamp = m, __spreadValues({
        type: R.DrawWork,
        dataType: q.Local,
        op: this.tmpPoints.map((g) => [...g.XY, 0]).flat(1),
        isSync: true,
        index: 0
      }, this.baseConsumeResult)) : { type: R.None };
    }
    const u = SI(d, this.oldRect);
    return this.oldRect = d, __spreadValues({
      rect: u,
      type: R.DrawWork,
      dataType: q.Local
    }, this.baseConsumeResult);
  }
  consumeAll() {
    var n;
    const t = this.workId;
    if (this.tmpPoints.length < 2)
      return {
        type: R.RemoveNode,
        removeIds: [t]
      };
    const e = this.fullLayer, s = this.draw({
      workId: t,
      layer: e,
      isDrawing: false
    });
    this.oldRect = s;
    const o = this.tmpPoints.map((a) => [...a.XY, 0]).flat(1), r = _t(o);
    return (n = this.vNodes) == null || n.setInfo(t, {
      rect: s,
      op: o,
      opt: this.workOptions,
      toolsType: this.toolsType,
      scaleType: this.scaleType,
      canRotate: this.canRotate,
      centerPos: s && b.getCenterPos(s, e)
    }), __spreadProps(__spreadValues({
      rect: s,
      type: R.FullWork,
      dataType: q.Local,
      ops: r,
      isSync: true
    }, this.baseConsumeResult), {
      updateNodeOpt: {
        useAnimation: true
      }
    });
  }
  draw(t) {
    const { workId: e, layer: s, isDrawEraserlines: o = true } = t, {
      strokeColor: r,
      fillColor: n,
      thickness: a,
      zIndex: c,
      placement: l,
      scale: h,
      rotate: p2,
      translate: d,
      strokeType: u,
      eraserlines: f,
      lineDash: m,
      lineCap: g
    } = this.workOptions, { rect: I, pos: S, points: v } = this.computDrawPoints(a, l), L = {
      points: v.map((M) => M.XY),
      lineWidth: a,
      fillColor: n !== "transparent" && n || void 0,
      strokeColor: r,
      normalize: true,
      className: `${S[0]},${S[1]}`,
      close: true,
      lineCap: g,
      lineDash: u === me.Dotted ? [
        m && m[0] || 1,
        (m && m[1] || 2) * a
      ] : u === me.LongDotted ? [
        (m && m[0] || 1) * a,
        (m && m[1] || 2) * a
      ] : void 0
    }, C = {
      name: e,
      id: e,
      zIndex: c,
      pos: S,
      anchor: [0.5, 0.5],
      size: [I.w, I.h]
    };
    h && (C.scale = h), p2 && (C.rotate = p2), d && (C.translate = d);
    const N = new _spritejs$Group(C), R2 = new _spritejs$Polyline(__spreadProps(__spreadValues({}, L), {
      pos: [0, 0]
    }));
    N.append(R2), f && (this.scaleType = Fe.proportional, this.drawEraserlines(
      {
        group: N,
        eraserlines: f,
        pos: S,
        layer: s
      },
      o
    )), this.replace(s, e, N);
    const P = N.getBoundingClientRect();
    return this.isDelete && N.setAttribute("opacity", 0), {
      x: Math.floor(P.x - b.SafeBorderPadding),
      y: Math.floor(P.y - b.SafeBorderPadding),
      w: Math.floor(P.width + b.SafeBorderPadding * 2),
      h: Math.floor(P.height + b.SafeBorderPadding * 2)
    };
  }
  transformControlPoints(t) {
    const e = jr(this.tmpPoints);
    switch (t) {
      case "bottom":
      case "bottomLeft":
      case "bottomRight": {
        const s = e.y + e.h * this.ratio;
        return [
          new U(e.x, e.y, 0),
          new U(e.x + e.w, e.y, 0),
          new U(e.x + e.w, s, 0),
          new U(e.x, s, 0)
        ];
      }
      case "top":
      case "topLeft":
      case "topRight": {
        const s = e.y + e.h * (1 - this.ratio);
        return [
          new U(e.x, s, 0),
          new U(e.x + e.w, s, 0),
          new U(e.x + e.w, e.y + e.h, 0),
          new U(e.x, e.y + e.h, 0)
        ];
      }
      case "left":
      case "leftBottom":
      case "leftTop": {
        const s = e.x + e.w * (1 - this.ratio);
        return [
          new U(s, e.y, 0),
          new U(e.x + e.w, e.y, 0),
          new U(e.x + e.w, e.y + e.h, 0),
          new U(s, e.y + e.h, 0)
        ];
      }
      case "right":
      case "rightBottom":
      case "rightTop": {
        const s = e.x + e.w * this.ratio;
        return [
          new U(e.x, e.y, 0),
          new U(s, e.y, 0),
          new U(s, e.y + e.h, 0),
          new U(e.x, e.y + e.h, 0)
        ];
      }
    }
  }
  computDrawPoints(t, e) {
    const s = jr(this.tmpPoints), o = this.transformControlPoints(e), r = Math.floor(s.w * 0.1), n = Math.floor(s.h * 0.1), a = [], c = U.Add(o[0], new U(0, n, 0)), l = U.Add(o[0], new U(r, 0, 0)), h = ot2.getBezierPoints(
      10,
      c,
      o[0],
      l
    ), p2 = U.Sub(o[1], new U(r, 0, 0)), d = U.Add(
      o[1],
      new U(0, n, 0)
    ), u = ot2.getBezierPoints(
      10,
      p2,
      o[1],
      d
    ), f = U.Sub(o[2], new U(0, n, 0)), m = U.Sub(
      o[2],
      new U(r, 0, 0)
    ), g = ot2.getBezierPoints(
      10,
      f,
      o[2],
      m
    ), I = U.Add(
      o[3],
      new U(r, 0, 0)
    ), S = U.Sub(
      o[3],
      new U(0, n, 0)
    ), v = ot2.getBezierPoints(
      10,
      I,
      o[3],
      S
    ), L = r * (1 - this.ratio) * 10, C = n * (1 - this.ratio) * 10;
    switch (e) {
      case "bottom": {
        const P = U.Sub(
          o[2],
          new U(r * 5 - L / 2, 0, 0)
        ), M = U.Sub(
          o[2],
          new U(r * 5, -C, 0)
        ), D = U.Sub(
          o[2],
          new U(r * 5 + L / 2, 0, 0)
        );
        a.push(
          M,
          D,
          ...v,
          ...h,
          ...u,
          ...g,
          P
        );
        break;
      }
      case "bottomRight": {
        const P = U.Sub(
          o[2],
          new U(r * 1.1, 0, 0)
        ), M = U.Sub(
          o[2],
          new U(r * 1.1 + L / 2, -C, 0)
        ), D = U.Sub(
          o[2],
          new U(r * 1.1 + L, 0, 0)
        );
        a.push(
          M,
          D,
          ...v,
          ...h,
          ...u,
          ...g,
          P
        );
        break;
      }
      case "bottomLeft": {
        const P = U.Add(
          o[3],
          new U(r * 1.1 + L, 0, 0)
        ), M = U.Add(
          o[3],
          new U(r * 1.1 + L / 2, C, 0)
        ), D = U.Add(
          o[3],
          new U(r * 1.1, 0, 0)
        );
        a.push(
          M,
          D,
          ...v,
          ...h,
          ...u,
          ...g,
          P
        );
        break;
      }
      case "top": {
        const P = U.Sub(
          o[1],
          new U(r * 5 - L / 2, 0, 0)
        ), M = U.Sub(
          o[1],
          new U(r * 5, C, 0)
        ), D = U.Sub(
          o[1],
          new U(r * 5 + L / 2, 0, 0)
        );
        a.push(
          M,
          P,
          ...u,
          ...g,
          ...v,
          ...h,
          D
        );
        break;
      }
      case "topRight": {
        const P = U.Sub(
          o[1],
          new U(r * 1.1, 0, 0)
        ), M = U.Sub(
          o[1],
          new U(r * 1.1 + L / 2, C, 0)
        ), D = U.Sub(
          o[1],
          new U(r * 1.1 + L, 0, 0)
        );
        a.push(
          M,
          P,
          ...u,
          ...g,
          ...v,
          ...h,
          D
        );
        break;
      }
      case "topLeft": {
        const P = U.Add(
          o[0],
          new U(r * 1.1 + L, 0, 0)
        ), M = U.Add(
          o[0],
          new U(r * 1.1 + L / 2, -C, 0)
        ), D = U.Add(
          o[0],
          new U(r * 1.1, 0, 0)
        );
        a.push(
          M,
          P,
          ...u,
          ...g,
          ...v,
          ...h,
          D
        );
        break;
      }
      case "left": {
        const P = U.Add(
          o[0],
          new U(0, n * 5 - C / 2, 0)
        ), M = U.Add(
          o[0],
          new U(-L, n * 5, 0)
        ), D = U.Add(
          o[0],
          new U(0, n * 5 + C / 2, 0)
        );
        a.push(
          M,
          P,
          ...h,
          ...u,
          ...g,
          ...v,
          D
        );
        break;
      }
      case "leftTop": {
        const P = U.Add(
          o[0],
          new U(0, n * 1.1, 0)
        ), M = U.Add(
          o[0],
          new U(-L, n * 1.1 + C / 2, 0)
        ), D = U.Add(
          o[0],
          new U(0, n * 1.1 + C, 0)
        );
        a.push(
          M,
          P,
          ...h,
          ...u,
          ...g,
          ...v,
          D
        );
        break;
      }
      case "leftBottom": {
        const P = U.Sub(
          o[3],
          new U(0, n * 1.1 + C, 0)
        ), M = U.Sub(
          o[3],
          new U(L, n * 1.1 + C / 2, 0)
        ), D = U.Sub(
          o[3],
          new U(0, n * 1.1, 0)
        );
        a.push(
          M,
          P,
          ...h,
          ...u,
          ...g,
          ...v,
          D
        );
        break;
      }
      case "right": {
        const P = U.Add(
          o[1],
          new U(0, n * 5 - C / 2, 0)
        ), M = U.Add(
          o[1],
          new U(L, n * 5, 0)
        ), D = U.Add(
          o[1],
          new U(0, n * 5 + C / 2, 0)
        );
        a.push(
          M,
          D,
          ...g,
          ...v,
          ...h,
          ...u,
          P
        );
        break;
      }
      case "rightTop": {
        const P = U.Add(
          o[1],
          new U(0, n * 1.1, 0)
        ), M = U.Add(
          o[1],
          new U(L, n * 1.1 + C / 2, 0)
        ), D = U.Add(
          o[1],
          new U(0, n * 1.1 + C, 0)
        );
        a.push(
          M,
          D,
          ...g,
          ...v,
          ...h,
          ...u,
          P
        );
        break;
      }
      case "rightBottom": {
        const P = U.Sub(
          o[2],
          new U(0, n * 1.1 + C, 0)
        ), M = U.Sub(
          o[2],
          new U(-L, n * 1.1 + C / 2, 0)
        ), D = U.Sub(
          o[2],
          new U(0, n * 1.1, 0)
        );
        a.push(
          M,
          D,
          ...g,
          ...v,
          ...h,
          ...u,
          P
        );
        break;
      }
    }
    const N = jr(this.tmpPoints, t), R2 = [
      Math.floor(N.x + N.w / 2),
      Math.floor(N.y + N.h / 2)
    ];
    return {
      rect: N,
      pos: R2,
      points: a
    };
  }
  updateTempPoints(t) {
    const e = t.slice(-2), s = new ve(e[0], e[1]), o = this.tmpPoints[0], { thickness: r } = this.workOptions;
    if (o.isNear(s, r) || ve.Sub(o, s).XY.includes(0))
      return false;
    if (this.tmpPoints.length === 2) {
      if (s.isNear(this.tmpPoints[1], 1))
        return false;
      this.tmpPoints[1] = s;
    } else
      this.tmpPoints.push(s);
    return true;
  }
  consumeService(t) {
    var c, l;
    const { op: e, isFullWork: s, workState: o = x.Done } = t, r = (c = this.workId) == null ? void 0 : c.toString();
    if (!r)
      return;
    this.tmpPoints.length = 0;
    for (let h = 0; h < e.length; h += 3)
      this.tmpPoints.push(new ve(e[h], e[h + 1], e[h + 2]));
    const n = s ? this.fullLayer : this.drawLayer || this.fullLayer, a = this.draw({
      workId: r,
      layer: n,
      isDrawing: false,
      isDrawEraserlines: o === x.Done
    });
    return this.oldRect = a, (l = this.vNodes) == null || l.setInfo(r, {
      rect: a,
      op: e,
      opt: this.workOptions,
      toolsType: this.toolsType,
      scaleType: this.scaleType,
      canRotate: this.canRotate,
      centerPos: a && b.getCenterPos(a, n)
    }), a;
  }
  clearTmpPoints() {
    this.tmpPoints.length = 0;
  }
  static updateNodeOpt(t) {
    var u;
    const { node: e, opt: s, vNodes: o } = t, { strokeColor: r, fillColor: n, toolsType: a, placement: c, strokeType: l } = s, h = o.get(e.name), p2 = h == null ? void 0 : h.opt;
    let d = e;
    return e.tagName === "GROUP" && (d = e.children[0]), r && (d.setAttribute("strokeColor", r), p2 != null && p2.strokeColor && (p2.strokeColor = r)), n && (n === "transparent" ? d.setAttribute("fillColor", "rgba(0,0,0,0)") : d.setAttribute("fillColor", n), p2 != null && p2.fillColor && (p2.fillColor = n)), a === T.SpeechBalloon && c && (p2.placement = c), l && (h != null && h.opt) && ((u = h.opt) != null && u.strokeType) && (h.opt.strokeType = l), h && o.setInfo(e.name, __spreadProps(__spreadValues({}, h), { opt: p2 })), b.updateNodeOpt(t);
  }
};
var Vs = Rt;
var Hs = yt;
var qs = "[object Boolean]";
function js(T2) {
  return T2 === true || T2 === false || Hs(T2) && Vs(T2) == qs;
}
var Zs = js;
var kt = Pn(Zs);
var ze = class extends b {
  constructor(t) {
    super(t);
    y(this, "canRotate", false);
    y(this, "scaleType", Fe.both);
    y(this, "toolsType", T.Straight);
    y(this, "tmpPoints", []);
    y(this, "workOptions");
    y(this, "oldRect");
    y(this, "straightTipWidth");
    y(this, "syncTimestamp");
    this.workOptions = t.toolsOpt, this.straightTipWidth = this.workOptions.thickness / 2, this.syncTimestamp = 0, this.syncUnitTime = 50;
  }
  consume(t) {
    const {
      data: e,
      isFullWork: s,
      isSubWorker: o,
      isMainThread: r,
      smoothSync: n,
      isSimpleWorker: a
    } = t, c = this.workId, { op: l, syncUnitTime: h } = e, p2 = l == null ? void 0 : l.length;
    if (!p2 || p2 < 2)
      return { type: R.None };
    h && (this.syncUnitTime = h);
    let d;
    if (this.tmpPoints.length === 0 ? (this.tmpPoints = [new ve(l[0], l[1])], d = false) : d = this.updateTempPoints(l), !d)
      return { type: R.None };
    let u;
    if (o || r || a) {
      const m = s ? this.fullLayer : this.drawLayer || this.fullLayer;
      u = this.draw({ workId: c, layer: m });
    }
    if (a) {
      const m = SI(u, this.oldRect);
      this.oldRect = u;
      const g = __spreadValues({
        rect: m,
        type: R.DrawWork,
        dataType: q.Local
      }, this.baseConsumeResult);
      if (n) {
        const I = Date.now();
        I - this.syncTimestamp > this.syncUnitTime && (this.syncTimestamp = I, g.op = this.tmpPoints.map((S) => [...S.XY, 0]).flat(1), g.index = 0, g.isSync = true);
      }
      return g;
    }
    if (!o && n) {
      const m = Date.now();
      return m - this.syncTimestamp > this.syncUnitTime ? (this.syncTimestamp = m, __spreadValues({
        type: R.DrawWork,
        dataType: q.Local,
        op: this.tmpPoints.map((g) => [...g.XY, 0]).flat(1),
        isSync: true,
        index: 0
      }, this.baseConsumeResult)) : { type: R.None };
    }
    const f = SI(u, this.oldRect);
    return this.oldRect = u, __spreadValues({
      rect: f,
      type: R.DrawWork,
      dataType: q.Local
    }, this.baseConsumeResult);
  }
  consumeAll() {
    var n;
    const t = this.workId;
    if (this.tmpPoints.length < 2)
      return {
        type: R.RemoveNode,
        removeIds: [t]
      };
    const e = this.fullLayer, s = this.draw({ workId: t, layer: e });
    this.oldRect = s;
    const o = this.tmpPoints.map((a) => [...a.XY, 0]).flat(1), r = _t(o);
    return (n = this.vNodes) == null || n.setInfo(t, {
      rect: s,
      op: o,
      opt: this.workOptions,
      toolsType: this.toolsType,
      scaleType: this.scaleType,
      canRotate: this.canRotate,
      centerPos: s && b.getCenterPos(s, e)
    }), __spreadProps(__spreadValues({
      rect: s,
      type: R.FullWork,
      dataType: q.Local,
      ops: r,
      isSync: true
    }, this.baseConsumeResult), {
      updateNodeOpt: {
        useAnimation: true
      }
    });
  }
  draw(t) {
    const { workId: e, layer: s, isDrawEraserlines: o = true } = t, {
      strokeColor: r,
      thickness: n,
      zIndex: a,
      scale: c,
      rotate: l,
      translate: h,
      strokeType: p2,
      eraserlines: d,
      lineCap: u,
      lineDash: f
    } = this.workOptions, m = s.worldPosition, g = s.worldScaling, { d: I, rect: S, isDot: v } = this.computDrawPoints(n, p2), L = [
      S.x + S.w / 2,
      S.y + S.h / 2
    ], C = new _spritejs$Group({
      pos: L,
      anchor: [0.5, 0.5],
      size: [S.w, S.h],
      name: e,
      id: e,
      normalize: true,
      zIndex: a
    }), N = {
      pos: [0, 0],
      normalize: true,
      d: I,
      fillColor: r,
      strokeColor: r,
      lineDash: v ? void 0 : p2 === me.Dotted ? [
        f && f[0] || 1,
        (f && f[1] || 2) * n
      ] : p2 === me.LongDotted ? [
        (f && f[0] || 1) * n,
        (f && f[1] || 2) * n
      ] : void 0,
      lineCap: v ? void 0 : u,
      lineWidth: p2 === me.Normal || v ? 0 : n
    };
    c && (N.scale = c), l && (N.rotate = l), h && (N.translate = h);
    const R2 = new _spritejs$Path(N);
    C.append(R2), d && (this.scaleType = Fe.proportional, this.drawEraserlines(
      {
        group: C,
        eraserlines: d,
        pos: L,
        layer: s
      },
      o
    )), this.replace(s, e, C);
    let P = {
      x: Math.floor(
        S.x * g[0] + m[0] - b.SafeBorderPadding
      ),
      y: Math.floor(
        S.y * g[1] + m[1] - b.SafeBorderPadding
      ),
      w: Math.floor(
        S.w * g[0] + 2 * b.SafeBorderPadding
      ),
      h: Math.floor(
        S.h * g[1] + 2 * b.SafeBorderPadding
      )
    };
    if (l || c || h) {
      const M = R2.getBoundingClientRect();
      P = {
        x: Math.floor(M.x - b.SafeBorderPadding),
        y: Math.floor(M.y - b.SafeBorderPadding),
        w: Math.floor(M.width + b.SafeBorderPadding * 2),
        h: Math.floor(M.height + b.SafeBorderPadding * 2)
      };
    }
    return this.isDelete && C.setAttribute("opacity", 0), P;
  }
  computDrawPoints(t, e) {
    return this.tmpPoints[1].distance(this.tmpPoints[0]) > this.straightTipWidth ? this.computFullPoints(t, e) : this.computDotPoints(t);
  }
  computFullPoints(t, e) {
    const s = U.Sub(this.tmpPoints[1], this.tmpPoints[0]).uni(), o = U.Per(s).mul(t / 2), r = ve.Sub(
      this.tmpPoints[0],
      o
    ), n = ve.Add(
      this.tmpPoints[0],
      o
    ), a = ve.Sub(
      this.tmpPoints[1],
      o
    ), c = ve.Add(
      this.tmpPoints[1],
      o
    ), l = ve.GetSemicircleStroke(
      this.tmpPoints[1],
      a,
      -1,
      8
    ), h = ve.GetSemicircleStroke(
      this.tmpPoints[0],
      n,
      -1,
      8
    ), p2 = [
      r,
      a,
      ...l,
      c,
      n,
      ...h
    ];
    let d;
    return e !== me.Normal ? d = mt(this.tmpPoints, false) : d = mt(p2, true), {
      d,
      rect: jr(p2),
      isDot: false,
      pos: this.tmpPoints[0].XY
    };
  }
  computDotPoints(t) {
    const e = ve.GetDotStroke(
      this.tmpPoints[0],
      t / 2,
      8
    );
    return {
      d: mt(e, true),
      rect: jr(e),
      isDot: true,
      pos: this.tmpPoints[0].XY
    };
  }
  updateTempPoints(t) {
    const e = t.slice(-2), s = new ve(e[0], e[1]), o = this.tmpPoints[0], { thickness: r } = this.workOptions;
    if (o.isNear(s, r))
      return false;
    if (this.tmpPoints.length === 2) {
      if (s.isNear(this.tmpPoints[1], 1))
        return false;
      this.tmpPoints[1] = s;
    } else
      this.tmpPoints.push(s);
    return true;
  }
  consumeService(t) {
    var c, l;
    const { op: e, isFullWork: s, workState: o = x.Done } = t, r = (c = this.workId) == null ? void 0 : c.toString();
    if (!r)
      return;
    this.tmpPoints.length = 0;
    for (let h = 0; h < e.length; h += 3)
      this.tmpPoints.push(new ve(e[h], e[h + 1], e[h + 2]));
    const n = s ? this.fullLayer : this.drawLayer || this.fullLayer, a = this.draw({
      workId: r,
      layer: n,
      isDrawEraserlines: o === x.Done
    });
    return this.oldRect = a, (l = this.vNodes) == null || l.setInfo(r, {
      rect: a,
      op: e,
      opt: this.workOptions,
      toolsType: this.toolsType,
      scaleType: this.scaleType,
      canRotate: this.canRotate,
      centerPos: a && b.getCenterPos(a, n)
    }), a;
  }
  clearTmpPoints() {
    this.tmpPoints.length = 0;
  }
  static updateNodeOpt(t) {
    var c, l;
    const { node: e, opt: s, vNodes: o } = t, { strokeColor: r, strokeType: n } = s, a = o.get(e.name);
    return r && (e.tagName === "GROUP" ? e.children.forEach((h) => {
      h.setAttribute("strokeColor", r), h.getAttribute("fillColor") && h.setAttribute("fillColor", r);
    }) : (e.setAttribute("strokeColor", r), e.setAttribute("fillColor", r)), (c = a == null ? void 0 : a.opt) != null && c.strokeColor && (a.opt.strokeColor = r)), n && (a != null && a.opt) && ((l = a.opt) != null && l.strokeType) && (a.opt.strokeType = n), a && o.setInfo(e.name, a), b.updateNodeOpt(t);
  }
};
var ft = class ft2 extends b {
  constructor(t) {
    super(t);
    y(this, "canRotate", false);
    y(this, "scaleType", Fe.proportional);
    y(this, "toolsType", T.Text);
    y(this, "tmpPoints", []);
    y(this, "workOptions");
    y(this, "oldRect");
    this.workOptions = t.toolsOpt;
  }
  consume() {
    return {
      type: R.None
    };
  }
  consumeAll() {
    return {
      type: R.None
    };
  }
  consumeService() {
  }
  draw(t) {
    return __async(this, null, function* () {
      const { workId: e, layer: s, isDrawLabel: o, boxRect: r } = t, { boxSize: n, boxPoint: a, zIndex: c } = this.workOptions, l = s.worldPosition, h = s.worldScaling;
      if (!a || !n)
        return;
      const p2 = {
        name: e,
        id: e,
        pos: [a[0] - Ai, a[1] - Ai],
        anchor: [0, 0],
        size: n,
        zIndex: c
      }, d = new _spritejs$Group(p2), u = {
        x: a[0] - Ai,
        y: a[1] - Ai,
        w: n[0],
        h: n[1]
      }, f = {
        x: Math.floor(u.x * h[0] + l[0]),
        y: Math.floor(u.y * h[1] + l[1]),
        w: Math.floor(u.w * h[0]) + 2,
        h: Math.floor(u.h * h[1]) + 2
      };
      this.replace(s, e, d);
      let m;
      if (r && (m = FI(f, r)), (o || r && m !== Gt.outside) && s && this.workOptions.text) {
        const g = yield ft2.createLabels(this.workOptions, s, f), { labels: I, maxWidth: S } = g;
        d.append(...I), f.w = Math.ceil(Math.max(S * s.worldScaling[0], f.w));
      }
      return f;
    });
  }
  consumeServiceAsync(t) {
    return __async(this, null, function* () {
      var l, h, p2, d;
      const e = (l = this.workId) == null ? void 0 : l.toString();
      if (!e)
        return;
      const { isFullWork: s, replaceId: o, isDrawLabel: r, boxRect: n } = t;
      this.oldRect = o && ((p2 = (h = this.vNodes) == null ? void 0 : h.get(o)) == null ? void 0 : p2.rect) || void 0;
      const a = s ? this.fullLayer : this.drawLayer || this.fullLayer, c = yield this.draw({
        workId: e,
        layer: a,
        isDrawLabel: typeof r > "u" && this.workOptions.workState === x.Done || r,
        boxRect: n
      });
      return (d = this.vNodes) == null || d.setInfo(e, {
        rect: c,
        op: [],
        opt: this.workOptions,
        toolsType: this.toolsType,
        scaleType: this.scaleType,
        canRotate: this.canRotate,
        centerPos: c && b.getCenterPos(c, a)
      }), c;
    });
  }
  updataOptService() {
  }
  updataOptServiceAsync(t, e, s) {
    return __async(this, null, function* () {
      var f, m;
      if (!this.workId)
        return;
      const o = this.workId.toString(), {
        fontColor: r,
        fontBgColor: n,
        bold: a,
        italic: c,
        lineThrough: l,
        underline: h,
        zIndex: p2
      } = t, d = (f = this.vNodes) == null ? void 0 : f.get(o);
      if (!d)
        return;
      r && (d.opt.fontColor = r), n && (d.opt.fontBgColor = n), a && (d.opt.bold = a), c && (d.opt.italic = c), kt(l) && (d.opt.lineThrough = l), kt(h) && (d.opt.underline = h), H(p2) && (d.opt.zIndex = p2), this.oldRect = d.rect;
      const u = yield this.draw({
        workId: o,
        layer: this.fullLayer,
        isDrawLabel: typeof e > "u" && this.workOptions.workState === x.Done || e,
        boxRect: s
      });
      return (m = this.vNodes) == null || m.setInfo(o, {
        rect: u,
        op: [],
        opt: this.workOptions,
        toolsType: this.toolsType,
        scaleType: this.scaleType,
        canRotate: this.canRotate,
        centerPos: u && b.getCenterPos(u, this.fullLayer)
      }), u;
    });
  }
  clearTmpPoints() {
    this.tmpPoints.length = 0;
  }
  static getSafetySnippetRatio(t) {
    const e = (t == null ? void 0 : t.parent).displayRatio || 1, s = Math.ceil(t.worldScaling[0] * 10) / 10;
    let o = s;
    return s <= 2 ? o = s * e * 2 : s > 2 && s <= 3 ? o = s * e * 1.6 : s > 3 && s <= 4 ? o = s * e * 1.2 : s > 4 && (o = s * e), Math.floor(o * 1e3) / 1e3;
  }
  static getSafetySnippetFontLength(t) {
    return Math.floor(ft2.textImageSnippetSize * 3 / 4 / t) || 1;
  }
  static createLabels(t, e, s) {
    return __async(this, null, function* () {
      var D;
      const o = [], { x: r, y: n } = s, { width: a, height: c } = (D = e.parent) == null ? void 0 : D.parent, l = $n(t.text), h = l.length, {
        fontSize: p2,
        lineHeight: d,
        bold: u,
        textAlign: f,
        italic: m,
        fontFamily: g,
        verticalAlign: I,
        fontColor: S,
        fontBgColor: v,
        underline: L,
        lineThrough: C
      } = t, N = ft2.getSafetySnippetRatio(e) || 1, R2 = Math.floor(p2 * N), P = ft2.getSafetySnippetFontLength(R2);
      let M = 0;
      for (let B = 0; B < h; B++) {
        const F = l[B], U2 = d || R2 * 1.5;
        if (F) {
          const V = VI(F), Z = [0, 0], K = [0, p2 * 1.2];
          I === "middle" && (Z[1] = Math.floor(
            B * p2 * 1.2 + 6 + p2 * (1.1 - 1)
          ));
          const nt2 = [0, Math.floor(-p2 * 0.15)];
          Z[0] = 6;
          const gt = Math.sin(Math.PI / 180 * 20);
          let pt = 0;
          const Ot = [];
          let Ct = 0;
          for (; Ct < V; ) {
            f === "left" && (nt2[0] = pt), Ct === 0 && m === "italic" && (nt2[0] = nt2[0] - gt / 2 * p2);
            const qt = F.slice(Ct, Ct + P), At2 = {
              anchor: [0, 0],
              pos: nt2,
              text: qt,
              fontFamily: g,
              fontSize: R2,
              lineHeight: U2,
              strokeColor: S,
              fontWeight: u,
              fillColor: S,
              textAlign: f,
              fontStyle: m,
              scale: [1 / N, 1 / N]
            }, dt = new _spritejs$Label(At2), at = yield dt.textImageReady;
            let Et2 = true;
            if (at) {
              const ae = at.rect && at.rect[2], ce = at.rect && at.rect[3];
              if (ae && ce) {
                const le = ae / N, $e = ce / N;
                pt = le + pt, m === "italic" && (u === "bold" ? pt = pt - gt * p2 * 1.2 : pt = pt - gt * p2), ((nt2[0] + Z[0] + le) * e.worldScaling[0] + r <= 0 || (nt2[0] + Z[0]) * e.worldScaling[0] + r >= a || (nt2[1] + Z[1] + $e) * e.worldScaling[1] + n <= 0 || (nt2[1] + Z[1]) * e.worldScaling[1] + n >= c) && (dt.disconnect(), Et2 = false), Et2 && Ot.push(dt);
              }
            }
            Ct += P;
          }
          K[0] = pt, m === "italic" && (K[0] = K[0] + gt * p2), M = Math.max(M, K[0]);
          let ne = true;
          if (((Z[0] + K[0]) * e.worldScaling[0] + r <= 0 || Z[0] * e.worldScaling[0] + r >= a || (Z[1] + K[1]) * e.worldScaling[0] + n <= 0 || Z[1] * e.worldScaling[1] + n >= c) && (ne = false), ne) {
            if (L) {
              const dt = Math.floor(p2 / 10), at = {
                normalize: false,
                pos: [0, p2 * 1.1 + dt / 2],
                lineWidth: dt,
                points: [0, 0, Math.ceil(K[0]), 0],
                strokeColor: S,
                className: "underline"
              }, Et2 = new _spritejs$Polyline(at);
              Ot.push(Et2);
            }
            if (C) {
              const dt = {
                normalize: false,
                pos: [0, p2 * 1.2 / 2],
                lineWidth: Math.floor(p2 / 10),
                points: [0, 0, Math.ceil(K[0]), 0],
                strokeColor: S,
                className: "lineThrough"
              }, at = new _spritejs$Polyline(dt);
              Ot.push(at);
            }
            const qt = {
              pos: Z,
              anchor: [0, 0],
              size: K,
              bgcolor: v
            }, At2 = new _spritejs$Group(qt);
            At2.append(...Ot), o.push(At2);
          }
        }
      }
      return { labels: o, maxWidth: M };
    });
  }
  static updateNodeOpt(t) {
    const { node: e, opt: s, vNodes: o, targetNode: r } = t, {
      fontBgColor: n,
      fontColor: a,
      translate: c,
      originPoint: l,
      scenePoint: h,
      scale: p2,
      bold: d,
      italic: u,
      lineThrough: f,
      underline: m,
      fontSize: g,
      textInfos: I,
      zIndex: S
    } = s, v = r && un(r) || o.get(e.name);
    if (!v || !e.parent) return;
    const C = v.opt;
    if (H(S) && (e.setAttribute("zIndex", S), v.opt.zIndex = S), a && C.fontColor && (C.fontColor = a, e.children.forEach((N) => {
      N.tagName === "GROUP" && N.children.forEach((R2) => {
        R2.tagName === "LABEL" ? (R2.setAttribute("fillColor", a), R2.setAttribute("strokeColor", a)) : R2.tagName === "POLYLINE" && R2.setAttribute("strokeColor", a);
      });
    })), n && C.fontBgColor && (C.fontBgColor = n, e.children.forEach((N) => {
      N.tagName === "GROUP" && N.setAttribute("bgcolor", n);
    })), d && (C.bold = d), u && (C.italic = u), kt(f) && (C.lineThrough = f), kt(m) && (C.underline = m), g && (C.fontSize = g), l && h && p2 && r && C.boxPoint) {
      const N = I == null ? void 0 : I.get(e.name);
      if (N) {
        const { fontSize: D, boxSize: B } = N;
        C.boxSize = B || C.boxSize, C.fontSize = D || C.fontSize;
      }
      const R2 = [C.boxPoint[0], C.boxPoint[1]];
      EI(C.boxPoint, h, p2, c);
      const P = [v.op[0], v.op[1]], M = [P[0] - R2[0], P[1] - R2[1]];
      if (v.centerPos = [
        v.centerPos[0] + M[0],
        v.centerPos[1] + M[1]
      ], v.opt.eraserlines)
        for (const D of v.opt.eraserlines) {
          const { op: B, thickness: F } = D;
          D.thickness = Math.round(F * Math.max(p2[0], p2[1]));
          for (let U2 = 0; U2 < B.length; U2++)
            LI(B[U2], h, p2, c);
        }
    } else if (c && C.boxPoint && (C.boxPoint = [
      Math.round(C.boxPoint[0] + c[0]),
      Math.round(C.boxPoint[1] + c[1])
    ], v.centerPos = [
      v.centerPos[0] + c[0],
      v.centerPos[1] + c[1]
    ], v.opt.eraserlines))
      for (const N of v.opt.eraserlines) {
        const { op: R2 } = N;
        for (let P = 0; P < R2.length; P++) {
          const M = R2[P].map((D, B) => B % 2 ? D + c[1] : D + c[0]);
          N.op[P] = M;
        }
      }
    return v && o.setInfo(e.name, v), v == null ? void 0 : v.rect;
  }
  static getRectFromLayer(t, e) {
    const s = t.getElementsByName(e)[0];
    if (s) {
      const o = s.getBoundingClientRect();
      let r = {
        x: Math.floor(o.x),
        y: Math.floor(o.y),
        w: Math.floor(o.width + 2),
        h: Math.floor(o.height + 2)
      };
      return s.children.forEach((n) => {
        if (n.tagName === "GROUP") {
          const a = s.getBoundingClientRect();
          r = SI(r, {
            x: Math.floor(a.x),
            y: Math.floor(a.y),
            w: Math.floor(a.width + 2),
            h: Math.floor(a.height + 2)
          });
        }
      }), r;
    }
  }
};
y(ft, "textImageSnippetSize", 1024 * 4), y(ft, "SafeBorderPadding", 30);
var Xt = ft;
var It = class It2 extends b {
  constructor(t) {
    super(t);
    y(this, "canRotate", false);
    y(this, "scaleType", Fe.none);
    y(this, "toolsType", T.PencilEraser);
    y(this, "tmpPoints", []);
    y(this, "workOptions");
    y(this, "worldPosition");
    y(this, "worldScaling");
    y(this, "eraserRect");
    y(this, "eraserPolyline");
    this.workOptions = t.toolsOpt, this.worldPosition = this.fullLayer.worldPosition, this.worldScaling = this.fullLayer.worldScaling;
  }
  /** 批量合并消费本地数据,返回绘制结果 */
  combineConsume(t) {
    const { workerEngine: e } = t;
    if (this.tmpPoints.length < 2)
      return {
        type: R.None
      };
    const { thickness: s } = this.workOptions, { width: o, height: r } = It2.eraserSizes[s], n = Math.round(Math.max(o, r) / this.worldScaling[0]), { rect: a, willNewNodes: c, willDeleteNodes: l } = this.getChangeNodes(
      n,
      e
    );
    return __spreadProps(__spreadValues({}, this.baseConsumeResult), {
      type: R.DrawWork,
      rect: a,
      willNewNodes: c,
      willDeleteNodes: l
    });
  }
  consumeService() {
  }
  updateTempPoints(t, e) {
    let s = this.tmpPoints.length - 1;
    const o = Date.now();
    for (let r = 0; r < t.length; r += 2) {
      const n = this.tmpPoints.length, a = new ve(
        t[r],
        t[r + 1]
      );
      if (n === 0) {
        this.tmpPoints.push(a);
        continue;
      }
      const c = n - 1, l = this.tmpPoints[c], h = U.Sub(a, l).uni();
      a.isNear(l, e / 2) || (U.Equals(h, l.v, 0.02) && (this.tmpPoints.pop(), s--), a.setv(h), a.setT(o), this.tmpPoints.push(a));
    }
    return Math.max(s, 0);
  }
  getChangeNodes(t, e) {
    const s = /* @__PURE__ */ new Map(), o = /* @__PURE__ */ new Set();
    let r;
    if (!this.vNodes)
      return { willDeleteNodes: o, willNewNodes: s, rect: r };
    const n = this.worldPosition;
    let a = jr(this.tmpPoints, t);
    a = jI(a, n), a = zI(a, this.worldScaling, n);
    const { nodeRange: c } = this.vNodes.getRectIntersectRange(a);
    if (!c.size)
      return { willDeleteNodes: o, willNewNodes: s, rect: r };
    const l = this.computEraserPointLines(this.tmpPoints, 90);
    for (const [h, p2] of c.entries()) {
      if (p2.toolsType !== T.Pencil)
        continue;
      let d = jI(p2.rect, [-n[0], -n[1]]);
      d = {
        x: d.x / this.worldScaling[0],
        y: d.y / this.worldScaling[1],
        w: d.w / this.worldScaling[0],
        h: d.h / this.worldScaling[1]
      };
      const u = [
        d.x,
        d.y,
        d.x + d.w,
        d.y + d.h
      ], f = [], m = [];
      for (const g of l)
        import_lineclip.default.polyline(g, u, f);
      if (f.length && p2.opt)
        for (const g of f) {
          const I = [];
          for (let S = 0; S < g.length; S++)
            S !== 0 && lt(g[S], g[S - 1]) || I.push(...g[S]);
          m.push(I);
        }
      if (m.length && e) {
        const g = e.createWorkShapeNode({
          workId: h,
          toolsType: T.Pencil,
          toolsOpt: p2.opt
        });
        g.setWorkId(h);
        const I = g == null ? void 0 : g.computPencilPoints({
          op: p2.op,
          eraserPolylines: m,
          eraserThickness: t
        });
        if (r = SI(r, p2.rect), o.add(h), e.removeNode(h), I.length)
          for (let S = 0; S < I.length; S++) {
            const v = `${h}_${Date.now()}_${S}`, L = v, N = e.setFullWork({
              workId: L,
              opt: p2.opt,
              toolsType: T.Pencil
            }).consumeService({
              op: I[S],
              isFullWork: true,
              replaceId: L
            });
            e.clearWorkShapeNodeCache(L), r = SI(r, N), s.set(v, __spreadProps(__spreadValues({}, p2), {
              toolsType: T.Pencil,
              rect: N ?? p2.rect,
              name: v,
              op: I[S]
            }));
          }
      }
    }
    return { willNewNodes: s, willDeleteNodes: o, rect: r };
  }
  consume(t) {
    const { data: e } = t, { op: s } = e;
    if (!s || s.length === 0)
      return __spreadValues({
        type: R.None
      }, this.baseConsumeResult);
    const { thickness: o } = this.workOptions, { width: r, height: n } = It2.eraserSizes[o], a = Math.max(r, n) / this.worldScaling[0];
    return this.updateTempPoints(s, a), __spreadValues({
      type: R.None
    }, this.baseConsumeResult);
  }
  computEraserPointLines(t, e = 60) {
    const s = [];
    let o = 0;
    const r = 360 - e;
    for (let n = 1; n < t.length; n++) {
      const a = t[n - 1], c = t[n];
      if (n === 1 && o === 0 && (s[o] = [a.XY]), s[o].push(c.XY), n < t.length - 1) {
        const l = c.getAngleByPoints(a, t[n + 1]);
        (l < e || l > r) && (o++, s[o] = [c.XY]);
      }
    }
    return t.length === 1 && s.length === 0 && s.push([t[0].XY, t[0].XY]), s;
  }
  consumeAll(t) {
    const { workerEngine: e } = t;
    if (this.replace(this.fullLayer, this.workId), !this.tmpPoints.length)
      return __spreadValues({
        type: R.None
      }, this.baseConsumeResult);
    const { thickness: s } = this.workOptions, { width: o, height: r } = It2.eraserSizes[s], n = Math.max(o, r) / this.worldScaling[0], { willDeleteNodes: a, willNewNodes: c, rect: l } = this.getChangeNodes(
      n,
      e
    );
    if (a.size === 0 && c.size === 0)
      return __spreadProps(__spreadValues({
        type: R.FullWork
      }, this.baseConsumeResult), {
        rect: l
      });
    const h = [];
    this.tmpPoints.map((d) => {
      h.push(d.x, d.y, n / 2);
    });
    const p2 = _t(h);
    return __spreadProps(__spreadValues({}, this.baseConsumeResult), {
      type: R.FullWork,
      dataType: q.Local,
      rect: l,
      ops: p2,
      willNewNodes: c,
      willDeleteNodes: a
    });
  }
  clearTmpPoints() {
    this.tmpPoints.length = 0;
  }
};
y(It, "eraserSizes", re);
var Yt = It;
var Pt = class Pt2 extends b {
  constructor(t) {
    super(t);
    y(this, "canRotate", false);
    y(this, "scaleType", Fe.none);
    y(this, "toolsType", T.BitMapEraser);
    y(this, "tmpPoints", []);
    y(this, "workOptions");
    y(this, "worldPosition");
    y(this, "worldScaling");
    y(this, "eraserRect");
    y(this, "eraserPolyline");
    y(this, "oldRect");
    this.workOptions = t.toolsOpt, this.worldPosition = this.fullLayer.worldPosition, this.worldScaling = this.fullLayer.worldScaling;
  }
  /** 批量合并消费本地数据,返回绘制结果 */
  combineConsume(t) {
    const { workerEngine: e } = t;
    if (this.tmpPoints.length < 2)
      return {
        type: R.None
      };
    const { thickness: s } = this.workOptions, { width: o, height: r } = Pt2.eraserSizes[s], n = Math.round(Math.max(o, r) / this.worldScaling[0]), { willDeleteNodes: a, rect: c } = this.getChangeNodes(n, e);
    return __spreadProps(__spreadValues({}, this.baseConsumeResult), {
      type: R.DrawWork,
      rect: c,
      willDeleteNodes: a
    });
  }
  consumeService() {
  }
  updateTempPoints(t, e) {
    let s = this.tmpPoints.length - 1;
    const o = Date.now();
    for (let r = 0; r < t.length; r += 2) {
      const n = this.tmpPoints.length, a = new ve(
        t[r],
        t[r + 1]
      );
      if (n === 0) {
        this.tmpPoints.push(a);
        continue;
      }
      const c = n - 1, l = this.tmpPoints[c], h = U.Sub(a, l).uni();
      a.isNear(l, e / 2) || (U.Equals(h, l.v, 0.02) && (this.tmpPoints.pop(), s--), a.setv(h), a.setT(o), this.tmpPoints.push(a));
    }
    return Math.max(s, 0);
  }
  getChangeNodes(t, e) {
    const s = /* @__PURE__ */ new Map(), o = /* @__PURE__ */ new Set();
    let r;
    if (!this.vNodes)
      return { willDeleteNodes: o, willUpdateNodes: s, rect: r };
    const n = this.worldPosition;
    let a = jr(this.tmpPoints, t);
    a = jI(a, n), a = zI(a, this.worldScaling, n);
    const { nodeRange: c } = this.vNodes.getRectIntersectRange(a);
    if (!c.size)
      return { willDeleteNodes: o, willUpdateNodes: s, rect: r };
    const l = this.computEraserPointLines(this.tmpPoints, 120);
    for (const [h, p2] of c.entries()) {
      if (p2.toolsType === T.Text || p2.toolsType === T.Image)
        continue;
      let d = jI(p2.rect, [-n[0], -n[1]]);
      d = {
        x: d.x / this.worldScaling[0],
        y: d.y / this.worldScaling[1],
        w: d.w / this.worldScaling[0],
        h: d.h / this.worldScaling[1]
      };
      const u = [
        d.x,
        d.y,
        d.x + d.w,
        d.y + d.h
      ], f = [], m = [];
      for (const S of l)
        import_lineclip.default.polyline(S, u, f);
      if (f.length && p2.opt)
        for (const S of f) {
          const v = [];
          for (let L = 0; L < S.length && !(isNaN(S[L][0]) || isNaN(S[L][1])); L++) {
            const C = S[L].map((N) => Math.round(N * 100) / 100);
            if (L !== 0) {
              const N = S[L - 1].map((R2) => Math.round(R2 * 100) / 100);
              if (lt(C, N))
                continue;
            }
            v.push(...C);
          }
          v.length !== 0 && m.push(v);
        }
      if (!m.length)
        continue;
      const g = p2.opt.eraserlines ? p2.opt.eraserlines : [], I = g.find((S) => S.workId === this.workId);
      if (I ? I.op = m : g.push({ workId: this.workId, op: m, thickness: t }), p2.opt.eraserlines = g, e) {
        const { toolsType: S } = p2, v = e.createWorkShapeNode({
          workId: h,
          toolsType: S,
          toolsOpt: p2.opt
        });
        v == null || v.setWorkId(h);
        const L = v == null ? void 0 : v.consumeService({
          op: p2.op,
          isFullWork: true,
          replaceId: h
        });
        if (r = SI(r, L), v != null && v.isDelete) {
          e.removeNode(h), o.add(h);
          continue;
        }
      }
      s.set(h, p2);
    }
    return { willUpdateNodes: s, willDeleteNodes: o, rect: r };
  }
  consume(t) {
    const { data: e } = t, { op: s } = e;
    if (!s || s.length === 0)
      return __spreadValues({
        type: R.None
      }, this.baseConsumeResult);
    const { thickness: o } = this.workOptions, { width: r, height: n } = Pt2.eraserSizes[o], a = Math.round(Math.max(r, n) / this.worldScaling[0]);
    return this.updateTempPoints(s, a), __spreadValues({
      type: R.None
    }, this.baseConsumeResult);
  }
  computEraserPointLines(t, e = 60) {
    const s = [];
    let o = 0;
    const r = 360 - e;
    for (let n = 1; n < t.length; n++) {
      const a = t[n - 1], c = t[n];
      if (n === 1 && o === 0 && (s[o] = [a.XY]), s[o].push(c.XY), n < t.length - 1) {
        const l = c.getAngleByPoints(a, t[n + 1]);
        (l < e || l > r) && (o++, s[o] = [c.XY]);
      }
    }
    return t.length === 1 && s.length === 0 && s.push([t[0].XY, t[0].XY]), s;
  }
  consumeAll(t) {
    const { workerEngine: e } = t;
    if (this.replace(this.fullLayer, this.workId), !this.tmpPoints.length)
      return __spreadValues({
        type: R.None
      }, this.baseConsumeResult);
    const { thickness: s } = this.workOptions, { width: o, height: r } = Pt2.eraserSizes[s], n = Math.round(Math.max(o, r) / this.worldScaling[0]), { willDeleteNodes: a, willUpdateNodes: c, rect: l } = this.getChangeNodes(
      n,
      e
    );
    if (a.size === 0 && c.size === 0)
      return __spreadProps(__spreadValues({
        type: R.FullWork
      }, this.baseConsumeResult), {
        rect: l
      });
    const h = [];
    this.tmpPoints.map((d) => {
      h.push(d.x, d.y, n / 2);
    });
    const p2 = _t(h);
    return __spreadProps(__spreadValues({}, this.baseConsumeResult), {
      type: R.FullWork,
      dataType: q.Local,
      rect: l,
      ops: p2,
      willUpdateNodes: c,
      willDeleteNodes: a
    });
  }
  clearTmpPoints() {
    this.tmpPoints.length = 0;
  }
};
y(Pt, "eraserSizes", re);
var $t = Pt;
var xt = class _xt extends b {
  constructor(t) {
    super(t);
    y(this, "canRotate", false);
    y(this, "scaleType", Fe.all);
    y(this, "toolsType", T.BackgroundSVG);
    y(this, "tmpPoints", []);
    y(this, "workOptions");
    y(this, "oldRect");
    this.workOptions = t.toolsOpt, this.scaleType = _xt.getScaleType(this.workOptions);
  }
  consume() {
    return { type: R.None };
  }
  consumeAll() {
    return { type: R.None };
  }
  draw(t) {
    const { layer: e, workId: s, replaceId: o } = t, { centerX: r, centerY: n, width: a, height: c, rotate: l, zIndex: h } = this.workOptions, p2 = (h || 0) - 100, d = new _spritejs$Group({
      anchor: [0.5, 0.5],
      pos: [r, n],
      name: s,
      size: [a, c],
      zIndex: p2,
      rotate: l
    }), u = new _spritejs$Rect({
      normalize: true,
      pos: [0, 0],
      width: a,
      height: c
    });
    d.appendChild(u), this.replace(e, o || s, d);
    const f = d.getBoundingClientRect();
    if (f)
      return {
        x: Math.floor(f.x - b.SafeBorderPadding),
        y: Math.floor(f.y - b.SafeBorderPadding),
        w: Math.floor(f.width + b.SafeBorderPadding * 2),
        h: Math.floor(f.height + b.SafeBorderPadding * 2)
      };
  }
  consumeService(t) {
    var r;
    const { replaceId: e } = t, s = this.workId, o = this.draw({ workId: s, layer: this.fullLayer, replaceId: e });
    return (r = this.vNodes) == null || r.setInfo(s, {
      rect: o,
      op: [],
      opt: this.workOptions,
      toolsType: this.toolsType,
      scaleType: this.scaleType,
      canRotate: this.canRotate,
      centerPos: o && b.getCenterPos(o, this.fullLayer)
    }), o;
  }
  clearTmpPoints() {
    this.tmpPoints.length = 0;
  }
  static getScaleType(t) {
    const { uniformScale: e, rotate: s } = t;
    return e !== false ? Fe.proportional : s && Math.abs(s) % 90 > 0 ? Fe.proportional : Fe.all;
  }
  static updateNodeOpt(t) {
    const { node: e, opt: s, vNodes: o, targetNode: r } = t, {
      translate: n,
      originPoint: a,
      scenePoint: c,
      scale: l,
      angle: h,
      isLocked: p2,
      zIndexForBackgroundSVG: d
    } = s, u = r && un(r) || o.get(e.name);
    if (!u) return;
    const f = e.parent;
    if (f) {
      if (H(d)) {
        const m = (d || 0) - 100;
        e.setAttribute("zIndex", m), u.opt.zIndex = d;
      }
      if (kt(p2) && (u.opt.locked = p2), a && c && l && n) {
        const { centerX: m, centerY: g, width: I, height: S, uniformScale: v } = u.opt, L = v !== false ? [l[0], l[0]] : l, C = [m, g], N = [m, g];
        EI(N, c, L, n);
        const R2 = [
          N[0] - C[0],
          N[1] - C[1]
        ];
        u.centerPos = [
          u.centerPos[0] + R2[0],
          u.centerPos[1] + R2[1]
        ], u.opt.width = Math.round(I * L[0]), u.opt.height = Math.round(S * L[1]), u.opt.centerX = N[0], u.opt.centerY = N[1];
      } else if (n)
        u.opt.centerX = u.opt.centerX + n[0], u.opt.centerY = u.opt.centerY + n[1], u.centerPos = [
          u.centerPos[0] + n[0],
          u.centerPos[1] + n[1]
        ];
      else if (H(h))
        if (u.opt.rotate = h, u.scaleType = _xt.getScaleType(
          u.opt
        ), r) {
          const m = DI(u.rect, h);
          u.rect = m;
        } else {
          const m = b.getRectFromLayer(f, e.name);
          u.rect = m || u.rect;
        }
      return u && o.setInfo(e.name, u), u == null ? void 0 : u.rect;
    }
  }
};
function Ue(T2) {
  switch (T2) {
    case T.Arrow:
      return Me;
    case T.Pencil:
      return Ne;
    case T.Straight:
      return ze;
    case T.Ellipse:
      return Oe;
    case T.Polygon:
    case T.Triangle:
      return Be;
    case T.Star:
    case T.Rhombus:
      return Ee;
    case T.Rectangle:
      return Ae;
    case T.SpeechBalloon:
      return Fe2;
    case T.Text:
      return Xt;
    case T.LaserPen:
      return xe;
    case T.Eraser:
      return Ut;
    case T.PencilEraser:
      return Yt;
    case T.BitMapEraser:
      return $t;
    case T.Selector:
      return Gt2;
    case T.Image:
      return Dt;
    case T.BackgroundSVG:
      return xt;
  }
}
function Lt(T2, i) {
  const _a = T2, { toolsType: t } = _a, e = __objRest(_a, ["toolsType"]);
  switch (t) {
    case T.Arrow:
      return new Me(e);
    case T.Pencil:
      return new Ne(e);
    case T.Straight:
      return new ze(e);
    case T.Ellipse:
      return new Oe(e);
    case T.Polygon:
    case T.Triangle:
      return new Be(e);
    case T.Star:
    case T.Rhombus:
      return new Ee(e);
    case T.Rectangle:
      return new Ae(e);
    case T.SpeechBalloon:
      return new Fe2(e);
    case T.Text:
      return new Xt(e);
    case T.LaserPen:
      return new xe(e);
    case T.Eraser:
      return new Ut(e, i);
    case T.BitMapEraser:
      return new $t(e);
    case T.PencilEraser:
      return new Yt(e);
    case T.Selector:
      return e.vNodes ? new Gt2(__spreadProps(__spreadValues({}, e), {
        vNodes: e.vNodes,
        drawLayer: e.fullLayer
      })) : void 0;
    case T.Image:
      return new Dt(e);
    case T.BackgroundSVG:
      return new xt(__spreadProps(__spreadValues({}, e), {
        toolsOpt: e.toolsOpt
      }));
    default:
      return;
  }
}
function Ge(T2) {
  const i = [], t = [
    "PATH",
    "SPRITE",
    "POLYLINE",
    "RECT",
    "ELLIPSE",
    "LABEL",
    "SPRITESVG"
  ];
  for (const e of T2) {
    if (e.tagName === "GROUP" && e.children.length) {
      const s = Ge(e.children);
      i.push(...s);
    }
    e.tagName && t.includes(e.tagName) && i.push(e);
  }
  return i;
}
var Xe = class {
  constructor(i, t) {
    y(this, "viewId");
    y(this, "scene");
    y(this, "fullLayer");
    y(this, "curNodeMap");
    y(this, "targetNodeMap", []);
    y(this, "highLevelIds");
    y(this, "canClearUids");
    y(this, "localUid");
    this.viewId = i, this.scene = t, this.curNodeMap = new p();
  }
  init(i) {
    this.fullLayer = i;
  }
  get(i) {
    return this.curNodeMap.get(i);
  }
  setLocalUid(i) {
    this.localUid = i;
  }
  getLocalUid() {
    return this.localUid;
  }
  setCanClearUids(i) {
    this.canClearUids = i;
  }
  getCanClearUids() {
    return this.canClearUids;
  }
  getCanEraserNodes(i, t) {
    const e = /* @__PURE__ */ new Map();
    for (const [s, o] of i.entries())
      o.toolsType !== T.BackgroundSVG && (o.toolsType === T.Image && o.opt.type === Zt.Image && (o.opt.locked || t && t.disableEraseImage) || o.toolsType === T.Text && (o.opt.workState === x.Doing || o.opt.workState === x.Start || t && t.disableEraseText) || this.isCanClearWorkId(s) && e.set(s, o));
    return e;
  }
  getNodesByType(i) {
    const t = /* @__PURE__ */ new Map();
    return this.curNodeMap.forEach((e, s) => {
      e.toolsType === i && t.set(s, e);
    }), t;
  }
  gethasEraserNodes() {
    const i = /* @__PURE__ */ new Map();
    return this.curNodeMap.forEach((t, e) => {
      var s;
      (s = t.opt.eraserlines) != null && s.length && i.set(e, t);
    }), i;
  }
  hasRenderNodes() {
    return true;
  }
  has(i) {
    return this.curNodeMap.has(i);
  }
  setInfo(i, t) {
    const e = this.curNodeMap.get(i) || {
      name: i,
      rect: t.rect
    };
    t.rect && (e.rect = un(t.rect)), t.op && OI(t.op) && (e.op = un(t.op)), t.canRotate && (e.canRotate = t.canRotate), t.scaleType && (e.scaleType = t.scaleType), t.opt && (e.opt = un(t.opt)), t.toolsType && (e.toolsType = t.toolsType), t.centerPos && (e.centerPos = un(t.centerPos)), kt(t.isSelected) && (e.isSelected = t.isSelected), e.rect ? this.curNodeMap.set(i, e) : this.curNodeMap.delete(i);
  }
  selected(i) {
    this.setInfo(i, { isSelected: true });
  }
  unSelected(i) {
    this.setInfo(i, { isSelected: false });
  }
  delete(i) {
    this.curNodeMap.delete(i);
  }
  clear() {
    this.curNodeMap.clear(), this.targetNodeMap.length = 0;
  }
  getRectIntersectRange(i, t = true, e = true) {
    let s;
    const o = /* @__PURE__ */ new Map();
    for (const [r, n] of this.curNodeMap.entries())
      if (kI(i, n.rect)) {
        if (t && n.toolsType === T.Image && n.opt.locked || e && n.toolsType === T.Text && (n.opt.workState === x.Doing || n.opt.workState === x.Start))
          continue;
        s = SI(s, n.rect), o.set(r, n);
      }
    return {
      rectRange: s,
      nodeRange: o
    };
  }
  getNodeRectFormShape(i, t) {
    const e = Ue(t.toolsType);
    return this.fullLayer && (e == null ? void 0 : e.getRectFromLayer(this.fullLayer, i));
  }
  updateNodeRect(i) {
    const t = this.curNodeMap.get(i);
    if (t) {
      const e = this.getNodeRectFormShape(i, t);
      if (!e) {
        this.curNodeMap.delete(i);
        return;
      }
      t.rect = e, this.curNodeMap.set(i, t);
    }
  }
  updateHighLevelNodesRect(i) {
    this.highLevelIds = i;
    for (const t of this.highLevelIds.keys())
      this.updateNodeRect(t);
  }
  updateLowLevelNodesRect() {
    var i;
    for (const t of this.curNodeMap.keys())
      (i = this.highLevelIds) != null && i.has(t) || this.updateNodeRect(t);
  }
  clearHighLevelIds() {
    this.highLevelIds = void 0;
  }
  setTargetAssignKeys(i) {
    const t = new p();
    for (const e of i) {
      const s = this.curNodeMap.get(e);
      s && t.set(e, un(s));
    }
    return this.targetNodeMap.push(un(t)), this.targetNodeMap.length - 1;
  }
  setTarget() {
    const i = this.curNodeMap.keys();
    return this.setTargetAssignKeys(Array.from(i));
  }
  getLastTarget() {
    return this.targetNodeMap[this.targetNodeMap.length - 1];
  }
  deleteLastTarget() {
    this.targetNodeMap.length && (this.targetNodeMap.length = this.targetNodeMap.length - 1);
  }
  getTarget(i) {
    return this.targetNodeMap[i];
  }
  deleteTarget(i) {
    this.targetNodeMap.length = i;
  }
  clearTarget() {
    this.targetNodeMap.length = 0;
  }
  isLocalWorkId(i) {
    return i.split(st).length === 1;
  }
  isCanClearWorkId(i) {
    if (this.canClearUids === void 0 || this.canClearUids === true)
      return true;
    if (vI(this.canClearUids)) {
      const t = i.split(st);
      if (t.length === 1)
        return this.canClearUids.has("localSelf") || this.localUid && this.canClearUids.has(this.localUid);
      if (t.length === 2)
        return this.canClearUids.has(t[0]);
    }
    return false;
  }
};
var Ks = class {
  constructor(i, t, e) {
    y(this, "viewId");
    y(this, "type");
    y(this, "scene");
    y(this, "fullLayer");
    y(this, "vNodes");
    y(this, "dpr");
    y(this, "contextType");
    y(this, "opt");
    y(this, "cameraOpt");
    y(this, "isSafari", false);
    y(this, "combinePostMsg", /* @__PURE__ */ new Set());
    y(this, "workerTaskId");
    y(this, "protectedTask");
    y(this, "delayPostDoneResolve");
    y(this, "cacheImages", /* @__PURE__ */ new Map());
    y(this, "imageResolveMap", /* @__PURE__ */ new Map());
    y(this, "taskUpdateCameraId");
    y(this, "debounceUpdateCameraId");
    var s, o;
    if (this.viewId = i, this.type = e, this.opt = t, this.dpr = t.dpr, this.contextType = this.getSupportContextType(
      e,
      (s = t == null ? void 0 : t.offscreenCanvasOpt) == null ? void 0 : s.contextType
    ), !this.contextType)
      throw new Error(
        "Sorry, your browser doesn't support canvas context type 2d or webgl"
      );
    try {
      this.scene = this.createScene(t), this.createRenderLayer(this.scene, t);
    } catch (r) {
      if (this.contextType !== "2d")
        this.contextType = "2d", (o = this.scene) == null || o.disconnect(), this.scene = this.createScene(t), this.createRenderLayer(this.scene, t);
      else
        throw r;
    }
    this.vNodes = new Xe(i, this.scene);
  }
  createRenderLayer(i, t) {
    const e = Se.bufferSize.full, s = Se.bufferSize.sub;
    this.fullLayer = this.createLayer("fullLayer", i, __spreadProps(__spreadValues({}, t.layerOpt), {
      bufferSize: this.viewId === pe ? e : s * 2
    }));
  }
  updateDpr(i) {
    return __async(this, null, function* () {
      this.dpr = i, this.scene.displayRatio = i;
    });
  }
  getCachedImages(i) {
    var t;
    return (t = this.cacheImages.get(i)) == null ? void 0 : t.imageBitmap;
  }
  getCachedImagesByWorkId(i) {
    for (const [t, e] of this.cacheImages.entries())
      if (t === i && e.imageBitmap)
        return e.imageBitmap;
  }
  deleteCachedImagesByWorkId(i, t) {
    for (const [e, s] of this.cacheImages.entries())
      t && e === t || s.workId === i && (s.imageBitmap.close(), this.cacheImages.delete(e));
  }
  clearCacheImages() {
    this.cacheImages.forEach((i) => i.imageBitmap.close()), this.cacheImages.clear();
  }
  clearImageResolveMap() {
    this.imageResolveMap.forEach(({ timer: i }) => {
      i && clearTimeout(i);
    }), this.imageResolveMap.clear();
  }
  setIsSafari(i) {
    this.isSafari = i;
  }
  on(i) {
    return __async(this, null, function* () {
      const { msgType: t, toolsType: e, opt: s, imageSrc: o, imageBitmap: r, workId: n, dpr: a } = i;
      switch (t) {
        case R.UpdateDpr: {
          H(a) && (yield this.updateDpr(a));
          break;
        }
        case R.UpdateCamera: {
          yield this.updateCamera(i);
          break;
        }
        case R.Destroy: {
          this.destroy();
          break;
        }
        case R.Clear: {
          yield this.clearAll();
          break;
        }
        case R.UpdateTools: {
          if (e && s) {
            const c = {
              toolsType: e,
              toolsOpt: s,
              combineUnitTime: i.combineUnitTime,
              maxCombineEraserTime: i.maxCombineEraserTime
            };
            this.localWork.setToolsOpt(c);
          }
          break;
        }
        case R.GetImageBitMap: {
          if (o && r && n) {
            const c = n.toString();
            this.cacheImages.set(o, {
              imageBitmap: r,
              workId: c
            });
            const l = this.imageResolveMap.get(o);
            if (l) {
              const { resolve: h, timer: p2 } = l;
              p2 && clearTimeout(p2), h && h(o);
            }
            this.deleteCachedImagesByWorkId(c, o);
          }
          break;
        }
      }
    });
  }
  getIconSize(i, t, e) {
    const s = i * e, o = t * e;
    return s <= 50 || o <= 50 ? [50, 50] : s <= 100 || o <= 100 ? [100, 100] : s <= 200 || o <= 200 ? [200, 200] : s <= 400 || o <= 400 ? [400, 400] : s <= 800 || o <= 800 ? [800, 800] : [1600, 1600];
  }
  loadImageBitMap(i) {
    return __async(this, null, function* () {
      const { toolsType: t, opt: e, workId: s, isSubWorker: o } = i;
      if (t === T.Image && e && s) {
        const r = s.toString(), { src: n, type: a, width: c, height: l, strokeColor: h } = e;
        if (!n || !a || !c || !l)
          return;
        let p2 = n;
        if (a === Zt.Iconify) {
          const [m, g] = this.getIconSize(c, l, this.dpr);
          p2 = `${n}?width=${m}&height=${g}&color=${h}`;
        }
        if (this.cacheImages.has(p2)) {
          const m = this.getCachedImages(p2);
          if (m)
            return m;
        }
        if (this.imageResolveMap.has(p2)) {
          const m = this.getCachedImagesByWorkId(r);
          if (m)
            return m;
        }
        const f = yield new Promise((m) => {
          const g = this.imageResolveMap.get(p2) || {
            resolve: void 0,
            timer: void 0
          };
          g.timer && clearTimeout(g.timer), g.resolve = m, g.timer = setTimeout(() => {
            const I = this.imageResolveMap.get(p2);
            I != null && I.resolve && I.resolve(p2);
          }, 5e3), this.imageResolveMap.set(p2, g), this._post({
            sp: [
              {
                imageSrc: p2,
                workId: r,
                viewId: this.viewId,
                isgl: !!this.fullLayer.parent.gl,
                isSubWorker: o,
                type: R.GetImageBitMap
              }
            ]
          });
        });
        return this.imageResolveMap.delete(f), this.getCachedImages(p2);
      }
    });
  }
  createLocalWork(i) {
    const { workId: t, opt: e, toolsType: s } = i;
    t && e && (!this.localWork.getToolsOpt() && s && this.setToolsOpt({
      toolsType: s,
      toolsOpt: e
    }), this.setWorkOpt({
      workId: t,
      toolsOpt: e
    }));
  }
  updateScene(i, t, e) {
    i.attr(__spreadValues({}, t));
    const { width: s, height: o } = t;
    i.container.width = s, i.container.height = o, i.width = s, i.height = o, this.updateLayer({ width: s, height: o }, e);
  }
  updateLayer(i, t) {
    const { width: e, height: s } = i, o = t || this.fullLayer;
    o && (o.parent.setAttribute("width", e), o.parent.setAttribute("height", s), o.setAttribute("size", [e, s]), o.setAttribute("pos", [e * 0.5, s * 0.5]));
  }
  getSupportContextType(i, t) {
    const e = new OffscreenCanvas(100, 100);
    let s = ["2d"];
    i === Qt.Full && this.viewId === pe && (s = ["webgl2", "webgl", "2d"], t && s.unshift(t));
    for (const o of s)
      try {
        if (e.getContext(o))
          return o;
      } catch (r) {
        throw r;
      }
  }
  createScene(i) {
    const { offscreenCanvasOpt: t } = i, { width: e, height: s } = t, o = new OffscreenCanvas(e, s);
    t.contextType && delete t.contextType;
    const r = new _spritejs$Scene(__spreadProps(__spreadValues({
      container: o,
      displayRatio: this.dpr,
      depth: false,
      desynchronized: true,
      failIfMajorPerformanceCaveat: true
    }, t), {
      contextType: this.contextType,
      id: this.viewId
    }));
    return r.setAttribute("id", this.viewId), r;
  }
  createLayer(i, t, e, s) {
    const o = `offscreen-${i}`, { width: r, height: n } = e;
    s && (e.offscreen = true, e.canvas = s);
    const a = t.layer(o, e), c = new _spritejs$Group({
      anchor: [0.5, 0.5],
      pos: [r * 0.5, n * 0.5],
      size: [r, n],
      name: "viewport",
      id: i
    });
    return a.append(c), c;
  }
  clearAll() {
    return __async(this, null, function* () {
      var i;
      this.fullLayer && (this.fullLayer.parent.children.forEach((t) => {
        t.name !== "viewport" && t.remove();
      }), Q(this.fullLayer, this.fullLayer.parent)), this.taskUpdateCameraId && (clearTimeout(this.taskUpdateCameraId), this.taskUpdateCameraId = void 0), this.debounceUpdateCameraId && (clearTimeout(this.debounceUpdateCameraId), this.debounceUpdateCameraId = void 0), this.clearCacheImages(), this.clearImageResolveMap(), this.localWork.destroy(), (i = this.serviceWork) == null || i.destroy();
    });
  }
  setToolsOpt(i) {
    this.localWork.setToolsOpt(i);
  }
  setWorkOpt(i) {
    const { workId: t, toolsOpt: e } = i;
    t && e && this.localWork.setWorkOptions(t.toString(), e);
  }
  destroy() {
    var i;
    this.vNodes.clear(), this.fullLayer.remove(), q2(this.fullLayer, this.fullLayer.parent), this.clearCacheImages(), this.clearImageResolveMap(), this.scene.remove(), this.localWork.destroy(), (i = this.serviceWork) == null || i.destroy();
  }
  post(i) {
    return __async(this, null, function* () {
      this.combinePostMsg.add(i), yield this.runBatchPostData();
    });
  }
  runBatchPostData() {
    return __async(this, null, function* () {
      this.workerTaskId || (this.workerTaskId = setTimeout(() => {
        this.workerTaskId = void 0, this.combinePost();
      }, 16)), this.type === Qt.Full && !this.delayPostDoneResolve && (yield new Promise((t) => {
        this.delayPostDoneResolve = t;
      })) && (this.delayPostDoneResolve = void 0);
    });
  }
  combinePostData() {
    var n, a;
    this.workerTaskId = void 0;
    const i = [], t = [];
    let e, s, o;
    const r = /* @__PURE__ */ new Set();
    for (const c of this.combinePostMsg.values()) {
      if ((n = c.render) != null && n.length)
        for (const l of c.render) {
          let h = false;
          if (l.workId && r.add(l.workId), l.isClearAll && (l.rect = this.getSceneRect(), l.isClear = true, delete l.isClearAll), l.drawCanvas) {
            const p2 = this.getLayer(l.drawCanvas);
            if (!p2 || !(p2.parent instanceof _spritejs$Layer))
              continue;
            if (p2.parent.render(), l.isDrawAll) {
              const d = this.getSceneRect();
              l.rect = d, delete l.isDrawAll;
            }
          }
          for (const p2 of i)
            if (l.viewId === p2.viewId) {
              l.isClear && p2.clearCanvas && p2.isClear && p2.clearCanvas === l.clearCanvas && (p2.rect = SI(
                p2.rect,
                l.rect
              ), h = true), p2.drawCanvas && p2.drawCanvas === l.drawCanvas && (p2.rect = SI(
                p2.rect,
                l.rect
              ), h = true);
              continue;
            }
          h || (l.isClear && !l.drawCanvas ? i.unshift(l) : i.push(l));
        }
      if ((a = c.sp) != null && a.length)
        for (const l of c.sp) {
          let h = false;
          for (const p2 of t)
            if (lt(l, p2)) {
              h = true;
              break;
            }
          h || t.push(l);
        }
      H(c.fullWorkerDrawCount) && (e = c.fullWorkerDrawCount), H(c.subWorkerDrawCount) && (s = c.subWorkerDrawCount), H(c.consumeCount) && (o = c.consumeCount);
    }
    return this.combinePostMsg.clear(), {
      render: i,
      sp: t,
      fullWorkerDrawCount: e,
      subWorkerDrawCount: s,
      consumeCount: o,
      workIds: r,
      viewId: this.viewId
    };
  }
  getSceneRect() {
    const { width: i, height: t } = this.scene;
    return {
      x: 0,
      y: 0,
      w: Math.floor(i),
      h: Math.floor(t)
    };
  }
};
var Ye = class {
  constructor(i) {
    y(this, "viewId");
    y(this, "vNodes");
    y(this, "thread");
    y(this, "fullLayer");
    y(this, "drawLayer");
    y(this, "_post");
    y(this, "tmpOpt");
    y(this, "workShapes", /* @__PURE__ */ new Map());
    y(this, "drawCount", 0);
    y(this, "consumeCount", 0);
    y(this, "syncUnitTime", Se.syncOpt.interval);
    y(this, "combineUnitTime", Se.bezier.combineUnitTime);
    y(this, "maxCombineEraserTime", Se.pencilEraser.maxCombineTime);
    this.thread = i.thread, this.viewId = i.viewId, this.vNodes = i.vNodes, this.fullLayer = i.fullLayer, this.drawLayer = i.drawLayer, this._post = this.thread.post.bind(i.thread);
  }
  setmaxCombineEraserTime(i) {
    this.maxCombineEraserTime = i;
  }
  setCombineUnitTime(i) {
    this.combineUnitTime = i;
  }
  setSyncUnitTime(i) {
    this.syncUnitTime = i;
  }
  destroy() {
    this.workShapes.clear();
  }
  getWorkShapes() {
    return this.workShapes;
  }
  getWorkShape(i) {
    return this.workShapes.get(i);
  }
  createWorkShape(i, t) {
    if (i && this.tmpOpt) {
      const e = {
        toolsType: this.tmpOpt.toolsType,
        toolsOpt: t || this.tmpOpt.toolsOpt
      }, s = this.createWorkShapeNode(__spreadProps(__spreadValues({}, e), { workId: i }));
      s && this.workShapes.set(i, s);
    }
  }
  setWorkOptions(i, t) {
    const e = this.getWorkShape(i);
    e || this.createWorkShape(i, t), e == null || e.setWorkOptions(t);
  }
  createWorkShapeNode(i) {
    var t;
    return Lt(
      __spreadProps(__spreadValues({}, i), {
        vNodes: this.vNodes,
        fullLayer: this.fullLayer,
        drawLayer: this.drawLayer
      }),
      (t = this.thread) == null ? void 0 : t.serviceWork
    );
  }
  setToolsOpt(i) {
    var t, e, s;
    ((t = this.tmpOpt) == null ? void 0 : t.toolsType) !== i.toolsType && (e = this.tmpOpt) != null && e.toolsType && this.clearAllWorkShapesCache(), this.tmpOpt = i, (s = i.toolsOpt) != null && s.syncUnitTime && (this.syncUnitTime = i.toolsOpt.syncUnitTime), i.combineUnitTime && (this.combineUnitTime = i.combineUnitTime), i.maxCombineEraserTime && (this.maxCombineEraserTime = i.maxCombineEraserTime);
  }
  getToolsOpt() {
    return this.tmpOpt;
  }
  clearWorkShapeNodeCache(i) {
    var t;
    (t = this.getWorkShape(i)) == null || t.clearTmpPoints(), this.workShapes.delete(i);
  }
  clearAllWorkShapesCache() {
    this.workShapes.forEach((i) => i.clearTmpPoints()), this.workShapes.clear();
  }
  setFullWork(i) {
    const { workId: t, opt: e, toolsType: s } = i;
    if (t && e && s) {
      const o = t.toString();
      let r;
      return t && this.workShapes.has(o) ? (r = this.workShapes.get(o), r == null || r.setWorkOptions(e)) : r = this.createWorkShapeNode({
        toolsOpt: e,
        toolsType: s,
        workId: o
      }), r ? (this.workShapes.set(o, r), r) : void 0;
    }
  }
};
var Qs = class extends Ye {
  constructor(t) {
    super(t);
    y(this, "drawWorkActiveId");
  }
  runSelectWork(t) {
    var s;
    const e = this.setFullWork(t);
    e && ((s = t.selectIds) != null && s.length) && t.workId && e.selectServiceNode(
      t.workId.toString(),
      { selectIds: t.selectIds },
      false
    );
  }
  workShapesDone() {
    for (const t of this.workShapes.keys())
      this.clearWorkShapeNodeCache(t);
    Q(this.fullLayer, this.fullLayer.parent);
  }
  consumeDraw(t) {
    return __async(this, null, function* () {
      const { workId: e, fullWorkerDrawCount: s, postCount: o } = t, r = e == null ? void 0 : e.toString(), n = r && this.workShapes.get(r);
      if (!n)
        return;
      this.drawWorkActiveId && this.drawWorkActiveId !== r && (yield this.consumeDrawAll({
        workId: this.drawWorkActiveId,
        viewId: this.viewId,
        msgType: R.DrawWork,
        dataType: q.Local
      }), this.drawWorkActiveId = void 0), !this.drawWorkActiveId && r !== $ && (this.drawWorkActiveId = r);
      const a = n.toolsType;
      H(o) && (this.consumeCount = o);
      const c = n.consume({
        data: t,
        drawCount: this.drawCount,
        isFullWork: true,
        isSubWorker: true,
        removeDrawCount: s
      });
      switch (a) {
        case T.Selector:
          c && (this.drawCount++, yield this.drawSelector(c));
          break;
        case T.Ellipse:
        case T.Arrow:
        case T.Straight:
        case T.Rectangle:
        case T.Star:
        case T.Polygon:
        case T.SpeechBalloon: {
          c && (this.drawCount++, yield this.drawShape(c));
          break;
        }
        case T.Pencil: {
          c && (this.drawCount++, yield this.drawPencil(c, e == null ? void 0 : e.toString()));
          break;
        }
        case T.BitMapEraser:
        case T.PencilEraser: {
          c && (this.drawCount++, yield this._post({
            subWorkerDrawCount: this.drawCount,
            consumeCount: this.consumeCount
          }));
          break;
        }
      }
    });
  }
  consumeDrawAll(t) {
    return __async(this, null, function* () {
      const { workId: e } = t;
      if (e) {
        const s = e.toString();
        this.drawWorkActiveId === s && (this.drawWorkActiveId = void 0);
        const o = this.workShapes.get(s);
        if (!o)
          return;
        switch (o.toolsType) {
          case T.Selector:
            this.drawCount = 0, Q(this.fullLayer, this.fullLayer.parent), this.clearWorkShapeNodeCache(s), this._post({
              render: [
                {
                  isClearAll: true,
                  clearCanvas: nt.Float,
                  viewId: this.viewId
                }
              ]
            });
            break;
          case T.Arrow:
          case T.Straight:
          case T.Ellipse:
          case T.Pencil:
          case T.Rectangle:
          case T.Star:
          case T.Polygon:
          case T.SpeechBalloon:
          case T.BitMapEraser:
          case T.PencilEraser:
            this.drawCount = 0, Q(this.fullLayer, this.fullLayer.parent), this.clearWorkShapeNodeCache(s);
            break;
        }
      }
    });
  }
  removeWork(t) {
    return __async(this, null, function* () {
      const { workId: e } = t, s = e == null ? void 0 : e.toString();
      if (s) {
        const o = this.removeNode(s);
        if (o) {
          const r = [];
          r.push(
            {
              rect: xI(o),
              clearCanvas: nt.Float,
              isClear: true,
              viewId: this.viewId
            },
            {
              rect: xI(o),
              drawCanvas: nt.Float,
              viewId: this.viewId
            }
          ), yield this._post({
            render: r
          });
        }
      }
    });
  }
  removeNode(t) {
    const e = this.workShapes.has(t);
    let s;
    return e && (this.fullLayer.getElementsByName(t).forEach((o) => {
      const r = o.getBoundingClientRect();
      s = SI(s, {
        x: r.x - b.SafeBorderPadding,
        y: r.y - b.SafeBorderPadding,
        w: r.width + b.SafeBorderPadding * 2,
        h: r.height + b.SafeBorderPadding * 2
      }), o.remove(), q2(o, this.fullLayer.parent);
    }), s && this.clearWorkShapeNodeCache(t)), s;
  }
  drawPencil(t, e) {
    return __async(this, null, function* () {
      yield this._post({
        subWorkerDrawCount: this.drawCount,
        consumeCount: this.consumeCount,
        render: [
          {
            rect: t == null ? void 0 : t.rect,
            workId: e,
            drawCanvas: nt.Float,
            viewId: this.viewId
          }
        ],
        sp: (t == null ? void 0 : t.op) && [t]
      });
    });
  }
  drawShape(t) {
    return __async(this, null, function* () {
      yield this._post({
        subWorkerDrawCount: this.drawCount,
        consumeCount: this.consumeCount,
        render: [
          {
            rect: (t == null ? void 0 : t.rect) && xI(t.rect),
            isClear: true,
            clearCanvas: nt.Float,
            viewId: this.viewId
          },
          {
            rect: (t == null ? void 0 : t.rect) && xI(t.rect),
            drawCanvas: nt.Float,
            viewId: this.viewId
          }
        ]
      });
    });
  }
  drawSelector(t) {
    return __async(this, null, function* () {
      yield this._post({
        subWorkerDrawCount: this.drawCount,
        consumeCount: this.consumeCount,
        render: [
          {
            rect: (t == null ? void 0 : t.rect) && xI(t.rect),
            isClear: true,
            clearCanvas: nt.Float,
            viewId: this.viewId
          },
          {
            rect: (t == null ? void 0 : t.rect) && xI(t.rect),
            drawCanvas: nt.Float,
            viewId: this.viewId
          }
        ]
      });
    });
  }
};
var Js = class {
  constructor(i) {
    y(this, "viewId");
    y(this, "vNodes");
    y(this, "topLayer");
    y(this, "thread");
    y(this, "post");
    y(this, "serviceWorkShapes", /* @__PURE__ */ new Map());
    y(this, "localWorkShapes", /* @__PURE__ */ new Map());
    y(this, "tmpOpt");
    y(this, "syncUnitTime", Se.syncOpt.interval);
    y(this, "animationId");
    this.viewId = i.viewId, this.vNodes = i.vNodes, this.topLayer = i.topLayer, this.thread = i.thread, this.post = i.thread.post.bind(i.thread);
  }
  canUseTopLayer(i) {
    return i === T.LaserPen;
  }
  getWorkShape(i) {
    return this.localWorkShapes.get(i);
  }
  createWorkShape(i, t) {
    if (i && this.tmpOpt) {
      const e = {
        toolsType: this.tmpOpt.toolsType,
        toolsOpt: t || this.tmpOpt.toolsOpt
      }, s = this.createWorkShapeNode(__spreadProps(__spreadValues({}, e), { workId: i }));
      return s && this.localWorkShapes.set(i, {
        node: s,
        toolsType: s.toolsType,
        workState: x.Start
      }), s;
    }
  }
  setWorkOptions(i, t) {
    var s;
    const e = (s = this.localWorkShapes.get(i)) == null ? void 0 : s.node;
    e || this.createWorkShape(i, t), e == null || e.setWorkOptions(t);
  }
  createWorkShapeNode(i) {
    const { toolsType: t } = i;
    if (t === T.LaserPen)
      return Lt(__spreadProps(__spreadValues({}, i), {
        vNodes: this.vNodes,
        fullLayer: this.topLayer,
        drawLayer: this.topLayer
      }));
  }
  clearAllWorkShapesCache() {
    this.localWorkShapes.forEach((i) => {
      var t;
      return (t = i.node) == null ? void 0 : t.clearTmpPoints();
    }), this.localWorkShapes.clear();
  }
  setToolsOpt(i) {
    var t;
    this.tmpOpt = i, (t = i.toolsOpt) != null && t.syncUnitTime && (this.syncUnitTime = i.toolsOpt.syncUnitTime);
  }
  getToolsOpt() {
    return this.tmpOpt;
  }
  consumeDraw(i) {
    const { workId: t, dataType: e } = i;
    if (e === q.Service)
      this.activeServiceWorkShape(i);
    else {
      const s = t == null ? void 0 : t.toString(), o = s && this.localWorkShapes.get(s);
      if (!o)
        return;
      const r = o.node.consume({
        data: i,
        isFullWork: false,
        isSubWorker: true
      });
      r.rect && (o.totalRect = SI(
        r.rect,
        o.totalRect
      ), o.result = r, o.workState = x.Doing, s && this.localWorkShapes.set(s, o));
    }
    this.runAnimation();
  }
  consumeDrawAll(i) {
    const { workId: t, dataType: e } = i;
    if (e === q.Service)
      this.activeServiceWorkShape(i);
    else {
      const s = t == null ? void 0 : t.toString(), o = s && this.localWorkShapes.get(s);
      if (!o)
        return;
      const r = o.node.consumeAll({ data: i });
      o.totalRect = SI(
        r.rect,
        o.totalRect
      ), o.result = r, o.workState = x.Done, s && this.localWorkShapes.set(s, o);
    }
    this.runAnimation();
  }
  destroy() {
    this.serviceWorkShapes.clear(), this.localWorkShapes.clear();
  }
  setNodeKey(i, t, e, s) {
    return t.toolsType = e, t.node = this.createWorkShapeNode({
      workId: i,
      toolsType: e,
      toolsOpt: s
    }), t;
  }
  activeServiceWorkShape(i) {
    var p2, d;
    const { workId: t, opt: e, toolsType: s, type: o, updateNodeOpt: r, ops: n, op: a } = i;
    if (!t)
      return;
    const c = t.toString(), l = (p2 = this.vNodes.get(c)) == null ? void 0 : p2.rect;
    if (!((d = this.serviceWorkShapes) != null && d.has(c))) {
      let u = {
        toolsType: s,
        animationWorkData: a || [],
        animationIndex: 0,
        type: o,
        updateNodeOpt: r,
        ops: n,
        oldRect: l
      };
      s && e && (u = this.setNodeKey(c, u, s, e)), this.serviceWorkShapes.set(c, u);
    }
    const h = this.serviceWorkShapes.get(c);
    o && (h.type = o), n && (h.animationWorkData = $n(n), h.ops = n), r && (h.updateNodeOpt = r), a && (h.animationWorkData = a), h.node && h.node.getWorkId() !== c && h.node.setWorkId(c), l && (h.oldRect = l), s && e && (h.toolsType !== s && s && e && this.setNodeKey(c, h, s, e), h.node && h.node.setWorkOptions(e));
  }
  computNextAnimationIndex(i, t) {
    const e = Math.floor(
      (i.animationWorkData || []).slice(i.animationIndex).length * 32 / t / this.syncUnitTime
    ) * t;
    return Math.min(
      (i.animationIndex || 0) + (e || t),
      (i.animationWorkData || []).length
    );
  }
  animationDraw() {
    return __async(this, null, function* () {
      var r, n, a, c;
      this.animationId = void 0;
      let i = false;
      const t = /* @__PURE__ */ new Map(), e = [], s = [], o = [];
      for (const [l, h] of this.serviceWorkShapes.entries())
        switch (h.toolsType) {
          case T.LaserPen: {
            const d = this.computNextAnimationIndex(
              h,
              8
            ), u = Math.max(0, h.animationIndex || 0), f = (h.animationWorkData || []).slice(
              u,
              d
            );
            if ((h.animationIndex || 0) < d) {
              const m = (r = h.node) == null ? void 0 : r.consumeService({
                op: f,
                isFullWork: false
              });
              h.totalRect = SI(h.totalRect, m), h.animationIndex = d, f.length && t.set(l, {
                workState: u === 0 ? x.Start : d === ((n = h.animationWorkData) == null ? void 0 : n.length) ? x.Done : x.Doing,
                op: f.slice(-2)
              });
            }
            if (s.push({
              isClear: true,
              rect: h.totalRect,
              clearCanvas: nt.TopFloat,
              viewId: this.viewId
            }), e.push({
              rect: h.totalRect,
              drawCanvas: nt.TopFloat,
              viewId: this.viewId
            }), h.isDel) {
              (a = h.node) == null || a.clearTmpPoints(), this.serviceWorkShapes.delete(l);
              break;
            }
            h.ops && h.animationIndex === ((c = h.animationWorkData) == null ? void 0 : c.length) && !h.isDel && (this.topLayer.getElementsByName(l.toString())[0] || (h.isDel = true, this.serviceWorkShapes.set(l, h))), i = true;
            break;
          }
        }
      for (const [l, h] of this.localWorkShapes.entries()) {
        const { result: p2, toolsType: d, totalRect: u, isDel: f, workState: m } = h;
        switch (d) {
          case T.LaserPen: {
            if (u && (s.push({
              isClear: true,
              rect: u,
              clearCanvas: nt.TopFloat,
              viewId: this.viewId
            }), e.push({
              rect: u,
              drawCanvas: nt.TopFloat,
              viewId: this.viewId
            })), f) {
              h.node.clearTmpPoints(), this.localWorkShapes.delete(l), o.push({
                removeIds: [l.toString()],
                viewId: this.viewId,
                type: R.RemoveNode
              });
              break;
            }
            p2 && ((p2.op || p2.ops) && o.push(p2), h.result = void 0), !this.topLayer.getElementsByName(l.toString())[0] && m === x.Done && (h.isDel = true, this.localWorkShapes.set(l, h)), i = true;
            break;
          }
        }
      }
      i && this.runAnimation(), t.size && t.forEach((l, h) => {
        o.push({
          type: R.Cursor,
          uid: h.split(st)[0],
          op: l.op,
          workState: l.workState,
          viewId: this.viewId
        });
      }), (e.length || s.length || o.length) && this.post({
        render: [...s, ...e],
        sp: o
      });
    });
  }
  runAnimation() {
    this.animationId || (this.animationId = setTimeout(() => {
      this.animationId = void 0, this.animationDraw();
    }, 16));
  }
};
var to = class extends Ye {
  constructor(t) {
    super(t);
    y(this, "opt");
    y(this, "scene");
    y(this, "cameraOpt");
    y(this, "vNodes");
    this.opt = t, this.vNodes = void 0;
  }
  createSnapshotFullLayer(t, e) {
    this.scene = this.opt.createScene(t), this.fullLayer = this.opt.createLayer("snapshotFullLayer", this.scene, e);
  }
  destroySnapshotFullLayer() {
    var t, e, s;
    Q(this.fullLayer, this.fullLayer.parent), (t = this.fullLayer) == null || t.disconnect(), (e = this.fullLayer) == null || e.remove(), (s = this.scene) == null || s.remove(), this.scene = void 0;
  }
  updateScene(t) {
    if (!this.scene)
      throw new Error("SnapshotWork scene is not initialized");
    this.scene.attr(__spreadValues({}, t));
    const { width: e, height: s } = t;
    this.scene.width = e, this.scene.height = s, this.updateLayer({ width: e, height: s });
  }
  updateLayer(t) {
    if (!this.fullLayer)
      throw new Error("SnapshotWork snapshotFullLayer is not initialized");
    const { width: e, height: s } = t;
    this.fullLayer.parent.setAttribute("width", e), this.fullLayer.parent.setAttribute("height", s), this.fullLayer.setAttribute("size", [e, s]), this.fullLayer.setAttribute("pos", [e * 0.5, s * 0.5]);
  }
  setCameraOpt(t) {
    this.cameraOpt = t;
    const { scale: e, centerX: s, centerY: o, width: r, height: n } = t;
    if (!this.scene)
      throw new Error("SnapshotWork scene is not initialized");
    if (!this.fullLayer)
      throw new Error("SnapshotWork snapshotFullLayer is not initialized");
    (r !== this.scene.width || n !== this.scene.height) && this.updateScene({ width: r, height: n }), this.fullLayer.setAttribute("scale", [e, e]), this.fullLayer.setAttribute("translate", [-s, -o]);
  }
  getOffscreen() {
    var e;
    return ((e = this.fullLayer) == null ? void 0 : e.parent).canvas;
  }
  getRectImageBitmap(t, e) {
    const { rect: s } = t, o = this.thread.dpr, r = Math.floor(s.x * o), n = Math.floor(s.y * o), a = Math.floor(s.w * o), c = Math.floor(s.h * o);
    return createImageBitmap(this.getOffscreen(), r, n, a, c, e);
  }
  getSnapshot(t) {
    return __async(this, null, function* () {
      const { scenePath: e, scenes: s, cameraOpt: o, w: r, h: n } = t;
      if (e && s && o && this.fullLayer) {
        this.setCameraOpt(o);
        for (const [c, l] of Object.entries(s))
          if (l != null && l.type)
            switch (l == null ? void 0 : l.type) {
              case R.UpdateNode:
              case R.FullWork: {
                const { opt: h } = l, p2 = __spreadProps(__spreadValues({}, l), {
                  opt: h,
                  workId: c,
                  msgType: R.FullWork,
                  dataType: q.Service,
                  viewId: this.viewId
                });
                yield this.runFullWork(p2);
                break;
              }
            }
        let a;
        r && n && (a = {
          resizeWidth: r,
          resizeHeight: n
        });
        try {
          yield this.getSnapshotRender({ scenePath: e, options: a });
        } catch (c) {
          const l = c && c instanceof Error ? c.message : c == null ? void 0 : c.toString();
          console.error("[SnapshotWork] getSnapshotRender error", l), this.thread._post({
            sp: [
              {
                type: R.ReportError,
                reportString: `[SnapshotWork] getSnapshotRender error: ${l}`
              }
            ]
          });
        }
      }
    });
  }
  runFullWork(t) {
    return __async(this, null, function* () {
      var o;
      const e = this.setFullWork(t), s = t.ops && $n(t.ops);
      if (e) {
        let r, n;
        const a = (o = e.getWorkId()) == null ? void 0 : o.toString();
        return e.toolsType === T.BackgroundSVG ? r = e.consumeService({
          isFullWork: true,
          replaceId: a
        }) : e.toolsType === T.Image ? r = yield e.consumeServiceAsync({
          isFullWork: true,
          worker: this.thread
        }) : e.toolsType === T.Text ? r = yield e.consumeServiceAsync({
          isFullWork: true,
          replaceId: a,
          isDrawLabel: true
        }) : (r = e.consumeService({
          op: s,
          isFullWork: true,
          replaceId: a
        }), n = (t == null ? void 0 : t.updateNodeOpt) && e.updataOptService(t.updateNodeOpt)), t.workId && this.workShapes.delete(t.workId.toString()), SI(r, n);
      }
    });
  }
  getSceneRect() {
    if (!this.scene)
      throw new Error("SnapshotWork scene is not initialized");
    const { width: t, height: e } = this.scene;
    return {
      x: 0,
      y: 0,
      w: Math.floor(t),
      h: Math.floor(e)
    };
  }
  getSnapshotRender(t) {
    return __async(this, null, function* () {
      var r;
      const { scenePath: e, options: s } = t;
      ((r = this.fullLayer) == null ? void 0 : r.parent).render();
      const o = yield this.getRectImageBitmap(
        { rect: this.getSceneRect(), drawCanvas: nt.None },
        s
      );
      o && (this.thread._post(
        {
          sp: [
            {
              type: R.Snapshot,
              scenePath: e,
              imageBitmap: o,
              viewId: this.viewId,
              index: 0
            }
          ]
        },
        [o]
      ), o.close());
    });
  }
  getBoundingRect(t) {
    return __async(this, null, function* () {
      const { scenePath: e, scenes: s, cameraOpt: o } = t;
      if (e && s && o && this.fullLayer) {
        this.setCameraOpt(o);
        let r;
        for (const [n, a] of Object.entries(s))
          if (a != null && a.type)
            switch (a == null ? void 0 : a.type) {
              case R.UpdateNode:
              case R.FullWork: {
                const c = yield this.runFullWork(__spreadProps(__spreadValues({}, a), {
                  workId: n,
                  msgType: R.FullWork,
                  dataType: q.Service,
                  viewId: this.viewId
                }));
                r = SI(r, c);
                break;
              }
            }
        r && this.thread._post({
          sp: [
            {
              type: R.BoundingBox,
              scenePath: e,
              rect: r,
              viewId: this.viewId
            }
          ]
        });
      }
    });
  }
};
var Qt = ((T2) => (T2.Full = "full", T2.Sub = "sub", T2))(Qt || {});
var eo = class _eo extends Ks {
  constructor(t, e, s) {
    super(
      t,
      e,
      "sub"
      /* Sub */
    );
    y(this, "type", "sub");
    y(this, "_post");
    y(this, "topLayer");
    y(this, "serviceWork");
    y(this, "localWork");
    y(this, "topLayerWork");
    y(this, "snapshotWork");
    this._post = s;
    const o = Se.bufferSize.sub;
    this.topLayer = e.isUseSimple ? void 0 : this.createLayer("topLayer", this.scene, __spreadProps(__spreadValues({}, e.layerOpt), {
      bufferSize: o,
      contextType: "2d"
    }));
    const r = {
      thread: this,
      viewId: this.viewId,
      vNodes: this.vNodes,
      fullLayer: this.fullLayer,
      topLayer: this.topLayer,
      isUseSimple: e.isUseSimple
    };
    this.localWork = new Qs(r), e.isUseSimple || (this.topLayerWork = new Js(r)), this.snapshotWork = new to(__spreadProps(__spreadValues({}, r), {
      createScene: this.createScene.bind(this),
      createLayer: this.createLayer.bind(this)
    })), this.vNodes.init(this.fullLayer);
  }
  combinePost() {
    return __async(this, null, function* () {
      var r, n;
      const _a = this.combinePostData(), { render: t } = _a, e = __objRest(_a, ["render"]);
      let s;
      if (t != null && t.length) {
        const a = [];
        for (const c of t)
          if (c.rect) {
            if (c.rect = io(
              un(c.rect),
              this.scene,
              this.dpr
            ), !c.rect)
              continue;
            if (c.drawCanvas && c.rect && c.rect.w > 0 && c.rect.h > 0) {
              const l = yield this.getRectImageBitmap(
                c
              );
              c.imageBitmap = l, s || (s = []), s.push(l);
            }
            a.push(c);
          }
        e.render = a;
      }
      const o = (r = e.sp) == null ? void 0 : r.filter(
        (a) => a.type !== R.None || a.isLockSentEventCursor || a.needUndoTicker
      );
      o != null && o.length ? e.sp = o.map((a) => __spreadProps(__spreadValues({}, a), { viewId: this.viewId })) : delete e.sp, e.consumeCount === void 0 && delete e.consumeCount, e.subWorkerDrawCount === void 0 && delete e.subWorkerDrawCount, (o != null && o.length || e.consumeCount || e.subWorkerDrawCount || (n = e == null ? void 0 : e.render) != null && n.length) && this._post(e, s), this.delayPostDoneResolve && this.delayPostDoneResolve(true);
    });
  }
  on(t) {
    return __async(this, null, function* () {
      var c, l;
      const {
        msgType: e,
        toolsType: s,
        opt: o,
        dataType: r,
        workState: n,
        isLockSentEventCursor: a
      } = t;
      switch (e) {
        case R.UpdateTools: {
          if (s && ((c = this.topLayerWork) != null && c.canUseTopLayer(s)) && o) {
            const h = {
              toolsType: s,
              toolsOpt: o
            };
            this.topLayerWork.setToolsOpt(h);
            return;
          }
          break;
        }
        case R.CreateWork: {
          this.createLocalWork(t);
          return;
        }
        case R.DrawWork: {
          n === x.Done && r === q.Local ? (yield this.consumeDrawAll(r, t), s === T.LaserPen && a && this.post({
            sp: [
              {
                type: R.None,
                isLockSentEventCursor: a
              }
            ]
          })) : this.consumeDraw(r, t);
          return;
        }
        case R.RemoveNode: {
          yield this.removeNode(t);
          return;
        }
        case R.FullWork: {
          s && ((l = this.topLayerWork) != null && l.canUseTopLayer(s)) && (yield this.consumeDrawAll(r, t));
          return;
        }
        case R.Snapshot: {
          this.snapshotWork.createSnapshotFullLayer(this.opt, __spreadProps(__spreadValues({}, this.opt.layerOpt), {
            bufferSize: this.viewId === pe ? 6e3 : 3e3,
            contextType: "2d"
          })), yield this.snapshotWork.getSnapshot(t), this.snapshotWork.destroySnapshotFullLayer();
          return;
        }
        case R.BoundingBox: {
          this.snapshotWork.createSnapshotFullLayer(this.opt, __spreadProps(__spreadValues({}, this.opt.layerOpt), {
            bufferSize: this.viewId === pe ? 6e3 : 3e3,
            contextType: "2d"
          })), yield this.snapshotWork.getBoundingRect(t), this.snapshotWork.destroySnapshotFullLayer();
          return;
        }
      }
      yield __superGet(_eo.prototype, this, "on").call(this, t);
    });
  }
  createLocalWork(t) {
    var r;
    const { workId: e, toolsType: s, opt: o } = t;
    if (s && ((r = this.topLayerWork) != null && r.canUseTopLayer(s)) && e && o) {
      this.topLayerWork.getToolsOpt() || this.topLayerWork.setToolsOpt({
        toolsType: s,
        toolsOpt: o
      }), this.topLayerWork.setWorkOptions(e.toString(), o);
      return;
    }
    s && super.createLocalWork(t);
  }
  removeNode(t) {
    return __async(this, null, function* () {
      const { dataType: e } = t;
      e === q.Local && (yield this.localWork.removeWork(t));
    });
  }
  getLayer(t) {
    switch (t) {
      case nt.TopFloat:
        return this.topLayer || this.fullLayer;
      default:
        return this.fullLayer;
    }
  }
  getOffscreen(t) {
    var s;
    return ((s = this.getLayer(t)) == null ? void 0 : s.parent).canvas;
  }
  consumeDraw(t, e) {
    return __async(this, null, function* () {
      var r;
      const { workId: s, toolsType: o } = e;
      if (s) {
        if (o && ((r = this.topLayerWork) != null && r.canUseTopLayer(o))) {
          t === q.Local && (this.topLayerWork.getWorkShape(
            s.toString()
          ) || this.createLocalWork(e)), this.topLayerWork.consumeDraw(e);
          return;
        }
        o && (this.localWork.getWorkShape(s.toString()) || this.createLocalWork(e), yield this.localWork.consumeDraw(e));
        return;
      }
    });
  }
  consumeDrawAll(t, e) {
    return __async(this, null, function* () {
      var n;
      const { workId: s, toolsType: o, dataType: r } = e;
      if (s) {
        const a = s.toString();
        if (o && ((n = this.topLayerWork) != null && n.canUseTopLayer(o))) {
          r === q.Local && (this.topLayerWork.getWorkShape(a) || this.createLocalWork(e)), this.topLayerWork.consumeDrawAll(e);
          return;
        }
        o && (this.localWork.getWorkShape(a) || __superGet(_eo.prototype, this, "createLocalWork").call(this, e), this.localWork.consumeDrawAll(e));
        return;
      }
    });
  }
  clearAll() {
    return __async(this, null, function* () {
      this.vNodes.clear(), __superGet(_eo.prototype, this, "clearAll").call(this), this.topLayer && (this.topLayer.parent.children.forEach((t) => {
        t.name !== "viewport" && t.remove();
      }), Q(this.topLayer, this.topLayer.parent)), yield this.post({
        render: [
          {
            isClearAll: true,
            clearCanvas: nt.TopFloat,
            viewId: this.viewId
          }
        ],
        sp: [
          {
            type: R.Clear
          }
        ]
      });
    });
  }
  getRectImageBitmap(t, e) {
    const { rect: s, drawCanvas: o } = t, r = Math.floor(s.x * this.dpr), n = Math.floor(s.y * this.dpr), a = Math.floor(s.w * this.dpr || 1), c = Math.floor(s.h * this.dpr || 1);
    return createImageBitmap(
      this.getOffscreen(o),
      r,
      n,
      a,
      c,
      e
    );
  }
  updateLayer(t) {
    const { width: e, height: s } = t;
    super.updateLayer(t), this.topLayer && (this.topLayer.parent.setAttribute("width", e), this.topLayer.parent.setAttribute("height", s), this.topLayer.setAttribute("size", [e, s]), this.topLayer.setAttribute("pos", [e * 0.5, s * 0.5]));
  }
  updateDpr(t) {
    return __async(this, null, function* () {
      __superGet(_eo.prototype, this, "updateDpr").call(this, t);
      const e = this.topLayerWork && !!this.topLayerWork.localWorkShapes.size || false, s = !!this.localWork.getWorkShapes().size, o = [];
      s && o.push({
        isClearAll: true,
        clearCanvas: nt.Float,
        viewId: this.viewId
      }), e && o.push({
        isClearAll: true,
        clearCanvas: nt.TopFloat,
        viewId: this.viewId
      }), o.length && (yield this.post({ render: o }));
    });
  }
  updateCamera(t) {
    return __async(this, null, function* () {
      var o;
      const e = [], { cameraOpt: s } = t;
      if (s && !lt(this.cameraOpt, s)) {
        const r = this.topLayerWork && !!this.topLayerWork.localWorkShapes.size || false, n = !!this.localWork.getWorkShapes().size;
        if (n && this.localWork.workShapesDone(), this.setCameraOpt(s), n && e.push({
          isClearAll: true,
          clearCanvas: nt.Float,
          viewId: this.viewId
        }), r && this.topLayerWork) {
          e.push({
            isClearAll: true,
            clearCanvas: nt.TopFloat,
            viewId: this.viewId
          });
          for (const [
            a,
            c
          ] of this.topLayerWork.localWorkShapes.entries() || [])
            if (c.totalRect) {
              let l;
              (o = this.topLayer) == null || o.getElementsByName(a.toString()).forEach((h) => {
                const p2 = h.getBoundingClientRect(), d = xI({
                  x: p2.x,
                  y: p2.y,
                  w: p2.width,
                  h: p2.height
                });
                l = SI(l, d);
              }), c.totalRect = l, this.topLayerWork.localWorkShapes.set(a, c);
            }
        }
        e.length && (yield this.post({ render: e }));
      }
    });
  }
  setCameraOpt(t, e) {
    var c, l;
    this.cameraOpt = t;
    const { scale: s, centerX: o, centerY: r, width: n, height: a } = t;
    (n !== this.scene.width || a !== this.scene.height) && this.updateScene(this.scene, { width: n, height: a }), e ? (e.setAttribute("scale", [s, s]), e.setAttribute("translate", [-o, -r])) : (this.fullLayer.setAttribute("scale", [s, s]), this.fullLayer.setAttribute("translate", [-o, -r]), (c = this.topLayer) == null || c.setAttribute("scale", [s, s]), (l = this.topLayer) == null || l.setAttribute("translate", [-o, -r]));
  }
};
var Dt = class _Dt extends b {
  constructor(t) {
    super(t);
    y(this, "canRotate", true);
    y(this, "scaleType", Fe.all);
    y(this, "toolsType", T.Image);
    y(this, "tmpPoints", []);
    y(this, "workOptions");
    y(this, "oldRect");
    this.workOptions = t.toolsOpt, this.scaleType = _Dt.getScaleType(this.workOptions);
  }
  consume() {
    return { type: R.None };
  }
  consumeAll() {
    return { type: R.None };
  }
  draw(t) {
    const { layer: e, workId: s, replaceId: o, imageBitmap: r } = t, { centerX: n, centerY: a, width: c, height: l, rotate: h, zIndex: p2, eraserlines: d } = this.workOptions, u = new _spritejs$Group({
      anchor: [0.5, 0.5],
      pos: [n, a],
      name: s,
      size: [c, l],
      zIndex: p2,
      rotate: h
    }), f = {
      anchor: [0.5, 0.5],
      pos: [0, 0],
      size: [c, l],
      texture: r
    };
    r || (f.bgcolor = "rgba(0,0,0,0.3)");
    const m = new _spritejs$Sprite(f);
    u.append(m), d && this.drawEraserlines({
      group: u,
      eraserlines: d,
      pos: [n, a],
      layer: e
    }), this.replace(e, o || s, u);
    const g = u.getBoundingClientRect();
    if (g)
      return {
        x: Math.floor(g.x - b.SafeBorderPadding),
        y: Math.floor(g.y - b.SafeBorderPadding),
        w: Math.floor(g.width + b.SafeBorderPadding * 2),
        h: Math.floor(g.height + b.SafeBorderPadding * 2)
      };
  }
  consumeService() {
  }
  consumeServiceAsync(t) {
    return __async(this, null, function* () {
      var l, h, p2, d;
      const { isFullWork: e, replaceId: s, worker: o } = t, { src: r, uuid: n } = this.workOptions, a = ((l = this.workId) == null ? void 0 : l.toString()) || n, c = e ? this.fullLayer : this.drawLayer || this.fullLayer;
      if (r) {
        const u = yield o.loadImageBitMap({
          toolsType: this.toolsType,
          opt: this.workOptions,
          workId: a,
          isSubWorker: o instanceof eo
        });
        if (u) {
          const f = this.draw({ workId: a, layer: c, replaceId: s, imageBitmap: u });
          return this.oldRect = a && ((p2 = (h = this.vNodes) == null ? void 0 : h.get(a)) == null ? void 0 : p2.rect) || void 0, (d = this.vNodes) == null || d.setInfo(a, {
            rect: f,
            op: [],
            opt: this.workOptions,
            toolsType: this.toolsType,
            scaleType: this.scaleType,
            canRotate: this.canRotate,
            centerPos: f && b.getCenterPos(f, c)
          }), f;
        }
      }
    });
  }
  clearTmpPoints() {
    this.tmpPoints.length = 0;
  }
  static getScaleType(t) {
    const { uniformScale: e, rotate: s } = t;
    return e !== false ? Fe.proportional : s && Math.abs(s) % 90 > 0 ? Fe.proportional : Fe.all;
  }
  static updateNodeOpt(t) {
    const { node: e, opt: s, vNodes: o, targetNode: r } = t, {
      translate: n,
      originPoint: a,
      scenePoint: c,
      scale: l,
      angle: h,
      isLocked: p2,
      zIndex: d,
      strokeColor: u
    } = s, f = r && un(r) || o.get(e.name);
    if (!f) return;
    const m = e.parent;
    if (m) {
      if (u && (f.opt.strokeColor = u), H(d) && (e.setAttribute("zIndex", d), f.opt.zIndex = d), kt(p2) && (f.opt.locked = p2), a && c && l && n) {
        const { centerX: g, centerY: I, width: S, height: v, uniformScale: L } = f.opt, C = L !== false ? [l[0], l[0]] : l, N = [g, I], R2 = [g, I];
        EI(R2, c, C, n);
        const P = [
          R2[0] - N[0],
          R2[1] - N[1]
        ];
        if (f.centerPos = [
          f.centerPos[0] + P[0],
          f.centerPos[1] + P[1]
        ], f.opt.width = Math.round(S * C[0]), f.opt.height = Math.round(v * C[1]), f.opt.centerX = R2[0], f.opt.centerY = R2[1], f.opt.eraserlines)
          for (const M of f.opt.eraserlines) {
            const { op: D, thickness: B } = M;
            M.thickness = Math.round(B * Math.max(l[0], l[1]));
            for (let F = 0; F < D.length; F++)
              LI(D[F], c, l, n);
          }
      } else if (n) {
        if (f.opt.centerX = f.opt.centerX + n[0], f.opt.centerY = f.opt.centerY + n[1], f.centerPos = [
          f.centerPos[0] + n[0],
          f.centerPos[1] + n[1]
        ], f.opt.eraserlines)
          for (const g of f.opt.eraserlines) {
            const { op: I } = g;
            for (let S = 0; S < I.length; S++) {
              const v = I[S].map((L, C) => C % 2 ? L + n[1] : L + n[0]);
              g.op[S] = v;
            }
          }
      } else if (H(h))
        if (f.opt.rotate = h, f.scaleType = _Dt.getScaleType(f.opt), r) {
          const g = DI(f.rect, h);
          f.rect = g;
        } else {
          const g = b.getRectFromLayer(m, e.name);
          f.rect = g || f.rect;
        }
      return f && o.setInfo(e.name, f), f == null ? void 0 : f.rect;
    }
  }
};
var so = (T2) => {
  if (T2.tagName === "GROUP") {
    const i = Object.getOwnPropertySymbols(T2).find(
      (t) => t.toString() === "Symbol(sealed)"
    );
    if (i && T2[i])
      return true;
  }
  return false;
};
var q2 = (T2, i) => {
  if (T2.mesh && T2.mesh.texture && T2.name === "eraserTexture") {
    const t = T2.mesh.texture.image;
    i.deleteTexture(t);
  } else if (T2.tagName === "GROUP" && T2 && T2.children)
    for (const t of T2.children)
      q2(t, i);
};
var Q = (T2, i) => {
  q2(T2, i), T2.removeAllChildren();
};
var oo = (T2, i, t) => {
  const e = t.parent;
  if (i) {
    const s = i.children;
    if (s) {
      for (const o of s)
        if (q2(o, e), o.tagName === "GROUP")
          for (const r of o.children)
            q2(r, e);
    }
  }
  i.parent.replaceChild(T2, i);
};
var io = (T2, i, t) => {
  if (T2.w + T2.x <= 0 || T2.h + T2.y <= 0 || T2.w <= 0 || T2.h <= 0)
    return;
  const e = i.width, s = i.height, o = {
    x: Math.floor(Math.max(0, T2.x)),
    y: Math.floor(Math.max(0, T2.y)),
    w: Math.floor(Math.min(e, T2.w)),
    h: Math.floor(Math.min(s, T2.h))
  };
  if (o.x + o.w > e && (o.w = Math.floor(e - o.x)), o.y + o.h > s && (o.h = Math.floor(s - o.y)), RI(t)) {
    const r = YI(t), n = Math.pow(10, r), a = UI(n, t * n), c = o.x % a, l = o.x - c;
    l >= 0 ? (o.x = l, o.w = o.w + c) : (o.x = 0, o.w = o.w + c - l);
    const h = o.y % a, p2 = o.y - h;
    p2 >= 0 ? (o.y = p2, o.h = o.h + h) : (o.y = 0, o.h = o.h + h - p2);
  }
  return o;
};
var ro = class {
  constructor(i) {
    y(this, "vNodes");
    y(this, "thread");
    y(this, "serviceWorkShapes", /* @__PURE__ */ new Map());
    y(this, "localWorkShapes", /* @__PURE__ */ new Map());
    y(this, "tmpOpt");
    y(this, "animationId");
    y(this, "syncUnitTime", Se.syncOpt.interval);
    this.vNodes = i.vNodes, this.thread = i.thread;
  }
  createLocalWork(i) {
    const { workId: t, opt: e, toolsType: s } = i;
    if (t && e) {
      const o = t.toString();
      !this.getToolsOpt() && s && this.setToolsOpt({
        toolsType: s,
        toolsOpt: e
      }), this.setWorkOptions(o, e);
    }
  }
  getLocalWorkShape(i) {
    return this.localWorkShapes.get(i);
  }
  createLocalWorkShape(i, t) {
    if (i && this.tmpOpt) {
      const e = {
        toolsType: this.tmpOpt.toolsType,
        toolsOpt: t || this.tmpOpt.toolsOpt
      }, s = this.createWorkShapeNode(__spreadProps(__spreadValues({}, e), { workId: i }));
      return s && this.localWorkShapes.set(i, {
        node: s,
        toolsType: s.toolsType,
        workState: x.Start
      }), s;
    }
  }
  canUseTopLayer(i) {
    return i === T.LaserPen;
  }
  destroy() {
    this.clearAll();
  }
  clearAll() {
    this.thread.topLayer.children.length && (this.thread.topLayer.parent.children.forEach((i) => {
      i.name !== "viewport" && i.remove();
    }), Q(
      this.thread.serviceLayer,
      this.thread.serviceLayer.parent
    )), this.serviceWorkShapes.clear(), this.localWorkShapes.clear();
  }
  consumeDraw(i) {
    const { workId: t, dataType: e } = i;
    if (e === q.Service)
      this.activeServiceWorkShape(i);
    else {
      const s = t == null ? void 0 : t.toString(), o = s && this.localWorkShapes.get(s);
      if (!o)
        return;
      const r = o.node.consume({
        data: i,
        isFullWork: false,
        isSubWorker: true
      });
      r.rect && (o.result = r, o.workState = x.Doing, s && this.localWorkShapes.set(s, o));
    }
    this.runAnimation();
  }
  setToolsOpt(i) {
    var t;
    this.tmpOpt = i, (t = i.toolsOpt) != null && t.syncUnitTime && (this.syncUnitTime = i.toolsOpt.syncUnitTime);
  }
  getToolsOpt() {
    return this.tmpOpt;
  }
  createWorkShapeNode(i) {
    const { toolsType: t } = i;
    if (t === T.LaserPen)
      return Lt(__spreadProps(__spreadValues({}, i), {
        vNodes: this.vNodes,
        fullLayer: this.thread.topLayer,
        drawLayer: this.thread.topLayer
      }));
  }
  setNodeKey(i, t, e, s) {
    return t.toolsType = e, t.node = this.createWorkShapeNode({
      workId: i,
      toolsType: e,
      toolsOpt: s
    }), t;
  }
  activeServiceWorkShape(i) {
    var p2, d;
    const { workId: t, opt: e, toolsType: s, type: o, updateNodeOpt: r, ops: n, op: a } = i;
    if (!t)
      return;
    const c = t.toString(), l = (p2 = this.vNodes.get(c)) == null ? void 0 : p2.rect;
    if (!((d = this.serviceWorkShapes) != null && d.has(c))) {
      let u = {
        toolsType: s,
        animationWorkData: a || [],
        animationIndex: 0,
        type: o,
        updateNodeOpt: r,
        ops: n,
        oldRect: l
      };
      s && e && (u = this.setNodeKey(c, u, s, e)), this.serviceWorkShapes.set(c, u);
    }
    const h = this.serviceWorkShapes.get(c);
    o && (h.type = o), n && (h.animationWorkData = $n(n), h.ops = n), r && (h.updateNodeOpt = r), a && (h.animationWorkData = a), h.node && h.node.getWorkId() !== c && h.node.setWorkId(c), l && (h.oldRect = l), s && e && (h.toolsType !== s && s && e && this.setNodeKey(c, h, s, e), h.node && h.node.setWorkOptions(e));
  }
  computNextAnimationIndex(i, t) {
    var o;
    const e = ((o = i.node) == null ? void 0 : o.syncUnitTime) || this.syncUnitTime, s = Math.floor(
      (i.animationWorkData || []).slice(i.animationIndex).length * 32 / t / e
    ) * t;
    return Math.min(
      (i.animationIndex || 0) + (s || t),
      (i.animationWorkData || []).length
    );
  }
  animationDraw() {
    var s, o, r, n;
    this.animationId = void 0;
    let i = false;
    const t = /* @__PURE__ */ new Map(), e = [];
    for (const [a, c] of this.serviceWorkShapes.entries())
      switch (c.toolsType) {
        case T.LaserPen: {
          const h = this.computNextAnimationIndex(
            c,
            8
          ), p2 = Math.max(0, c.animationIndex || 0), d = (c.animationWorkData || []).slice(
            p2,
            h
          );
          if ((c.animationIndex || 0) < h && ((s = c.node) == null || s.consumeService({
            op: d,
            isFullWork: false
          }), c.animationIndex = h, d.length && t.set(a, {
            workState: p2 === 0 ? x.Start : h === ((o = c.animationWorkData) == null ? void 0 : o.length) ? x.Done : x.Doing,
            op: d.slice(-2)
          })), c.isDel) {
            (r = c.node) == null || r.clearTmpPoints(), this.serviceWorkShapes.delete(a);
            break;
          }
          c.ops && c.animationIndex === ((n = c.animationWorkData) == null ? void 0 : n.length) && !c.isDel && (this.thread.topLayer.getElementsByName(
            a.toString()
          )[0] || (c.isDel = true, this.serviceWorkShapes.set(a, c))), i = true;
          break;
        }
      }
    for (const [a, c] of this.localWorkShapes.entries()) {
      const { result: l, toolsType: h, isDel: p2, workState: d } = c;
      switch (h) {
        case T.LaserPen: {
          if (p2) {
            c.node.clearTmpPoints(), this.localWorkShapes.delete(a), e.push({
              removeIds: [a.toString()],
              type: R.RemoveNode
            });
            break;
          }
          l && ((l.op || l.ops) && e.push(l), c.result = void 0), !this.thread.topLayer.getElementsByName(
            a.toString()
          )[0] && d === x.Done && (c.isDel = true, this.localWorkShapes.set(a, c)), i = true;
          break;
        }
      }
    }
    i && this.runAnimation(), t.size && t.forEach((a, c) => {
      e.push({
        type: R.Cursor,
        uid: c.split(st)[0],
        op: a.op,
        workState: a.workState,
        viewId: this.thread.viewId
      });
    }), e.length && this.thread.post({ sp: e });
  }
  runAnimation() {
    this.animationId || (this.animationId = requestAnimationFrame(this.animationDraw.bind(this)));
  }
  setWorkOptions(i, t) {
    var s;
    let e = (s = this.localWorkShapes.get(i)) == null ? void 0 : s.node;
    if (!e && this.tmpOpt) {
      const { toolsType: o } = this.tmpOpt;
      this.tmpOpt.toolsOpt = t, e = this.createWorkShapeNode({ workId: i, toolsType: o, toolsOpt: t }), e && this.localWorkShapes.set(i, {
        node: e,
        toolsType: o,
        workState: x.Start
      }), this.setToolsOpt(this.tmpOpt);
    }
    t != null && t.syncUnitTime || (t.syncUnitTime = this.syncUnitTime), e && e.setWorkOptions(t);
  }
  consumeDrawAll(i) {
    const { workId: t, dataType: e } = i;
    if (e === q.Service)
      this.activeServiceWorkShape(i);
    else {
      const s = t == null ? void 0 : t.toString(), o = s && this.localWorkShapes.get(s);
      if (!o)
        return;
      const r = o.node.consumeAll({ data: i });
      o.result = r, o.workState = x.Done, s && this.localWorkShapes.set(s, o);
    }
    this.runAnimation();
  }
};
var no = class {
  constructor(i) {
    y(this, "vNodes");
    y(this, "thread");
    y(this, "workShapes", /* @__PURE__ */ new Map());
    y(this, "effectSelectNodeData", /* @__PURE__ */ new Set());
    y(this, "batchEraserRemoveNodes", /* @__PURE__ */ new Set());
    y(this, "batchEraserWorks", /* @__PURE__ */ new Set());
    y(this, "tmpOpt");
    y(this, "syncUnitTime", Se.syncOpt.interval);
    y(this, "fullWorkerDrawCount", 0);
    y(this, "drawWorkActiveId");
    y(this, "consumeCount", 0);
    y(this, "combineTimerId");
    y(this, "combineDrawResolve");
    y(this, "combineDrawActiveId");
    this.vNodes = i.vNodes, this.thread = i.thread;
  }
  loadImageBitMap(i) {
    return __async(this, null, function* () {
      return yield this.thread.loadImageBitMap(i);
    });
  }
  createLocalWork(i) {
    const { workId: t, opt: e, toolsType: s } = i;
    if (t && e) {
      const o = t.toString();
      !this.getToolsOpt() && s && this.setToolsOpt({
        toolsType: s,
        toolsOpt: e
      }), this.setWorkOptions(o, e);
    }
  }
  // async workShapesDone(scenePath: string, serviceWork: SubServiceThread) {
  //     for (const key of this.workShapes.keys()) {
  //         await this.consumeDrawAll({
  //             workId: key,
  //             scenePath,
  //             viewId: this.thread.viewId,
  //             msgType: EPostMessageType.DrawWork,
  //             dataType: EDataType.Local
  //         }, serviceWork)
  //     }
  // }
  updateSelector(i) {
    return __async(this, null, function* () {
      var d;
      const t = this.workShapes.get(
        $
      );
      if (!((d = t == null ? void 0 : t.selectIds) != null && d.length)) return;
      const _a = i, { callback: e } = _a, s = __objRest(_a, ["callback"]), { updateSelectorOpt: o, willSerializeData: r, smoothSync: n } = s, a = yield t == null ? void 0 : t.updateSelector({
        updateSelectorOpt: o,
        selectIds: (0, import_lodash.cloneDeep)(t.selectIds),
        vNodes: this.vNodes,
        willSerializeData: r,
        worker: this
      }), c = /* @__PURE__ */ new Map();
      let l;
      a != null && a.selectIds && (l = (0, import_lodash.xor)(t.selectIds, a.selectIds), a.selectIds.forEach((u) => {
        const f = this.vNodes.get(u);
        if (f) {
          const { toolsType: m, op: g, opt: I } = f;
          c.set(u, {
            opt: I,
            toolsType: m,
            ops: (g == null ? void 0 : g.length) && _t(g) || void 0
          });
        }
      }), t.selectIds = a.selectIds);
      const h = [], p2 = e && e({
        res: a,
        workShapeNode: t,
        param: s,
        postData: { sp: h },
        newServiceStore: c,
        smoothSync: n
      }) || { sp: h };
      l && p2.sp.push({
        type: R.RemoveNode,
        removeIds: l,
        viewId: this.thread.viewId
      }), p2.sp.length && this.thread.post(p2);
    });
  }
  destroy() {
    this.clearAll();
  }
  clearAll() {
    if (this.thread.localLayer.children.length && (this.thread.topLayer.parent.children.forEach((t) => {
      t.name !== "viewport" && t.remove();
    }), Q(
      this.thread.localLayer,
      this.thread.localLayer.parent
    )), this.workShapes.get(
      $
    )) {
      const t = [];
      t.push({
        type: R.Select,
        dataType: q.Local,
        selectIds: [],
        willSyncService: false
      }), this.thread.post({ sp: t });
    }
    this.workShapes.clear(), this.effectSelectNodeData.clear(), this.batchEraserWorks.clear(), this.batchEraserRemoveNodes.clear();
  }
  checkTextActive(i) {
    return __async(this, null, function* () {
      const { op: t, viewId: e, dataType: s } = i;
      if (t != null && t.length) {
        let o;
        for (const r of this.vNodes.curNodeMap.values()) {
          const { rect: n, name: a, toolsType: c, opt: l } = r, h = t[0] * this.thread.fullLayer.worldScaling[0] + this.thread.fullLayer.worldPosition[0], p2 = t[1] * this.thread.fullLayer.worldScaling[1] + this.thread.fullLayer.worldPosition[1];
          if (c === T.Text && PM([h, p2], n) && l.workState === x.Done) {
            o = a;
            break;
          }
        }
        o && (yield this.blurSelector({
          viewId: e,
          msgType: R.Select,
          dataType: s,
          isSync: true
        }), this.thread.post({
          sp: [
            {
              type: R.GetTextActive,
              toolsType: T.Text,
              workId: o
            }
          ]
        }));
      }
    });
  }
  cursorHover(i) {
    const { opt: t, toolsType: e, point: s } = i, o = this.setFullWork({
      workId: NI,
      toolsType: e,
      opt: t
    });
    o && s && o.cursorHover(s);
  }
  cursorBlur() {
    var t;
    const i = this.getWorkShape(NI);
    i && ((t = i.selectIds) != null && t.length) && (i.cursorBlur(), this.clearWorkShapeNodeCache(NI)), this.thread.fullLayer.parent.children.forEach((e) => {
      e.name === "Cursor_Hover_Id" && e.remove();
    });
  }
  updateFullSelectWork(i) {
    var s, o, r, n, a;
    const t = this.workShapes.get(
      $
    ), { selectIds: e } = i;
    if (!(e != null && e.length)) {
      this.blurSelector(i);
      return;
    }
    if (!t) {
      const c = this.setFullWork(i);
      !c && i.workId && this.tmpOpt && ((s = this.tmpOpt) == null ? void 0 : s.toolsType) === T.Selector && this.setWorkOptions(
        i.workId.toString(),
        i.opt || this.tmpOpt.toolsOpt
      ), c && this.updateFullSelectWork(i);
      return;
    }
    if (t && (e != null && e.length)) {
      const { selectRect: c } = t.updateSelectIds(e), l = [
        __spreadProps(__spreadValues({}, i), {
          selectorColor: ((o = i.opt) == null ? void 0 : o.strokeColor) || t.selectorColor,
          strokeColor: ((r = i.opt) == null ? void 0 : r.strokeColor) || t.strokeColor,
          fillColor: ((n = i.opt) == null ? void 0 : n.fillColor) || t.fillColor,
          textOpt: ((a = i.opt) == null ? void 0 : a.textOpt) || t.textOpt,
          canTextEdit: t.canTextEdit,
          canRotate: t.canRotate,
          scaleType: t.scaleType,
          type: R.Select,
          selectRect: c,
          points: t.getChildrenPoints(),
          willSyncService: (i == null ? void 0 : i.willSyncService) || false,
          opt: (i == null ? void 0 : i.willSyncService) && t.getWorkOptions() || void 0,
          canLock: t.canLock,
          isLocked: t.isLocked,
          toolsTypes: t.toolsTypes,
          shapeOpt: t.shapeOpt,
          thickness: t.thickness,
          useStroke: t.useStroke,
          strokeType: t.strokeType
        })
      ];
      this.thread.post({ sp: l });
    }
  }
  removeSelector(i) {
    return __async(this, null, function* () {
      const { willSyncService: t, needUndoTicker: e } = i, s = [], o = [], r = this.workShapes.get(
        $
      );
      if (!r)
        return;
      const n = r.selectIds && [...r.selectIds] || [];
      for (const a of n) {
        const c = this.vNodes.get(a);
        if (c)
          switch (c.toolsType) {
            case T.Text: {
              s.push({
                type: R.TextUpdate,
                toolsType: T.Text,
                workId: a,
                dataType: q.Local
              });
              break;
            }
            case T.BackgroundSVG:
              s.push({
                type: R.BackgroundSVGDelete,
                toolsType: T.BackgroundSVG,
                workId: a,
                dataType: q.Local,
                viewId: this.thread.viewId
              });
              break;
          }
        this.removeNode(a), o.push(a);
      }
      o.length && s.push({
        type: R.RemoveNode,
        removeIds: o
      }), s.push({
        type: R.Select,
        selectIds: [],
        willSyncService: t
      }), yield this.blurSelector(), e && s.push({
        type: R.None,
        needUndoTicker: e
      }), s.length && this.thread.post({ sp: s });
    });
  }
  removeWork(i) {
    const { workId: t } = i, e = t == null ? void 0 : t.toString();
    e && this.removeNode(e);
  }
  removeNode(i) {
    var s;
    const t = this.vNodes.get(i);
    t && (t.toolsType === T.BackgroundSVG && this.thread.post({
      sp: [
        {
          type: R.BackgroundSVGDelete,
          toolsType: T.BackgroundSVG,
          workId: i,
          dataType: q.Local,
          viewId: this.thread.viewId
        }
      ]
    }), (s = this.thread.fullLayer) == null || s.getElementsByName(i).forEach((o) => {
      o.remove(), q2(o, this.thread.fullLayer.parent);
    }), this.vNodes.delete(i)), this.workShapes.has(i) && (this.thread.localLayer.getElementsByName(i).forEach((o) => {
      o.remove(), q2(o, this.thread.localLayer.parent);
    }), this.clearWorkShapeNodeCache(i));
  }
  setFullWork(i) {
    const { workId: t, opt: e, toolsType: s } = i;
    if (t && e && s) {
      const o = t.toString();
      let r;
      return t && this.workShapes.has(o) ? (r = this.workShapes.get(o), r == null || r.setWorkOptions(e)) : r = this.createWorkShapeNode({
        toolsOpt: e,
        toolsType: s,
        workId: o
      }), r ? (this.workShapes.set(o, r), r) : void 0;
    }
  }
  consumeFull(i) {
    return __async(this, null, function* () {
      var s;
      const t = this.setFullWork(i), e = i.ops && $n(i.ops);
      if (t) {
        const o = (s = i.workId) == null ? void 0 : s.toString();
        t.toolsType === T.BackgroundSVG ? t.consumeService({
          isFullWork: true,
          replaceId: o
        }) : t.toolsType === T.Image ? yield t.consumeServiceAsync({
          isFullWork: true,
          replaceId: o,
          worker: this
        }) : t.toolsType === T.Text ? yield t.consumeServiceAsync({
          isFullWork: true,
          replaceId: o,
          boxRect: this.thread.getSceneRect()
        }) : t.consumeService({
          op: e,
          isFullWork: true,
          replaceId: o
        }), i != null && i.updateNodeOpt && t.updataOptService(i.updateNodeOpt);
        const r = [];
        i.workId && this.workShapes.delete(i.workId.toString()), i.willSyncService && r.push({
          opt: i.opt,
          toolsType: i.toolsType,
          type: R.FullWork,
          workId: i.workId,
          ops: i.ops,
          updateNodeOpt: i.updateNodeOpt,
          viewId: this.thread.viewId
        }), i.needUndoTicker && r.push({
          type: R.None,
          needUndoTicker: i.needUndoTicker
        }), r.length && this.thread.post({ sp: r });
      }
    });
  }
  colloctEffectSelectWork(i) {
    return __async(this, null, function* () {
      const t = this.workShapes.get(
        $
      ), { workId: e, msgType: s } = i;
      if (t && e && t.selectIds && t.selectIds.includes(e.toString())) {
        s === R.RemoveNode ? t.selectIds = t.selectIds.filter(
          (o) => o !== e.toString()
        ) : this.effectSelectNodeData.add(i), yield new Promise((o) => {
          setTimeout(() => {
            o(true);
          }, 0);
        }), yield this.runEffectSelectWork(true).then(() => {
          var o;
          (o = this.effectSelectNodeData) == null || o.clear();
        });
        return;
      }
      return i;
    });
  }
  runEffectSelectWork(i) {
    return __async(this, null, function* () {
      var t;
      for (const e of this.effectSelectNodeData.values()) {
        const s = this.setFullWork(e);
        if (s) {
          const o = (t = e.workId) == null ? void 0 : t.toString();
          if (s.toolsType === T.BackgroundSVG)
            s.consumeService({
              isFullWork: true,
              replaceId: o
            });
          else if (s.toolsType === T.Image)
            yield s.consumeServiceAsync({
              isFullWork: true,
              replaceId: o,
              worker: this
            });
          else if (s.toolsType === T.Text)
            yield s.consumeServiceAsync({
              isFullWork: true,
              replaceId: o,
              boxRect: this.thread.getSceneRect()
            });
          else {
            const r = e.ops && $n(e.ops);
            s.consumeService({
              op: r,
              isFullWork: true,
              replaceId: o
            }), e != null && e.updateNodeOpt && s.updataOptService(e.updateNodeOpt);
          }
          e.workId && this.workShapes.delete(e.workId.toString());
        }
      }
      this.reRenderSelector(i);
    });
  }
  hasSelector() {
    return this.workShapes.has($);
  }
  getSelector() {
    return this.workShapes.get($);
  }
  reRenderSelector(i = false) {
    var s;
    const t = this.workShapes.get(
      $
    );
    if (!t) return;
    if (t && !((s = t.selectIds) != null && s.length))
      return this.blurSelector();
    const e = t.reRenderSelector();
    e && this.thread.post({
      sp: [
        {
          type: R.Select,
          selectIds: t.selectIds,
          selectRect: e,
          willSyncService: i,
          viewId: this.thread.viewId,
          points: t.getChildrenPoints(),
          textOpt: t.textOpt,
          selectorColor: t.selectorColor,
          strokeColor: t.strokeColor,
          fillColor: t.fillColor,
          canTextEdit: t.canTextEdit,
          canRotate: t.canRotate,
          scaleType: t.scaleType,
          opt: t.getWorkOptions() || void 0,
          canLock: t.canLock,
          isLocked: t.isLocked,
          toolsTypes: t.toolsTypes,
          shapeOpt: t.shapeOpt,
          thickness: t.thickness,
          useStroke: t.useStroke,
          strokeType: t.strokeType
        }
      ]
    });
  }
  blurSelector(i) {
    return __async(this, null, function* () {
      var s;
      const t = this.workShapes.get(
        $
      ), e = t == null ? void 0 : t.blurSelector();
      if (this.clearWorkShapeNodeCache($), ((s = this.thread.fullLayer) == null ? void 0 : s.parent).children.forEach((o) => {
        o.name === $ && o.remove();
      }), e) {
        const o = [];
        o.push(__spreadProps(__spreadValues({}, e), {
          isSync: i == null ? void 0 : i.isSync
        })), this.thread.post({ sp: o });
      }
    });
  }
  clearWorkShapeNodeCache(i) {
    var t;
    (t = this.getWorkShape(i)) == null || t.clearTmpPoints(), this.workShapes.delete(i);
  }
  drawBitMapEraserFull(i, t, e, s) {
    return __async(this, null, function* () {
      const { willUpdateNodes: o, willDeleteNodes: r } = t, n = i.getWorkId(), a = [
        {
          type: R.RemoveNode,
          removeIds: [n],
          viewId: this.thread.viewId
        }
      ];
      if (e && a.push({
        type: R.None,
        isLockSentEventCursor: e
      }), o != null && o.size || r != null && r.size) {
        if (o != null && o.size)
          for (const [c, l] of o)
            a.push({
              type: R.UpdateNode,
              dataType: q.Local,
              opt: l.opt,
              workId: c,
              updateNodeOpt: {
                useAnimation: false
              }
            });
        r != null && r.size && a.push({
          type: R.RemoveNode,
          removeIds: [...r],
          viewId: this.thread.viewId
        });
      }
      s && a.push({
        type: R.None,
        needUndoTicker: s
      }), a.length && this.thread.post({ sp: a });
    });
  }
  drawPencilEraserFull(i, t, e, s) {
    const { willNewNodes: o, willDeleteNodes: r } = t, n = i.getWorkId(), a = [
      {
        type: R.RemoveNode,
        removeIds: [n],
        viewId: this.thread.viewId
      }
    ];
    if (e && a.push({
      type: R.None,
      isLockSentEventCursor: e
    }), o != null && o.size || r != null && r.size) {
      if (o != null && o.size)
        for (const [c, l] of o)
          a.push({
            type: R.FullWork,
            dataType: q.Local,
            toolsType: l.toolsType,
            ops: _t(l.op),
            opt: l.opt,
            workId: c,
            updateNodeOpt: {
              useAnimation: false
            }
          });
      r != null && r.size && a.push({
        type: R.RemoveNode,
        removeIds: [...r],
        viewId: this.thread.viewId
      });
    }
    s && a.push({
      type: R.None,
      needUndoTicker: s
    }), a.length && this.thread.post({ sp: a });
  }
  drawEraser(i, t, e) {
    const s = [];
    i.removeIds && s.push(i), t && s.push({
      type: R.None,
      isLockSentEventCursor: t
    }), e && s.push({
      type: R.None,
      needUndoTicker: e
    }), this.thread.post({ sp: s, consumeCount: this.consumeCount });
  }
  getWorkShape(i) {
    return this.workShapes.get(i);
  }
  getWorkShapes() {
    return this.workShapes;
  }
  consumeDraw(i, t) {
    const { op: e, workId: s, scenePath: o, postCount: r, smoothSync: n } = i;
    if (e != null && e.length && s) {
      const a = s.toString(), c = this.workShapes.get(a);
      if (!c)
        return;
      const l = c.toolsType;
      if (l === T.LaserPen)
        return;
      switch (this.combineDrawActiveId && this.combineDrawActiveId !== a && (this.combineTimerId && (clearTimeout(this.combineTimerId), this.combineTimerId = void 0, this.combineDrawResolve && this.combineDrawResolve(false), this.combineDrawActiveId = void 0), this.consumeDrawAll(
        {
          workId: this.combineDrawActiveId,
          scenePath: o,
          viewId: this.thread.viewId,
          msgType: R.DrawWork,
          dataType: q.Local
        },
        t
      )), this.drawWorkActiveId && this.drawWorkActiveId !== a && (this.consumeDrawAll(
        {
          workId: this.drawWorkActiveId,
          scenePath: o,
          viewId: this.thread.viewId,
          msgType: R.DrawWork,
          dataType: q.Local
        },
        t
      ), this.drawWorkActiveId = void 0), !this.drawWorkActiveId && a !== $ && (this.drawWorkActiveId = a), (0, import_lodash.isNumber)(r) && (this.consumeCount = r), l) {
        case T.Selector: {
          const h = c.consume({
            data: i,
            isFullWork: true,
            isMainThread: true
          });
          this.fullWorkerDrawCount++;
          const p2 = [];
          h.type === R.Select && (h.selectIds && t.runReverseSelectWork(h.selectIds), p2.push(h)), this.thread.post({
            consumeCount: this.consumeCount,
            fullWorkerDrawCount: this.fullWorkerDrawCount,
            sp: p2
          });
          break;
        }
        case T.PencilEraser:
        case T.BitMapEraser: {
          c.consume({
            data: i,
            isFullWork: false,
            isMainThread: true
          }), this.fullWorkerDrawCount++, this.thread.post({
            sp: void 0,
            consumeCount: this.consumeCount,
            fullWorkerDrawCount: this.fullWorkerDrawCount
          }), this.combineTimerId || new Promise((h) => {
            this.combineDrawActiveId = a, this.combineDrawResolve = h, this.combineTimerId = ot(
              () => {
                this.combineTimerId = void 0, this.combineDrawResolve && this.combineDrawResolve(true);
              },
              this.thread.master.maxCombineEraserTime,
              this.thread.master.control.hasPolyfillMethod(
                "requestIdleCallback"
              )
            );
          }).then((h) => {
            h && this.drawEraserCombine(a), this.combineDrawResolve = void 0;
          });
          break;
        }
        case T.Eraser:
          {
            const h = c.consume({
              data: i,
              isFullWork: true
            });
            this.drawEraser(h);
          }
          break;
        case T.Arrow:
        case T.Straight:
        case T.Ellipse:
        case T.Rectangle:
        case T.Star:
        case T.Polygon:
        case T.SpeechBalloon:
        case T.Pencil:
          {
            const h = c.consume({
              data: i,
              isFullWork: false,
              isMainThread: true,
              smoothSync: n
            });
            h && (this.fullWorkerDrawCount++, this.thread.post({
              consumeCount: this.consumeCount,
              fullWorkerDrawCount: this.fullWorkerDrawCount,
              sp: h.op && [__spreadProps(__spreadValues({}, h), { scenePath: o })] || void 0
            }));
          }
          break;
      }
    }
  }
  drawEraserCombine(i) {
    var e, s, o, r;
    const t = (e = this.workShapes.get(i)) == null ? void 0 : e.combineConsume({ workerEngine: this });
    if (t) {
      const { willDeleteNodes: n, willNewNodes: a } = t, c = {
        render: [],
        sp: []
      };
      if (t != null && t.rect) {
        const l = xI(t.rect);
        (s = c.render) == null || s.push(
          {
            rect: l,
            isClear: true,
            clearCanvas: nt.Bg,
            viewId: this.thread.viewId
          },
          {
            rect: l,
            drawCanvas: nt.Bg,
            viewId: this.thread.viewId
          }
        );
      }
      if (n != null && n.size && ((o = c.sp) == null || o.push({
        type: R.RemoveNode,
        removeIds: [...n],
        viewId: this.thread.viewId
      })), a != null && a.size)
        for (const [l, h] of a)
          (r = c.sp) == null || r.push({
            type: R.FullWork,
            dataType: q.Local,
            toolsType: h.toolsType,
            ops: _t(h.op),
            opt: h.opt,
            workId: l,
            updateNodeOpt: {
              useAnimation: false
            }
          });
      this.thread.post(c);
    }
  }
  consumeDrawAll(i, t) {
    var r, n, a;
    const { workId: e, scenePath: s, isLockSentEventCursor: o } = i;
    if (e) {
      this.combineTimerId && (clearTimeout(this.combineTimerId), this.combineTimerId = void 0, this.combineDrawResolve && this.combineDrawResolve(false), this.combineDrawActiveId = void 0);
      const c = e.toString();
      this.drawWorkActiveId === c && (this.drawWorkActiveId = void 0);
      const l = this.workShapes.get(c);
      if (!l)
        return;
      const h = l.toolsType;
      if (h === T.LaserPen)
        return;
      const p2 = this.workShapes.get(NI), d = (r = p2 == null ? void 0 : p2.selectIds) == null ? void 0 : r[0], u = l.consumeAll({ data: i, workerEngine: this });
      switch (h) {
        case T.Selector:
          {
            u.selectIds && d && ((n = u.selectIds) != null && n.includes(d)) && p2.cursorBlur();
            const f = [];
            o && f.push({
              type: R.None,
              isLockSentEventCursor: o
            }), u.type === R.Select && (u.selectIds && t.runReverseSelectWork(u.selectIds), f.push(__spreadProps(__spreadValues({}, u), { scenePath: s }))), f.length && this.thread.post({ sp: f }), (a = l.selectIds) != null && a.length ? l.clearTmpPoints() : this.clearWorkShapeNodeCache(c);
          }
          break;
        case T.PencilEraser:
          this.drawPencilEraserFull(
            l,
            u,
            o,
            i.needUndoTicker
          ), this.fullWorkerDrawCount = 0, this.clearWorkShapeNodeCache(c);
          break;
        case T.BitMapEraser:
          this.drawBitMapEraserFull(
            l,
            u,
            o,
            i.needUndoTicker
          ), this.fullWorkerDrawCount = 0, this.clearWorkShapeNodeCache(c);
          break;
        case T.Eraser:
          this.drawEraser(
            __spreadProps(__spreadValues({}, u), { scenePath: s }),
            o,
            i.needUndoTicker
          ), l.clearTmpPoints();
          break;
        case T.Arrow:
        case T.Straight:
        case T.Ellipse:
        case T.Rectangle:
        case T.Star:
        case T.Polygon:
        case T.SpeechBalloon:
        case T.Pencil: {
          const f = [];
          o && f.push({
            type: R.None,
            isLockSentEventCursor: o
          }), u && (f.push(u), this.fullWorkerDrawCount = 0, i.needUndoTicker && f.push({
            type: R.None,
            needUndoTicker: i.needUndoTicker
          }), this.thread.post({
            fullWorkerDrawCount: this.fullWorkerDrawCount,
            sp: f
          })), this.clearWorkShapeNodeCache(c);
          break;
        }
      }
    }
  }
  getToolsOpt() {
    return this.tmpOpt;
  }
  setToolsOpt(i) {
    var t;
    this.tmpOpt = i, (t = i.toolsOpt) != null && t.syncUnitTime && (this.syncUnitTime = i.toolsOpt.syncUnitTime);
  }
  setWorkOptions(i, t) {
    let e = this.workShapes.get(i);
    if (!e && this.tmpOpt) {
      const { toolsType: s } = this.tmpOpt;
      this.tmpOpt.toolsOpt = t, e = this.createWorkShapeNode({ workId: i, toolsType: s, toolsOpt: t }), e && this.workShapes.set(i, e), this.setToolsOpt(this.tmpOpt);
    }
    t.syncUnitTime || (t.syncUnitTime = this.syncUnitTime), e == null || e.setWorkOptions(t);
  }
  createWorkShapeNode(i) {
    return Lt(
      __spreadProps(__spreadValues({}, i), {
        vNodes: this.vNodes,
        fullLayer: this.thread.fullLayer,
        drawLayer: this.thread.localLayer
      }),
      this.thread.serviceWork
    );
  }
};
var ao = class {
  constructor(i) {
    y(this, "vNodes");
    y(this, "thread");
    y(this, "workShapes", /* @__PURE__ */ new Map());
    y(this, "selectorWorkShapes", /* @__PURE__ */ new Map());
    y(this, "willRunEffectSelectorIds", /* @__PURE__ */ new Set());
    y(this, "runEffectId");
    y(this, "animationId");
    y(this, "syncUnitTime", Se.syncOpt.interval);
    this.vNodes = i.vNodes, this.thread = i.thread;
  }
  loadImageBitMap(i) {
    return __async(this, null, function* () {
      return yield this.thread.loadImageBitMap(i);
    });
  }
  destroy() {
    this.clearAll();
  }
  clearAll() {
    this.thread.serviceLayer.children.length && (this.thread.serviceLayer.parent.children.forEach((i) => {
      i.name !== "viewport" && i.remove();
    }), Q(
      this.thread.serviceLayer,
      this.thread.serviceLayer.parent
    )), this.workShapes.clear(), this.selectorWorkShapes.clear(), this.willRunEffectSelectorIds.clear();
  }
  runEffect() {
    this.runEffectId || (this.runEffectId = setTimeout(
      this.effectRunSelector.bind(this),
      0
    ));
  }
  effectRunSelector() {
    this.runEffectId = void 0, this.willRunEffectSelectorIds.forEach((i) => {
      var e, s;
      const t = this.selectorWorkShapes.get(i);
      t && t.selectIds && ((e = t.node) == null || e.selectServiceNode(
        i,
        t,
        true
      )), (s = t == null ? void 0 : t.selectIds) != null && s.length || this.selectorWorkShapes.delete(i);
    }), this.willRunEffectSelectorIds.clear();
  }
  runSelectWork(i) {
    this.activeSelectorShape(i);
    const { workId: t } = i, e = t == null ? void 0 : t.toString();
    e && this.willRunEffectSelectorIds.add(e), this.runEffect();
  }
  removeWork(i) {
    const { workId: t } = i, e = t == null ? void 0 : t.toString();
    if (e) {
      if (this.workShapes.get(e)) {
        this.workShapes.delete(e), this.removeNode(e, i);
        return;
      }
      this.removeNode(e, i);
    }
  }
  consumeFull(i) {
    this.activeWorkShape(i), this.runAnimation();
  }
  runReverseSelectWork(i) {
    i.forEach((t) => {
      this.selectorWorkShapes.forEach((e, s) => {
        var o;
        if ((o = e.selectIds) != null && o.length) {
          const r = e.selectIds.indexOf(t);
          r > -1 && (e.selectIds.splice(r, 1), this.willRunEffectSelectorIds.add(s));
        }
      });
    }), this.willRunEffectSelectorIds.size && this.runEffect();
  }
  consumeDraw(i) {
    this.activeWorkShape(i), this.runAnimation();
  }
  computNextAnimationIndex(i, t) {
    const e = Math.floor(
      (i.animationWorkData || []).slice(i.animationIndex).length * 32 / t / this.syncUnitTime
    ) * t;
    return Math.min(
      (i.animationIndex || 0) + (e || t),
      (i.animationWorkData || []).length
    );
  }
  animationDraw() {
    return __async(this, null, function* () {
      var e, s, o, r, n, a, c, l, h, p2, d, u, f, m, g, I, S, v, L, C, N;
      this.animationId = void 0;
      let i = false;
      const t = /* @__PURE__ */ new Map();
      for (const [R2, P] of this.workShapes.entries())
        switch (P.toolsType) {
          case T.BackgroundSVG: {
            (e = P.node) == null || e.consumeService({
              isFullWork: true,
              replaceId: R2
            }), this.selectorWorkShapes.forEach((M, D) => {
              var B;
              (B = M.selectIds) != null && B.includes(R2) && (this.willRunEffectSelectorIds.add(D), this.runEffect());
            }), this.workShapes.delete(R2);
            break;
          }
          case T.Image: {
            yield (s = P.node) == null ? void 0 : s.consumeServiceAsync({
              isFullWork: true,
              worker: this
            }), this.selectorWorkShapes.forEach((M, D) => {
              var B;
              (B = M.selectIds) != null && B.includes(R2) && (this.willRunEffectSelectorIds.add(D), this.runEffect());
            }), this.workShapes.delete(R2);
            break;
          }
          case T.Text: {
            P.node && (yield (o = P.node) == null ? void 0 : o.consumeServiceAsync({
              isFullWork: true,
              replaceId: R2,
              boxRect: this.thread.getSceneRect()
            }), this.selectorWorkShapes.forEach((M, D) => {
              var B;
              (B = M.selectIds) != null && B.includes(R2) && (this.willRunEffectSelectorIds.add(D), this.runEffect());
            }), (r = P.node) == null || r.clearTmpPoints(), this.workShapes.delete(R2));
            break;
          }
          case T.Arrow:
          case T.Straight:
          case T.Rectangle:
          case T.Ellipse:
          case T.Star:
          case T.Polygon:
          case T.SpeechBalloon: {
            const M = !!P.ops;
            if ((n = P.animationWorkData) != null && n.length) {
              const D = P.oldRect;
              (a = P.node) == null || a.consumeService({
                op: P.animationWorkData,
                isFullWork: M
              }), M && (this.selectorWorkShapes.forEach((B, F) => {
                var U2;
                (U2 = B.selectIds) != null && U2.includes(R2) && (this.willRunEffectSelectorIds.add(F), this.runEffect());
              }), (c = P.node) == null || c.clearTmpPoints(), this.workShapes.delete(R2)), P.isEnableCursor ? t.set(R2, {
                workState: D ? P.ops ? x.Done : x.Doing : x.Start,
                op: P.animationWorkData.slice(-3, -1)
              }) : M && !P.useAnimation && ((l = P.updateNodeOpt) != null && l.useAnimation) && t.set(R2, {
                workState: x.Done,
                op: P.animationWorkData.slice(-3, -1),
                uid: (h = P.updateNodeOpt) == null ? void 0 : h.uid
              }), P.animationWorkData.length = 0;
            }
            break;
          }
          case T.Pencil: {
            if (P.useAnimation) {
              if (P.useAnimation) {
                if (P.isDel) {
                  (I = P.node) == null || I.clearTmpPoints(), this.workShapes.delete(R2);
                  break;
                }
                const M = 3, D = this.computNextAnimationIndex(
                  P,
                  M
                ), B = P.isDiff ? 0 : Math.max(0, (P.animationIndex || 0) - M), F = (P.animationWorkData || []).slice(
                  B,
                  D
                ), U2 = (v = (S = P.node) == null ? void 0 : S.getWorkId()) == null ? void 0 : v.toString();
                if ((P.animationIndex || 0) < D || P.isDiff) {
                  if ((L = P.node) == null || L.consumeService({
                    op: F,
                    isFullWork: false
                  }), P.animationIndex = D, P.isDiff && (P.isDiff = false), F.length && P.isEnableCursor) {
                    const V = F.slice(-3, -1);
                    t.set(R2, {
                      workState: B === 0 ? x.Start : D === ((C = P.animationWorkData) == null ? void 0 : C.length) ? x.Done : x.Doing,
                      op: V
                    });
                  }
                } else P.ops && ((N = P.node) == null || N.consumeService({
                  op: P.animationWorkData || [],
                  isFullWork: true,
                  replaceId: U2
                }), P.isDel = true, P.isEnableCursor && t.set(R2, {
                  workState: x.Done,
                  op: F.slice(-3, -1)
                }));
                i = true;
                break;
              }
            } else {
              const M = !!P.ops;
              if ((p2 = P.node) == null || p2.consumeService({
                op: P.animationWorkData || [],
                isFullWork: M,
                replaceId: R2
              }), (d = P.node) == null || d.updataOptService(P.updateNodeOpt), M) {
                if (!P.isEnableCursor && ((u = P.updateNodeOpt) != null && u.useAnimation) && ((f = P.animationWorkData) != null && f.length)) {
                  const D = P.animationWorkData.slice(-3, -1);
                  t.set(R2, {
                    workState: x.Done,
                    op: D,
                    uid: (m = P.updateNodeOpt) == null ? void 0 : m.uid
                  });
                }
                this.selectorWorkShapes.forEach((D, B) => {
                  var F;
                  (F = D.selectIds) != null && F.includes(R2) && (this.willRunEffectSelectorIds.add(B), this.runEffect());
                }), (g = P.node) == null || g.clearTmpPoints(), this.workShapes.delete(R2);
              }
            }
            break;
          }
        }
      if (i && this.runAnimation(), t.size) {
        const R2 = [];
        t.forEach((P, M) => {
          R2.push({
            type: R.Cursor,
            uid: P.uid || M.split(st)[0],
            op: P.op,
            workState: P.workState,
            viewId: this.thread.viewId
          });
        }), this.thread.post({ sp: R2 });
      }
    });
  }
  runAnimation() {
    this.animationId || (this.animationId = requestAnimationFrame(this.animationDraw.bind(this)));
  }
  hasDiffData(i, t, e) {
    const s = i.length;
    if (t.length < s)
      return true;
    switch (e) {
      case T.Pencil: {
        for (let o = 0; o < s; o += 3)
          if (t[o] !== i[o] || t[o + 1] !== i[o + 1])
            return true;
        break;
      }
      case T.LaserPen: {
        for (let o = 0; o < s; o += 2)
          if (t[o] !== i[o] || t[o + 1] !== i[o + 1])
            return true;
        break;
      }
    }
    return false;
  }
  activeWorkShape(i) {
    var f, m, g, I;
    const {
      workId: t,
      opt: e,
      toolsType: s,
      type: o,
      updateNodeOpt: r,
      ops: n,
      op: a,
      useAnimation: c,
      imageBitmap: l,
      isEnableCursor: h
    } = i;
    if (!t)
      return;
    const p2 = t.toString(), d = (f = this.vNodes.get(p2)) == null ? void 0 : f.rect;
    if (!((m = this.workShapes) != null && m.has(p2))) {
      let S = {
        toolsType: s,
        animationWorkData: a || [],
        animationIndex: 0,
        type: o,
        updateNodeOpt: r,
        ops: n,
        useAnimation: typeof c < "u" ? c : typeof (r == null ? void 0 : r.useAnimation) < "u" ? r == null ? void 0 : r.useAnimation : true,
        oldRect: d,
        isDiff: false,
        imageBitmap: l,
        isEnableCursor: h
      };
      s && e && (S = this.setNodeKey(p2, S, s, e)), (g = this.workShapes) == null || g.set(p2, S);
    }
    const u = (I = this.workShapes) == null ? void 0 : I.get(p2);
    u.isEnableCursor = h, o && (u.type = o), n && (u.animationWorkData = $n(n), u.ops = n), r && (u.updateNodeOpt = r), a && (u.isDiff = this.hasDiffData(
      u.animationWorkData || [],
      a,
      u.toolsType
    ), u.animationWorkData = a), u.node && u.node.getWorkId() !== p2 && u.node.setWorkId(p2), d && (u.oldRect = d), s && e && (e.syncUnitTime && (this.syncUnitTime = e.syncUnitTime), u.toolsType !== s && s && e && this.setNodeKey(p2, u, s, e), u.node && u.node.setWorkOptions(e)), l && (u.imageBitmap = l);
  }
  removeNode(i, t) {
    i.indexOf($) > -1 && this.removeSelectWork(t), this.thread.fullLayer.getElementsByName(i).forEach((s) => {
      s.remove(), q2(s, this.thread.fullLayer.parent);
    }), this.thread.serviceLayer.getElementsByName(i).forEach((s) => {
      s.remove(), q2(s, this.thread.serviceLayer.parent);
    });
    const e = this.vNodes.get(i);
    e && e.toolsType === T.BackgroundSVG && this.thread.post({
      sp: [
        {
          type: R.BackgroundSVGDelete,
          toolsType: T.BackgroundSVG,
          workId: i,
          dataType: q.Service,
          viewId: this.thread.viewId
        }
      ]
    }), this.vNodes.delete(i);
  }
  removeSelectWork(i) {
    const { workId: t } = i, e = t == null ? void 0 : t.toString();
    e && (this.activeSelectorShape(i), this.willRunEffectSelectorIds.add(e)), this.runEffect();
  }
  activeSelectorShape(i) {
    var c, l, h;
    const { workId: t, opt: e, toolsType: s, type: o, selectIds: r } = i;
    if (!t)
      return;
    const n = t.toString();
    if (!((c = this.selectorWorkShapes) != null && c.has(n))) {
      let p2 = {
        toolsType: s,
        selectIds: r,
        type: o,
        opt: e
      };
      s && e && (p2 = this.setNodeKey(n, p2, s, e)), (l = this.selectorWorkShapes) == null || l.set(n, p2);
    }
    const a = (h = this.selectorWorkShapes) == null ? void 0 : h.get(n);
    o && (a.type = o), a.node && a.node.getWorkId() !== n && a.node.setWorkId(n), a.selectIds = r || [];
  }
  setNodeKey(i, t, e, s) {
    return t.toolsType = e, t.node = Lt(
      {
        toolsType: e,
        toolsOpt: s,
        vNodes: this.vNodes,
        fullLayer: this.thread.fullLayer,
        drawLayer: this.thread.serviceLayer,
        workId: i
      },
      this
    ), t;
  }
};
var rt = class {
  constructor() {
    y(this, "localWork");
    y(this, "serviceWork");
    y(this, "threadEngine");
  }
  registerMainThread(i) {
    return this.threadEngine = i, this.localWork = i.localWork, this.serviceWork = i.serviceWork, this;
  }
};
var co = class extends rt {
  constructor() {
    super(...arguments);
    y(this, "emitEventType", W.CopyNode);
  }
  consume(t) {
    return __async(this, null, function* () {
      const { msgType: e, dataType: s, emitEventType: o } = t;
      if (e === R.FullWork && s === q.Local && o === this.emitEventType)
        return this.consumeForLocalWorker(t), true;
    });
  }
  consumeForLocalWorker(t) {
    return __async(this, null, function* () {
      var s;
      const { workId: e } = t;
      e && (yield (s = this.localWork) == null ? void 0 : s.consumeFull(t));
    });
  }
};
var lo = class extends rt {
  constructor() {
    super(...arguments);
    y(this, "emitEventType", W.SetColorNode);
  }
  consume(t) {
    return __async(this, null, function* () {
      const { msgType: e, dataType: s, emitEventType: o } = t;
      if (e === R.UpdateNode && s === q.Local && o === this.emitEventType)
        return this.consumeForLocalWorker(t), true;
    });
  }
  consumeForLocalWorker(t) {
    return __async(this, null, function* () {
      var l;
      const {
        workId: e,
        updateNodeOpt: s,
        willRefreshSelector: o,
        willSyncService: r,
        willSerializeData: n,
        textUpdateForWoker: a,
        needUndoTicker: c
      } = t;
      e === $ && s && (yield (l = this.localWork) == null ? void 0 : l.updateSelector({
        updateSelectorOpt: s,
        willRefreshSelector: o,
        willSyncService: r,
        willSerializeData: n,
        textUpdateForWoker: a,
        callback: this.updateSelectorCallback,
        needUndoTicker: c
      }));
    });
  }
  updateSelectorCallback(t) {
    const { param: e, postData: s, newServiceStore: o } = t, { willSyncService: r, isSync: n, textUpdateForWoker: a, needUndoTicker: c } = e, l = s.sp || [];
    if (r)
      for (const [h, p2] of o.entries())
        a && p2.toolsType === T.Text ? l.push(__spreadProps(__spreadValues({}, p2), {
          workId: h,
          type: R.TextUpdate,
          dataType: q.Local,
          willSyncService: true
        })) : l.push(__spreadProps(__spreadValues({}, p2), {
          workId: h,
          type: R.UpdateNode,
          updateNodeOpt: {
            useAnimation: false
          },
          isSync: n
        }));
    return c && l.push({
      type: R.None,
      needUndoTicker: c
    }), {
      sp: l
    };
  }
};
var ho = class extends rt {
  constructor() {
    super(...arguments);
    y(this, "emitEventType", W.ZIndexNode);
  }
  consume(t) {
    return __async(this, null, function* () {
      const { msgType: e, dataType: s, emitEventType: o } = t;
      if (e === R.UpdateNode && s === q.Local && o === this.emitEventType)
        return this.consumeForLocalWorker(t), true;
    });
  }
  consumeForLocalWorker(t) {
    return __async(this, null, function* () {
      var a;
      const {
        workId: e,
        updateNodeOpt: s,
        willRefreshSelector: o,
        willSyncService: r,
        willSerializeData: n
      } = t;
      e === $ && s && (yield (a = this.localWork) == null ? void 0 : a.updateSelector({
        updateSelectorOpt: s,
        willRefreshSelector: o,
        willSyncService: r,
        willSerializeData: n,
        callback: this.updateSelectorCallback
      }));
    });
  }
  updateSelectorCallback(t) {
    const { param: e, postData: s, newServiceStore: o } = t, { willSyncService: r, isSync: n } = e, a = s.sp || [];
    if (r && a)
      for (const [c, l] of o.entries())
        l.toolsType === T.BackgroundSVG && a.push(__spreadProps(__spreadValues({}, l), {
          workId: c,
          type: R.BackgroundSVGUpdate,
          dataType: q.Local,
          updateNodeOpt: {
            useAnimation: false
          }
        })), a.push(__spreadProps(__spreadValues({}, l), {
          workId: c,
          type: R.UpdateNode,
          updateNodeOpt: {
            useAnimation: false
          },
          isSync: n
        }));
    return {
      sp: a
    };
  }
};
var po = class extends rt {
  constructor() {
    super(...arguments);
    y(this, "emitEventType", W.TranslateNode);
  }
  consume(t) {
    return __async(this, null, function* () {
      const { msgType: e, dataType: s, emitEventType: o } = t;
      if (e === R.UpdateNode && s === q.Local && o === this.emitEventType)
        return yield this.consumeForLocalWorker(t), true;
    });
  }
  consumeForLocalWorker(t) {
    return __async(this, null, function* () {
      var p2, d;
      const {
        workId: e,
        updateNodeOpt: s,
        willRefreshSelector: o,
        willSyncService: r,
        willSerializeData: n,
        textUpdateForWoker: a,
        emitEventType: c,
        smoothSync: l,
        needUndoTicker: h
      } = t;
      e === $ && s && (s.workState === x.Done && (s != null && s.translate) && (s.translate[0] || s.translate[1]) || s.workState !== x.Done ? yield (p2 = this.localWork) == null ? void 0 : p2.updateSelector({
        updateSelectorOpt: s,
        willRefreshSelector: o,
        willSyncService: r,
        willSerializeData: n,
        isSync: true,
        textUpdateForWoker: a,
        emitEventType: c,
        callback: this.updateSelectorCallback,
        smoothSync: l,
        needUndoTicker: h
      }) : s.workState === x.Done && ((d = this.localWork) == null || d.vNodes.deleteLastTarget()));
    });
  }
  updateSelectorCallback(t) {
    const { param: e, postData: s, newServiceStore: o, workShapeNode: r, res: n, smoothSync: a } = t, {
      willSyncService: c,
      isSync: l,
      updateSelectorOpt: h,
      textUpdateForWoker: p2,
      needUndoTicker: d
    } = e, u = h.workState, f = s.sp || [];
    if (u === x.Start)
      return {
        sp: [],
        render: []
      };
    const m = n == null ? void 0 : n.selectRect;
    if (c) {
      f.push({
        type: R.Select,
        selectIds: r.selectIds,
        selectRect: m,
        willSyncService: u === x.Done ? true : a,
        isSync: true,
        points: u === x.Done && r.getChildrenPoints() || void 0,
        textOpt: r.textOpt
      });
      const g = {
        useAnimation: h.useAnimation || false
      };
      h.uid && (g.uid = h.uid);
      for (const [I, S] of o.entries())
        S.toolsType === T.BackgroundSVG && f.push(__spreadProps(__spreadValues({}, S), {
          workId: I,
          type: R.BackgroundSVGUpdate,
          dataType: q.Local,
          willSyncService: u === x.Done ? true : a,
          updateNodeOpt: g
        })), p2 && S.toolsType === T.Text ? f.push(__spreadProps(__spreadValues({}, S), {
          workId: I,
          type: R.TextUpdate,
          dataType: q.Local,
          willSyncService: u === x.Done ? true : a,
          updateNodeOpt: g
        })) : (a || u === x.Done) && f.push(__spreadProps(__spreadValues({}, S), {
          workId: I,
          type: R.UpdateNode,
          updateNodeOpt: g,
          isSync: l
        }));
    }
    return d && f.push({
      type: R.None,
      needUndoTicker: d
    }), {
      sp: f
    };
  }
};
var uo = class extends rt {
  constructor() {
    super(...arguments);
    y(this, "emitEventType", W.ScaleNode);
  }
  consume(t) {
    return __async(this, null, function* () {
      const { msgType: e, dataType: s, emitEventType: o } = t;
      if (e === R.UpdateNode && s === q.Local && o === this.emitEventType)
        return yield this.consumeForLocalWorker(t), true;
    });
  }
  consumeForLocalWorker(t) {
    return __async(this, null, function* () {
      var c;
      const {
        workId: e,
        updateNodeOpt: s,
        willSyncService: o,
        willSerializeData: r,
        smoothSync: n,
        needUndoTicker: a
      } = t;
      e === $ && s && (yield (c = this.localWork) == null ? void 0 : c.updateSelector({
        updateSelectorOpt: s,
        willSyncService: o,
        willSerializeData: r,
        isSync: true,
        callback: this.updateSelectorCallback.bind(this),
        smoothSync: n,
        needUndoTicker: a
      }));
    });
  }
  updateSelectorCallback(t) {
    const { param: e, postData: s, workShapeNode: o, res: r, newServiceStore: n, smoothSync: a } = t, { updateSelectorOpt: c, willSyncService: l, needUndoTicker: h } = e, p2 = c.workState, d = s.sp || [], u = r == null ? void 0 : r.selectRect;
    if (p2 === x.Start)
      return {
        sp: [],
        render: []
      };
    if (l) {
      d.push({
        type: R.Select,
        selectIds: o.selectIds,
        selectRect: u,
        willSyncService: p2 === x.Done ? true : a,
        isSync: true,
        points: p2 === x.Done && o.getChildrenPoints() || void 0,
        textOpt: o.textOpt
      });
      const f = {
        useAnimation: c.useAnimation || false
      };
      c.uid && (f.uid = c.uid);
      for (const [m, g] of n.entries())
        g.toolsType === T.BackgroundSVG && d.push(__spreadProps(__spreadValues({}, g), {
          workId: m,
          type: R.BackgroundSVGUpdate,
          dataType: q.Local,
          willSyncService: p2 === x.Done ? true : a,
          updateNodeOpt: f
        })), g.toolsType === T.Text ? d.push(__spreadProps(__spreadValues({}, g), {
          workId: m,
          type: R.TextUpdate,
          dataType: q.Local,
          willSyncService: p2 === x.Done ? true : a,
          updateNodeOpt: f
        })) : (a || p2 === x.Done) && d.push(__spreadProps(__spreadValues({}, g), {
          workId: m,
          type: R.UpdateNode,
          updateNodeOpt: f,
          isSync: true
        }));
    }
    return h && d.push({
      type: R.None,
      needUndoTicker: h
    }), {
      sp: d
    };
  }
};
var fo = class extends rt {
  constructor() {
    super(...arguments);
    y(this, "emitEventType", W.RotateNode);
  }
  consume(t) {
    return __async(this, null, function* () {
      const { msgType: e, dataType: s, emitEventType: o } = t;
      if (e === R.UpdateNode && s === q.Local && o === this.emitEventType)
        return yield this.consumeForLocalWorker(t), true;
    });
  }
  consumeForLocalWorker(t) {
    return __async(this, null, function* () {
      var h;
      const {
        workId: e,
        updateNodeOpt: s,
        willRefreshSelector: o,
        willSyncService: r,
        willSerializeData: n,
        emitEventType: a,
        smoothSync: c,
        needUndoTicker: l
      } = t;
      e === $ && s && (yield (h = this.localWork) == null ? void 0 : h.updateSelector({
        updateSelectorOpt: s,
        willRefreshSelector: o,
        willSyncService: r,
        willSerializeData: n,
        emitEventType: a,
        isSync: true,
        callback: this.updateSelectorCallback,
        smoothSync: c,
        needUndoTicker: l
      }));
    });
  }
  updateSelectorCallback(t) {
    const { param: e, postData: s, workShapeNode: o, res: r, newServiceStore: n, smoothSync: a } = t, {
      updateSelectorOpt: c,
      willSyncService: l,
      willSerializeData: h,
      isSync: p2,
      needUndoTicker: d
    } = e, u = c.workState, f = s.sp || [], m = r == null ? void 0 : r.selectRect;
    if (l) {
      h && u === x.Done && f.push({
        type: R.Select,
        selectIds: o.selectIds,
        selectRect: m,
        willSyncService: u === x.Done ? true : a,
        isSync: p2,
        points: o.getChildrenPoints()
      });
      const g = {
        useAnimation: c.useAnimation || false
      };
      if (c.uid && (g.uid = c.uid), a || u === x.Done)
        for (const [I, S] of n.entries())
          f.push(__spreadProps(__spreadValues({}, S), {
            workId: I,
            type: R.UpdateNode,
            updateNodeOpt: g,
            isSync: p2
          }));
    }
    return d && f.push({
      type: R.None,
      needUndoTicker: d
    }), {
      sp: f
    };
  }
};
var mo = class extends rt {
  constructor() {
    super(...arguments);
    y(this, "emitEventType", W.SetFontStyle);
  }
  consume(t) {
    return __async(this, null, function* () {
      const { msgType: e, dataType: s, emitEventType: o } = t;
      if (e === R.UpdateNode && s === q.Local && o === this.emitEventType)
        return yield this.consumeForLocalWorker(t), true;
    });
  }
  consumeForLocalWorker(t) {
    return __async(this, null, function* () {
      var c;
      const {
        workId: e,
        updateNodeOpt: s,
        willRefreshSelector: o,
        willSyncService: r,
        willSerializeData: n,
        textUpdateForWoker: a
      } = t;
      e === $ && s && (yield (c = this.localWork) == null ? void 0 : c.updateSelector({
        updateSelectorOpt: s,
        willRefreshSelector: o,
        willSyncService: r,
        willSerializeData: n,
        textUpdateForWoker: a,
        callback: this.updateSelectorCallback
      }));
    });
  }
  updateSelectorCallback(t) {
    const { param: e, postData: s, newServiceStore: o, workShapeNode: r, res: n } = t, { willSyncService: a, isSync: c, updateSelectorOpt: l, textUpdateForWoker: h } = e, p2 = s.sp || [], d = n == null ? void 0 : n.selectRect;
    if (a && p2) {
      l.fontSize && p2.push({
        type: R.Select,
        selectIds: r.selectIds,
        selectRect: d,
        willSyncService: a,
        isSync: c,
        points: r.getChildrenPoints()
      });
      for (const [u, f] of o.entries())
        h && f.toolsType === T.Text ? p2.push(__spreadProps(__spreadValues({}, f), {
          workId: u,
          type: R.TextUpdate,
          dataType: q.Local,
          willSyncService: true
        })) : p2.push(__spreadProps(__spreadValues({}, f), {
          workId: u,
          type: R.UpdateNode,
          updateNodeOpt: {
            useAnimation: false
          },
          isSync: c
        }));
    }
    return {
      sp: p2
    };
  }
};
var yo = class extends rt {
  constructor() {
    super(...arguments);
    y(this, "emitEventType", W.SetPoint);
  }
  consume(t) {
    return __async(this, null, function* () {
      const { msgType: e, dataType: s, emitEventType: o } = t;
      if (e === R.UpdateNode && s === q.Local && o === this.emitEventType)
        return this.consumeForLocalWorker(t), true;
    });
  }
  consumeForLocalWorker(t) {
    return __async(this, null, function* () {
      var l;
      const {
        workId: e,
        updateNodeOpt: s,
        willRefreshSelector: o,
        willSyncService: r,
        willSerializeData: n,
        textUpdateForWoker: a,
        needUndoTicker: c
      } = t;
      e === $ && s && (yield (l = this.localWork) == null ? void 0 : l.updateSelector({
        updateSelectorOpt: s,
        willRefreshSelector: o,
        willSyncService: r,
        emitEventType: this.emitEventType,
        willSerializeData: n,
        isSync: true,
        textUpdateForWoker: a,
        callback: this.updateSelectorCallback,
        needUndoTicker: c
      }));
    });
  }
  updateSelectorCallback(t) {
    const { param: e, postData: s, newServiceStore: o, workShapeNode: r, res: n } = t, { willSyncService: a, isSync: c, needUndoTicker: l } = e, h = s.sp || [], p2 = n == null ? void 0 : n.selectRect;
    if (a && h) {
      for (const [d, u] of o.entries())
        h.push(__spreadProps(__spreadValues({}, u), {
          workId: d,
          type: R.UpdateNode,
          updateNodeOpt: {
            useAnimation: false
          },
          isSync: c
        }));
      h.push({
        type: R.Select,
        selectIds: r.selectIds,
        selectRect: p2,
        willSyncService: a,
        isSync: c,
        points: r.getChildrenPoints()
      });
    }
    return l && h.push({
      type: R.None,
      needUndoTicker: l
    }), {
      sp: h
    };
  }
};
var wo = class extends rt {
  constructor() {
    super(...arguments);
    y(this, "emitEventType", W.SetLock);
  }
  consume(t) {
    return __async(this, null, function* () {
      const { msgType: e, dataType: s, emitEventType: o } = t;
      if (e === R.UpdateNode && s === q.Local && o === this.emitEventType)
        return this.consumeForLocalWorker(t), true;
    });
  }
  consumeForLocalWorker(t) {
    return __async(this, null, function* () {
      var c;
      const {
        workId: e,
        updateNodeOpt: s,
        willRefreshSelector: o,
        willSyncService: r,
        willSerializeData: n,
        needUndoTicker: a
      } = t;
      e === $ && s && (yield (c = this.localWork) == null ? void 0 : c.updateSelector({
        updateSelectorOpt: s,
        willRefreshSelector: o,
        willSyncService: r,
        willSerializeData: n,
        callback: this.updateSelectorCallback,
        needUndoTicker: a
      }));
    });
  }
  updateSelectorCallback(t) {
    const { param: e, postData: s, newServiceStore: o, workShapeNode: r, res: n } = t, { willSyncService: a, isSync: c, updateSelectorOpt: l, needUndoTicker: h } = e, p2 = s.sp || [], d = n == null ? void 0 : n.selectRect;
    if (a && p2) {
      for (const [u, f] of o.entries())
        p2.push(__spreadProps(__spreadValues({}, f), {
          workId: u,
          type: R.UpdateNode,
          updateNodeOpt: {
            useAnimation: false
          },
          isSync: c
        }));
      p2.push({
        isLocked: l.isLocked,
        selectorColor: r.selectorColor,
        scaleType: r.scaleType,
        canRotate: r.canRotate,
        type: R.Select,
        selectIds: r.selectIds,
        selectRect: d,
        willSyncService: a,
        isSync: c
      });
    }
    return h && p2.push({
      type: R.None,
      needUndoTicker: h
    }), {
      sp: p2
    };
  }
};
var go = class extends rt {
  constructor() {
    super(...arguments);
    y(this, "emitEventType", W.SetShapeOpt);
  }
  consume(t) {
    return __async(this, null, function* () {
      const { msgType: e, dataType: s, emitEventType: o } = t;
      if (e === R.UpdateNode && s === q.Local && o === this.emitEventType)
        return this.consumeForLocalWorker(t), true;
    });
  }
  consumeForLocalWorker(t) {
    return __async(this, null, function* () {
      var c;
      const {
        workId: e,
        updateNodeOpt: s,
        willRefreshSelector: o,
        willSyncService: r,
        willSerializeData: n,
        needUndoTicker: a
      } = t;
      e === $ && s && (yield (c = this.localWork) == null ? void 0 : c.updateSelector({
        updateSelectorOpt: s,
        willRefreshSelector: o,
        willSyncService: r,
        willSerializeData: n,
        callback: this.updateSelectorCallback,
        needUndoTicker: a
      }));
    });
  }
  updateSelectorCallback(t) {
    const { param: e, postData: s, newServiceStore: o } = t, { willSyncService: r, isSync: n, needUndoTicker: a } = e, c = s.sp || [];
    if (r && c)
      for (const [l, h] of o.entries())
        c.push(__spreadProps(__spreadValues({}, h), {
          workId: l,
          type: R.UpdateNode,
          updateNodeOpt: {
            useAnimation: false
          },
          isSync: n
        }));
    return a && c.push({
      type: R.None,
      needUndoTicker: a
    }), {
      sp: c
    };
  }
};
var ko = class {
  constructor(i) {
    y(this, "builders", /* @__PURE__ */ new Map());
    this.builders = new Map(i.map((t) => [t, this.build(t)]));
  }
  build(i) {
    switch (i) {
      case W.TranslateNode:
        return new po();
      case W.ZIndexNode:
        return new ho();
      case W.CopyNode:
        return new co();
      case W.SetColorNode:
        return new lo();
      case W.ScaleNode:
        return new uo();
      case W.RotateNode:
        return new fo();
      case W.SetFontStyle:
        return new mo();
      case W.SetPoint:
        return new yo();
      case W.SetLock:
        return new wo();
      case W.SetShapeOpt:
        return new go();
    }
  }
  registerForMainThread(i) {
    return this.builders.forEach((t) => {
      t && t.registerMainThread(i);
    }), this;
  }
  consumeForMainThread(i) {
    return __async(this, null, function* () {
      for (const t of this.builders.values())
        if (yield t == null ? void 0 : t.consume(i))
          return true;
      return false;
    });
  }
};
var So = class {
  constructor(i, t) {
    y(this, "viewId");
    y(this, "fullLayer");
    y(this, "topLayer");
    y(this, "localLayer");
    y(this, "serviceLayer");
    y(this, "snapshotFullLayer");
    y(this, "vNodes");
    y(this, "master");
    y(this, "opt");
    y(this, "cameraOpt");
    y(this, "scene");
    y(this, "localWork");
    y(this, "serviceWork");
    y(this, "topWork");
    y(this, "taskUpdateCameraId");
    y(this, "debounceUpdateCameraId");
    y(this, "debounceUpdateCache", /* @__PURE__ */ new Set());
    y(this, "mainThreadPostId");
    y(this, "combinePostMsg", /* @__PURE__ */ new Set());
    y(this, "methodBuilder");
    y(this, "cacheImages", /* @__PURE__ */ new Map());
    y(this, "imageResolveMap", /* @__PURE__ */ new Map());
    this.viewId = i, this.opt = t, this.scene = this.createScene(__spreadProps(__spreadValues({}, t.canvasOpt), {
      container: t.container
    })), this.master = t.master;
    const e = Se.bufferSize.full, s = Se.bufferSize.sub;
    this.fullLayer = this.createLayer("fullLayer", this.scene, __spreadProps(__spreadValues({}, t.layerOpt), {
      bufferSize: this.viewId === pe ? e : s * 2
    })), this.topLayer = this.createLayer("topLayer", this.scene, __spreadProps(__spreadValues({}, t.layerOpt), {
      bufferSize: (this.viewId === pe, s),
      contextType: "2d"
    })), this.localLayer = this.createLayer("localLayer", this.scene, __spreadProps(__spreadValues({}, t.layerOpt), {
      bufferSize: (this.viewId === pe, s),
      contextType: "2d"
    })), this.serviceLayer = this.createLayer("serviceLayer", this.scene, __spreadProps(__spreadValues({}, t.layerOpt), {
      bufferSize: (this.viewId === pe, s),
      contextType: "2d"
    })), this.vNodes = new Xe(i, this.scene);
    const o = {
      thread: this,
      vNodes: this.vNodes
    };
    this.localWork = new no(o), this.serviceWork = new ao(o), this.topWork = new ro(o), this.vNodes.init(this.fullLayer), this.methodBuilder = new ko([
      W.CopyNode,
      W.SetColorNode,
      W.DeleteNode,
      W.RotateNode,
      W.ScaleNode,
      W.TranslateNode,
      W.ZIndexNode,
      W.SetFontStyle,
      W.SetPoint,
      W.SetLock,
      W.SetShapeOpt
    ]).registerForMainThread(this);
  }
  getCachedImages(i) {
    var t;
    return (t = this.cacheImages.get(i)) == null ? void 0 : t.imageBitmap;
  }
  getCachedImagesByWorkId(i) {
    for (const [t, e] of this.cacheImages.entries())
      if (t === i && e.imageBitmap)
        return e.imageBitmap;
  }
  deleteCachedImagesByWorkId(i) {
    for (const [t, e] of this.cacheImages.entries())
      e.workId === i && (e.imageBitmap.close(), this.cacheImages.delete(t));
  }
  clearCacheImages() {
    this.cacheImages.forEach((i) => i.imageBitmap.close()), this.cacheImages.clear();
  }
  clearImageResolveMap() {
    this.imageResolveMap.forEach(({ timer: i }) => {
      i && clearTimeout(i);
    }), this.imageResolveMap.clear();
  }
  post(i) {
    this.combinePostMsg.add(i), this.runBatchPostData();
  }
  updateDpr(i) {
    this.scene.displayRatio = i;
  }
  on(i) {
    return __async(this, null, function* () {
      if (!(yield this.methodBuilder.consumeForMainThread(i))) {
        const {
          msgType: t,
          toolsType: e,
          opt: s,
          dataType: o,
          workId: r,
          workState: n,
          imageSrc: a,
          imageBitmap: c,
          workIds: l,
          isLockSentEventCursor: h
        } = i, p2 = r == null ? void 0 : r.toString();
        switch (t) {
          case R.UpdateDpr:
            (0, import_lodash.isNumber)(i.dpr) && this.updateDpr(i.dpr);
            break;
          case R.AuthClear: {
            const { clearUids: d, localUid: u } = i;
            this.vNodes.setCanClearUids(d), this.vNodes.setLocalUid(u);
            break;
          }
          case R.Destroy:
            this.destroy();
            break;
          case R.Clear:
            this.clearAll();
            break;
          case R.UpdateCamera:
            yield this.updateCamera(i);
            break;
          case R.UpdateTools:
            if (e && s) {
              const d = {
                toolsType: e,
                toolsOpt: s
              };
              this.topWork.canUseTopLayer(e) ? this.topWork.setToolsOpt(d) : this.localWork.setToolsOpt(d);
            }
            break;
          case R.CreateWork:
            if (p2 && s && e) {
              if (this.topWork.canUseTopLayer(e)) {
                this.topWork.getToolsOpt() || this.topWork.setToolsOpt({
                  toolsType: e,
                  toolsOpt: s
                }), this.topWork.setWorkOptions(p2, s);
                break;
              }
              this.localWork.getToolsOpt() || this.localWork.setToolsOpt({
                toolsType: e,
                toolsOpt: s
              }), this.localWork.setWorkOptions(p2, s);
            }
            break;
          case R.DrawWork:
            n === x.Done && o === q.Local ? (this.consumeDrawAll(o, i), e === T.LaserPen && h && this.post({
              sp: [
                {
                  type: R.None,
                  isLockSentEventCursor: h
                }
              ]
            })) : this.consumeDraw(o, i);
            break;
          case R.UpdateNode:
          case R.FullWork:
            if (e && this.topWork.canUseTopLayer(e)) {
              this.consumeDrawAll(o, i);
              break;
            }
            this.consumeFull(o, i);
            break;
          case R.RemoveNode:
            yield this.removeNode(i);
            return;
          case R.Select:
            o === q.Service && (r === $ ? this.localWork.updateFullSelectWork(i) : this.serviceWork.runSelectWork(i));
            break;
          case R.CursorBlur:
            this.localWork.cursorBlur();
            return;
          case R.CursorHover:
            this.localWork.cursorHover(i);
            break;
          case R.GetTextActive:
            o === q.Local && this.localWork.checkTextActive(i);
            break;
          case R.GetImageBitMap:
            if (a && c && r) {
              const d = r.toString();
              this.deleteCachedImagesByWorkId(d), this.cacheImages.set(a, {
                imageBitmap: c,
                workId: d
              });
              const u = this.imageResolveMap.get(a);
              if (u) {
                const { resolve: f, timer: m } = u;
                m && clearTimeout(m), f && f(a);
              }
            }
            break;
          case R.GetVNodeInfo:
            if (r && l) {
              const d = l.map((u) => this.vNodes.get(u));
              this.post({
                sp: [
                  {
                    type: R.GetVNodeInfo,
                    dataType: q.Local,
                    workId: r,
                    vInfo: d
                  }
                ]
              });
            }
            break;
        }
      }
    });
  }
  getIconSize(i, t, e) {
    const s = i * e, o = t * e;
    return s <= 50 || o <= 50 ? [50, 50] : s <= 100 || o <= 100 ? [100, 100] : s <= 200 || o <= 200 ? [200, 200] : s <= 400 || o <= 400 ? [400, 400] : s <= 800 || o <= 800 ? [800, 800] : [1600, 1600];
  }
  loadImageBitMap(i) {
    return __async(this, null, function* () {
      const { toolsType: t, opt: e, workId: s } = i;
      if (t === T.Image && e && s) {
        const o = s.toString(), { src: r, type: n, width: a, height: c, strokeColor: l } = e;
        if (!r || !n || !a || !c)
          return;
        let h = r;
        if (n === Zt.Iconify) {
          const [f, m] = this.getIconSize(a, c, this.opt.displayer.dpr);
          h = `${r}?width=${f}&height=${m}&color=${l}`;
        }
        if (this.cacheImages.has(h)) {
          const f = this.getCachedImages(h);
          if (f)
            return f;
        }
        if (this.imageResolveMap.has(h)) {
          const f = this.getCachedImagesByWorkId(o);
          if (f)
            return f;
        }
        const u = yield new Promise((f) => {
          const m = this.imageResolveMap.get(h) || {
            resolve: void 0,
            timer: void 0
          };
          m.timer && clearTimeout(m.timer), m.resolve = f, m.timer = setTimeout(() => {
            const g = this.imageResolveMap.get(h);
            g != null && g.resolve && g.resolve(h);
          }, 5e3), this.imageResolveMap.set(h, m), this.opt.post({
            sp: [
              {
                imageSrc: h,
                workId: o,
                viewId: this.viewId,
                isgl: !!this.fullLayer.parent.gl,
                isSubWorker: false,
                type: R.GetImageBitMap
              }
            ]
          });
        });
        return this.imageResolveMap.delete(u), this.getCachedImages(h);
      }
    });
  }
  removeNode(i) {
    return __async(this, null, function* () {
      const { dataType: t, workId: e, removeIds: s } = i, o = s || [];
      if (e && o.push(e.toString()), o.length)
        for (const r of o) {
          if (r === $) {
            yield this.localWork.removeSelector(i);
            continue;
          }
          t === q.Local ? this.localWork.removeWork(i) : t === q.Service && this.serviceWork.removeWork(i), yield this.localWork.colloctEffectSelectWork(i);
        }
    });
  }
  consumeFull(i, t) {
    return __async(this, null, function* () {
      const e = yield this.localWork.colloctEffectSelectWork(t);
      e && i === q.Local && (yield this.localWork.consumeFull(e)), e && i === q.Service && this.serviceWork.consumeFull(e);
    });
  }
  setCameraOpt(i) {
    this.cameraOpt = i;
    const { scale: t, centerX: e, centerY: s, width: o, height: r } = i;
    (o !== this.scene.width || r !== this.scene.height) && this.updateScene({ width: o, height: r }), this.fullLayer.setAttribute("scale", [t, t]), this.fullLayer.setAttribute("translate", [-e, -s]), this.topLayer.setAttribute("scale", [t, t]), this.topLayer.setAttribute("translate", [-e, -s]), this.localLayer.setAttribute("scale", [t, t]), this.localLayer.setAttribute("translate", [-e, -s]), this.serviceLayer.setAttribute("scale", [t, t]), this.serviceLayer.setAttribute("translate", [-e, -s]);
  }
  runBatchPostData() {
    this.mainThreadPostId || (this.mainThreadPostId = requestAnimationFrame(
      this.combinePost.bind(this)
    ));
  }
  combinePostData() {
    var s;
    this.mainThreadPostId = void 0;
    const i = [];
    let t, e;
    for (const o of this.combinePostMsg.values()) {
      if ((s = o.sp) != null && s.length)
        for (const r of o.sp) {
          let n = false;
          for (const a of i)
            if ((0, import_lodash.isEqual)(r, a)) {
              n = true;
              break;
            }
          n || i.push(r);
        }
      (0, import_lodash.isNumber)(o.fullWorkerDrawCount) && (t = o.fullWorkerDrawCount), (0, import_lodash.isNumber)(o.consumeCount) && (e = o.consumeCount);
    }
    return this.combinePostMsg.clear(), {
      sp: i,
      fullWorkerDrawCount: t,
      consumeCount: e
    };
  }
  combinePost() {
    var e, s;
    const i = this.combinePostData(), t = (e = i.sp) == null ? void 0 : e.filter(
      (o) => o.type !== R.None || o.isLockSentEventCursor || o.needUndoTicker
    );
    t != null && t.length ? i.sp = t.map((o) => o.viewId ? o : __spreadProps(__spreadValues({}, o), { viewId: this.viewId })) : delete i.sp, i.consumeCount === void 0 && delete i.consumeCount, i.fullWorkerDrawCount === void 0 && delete i.fullWorkerDrawCount, (i != null && i.consumeCount || i != null && i.fullWorkerDrawCount || (s = i.sp) != null && s.length) && this.opt.post(i);
  }
  clearAll() {
    this.fullLayer.children.length && (this.fullLayer.parent.children.forEach((i) => {
      i.name !== "viewport" && i.remove();
    }), Q(this.fullLayer, this.fullLayer.parent)), this.clearCacheImages(), this.clearImageResolveMap(), this.localWork.clearAll(), this.topWork.clearAll(), this.serviceWork.clearAll(), this.vNodes.clear(), this.post({
      sp: [
        {
          type: R.Clear
        }
      ]
    });
  }
  consumeDrawAll(i, t) {
    const { toolsType: e, workId: s } = t;
    if (s) {
      const o = s.toString();
      if (e && this.topWork.canUseTopLayer(e)) {
        i === q.Local && (this.topWork.getLocalWorkShape(
          s.toString()
        ) || this.topWork.createLocalWork(t)), this.topWork.consumeDrawAll(t);
        return;
      }
      i === q.Local && (this.localWork.getWorkShape(o) || this.localWork.createLocalWork(t), this.localWork.consumeDrawAll(t, this.serviceWork));
    }
  }
  consumeDraw(i, t) {
    const { opt: e, workId: s, toolsType: o } = t;
    if (s && o && e) {
      const r = s.toString();
      if (this.topWork.canUseTopLayer(o)) {
        i === q.Local && (this.topWork.getLocalWorkShape(r) || this.topWork.createLocalWork(t)), this.topWork.consumeDraw(t);
        return;
      }
      i === q.Local ? (this.localWork.getWorkShape(r) || this.localWork.createLocalWork(t), this.localWork.consumeDraw(t, this.serviceWork)) : i === q.Service && this.serviceWork.consumeDraw(t);
      return;
    }
  }
  updateCamera(i) {
    return __async(this, null, function* () {
      var s;
      const { cameraOpt: t, scenePath: e } = i;
      if (t && !(0, import_lodash.isEqual)(this.cameraOpt, t)) {
        if (this.taskUpdateCameraId && (clearTimeout(this.taskUpdateCameraId), this.taskUpdateCameraId = void 0), e) {
          let c = false;
          for (const [l, h] of this.localWork.getWorkShapes().entries())
            switch (h.toolsType) {
              case T.Text:
              case T.BitMapEraser:
              case T.PencilEraser:
              case T.Eraser:
              case T.Selector:
              case T.LaserPen:
                break;
              default:
                l !== NI && l !== $ && (c = true);
                break;
            }
          if (c) {
            this.taskUpdateCameraId = setTimeout(() => {
              this.taskUpdateCameraId = void 0, this.updateCamera(i);
            }, Xi);
            return;
          }
        }
        const o = /* @__PURE__ */ new Map();
        for (const [c, l] of this.vNodes.getNodesByType(T.Text).entries()) {
          const h = l.rect;
          o.set(c, (0, import_lodash.cloneDeep)(h));
        }
        const r = new Set(o.keys());
        let n = false;
        if (this.localWork.hasSelector()) {
          const c = (s = this.localWork.getSelector()) == null ? void 0 : s.selectIds;
          if (c) {
            n = true;
            for (const l of c)
              r.add(l);
          }
        }
        let a = false;
        if (this.serviceWork.selectorWorkShapes.size)
          for (const c of this.serviceWork.selectorWorkShapes.values()) {
            const l = c.selectIds;
            if (l) {
              a = true;
              for (const h of l)
                r.add(h);
            }
          }
        if (this.setCameraOpt(t), this.vNodes.curNodeMap.size) {
          this.vNodes.clearTarget(), this.vNodes.updateHighLevelNodesRect(r), this.debounceUpdateCameraId && clearTimeout(this.debounceUpdateCameraId);
          for (const [c, l] of o.entries()) {
            const h = this.vNodes.get(c);
            if (h) {
              const p2 = l, d = h.rect, u = this.getSceneRect(), f = FI(p2, u), m = FI(d, u);
              let g = false;
              if ((f !== m || p2.w !== d.w || p2.h !== d.h || m === Gt.intersect) && (g = true), g) {
                const { toolsType: I } = h;
                I === T.Text && this.debounceUpdateCache.add(c);
              }
            }
          }
          if (n && this.localWork.reRenderSelector(), a)
            for (const [
              c,
              l
            ] of this.serviceWork.selectorWorkShapes.entries())
              this.serviceWork.runSelectWork({
                workId: c,
                selectIds: l.selectIds,
                msgType: R.Select,
                dataType: q.Service,
                viewId: this.viewId
              });
          this.debounceUpdateCameraId = setTimeout(() => {
            var l;
            this.debounceUpdateCameraId = void 0;
            const c = [];
            for (const h of this.debounceUpdateCache.values()) {
              if ((l = this.fullLayer) == null ? void 0 : l.getElementsByName(h)[0]) {
                const d = this.vNodes.get(h);
                if (d) {
                  const { toolsType: u, opt: f } = d, m = this.localWork.setFullWork({
                    toolsType: u,
                    opt: f,
                    workId: h
                  });
                  if (m) {
                    const g = this.getSceneRect();
                    c.push(
                      m.consumeServiceAsync({
                        isFullWork: true,
                        replaceId: h,
                        boxRect: g
                      })
                    );
                  }
                }
              }
              this.debounceUpdateCache.delete(h);
            }
            this.vNodes.updateLowLevelNodesRect(), this.vNodes.clearHighLevelIds();
          }, Xi);
        }
      }
    });
  }
  getSceneRect() {
    const { width: i, height: t } = this.scene;
    return {
      x: 0,
      y: 0,
      w: Math.floor(i),
      h: Math.floor(t)
    };
  }
  createScene(i) {
    return new _spritejs$Scene(__spreadProps(__spreadValues({
      displayRatio: this.opt.displayer.dpr,
      depth: false,
      desynchronized: true
    }, i), {
      autoRender: true,
      id: this.viewId,
      contextType: "2d"
    }));
  }
  createLayer(i, t, e) {
    const { width: s, height: o } = e, r = `canvas-${i}`, n = t.layer(r, __spreadProps(__spreadValues({}, e), { offscreen: false })), a = new _spritejs$Group({
      anchor: [0.5, 0.5],
      pos: [s * 0.5, o * 0.5],
      size: [s, o],
      name: "viewport",
      id: i
    });
    return n.append(a), a;
  }
  updateScene(i) {
    this.scene.attr(__spreadValues({}, i));
    const { width: t, height: e } = i;
    this.scene.width = t, this.scene.height = e, this.updateLayer({ width: t, height: e });
  }
  updateLayer(i) {
    const { width: t, height: e } = i;
    this.fullLayer.parent.setAttribute("width", t), this.fullLayer.parent.setAttribute("height", e), this.fullLayer.setAttribute("size", [t, e]), this.fullLayer.setAttribute("pos", [t * 0.5, e * 0.5]), this.topLayer.parent.setAttribute("width", t), this.topLayer.parent.setAttribute("height", e), this.topLayer.setAttribute("size", [t, e]), this.topLayer.setAttribute("pos", [t * 0.5, e * 0.5]), this.localLayer.parent.setAttribute("width", t), this.localLayer.parent.setAttribute("height", e), this.localLayer.setAttribute("size", [t, e]), this.localLayer.setAttribute("pos", [t * 0.5, e * 0.5]), this.serviceLayer.parent.setAttribute("width", t), this.serviceLayer.parent.setAttribute("height", e), this.serviceLayer.setAttribute("size", [t, e]), this.serviceLayer.setAttribute("pos", [t * 0.5, e * 0.5]);
  }
  destroy() {
    this.clearCacheImages(), this.clearImageResolveMap(), this.vNodes.clear(), this.fullLayer.remove(), q2(this.fullLayer, this.fullLayer.parent), this.topLayer.remove(), q2(this.topLayer, this.topLayer.parent), this.localLayer.remove(), q2(this.localLayer, this.localLayer.parent), this.serviceLayer.remove(), q2(this.serviceLayer, this.serviceLayer.parent), this.scene.remove(), this.localWork.destroy(), this.serviceWork.destroy(), this.topWork.destroy();
  }
};
var To = class {
  constructor(i, t) {
    y(this, "viewId");
    y(this, "fullLayer");
    y(this, "master");
    y(this, "opt");
    y(this, "scene");
    y(this, "mainThreadPostId");
    y(this, "combinePostMsg", /* @__PURE__ */ new Set());
    y(this, "workShapes", /* @__PURE__ */ new Map());
    y(this, "cacheImages", /* @__PURE__ */ new Map());
    y(this, "imageResolveMap", /* @__PURE__ */ new Map());
    this.viewId = i, this.opt = t, this.scene = this.createScene(__spreadProps(__spreadValues({}, t.canvasOpt), {
      container: t.container
    })), this.master = t.master, this.fullLayer = this.createLayer("fullLayer", this.scene, __spreadProps(__spreadValues({}, t.layerOpt), {
      bufferSize: this.viewId === pe ? 6e3 : 3e3,
      contextType: "2d"
    }));
  }
  getCachedImages(i) {
    var t;
    return (t = this.cacheImages.get(i)) == null ? void 0 : t.imageBitmap;
  }
  getCachedImagesByWorkId(i) {
    for (const [t, e] of this.cacheImages.entries())
      if (t === i && e.imageBitmap)
        return e.imageBitmap;
  }
  deleteCachedImagesByWorkId(i) {
    for (const [t, e] of this.cacheImages.entries())
      e.workId === i && (e.imageBitmap.close(), this.cacheImages.delete(t));
  }
  clearCacheImages() {
    this.cacheImages.forEach((i) => i.imageBitmap.close()), this.cacheImages.clear();
  }
  clearImageResolveMap() {
    this.imageResolveMap.forEach(({ timer: i }) => {
      i && clearTimeout(i);
    }), this.imageResolveMap.clear();
  }
  post(i) {
    this.combinePostMsg.add(i), this.runBatchPostData();
  }
  on(i) {
    return __async(this, null, function* () {
      const { msgType: t, imageSrc: e, imageBitmap: s, workId: o } = i;
      switch (t) {
        case R.Snapshot: {
          yield this.getSnapshot(i), this.destroy();
          return;
        }
        case R.BoundingBox: {
          yield this.getBoundingRect(i), this.destroy();
          return;
        }
        case R.GetImageBitMap: {
          if (e && s && o) {
            const r = o.toString();
            this.deleteCachedImagesByWorkId(r), this.cacheImages.set(e, {
              imageBitmap: s,
              workId: r
            });
            const n = this.imageResolveMap.get(e);
            if (n) {
              const { resolve: a, timer: c } = n;
              c && clearTimeout(c), a && a(e);
            }
          }
          break;
        }
      }
    });
  }
  getIconSize(i, t, e) {
    const s = i * e, o = t * e;
    return s <= 50 || o <= 50 ? [50, 50] : s <= 100 || o <= 100 ? [100, 100] : s <= 200 || o <= 200 ? [200, 200] : s <= 400 || o <= 400 ? [400, 400] : s <= 800 || o <= 800 ? [800, 800] : [1600, 1600];
  }
  loadImageBitMap(i) {
    return __async(this, null, function* () {
      const { toolsType: t, opt: e, workId: s } = i;
      if (t === T.Image && e && s) {
        const o = s.toString(), { src: r, type: n, width: a, height: c, strokeColor: l } = e;
        if (!r || !n || !a || !c)
          return;
        let h = r;
        if (n === Zt.Iconify) {
          const [f, m] = this.getIconSize(a, c, this.opt.displayer.dpr);
          h = `${r}?width=${f}&height=${m}&color=${l}`;
        }
        if (this.cacheImages.has(h)) {
          const f = this.getCachedImages(h);
          if (f)
            return f;
        }
        if (this.imageResolveMap.has(h)) {
          const f = this.getCachedImagesByWorkId(o);
          if (f)
            return f;
        }
        const u = yield new Promise((f) => {
          const m = this.imageResolveMap.get(h) || {
            resolve: void 0,
            timer: void 0
          };
          m.timer && clearTimeout(m.timer), m.resolve = f, m.timer = setTimeout(() => {
            const g = this.imageResolveMap.get(h);
            g != null && g.resolve && g.resolve(h);
          }, 5e3), this.imageResolveMap.set(h, m), this.opt.post({
            sp: [
              {
                imageSrc: h,
                workId: o,
                viewId: this.viewId,
                isgl: !!this.fullLayer.parent.gl,
                isSubWorker: true,
                type: R.GetImageBitMap
              }
            ]
          });
        });
        return this.imageResolveMap.delete(u), this.getCachedImages(h);
      }
    });
  }
  createWorkShapeNode(i) {
    return Lt(__spreadProps(__spreadValues({}, i), {
      fullLayer: this.fullLayer,
      drawLayer: void 0
    }));
  }
  setFullWork(i) {
    const { workId: t, opt: e, toolsType: s } = i;
    if (t && e && s) {
      const o = t.toString();
      let r;
      return t && this.workShapes.has(o) ? (r = this.workShapes.get(o), r == null || r.setWorkOptions(e)) : r = this.createWorkShapeNode({
        toolsOpt: e,
        toolsType: s,
        workId: o
      }), r ? (this.workShapes.set(o, r), r) : void 0;
    }
  }
  runFullWork(i) {
    return __async(this, null, function* () {
      var s;
      const t = this.setFullWork(i), e = i.ops && $n(i.ops);
      if (t) {
        let o, r;
        const n = (s = t.getWorkId()) == null ? void 0 : s.toString();
        return t.toolsType === T.BackgroundSVG ? o = t.consumeService({
          isFullWork: true,
          replaceId: n
        }) : t.toolsType === T.Image ? o = yield t.consumeServiceAsync({
          isFullWork: true,
          worker: this
        }) : t.toolsType === T.Text ? o = yield t.consumeServiceAsync({
          isFullWork: true,
          replaceId: n,
          isDrawLabel: true
        }) : (o = t.consumeService({
          op: e,
          isFullWork: true,
          replaceId: n
        }), r = (i == null ? void 0 : i.updateNodeOpt) && t.updataOptService(i.updateNodeOpt)), SI(o, r);
      }
    });
  }
  getSnapshot(i) {
    return __async(this, null, function* () {
      const { scenePath: t, scenes: e, cameraOpt: s, w: o, h: r } = i;
      if (t && e && s) {
        this.setCameraOpt(s);
        for (const [a, c] of Object.entries(e))
          if (c != null && c.type)
            switch (c == null ? void 0 : c.type) {
              case R.UpdateNode:
              case R.FullWork: {
                const { opt: l } = c, h = __spreadProps(__spreadValues({}, c), {
                  opt: l,
                  workId: a,
                  msgType: R.FullWork,
                  dataType: q.Service,
                  viewId: this.viewId
                });
                yield this.runFullWork(h);
                break;
              }
            }
        let n;
        o && r && (n = {
          resizeWidth: o,
          resizeHeight: r
        });
        try {
          yield this.getSnapshotRender({ scenePath: t, options: n });
        } catch (a) {
          const c = a && a instanceof Error ? a.message : a == null ? void 0 : a.toString();
          console.error(
            "[SnapshotThreadImpl] getSnapshotRender error",
            c
          ), this.post({
            sp: [
              {
                type: R.ReportError,
                reportString: `[SnapshotThreadImpl] getSnapshotRender error: ${c}`
              }
            ]
          });
        }
      }
    });
  }
  getSceneRect() {
    const { width: i, height: t } = this.scene;
    return {
      x: 0,
      y: 0,
      w: Math.floor(i),
      h: Math.floor(t)
    };
  }
  getRectImageBitmap(i, t) {
    const e = Math.floor(i.x * this.opt.displayer.dpr), s = Math.floor(i.y * this.opt.displayer.dpr), o = i.w > 0 && Math.floor(i.w * this.opt.displayer.dpr || 1) || 1, r = i.h > 0 && Math.floor(i.h * this.opt.displayer.dpr || 1) || 1;
    return createImageBitmap(
      this.fullLayer.parent.canvas,
      e,
      s,
      o,
      r,
      t
    );
  }
  getSnapshotRender(i) {
    return __async(this, null, function* () {
      var o;
      const { scenePath: t, options: e } = i;
      ((o = this.fullLayer) == null ? void 0 : o.parent).render();
      const s = yield this.getRectImageBitmap(
        this.getSceneRect(),
        e
      );
      s && (this.post({
        sp: [
          {
            type: R.Snapshot,
            scenePath: t,
            imageBitmap: s,
            viewId: this.viewId,
            index: 0
          }
        ]
      }), this.fullLayer && Q(this.fullLayer, this.fullLayer.parent));
    });
  }
  getBoundingRect(i) {
    return __async(this, null, function* () {
      const { scenePath: t, scenes: e, cameraOpt: s } = i;
      if (t && e && s) {
        this.setCameraOpt(s);
        let o;
        for (const [r, n] of Object.entries(e))
          if (n != null && n.type)
            switch (n == null ? void 0 : n.type) {
              case R.UpdateNode:
              case R.FullWork: {
                const a = yield this.runFullWork(__spreadProps(__spreadValues({}, n), {
                  workId: r,
                  msgType: R.FullWork,
                  dataType: q.Service,
                  viewId: this.viewId
                }));
                o = SI(o, a);
                break;
              }
            }
        o && this.post({
          sp: [
            {
              type: R.BoundingBox,
              scenePath: t,
              rect: o
            }
          ]
        });
      }
    });
  }
  setCameraOpt(i) {
    const { scale: t, centerX: e, centerY: s, width: o, height: r } = i;
    this.updateScene({ width: o, height: r }), this.fullLayer.setAttribute("scale", [t, t]), this.fullLayer.setAttribute("translate", [-e, -s]);
  }
  runBatchPostData() {
    this.mainThreadPostId || (this.mainThreadPostId = requestAnimationFrame(
      this.combinePost.bind(this)
    ));
  }
  combinePostData() {
    var t;
    this.mainThreadPostId = void 0;
    const i = [];
    for (const e of this.combinePostMsg.values())
      if ((t = e.sp) != null && t.length)
        for (const s of e.sp) {
          let o = false;
          for (const r of i)
            if (lt(s, r)) {
              o = true;
              break;
            }
          o || i.push(s);
        }
    return this.combinePostMsg.clear(), {
      sp: i
    };
  }
  combinePost() {
    var e, s;
    const i = this.combinePostData(), t = (e = i.sp) == null ? void 0 : e.filter(
      (o) => o.type !== R.None || o.isLockSentEventCursor || o.needUndoTicker
    );
    t != null && t.length ? i.sp = t.map((o) => o.viewId ? o : __spreadProps(__spreadValues({}, o), { viewId: this.viewId })) : delete i.sp, (s = i.sp) != null && s.length && this.opt.post(i);
  }
  createScene(i) {
    return new _spritejs$Scene(__spreadProps(__spreadValues({
      displayRatio: this.opt.displayer.dpr,
      depth: false,
      desynchronized: true
    }, i), {
      autoRender: false,
      contextType: "2d"
    }));
  }
  createLayer(i, t, e) {
    const { width: s, height: o } = e, r = `canvas-${i}`, n = t.layer(r, e), a = new _spritejs$Group({
      anchor: [0.5, 0.5],
      pos: [s * 0.5, o * 0.5],
      size: [s, o],
      name: "viewport",
      id: i
    });
    return n.append(a), a;
  }
  updateScene(i) {
    this.scene.attr(__spreadValues({}, i));
    const { width: t, height: e } = i;
    this.scene.width = t, this.scene.height = e, this.updateLayer({ width: t, height: e });
  }
  updateLayer(i) {
    const { width: t, height: e } = i;
    this.fullLayer.parent.setAttribute("width", t), this.fullLayer.parent.setAttribute("height", e), this.fullLayer.setAttribute("size", [t, e]), this.fullLayer.setAttribute("pos", [t * 0.5, e * 0.5]);
  }
  destroy() {
    this.clearCacheImages(), this.clearImageResolveMap(), this.fullLayer.remove(), q2(this.fullLayer, this.fullLayer.parent), this.scene.remove();
  }
};
var Oo = class {
  constructor(i) {
    y(this, "mainThreadMap", /* @__PURE__ */ new Map());
    y(this, "snapshotThread");
    y(this, "master");
    y(this, "post", (i2) => {
      const { fullWorkerDrawCount: t, sp: e, workerTasksqueueCount: s, consumeCount: o } = i2;
      this.master.isBusy && H(s) && this.master.setWorkerTasksqueueCount(s), H(t) && this.master.setMaxDrawCount(t), H(o) && this.master.setConsumeCount(o), e && this.master.collectorSyncData(e);
    });
    this.master = i;
  }
  destroy() {
    this.mainThreadMap.clear();
  }
  createMainThread(i, t) {
    return new So(i, t);
  }
  createSnapshotThread(i, t) {
    return new To(i, t);
  }
  consume(i) {
    return __async(this, null, function* () {
      var t, e, s, o;
      for (const r of i.values()) {
        const {
          msgType: n,
          viewId: a,
          tasksqueue: c,
          mainTasksqueueCount: l,
          layerOpt: h,
          offscreenCanvasOpt: p2,
          cameraOpt: d,
          isSubWorker: u
        } = r;
        if (n === R.Console) {
          console.log(this);
          continue;
        }
        if (n === R.Init) {
          const m = (t = this.master.control.viewContainerManager.getView(a)) == null ? void 0 : t.displayer, g = m == null ? void 0 : m.canvasContainerRef.current;
          if (m && g && h && p2) {
            const I = this.createMainThread(a, {
              displayer: m,
              container: g,
              layerOpt: h,
              master: this.master,
              canvasOpt: p2,
              post: this.post
            });
            this.mainThreadMap.set(a, I), I && d && I.setCameraOpt(d);
          }
          continue;
        }
        if ((n === R.Snapshot || n === R.BoundingBox) && a === ((e = this.master.control.viewContainerManager.mainView) == null ? void 0 : e.id)) {
          const m = (s = this.master.control.viewContainerManager.getView(a)) == null ? void 0 : s.displayer, g = (o = m.snapshotContainerRef) == null ? void 0 : o.current;
          if (m && g && d) {
            g.style.width = `${d.width}px`, g.style.height = `${d.height}px`;
            const I = __spreadProps(__spreadValues({}, Et.defaultLayerOpt), {
              offscreen: false,
              width: d.width,
              height: d.height
            }), S = __spreadProps(__spreadValues({}, Et.defaultScreenCanvasOpt), {
              width: d.width,
              height: d.height
            });
            this.snapshotThread = this.createSnapshotThread(a, {
              displayer: m,
              container: g,
              layerOpt: I,
              master: this.master,
              canvasOpt: S,
              post: this.post
            }), this.snapshotThread.on(r).then(() => {
              this.snapshotThread = void 0, g.innerHTML = "", g.style.width = "", g.style.height = "";
            });
            continue;
          }
        }
        if (n === R.GetImageBitMap && u && this.snapshotThread) {
          this.snapshotThread.on(r);
          continue;
        }
        if (n === R.TasksQueue && (c != null && c.size)) {
          for (const [m, g] of this.mainThreadMap.entries()) {
            const I = c.get(m);
            I && (yield g.on(I), l && this.post({ workerTasksqueueCount: l }));
          }
          continue;
        }
        if (a === fg) {
          for (const m of this.mainThreadMap.values())
            m.on(r), n === R.Destroy && this.mainThreadMap.delete(a);
          continue;
        }
        const f = this.mainThreadMap.get(a);
        f && (f.on(r), n === R.Destroy && this.mainThreadMap.delete(a));
      }
    });
  }
};
export {
  Oo as MainThreadManagerImpl
};
//# sourceMappingURL=index-BZIAL5Sa-WPT727VI.js.map
