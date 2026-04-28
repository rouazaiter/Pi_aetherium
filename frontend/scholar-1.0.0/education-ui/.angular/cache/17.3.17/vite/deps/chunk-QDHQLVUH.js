// node_modules/@netless/appliance-plugin/dist/ObserverMap-BudneEfB.mjs
var o = Object.defineProperty;
var h = (r, e, s) => e in r ? o(r, e, { enumerable: true, configurable: true, writable: true, value: s }) : r[e] = s;
var a = (r, e, s) => h(r, typeof e != "symbol" ? e + "" : e, s);
var p = class {
  constructor(e) {
    a(this, "_map");
    a(this, "_observers", /* @__PURE__ */ new Set());
    this._map = new Map(e);
  }
  notifyObservers(e, s, t) {
    for (const i of this._observers)
      i(e, s, t);
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
  set(e, s, t = true) {
    const i = this._map.has(e) ? "update" : "add";
    return this._map.set(e, s), t && this.notifyObservers(i, e, s), this;
  }
  has(e) {
    return this._map.has(e);
  }
  delete(e, s = true) {
    const t = this._map.get(e), i = this._map.delete(e);
    return s && t && this.notifyObservers("delete", e, t), i;
  }
  clear(e = true) {
    const s = this._map.keys();
    if (this._map.clear(), e)
      for (const t of s)
        this.notifyObservers("clear", t, void 0);
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
  forEach(e, s) {
    this._map.forEach(e, s);
  }
};

export {
  p
};
//# sourceMappingURL=chunk-QDHQLVUH.js.map
