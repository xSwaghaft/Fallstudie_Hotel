import { T as jl, V as Vl, X as Xu, U as su, b as le, y as y$1, W as Ru, w as wt, N as gn, D as Do, a as Re$1, Y as Pu, _ as ou, $ as du, m as m$1, c as yn, B as Bo, a0 as od, e as ee, A as A$1, a1 as nn, a2 as Po, a3 as ju, l as lc, C as Cu, a4 as Tu, O as ce$1, o as Ve, a5 as sd, a6 as ht, a7 as V$1, a8 as Kc, a9 as Du, aa as ku, ab as Ju, ac as _l, K as pe$1, q as qc, ad as $u, ae as yl, f as ad, af as ko, ag as tt$1, i as id, s as ld, ah as gl, ai as Qu, aj as ed, r as pc, ak as Bu, al as Wc, am as tu, M as Mu, an as No, ao as Uu, ap as So, aq as oc, ar as Co, as as Zu } from "./indexhtml-jcdUVBfh.js";
import { b, h } from "./state-DecwIYub-BDlWk3uv.js";
import { f } from "./query-BykXNUlT-DDC0EGul.js";
import { o } from "./icons-iLxqH3Sc-DkBBcM_q.js";
import { i as i$1 } from "./overlay-monkeypatch-HZaVJ-wi-BczBz-Gi.js";
import { T } from "./index-CjujSS76-RBB9MF2-.js";
const Lt = 1, Re = 36, zt = 18;
function Mt(e, t) {
  if (e.length === 0)
    return;
  const i = _t(e, t);
  for (const n in e)
    e[n].style.setProperty("--content-height", `${i[n]}px`);
}
function _t(e, t) {
  const i = e.length, n = e.filter((s) => s.panelInfo && s.panelInfo.expanded).length, o2 = i - n;
  return e.map((s) => {
    const a = s.panelInfo;
    return a && !a.expanded ? Re : (t.offsetHeight - (t.position === "bottom" ? zt : 0) - o2 * Re - i * Lt) / n;
  });
}
var Ot = Object.defineProperty, Tt = Object.getOwnPropertyDescriptor, B = (e, t, i, n) => {
  for (var o2 = n > 1 ? void 0 : n ? Tt(t, i) : t, s = e.length - 1, a; s >= 0; s--)
    (a = e[s]) && (o2 = (n ? a(t, i, o2) : a(o2)) || o2);
  return n && o2 && Ot(t, i, o2), o2;
};
const de = "data-drag-initial-index", q = "data-drag-final-index";
let M = class extends lc {
  constructor() {
    super(...arguments), this.position = "right", this.opened = false, this.keepOpen = false, this.resizing = false, this.closingForcefully = false, this.draggingSectionPanel = null, this.drawerDragLeaveListener = (e) => {
      const { x: t, y: i } = e;
      _l(this.getBoundingClientRect(), t, i) ? this.debounceHideDrawerWhenDragLeave.clear() : this.debounceHideDrawerWhenDragLeave();
    }, this.drawerDragEnterListener = () => {
      this.opened = true;
    }, this.debounceHideDrawerWhenDragLeave = ou(() => {
      this.opened = false;
    }, 200), this.panelCountChanged = ou(() => {
      this.refreshSplit();
    }, 100), this.documentMouseUpListener = () => {
      this.resizing && y$1.emit("user-select", { allowSelection: true }), this.resizing = false, m$1.setDrawerResizing(false), this.removeAttribute("resizing");
    }, this.resizingMouseMoveListener = (e) => {
      if (!this.resizing)
        return;
      const { x: t, y: i } = e;
      e.stopPropagation(), e.preventDefault(), requestAnimationFrame(() => {
        let n;
        if (this.position === "right") {
          const o2 = document.body.clientWidth - t;
          this.style.setProperty("--size", `${o2}px`), pe$1.saveDrawerSize(this.position, o2), n = { width: o2 };
        } else if (this.position === "left") {
          const o2 = t;
          this.style.setProperty("--size", `${o2}px`), pe$1.saveDrawerSize(this.position, o2), n = { width: o2 };
        } else if (this.position === "bottom") {
          const o2 = document.body.clientHeight - i;
          this.style.setProperty("--size", `${o2}px`), pe$1.saveDrawerSize(this.position, o2), n = { height: o2 };
        }
        this.setActualSize(), le.panels.filter((o2) => !o2.floating && o2.panel === this.position).forEach((o2) => {
          le.updatePanel(o2.tag, n);
        });
      });
    }, this.sectionPanelDraggingStarted = (e, t) => {
      this.draggingSectionPanel = e, y$1.emit("user-select", { allowSelection: false }), this.draggingSectionPointerStartY = t.clientY, e.toggleAttribute("dragging", true), e.style.zIndex = "1000", Array.from(this.querySelectorAll("copilot-section-panel-wrapper")).forEach((i, n) => {
        i.setAttribute(de, `${n}`);
      }), document.addEventListener("mousemove", this.sectionPanelDragging), document.addEventListener("mouseup", this.sectionPanelDraggingFinished);
    }, this.sectionPanelDragging = (e) => {
      if (!this.draggingSectionPanel)
        return;
      const { clientX: t, clientY: i } = e;
      if (!_l(this.getBoundingClientRect(), t, i)) {
        this.cleanUpDragging();
        return;
      }
      const n = i - this.draggingSectionPointerStartY;
      this.draggingSectionPanel.style.transform = `translateY(${n}px)`, this.updateSectionPanelPositionsWhileDragging();
    }, this.sectionPanelDraggingFinished = () => {
      if (!this.draggingSectionPanel)
        return;
      y$1.emit("user-select", { allowSelection: true });
      const e = this.getAllPanels().filter(
        (t) => t.hasAttribute(q) && t.panelInfo?.panelOrder !== Number.parseInt(t.getAttribute(q), 10)
      ).map((t) => ({
        tag: t.panelTag,
        order: Number.parseInt(t.getAttribute(q), 10)
      }));
      this.cleanUpDragging(), le.updateOrders(e), document.removeEventListener("mouseup", this.sectionPanelDraggingFinished), document.removeEventListener("mousemove", this.sectionPanelDragging), this.refreshSplit();
    }, this.updateSectionPanelPositionsWhileDragging = () => {
      const e = this.draggingSectionPanel.getBoundingClientRect().height;
      this.getAllPanels().sort((t, i) => {
        const n = t.getBoundingClientRect(), o2 = i.getBoundingClientRect(), s = (n.top + n.bottom) / 2, a = (o2.top + o2.bottom) / 2;
        return s - a;
      }).forEach((t, i) => {
        if (t.setAttribute(q, `${i}`), t.panelTag !== this.draggingSectionPanel?.panelTag) {
          const n = Number.parseInt(t.getAttribute(de), 10);
          n > i ? t.style.transform = `translateY(${-e}px)` : n < i ? t.style.transform = `translateY(${e}px)` : t.style.removeProperty("transform");
        }
      });
    }, this.panelExpandedListener = (e) => {
      this.querySelector(`copilot-section-panel-wrapper[paneltag="${e.detail.panelTag}"]`) && this.refreshSplit();
    }, this.setActualSize = () => {
      let e = this.offsetWidth;
      this.position === "bottom" && (e = this.offsetHeight), this.style.setProperty("--actual-size", `calc(${e}px - var(--hover-size))`);
    };
  }
  static get styles() {
    return [
      V$1(qc),
      jl`
        :host {
          --size: 350px;
          --actual-size: 350px;
          --min-size: 20%;
          --max-size: 80%;
          --default-content-height: 300px;
          --transition-duration: var(--duration-2);
          --opening-delay: var(--duration-2);
          --closing-delay: var(--duration-3);
          --hover-size: 18px;
          position: absolute;
          z-index: var(--z-index-drawer);
          transition: translate var(--transition-duration) var(--closing-delay);
        }

        :host(:is([bounce][position='left'])) .drawer-indicator {
          animation: bounceLeft 0.5s ease-out;
        }

        :host(:is([bounce][position='right'])) .drawer-indicator {
          animation: bounceRight 0.5s ease-out;
        }

        :host(:is([bounce][position='bottom'])) .drawer-indicator {
          animation: bounceBottom 0.5s ease-out;
        }

        :host(:is([position='left'], [position='right'])) {
          width: var(--size);
          min-width: var(--min-size);
          max-width: var(--max-size);
          top: 0;
          bottom: 0;
        }

        :host([position='left']) {
          left: calc(0px - var(--actual-size));
          translate: 0% 0%;
          padding-right: var(--hover-size);
        }

        :host([position='right']) {
          right: calc(0px - var(--actual-size));
          translate: 0% 0%;
          padding-left: var(--hover-size);
        }

        :host([position='bottom']) {
          height: var(--size);
          min-height: var(--min-size);
          max-height: var(--max-size);
          bottom: calc(0px - var(--actual-size));
          left: 0;
          right: 0;
          translate: 0% 0%;
          padding-top: var(--hover-size);
        }

        /* The visible container. Needed to have extra space for hover and resize handle outside it. */

        .container {
          display: flex;
          flex-direction: column;
          box-sizing: border-box;
          height: 100%;
          background: var(--background-color);
          -webkit-backdrop-filter: var(--surface-backdrop-filter);
          backdrop-filter: var(--surface-backdrop-filter);
          overflow-y: auto;
          overflow-x: hidden;
          box-shadow: var(--surface-box-shadow-2);
          transition:
            opacity var(--transition-duration) var(--closing-delay),
            visibility calc(var(--transition-duration) * 2) var(--closing-delay);
          opacity: 0;
          /* For accessibility (restored when open) */
          visibility: hidden;
        }

        :host([position='left']) .container {
          border-right: 1px solid var(--surface-border-color);
        }

        :host([position='right']) .container {
          border-left: 1px solid var(--surface-border-color);
        }

        :host([position='bottom']) .container {
          border-top: 1px solid var(--surface-border-color);
        }

        /* Opened state */

        :host([position='left']:is([opened], [keepopen])) {
          translate: calc(100% - var(--hover-size)) 0%;
        }
        :host([position='right']:is([opened], [keepopen])) {
          translate: calc(-100% + var(--hover-size)) 0%;
        }
        :host([position='bottom']:is([opened], [keepopen])) {
          translate: 0 calc(-100% + var(--hover-size));
        }

        :host(:is([opened], [keepopen])) {
          transition-delay: var(--opening-delay);
          z-index: var(--z-index-opened-drawer);
        }

        :host(:is([opened], [keepopen])) .container {
          transition-delay: var(--opening-delay);
          visibility: visible;
          opacity: 1;
        }

        .drawer-indicator {
          align-items: center;
          border-radius: 9999px;
          box-shadow: inset 0 0 0 1px hsl(0 0% 0% / 0.2);
          color: white;
          display: flex;
          height: 1.75rem;
          justify-content: center;
          overflow: hidden;
          opacity: 1;
          position: absolute;
          transition-delay: 0.5s;
          transition-duration: 0.2s;
          transition-property: opacity;
          width: 1.75rem;
        }

        .drawer-indicator::before {
          animation: 5s swirl linear infinite;
          animation-play-state: running;
          background-image:
            radial-gradient(circle at 50% -10%, hsl(221 100% 55% / 0.6) 0%, transparent 60%),
            radial-gradient(circle at 25% 40%, hsl(303 71% 64%) 0%, transparent 70%),
            radial-gradient(circle at 80% 10%, hsla(262, 38%, 9%, 0.5) 0%, transparent 80%),
            radial-gradient(circle at 110% 50%, hsla(147, 100%, 77%, 1) 20%, transparent 100%);
          content: '';
          inset: 0;
          opacity: 1;
          position: absolute;
          transition: opacity 0.5s;
        }

        :host([attention-required]) .drawer-indicator::before {
          background-image:
            radial-gradient(circle at 50% -10%, hsl(0deg 100% 55% / 60%) 0%, transparent 60%),
            radial-gradient(circle at 25% 40%, hsl(0deg 71% 64%) 0%, transparent 70%),
            radial-gradient(circle at 80% 10%, hsl(0deg 38% 9% / 50%) 0%, transparent 80%),
            radial-gradient(circle at 110% 50%, hsl(0deg 100% 77%) 20%, transparent 100%);
        }

        :host([opened]) .drawer-indicator {
          opacity: 0;
          transition-delay: 0s;
        }

        .drawer-indicator svg {
          height: 0.75rem;
          width: 0.75rem;
          z-index: 1;
        }

        :host([position='right']) .drawer-indicator {
          left: 0.25rem;
          top: calc(50% - 0.875rem);
        }

        :host([position='right']) .drawer-indicator svg {
          margin-inline-start: -0.625rem;
          transform: rotate(-90deg);
        }

        :host([position='left']) .drawer-indicator {
          right: 0.25rem;
          top: calc(50% - 0.875rem);
        }

        :host([position='left']) .drawer-indicator svg {
          margin-inline-end: -0.625rem;
          transform: rotate(90deg);
        }

        :host([position='bottom']) .drawer-indicator {
          left: calc(50% - 0.875rem);
          top: 0.25rem;
        }

        :host([position='bottom']) .drawer-indicator svg {
          margin-top: -0.625rem;
        }

        .resize {
          position: absolute;
          z-index: 10;
          inset: 0;
        }

        :host(:is([position='left'], [position='right'])) .resize {
          width: var(--hover-size);
          cursor: col-resize;
        }

        :host([position='left']) .resize {
          left: auto;
          right: calc(var(--hover-size) * 0.5);
        }

        :host([position='right']) .resize {
          right: auto;
          left: calc(var(--hover-size) * 0.5);
        }

        :host([position='bottom']) .resize {
          height: var(--hover-size);
          bottom: auto;
          top: calc(var(--hover-size) * 0.5);
          cursor: row-resize;
        }

        :host([resizing]) .container {
          /* vaadin-grid (used in the outline) blocks the mouse events */
          pointer-events: none;
        }

        /* Visual indication of the drawer */

        :host::before {
          content: '';
          position: absolute;
          pointer-events: none;
          z-index: -1;
          inset: var(--hover-size);
        }

        :host([document-hidden])::before {
          animation: none;
        }

        :host([document-hidden]) .drawer-indicator {
          -webkit-filter: grayscale(100%); /* Chrome, Safari, Opera */
          filter: grayscale(100%);
        }

        :host(:is([opened], [keepopen]))::before {
          transition-delay: var(--opening-delay);
          opacity: 0;
        }
      `
    ];
  }
  connectedCallback() {
    super.connectedCallback(), this.reaction(
      () => le.panels,
      () => this.requestUpdate()
    ), this.reaction(
      () => m$1.operationInProgress,
      (t) => {
        t === Ju.DragAndDrop && !this.opened && !this.keepOpen ? this.style.setProperty("pointer-events", "none") : this.style.setProperty("pointer-events", "auto");
      }
    ), this.reaction(
      () => le.getAttentionRequiredPanelConfiguration(),
      () => {
        const t = le.getAttentionRequiredPanelConfiguration();
        t && !t.floating && this.toggleAttribute($u, t.panel === this.position);
      }
    ), this.reaction(
      () => m$1.active,
      () => {
        m$1.active || (this.opened = false);
      }
    ), document.addEventListener("mouseup", this.documentMouseUpListener);
    const e = pe$1.getDrawerSize(this.position);
    e && (this.style.setProperty("--size", `${e}px`), this.setActualSize()), document.addEventListener("mousemove", this.resizingMouseMoveListener), this.addEventListener("mouseenter", this.mouseEnterListener), y$1.on("document-activation-change", (t) => {
      this.toggleAttribute("document-hidden", !t.detail.active);
    }), y$1.on("panel-expanded", this.panelExpandedListener), y$1.on("copilot-main-resized", this.setActualSize), this.reaction(
      () => le.panels.filter(
        (t) => !t.floating && t.panel === this.position
      ).length,
      () => {
        this.panelCountChanged();
      }
    ), this.addEventListener("dragleave", this.drawerDragLeaveListener), this.addEventListener("dragenter", this.drawerDragEnterListener);
  }
  firstUpdated(e) {
    super.firstUpdated(e), this.resizeElement.addEventListener("mousedown", (t) => {
      t.button === 0 && (this.resizing = true, m$1.setDrawerResizing(true), this.setAttribute("resizing", ""), y$1.emit("user-select", { allowSelection: false }));
    });
  }
  updated(e) {
    super.updated(e), e.has("opened") && this.opened && this.hasAttribute($u) && (this.removeAttribute($u), le.clearAttention());
  }
  disconnectedCallback() {
    super.disconnectedCallback(), document.removeEventListener("mousemove", this.resizingMouseMoveListener), document.removeEventListener("mouseup", this.documentMouseUpListener), this.removeEventListener("mouseenter", this.mouseEnterListener), y$1.off("panel-expanded", this.panelExpandedListener), y$1.off("copilot-main-resized", this.setActualSize), this.removeEventListener("dragleave", this.drawerDragLeaveListener), this.removeEventListener("dragenter", this.drawerDragEnterListener);
  }
  /**
   * Cleans up attributes/styles etc... for dragging operations
   * @private
   */
  cleanUpDragging() {
    this.draggingSectionPanel && (m$1.setSectionPanelDragging(false), this.draggingSectionPanel.style.zIndex = "", Array.from(this.querySelectorAll("copilot-section-panel-wrapper")).forEach((e) => {
      e.style.removeProperty("transform"), e.removeAttribute(q), e.removeAttribute(de);
    }), this.draggingSectionPanel.removeAttribute("dragging"), this.draggingSectionPanel = null);
  }
  getAllPanels() {
    return Array.from(this.querySelectorAll("copilot-section-panel-wrapper"));
  }
  getAllPanelsOrdered() {
    return this.getAllPanels().sort((e, t) => e.panelInfo && t.panelInfo ? e.panelInfo.panelOrder - t.panelInfo.panelOrder : 0);
  }
  /**
   * Closes the drawer and disables mouse enter event for a while.
   */
  forceClose() {
    this.closingForcefully = true, this.opened = false, setTimeout(() => {
      this.closingForcefully = false;
    }, 0.5);
  }
  mouseEnterListener(e) {
    if (this.closingForcefully || m$1.sectionPanelResizing)
      return;
    document.querySelector("copilot-main").shadowRoot.querySelector("copilot-drawer-panel[opened]") || (this.refreshSplit(), this.opened = true);
  }
  render() {
    return ee`
      <div class="container">
        <slot></slot>
      </div>
      <div class="resize"></div>
      <div class="drawer-indicator">${o.chevronUp}</div>
    `;
  }
  refreshSplit() {
    Mt(this.getAllPanelsOrdered(), this);
  }
};
B([
  h({ reflect: true, attribute: true })
], M.prototype, "position", 2);
B([
  h({ reflect: true, type: Boolean })
], M.prototype, "opened", 2);
B([
  h({ reflect: true, type: Boolean })
], M.prototype, "keepOpen", 2);
B([
  f(".container")
], M.prototype, "container", 2);
B([
  f(".resize")
], M.prototype, "resizeElement", 2);
M = B([
  Vl("copilot-drawer-panel")
], M);
var Ht = Object.defineProperty, Ut = Object.getOwnPropertyDescriptor, Ae = (e, t, i, n) => {
  for (var o2 = n > 1 ? void 0 : n ? Ut(t, i) : t, s = e.length - 1, a; s >= 0; s--)
    (a = e[s]) && (o2 = (n ? a(t, i, o2) : a(o2)) || o2);
  return n && o2 && Ht(t, i, o2), o2;
};
let te = class extends ht {
  constructor() {
    super(...arguments), this.checked = false, this.documentMouseDownEventClickListener = (e) => {
      const t = yl([this]);
      _l(t, e.x, e.y) || (this.inputElement.blur(), document.removeEventListener("mousedown", this.documentMouseDownEventClickListener, { capture: true }));
    };
  }
  static get styles() {
    return jl`
      .switch {
        display: inline-flex;
        gap: var(--space-100);
        padding-block: calc((var(--size-m) - var(--line-height-1)) / 2);
        position: relative;
      }

      .switch input {
        opacity: 0;
        position: absolute;
      }

      .slider {
        align-items: center;
        border: 2px solid var(--secondary-text-color);
        border-radius: 9999px;
        box-sizing: border-box;
        display: flex;
        flex-shrink: 0;
        height: 1.125rem;
        transition: 200ms;
        width: 1.75rem;
      }

      .slider::before {
        background: var(--secondary-text-color);
        border-radius: 9999px;
        content: '';
        display: flex;
        height: 0.5rem;
        transition: 200ms;
        translate: 0.1875rem 0;
        width: 0.5rem;
      }

      input:focus + .slider {
        outline: 2px solid var(--focus-color);
        outline-offset: 1px;
      }

      input:checked + .slider {
        background: var(--blue-11);
        border-color: var(--blue-11);
      }

      input:checked + .slider::before {
        background: var(--blue-5);
        height: 0.75rem;
        translate: 0.6875rem 0;
        width: 0.75rem;
      }
    `;
  }
  /*
    TODO: We should refactor to use button instead.
     <label for="xxx-switch">Edit Mode</label>
     <button aria-checked="false" id="xxx-switch" role="switch" type="button">
  */
  render() {
    return ee`
      <label class="switch">
        <input
          class="feature-toggle"
          id="feature-toggle-${this.id}"
          type="checkbox"
          @focusin="${() => {
      document.addEventListener("mousedown", this.documentMouseDownEventClickListener, { capture: true });
    }}"
          ?checked="${this.checked}"
          @change=${(e) => {
      e.preventDefault(), this.checked = e.target.checked, this.dispatchEvent(new CustomEvent("on-change"));
    }} />
        <span aria-hidden="true" class="slider"></span>
        ${this.title}
      </label>
    `;
  }
  //  @change=${(e: InputEvent) => this.toggleFeatureFlag(e, feature)}
};
Ae([
  f("input.feature-toggle")
], te.prototype, "inputElement", 2);
Ae([
  h({ reflect: true, type: Boolean })
], te.prototype, "checked", 2);
te = Ae([
  Vl("copilot-toggle-button")
], te);
class jt {
  constructor() {
    this.offsetX = 0, this.offsetY = 0;
  }
  draggingStarts(t, i) {
    this.offsetX = i.clientX - t.getBoundingClientRect().left, this.offsetY = i.clientY - t.getBoundingClientRect().top;
  }
  dragging(t, i) {
    const n = i.clientX, o2 = i.clientY, s = n - this.offsetX, a = n - this.offsetX + t.getBoundingClientRect().width, u = o2 - this.offsetY, m = o2 - this.offsetY + t.getBoundingClientRect().height;
    return this.adjust(t, s, u, a, m);
  }
  adjust(t, i, n, o2, s) {
    let a, u, m, P;
    const L = document.documentElement.getBoundingClientRect().width, le2 = document.documentElement.getBoundingClientRect().height;
    return (o2 + i) / 2 < L / 2 ? (t.style.setProperty("--left", `${i}px`), t.style.setProperty("--right", ""), P = void 0, a = Math.max(0, i)) : (t.style.removeProperty("--left"), t.style.setProperty("--right", `${L - o2}px`), a = void 0, P = Math.max(0, L - o2)), (n + s) / 2 < le2 / 2 ? (u = Math.max(0, n), t.style.setProperty("--top", `${u}px`), t.style.setProperty("--bottom", ""), m = void 0) : (t.style.setProperty("--top", ""), t.style.setProperty("--bottom", `${le2 - s}px`), u = void 0, m = Math.max(0, le2 - s)), {
      left: a,
      right: P,
      top: u,
      bottom: m
    };
  }
  anchor(t) {
    const { left: i, top: n, bottom: o2, right: s } = t.getBoundingClientRect();
    return this.adjust(t, i, n, s, o2);
  }
  anchorLeftTop(t) {
    const { left: i, top: n } = t.getBoundingClientRect();
    return t.style.setProperty("--left", `${i}px`), t.style.setProperty("--right", ""), t.style.setProperty("--top", `${n}px`), t.style.setProperty("--bottom", ""), {
      left: i,
      top: n
    };
  }
}
const A = new jt();
var Nt = Object.defineProperty, Bt = Object.getOwnPropertyDescriptor, V = (e, t, i, n) => {
  for (var o2 = n > 1 ? void 0 : n ? Bt(t, i) : t, s = e.length - 1, a; s >= 0; s--)
    (a = e[s]) && (o2 = (n ? a(t, i, o2) : a(o2)) || o2);
  return n && o2 && Nt(t, i, o2), o2;
};
const De = "https://github.com/JetBrains/JetBrainsRuntime/releases";
function Vt(e, t) {
  if (!t)
    return true;
  const [i, n, o2] = t.split(".").map((m) => parseInt(m)), [s, a, u] = e.split(".").map((m) => parseInt(m));
  if (i < s)
    return true;
  if (i === s) {
    if (n < a)
      return true;
    if (n === a)
      return o2 < u;
  }
  return false;
}
const Le = "Download complete";
let I = class extends lc {
  constructor() {
    super(), this.javaPluginSectionOpened = false, this.hotswapSectionOpened = false, this.hotswapTab = "hotswapagent", this.downloadStatusMessages = [], this.downloadProgress = 0, this.onDownloadStatusUpdate = this.downloadStatusUpdate.bind(this), this.handleESC = (e) => {
      m$1.active && e.key === "Escape" && le.updatePanel(ie.tag, { floating: false });
    }, this.reaction(
      () => [yn.jdkInfo, m$1.idePluginState],
      () => {
        m$1.idePluginState && (!m$1.idePluginState.ide || !m$1.idePluginState.active ? this.javaPluginSectionOpened = true : (!(/* @__PURE__ */ new Set(["vscode", "intellij"])).has(m$1.idePluginState.ide) || !m$1.idePluginState.active) && (this.javaPluginSectionOpened = false)), yn.jdkInfo && Bo() !== "success" && (this.hotswapSectionOpened = true);
      },
      { fireImmediately: true }
    );
  }
  connectedCallback() {
    super.connectedCallback(), y$1.on("set-up-vs-code-hotswap-status", this.onDownloadStatusUpdate), this.addESCListener();
  }
  disconnectedCallback() {
    super.disconnectedCallback(), y$1.off("set-up-vs-code-hotswap-status", this.onDownloadStatusUpdate), this.removeESCListener();
  }
  render() {
    const e = {
      intellij: m$1.idePluginState?.ide === "intellij",
      vscode: m$1.idePluginState?.ide === "vscode",
      eclipse: m$1.idePluginState?.ide === "eclipse",
      idePluginInstalled: !!m$1.idePluginState?.active
    };
    return ee`
      <div part="container">${this.renderPluginSection(e)} ${this.renderHotswapSection(e)}</div>
      <div part="footer">
        <vaadin-button
          id="close"
          @click="${() => le.updatePanel(ie.tag, { floating: false })}"
          >Close
        </vaadin-button>
      </div>
    `;
  }
  renderPluginSection(e) {
    let t = "";
    e.intellij ? t = "IntelliJ" : e.vscode ? t = "VS Code" : e.eclipse && (t = "Eclipse");
    let i, n;
    e.vscode || e.intellij ? e.idePluginInstalled ? (i = `Plugin for ${t} installed`, n = this.renderPluginInstalledContent()) : (i = `Plugin for ${t} not installed`, n = this.renderPluginIsNotInstalledContent(e)) : e.eclipse ? (i = e.idePluginInstalled ? "Eclipse plugin installed" : "Eclipse plugin not installed", n = e.idePluginInstalled ? this.renderPluginInstalledContent() : this.renderEclipsePluginContent()) : (i = "No IDE found", n = this.renderNoIdePluginContent());
    const o$1 = e.idePluginInstalled ? o.checkCircle : o.alertTriangle;
    return ee`
      <details
        part="panel"
        .open=${this.javaPluginSectionOpened}
        @toggle=${(s) => {
      Cu(() => {
        this.javaPluginSectionOpened = s.target.open;
      });
    }}>
        <summary part="header">
          <span class="icon ${e.idePluginInstalled ? "success" : "warning"}">${o$1}</span>
          <div>${i}</div>
        </summary>
        <div part="content">${n}</div>
      </details>
    `;
  }
  renderNoIdePluginContent() {
    return ee`
      <div>
        <div>We could not detect an IDE</div>
        ${this.recommendSupportedPlugin()}
      </div>
    `;
  }
  renderEclipsePluginContent() {
    return ee`
      <div>
        <div>Install the Vaadin Eclipse Plugin to ensure a smooth development workflow</div>
        <p>
          Installing the plugin is not required, but strongly recommended.<br />Some Vaadin Copilot functionality, such
          as undo, will not function optimally without the plugin.
        </p>
        <div>
          <vaadin-button
            @click="${() => {
      window.open(Tu, "_blank");
    }}"
            >Install from Eclipse Marketplace
            <vaadin-icon icon="vaadin:external-link"></vaadin-icon>
          </vaadin-button>
        </div>
      </div>
    `;
  }
  recommendSupportedPlugin() {
    return ee`<div>
      Please use <a href="https://code.visualstudio.com">Visual Studio Code</a> or
      <a href="https://www.jetbrains.com/idea">IntelliJ IDEA</a> for better development experience
    </div>`;
  }
  renderPluginInstalledContent() {
    return ee` <div>You have a running plugin. Enjoy your awesome development workflow!</div> `;
  }
  renderPluginIsNotInstalledContent(e) {
    let t = null, i = "Install from Marketplace";
    return e.intellij ? (t = Du, i = "Install from JetBrains Marketplace") : e.vscode ? (t = ku, i = "Install from VSCode Marketplace") : e.eclipse && (t = Tu, i = "Install from Eclipse Marketplace"), ee`
      <div>
        <div>Install the Vaadin IDE Plugin to ensure a smooth development workflow</div>
        <p>
          Installing the plugin is not required, but strongly recommended.<br />Some Vaadin Copilot functionality, such
          as undo, will not function optimally without the plugin.
        </p>
        ${t ? ee` <div>
              <vaadin-button
                @click="${() => {
      window.open(t, "_blank");
    }}"
                >${i}
                <vaadin-icon icon="vaadin:external-link"></vaadin-icon>
              </vaadin-button>
            </div>` : A$1}
      </div>
    `;
  }
  renderHotswapSection(e) {
    const { jdkInfo: t } = yn;
    if (!t)
      return A$1;
    const i = Bo(), n = od();
    let o$1, s, a;
    return i === "success" ? (o$1 = o.checkCircle, a = "Java Hotswap is enabled") : i === "warning" ? (o$1 = o.alertTriangle, a = "Java Hotswap is not enabled") : i === "error" && (o$1 = o.alertTriangle, a = "Java Hotswap is partially enabled"), this.hotswapTab === "jrebel" ? t.jrebel ? s = this.renderJRebelInstalledContent() : s = this.renderJRebelNotInstalledContent() : e.intellij ? s = this.renderHotswapAgentPluginContent(this.renderIntelliJHotswapHint) : e.vscode ? s = this.renderHotswapAgentPluginContent(this.renderVSCodeHotswapHint) : s = this.renderHotswapAgentNotInstalledContent(e), ee` <details
      part="panel"
      .open=${this.hotswapSectionOpened}
      @toggle=${(u) => {
      Cu(() => {
        this.hotswapSectionOpened = u.target.open;
      });
    }}>
      <summary part="header">
        <span class="icon ${i}">${o$1}</span>
        <div>${a}</div>
      </summary>
      <div part="content">
        ${n !== "none" ? ee`${n === "jrebel" ? this.renderJRebelInstalledContent() : this.renderHotswapAgentInstalledContent()}` : ee`
            <div class="tabs" role="tablist">
              <button
                aria-selected="${this.hotswapTab === "hotswapagent" ? "true" : "false"}"
                class="tab"
                role="tab"
                @click=${() => {
      this.hotswapTab = "hotswapagent";
    }}>
                Hotswap Agent
              </button>
              <button
                aria-selected="${this.hotswapTab === "jrebel" ? "true" : "false"}"
                class="tab"
                role="tab"
                @click=${() => {
      this.hotswapTab = "jrebel";
    }}>
                JRebel
              </button>
            </div>
            <div part="content">${s}</div>
            </div>
            </details>
          `}
      </div>
    </details>`;
  }
  renderJRebelNotInstalledContent() {
    return ee`
      <div>
        <a href="https://www.jrebel.com">JRebel ${o.share}</a> is a commercial hotswap solution. Vaadin detects the
        JRebel Agent and automatically reloads the application in the browser after the Java changes have been
        hotpatched.
        <p>
          Go to
          <a href="https://www.jrebel.com/products/jrebel/learn" target="_blank" rel="noopener noreferrer">
            https://www.jrebel.com/products/jrebel/learn ${o.share}</a
          >
          to get started
        </p>
      </div>
    `;
  }
  renderHotswapAgentNotInstalledContent(e) {
    const t = [
      this.renderJavaRunningInDebugModeSection(),
      this.renderHotswapAgentJdkSection(e),
      this.renderInstallHotswapAgentJdkSection(e),
      this.renderHotswapAgentVersionSection(),
      this.renderHotswapAgentMissingArgParam(e)
    ];
    return ee` <div part="hotswap-agent-section-container">${t}</div> `;
  }
  renderHotswapAgentPluginContent(e) {
    const i = Bo() === "success";
    return ee`
      <div part="hotswap-agent-section-container">
        <div class="inner-section">
          <span class="hotswap icon ${i ? "success" : "warning"}"
            >${i ? o.checkCircle : o.alertTriangle}</span
          >
          ${e()}
        </div>
      </div>
    `;
  }
  renderIntelliJHotswapHint() {
    return ee` <div class="hint">
      <h3>Use 'Debug using Hotswap Agent' launch configuration</h3>
      Vaadin IntelliJ plugin offers launch mode that does not require any manual configuration!
      <p>
        In order to run recommended launch configuration, you should click three dots right next to Debug button and
        select <code>Debug using Hotswap Agent</code> option.
      </p>
    </div>`;
  }
  renderVSCodeHotswapHint() {
    return ee` <div class="hint">
      <h3>Use 'Debug (hotswap)'</h3>
      With Vaadin Visual Studio Code extension you can run Hotswap Agent without any manual configuration required!
      <p>Click <code>Debug (hotswap)</code> within your main class to debug application using Hotswap Agent.</p>
    </div>`;
  }
  renderJavaRunningInDebugModeSection() {
    const e = yn.jdkInfo?.runningInJavaDebugMode;
    return ee`
      <div class="inner-section">
        <details class="inner" .open="${!e}">
          <summary>
            <span class="icon ${e ? "success" : "warning"}"
              >${e ? o.checkCircle : o.alertTriangle}</span
            >
            <div>Run Java in debug mode</div>
          </summary>
          <div class="hint">Start the application in debug mode in the IDE</div>
        </details>
      </div>
    `;
  }
  renderHotswapAgentMissingArgParam(e) {
    const t = yn.jdkInfo?.runningWitHotswap && yn.jdkInfo?.runningWithExtendClassDef;
    return ee`
      <div class="inner-section">
        <details class="inner" .open="${!t}">
          <summary>
            <span class="icon ${t ? "success" : "warning"}"
              >${t ? o.checkCircle : o.alertTriangle}</span
            >
            <div>Enable HotswapAgent</div>
          </summary>
          <div class="hint">
            <ul>
              ${e.intellij ? ee`<li>Launch as mentioned in the previous step</li>` : A$1}
              ${e.intellij ? ee`<li>
                    To manually configure IntelliJ, add the
                    <code>-XX:HotswapAgent=fatjar -XX:+AllowEnhancedClassRedefinition -XX:+UpdateClasses</code> JVM
                    arguments when launching the application
                  </li>` : ee`<li>
                    Add the
                    <code>-XX:HotswapAgent=fatjar -XX:+AllowEnhancedClassRedefinition -XX:+UpdateClasses</code> JVM
                    arguments when launching the application
                  </li>`}
            </ul>
          </div>
        </details>
      </div>
    `;
  }
  renderHotswapAgentJdkSection(e) {
    const t = yn.jdkInfo?.extendedClassDefCapable, i = this.downloadStatusMessages?.[this.downloadStatusMessages.length - 1] === Le;
    return ee`
      <div class="inner-section">
        <details class="inner" .open="${!t}">
          <summary>
            <span class="icon ${t ? "success" : "warning"}"
              >${t ? o.checkCircle : o.alertTriangle}</span
            >
            <div>Run using JetBrains Runtime JDK</div>
          </summary>
          <div class="hint">
            JetBrains Runtime provides much better hotswapping compared to other JDKs.
            <ul>
              ${e.intellij && Vt("1.3.0", m$1.idePluginState?.version) ? ee` <li>Upgrade to the latest IntelliJ plugin</li>` : A$1}
              ${e.intellij ? ee` <li>Launch the application in IntelliJ using "Debug using Hotswap Agent"</li>` : A$1}
              ${e.vscode ? ee` <li>
                    <a href @click="${(n) => this.downloadJetbrainsRuntime(n)}"
                      >Let Copilot download and set up JetBrains Runtime for VS Code</a
                    >
                    ${this.downloadProgress > 0 ? ee`<vaadin-progress-bar
                          .value="${this.downloadProgress}"
                          min="0"
                          max="1"></vaadin-progress-bar>` : A$1}
                    <ul>
                      ${this.downloadStatusMessages.map((n) => ee`<li>${n}</li>`)}
                      ${i ? ee`<h3>Go to VS Code and launch the 'Debug using Hotswap Agent' configuration</h3>` : A$1}
                    </ul>
                  </li>` : A$1}
              <li>
                ${e.intellij || e.vscode ? ee`If there is a problem, you can manually
                      <a target="_blank" href="${De}">download JetBrains Runtime JDK</a> and set up
                      your debug configuration to use it.` : ee`<a target="_blank" href="${De}">Download JetBrains Runtime JDK</a> and set up
                      your debug configuration to use it.`}
              </li>
            </ul>
          </div>
        </details>
      </div>
    `;
  }
  renderInstallHotswapAgentJdkSection(e) {
    const t = yn.jdkInfo?.hotswapAgentFound, i = yn.jdkInfo?.extendedClassDefCapable;
    return ee`
      <div class="inner-section">
        <details class="inner" .open="${!t}">
          <summary>
            <span class="icon ${t ? "success" : "warning"}"
              >${t ? o.checkCircle : o.alertTriangle}</span
            >
            <div>Install HotswapAgent</div>
          </summary>
          <div class="hint">
            Hotswap Agent provides application level support for hot reloading, such as reinitalizing Vaadin @Route or
            @BrowserCallable classes when they are updated
            <ul>
              ${e.intellij ? ee`<li>Launch as mentioned in the previous step</li>` : A$1}
              ${!e.intellij && !i ? ee`<li>First install JetBrains Runtime as mentioned in the step above.</li>` : A$1}
              ${e.intellij ? ee`<li>
                    To manually configure IntelliJ, download HotswapAgent and install the jar file as
                    <code>[JAVA_HOME]/lib/hotswap/hotswap-agent.jar</code> in the JetBrains Runtime JDK. Note that the
                    file must be renamed to exactly match this path.
                  </li>` : ee`<li>
                    Download HotswapAgent and install the jar file as
                    <code>[JAVA_HOME]/lib/hotswap/hotswap-agent.jar</code> in the JetBrains Runtime JDK. Note that the
                    file must be renamed to exactly match this path.
                  </li>`}
            </ul>
          </div>
        </details>
      </div>
    `;
  }
  renderHotswapAgentVersionSection() {
    if (!yn.jdkInfo?.hotswapAgentFound)
      return A$1;
    const e = yn.jdkInfo?.hotswapVersionOk, t = yn.jdkInfo?.hotswapVersion, i = yn.jdkInfo?.hotswapAgentLocation;
    return ee`
      <div class="inner-section">
        <details class="inner" .open="${!e}">
          <summary>
            <span class="icon ${e ? "success" : "warning"}"
              >${e ? o.checkCircle : o.alertTriangle}</span
            >
            <div>Hotswap version requires update</div>
          </summary>
          <div class="hint">
            HotswapAgent version ${t} is in use
            <a target="_blank" href="https://github.com/HotswapProjects/HotswapAgent/releases"
              >Download the latest HotswapAgent</a
            >
            and place it in <code>${i}</code>
          </div>
        </details>
      </div>
    `;
  }
  renderJRebelInstalledContent() {
    return ee` <div>JRebel is in use. Enjoy your awesome development workflow!</div> `;
  }
  renderHotswapAgentInstalledContent() {
    return ee` <div>Hotswap agent is in use. Enjoy your awesome development workflow!</div> `;
  }
  async downloadJetbrainsRuntime(e) {
    return e.target.disabled = true, e.preventDefault(), this.downloadStatusMessages = [], gn(`${Ve}set-up-vs-code-hotswap`, {}, (t) => {
      t.data.error ? (ce$1("Error downloading JetBrains runtime", t.data.error), this.downloadStatusMessages = [...this.downloadStatusMessages, "Download failed"]) : this.downloadStatusMessages = [...this.downloadStatusMessages, Le];
    });
  }
  downloadStatusUpdate(e) {
    const t = e.detail.progress;
    t ? this.downloadProgress = t : this.downloadStatusMessages = [...this.downloadStatusMessages, e.detail.message];
  }
  addESCListener() {
    document.addEventListener("keydown", this.handleESC);
  }
  removeESCListener() {
    document.removeEventListener("keydown", this.handleESC);
  }
};
I.NAME = "copilot-development-setup-user-guide";
I.styles = jl`
    :host {
      --icon-size: 24px;
      --summary-header-gap: 10px;
      --footer-height: calc(50px + var(--space-150));
      color: var(--color);
    }
    :host code {
      background-color: var(--gray-50);
      font-size: var(--font-size-0);
      display: inline-block;
      margin-top: var(--space-100);
      margin-bottom: var(--space-100);
      user-select: all;
    }

    [part='container'] {
      display: flex;
      flex-direction: column;
      gap: var(--space-150);
      padding: var(--space-150);
      box-sizing: border-box;
      height: calc(100% - var(--footer-height));
      overflow: auto;
    }

    [part='footer'] {
      display: flex;
      justify-content: flex-end;
      height: var(--footer-height);
      padding-left: var(--space-150);
      padding-right: var(--space-150);
    }
    [part='hotswap-agent-section-container'] {
      display: flex;
      flex-direction: column;
      gap: var(--space-100);
      position: relative;
    }
    [part='content'] {
      display: flex;
      padding: var(--space-150);
      flex-direction: column;
    }
    div.inner-section div.hint {
      margin-left: calc(var(--summary-header-gap) + var(--icon-size));
      margin-top: var(--space-75);
    }
    details {
      display: flex;
      flex-direction: column;
      box-sizing: border-box;

      & span.icon {
        display: flex;
      }
      & span.icon.warning {
        color: var(--warning-color);
      }
      & span.icon.success {
        color: var(--success-color);
      }
      & span.hotswap.icon {
        position: absolute;
        top: var(--space-75);
        left: var(--space-75);
      }
      & > summary,
      summary::part(header) {
        display: flex;
        flex-direction: row;
        align-items: center;
        cursor: pointer;
        position: relative;
        gap: var(--summary-header-gap);
        font: var(--font);
        font-size: var(--font-size-2);
      }
      summary::after,
      summary::part(header)::after {
        content: '';
        display: block;
        width: 4px;
        height: 4px;
        border-color: var(--color);
        opacity: var(--panel-toggle-opacity, 0.2);
        border-width: 2px;
        border-style: solid solid none none;
        transform: rotate(var(--panel-toggle-angle, -45deg));
        position: absolute;
        right: 15px;
        top: calc(50% - var(--panel-toggle-offset, 2px));
      }
      &:not([open]) {
        --panel-toggle-angle: 135deg;
        --panel-toggle-offset: 4px;
      }
    }
    details[part='panel'] {
      background: var(--card-bg);
      border: var(--card-border);
      border-radius: 4px;
      user-select: none;

      &:has(summary:hover) {
        border-color: var(--accent-color);
      }

      & > summary,
      summary::part(header) {
        padding: 10px 10px;
        padding-right: 25px;
      }

      summary:hover,
      summary::part(header):hover {
        --panel-toggle-opacity: 0.5;
      }

      input[type='checkbox'],
      summary::part(checkbox) {
        margin: 0;
      }

      &:not([open]):hover {
        background: var(--card-hover-bg);
      }

      &[open] {
        background: var(--card-open-bg);
        box-shadow: var(--card-open-shadow);

        & > summary {
          font-weight: bold;
        }
      }
      .tabs {
        border-bottom: 1px solid var(--border-color);
        box-sizing: border-box;
        display: flex;
        height: 2.25rem;
      }

      .tab {
        background: none;
        border: none;
        border-bottom: 1px solid transparent;
        color: var(--color);
        font: var(--font-button);
        height: 2.25rem;
        padding: 0 0.75rem;
      }

      .tab[aria-selected='true'] {
        color: var(--color-high-contrast);
        border-bottom-color: var(--color-high-contrast);
      }

      .tab-content {
        flex: 1 1 auto;
        gap: var(--space-150);
        overflow: auto;
        padding: var(--space-150);
      }

      h3 {
        margin-top: 0;
      }
    }
  `;
