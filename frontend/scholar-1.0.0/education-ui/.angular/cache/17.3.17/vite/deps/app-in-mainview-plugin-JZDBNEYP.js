import {
  require_eventemitter2
} from "./chunk-M62YKLN3.js";
import {
  require_lodash,
  require_white_web_sdk
} from "./chunk-LV5RZA64.js";
import "./chunk-SMZDIAJL.js";
import {
  __async,
  __spreadProps,
  __spreadValues,
  __toESM
} from "./chunk-WME3VOFU.js";

// node_modules/@netless/app-in-mainview-plugin/dist/app-in-mainview-plugin.mjs
var import_white_web_sdk = __toESM(require_white_web_sdk(), 1);
var import_eventemitter2 = __toESM(require_eventemitter2(), 1);
var import_lodash = __toESM(require_lodash(), 1);
var f = Object.defineProperty;
var w = (l, e, t) => e in l ? f(l, e, { enumerable: true, configurable: true, writable: true, value: t }) : l[e] = t;
var n = (l, e, t) => w(l, typeof e != "symbol" ? e + "" : e, t);
var C = class {
  constructor(e) {
    n(this, "_map");
    n(this, "_observers", /* @__PURE__ */ new Set());
    this._map = new Map(e);
  }
  notifyObservers(e, t, i) {
    for (const r of this._observers)
      r(e, t, i);
  }
  observe(e) {
    this._observers.add(e);
  }
  unobserve(e) {
    this._observers.delete(e);
  }
  get(e) {
    return this._map.get(e);
  }
  set(e, t) {
    const i = this._map.has(e) ? "update" : "add";
    return this._map.set(e, t), this.notifyObservers(i, e, t), this;
  }
  has(e) {
    return this._map.has(e);
  }
  delete(e) {
    const t = this._map.get(e), i = this._map.delete(e);
    return t && this.notifyObservers("delete", e, t), i;
  }
  clear() {
    this._map.clear();
  }
  get size() {
    return this._map.size;
  }
  keys() {
    return this._map.keys();
  }
  values() {
    return this._map.values();
  }
  entries() {
    return this._map.entries();
  }
  forEach(e, t) {
    this._map.forEach(e, t);
  }
};
var P = Object.keys;
var V = class {
  constructor(e) {
    n(this, "control");
    n(this, "plugin");
    n(this, "storageObserver");
    n(this, "stateDisposer");
    this.control = e.control, this.plugin = e.plugin, this.storageObserver = new C(Object.entries(this.getAttributes())), this.storageObserver.observe((t, i, r) => {
      this.control.onAppStateChange(t, i, r);
    }), this.observeStorage();
  }
  get storage() {
    return this.storageObserver;
  }
  getAppState(e) {
    return this.storageObserver.get(e);
  }
  addAppState(e, t) {
    var i;
    this.control.isWritable && ((i = this.plugin) == null || i.updateAttributes([e], t));
  }
  deleteAppState(e) {
    var t;
    this.control.isWritable && ((t = this.plugin) == null || t.updateAttributes([e], void 0));
  }
  updateAppState(e, t) {
    var i;
    this.control.isWritable && ((i = this.plugin) == null || i.updateAttributes([e], t));
  }
  getAttributes() {
    return (0, import_white_web_sdk.toJS)(this.plugin.attributes) || {};
  }
  diff(e) {
    const t = P(e);
    for (const i of t)
      this.storageObserver.has(i) ? (0, import_lodash.isEqual)(this.storageObserver.get(i), e[i]) || this.storageObserver.set(i, e[i]) : this.storageObserver.set(i, e[i]);
    for (const i of [...this.storageObserver.keys()])
      t.includes(i) || this.storageObserver.delete(i);
  }
  observeStorage() {
    this.stateDisposer = (0, import_white_web_sdk.autorun)(() => __async(this, null, function* () {
      const e = this.getAttributes();
      this.diff(e);
    }));
  }
  destroy() {
    var e;
    (e = this.stateDisposer) == null || e.call(this);
  }
};
var g = "0.0.9";
var H = "@netless/app-in-mainview-plugin";
if (typeof window < "u") {
  let l = window.__netlessUA || "";
  l += ` ${H}@${g}`, window.__netlessUA = l;
}
var d = {
  enableDefaultUI: true,
  onlyShowHidden: false,
  language: "en",
  theme: "light"
};
var b = "default-app-in-main-view-plugin";
var T = {
  en: {
    show: "show all",
    hidden: "hidden all"
  },
  "zh-CN": {
    show: "全部展开",
    hidden: "全部收起"
  }
};
var E = class {
  constructor(e) {
    n(this, "namespace", b);
    n(this, "container", document.createElement("div"));
    n(this, "badge", document.createElement("div"));
    n(this, "menuView", document.createElement("div"));
    n(this, "manager");
    n(this, "onlyShowHidden");
    n(this, "language");
    n(this, "i18n");
    n(this, "theme");
    n(this, "isBindContainer", false);
    n(this, "containerClickHandler", (e2) => {
      if (e2.stopPropagation(), e2.stopImmediatePropagation(), this.manager.control.wm.readonly)
        return;
      getComputedStyle(this.menuView).display === "flex" ? this.menuView.style.display = "none" : this.menuView.style.display = "flex";
    });
    n(this, "menuViewClickHandler", (e2) => {
      e2.stopPropagation(), e2.stopImmediatePropagation();
      const t = e2.target, i = t.getAttribute(`data-${this.c("app-id")}`);
      if (i) {
        this.manager.control.showApp(i);
        return;
      }
      const r = t.getAttribute(`data-${this.c("btn-type")}`);
      if (r === "show-all") {
        this.manager.control.showCurrentPageApps();
        return;
      }
      if (r === "hidden-all") {
        this.manager.control.hiddenCurrentPageApps();
        return;
      }
    });
    n(this, "appMenuChangeHandler", (e2) => {
      this.render(e2);
    });
    n(this, "onPrefersColorSchemeChangeHandler", () => {
      this.container.classList.remove(this.theme), this.theme = this.manager.control.wmTheme, this.container.classList.add(this.theme);
    });
    n(this, "onMainViewMountedHandler", () => {
      this.isBindContainer || this.bindContainer();
    });
    n(this, "onMainViewRebindHandler", () => {
      this.removeContainer(), this.bindContainer();
    });
    n(this, "onFullscreenChangeHandler", (e2) => {
      e2 ? this.container.style.display = "none" : this.container.style.display = "block";
    });
    this.manager = e.manager, this.onlyShowHidden = e.onlyShowHidden, this.language = e.language, this.i18n = T[this.language], this.theme = e.theme, this.init(), this.observe();
  }
  c(e) {
    return `${this.namespace}-${e}`;
  }
  bindContainer() {
    const e = document.querySelector("button.telebox-collector");
    e && (e.insertAdjacentElement("afterend", this.container), this.isBindContainer = true);
  }
  removeContainer() {
    const e = this.container.parentElement;
    e && (e.removeChild(this.container), this.isBindContainer = false);
  }
  createDefaultAppMenu() {
    this.badge.classList.add(this.c("app-menu-badge")), this.menuView.classList.add(this.c("app-menu-tooltip")), this.container.classList.add(this.c("app-menu-container"), this.theme), this.menuView.addEventListener("click", this.menuViewClickHandler), this.container.addEventListener("click", this.containerClickHandler), this.container.append(this.badge, this.menuView), this.bindContainer();
  }
  init() {
    return __async(this, null, function* () {
      const e = yield this.manager.getApps();
      this.createDefaultAppMenu(), this.render(e);
    });
  }
  observe() {
    this.manager.control.publicEventEmitter.on("appMenuChange", this.appMenuChangeHandler), this.manager.control.wm.emitter.on("prefersColorSchemeChange", this.onPrefersColorSchemeChangeHandler), this.manager.control.wm.emitter.on("onMainViewMounted", this.onMainViewMountedHandler), this.manager.control.wm.emitter.on("onMainViewRebind", this.onMainViewRebindHandler), this.manager.control.wm.emitter.on("fullscreenChange", this.onFullscreenChangeHandler);
  }
  unobserve() {
    this.manager.control.publicEventEmitter.off("appMenuChange", this.appMenuChangeHandler), this.manager.control.wm.emitter.off("prefersColorSchemeChange", this.onPrefersColorSchemeChangeHandler), this.manager.control.wm.emitter.off("onMainViewMounted", this.onMainViewMountedHandler), this.manager.control.wm.emitter.off("onMainViewRebind", this.onMainViewRebindHandler), this.manager.control.wm.emitter.off("fullscreenChange", this.onFullscreenChangeHandler);
  }
  createItem(e, t) {
    const i = document.createElement("div");
    if (i.classList.add(this.c("app-menu-item")), t.status === "hidden" && i.classList.add("active"), this.onlyShowHidden || i.classList.add("has-dot"), i.setAttribute(`data-${this.c("app-id")}`, e), !this.onlyShowHidden && t.status === "visible") {
      const a = document.createElement("div");
      a.classList.add(this.c("app-menu-item-dot")), i.appendChild(a);
    }
    const r = document.createElement("div");
    if (r.classList.add(this.c("app-menu-item-title")), t.appInfo) {
      const c = t.appInfo.appAttributes.options.title || e;
      r.innerText = c;
    } else
      r.innerText = e;
    return i.appendChild(r), i;
  }
  createShowBtn() {
    const e = document.createElement("div");
    e.classList.add(this.c("app-menu-item"), "show-all"), e.setAttribute(`data-${this.c("btn-type")}`, "show-all");
    const t = document.createElement("div");
    return t.classList.add(this.c("app-menu-item-title")), t.innerText = this.i18n.show, e.appendChild(t), e;
  }
  createHidBtn() {
    const e = document.createElement("div");
    e.classList.add(this.c("app-menu-item"), "hidden-all"), e.setAttribute(`data-${this.c("btn-type")}`, "hidden-all");
    const t = document.createElement("div");
    return t.classList.add(this.c("app-menu-item-title")), t.innerText = this.i18n.hidden, e.appendChild(t), e;
  }
  updateMenuView(e) {
    let t = false, i = false;
    this.manager.control.getCurrentPageApps().forEach((h) => {
      h === "visible" ? i = true : t = true;
    });
    const a = [];
    e.forEach((h, p) => {
      a.push(this.createItem(p, h));
    });
    const c = this.createShowBtn();
    t && c.classList.add("active");
    const o = this.createHidBtn();
    i && o.classList.add("active"), this.menuView.append(...a, c, o);
  }
  render(e) {
    this.menuView.style.display = "none", this.badge.innerText = "", this.menuView.innerHTML = "", e.size === 0 ? this.container.style.display = "none" : (this.badge.innerText = e.size.toString(), this.updateMenuView(e), this.container.style.display = "block");
  }
  destroy() {
    this.unobserve(), this.badge.remove(), this.menuView.removeEventListener("click", this.menuViewClickHandler), this.menuView.remove(), this.container.removeEventListener("click", this.containerClickHandler), this.container.remove(), this.removeContainer();
  }
};
var B = class {
  constructor(e) {
    n(this, "enableDefaultUI", d.enableDefaultUI);
    n(this, "onlyShowHidden");
    n(this, "apps");
    n(this, "scenePath");
    n(this, "resolvePublicEventEmitter");
    n(this, "resolveTimer");
    n(this, "appMenu");
    n(this, "timer");
    n(this, "control");
    this.control = e.control, this.enableDefaultUI = (0, import_lodash.isBoolean)(e.options.enableDefaultUI) ? e.options.enableDefaultUI : d.enableDefaultUI, this.onlyShowHidden = (0, import_lodash.isBoolean)(e.options.onlyShowHidden) ? e.options.onlyShowHidden : d.onlyShowHidden, this.scenePath = this.currentScenePath, this.apps = this.getCurrentApps(), this.enableDefaultUI && (this.appMenu = new E({
      manager: this,
      onlyShowHidden: this.onlyShowHidden,
      language: e.options.language || d.language,
      theme: e.options.theme || this.control.wmTheme
    }));
  }
  get mainView() {
    return this.control.wm.mainView;
  }
  get currentScenePath() {
    return this.mainView.focusScenePath;
  }
  get wmAppProxies() {
    var e;
    return ((e = this.control.wm.appManager) == null ? void 0 : e.appProxies) || /* @__PURE__ */ new Map();
  }
  getApps() {
    return __async(this, null, function* () {
      return this.checkAppChangeAllReady() ? this.apps : (yield this.willAppInfoAllReady(), this.apps);
    });
  }
  updateAppInfo(e) {
    const t = this.apps.get(e);
    t && (t.appInfo = this.wmAppProxies.get(e), this.resolvePublicEventEmitter && this.checkAppChangeAllReady() && this.resolvePublicEventEmitter(true));
  }
  pageChangeHandler(e, t) {
    return __async(this, null, function* () {
      this.scenePath = e, this.apps = this.getCurrentApps(t), yield this.willPublicEmitterMenuChange();
    });
  }
  appChangeHandler(e, t, i) {
    if (i.scenePath === this.scenePath)
      if (this.onlyShowHidden && i.status === "visible")
        this.apps.delete(t);
      else if ((e === "add" || e === "update") && this.wmAppProxies.has(t)) {
        const r = {
          status: i.status,
          appInfo: this.wmAppProxies.get(t)
        };
        this.apps.set(t, r);
      } else e === "delete" && this.apps.delete(t);
    this.timer && clearTimeout(this.timer), this.timer = setTimeout(() => {
      this.timer = void 0, this.willPublicEmitterMenuChange();
    }, 100);
  }
  destroy() {
    var e;
    this.apps.clear(), (e = this.appMenu) == null || e.destroy(), this.timer && clearTimeout(this.timer), this.timer = void 0, this.resolveTimer && clearTimeout(this.resolveTimer), this.resolveTimer = void 0;
  }
  getCurrentApps(e) {
    let t = e;
    t || (t = this.control.getCurrentPageApps());
    const i = /* @__PURE__ */ new Map();
    return t.forEach((r, a) => {
      this.onlyShowHidden && r === "visible" || i.set(a, {
        status: r,
        appInfo: this.wmAppProxies.get(a)
      });
    }), i;
  }
  checkAppChangeAllReady() {
    for (const e of this.apps.values())
      if (!e.appInfo)
        return false;
    return true;
  }
  willAppInfoAllReady(e) {
    return __async(this, null, function* () {
      if (this.resolvePublicEventEmitter && this.resolvePublicEventEmitter(false), this.checkAppChangeAllReady()) {
        e && e();
        return;
      }
      const t = yield new Promise((i) => {
        this.resolvePublicEventEmitter = i, this.resolveTimer = setTimeout(() => {
          this.resolveTimer = void 0, this.resolvePublicEventEmitter && this.resolvePublicEventEmitter(true);
        }, 2e3);
      });
      this.resolveTimer && (clearTimeout(this.resolveTimer), this.resolveTimer = void 0), this.resolvePublicEventEmitter = void 0, t && e && e();
    });
  }
  willPublicEmitterMenuChange() {
    return __async(this, null, function* () {
      yield this.willAppInfoAllReady(() => {
        this.control.publicEventEmitter.emit("appMenuChange", this.apps), this.control.logger.info("[AppInMainViewPlugin] emit appMenuChange");
      });
    });
  }
};
var I = class extends import_eventemitter2.default {
  emit(e, ...t) {
    return super.emit(e, ...t);
  }
  on(e, t) {
    return super.on(e, t);
  }
};
var z = class {
  constructor(e) {
    n(this, "wm");
    n(this, "publicEventEmitter", new I());
    n(this, "logger");
    n(this, "version", g);
    n(this, "pluginOptions");
    n(this, "injectStyleId", `${b}-inject-style`);
    n(this, "collector");
    n(this, "plugin");
    n(this, "isCurWritable", false);
    n(this, "originSetBoxState");
    n(this, "originSetMinimized");
    n(this, "appMenuManager");
    n(this, "focueTimer");
    n(this, "titlebarTimer");
    n(this, "onWritableChange", () => {
      const e2 = this.wm.displayer.isWritable;
      this.isCurWritable = e2;
    });
    n(this, "observeTitlebarHandler", () => {
      const e2 = this.wm.boxState;
      let t = false;
      const i = this.getCurrentPageVisibleApps();
      e2 === "maximized" ? (i.size < 2 ? this.setTitlebarNodeDisplay("none") : this.setTitlebarNodeDisplay("flex"), t = true) : this.setTitlebarNodeDisplay("none");
      for (const r of i)
        t ? this.setAppNodeDisplay(r, "block", true) : this.activeMaximizeBtn(r, false);
    });
    n(this, "observeBoxStateChangeHandler", () => {
      this.checkBoxState() && (this.observeTitlebarTimer(), this.wm.boxState === "maximized" && this.observerFocusAppTimer());
    });
    n(this, "getTargetParent", (e2) => e2.hasAttribute("data-tele-box-i-d") ? e2 : e2.parentElement ? this.getTargetParent(e2.parentElement) : null);
    n(this, "minimizeBtnClickHandler", (e2) => {
      if (e2.stopPropagation(), e2.stopImmediatePropagation(), this.wm.readonly)
        return;
      const t = this.getTargetParent(e2.target);
      if (t) {
        const i = t.getAttribute("data-tele-box-i-d");
        i && this.hideApp(i);
      }
    });
    n(this, "observeAppSetupHandler", (e2) => {
      if (document.querySelector(`div.telebox-box[data-tele-box-i-d="${e2}"]`)) {
        const i = this.collector.getAppState(e2), r = this.wm.mainView.focusScenePath;
        i ? (i.status === "visible" && r === i.scenePath ? this.setAppNodeDisplay(e2, "block") : i.status === "hidden" && this.setAppNodeDisplay(e2, "none"), this.wm.boxState === "maximized" && (this.observeTitlebarTimer(), this.observerFocusAppTimer())) : r && this.collector.addAppState(e2, {
          scenePath: r,
          status: "visible"
        });
        const a = document.querySelector(`div[data-tele-box-i-d="${e2}"] .telebox-titlebar-icon-minimize`);
        a && (a.removeEventListener("click", this.minimizeBtnClickHandler), a.addEventListener("click", this.minimizeBtnClickHandler));
      }
      this.appMenuManager.updateAppInfo(e2);
    });
    n(this, "observeBoxCloseHandler", (e2) => {
      this.collector.storage.has(e2.appId) && this.collector.deleteAppState(e2.appId);
      const i = document.querySelector(`div[data-tele-box-i-d="${e2.appId}"] .telebox-titlebar-icon-minimize`);
      i && i.removeEventListener("click", this.minimizeBtnClickHandler);
    });
    n(this, "observeMainViewScenePathChangeHandler", (e2) => {
      this.collector.storage.forEach((i, r) => {
        i.scenePath === e2 && i.status === "visible" ? this.setAppNodeDisplay(r, "block") : this.setAppNodeDisplay(r, "none");
      }), this.wm.boxState === "maximized" && (this.observeTitlebarTimer(), this.observerFocusAppTimer());
      const t = this.getTargetPageApps(e2);
      this.appMenuManager.pageChangeHandler(e2, t);
    });
    n(this, "observeMaxStateMinimizeBtnClickHandler", (e2) => {
      e2.stopPropagation(), e2.stopImmediatePropagation();
      const t = this.topBoxId;
      t && this.hideApp(t);
    });
    n(this, "observeMainViewMountedHandler", () => {
      this.bindMaxStateMinimizeBtnClickHandler();
    });
    n(this, "observeMainViewRebindHandler", () => {
      this.removeMaxStateMinimizeBtnClickHandler(), this.bindMaxStateMinimizeBtnClickHandler();
    });
    this.wm = e.windowManager, this.wm.room && (this.isCurWritable = this.wm.room.isWritable), this.logger = e.logger, this.pluginOptions = e.options, this.restrictedSetBoxState();
  }
  get isWritable() {
    return this.isCurWritable;
  }
  get wmTheme() {
    return this.wm.prefersColorScheme && this.wm.prefersColorScheme !== "auto" ? this.wm.prefersColorScheme : d.theme;
  }
  get topBoxId() {
    const e = Array.from(document.querySelectorAll("div.telebox-box"));
    if (!e.length)
      return;
    let t = 0, i;
    for (const r of e) {
      if (getComputedStyle(r).display === "none")
        continue;
      const a = Number(getComputedStyle(r).zIndex), c = r.getAttribute("data-tele-box-i-d");
      a > t && c && (t = a, i = c);
    }
    return i;
  }
  get focused() {
    return this.wm.focused;
  }
  init() {
    this.wm.room && this.wm.room.logger.info(`[AppInMainViewPlugin] appInMainViewManager init ${JSON.stringify(this.pluginOptions)}`), this.initInjectStyle(), this.collector = new V({
      control: this,
      plugin: this.plugin
    }), this.observeWm(), this.appMenuManager = new B({
      control: this,
      options: this.pluginOptions
    });
  }
  initInjectStyle() {
    this.removeInjectStyle();
    const e = document.createElement("style");
    e.id = this.injectStyleId, document.head.appendChild(e);
    try {
      if (!e.sheet) {
        console.error("Style sheet is not available");
        return;
      }
      e.sheet.insertRule(".telebox-titles .telebox-titles-tab[data-tele-box-i-d], .telebox-box[data-tele-box-i-d], .telebox-titlebar.telebox-max-titlebar { display: none; }", e.sheet.cssRules.length);
    } catch (t) {
      console.warn("Failed to insert style rule:", t), e.textContent = ".telebox-titles .telebox-titles-tab[data-tele-box-i-d], .telebox-box[data-tele-box-i-d], .telebox-titlebar.telebox-max-titlebar { display: none; }";
    }
  }
  removeInjectStyle() {
    const e = document.getElementById(this.injectStyleId);
    e && e.remove();
  }
  bindPlugin(e) {
    this.plugin = e, this.init();
  }
  destroy() {
    this.unobserveWm(), this.focueTimer && (clearTimeout(this.focueTimer), this.focueTimer = void 0), this.titlebarTimer && (clearTimeout(this.titlebarTimer), this.titlebarTimer = void 0), this.appMenuManager.destroy(), this.removeInjectStyle(), this.wm.room && this.wm.room.logger.info("[AppInMainViewPlugin] AppInMainViewManager has been destroyed");
  }
  setTitlebarNodeDisplay(e) {
    const t = document.querySelector("div.telebox-titlebar.telebox-max-titlebar");
    t && (e === "flex" ? t.style.display = `${e}` : t.style.removeProperty("display"));
  }
  activeMaximizeBtn(e, t = false) {
    let i;
    e instanceof HTMLDivElement ? i = e.querySelector("button.telebox-titlebar-icon-maximize") : i = document.querySelector(`.telebox-box[data-tele-box-i-d="${e}"] button.telebox-titlebar-icon-maximize`), i && (t && !i.classList.contains("is-active") ? i.classList.add("is-active") : !t && i.classList.contains("is-active") && i.classList.remove("is-active"));
  }
  setAppNodeDisplay(e, t, i = false) {
    const r = Array.from(document.querySelectorAll(`[data-tele-box-i-d="${e}"]`));
    r.length && r.forEach((a) => {
      t === "block" ? (a.style.display = `${t}`, i && a instanceof HTMLDivElement && (this.wm.boxState === "maximized" ? this.activeMaximizeBtn(a, true) : this.activeMaximizeBtn(a, false))) : a.style.removeProperty("display");
    });
  }
  observerFocusAppTimer() {
    this.focueTimer && clearTimeout(this.focueTimer), this.focueTimer = setTimeout(() => {
      if (this.focueTimer = void 0, this.wm.boxState === "maximized") {
        const t = this.topBoxId;
        t && this.focused !== t && this.wm.focusApp(t);
      }
    }, 100);
  }
  observeTitlebarTimer() {
    this.titlebarTimer && clearTimeout(this.titlebarTimer), this.titlebarTimer = setTimeout(() => {
      this.titlebarTimer = void 0, this.observeTitlebarHandler();
    }, 100);
  }
  restrictedSetBoxState() {
    this.originSetBoxState = this.wm.setBoxState, this.originSetMinimized = this.wm.setMinimized, this.wm.setBoxState = (e) => {
      if (e === "minimized") {
        this.logger.warn("[AppInMainViewPlugin] when use appInMainViewManager, setBoxState can not set to minimized");
        return;
      }
      this.originSetBoxState && this.originSetBoxState.call(this.wm, e);
    }, this.wm.setMinimized = (e) => {
      if (e) {
        this.logger.warn("[AppInMainViewPlugin] when use appInMainViewManager, setMinimized can not set to minimized");
        return;
      }
      this.originSetMinimized && this.originSetMinimized.call(this.wm, e);
    };
  }
  bindMaxStateMinimizeBtnClickHandler() {
    const e = document.querySelector("div.telebox-max-titlebar .telebox-titlebar-icon-minimize");
    e && e.addEventListener("click", this.observeMaxStateMinimizeBtnClickHandler);
  }
  removeMaxStateMinimizeBtnClickHandler() {
    const e = document.querySelector("div.telebox-max-titlebar .telebox-titlebar-icon-minimize");
    e && e.removeEventListener("click", this.observeMaxStateMinimizeBtnClickHandler);
  }
  checkBoxState() {
    return this.wm.boxState === "minimized" ? (this.logger.warn("[AppInMainViewPlugin] when use appInMainViewManager boxState can not minimized, but boxState is minimized now"), this.isWritable ? this.wm.setMinimized(false) : this.logger.error(`[AppInMainViewPlugin] when use appInMainViewManager boxState can not minimized, but boxState is ${this.wm.boxState} and isWritable is ${this.isWritable} now.`), false) : true;
  }
  observeWm() {
    this.bindMaxStateMinimizeBtnClickHandler(), this.wm.emitter.on("boxStateChange", this.observeBoxStateChangeHandler), this.wm.emitter.on("onAppSetup", this.observeAppSetupHandler), this.wm.emitter.on("onBoxClose", this.observeBoxCloseHandler), this.wm.emitter.on("mainViewScenePathChange", this.observeMainViewScenePathChangeHandler), this.wm.emitter.on("onMainViewMounted", this.observeMainViewMountedHandler), this.wm.emitter.on("onMainViewRebind", this.observeMainViewRebindHandler), this.wm.displayer.callbacks.on("onEnableWriteNowChanged", this.onWritableChange), this.checkBoxState();
  }
  unobserveWm() {
    this.wm.setBoxState = this.originSetBoxState, this.wm.setMinimized = this.originSetMinimized, this.wm.emitter.off("boxStateChange", this.observeBoxStateChangeHandler), this.wm.emitter.off("onAppSetup", this.observeAppSetupHandler), this.wm.emitter.off("onBoxClose", this.observeBoxCloseHandler), this.wm.emitter.off("mainViewScenePathChange", this.observeMainViewScenePathChangeHandler), this.wm.emitter.off("onMainViewMounted", this.observeMainViewMountedHandler), this.wm.emitter.off("onMainViewRebind", this.observeMainViewRebindHandler), this.wm.displayer.callbacks.off("onEnableWriteNowChanged", this.onWritableChange), this.removeMaxStateMinimizeBtnClickHandler();
  }
  onAppStateChange(e, t, i) {
    const r = this.wm.mainView.focusScenePath;
    if (r) {
      if (i.scenePath !== r)
        this.setAppNodeDisplay(t, "none");
      else {
        switch (e) {
          case "add": {
            this.setAppNodeDisplay(t, "block");
            break;
          }
          case "delete": {
            this.setAppNodeDisplay(t, "none");
            break;
          }
          case "update": {
            i.status === "visible" ? this.setAppNodeDisplay(t, "block") : this.setAppNodeDisplay(t, "none");
            break;
          }
        }
        this.wm.boxState === "maximized" && (this.observeTitlebarTimer(), this.observerFocusAppTimer());
      }
      this.appMenuManager.appChangeHandler(e, t, i);
    }
  }
  hideApp(e) {
    const t = this.collector.getAppState(e);
    t && t.status === "visible" && this.collector.updateAppState(e, __spreadProps(__spreadValues({}, t), {
      status: "hidden"
    }));
  }
  showApp(e) {
    const t = this.collector.getAppState(e);
    t && t.status === "hidden" && this.collector.updateAppState(e, __spreadProps(__spreadValues({}, t), {
      status: "visible"
    }));
  }
  showCurrentPageApps() {
    this.getCurrentPageApps().forEach((t, i) => {
      t === "hidden" && this.showApp(i);
    });
  }
  hiddenCurrentPageApps() {
    this.getCurrentPageVisibleApps().forEach((t) => {
      this.hideApp(t);
    });
  }
  get isCurrentPageAppsAllVisible() {
    return this.getCurrentPageApps().values().every((t) => t === "visible");
  }
  get isCurrentPageAppsAllHidden() {
    return this.getCurrentPageApps().values().every((t) => t === "hidden");
  }
  getTargetPageVisibleApps(e) {
    const t = /* @__PURE__ */ new Set();
    return this.collector.storage.forEach((i, r) => {
      i.scenePath === e && i.status === "visible" && t.add(r);
    }), t;
  }
  getTargetPageApps(e) {
    const t = /* @__PURE__ */ new Map();
    return this.collector.storage.forEach((i, r) => {
      i.scenePath === e && t.set(r, i.status);
    }), t;
  }
  getCurrentPageVisibleApps() {
    const e = this.wm.mainView.focusScenePath;
    return e ? this.getTargetPageVisibleApps(e) : /* @__PURE__ */ new Set();
  }
  getCurrentPageApps() {
    const e = this.wm.mainView.focusScenePath;
    return e ? this.getTargetPageApps(e) : /* @__PURE__ */ new Map();
  }
};
var s = class s2 extends import_white_web_sdk.InvisiblePlugin {
  static getInstance(e, t, i) {
    return __async(this, null, function* () {
      i && (s2.logger = i);
      const r = e.displayer;
      let a = r.getInvisiblePlugin(s2.kind);
      s2.currentManager || s2.createCurrentManager(e, t || {}), a || (a = yield s2.createAppInMainViewPlugin(r, s2.kind)), a && s2.currentManager && s2.currentManager.bindPlugin(a);
      const c = {
        displayer: r,
        windowManager: e,
        currentManager: s2.currentManager,
        destroy() {
          s2.currentManager && (s2.logger.info("[AppInMainViewPlugin] has been destroyed"), s2.currentManager.destroy(), s2.currentManager = void 0);
        },
        addListener: (o, h) => {
          var p;
          (p = s2.currentManager) == null || p.publicEventEmitter.on(o, h);
        },
        removeListener: (o, h) => {
          var p;
          (p = s2.currentManager) == null || p.publicEventEmitter.off(o, h);
        },
        hideApp: (o) => {
          var h;
          s2.logger.info(`[AppInMainViewPlugin] hideApp ${o}`), (h = s2.currentManager) == null || h.hideApp(o);
        },
        showApp: (o) => {
          var h;
          s2.logger.info(`[AppInMainViewPlugin] showApp ${o}`), (h = s2.currentManager) == null || h.showApp(o);
        },
        showCurrentPageApps: () => {
          var o;
          s2.logger.info("[AppInMainViewPlugin] showCurrentPageApps"), (o = s2.currentManager) == null || o.showCurrentPageApps();
        },
        hiddenCurrentPageApps: () => {
          var o;
          s2.logger.info("[AppInMainViewPlugin] hiddenCurrentPageApps"), (o = s2.currentManager) == null || o.hiddenCurrentPageApps();
        }
      };
      return Object.defineProperty(c, "currentPageVisibleApps", {
        get() {
          return s2.currentManager ? s2.currentManager.getCurrentPageVisibleApps() : /* @__PURE__ */ new Set();
        }
      }), Object.defineProperty(c, "currentPageApps", {
        get() {
          return s2.currentManager ? s2.currentManager.getCurrentPageApps() : /* @__PURE__ */ new Map();
        }
      }), e._appInMainViewPlugin = c, e._appInMainViewPlugin;
    });
  }
  static onCreate(e) {
    e && s2.currentManager && (s2.timer && (clearTimeout(s2.timer), s2.timer = void 0), s2.currentManager.bindPlugin(e));
  }
  static createAppInMainViewPlugin(e, t) {
    return __async(this, null, function* () {
      if ((0, import_white_web_sdk.isRoom)(e))
        try {
          if (e.isWritable)
            return yield e.createInvisiblePlugin(s2, {});
          {
            yield e.setWritable(true);
            const r = yield s2.createAppInMainViewPlugin(e, t);
            return yield e.setWritable(false), r;
          }
        } catch (r) {
          s2.logger.error("[AppInMainViewPlugin] createAppInMainViewPlugin error", r);
        }
      let i = e.getInvisiblePlugin(t);
      return i || (yield new Promise((r) => {
        s2.timer && (clearTimeout(s2.timer), s2.timer = void 0), s2.timer = setTimeout(() => {
          s2.timer = void 0, r(true);
        }, 1e3);
      }), i = yield s2.createAppInMainViewPlugin(e, t)), i;
    });
  }
  destroy() {
    var e;
    s2.logger.info("[AppInMainViewPlugin] passive destroyed"), (e = s2.currentManager) == null || e.destroy(), s2.currentManager = void 0;
  }
};
n(s, "kind", "app-in-main-view-plugin"), n(s, "currentManager"), n(s, "timer"), n(s, "logger", {
  info: console.log,
  warn: console.warn,
  error: console.error
}), n(s, "createCurrentManager", (e, t) => {
  s.currentManager && s.currentManager.destroy();
  const i = {
    windowManager: e,
    options: t,
    logger: s.logger
  }, r = new z(i);
  e.room && s.logger.info("[AppInMainViewPlugin] new appInMainViewManager"), s.currentManager = r;
});
var u = s;
export {
  z as AppInMainViewManager,
  u as AppInMainViewPlugin,
  V as Collector,
  I as EventEmitter,
  P as plainObjectKeys
};
//# sourceMappingURL=app-in-mainview-plugin-JZDBNEYP.js.map
