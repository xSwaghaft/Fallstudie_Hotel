import { X as Xu, V as Vl, y as y$1, A, e as ee, b as le, r as pc, s as ld } from "./indexhtml-jcdUVBfh.js";
import { l } from "./base-panel-Bh_ywZZN-PMSDJ968.js";
import { o } from "./icons-iLxqH3Sc-DkBBcM_q.js";
const y = 'copilot-shortcuts-panel{display:flex;flex-direction:column;padding:var(--space-150)}copilot-shortcuts-panel h3{font:var(--font-xsmall-semibold);margin-bottom:var(--space-100);margin-top:0}copilot-shortcuts-panel h3:not(:first-of-type){margin-top:var(--space-200)}copilot-shortcuts-panel ul{display:flex;flex-direction:column;list-style:none;margin:0;padding:0}copilot-shortcuts-panel ul li{display:flex;align-items:center;gap:var(--space-50);position:relative}copilot-shortcuts-panel ul li:not(:last-of-type):before{border-bottom:1px dashed var(--border-color);content:"";inset:auto 0 0 calc(var(--size-m) + var(--space-50));position:absolute}copilot-shortcuts-panel ul li span:has(svg){align-items:center;display:flex;height:var(--size-m);justify-content:center;width:var(--size-m)}copilot-shortcuts-panel .kbds{margin-inline-start:auto}copilot-shortcuts-panel kbd{align-items:center;border:1px solid var(--border-color);border-radius:var(--radius-2);box-sizing:border-box;display:inline-flex;font-family:var(--font-family);font-size:var(--font-size-1);line-height:var(--line-height-1);padding:0 var(--space-50)}', u = window.Vaadin.copilot.tree;
if (!u)
  throw new Error("Tried to access copilot tree before it was initialized.");
var x = (s, i, h, p) => {
  for (var o2 = i, n = s.length - 1, r; n >= 0; n--)
    (r = s[n]) && (o2 = r(o2) || o2);
  return o2;
};
let d = class extends l {
  constructor() {
    super(), this.onTreeUpdated = () => {
      this.requestUpdate();
    };
  }
  connectedCallback() {
    super.connectedCallback(), y$1.on("copilot-tree-created", this.onTreeUpdated);
  }
  disconnectedCallback() {
    super.disconnectedCallback(), y$1.off("copilot-tree-created", this.onTreeUpdated);
  }
  render() {
    const s = u.hasFlowComponents();
    return ee`<style>
        ${y}
      </style>
      <h3>Global</h3>
      <ul>
        <li>
          <span>${o.vaadinLogo}</span>
          <span>Copilot</span>
          ${a(ld.toggleCopilot)}
        </li>
        <li>
          <span>${o.terminal}</span>
          <span>Command window</span>
          ${a(ld.toggleCommandWindow)}
        </li>
        <li>
          <span>${o.flipBack}</span>
          <span>Undo</span>
          ${a(ld.undo)}
        </li>
        <li>
          <span>${o.flipForward}</span>
          <span>Redo</span>
          ${a(ld.redo)}
        </li>
      </ul>
      <h3>Selected component</h3>
      <ul>
        <li>
          <span>${o.fileCodeAlt}</span>
          <span>Go to source</span>
          ${a(ld.goToSource)}
        </li>
        ${s ? ee`<li>
              <span>${o.code}</span>
              <span>Go to attach source</span>
              ${a(ld.goToAttachSource)}
            </li>` : A}
        <li>
          <span>${o.copy}</span>
          <span>Copy</span>
          ${a(ld.copy)}
        </li>
        <li>
          <span>${o.clipboard}</span>
          <span>Paste</span>
          ${a(ld.paste)}
        </li>
        <li>
          <span>${o.copyAlt}</span>
          <span>Duplicate</span>
          ${a(ld.duplicate)}
        </li>
        <li>
          <span>${o.userUp}</span>
          <span>Select parent</span>
          ${a(ld.selectParent)}
        </li>
        <li>
          <span>${o.userLeft}</span>
          <span>Select previous sibling</span>
          ${a(ld.selectPreviousSibling)}
        </li>
        <li>
          <span>${o.userRight}</span>
          <span>Select first child / next sibling</span>
          ${a(ld.selectNextSibling)}
        </li>
        <li>
          <span>${o.trash}</span>
          <span>Delete</span>
          ${a(ld.delete)}
        </li>
        <li>
          <span>${o.zap}</span>
          <span>Quick add from palette</span>
          ${a("<kbd>A ... Z</kbd>")}
        </li>
      </ul>`;
  }
  /**
   * Closes the panel. Used from shortcuts
   */
  close() {
    le.updatePanel("copilot-shortcuts-panel", {
      floating: false
    });
  }
};
d = x([
  Vl("copilot-shortcuts-panel")
], d);
function a(s) {
  return ee`<span class="kbds">${pc(s)}</span>`;
}
const C = Xu({
  header: "Keyboard Shortcuts",
  tag: "copilot-shortcuts-panel",
  width: 400,
  height: 550,
  floatingPosition: {
    top: 50,
    left: 50
  }
}), P = {
  init(s) {
    s.addPanel(C);
  }
};
window.Vaadin.copilot.plugins.push(P);