V([
  b()
], I.prototype, "javaPluginSectionOpened", 2);
V([
  b()
], I.prototype, "hotswapSectionOpened", 2);
V([
  b()
], I.prototype, "hotswapTab", 2);
V([
  b()
], I.prototype, "downloadStatusMessages", 2);
V([
  b()
], I.prototype, "downloadProgress", 2);
I = V([
  Vl(I.NAME)
], I);
const ie = Xu({
  header: "Development Workflow",
  tag: su,
  width: 800,
  height: 800,
  floatingPosition: {
    top: 50,
    left: 50
  },
  individual: true
}), Ft = {
  init(e) {
    e.addPanel(ie);
  }
};
window.Vaadin.copilot.plugins.push(Ft);
le.addPanel(ie);
var qt = (e, t, i, n) => {
  for (var o2 = t, s = e.length - 1, a; s >= 0; s--)
    (a = e[s]) && (o2 = a(o2) || o2);
  return o2;
};
let ze = class extends lc {
  createRenderRoot() {
    return this;
  }
  connectedCallback() {
    super.connectedCallback(), this.classList.add("custom-menu-item");
  }
  render() {
    const t = ad(), i = t.status === "warning" || t.status === "error";
    return ee`
      <div aria-hidden="true" class="prefix ${i ? t.status : ""}">${o.lightning}</div>
      <div class="content">
        <span class="label">Development Workflow</span>
        <span class="status ${i ? t.status : ""}">${t.message}</span>
      </div>
      <div aria-hidden="true" class="suffix">
        ${i ? ee`<div class="dot ${t.status}"></div>` : A$1}
      </div>
    `;
  }
};
ze = qt([
  Vl("copilot-activation-button-development-workflow")
], ze);
var Gt = (e, t, i, n) => {
  for (var o2 = t, s = e.length - 1, a; s >= 0; s--)
    (a = e[s]) && (o2 = a(o2) || o2);
  return o2;
};
let Me = class extends lc {
  constructor() {
    super(), this.reaction(
      () => m$1.userInfo,
      () => {
        this.requestUpdate();
      }
    );
  }
  createRenderRoot() {
    return this;
  }
  connectedCallback() {
    super.connectedCallback(), this.classList.add("custom-menu-item"), this.addEventListener("click", this.clickListener);
  }
  disconnectedCallback() {
    super.disconnectedCallback(), this.removeEventListener("click", this.clickListener);
  }
  render() {
    const e = this.getStatus();
    return ee`
      <div class="prefix">${this.renderPortrait()}</div>
      <div class="content">
        <span class="label"> ${this.getUsername()} </span>
        ${e ? ee` <span class="warning"> ${e} </span> ` : A$1}
      </div>
      <div aria-hidden="true" class="suffix">${this.renderDot()}</div>
    `;
  }
  clickListener() {
    if (m$1.userInfo?.validLicense) {
      window.open("https://vaadin.com/myaccount", "_blank", "noopener");
      return;
    }
    if (ko.active) {
      gn(`${Ve}log-in`, {}, (e) => {
        window.open(e.data.loginUrl, "_blank");
      }).catch((e) => ce$1("Login processing failed", e));
      return;
    }
    m$1.setLoginCheckActive(true);
  }
  getUsername() {
    return m$1.userInfo?.firstName ? `${m$1.userInfo.firstName} ${m$1.userInfo.lastName}` : "Log in";
  }
  getStatus() {
    if (m$1.userInfo?.validLicense)
      return m$1.userInfo?.copilotProjectCannotLeaveLocalhost ? "AI Disabled" : void 0;
    if (ko.active) {
      const e = Math.round(ko.remainingTimeInMillis / 864e5);
      return `Trial expires in ${e}${e === 1 ? " day" : " days"}`;
    }
    if (ko.expired && !m$1.userInfo?.validLicense)
      return "Trial expired";
    if (!ko.active && !ko.expired && !m$1.userInfo?.validLicense)
      return "No valid license available";
  }
  renderPortrait() {
    return m$1.userInfo?.portraitUrl ? ee`<div
        class="portrait"
        style="background-image: url('https://vaadin.com${m$1.userInfo.portraitUrl}')"></div>` : A$1;
  }
  renderDot() {
    return m$1.userInfo?.validLicense ? A$1 : ko.active || ko.expired ? ee`<div class="dot warning"></div>` : A$1;
  }
};
Me = Gt([
  Vl("copilot-activation-button-user-info")
], Me);
var Yt = (e, t, i, n) => {
  for (var o2 = t, s = e.length - 1, a; s >= 0; s--)
    (a = e[s]) && (o2 = a(o2) || o2);
  return o2;
};
function Kt() {
  le.updatePanel("copilot-feedback-panel", {
    floating: true
  }), tt$1.setFeedbackDisplayedAtLeastOnce(true);
}
let _e = class extends lc {
  constructor() {
    super(), this.reaction(
      () => tt$1.isFeedbackDisplayedAtLeastOnce(),
      () => {
        this.requestUpdate();
      }
    );
  }
  createRenderRoot() {
    return this;
  }
  connectedCallback() {
    super.connectedCallback(), this.classList.add("custom-menu-item");
  }
  render() {
    return ee`
      <div aria-hidden="true" class="prefix">${o.annotation}</div>
      <div class="content" style="display: flex; flex-direction: column;">
        <span class="label">Tell Us What You Think</span>
        <span class="hint">Give feedback or report an issue</span>
      </div>
      <div aria-hidden="true" class="suffix">
        ${tt$1.isFeedbackDisplayedAtLeastOnce() ? A$1 : ee`<div class="dot info"></div>`}
      </div>
    `;
  }
};
_e = Yt([
  Vl("copilot-activation-button-feedback")
], _e);
function v(e) {
  return Ke("vaadin-menu-bar-item", e);
}
function ce(e) {
  return Ke("vaadin-context-menu-item", e);
}
function Ke(e, t) {
  const i = document.createElement(e);
  if (t.style && (i.className = t.style), t.icon)
    if (typeof t.icon == "string") {
      const n = document.createElement("vaadin-icon");
      n.setAttribute("icon", t.icon), i.append(n);
    } else
      i.append(Oe(t.icon.strings[0]));
  if (t.label) {
    const n = document.createElement("span");
    n.className = "label", n.innerHTML = t.label, i.append(n);
  } else if (t.component) {
    const n = Uu(t.component) ? t.component : document.createElement(t.component);
    i.append(n);
  }
  if (t.description) {
    const n = document.createElement("span");
    n.className = "desc", n.innerHTML = t.description, i.append(n);
  }
  if (t.hint) {
    const n = document.createElement("span");
    n.className = "hint", n.innerHTML = t.hint, i.append(n);
  }
  if (t.suffix)
    if (typeof t.suffix == "string") {
      const n = document.createElement("span");
      n.innerHTML = t.suffix, i.append(n);
    } else
      i.append(Oe(t.suffix.strings[0]));
  return i;
}
function Oe(e) {
  if (!e) return null;
  const t = document.createElement("template");
  t.innerHTML = e;
  const i = t.content.children;
  return i.length === 1 ? i[0] : i;
}
function Ze(e) {
  return gn("copilot-switch-user", { username: e }, (t) => t.data.error ? (Do({ type: Re$1.ERROR, message: "Unable to switch user", details: t.data.error.message }), false) : true);
}
var Zt = Object.defineProperty, Qt = Object.getOwnPropertyDescriptor, F = (e, t, i, n) => {
  for (var o2 = n > 1 ? void 0 : n ? Qt(t, i) : t, s = e.length - 1, a; s >= 0; s--)
    (a = e[s]) && (o2 = (n ? a(t, i, o2) : a(o2)) || o2);
  return n && o2 && Zt(t, i, o2), o2;
};
const ei = 8;
function ti() {
  const e = document.createElement("vaadin-text-field");
  return e.label = "Username to Switch To", e.style.width = "100%", e.autocomplete = "off", e.addEventListener("click", async (t) => {
    t.stopPropagation();
  }), e.addEventListener("keydown", async (t) => {
    if (t.stopPropagation(), t.key === "Enter") {
      const i = e.value;
      await Ze(i) && (tt$1.addRecentSwitchedUsername(i), window.location.reload());
    }
  }), e;
}
let fe = class extends lc {
  constructor() {
    super(...arguments), this.username = "";
  }
  connectedCallback() {
    super.connectedCallback(), this.style.display = "contents";
  }
  render() {
    return ee`<span style="flex: 1;  display: flex; justify-content: space-between;"
      ><span>${this.username}</span
      ><span
        @click=${(e) => {
      tt$1.removeRecentSwitchedUsername(this.username), e.stopPropagation();
      const t = this.parentElement;
      if (t.style.display = "none", tt$1.getRecentSwitchedUsernames().length === 0) {
        const i = t.parentElement?.firstElementChild;
        i && (i.style.display = "none");
      }
    }}
        >${o.trash}</span
      ></span
    >`;
  }
};
F([
  h({ type: String })
], fe.prototype, "username", 2);
fe = F([
  Vl("copilot-switch-user")
], fe);
function ii(e) {
  const t = document.createElement("copilot-switch-user");
  return t.username = e, t;
}
let G = class extends lc {
  constructor() {
    super(...arguments), this.initialMouseDownPosition = null, this.dragging = false, this.items = [], this.mouseDownListener = (e) => {
      this.initialMouseDownPosition = { x: e.clientX, y: e.clientY }, A.draggingStarts(this, e), document.addEventListener("mousemove", this.documentDraggingMouseMoveEventListener);
    }, this.documentDraggingMouseMoveEventListener = (e) => {
      if (this.initialMouseDownPosition && !this.dragging) {
        const { clientX: t, clientY: i } = e;
        this.dragging = Math.abs(t - this.initialMouseDownPosition.x) + Math.abs(i - this.initialMouseDownPosition.y) > ei;
      }
      this.dragging && (this.setOverlayVisibility(false), A.dragging(this, e));
    }, this.documentMouseUpListener = (e) => {
      if (this.initialMouseDownPosition = null, document.removeEventListener("mousemove", this.documentDraggingMouseMoveEventListener), this.dragging) {
        const t = A.dragging(this, e);
        tt$1.setActivationButtonPosition(t), this.setOverlayVisibility(true);
      } else
        this.setMenuBarOnClick();
      this.postDragReset(e.composed ? e.composedPath() : []);
    }, this.postDragReset = ou((e) => {
      this.dragging = false, this.closeMenuIfMouseTargetIsOutsideOfActivationButton(e);
    }, 100), this.closeMenuMouseMoveListener = (e) => {
      e.composed && (this.dragging || this.closeMenuIfMouseTargetIsOutsideOfActivationButton(e.composedPath()));
    }, this.closeMenuIfMouseTargetIsOutsideOfActivationButton = (e) => {
      e.some((i) => {
        if (i instanceof HTMLElement) {
          const n = i;
          if (n.localName === this.localName || n.localName === "vaadin-menu-bar-overlay" && n.classList.contains("activation-button-menu"))
            return true;
        }
        return false;
      }) ? this.closeMenuWithDebounce.clear() : this.closeMenuWithDebounce();
    }, this.closeMenuWithDebounce = ou(() => {
      this.closeMenu();
    }, 250), this.dispatchSpotlightActivationEvent = (e) => {
      this.dispatchEvent(
        new CustomEvent("spotlight-activation-changed", {
          detail: e
        })
      );
    }, this.activationBtnClicked = (e) => {
      if (this.dragging) {
        e?.preventDefault();
        return;
      }
      if (m$1.active && this.handleAttentionRequiredOnClick()) {
        e?.stopPropagation(), e?.preventDefault();
        return;
      }
      e?.stopPropagation(), this.dispatchEvent(new CustomEvent("activation-btn-clicked")), requestAnimationFrame(() => {
        this.closeMenu(), this.openMenu();
      });
    }, this.handleAttentionRequiredOnClick = () => {
      const e = le.getAttentionRequiredPanelConfiguration();
      return e ? e.panel && !e.floating ? (y$1.emit("open-attention-required-drawer", null), true) : (le.clearAttention(), true) : false;
    }, this.closeMenu = () => {
      this.menubar._close();
    }, this.openMenu = () => {
      this.menubar._buttons[0].dispatchEvent(new CustomEvent("mouseover", { bubbles: true }));
    }, this.setMenuBarOnClick = () => {
      const e = this.shadowRoot.querySelector("vaadin-menu-bar-button");
      e && (e.onclick = this.activationBtnClicked);
    };
  }
  static get styles() {
    return [
      V$1(qc),
      jl`
        :host {
          --space: 8px;
          --height: 28px;
          --width: 28px;
          position: absolute;
          top: clamp(var(--space), var(--top), calc(100vh - var(--height) - var(--space)));
          left: clamp(var(--space), var(--left), calc(100vw - var(--width) - var(--space)));
          bottom: clamp(var(--space), var(--bottom), calc(100vh - var(--height) - var(--space)));
          right: clamp(var(--space), var(--right), calc(100vw - var(--width) - var(--space)));
          user-select: none;
          -ms-user-select: none;
          -moz-user-select: none;
          -webkit-user-select: none;
          --indicator-color: var(--red);
          /* Don't add a z-index or anything else that creates a stacking context */
        }

        :host .menu-button {
          min-width: unset;
        }

        :host([document-hidden]) {
          -webkit-filter: grayscale(100%); /* Chrome, Safari, Opera */
          filter: grayscale(100%);
        }

        .menu-button::part(container) {
          overflow: visible;
        }

        .menu-button vaadin-menu-bar-button {
          all: initial;
          display: block;
          position: relative;
          z-index: var(--z-index-activation-button);
          width: var(--width);
          height: var(--height);
          overflow: hidden;
          color: transparent;
          background: hsl(0 0% 0% / 0.25);
          border-radius: 8px;
          box-shadow: 0 0 0 1px hsl(0 0% 100% / 0.1);
          cursor: default;
          -webkit-backdrop-filter: blur(8px);
          backdrop-filter: blur(8px);
          transition:
            box-shadow 0.2s,
            background-color 0.2s;
        }

        /* visual effect when active */

        .menu-button vaadin-menu-bar-button::before {
          all: initial;
          content: '';
          background-image:
            radial-gradient(circle at 50% -10%, hsl(221 100% 55% / 0.6) 0%, transparent 60%),
            radial-gradient(circle at 25% 40%, hsl(303 71% 64%) 0%, transparent 70%),
            radial-gradient(circle at 80% 10%, hsla(262, 38%, 9%, 0.5) 0%, transparent 80%),
            radial-gradient(circle at 110% 50%, hsla(147, 100%, 77%, 1) 20%, transparent 100%);
          animation: 5s swirl linear infinite;
          animation-play-state: paused;
          inset: -6px;
          opacity: 0;
          position: absolute;
          transition: opacity 0.5s;
        }

        /* vaadin symbol */

        .menu-button vaadin-menu-bar-button::after {
          all: initial;
          content: '';
          position: absolute;
          inset: 1px;
          background: url('data:image/svg+xml;utf8,<svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M12.7407 9.70401C12.7407 9.74417 12.7378 9.77811 12.7335 9.81479C12.7111 10.207 12.3897 10.5195 11.9955 10.5195C11.6014 10.5195 11.2801 10.209 11.2577 9.8169C11.2534 9.7801 11.2504 9.74417 11.2504 9.70401C11.2504 9.31225 11.1572 8.90867 10.2102 8.90867H7.04307C5.61481 8.90867 5 8.22698 5 6.86345V5.70358C5 5.31505 5.29521 5 5.68008 5C6.06495 5 6.35683 5.31505 6.35683 5.70358V6.09547C6.35683 6.53423 6.655 6.85413 7.307 6.85413H10.4119C11.8248 6.85413 11.9334 7.91255 11.98 8.4729H12.0111C12.0577 7.91255 12.1663 6.85413 13.5791 6.85413H16.6841C17.3361 6.85413 17.614 6.54529 17.614 6.10641L17.6158 5.70358C17.6158 5.31505 17.9246 5 18.3095 5C18.6943 5 19 5.31505 19 5.70358V6.86345C19 8.22698 18.3763 8.90867 16.9481 8.90867H13.7809C12.8338 8.90867 12.7407 9.31225 12.7407 9.70401Z" fill="white"/><path d="M12.7536 17.7785C12.6267 18.0629 12.3469 18.2608 12.0211 18.2608C11.6907 18.2608 11.4072 18.0575 11.2831 17.7668C11.2817 17.7643 11.2803 17.7619 11.279 17.7595C11.2761 17.7544 11.2732 17.7495 11.2704 17.744L8.45986 12.4362C8.3821 12.2973 8.34106 12.1399 8.34106 11.9807C8.34106 11.4732 8.74546 11.0603 9.24238 11.0603C9.64162 11.0603 9.91294 11.2597 10.0985 11.6922L12.0216 15.3527L13.9468 11.6878C14.1301 11.2597 14.4014 11.0603 14.8008 11.0603C15.2978 11.0603 15.7021 11.4732 15.7021 11.9807C15.7021 12.1399 15.6611 12.2973 15.5826 12.4374L12.7724 17.7446C12.7683 17.7524 12.7642 17.7597 12.7601 17.767C12.7579 17.7708 12.7557 17.7746 12.7536 17.7785Z" fill="white"/></svg>');
          background-size: 100%;
        }

        .menu-button vaadin-menu-bar-button[focus-ring] {
          outline: 2px solid var(--selection-color);
          outline-offset: 2px;
        }

        .menu-button vaadin-menu-bar-button:hover {
          background: hsl(0 0% 0% / 0.8);
          box-shadow:
            0 0 0 1px hsl(0 0% 100% / 0.1),
            0 2px 8px -1px hsl(0 0% 0% / 0.3);
        }

        :host([active]) .menu-button vaadin-menu-bar-button {
          background-color: transparent;
          box-shadow:
            inset 0 0 0 1px hsl(0 0% 0% / 0.2),
            0 2px 8px -1px hsl(0 0% 0% / 0.3);
        }

        :host([active]) .menu-button vaadin-menu-bar-button::before {
          opacity: 1;
          animation-play-state: running;
        }

        :host([attention-required]) {
          animation: bounce 0.5s;
          animation-iteration-count: 2;
        }

        [part='indicator'] {
          top: -6px;
          right: -6px;
          width: 12px;
          height: 12px;
          box-sizing: border-box;
          border-radius: 100%;
          position: absolute;
          display: var(--indicator-display, none);
          background: var(--indicator-color);
          z-index: calc(var(--z-index-activation-button) + 1);
          border: 2px solid var(--indicator-border);
        }

        :host([indicator='info']) {
          --indicator-display: block;
          --indicator-color: var(--info-color);
        }

        :host([indicator='warning']) {
          --indicator-display: block;
          --indicator-color: var(--warning-color);
        }

        :host([indicator='error']) {
          --indicator-display: block;
          --indicator-color: var(--error-color);
        }
      `
    ];
  }
  connectedCallback() {
    super.connectedCallback(), this.reaction(
      () => le.attentionRequiredPanelTag,
      () => {
        this.toggleAttribute($u, le.attentionRequiredPanelTag !== null), this.updateIndicator();
      }
    ), this.reaction(
      () => m$1.active,
      () => {
        this.toggleAttribute("active", m$1.active);
      },
      { fireImmediately: true }
    ), this.addEventListener("mousedown", this.mouseDownListener), document.addEventListener("mouseup", this.documentMouseUpListener);
    const e = tt$1.getActivationButtonPosition();
    e ? (this.style.setProperty("--left", `${e.left}px`), this.style.setProperty("--bottom", `${e.bottom}px`), this.style.setProperty("--right", `${e.right}px`), this.style.setProperty("--top", `${e.top}px`)) : (this.style.setProperty("--bottom", "var(--space)"), this.style.setProperty("--right", "var(--space)")), y$1.on("document-activation-change", (t) => {
      this.toggleAttribute("document-hidden", !t.detail.active);
    }), this.reaction(
      () => [
        yn.jdkInfo,
        m$1.idePluginState,
        tt$1.isFeedbackDisplayedAtLeastOnce()
      ],
      () => {
        this.updateIndicator();
      }
    ), this.reaction(
      () => [
        m$1.active,
        m$1.idePluginState,
        tt$1.isActivationAnimation(),
        tt$1.isActivationShortcut(),
        tt$1.isSendErrorReportsAllowed(),
        tt$1.isAIUsageAllowed(),
        tt$1.getDismissedNotifications()
      ],
      () => {
        this.generateItems();
      }
    ), document.addEventListener("mousemove", this.closeMenuMouseMoveListener);
  }
  disconnectedCallback() {
    super.disconnectedCallback(), this.removeEventListener("mousedown", this.mouseDownListener), document.removeEventListener("mouseup", this.documentMouseUpListener), document.removeEventListener("mousemove", this.closeMenuMouseMoveListener);
  }
  updateIndicator() {
    if (this.hasAttribute($u)) {
      this.setAttribute("indicator", "error");
      return;
    }
    const e = ad();
    if (e.status !== "success") {
      this.setAttribute("indicator", e.status);
      return;
    }
    if (!tt$1.isFeedbackDisplayedAtLeastOnce()) {
      this.setAttribute("indicator", "info");
      return;
    }
    this.removeAttribute("indicator");
  }
  /**
   * To hide overlay while dragging
   * @param visible
   */
  setOverlayVisibility(e) {
    const t = this.shadowRoot.querySelector("vaadin-menu-bar-button").__overlay;
    e ? (t?.style.setProperty("display", "flex"), t?.style.setProperty("visibility", "visible")) : (t?.style.setProperty("display", "none"), t?.style.setProperty("visibility", "invisible"));
  }
  generateItems() {
    const e = m$1.active, t = e && !!m$1.idePluginState?.supportedActions?.find((o2) => o2 === "undo"), i = [];
    if (yn.springSecurityEnabled) {
      const o2 = tt$1.getRecentSwitchedUsernames();
      i.push(
        ...o2.map((s) => ({
          component: v({ component: ii(s) }),
          action: async () => {
            await Ze(s) && window.location.reload();
          }
        }))
      ), i.length > 0 && i.unshift({
        component: v({ label: "Recently Used Usernames" }),
        disabled: true
      });
    }
    const n = [
      {
        text: "Vaadin Copilot",
        children: [
          {
            visible: e,
            component: v({ component: "copilot-activation-button-user-info" })
          },
          { visible: e, component: "hr" },
          {
            component: v({ component: "copilot-activation-button-development-workflow" }),
            action: id
          },
          { visible: yn.springSecurityEnabled, component: "hr" },
          {
            visible: yn.springSecurityEnabled,
            component: v({
              icon: o.user,
              label: "Application's User"
            }),
            children: [
              ...i,
              {
                component: v({ component: ti() })
              }
            ]
          },
          {
            component: "hr",
            visible: t
          },
          {
            visible: t,
            component: v({
              icon: o.flipBack,
              label: "Undo",
              hint: ld.undo
            }),
            action: () => {
              y$1.emit("undoRedo", { undo: true });
            }
          },
          {
            visible: t,
            component: v({
              icon: o.flipForward,
              label: "Redo",
              hint: ld.redo
            }),
            action: () => {
              y$1.emit("undoRedo", { undo: false });
            }
          },
          {
            component: v({
              icon: o.starsAlt,
              label: "Toggle Command Window",
              hint: ld.toggleCommandWindow,
              style: "toggle-spotlight"
            }),
            action: () => {
              m$1.setSpotlightActive(!m$1.spotlightActive);
            }
          },
          {
            component: "hr",
            visible: e
          },
          {
            visible: e,
            component: v({
              icon: o.settings,
              label: "Settings"
            }),
            children: [
              {
                component: v({
                  icon: o.keyboard,
                  label: "Activation Shortcut",
                  suffix: tt$1.isActivationShortcut() ? '<div aria-hidden="true" class="switch on"></div>' : '<div aria-hidden="true" class="switch off"></div>'
                }),
                keepOpen: true,
                action: (o2) => {
                  tt$1.setActivationShortcut(!tt$1.isActivationShortcut()), pe(o2, tt$1.isActivationShortcut());
                }
              },
              {
                component: v({
                  icon: o.play,
                  label: "Activation Animation",
                  suffix: tt$1.isActivationAnimation() ? '<div aria-hidden="true" class="switch on"></div>' : '<div aria-hidden="true" class="switch off"></div>'
                }),
                keepOpen: true,
                action: (o2) => {
                  tt$1.setActivationAnimation(!tt$1.isActivationAnimation()), pe(o2, tt$1.isActivationAnimation());
                }
              },
              {
                component: v({
                  icon: o.starsAlt,
                  label: "AI Usage",
                  hint: Qe()
                }),
                keepOpen: true,
                action: (o2) => {
                  let s;
                  const a = tt$1.isAIUsageAllowed();
                  a === "ask" ? s = "yes" : a === "no" ? s = "ask" : s = "no", tt$1.setAIUsageAllowed(s), ni(o2);
                }
              },
              {
                visible: m$1.userInfo?.vaadiner,
                component: v({
                  icon: o.starsAlt,
                  label: "AI Provider (Experimental, Vaadin employees only)",
                  hint: et()
                }),
                keepOpen: true,
                action: (o2) => {
                  const s = tt$1.getAIProvider() === "ANY" ? "EU_ONLY" : "ANY";
                  tt$1.setAIProvider(s), oi(o2);
                }
              },
              {
                component: v({
                  icon: o.alertCircle,
                  label: "Report Errors to Vaadin",
                  suffix: tt$1.isSendErrorReportsAllowed() ? '<div aria-hidden="true" class="switch on"></div>' : '<div aria-hidden="true" class="switch off"></div>'
                }),
                keepOpen: true,
                action: (o2) => {
                  tt$1.setSendErrorReportsAllowed(!tt$1.isSendErrorReportsAllowed()), pe(o2, tt$1.isSendErrorReportsAllowed());
                }
              },
              { component: "hr" },
              {
                visible: e,
                component: v({
                  icon: o.annotation,
                  label: "Show Welcome Message"
                }),
                keepOpen: true,
                action: () => {
                  m$1.setWelcomeActive(true), m$1.setSpotlightActive(true);
                }
              },
              {
                visible: e,
                component: v({
                  icon: o.keyboard,
                  label: "Show Keyboard Shortcuts"
                }),
                action: () => {
                  le.updatePanel("copilot-shortcuts-panel", {
                    floating: true
                  });
                }
              },
              {
                visible: tt$1.getDismissedNotifications().length > 0,
                component: v({
                  icon: o.annotationX,
                  label: "Clear Dismissed Notifications"
                }),
                action: () => {
                  tt$1.clearDismissedNotifications();
                }
              }
            ]
          },
          { component: "hr" },
          {
            component: v({
              component: "copilot-activation-button-feedback"
            }),
            action: Kt
          },
          {
            component: v({
              icon: o.vaadinLogo,
              label: "Copilot",
              hint: tt$1.isActivationShortcut() ? ld.toggleCopilot : void 0,
              suffix: m$1.active ? '<div aria-hidden="true" class="switch on"></div>' : '<div aria-hidden="true" class="switch off"></div>'
            }),
            action: () => {
              this.activationBtnClicked();
            }
          }
        ]
      }
    ];
    this.items = n.filter(gl);
  }
  render() {
    return ee`
      <vaadin-menu-bar
        class="menu-button"
        .items="${this.items}"
        @item-selected="${(e) => {
      this.handleMenuItemClick(e.detail.value);
    }}"
        ?open-on-hover=${!this.dragging}
        overlay-class="activation-button-menu">
      </vaadin-menu-bar>
      <div part="indicator"></div>
    `;
  }
  handleMenuItemClick(e) {
    e.action && e.action(e);
  }
  firstUpdated() {
    i$1(this.shadowRoot);
  }
};
F([
  f("vaadin-menu-bar")
], G.prototype, "menubar", 2);
F([
  b()
], G.prototype, "dragging", 2);
F([
  b()
], G.prototype, "items", 2);
G = F([
  Vl("copilot-activation-button")
], G);
function pe(e, t) {
  const i = e.component;
  if (!i || typeof i == "string") {
    console.error("Unable to set switch value for a non-component item");
    return;
  }
  const n = i.querySelector(".switch");
  if (!n) {
    console.error("No element found when setting switch value");
    return;
  }
  t ? (n.classList.remove("off"), n.classList.add("on")) : (n.classList.add("off"), n.classList.remove("on"));
}
function ni(e) {
  const t = e.component;
  if (!t || typeof t == "string") {
    console.error("Unable to set switch value for a non-component item");
    return;
  }
  const i = t.querySelector(".hint");
  if (!i) {
    console.error("No element found when setting switch value");
    return;
  }
  i.innerText = Qe();
}
function oi(e) {
  const t = e.component;
  if (!t || typeof t == "string") {
    console.error("Unable to set switch value for a non-component item");
    return;
  }
  const i = t.querySelector(".hint");
  if (!i) {
    console.error("No element found when setting switch value");
    return;
  }
  i.innerText = et();
}
function Qe() {
  return tt$1.isAIUsageAllowed() === "ask" ? "Always Ask" : tt$1.isAIUsageAllowed() === "no" ? "Disabled" : "Enabled";
}
function et() {
  return tt$1.getAIProvider() === "ANY" ? "Any" : tt$1.getAIProvider() === "EU_ONLY" ? "Inside EU" : "???";
}
var si = Object.defineProperty, ai = Object.getOwnPropertyDescriptor, J = (e, t, i, n) => {
  for (var o2 = n > 1 ? void 0 : n ? ai(t, i) : t, s = e.length - 1, a; s >= 0; s--)
    (a = e[s]) && (o2 = (n ? a(t, i, o2) : a(o2)) || o2);
  return n && o2 && si(t, i, o2), o2;
};
const x = "resize-dir", he = "floating-resizing-active";
let _ = class extends lc {
  constructor() {
    super(...arguments), this.panelTag = "", this.dockingItems = [
      {
        component: ce({
          icon: o.layoutRight,
          label: "Dock right",
          style: "dock"
        }),
        panel: "right"
      },
      {
        component: ce({
          icon: o.layoutLeft,
          label: "Dock left",
          style: "dock"
        }),
        panel: "left"
      },
      {
        component: ce({
          icon: o.layoutBottom,
          label: "Dock bottom",
          style: "dock"
        }),
        panel: "bottom"
      }
    ], this.floatingResizingStarted = false, this.resizingInDrawerStarted = false, this.toggling = false, this.rectangleBeforeResizing = null, this.floatingResizeHandlerMouseMoveListener = (e) => {
      if (!this.panelInfo?.floating || this.floatingResizingStarted || !this.panelInfo?.expanded)
        return;
      const t = this.getBoundingClientRect(), i = Math.abs(e.clientX - t.x), n = Math.abs(t.x + t.width - e.clientX), o2 = Math.abs(e.clientY - t.y), s = Math.abs(t.y + t.height - e.clientY), a = Number.parseInt(
        window.getComputedStyle(this).getPropertyValue("--floating-offset-resize-threshold"),
        10
      );
      let u = "";
      i < a ? o2 < a ? (u = "nw-resize", this.setAttribute(x, "top left")) : s < a ? (u = "sw-resize", this.setAttribute(x, "bottom left")) : (u = "col-resize", this.setAttribute(x, "left")) : n < a ? o2 < a ? (u = "ne-resize", this.setAttribute(x, "top right")) : s < a ? (u = "se-resize", this.setAttribute(x, "bottom right")) : (u = "col-resize", this.setAttribute(x, "right")) : s < a ? (u = "row-resize", this.setAttribute(x, "bottom")) : o2 < a && (u = "row-resize", this.setAttribute(x, "top")), u !== "" ? (this.rectangleBeforeResizing = this.getBoundingClientRect(), this.style.setProperty("--resize-cursor", u)) : (this.style.removeProperty("--resize-cursor"), this.removeAttribute(x)), this.toggleAttribute(he, u !== "");
    }, this.floatingResizingMouseDownListener = (e) => {
      if (!this.hasAttribute(he) || e.button !== 0)
        return;
      e.stopPropagation(), e.preventDefault(), A.anchorLeftTop(this), this.floatingResizingStarted = true, this.toggleAttribute("resizing", true);
      const t = this.getResizeDirections(), { clientX: i, clientY: n } = e;
      (t.includes("top") || t.includes("bottom")) && this.style.setProperty("--section-height", null), t.forEach((o2) => this.setResizePosition(o2, i, n)), m$1.setSectionPanelResizing(true);
    }, this.floatingResizingMouseLeaveListener = () => {
      this.panelInfo?.floating && (this.floatingResizingStarted || (this.removeAttribute("resizing"), this.removeAttribute(he), this.removeAttribute("dragging"), this.style.removeProperty("--resize-cursor"), this.removeAttribute(x), this.panelInfo != null && this.panelInfo.height != null && this.panelInfo?.height > window.innerHeight && (le.updatePanel(this.panelInfo.tag, {
        height: window.innerHeight - 10
      }), this.setCssSizePositionProperties())));
    }, this.floatingResizingMouseMoveListener = (e) => {
      if (!this.panelInfo?.floating || !this.floatingResizingStarted)
        return;
      e.stopPropagation(), e.preventDefault();
      const t = this.getResizeDirections(), { clientX: i, clientY: n } = e;
      t.forEach((o2) => this.setResizePosition(o2, i, n));
    }, this.setFloatingResizeDirectionProps = (e, t, i, n) => {
      i && i > Number.parseFloat(window.getComputedStyle(this).getPropertyValue("--min-width")) && (this.style.setProperty(`--${e}`, `${t}px`), this.style.setProperty("width", `${i}px`));
      const o2 = window.getComputedStyle(this), s = Number.parseFloat(o2.getPropertyValue("--header-height")), a = Number.parseFloat(o2.getPropertyValue("--floating-offset-resize-threshold")) / 2;
      n && n > s + a && (this.style.setProperty(`--${e}`, `${t}px`), this.style.setProperty("height", `${n}px`), this.container.style.setProperty("margin-top", "calc(var(--floating-offset-resize-threshold) / 4)"), this.container.style.height = `calc(${n}px - var(--floating-offset-resize-threshold) / 2)`, this.contentArea.style.setProperty("height", `${n}px`));
    }, this.floatingResizingMouseUpListener = (e) => {
      if (!this.floatingResizingStarted || !this.panelInfo?.floating)
        return;
      e.stopPropagation(), e.preventDefault(), this.floatingResizingStarted = false, this.contentArea.style.removeProperty("height"), m$1.setSectionPanelResizing(false);
      const { width: t, height: i } = this.getBoundingClientRect(), { left: n, top: o2, bottom: s, right: a } = A.anchor(this), u = window.getComputedStyle(this.container), m = Number.parseInt(u.borderTopWidth, 10), P = Number.parseInt(u.borderBottomWidth, 10);
      le.updatePanel(this.panelInfo.tag, {
        width: t,
        height: i - (m + P),
        floatingPosition: {
          ...this.panelInfo.floatingPosition,
          left: n,
          top: o2,
          bottom: s,
          right: a
        }
      }), this.style.removeProperty("width"), this.style.removeProperty("height"), this.container.style.removeProperty("height"), this.container.style.removeProperty("margin-top"), this.setCssSizePositionProperties(), this.toggleAttribute("dragging", false);
    }, this.transitionEndEventListener = () => {
      this.toggling && (this.toggling = false, A.anchor(this));
    }, this.sectionPanelMouseEnterListener = () => {
      this.hasAttribute($u) && (this.removeAttribute($u), le.clearAttention());
    }, this.contentAreaMouseDownListener = () => {
      le.bringToFront(this.panelInfo.tag);
    }, this.documentMouseUpEventListener = () => {
      document.removeEventListener("mousemove", this.draggingEventListener), this.panelInfo?.floating && (this.toggleAttribute("dragging", false), m$1.setSectionPanelDragging(false));
    }, this.panelHeaderMouseDownEventListener = (e) => {
      e.button === 0 && (le.bringToFront(this.panelInfo.tag), !this.hasAttribute(x) && (e.target instanceof HTMLButtonElement && e.target.getAttribute("part") === "title-button" ? this.startDraggingDebounce(e) : this.startDragging(e)));
    }, this.panelHeaderMouseUpEventListener = (e) => {
      e.button === 0 && this.startDraggingDebounce.clear();
    }, this.startDragging = (e) => {
      A.draggingStarts(this, e), document.addEventListener("mousemove", this.draggingEventListener), m$1.setSectionPanelDragging(true), this.panelInfo?.floating ? this.toggleAttribute("dragging", true) : this.parentElement.sectionPanelDraggingStarted(this, e), e.preventDefault(), e.stopPropagation();
    }, this.startDraggingDebounce = ou(this.startDragging, 200), this.draggingEventListener = (e) => {
      const t = A.dragging(this, e);
      if (this.panelInfo?.floating && this.panelInfo?.floatingPosition) {
        e.preventDefault();
        const { left: i, top: n, bottom: o2, right: s } = t;
        le.updatePanel(this.panelInfo.tag, {
          floatingPosition: {
            ...this.panelInfo.floatingPosition,
            left: i,
            top: n,
            bottom: o2,
            right: s
          }
        });
      }
    }, this.setCssSizePositionProperties = () => {
      const e = le.getPanelByTag(this.panelTag);
      if (e && (e.height !== void 0 && (this.panelInfo?.floating || e.panel === "left" || e.panel === "right" ? this.style.setProperty("--section-height", `${e.height}px`) : this.style.removeProperty("--section-height")), e.width !== void 0 && (e.floating || e.panel === "bottom" ? this.style.setProperty("--section-width", `${e.width}px`) : this.style.removeProperty("--section-width")), e.floating && e.floatingPosition && !this.toggling)) {
        const { left: t, top: i, bottom: n, right: o2 } = e.floatingPosition;
        this.style.setProperty("--left", t !== void 0 ? `${t}px` : "auto"), this.style.setProperty("--top", i !== void 0 ? `${i}px` : "auto"), this.style.setProperty("--bottom", n !== void 0 ? `${n}px` : ""), this.style.setProperty("--right", o2 !== void 0 ? `${o2}px` : "");
        const s = window.getComputedStyle(this);
        parseInt(s.top, 10) < 0 && this.style.setProperty("--top", "0px"), parseInt(s.bottom, 10) < 0 && this.style.setProperty("--bottom", "0px");
      }
    }, this.renderPopupButton = () => {
      if (!this.panelInfo)
        return A$1;
      let e;
      return this.panelInfo.panel === void 0 ? e = "Close the popup" : e = this.panelInfo.floating ? `Dock ${this.panelInfo.header} to ${this.panelInfo.panel}` : `Open ${this.panelInfo.header} as a popup`, ee`
      <vaadin-context-menu .items=${this.dockingItems} @item-selected="${this.changeDockingPanel}">
        <button
          @click="${(t) => this.changePanelFloating(t)}"
          @mousedown="${(t) => t.stopPropagation()}"
          aria-label=${e}
          class="icon"
          part="popup-button"
          title="${e}">
          ${this.getPopupButtonIcon()}
        </button>
      </vaadin-context-menu>
    `;
    }, this.changePanelFloating = (e) => {
      if (this.panelInfo)
        if (e.stopPropagation(), Qu(this), this.panelInfo?.floating)
          le.updatePanel(this.panelInfo.tag, { floating: false });
        else {
          let t;
          if (this.panelInfo.floatingPosition)
            t = this.panelInfo.floatingPosition;
          else {
            const { left: o2, top: s } = this.getBoundingClientRect();
            t = {
              left: o2,
              top: s
            };
          }
          let i = this.panelInfo?.height;
          i === void 0 && this.panelInfo.expanded && (i = Number.parseInt(window.getComputedStyle(this).height, 10)), this.parentElement.forceClose(), le.updatePanel(this.panelInfo.tag, {
            floating: true,
            expanded: true,
            width: this.panelInfo?.width || Number.parseInt(window.getComputedStyle(this).width, 10),
            height: i,
            floatingPosition: t
          }), le.bringToFront(this.panelInfo.tag);
        }
    }, this.toggleExpand = (e) => {
      this.panelInfo && (e.stopPropagation(), A.anchorLeftTop(this), le.updatePanel(this.panelInfo.tag, {
        expanded: !this.panelInfo.expanded
      }), this.toggling = true, this.toggleAttribute("expanded", this.panelInfo.expanded), y$1.emit("panel-expanded", { panelTag: this.panelInfo.tag, expanded: this.panelInfo.expanded }));
    };
  }
  static get styles() {
    return [
      V$1(qc),
      V$1(Kc),
      jl`
        * {
          box-sizing: border-box;
        }

        :host {
          flex: none;
          --min-width: 160px;
          --header-height: 36px;
          --content-width: var(--content-width, 100%);
          --floating-border-width: 1px;
          --floating-offset-resize-threshold: 8px;
          cursor: var(--cursor, var(--resize-cursor, default));
          overflow: hidden;
        }

        :host(:not([expanded])) {
          grid-template-rows: auto 0fr;
        }

        [part='header'] {
          align-items: center;
          color: var(--color-high-contrast);
          display: flex;
          flex: none;
          font: var(--font-small-medium);
          gap: var(--space-50);
          justify-content: space-between;
          min-width: 100%;
          padding: var(--space-50);
          user-select: none;
          -webkit-user-select: none;
          width: var(--min-width);
        }

        :host([floating]) {
          --content-height: calc(var(--section-height));
        }

        :host([floating]:not([expanded])) [part='header'] {
          --min-width: unset;
        }

        :host([floating]) [part='header'] {
          transition: border-color var(--duration-2);
        }

        :host([floating]:not([expanded])) [part='header'] {
          border-color: transparent;
        }

        [part='title'] {
          flex: auto;
          margin: 0;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        [part='title']:first-child {
          margin-inline-start: var(--space-100);
        }

        [part='content'] {
          height: calc(var(--content-height) - var(--header-height));
          overflow: auto;
          transition:
            height var(--duration-2),
            width var(--duration-2),
            opacity var(--duration-2),
            visibility calc(var(--duration-2) * 2);
        }

        :host([floating]) [part='drawer-resize'] {
          display: none;
        }

        :host(:not([expanded])) [part='drawer-resize'] {
          display: none;
        }

        :host(:not([floating]):not(:last-child)) {
          border-bottom: 1px solid var(--divider-primary-color);
        }

        :host(:not([expanded])) [part='content'] {
          opacity: 0;
          visibility: hidden;
        }

        :host([floating]:not([expanded])) [part='content'] {
          width: 0;
          height: 0;
        }

        :host(:not([expanded])) [part='content'][style*='width'] {
          width: 0 !important;
        }

        :host([floating]) {
          position: fixed;
          min-width: 0;
          min-height: 0;
          z-index: calc(var(--z-index-floating-panel) + var(--z-index-focus, 0));
          top: clamp(0px, var(--top), calc(100vh - var(--section-height, var(--header-height)) * 0.5));
          left: clamp(calc(var(--section-width) * -0.5), var(--left), calc(100vw - var(--section-width) * 0.5));
          bottom: clamp(
            calc(var(--section-height, var(--header-height)) * -0.5),
            var(--bottom),
            calc(100vh - var(--section-height, var(--header-height)) * 0.5)
          );
          right: clamp(calc(var(--section-width) * -0.5), var(--right), calc(100vw - var(--section-width) * 0.5));
          width: var(--section-width);
          overflow: visible;
        }
        :host([floating]) [part='container'] {
          background: var(--background-color);
          border: var(--floating-border-width) solid var(--surface-border-color);
          -webkit-backdrop-filter: var(--surface-backdrop-filter);
          backdrop-filter: var(--surface-backdrop-filter);
          border-radius: var(--radius-2);
          margin: auto;
          box-shadow: var(--surface-box-shadow-2);
        }
        [part='container'] {
          overflow: hidden;
        }
        :host([floating][expanded]) [part='container'] {
          height: calc(100% - var(--floating-offset-resize-threshold) / 2);
          width: calc(100% - var(--floating-offset-resize-threshold) / 2);
        }

        :host([floating]:not([expanded])) {
          width: unset;
        }

        :host([floating]) .drag-handle {
          cursor: var(--resize-cursor, move);
        }

        :host([floating][expanded]) [part='content'] {
          min-width: var(--min-width);
          min-height: 0;
          width: var(--content-width);
        }

        /* :hover for Firefox, :active for others */

        :host([floating][expanded]) [part='content']:is(:hover, :active) {
          transition: none;
        }

        [part='title-button'] {
          font: var(--font-xxsmall-bold);
          padding: 0;
          text-align: start;
          text-transform: uppercase;
        }

        @media not (prefers-reduced-motion) {
          [part='toggle-button'] svg {
            transition: transform 0.15s cubic-bezier(0.2, 0, 0, 1);
          }
        }

        [part='toggle-button'][aria-expanded='true'] svg {
          transform: rotate(90deg);
        }

        .actions {
          display: none;
        }

        :host([expanded]) .actions {
          display: contents;
        }

        ::slotted(*) {
          box-sizing: border-box;
          display: block;
          /* padding: var(--space-150); */
          width: 100%;
        }

        /*workaround for outline to have a explicit height while floating by default. 
          may be removed after https://github.com/vaadin/web-components/issues/7620 is solved
        */
        :host([floating][expanded][paneltag='copilot-outline-panel']) {
          --grid-default-height: 400px;
        }

        :host([dragging]) {
          opacity: 0.4;
        }

        :host([dragging]) [part='content'] {
          pointer-events: none;
        }

        :host([hiding-while-drag-and-drop]) {
          display: none;
        }

        // dragging in drawer

        :host(:not([floating])) .drag-handle {
          cursor: grab;
        }

        :host(:not([floating])[dragging]) .drag-handle {
          cursor: grabbing;
        }

        vaadin-context-menu-item.dock {
          --vaadin-item-checkmark-display: none;
        }
      `
    ];
  }
  connectedCallback() {
    super.connectedCallback(), this.setAttribute("role", "region"), this.reaction(
      () => le.getAttentionRequiredPanelConfiguration(),
      () => {
        const e = le.getAttentionRequiredPanelConfiguration();
        this.toggleAttribute($u, e?.tag === this.panelTag && e?.floating);
      }
    ), this.addEventListener("mouseenter", this.sectionPanelMouseEnterListener), this.reaction(
      () => m$1.operationInProgress,
      () => {
        requestAnimationFrame(() => {
          this.toggleAttribute(
            "hiding-while-drag-and-drop",
            m$1.operationInProgress === Ju.DragAndDrop && this.panelInfo?.floating && !this.panelInfo.showWhileDragging && !this.hasDropTarget()
          );
        });
      }
    ), this.reaction(
      () => le.floatingPanelsZIndexOrder,
      () => {
        this.style.setProperty("--z-index-focus", `${le.getFloatingPanelZIndex(this.panelTag)}`);
      },
      { fireImmediately: true }
    ), this.reaction(
      () => le.getPanelByTag(this.panelTag)?.floatingPosition,
      () => {
        !this.floatingResizingStarted && !m$1.sectionPanelDragging && this.setCssSizePositionProperties();
      }
    ), this.addEventListener("transitionend", this.transitionEndEventListener), this.addEventListener("mousemove", this.floatingResizeHandlerMouseMoveListener), this.addEventListener("mousedown", this.floatingResizingMouseDownListener), this.addEventListener("mouseleave", this.floatingResizingMouseLeaveListener), document.addEventListener("mousemove", this.floatingResizingMouseMoveListener), document.addEventListener("mouseup", this.floatingResizingMouseUpListener);
  }
  disconnectedCallback() {
    super.disconnectedCallback(), this.removeEventListener("mouseenter", this.sectionPanelMouseEnterListener), this.removeEventListener("mousemove", this.floatingResizeHandlerMouseMoveListener), this.removeEventListener("mousedown", this.floatingResizingMouseDownListener), document.removeEventListener("mousemove", this.floatingResizingMouseMoveListener), document.removeEventListener("mouseup", this.floatingResizingMouseUpListener);
  }
  setResizePosition(e, t, i) {
    const n = this.rectangleBeforeResizing, o2 = 0, s = window.innerWidth, a = 0, u = window.innerHeight, m = Math.max(o2, Math.min(s, t)), P = Math.max(a, Math.min(u, i));
    if (e === "left")
      this.setFloatingResizeDirectionProps(
        "left",
        m,
        n.left - m + n.width
      );
    else if (e === "right")
      this.setFloatingResizeDirectionProps(
        "right",
        m,
        m - n.right + n.width
      );
    else if (e === "top") {
      const L = n.top - P + n.height;
      this.setFloatingResizeDirectionProps("top", P, void 0, L);
    } else if (e === "bottom") {
      const L = P - n.bottom + n.height;
      this.setFloatingResizeDirectionProps("bottom", P, void 0, L);
    }
  }
  willUpdate(e) {
    super.willUpdate(e), e.has("panelTag") && (this.panelInfo = le.getPanelByTag(this.panelTag), this.setAttribute("aria-labelledby", this.panelInfo.tag.concat("-title"))), this.toggleAttribute("floating", this.panelInfo?.floating);
  }
  updated(e) {
    super.updated(e), this.setCssSizePositionProperties(), requestAnimationFrame(() => {
      if (this.panelInfo !== void 0 && this.panelInfo.floating && this.panelInfo.floatingPosition?.top != null && (this.panelInfo?.height === void 0 || this.panelInfo?.width === void 0)) {
        let t = this.panelInfo?.height, i = this.panelInfo?.width, n = false;
        const o2 = this.panelInfo.floatingPosition;
        if (this.offsetWidth !== void 0 && this.offsetWidth !== 0 && this.panelInfo.width === void 0 && (n = true, i = this.offsetWidth), this.offsetHeight !== void 0 && this.offsetHeight !== 0 && this.panelInfo.height === void 0) {
          n = true, t = this.offsetHeight;
          const s = window.innerHeight;
          let a = this.panelInfo.floatingPosition.top;
          t > s && (t = s);
          const u = Math.floor(s / 3);
          t < u ? (t = u, a = u) : t > 2 * u ? a -= t - (s - this.panelInfo.floatingPosition.top) : a = u, o2.top = a;
        }
        n && (le.updatePanel(this.panelInfo?.tag, {
          height: t,
          width: i,
          floatingPosition: o2
        }), this.setCssSizePositionProperties());
      }
    });
  }
  firstUpdated(e) {
    super.firstUpdated(e), document.addEventListener("mouseup", this.documentMouseUpEventListener), this.headerDraggableArea.addEventListener("mousedown", this.panelHeaderMouseDownEventListener), this.headerDraggableArea.addEventListener("mouseup", this.panelHeaderMouseUpEventListener), this.toggleAttribute("expanded", this.panelInfo?.expanded), this.toggleAttribute("individual", this.panelInfo?.individual ?? false), ed(this), this.setCssSizePositionProperties(), this.contentArea.addEventListener("mousedown", this.contentAreaMouseDownListener), i$1(this.shadowRoot);
  }
  render() {
    return this.panelInfo ? ee`
      <div part="container">
        <div part="header" class="drag-handle">
          ${this.panelInfo.expandable !== false ? ee` <button
                @mousedown="${(e) => e.stopPropagation()}"
                @click="${(e) => this.toggleExpand(e)}"
                aria-controls="content"
                aria-expanded="${this.panelInfo.expanded}"
                aria-label="Expand ${this.panelInfo.header}"
                class="icon"
                part="toggle-button">
                <span>${o.chevronRight}</span>
              </button>` : A$1}
          <h2 id="${this.panelInfo.tag}-title" part="title">
            <button
              part="title-button"
              @dblclick="${(e) => {
      this.toggleExpand(e), this.startDraggingDebounce.clear();
    }}">
              ${le.getPanelHeader(this.panelInfo)}
            </button>
          </h2>
          <div class="actions" @mousedown="${(e) => e.stopPropagation()}">${this.renderActions()}</div>
          ${this.renderHelpButton()} ${this.renderPopupButton()}
        </div>
        <div part="content" id="content">
          <slot name="content"></slot>
        </div>
      </div>
    ` : A$1;
  }
  getPopupButtonIcon() {
    return this.panelInfo ? this.panelInfo.panel === void 0 ? o.x : this.panelInfo.floating ? this.panelInfo.panel === "bottom" ? o.layoutBottom : this.panelInfo.panel === "left" ? o.layoutLeft : this.panelInfo.panel === "right" ? o.layoutRight : A$1 : o.share : A$1;
  }
  renderHelpButton() {
    return this.panelInfo?.helpUrl ? ee` <button
      @click="${() => window.open(this.panelInfo.helpUrl, "_blank")}"
      @mousedown="${(e) => e.stopPropagation()}"
      aria-label="More information about ${this.panelInfo.header}"
      class="icon"
      title="More information about ${this.panelInfo.header}">
      <span>${o.help}</span>
    </button>` : A$1;
  }
  renderActions() {
    if (!this.panelInfo?.actionsTag)
      return A$1;
    const e = this.panelInfo.actionsTag;
    return pc(`<${e}></${e}>`);
  }
  changeDockingPanel(e) {
    const t = e.detail.value.panel;
    if (this.panelInfo?.panel !== t) {
      const i = le.panels.filter((n) => n.panel === t).map((n) => n.panelOrder).sort((n, o2) => o2 - n)[0];
      Qu(this), le.updatePanel(this.panelInfo.tag, { panel: t, panelOrder: i + 1 });
    }
    this.panelInfo.floating && this.changePanelFloating(e);
  }
  getResizeDirections() {
    const e = this.getAttribute(x);
    return e ? e.split(" ") : [];
  }
  hasDropTarget() {
    const e = this.shadowRoot?.querySelector("slot")?.assignedElements();
    if (!e)
      return false;
    for (const t of e) {
      const i = Bu(
        t.shadowRoot ?? t,
        "copilot-image-upload"
      );
      if (i && window.getComputedStyle(i).display !== "none")
        return true;
    }
    return false;
  }
};
J([
  h()
], _.prototype, "panelTag", 2);
J([
  f(".drag-handle")
], _.prototype, "headerDraggableArea", 2);
J([
  f("#content")
], _.prototype, "contentArea", 2);
J([
  f('[part="container"]')
], _.prototype, "container", 2);
J([
  b()
], _.prototype, "dockingItems", 2);
_ = J([
  Vl("copilot-section-panel-wrapper")
], _);
const ve = window.Vaadin.copilot.customComponentHandler;
if (!ve)
  throw new Error("Tried to access custom component handler before it was initialized.");
