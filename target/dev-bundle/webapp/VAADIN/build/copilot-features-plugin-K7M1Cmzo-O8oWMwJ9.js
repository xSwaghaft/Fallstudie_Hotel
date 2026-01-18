import { l as lc, m as m$1, A as A$1, g as fr, q as qc, h as yc, e as ee, w as wt, D as Do, j as mc, a as Re, k as Vo, V as Vl } from "./indexhtml-jcdUVBfh.js";
import { b } from "./state-DecwIYub-BDlWk3uv.js";
import { l as l$1 } from "./base-panel-Bh_ywZZN-PMSDJ968.js";
import { o as o$1 } from "./icons-iLxqH3Sc-DkBBcM_q.js";
import { i as i$1 } from "./overlay-monkeypatch-HZaVJ-wi-BczBz-Gi.js";
const P = "copilot-features-panel{padding:var(--space-100);font:var(--font-xsmall);display:grid;grid-template-columns:auto 1fr;gap:var(--space-50);height:auto}copilot-features-panel a{display:flex;align-items:center;gap:var(--space-50);white-space:nowrap}copilot-features-panel a svg{height:12px;width:12px;min-height:12px;min-width:12px}";
var S = Object.defineProperty, A = Object.getOwnPropertyDescriptor, o = (e, t, a, s) => {
  for (var r = s > 1 ? void 0 : s ? A(t, a) : t, i = e.length - 1, n; i >= 0; i--)
    (n = e[i]) && (r = (s ? n(t, a, r) : n(r)) || r);
  return s && r && S(t, a, r), r;
};
const l = window.Vaadin.devTools;
let g = class extends l$1 {
  constructor() {
    super(...arguments), this.toggledFeaturesThatAreRequiresServerRestart = [];
  }
  render() {
    return ee` <style>
        ${P}
      </style>
      ${m$1.featureFlags.map(
      (e) => ee`
          <copilot-toggle-button
            .title="${e.title}"
            ?checked=${e.enabled}
            @on-change=${(t) => this.toggleFeatureFlag(t, e)}>
          </copilot-toggle-button>
          <a class="ahreflike" href="${e.moreInfoLink}" title="Learn more" target="_blank"
            >learn more ${o$1.share}</a
          >
        `
    )}`;
  }
  toggleFeatureFlag(e, t) {
    const a = e.target.checked;
    wt("use-feature", { source: "toggle", enabled: a, id: t.id }), l.frontendConnection ? (l.frontendConnection.send("setFeature", { featureId: t.id, enabled: a }), t.requiresServerRestart && m$1.toggleServerRequiringFeatureFlag(t), Do({
      type: Re.INFORMATION,
      message: `“${t.title}” ${a ? "enabled" : "disabled"}`,
      details: t.requiresServerRestart ? mc() : void 0,
      dismissId: `feature${t.id}${a ? "Enabled" : "Disabled"}`
    }), Vo()) : l.log("error", `Unable to toggle feature ${t.title}: No server connection available`);
  }
};
o([
  b()
], g.prototype, "toggledFeaturesThatAreRequiresServerRestart", 2);
g = o([
  Vl("copilot-features-panel")
], g);
let c = class extends lc {
  constructor() {
    super(...arguments), this.serverRestarting = false;
  }
  createRenderRoot() {
    return this;
  }
  updated(e) {
    super.updated(e), i$1(this.renderRoot);
  }
  render() {
    if (m$1.serverRestartRequiringToggledFeatureFlags.length === 0)
      return A$1;
    if (!fr())
      return A$1;
    const e = this.serverRestarting ? "Restarting..." : "Click to restart server";
    return ee`
      <style>
        .fade-in-out {
          animation: fadeInOut 2s ease-in-out infinite;
          animation-play-state: running;
        }
        .fade-in-out:hover {
          animation-play-state: paused;
          opacity: 1 !important;
        }
        ${qc}
      </style>
      <button
        ?disabled="${this.serverRestarting}"
        id="restart-server-btn"
        class="icon ${this.serverRestarting ? "" : "fade-in-out"}"
        @click=${() => {
      this.serverRestarting = true, yc();
    }}>
        <span>${o$1.refresh}</span>
      </button>
      <vaadin-tooltip for="restart-server-btn" text=${e}></vaadin-tooltip>
    `;
  }
};
o([
  b()
], c.prototype, "serverRestarting", 2);
c = o([
  Vl("copilot-features-actions")
], c);
const C = {
  header: "Features",
  expanded: false,
  panelOrder: 35,
  panel: "right",
  floating: false,
  tag: "copilot-features-panel",
  helpUrl: "https://vaadin.com/docs/latest/flow/configuration/feature-flags",
  actionsTag: "copilot-features-actions"
}, I = {
  init(e) {
    e.addPanel(C);
  }
};
window.Vaadin.copilot.plugins.push(I);
export {
  c as CopilotFeaturesActions,
  g as CopilotFeaturesPanel
};