function ri(e) {
  m$1.setOperationWaitsHmrUpdate(e, 3e4);
}
y$1.on("undoRedo", (e) => {
  const i = { files: li(e), uiId: Ru() }, n = e.detail.undo ? "copilot-plugin-undo" : "copilot-plugin-redo", o2 = e.detail.undo ? "undo" : "redo";
  wt(o2), ri(Ju.RedoUndo), gn(n, i, (s) => {
    if (!s.data.performed) {
      if (s.data.error && s.data.error.message) {
        Do({
          type: Re$1.ERROR,
          message: s.data.error.message
        }), y$1.emit("vite-after-update", {});
        return;
      }
      Do({
        type: Re$1.INFORMATION,
        message: `Nothing to ${o2}`
      }), y$1.emit("vite-after-update", {});
    }
  });
});
function li(e) {
  if (e.detail.files)
    return e.detail.files;
  const t = ve.getActiveDrillDownContext();
  if (t) {
    const i = ve.getCustomComponentInfo(t);
    if (i)
      return new Array(i.customComponentFilePath);
  }
  return Pu();
}
var ci = (e, t, i, n) => {
  for (var o2 = t, s = e.length - 1, a; s >= 0; s--)
    (a = e[s]) && (o2 = a(o2) || o2);
  return o2;
};
let Te = class extends lc {
  static get styles() {
    return [
      V$1(qc),
      V$1(Kc),
      V$1(Wc),
      V$1(tu),
      jl`
        :host {
          --lumo-secondary-text-color: var(--dev-tools-text-color);
          --lumo-contrast-80pct: var(--dev-tools-text-color-emphasis);
          --lumo-contrast-60pct: var(--dev-tools-text-color-secondary);
          --lumo-font-size-m: 14px;

          position: fixed;
          bottom: 2.5rem;
          right: 0rem;
          visibility: visible; /* Always show, even if copilot is off */
          user-select: none;
          z-index: var(--copilot-notifications-container-z-index);

          --dev-tools-text-color: rgba(255, 255, 255, 0.8);

          --dev-tools-text-color-secondary: rgba(255, 255, 255, 0.65);
          --dev-tools-text-color-emphasis: rgba(255, 255, 255, 0.95);
          --dev-tools-text-color-active: rgba(255, 255, 255, 1);

          --dev-tools-background-color-inactive: rgba(45, 45, 45, 0.25);
          --dev-tools-background-color-active: rgba(45, 45, 45, 0.98);
          --dev-tools-background-color-active-blurred: rgba(45, 45, 45, 0.85);

          --dev-tools-border-radius: 0.5rem;
          --dev-tools-box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.05), 0 4px 12px -2px rgba(0, 0, 0, 0.4);

          --dev-tools-blue-hsl: 206, 100%, 70%;
          --dev-tools-blue-color: hsl(var(--dev-tools-blue-hsl));
          --dev-tools-green-hsl: 145, 80%, 42%;
          --dev-tools-green-color: hsl(var(--dev-tools-green-hsl));
          --dev-tools-grey-hsl: 0, 0%, 50%;
          --dev-tools-grey-color: hsl(var(--dev-tools-grey-hsl));
          --dev-tools-yellow-hsl: 38, 98%, 64%;
          --dev-tools-yellow-color: hsl(var(--dev-tools-yellow-hsl));
          --dev-tools-red-hsl: 355, 100%, 68%;
          --dev-tools-red-color: hsl(var(--dev-tools-red-hsl));

          /* Needs to be in ms, used in JavaScript as well */
          --dev-tools-transition-duration: 180ms;

          /* Copilot go to source file link styling */
          --source-file-link-color: var(--dev-tools-text-color-secondary);
          --source-file-link-text-decoration: underline;
          --source-file-link-font-weight: 500;
          --source-file-link-button-color: white;
        }

        .notification-tray {
          display: flex;
          flex-direction: column-reverse;
          align-items: flex-end;
          margin: 0.5rem;
          flex: none;
        }

        @supports (backdrop-filter: blur(1px)) {
          .notification-tray div.message {
            backdrop-filter: blur(8px);
          }

          .notification-tray div.message {
            background-color: var(--dev-tools-background-color-active-blurred);
          }
        }

        .notification-tray .message {
          animation: slideIn var(--dev-tools-transition-duration);
          background-color: var(--dev-tools-background-color-active);
          border-radius: var(--dev-tools-border-radius);
          box-shadow: var(--dev-tools-box-shadow);
          box-sizing: border-box;
          color: var(--dev-tools-text-color);
          flex-direction: row;
          gap: var(--space-100);
          justify-content: space-between;
          margin-top: var(--space-100);
          max-width: 40rem;
          padding-block: var(--space-100);
          pointer-events: auto;
          transform-origin: bottom right;
          transition: var(--dev-tools-transition-duration);
        }

        .notification-tray .message.animate-out {
          animation: slideOut forwards var(--dev-tools-transition-duration);
        }

        .notification-tray .message .message-details {
          word-break: break-all;
        }

        .message.information {
          --dev-tools-notification-color: var(--dev-tools-blue-color);
        }

        .message.warning {
          --dev-tools-notification-color: var(--dev-tools-yellow-color);
        }

        .message.error {
          --dev-tools-notification-color: var(--dev-tools-red-color);
        }

        .message {
          background-clip: padding-box;
          display: flex;
          padding-block: var(--space-75);
          padding-inline: var(--space-450) var(--space-100);
        }

        .message.log {
          padding-left: 0.75rem;
        }

        .message-content {
          align-self: center;
          flex: 1 1 auto;
          max-width: 100%;
          min-width: 0;
          user-select: text;
          -moz-user-select: text;
          -webkit-user-select: text;
        }

        .message .message-details {
          align-items: start;
          color: rgba(255, 255, 255, 0.7);
          display: flex;
          flex-direction: column;
        }

        .message .message-details[hidden] {
          display: none;
        }

        .message .message-details p {
          display: inline;
          margin: 0;
          margin-right: 0.375em;
          word-break: break-word;
        }

        .message .message-details vaadin-details {
          margin: 0;
          width: 100%;
        }

        .message .message-details vaadin-details-summary {
          font-size: var(--font-size-1);
          font-weight: var(--font-weight-medium);
          line-height: var(--line-height-1);
        }

        .message .persist::before {
        }

        .message .persist:hover::before {
        }

        .message .persist.on::before {
        }

        .message.log {
          color: var(--dev-tools-text-color-secondary);
        }

        .message-heading {
          color: white;
        }

        .message-heading::before {
          height: var(--icon-size-m);
          margin-inline-start: calc((var(--space-400) / -1) + ((var(--space-400) - var(--icon-size-m)) / 2));
          position: absolute;
          width: var(--icon-size-m);
        }

        .message.information .message-heading::before {
          content: url("data:image/svg+xml,%3Csvg width='18' height='18' viewBox='0 0 24 24' fill='none' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M12 16V12M12 8H12.01M22 12C22 17.5228 17.5228 22 12 22C6.47715 22 2 17.5228 2 12C2 6.47715 6.47715 2 12 2C17.5228 2 22 6.47715 22 12Z' stroke='%2395C6FF' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E");
        }

        .message.warning .message-heading::before,
        .message.error .message-heading::before {
          content: url("data:image/svg+xml,%3Csvg width='18' height='18' viewBox='0 0 24 24' fill='none' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M12 8V12M12 16H12.01M22 12C22 17.5228 17.5228 22 12 22C6.47715 22 2 17.5228 2 12C2 6.47715 6.47715 2 12 2C17.5228 2 22 6.47715 22 12Z' stroke='%23ff707a' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E");
        }

        @keyframes slideIn {
          from {
            transform: translateX(100%);
            opacity: 0;
          }
          to {
            transform: translateX(0%);
            opacity: 1;
          }
        }

        @keyframes slideOut {
          from {
            transform: translateX(0%);
            opacity: 1;
          }
          to {
            transform: translateX(100%);
            opacity: 0;
          }
        }

        @keyframes fade-in {
          0% {
            opacity: 0;
          }
        }

        @keyframes bounce {
          0% {
            transform: scale(0.8);
          }
          50% {
            transform: scale(1.5);
            background-color: hsla(var(--dev-tools-red-hsl), 1);
          }
          100% {
            transform: scale(1);
          }
        }
      `
    ];
  }
  render() {
    return ee`<div class="notification-tray">
      ${m$1.notifications.map((e) => this.renderNotification(e))}
    </div>`;
  }
  renderNotification(e) {
    return ee`
      <div
        class="message ${e.type} ${e.animatingOut ? "animate-out" : ""} ${e.details || e.link ? "has-details" : ""}"
        data-testid="message">
        <div class="message-content">
          <h2 class="message-heading font-bold m-0 relative text-1">${e.message}</h2>
          <div class="message-details" ?hidden="${!e.details && !e.link}">
            ${Mu(e.details)}
            ${e.link ? ee`<a class="ahreflike" href="${e.link}" target="_blank">Learn more</a>` : ""}
          </div>
          <!-- TODO: This needs to be an actual checkbox -->
          ${e.dismissId ? ee` <hr class="border-b border-white/10 border-e-0 border-s-0 border-t-0 mb-25 mt-100" />
                <div
                  class="flex gap-75 items-center py-75 relative hover:text-white"
                  @click=${() => {
      this.toggleDontShowAgain(e);
    }}>
                  ${ee`${e.dontShowAgain ? o.checkSquare : o.square}`}
                  ${pi(e)}
                </div>` : ""}
        </div>
        <button
          aria-label="Close"
          class="icon -me-50 -my-50 text-inherit"
          id="dismiss"
          title="Close"
          @click=${(t) => {
      Po(e), t.stopPropagation();
    }}>
          ${o.x}
        </button>
      </div>
    `;
  }
  toggleDontShowAgain(e) {
    e.dontShowAgain = !e.dontShowAgain, this.requestUpdate();
  }
};
Te = ci([
  Vl("copilot-notifications-container")
], Te);
function pi(e) {
  return e.dontShowAgainMessage ? e.dontShowAgainMessage : "Do not show this again";
}
Do({
  type: Re$1.WARNING,
  message: "Development Mode",
  details: "This application is running in development mode.",
  dismissId: "devmode"
});
const me = ou(async () => {
  await du();
}, 100);
y$1.on("vite-after-update", () => {
  m$1.active && me();
});
function tt() {
  m$1.active && (me.clear(), me(), sd());
}
if (window.__REACT_DEVTOOLS_GLOBAL_HOOK__) {
  const e = window.__REACT_DEVTOOLS_GLOBAL_HOOK__, t = e.onCommitFiberRoot;
  e.onCommitFiberRoot = (i, n, o2, s) => (tt(), t(i, n, o2, s));
}
const He = window?.Vaadin?.connectionState?.stateChangeListeners;
He ? He.add((e, t) => {
  e === "loading" && t === "connected" && m$1.active && tt();
}) : console.warn("Unable to add listener for connection state changes");
y$1.on("copilot-plugin-state", (e) => {
  m$1.setIdePluginState(e.detail), e.preventDefault();
});
y$1.on("copilot-early-project-state", (e) => {
  yn.setSpringSecurityEnabled(e.detail.springSecurityEnabled), yn.setSpringJpaDataEnabled(e.detail.springJpaDataEnabled), yn.setSupportsHilla(e.detail.supportsHilla), yn.setSpringApplication(e.detail.springApplication), yn.setUrlPrefix(e.detail.urlPrefix), yn.setServerVersions(e.detail.serverVersions), yn.setJdkInfo(e.detail.jdkInfo), Bo() === "success" && wt("hotswap-active", { value: od() }), e.preventDefault();
});
y$1.on("copilot-ide-notification", (e) => {
  Do({
    type: Re$1[e.detail.type],
    message: e.detail.message,
    dismissId: e.detail.dismissId
  }), e.preventDefault();
});
/**
 * @license
 * Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
 * This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
 * The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
 * The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
 * Code distributed by Google as part of the polymer project is also
 * subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
 */
let Ue = 0, it = 0;
const j = [];
let be = false;
function hi() {
  be = false;
  const e = j.length;
  for (let t = 0; t < e; t++) {
    const i = j[t];
    if (i)
      try {
        i();
      } catch (n) {
        setTimeout(() => {
          throw n;
        });
      }
  }
  j.splice(0, e), it += e;
}
const ui = {
  /**
   * Enqueues a function called at microtask timing.
   *
   * @memberof microTask
   * @param {!Function=} callback Callback to run
   * @return {number} Handle used for canceling task
   */
  run(e) {
    be || (be = true, queueMicrotask(() => hi())), j.push(e);
    const t = Ue;
    return Ue += 1, t;
  },
  /**
   * Cancels a previously enqueued `microTask` callback.
   *
   * @memberof microTask
   * @param {number} handle Handle returned from `run` of callback to cancel
   * @return {void}
   */
  cancel(e) {
    const t = e - it;
    if (t >= 0) {
      if (!j[t])
        throw new Error(`invalid async handle: ${e}`);
      j[t] = null;
    }
  }
};
/**
@license
Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/
const je = /* @__PURE__ */ new Set();
class ne {
  /**
   * Creates a debouncer if no debouncer is passed as a parameter
   * or it cancels an active debouncer otherwise. The following
   * example shows how a debouncer can be called multiple times within a
   * microtask and "debounced" such that the provided callback function is
   * called once. Add this method to a custom element:
   *
   * ```js
   * import {microTask} from '@vaadin/component-base/src/async.js';
   * import {Debouncer} from '@vaadin/component-base/src/debounce.js';
   * // ...
   *
   * _debounceWork() {
   *   this._debounceJob = Debouncer.debounce(this._debounceJob,
   *       microTask, () => this._doWork());
   * }
   * ```
   *
   * If the `_debounceWork` method is called multiple times within the same
   * microtask, the `_doWork` function will be called only once at the next
   * microtask checkpoint.
   *
   * Note: In testing it is often convenient to avoid asynchrony. To accomplish
   * this with a debouncer, you can use `enqueueDebouncer` and
   * `flush`. For example, extend the above example by adding
   * `enqueueDebouncer(this._debounceJob)` at the end of the
   * `_debounceWork` method. Then in a test, call `flush` to ensure
   * the debouncer has completed.
   *
   * @param {Debouncer?} debouncer Debouncer object.
   * @param {!AsyncInterface} asyncModule Object with Async interface
   * @param {function()} callback Callback to run.
   * @return {!Debouncer} Returns a debouncer object.
   */
  static debounce(t, i, n) {
    return t instanceof ne ? t._cancelAsync() : t = new ne(), t.setConfig(i, n), t;
  }
  constructor() {
    this._asyncModule = null, this._callback = null, this._timer = null;
  }
  /**
   * Sets the scheduler; that is, a module with the Async interface,
   * a callback and optional arguments to be passed to the run function
   * from the async module.
   *
   * @param {!AsyncInterface} asyncModule Object with Async interface.
   * @param {function()} callback Callback to run.
   * @return {void}
   */
  setConfig(t, i) {
    this._asyncModule = t, this._callback = i, this._timer = this._asyncModule.run(() => {
      this._timer = null, je.delete(this), this._callback();
    });
  }
  /**
   * Cancels an active debouncer and returns a reference to itself.
   *
   * @return {void}
   */
  cancel() {
    this.isActive() && (this._cancelAsync(), je.delete(this));
  }
  /**
   * Cancels a debouncer's async callback.
   *
   * @return {void}
   */
  _cancelAsync() {
    this.isActive() && (this._asyncModule.cancel(
      /** @type {number} */
      this._timer
    ), this._timer = null);
  }
  /**
   * Flushes an active debouncer and returns a reference to itself.
   *
   * @return {void}
   */
  flush() {
    this.isActive() && (this.cancel(), this._callback());
  }
  /**
   * Returns true if the debouncer is active.
   *
   * @return {boolean} True if active.
   */
  isActive() {
    return this._timer != null;
  }
}
/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */
const W = (e, t) => {
  const i = e._$AN;
  if (i === void 0) return false;
  for (const n of i) n._$AO?.(t, false), W(n, t);
  return true;
}, oe = (e) => {
  let t, i;
  do {
    if ((t = e._$AM) === void 0) break;
    i = t._$AN, i.delete(e), e = t;
  } while (i?.size === 0);
}, nt = (e) => {
  for (let t; t = e._$AM; e = t) {
    let i = t._$AN;
    if (i === void 0) t._$AN = i = /* @__PURE__ */ new Set();
    else if (i.has(e)) break;
    i.add(e), vi(t);
  }
};
function gi(e) {
  this._$AN !== void 0 ? (oe(this), this._$AM = e, nt(this)) : this._$AM = e;
}
function fi(e, t = false, i = 0) {
  const n = this._$AH, o2 = this._$AN;
  if (o2 !== void 0 && o2.size !== 0) if (t) if (Array.isArray(n)) for (let s = i; s < n.length; s++) W(n[s], false), oe(n[s]);
  else n != null && (W(n, false), oe(n));
  else W(this, e);
}
const vi = (e) => {
  e.type == So.CHILD && (e._$AP ??= fi, e._$AQ ??= gi);
};
class mi extends Co {
  constructor() {
    super(...arguments), this._$AN = void 0;
  }
  _$AT(t, i, n) {
    super._$AT(t, i, n), nt(this), this.isConnected = t._$AU;
  }
  _$AO(t, i = true) {
    t !== this.isConnected && (this.isConnected = t, t ? this.reconnected?.() : this.disconnected?.()), i && (W(this, t), oe(this));
  }
  setValue(t) {
    if (Zu(this._$Ct)) this._$Ct._$AI(t, this);
    else {
      const i = [...this._$Ct._$AH];
      i[this._$Ci] = t, this._$Ct._$AI(i, this, 0);
    }
  }
  disconnected() {
  }
  reconnected() {
  }
}
/**
 * @license
 * Copyright (c) 2016 - 2025 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */
const Ne = Symbol("valueNotInitialized");
class bi extends mi {
  constructor(t) {
    if (super(t), t.type !== So.ELEMENT)
      throw new Error(`\`${this.constructor.name}\` must be bound to an element.`);
    this.previousValue = Ne;
  }
  /** @override */
  render(t, i) {
    return A$1;
  }
  /** @override */
  update(t, [i, n]) {
    return this.hasChanged(n) ? (this.host = t.options && t.options.host, this.element = t.element, this.renderer = i, this.previousValue === Ne ? this.addRenderer() : this.runRenderer(), this.previousValue = Array.isArray(n) ? [...n] : n, A$1) : A$1;
  }
  /** @override */
  reconnected() {
    this.addRenderer();
  }
  /** @override */
  disconnected() {
    this.removeRenderer();
  }
  /** @abstract */
  addRenderer() {
    throw new Error("The `addRenderer` method must be implemented.");
  }
  /** @abstract */
  runRenderer() {
    throw new Error("The `runRenderer` method must be implemented.");
  }
  /** @abstract */
  removeRenderer() {
    throw new Error("The `removeRenderer` method must be implemented.");
  }
  /** @protected */
  renderRenderer(t, ...i) {
    const n = this.renderer.call(this.host, ...i);
    oc(n, t, { host: this.host });
  }
  /** @protected */
  hasChanged(t) {
    return Array.isArray(t) ? !Array.isArray(this.previousValue) || this.previousValue.length !== t.length ? true : t.some((i, n) => i !== this.previousValue[n]) : this.previousValue !== t;
  }
}
/**
 * @license
 * Copyright (c) 2017 - 2025 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */
const ue = Symbol("contentUpdateDebouncer");
class $e extends bi {
  /**
   * A property to that the renderer callback will be assigned.
   *
   * @abstract
   */
  get rendererProperty() {
    throw new Error("The `rendererProperty` getter must be implemented.");
  }
  /**
   * Adds the renderer callback to the dialog.
   */
  addRenderer() {
    this.element[this.rendererProperty] = (t, i) => {
      this.renderRenderer(t, i);
    };
  }
  /**
   * Runs the renderer callback on the dialog.
   */
  runRenderer() {
    this.element[ue] = ne.debounce(
      this.element[ue],
      ui,
      () => {
        this.element.requestContentUpdate();
      }
    );
  }
  /**
   * Removes the renderer callback from the dialog.
   */
  removeRenderer() {
    this.element[this.rendererProperty] = null, delete this.element[ue];
  }
}
class wi extends $e {
  get rendererProperty() {
    return "renderer";
  }
}
class yi extends $e {
  get rendererProperty() {
    return "headerRenderer";
  }
}
class xi extends $e {
  get rendererProperty() {
    return "footerRenderer";
  }
}
const ot = No(wi), st = No(yi), at = No(xi);
var Pi = Object.defineProperty, Ii = Object.getOwnPropertyDescriptor, rt = (e, t, i, n) => {
  for (var o2 = n > 1 ? void 0 : n ? Ii(t, i) : t, s = e.length - 1, a; s >= 0; s--)
    (a = e[s]) && (o2 = (n ? a(t, i, o2) : a(o2)) || o2);
  return n && o2 && Pi(t, i, o2), o2;
};
let we = class extends ht {
  constructor() {
    super(...arguments), this.rememberChoice = false, this.opened = false, this.handleESC = (e) => {
      !m$1.active || !this.opened || (e.key === "Escape" && this.sendEvent("cancel"), e.preventDefault(), e.stopPropagation());
    };
  }
  firstUpdated(e) {
    super.firstUpdated(e), i$1(this.renderRoot);
  }
  connectedCallback() {
    super.connectedCallback(), this.addESCListener();
  }
  disconnectedCallback() {
    super.disconnectedCallback(), this.removeESCListener();
  }
  render() {
    return ee` <vaadin-dialog
      id="ai-dialog"
      no-close-on-outside-click
      overlay-class="ai-dialog"
      ?opened=${this.opened}
      ${st(
      () => ee`
          <h2>This Operation Uses AI</h2>
          ${o.starsAlt}
        `
    )}
      ${ot(
      () => ee`
          <p>AI is a third-party service that will receive some of your project code as context for the operation.</p>
          <label>
            <input
              id="ai-dialog-checkbox"
              type="checkbox"
              @change=${(e) => {
        this.rememberChoice = e.target.checked;
      }} />Don’t ask again
          </label>
        `
    )}
      ${at(
      () => ee`
          <button @click=${() => this.sendEvent("cancel")}>Cancel</button>
          <button class="primary" @click=${() => this.sendEvent("ok")}>OK</button>
        `
    )}></vaadin-dialog>`;
  }
  sendEvent(e) {
    this.dispatchEvent(
      new CustomEvent("ai-usage-response", {
        detail: { response: e, rememberChoice: this.rememberChoice }
      })
    );
  }
  addESCListener() {
    document.addEventListener("keydown", this.handleESC, { capture: true });
  }
  removeESCListener() {
    document.removeEventListener("keydown", this.handleESC, { capture: true });
  }
};
rt([
  h()
], we.prototype, "opened", 2);
we = rt([
  Vl("copilot-ai-usage-confirmation-dialog")
], we);
var Ci = Object.defineProperty, Ai = Object.getOwnPropertyDescriptor, H = (e, t, i, n) => {
  for (var o2 = n > 1 ? void 0 : n ? Ai(t, i) : t, s = e.length - 1, a; s >= 0; s--)
    (a = e[s]) && (o2 = (n ? a(t, i, o2) : a(o2)) || o2);
  return n && o2 && Ci(t, i, o2), o2;
};
const Be = {
  info: "UI state info",
  stacktrace: "Exception details",
  versions: "Vaadin, Java, OS, etc.."
};
let D = class extends ht {
  constructor() {
    super(...arguments), this.exceptionReport = void 0, this.dialogOpened = false, this.visibleItemIndex = 0, this.versions = void 0, this.selectedItems = [], this.eventListener = (e) => {
      this.exceptionReport = e.detail, this.selectedItems = this.exceptionReport.items.map((t, i) => i), this.visibleItemIndex = 0, this.searchInputValue = void 0, this.dialogOpened = this.exceptionReport !== void 0;
    };
  }
  connectedCallback() {
    super.connectedCallback(), y$1.on("submit-exception-report-clicked", this.eventListener);
  }
  disconnectedCallback() {
    super.disconnectedCallback(), y$1.off("submit-exception-report-clicked", this.eventListener);
  }
  firstUpdated(e) {
    super.firstUpdated(e), i$1(this.renderRoot);
  }
  close() {
    this.dialogOpened = false;
  }
  clear() {
    this.exceptionReport = void 0;
  }
  render() {
    let e = "";
    return this.exceptionReport && this.exceptionReport.items.length > 0 && (e = this.exceptionReport.items[this.visibleItemIndex].content), ee` <vaadin-dialog
      id="report-exception-dialog"
      overlay-class="report-exception-dialog"
      no-close-on-outside-click
      draggable
      ?opened=${this.dialogOpened}
      @closed="${() => {
      this.clear();
    }}"
      @opened-changed="${(t) => {
      t.detail.value || this.close();
    }}"
      ${st(
      () => ee`
          <div
            class="draggable"
            style="display: flex; justify-content: space-between; align-items: center; width: 100%">
            <h2>Send report</h2>
            <vaadin-button theme="tertiary" @click="${this.close}">
              <vaadin-icon icon="lumo:cross"></vaadin-icon>
            </vaadin-button>
          </div>
        `
    )}
      ${ot(
      () => ee`
          <div class="description-container">
            <vaadin-text-area
              @input=${(t) => {
        this.searchInputValue = t.target.value;
      }}
              label="Description of the Bug"
              placeholder="A short, concise description of the bug and why you consider it a bug."></vaadin-text-area>
          </div>
          <div class="list-preview-container">
            <div class="left-menu">
              <div class="section-title">Include in Report</div>
              <vaadin-list-box
                single
                selected="${this.visibleItemIndex}"
                @selected-changed="${(t) => {
        this.visibleItemIndex = t.detail.value;
      }}">
                ${this.exceptionReport?.items.map(
        (t, i) => ee` <vaadin-item>
                      <input
                        type="checkbox"
                        .checked="${this.selectedItems.indexOf(i) !== -1}"
                        @change="${(n) => {
          const o2 = n.target, s = [...this.selectedItems];
          if (o2.checked)
            s.push(i), this.selectedItems = [...this.selectedItems];
          else {
            const a = this.selectedItems.indexOf(i);
            s.splice(a, 1);
          }
          this.selectedItems = s;
        }}" />
                      <div class="item-content">
                        <span class="item-name"> ${t.name} </span>
                        <span class="item-description">${this.renderItemDescription(t)}</span>
                      </div>
                    </vaadin-item>`
      )}
              </vaadin-list-box>
            </div>
            <div class="right-menu">
              <div class="section-title">Preview: ${this.exceptionReport?.items[this.visibleItemIndex].name}</div>
              <code class="codeblock">${e}</code>
            </div>
          </div>
        `,
      [this.exceptionReport, this.visibleItemIndex, this.selectedItems]
    )}
      ${at(
      () => ee`
          <button
            id="cancel"
            @click=${() => {
        this.close();
      }}>
            Cancel
          </button>

          <button
            id="submit"
            class="primary"
            @click=${() => {
        this.submitErrorToGithub(), this.close();
      }}>
            Submit in GitHub
            <vaadin-tooltip
              for="submit"
              text="${this.bodyLengthExceeds() ? "The error report will be copied to clipboard and blank new issue page will be opened" : "New issue page will be opened with data loaded"}"
              position="top-start"></vaadin-tooltip>
          </button>
        `,
      [this.exceptionReport, this.selectedItems, this.searchInputValue]
    )}></vaadin-dialog>`;
  }
  renderItemDescription(e) {
    return Object.keys(Be).indexOf(e.name.toLowerCase()) !== -1 ? Be[e.name.toLowerCase()] : null;
  }
  bodyLengthExceeds() {
    const e = this.getIssueBodyNotEncoded();
    return e !== void 0 && encodeURIComponent(e).length > 7500;
  }
  getIssueBodyNotEncoded() {
    if (!this.exceptionReport)
      return;
    const e = this.exceptionReport.items.filter((t, i) => this.selectedItems.indexOf(i) !== -1).map((t) => {
      let i = "```";
      return t.name.includes(".java") && (i = `${i}java`), `## ${t.name} 
 
 ${i}
${t.content}
\`\`\``;
    });
    return this.searchInputValue ? `## Description of the bug 
 ${this.searchInputValue} 
 ${e.join(`
`)}` : `## Description of the bug 
 Please enter bug description here 
 ${e.join(`
`)}`;
  }
  submitErrorToGithub() {
    const e = this.exceptionReport;
    if (!e)
      return;
    const t = encodeURIComponent(e.title ?? "Bug report "), i = this.getIssueBodyNotEncoded();
    if (!i)
      return;
    let n = encodeURIComponent(i);
    n.length >= 7500 && (T(i), n = encodeURIComponent("Please paste report here. It was automatically added to your clipboard."));
    const o2 = `https://github.com/vaadin/copilot/issues/new?title=${t}&body=${n}`;
    window.open(o2, "_blank");
  }
};
H([
  b()
], D.prototype, "exceptionReport", 2);
H([
  b()
], D.prototype, "dialogOpened", 2);
H([
  b()
], D.prototype, "visibleItemIndex", 2);
H([
  b()
], D.prototype, "versions", 2);
H([
  b()
], D.prototype, "selectedItems", 2);
H([
  b()
], D.prototype, "searchInputValue", 2);
D = H([
  Vl("copilot-report-exception-dialog")
], D);
let Q;
y$1.on("copilot-project-compilation-error", (e) => {
  if (e.detail.error) {
    let t;
    if (e.detail.files && e.detail.files.length > 0) {
      const i = m$1.idePluginState?.supportedActions?.includes("undo") ? ee`
            <button
              class="text-white"
              @click="${(n) => {
        n.preventDefault(), y$1.emit("undoRedo", { undo: true, files: e.detail.files?.map((o2) => o2.path) });
      }}">
              <span aria-hidden="true" class="prefix">${o.flipBack}</span>
              Undo Last Change
            </button>
          ` : A$1;
      t = nn(
        ee`<div>
          <span> Following files have compilation errors: </span>
          <ul class="mb-0 mt-25 ps-200">
            ${e.detail.files.map(
          (n) => ee` <li>
                  <button
                    class="-ms-75 text-white"
                    @click="${() => {
            y$1.emit("show-in-ide", { javaSource: { absoluteFilePath: n.path } });
          }}">
                    ${n.name}
                  </button>
                </li>`
        )}
          </ul>
          <hr class="border-b border-white/10 border-e-0 border-s-0 border-t-0 my-50" />
          ${i}
        </div>`
      );
    } else
      t = "Project contains one or more compilation errors.";
    Q = Do({
      message: "Compilation error",
      details: t,
      type: Re$1.WARNING,
      delay: 3e4
    });
  } else
    Q && Po(Q), Q = void 0;
});
var $i = Object.defineProperty, ki = Object.getOwnPropertyDescriptor, lt = (e, t, i, n) => {
  for (var o2 = n > 1 ? void 0 : n ? ki(t, i) : t, s = e.length - 1, a; s >= 0; s--)
    (a = e[s]) && (o2 = (n ? a(t, i, o2) : a(o2)) || o2);
  return n && o2 && $i(t, i, o2), o2;
};
let ye = class extends ht {
  constructor() {
    super(...arguments), this.text = () => (this.parentElement.textContent ?? "").trim();
  }
  createRenderRoot() {
    return this;
  }
  render() {
    return ee`<button
      aria-label="Copy to Clipboard"
      class="icon"
      title="Copy to Clipboard"
      @click=${(e) => {
      e.stopPropagation(), e.preventDefault();
      const t = this.text();
      T(t);
    }}>
      ${o.copy}
    </button>`;
  }
};
lt([
  h({ type: Function })
], ye.prototype, "text", 2);
ye = lt([
  Vl("copilot-copy")
], ye);
var Si = {
  202: "Accepted",
  502: "Bad Gateway",
  400: "Bad Request",
  409: "Conflict",
  100: "Continue",
  201: "Created",
  417: "Expectation Failed",
  424: "Failed Dependency",
  403: "Forbidden",
  504: "Gateway Timeout",
  410: "Gone",
  505: "HTTP Version Not Supported",
  418: "I'm a teapot",
  419: "Insufficient Space on Resource",
  507: "Insufficient Storage",
  500: "Internal Server Error",
  411: "Length Required",
  423: "Locked",
  420: "Method Failure",
  405: "Method Not Allowed",
  301: "Moved Permanently",
  302: "Moved Temporarily",
  207: "Multi-Status",
  300: "Multiple Choices",
  511: "Network Authentication Required",
  204: "No Content",
  203: "Non Authoritative Information",
  406: "Not Acceptable",
  404: "Not Found",
  501: "Not Implemented",
  304: "Not Modified",
  200: "OK",
  206: "Partial Content",
  402: "Payment Required",
  308: "Permanent Redirect",
  412: "Precondition Failed",
  428: "Precondition Required",
  102: "Processing",
  103: "Early Hints",
  426: "Upgrade Required",
  407: "Proxy Authentication Required",
  431: "Request Header Fields Too Large",
  408: "Request Timeout",
  413: "Request Entity Too Large",
  414: "Request-URI Too Long",
  416: "Requested Range Not Satisfiable",
  205: "Reset Content",
  303: "See Other",
  503: "Service Unavailable",
  101: "Switching Protocols",
  307: "Temporary Redirect",
  429: "Too Many Requests",
  401: "Unauthorized",
  451: "Unavailable For Legal Reasons",
  422: "Unprocessable Entity",
  415: "Unsupported Media Type",
  305: "Use Proxy",
  421: "Misdirected Request"
};
function Ei(e) {
  var t = Si[e.toString()];
  if (!t)
    throw new Error("Status code does not exist: " + e);
  return t;
}
function dt(e) {
  return `endpoint-request-${e.id}`;
}
y$1.on("endpoint-request", (e) => {
  const t = e.detail, i = dt(t);
  delete t.id;
  const n = Object.values(t.params), o2 = n.map(se).join(", ");
  y$1.emit("log", {
    id: i,
    type: Re$1.INFORMATION,
    message: `Called endpoint ${t.endpoint}.${t.method}(${o2})`,
    expandedMessage: nn(
      ee`Called endpoint ${t.endpoint}.${t.method} with parameters
        <code class="codeblock"><copilot-copy></copilot-copy>${se(n)}</code>`
    ),
    details: "Response: <pending>"
  });
});
y$1.on("endpoint-response", (e) => {
  let t;
  try {
    t = JSON.parse(e.detail.text);
  } catch {
    t = e.detail.text;
  }
  const i = {}, n = e.detail.status ?? 200;
  n === 200 ? (i.details = `Response: ${se(t)}`, i.expandedDetails = nn(
    ee`Response: <code class="codeblock"><copilot-copy></copilot-copy>${se(t)}</code>`
  )) : (i.details = `Error: ${n} ${Ei(n)}`, i.type = Re$1.ERROR), y$1.emit("update-log", {
    id: dt(e.detail),
    ...i
  });
});
function se(e) {
  return typeof e == "string" ? `${e}` : JSON.stringify(e, void 0, 2);
}
var Ri = Object.defineProperty, Di = Object.getOwnPropertyDescriptor, U = (e, t, i, n) => {
  for (var o2 = n > 1 ? void 0 : n ? Di(t, i) : t, s = e.length - 1, a; s >= 0; s--)
    (a = e[s]) && (o2 = (n ? a(t, i, o2) : a(o2)) || o2);
  return n && o2 && Ri(t, i, o2), o2;
};
class Li extends CustomEvent {
  constructor(t) {
    super("show-in-ide-clicked", {
      detail: t,
      bubbles: true,
      composed: true
    });
  }
}
let C = class extends ht {
  constructor() {
    super(...arguments), this.iconHidden = false, this.linkHidden = false, this.tooltipText = void 0, this.linkText = void 0, this.source = void 0, this.javaSource = void 0;
  }
  static get styles() {
    return [
      V$1(Kc),
      jl`
        :host {
          display: inline-block;
        }
        :host .content {
          display: flex;
          align-items: center;
        }
        a {
          cursor: pointer;
          color: var(--source-file-link-color, var(--blue-600));
          text-decoration: var(--source-file-link-text-decoration, none);
          text-decoration-color: var(--source-file-link-decoration-color, currentColor);
          font-weight: var(--source-file-link-font-weight, normal);
        }
        button {
          color: var(--source-file-link-button-color, currentColor);
        }
      `
    ];
  }
  firstUpdated(e) {
    super.firstUpdated(e), i$1(this.shadowRoot);
  }
  render() {
    if (this.iconHidden) {
      if (!this.linkHidden)
        return this.renderContent(this.renderAnchor());
    } else return this.linkHidden ? this.renderContent(this.renderIcon()) : this.renderContent([this.renderIcon(), this.renderAnchor()]);
    return A$1;
  }
  renderContent(e) {
    return ee` <div class="content">${e}</div> `;
  }
  renderIcon() {
    const e = this.tooltipText ?? `Open ${this.getFileName()} in IDE`;
    return ee`
      <button
        id="show-in-ide"
        @click=${(t) => {
      t.stopPropagation(), t.preventDefault(), this._showInIde();
    }}
        aria-label="${e}"
        class="icon">
        <span>${o.fileCodeAlt}</span>
      </button>
      ${this.renderTooltip("show-in-ide")}
    `;
  }
  renderAnchor() {
    return ee`
      <a
        id="link"
        href="#"
        class="ahreflike"
        @click=${(e) => {
      e.preventDefault(), this._showInIde();
    }}
        >${this.linkText ?? this.getFileName() ?? ""}</a
      >
      ${this.renderTooltip("link")}
    `;
  }
  dispatchClickedEvent() {
    this.dispatchEvent(
      new Li({
        source: this.source,
        javaSource: this.javaSource
      })
    );
  }
  renderTooltip(e) {
    const t = this.tooltipText ?? `Open ${this.getFileName()} in IDE`;
    return ee`<vaadin-tooltip for="${e}" text="${t}" position="top-start"></vaadin-tooltip>`;
  }
  getFileName() {
    if (this.tooltipText)
      return this.tooltipText;
    if (this.source && this.source.fileName)
      return this.source.fileName;
    if (this.javaSource)
      return this.javaSource.className;
  }
  _showInIde() {
    y$1.emit("show-in-ide", {
      source: this.source,
      javaSource: this.javaSource
    }), this.dispatchClickedEvent();
  }
};
C.TAG = "copilot-go-to-source";
U([
  h({ type: Boolean })
], C.prototype, "iconHidden", 2);
U([
  h({ type: Boolean })
], C.prototype, "linkHidden", 2);
U([
  h()
], C.prototype, "tooltipText", 2);
U([
  h()
], C.prototype, "linkText", 2);
U([
  h()
], C.prototype, "source", 2);
U([
  h()
], C.prototype, "javaSource", 2);
C = U([
  Vl(C.TAG)
], C);
y$1.on("copilot-java-after-update", (e) => {
  const t = e.detail.classes.filter((n) => n.redefined).map((n) => n.class).join(", ");
  if (t.length === 0)
    return;
  const i = "java-hot-deploy";
  e.detail.classes.find((n) => n.routePath !== void 0) && y$1.emit("update-routes", {}), Do({
    type: Re$1.INFORMATION,
    message: `Java changes were hot deployed for ${ju(t)}`,
    dismissId: i,
    delay: 5e3
  });
});
