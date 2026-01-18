import { l as lc, y as y$1, e as ee, m as m$1, c as yn, b as le, B as Bo, A as A$1, d as lu, f as ad, i as id, D as Do, a as Re, V as Vl } from "./indexhtml-jcdUVBfh.js";
import { l } from "./base-panel-Bh_ywZZN-PMSDJ968.js";
import { o } from "./icons-iLxqH3Sc-DkBBcM_q.js";
import { T as T$1 } from "./index-CjujSS76-RBB9MF2-.js";
const A = 'copilot-info-panel{--dev-tools-red-color: red;--dev-tools-grey-color: gray;--dev-tools-green-color: green;position:relative}copilot-info-panel dl{margin:0;width:100%}copilot-info-panel dl>div{align-items:center;display:flex;gap:var(--space-50);height:var(--size-m);padding:0 var(--space-150);position:relative}copilot-info-panel dl>div:after{border-bottom:1px solid var(--divider-secondary-color);content:"";inset:auto var(--space-150) 0;position:absolute}copilot-info-panel dl dt{color:var(--secondary-text-color)}copilot-info-panel dl dd{align-items:center;display:flex;font-weight:var(--font-weight-medium);gap:var(--space-50);margin:0;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}copilot-info-panel dl dd span{overflow:hidden;text-overflow:ellipsis;white-space:nowrap}copilot-info-panel dl dd span.icon{display:inline-flex;vertical-align:bottom}copilot-info-panel dd.live-reload-status>span{overflow:hidden;text-overflow:ellipsis;display:block;color:var(--status-color)}copilot-info-panel dd span.hidden{display:none}copilot-info-panel code{white-space:nowrap;-webkit-user-select:all;user-select:all}copilot-info-panel .checks{display:inline-grid;grid-template-columns:auto 1fr;gap:var(--space-50)}copilot-info-panel span.hint{font-size:var(--font-size-0);background:var(--gray-50);padding:var(--space-75);border-radius:var(--radius-2)}';
var h = (e, t, i, o2) => {
  for (var s = t, n = e.length - 1, l2; n >= 0; n--)
    (l2 = e[n]) && (s = l2(s) || s);
  return s;
};
let m = class extends l {
  connectedCallback() {
    super.connectedCallback(), this.onEventBus("system-info-with-callback", (e) => {
      e.detail.callback(this.getInfoForClipboard(e.detail.notify));
    }), this.reaction(
      () => m$1.idePluginState,
      () => {
        this.requestUpdate("serverInfo");
      }
    );
  }
  getIndex(e) {
    return yn.serverVersions.findIndex((t) => t.name === e);
  }
  render() {
    const e = m$1.newVaadinVersionState?.versions !== void 0 && m$1.newVaadinVersionState.versions.length > 0, t = [];
    m$1.userInfo?.vaadiner && t.push({
      name: "Vaadin Employee",
      version: "true"
    });
    const i = [
      ...yn.serverVersions,
      ...t,
      ...yn.clientVersions
    ].map((s) => {
      const n = { ...s };
      return n.name === "Vaadin" && (n.more = ee` <button
          aria-label="Edit Vaadin Version"
          class="icon relative"
          id="new-vaadin-version-btn"
          title="Edit Vaadin Version"
          @click="${(l2) => {
        l2.stopPropagation(), le.updatePanel("copilot-vaadin-versions", { floating: true });
      }}">
          ${o.editAlt}
          ${e ? ee`<span aria-hidden="true" class="absolute bg-error end-0 h-75 rounded-full top-0 w-75"></span>` : ""}
        </button>`), n;
    });
    let o$1 = this.getIndex("Spring") + 1;
    return o$1 === 0 && (o$1 = i.length), yn.springSecurityEnabled && (i.splice(o$1, 0, { name: "Spring Security", version: "true" }), o$1++), yn.springJpaDataEnabled && (i.splice(o$1, 0, { name: "Spring Data JPA", version: "true" }), o$1++), ee` <style>
        ${A}
      </style>
      <div class="flex flex-col gap-150 items-start">
        <dl>
          ${i.map(
      (s) => ee`
              <div>
                <dt>${s.name}</dt>
                <dd title="${s.version}">
                  <span> ${this.renderValue(s.version)} </span>
                  ${s.more}
                </dd>
              </div>
            `
    )}
          ${this.renderDevWorkflowSection()} ${this.renderDevelopmentWorkflowButton()}
        </dl>
      </div>`;
  }
  renderDevWorkflowSection() {
    const e = Bo(), t = this.getIdePluginLabelText(m$1.idePluginState), i = this.getHotswapAgentLabelText(e);
    return ee`
      <div>
        <dt>Java Hotswap</dt>
        <dd>
          ${f(e === "success", e === "success" ? "Enabled" : "Disabled")} ${i}
        </dd>
      </div>
      ${lu() !== "unsupported" ? ee` <div>
            <dt>IDE Plugin</dt>
            <dd>
              ${f(
      lu() === "success",
      lu() === "success" ? "Installed" : "Not Installed"
    )}
              ${t}
            </dd>
          </div>` : A$1}
    `;
  }
  renderDevelopmentWorkflowButton() {
    const e = ad();
    let t = "", i = null, o$1 = "";
    return e.status === "success" ? (t = "success", i = o.check, o$1 = "IDE Plugin and Java Hotswap are in use.") : e.status === "warning" ? (t = "warning", i = o.lightning, o$1 = "Improve Development Workflow") : e.status === "error" && (t = "error", i = o.alertCircle, o$1 = "Fix Development Workflow"), ee`
      <div>
        <dt>Development Workflow</dt>
        <dd>
          <span class="${t}-text icon" id="development-status-value">${i}</span>
          <vaadin-tooltip for="development-status-value" text="${o$1}"></vaadin-tooltip>
          <button
            id="development-workflow-status-detail"
            class="link-button"
            @click=${() => {
      id();
    }}>
            Show details
          </button>
        </dd>
      </div>
    `;
  }
  getHotswapAgentLabelText(e) {
    return e === "success" ? "Java Hotswap is enabled" : e === "error" ? "Hotswap is partially enabled" : "Hotswap is disabled";
  }
  getIdePluginLabelText(e) {
    if (lu() !== "success")
      return "Not installed";
    if (e?.version) {
      let t = null;
      return e?.ide && (e?.ide === "intellij" ? t = "IntelliJ" : e?.ide === "vscode" ? t = "VS Code" : e?.ide === "eclipse" && (t = "Eclipse")), t ? `${e?.version} ${t}` : e?.version;
    }
    return "Not installed";
  }
  renderValue(e) {
    return e === "false" ? f(false, "False") : e === "true" ? f(true, "True") : e;
  }
  getInfoForClipboard(e) {
    const t = this.renderRoot.querySelectorAll(".items-start dt"), s = Array.from(t).map((n) => ({
      key: n.textContent.trim(),
      value: n.nextElementSibling.textContent.trim()
    })).filter((n) => n.key !== "Live reload").filter((n) => !n.key.startsWith("Vaadin Emplo")).filter((n) => n.key !== "Development Workflow").map((n) => {
      const { key: l2 } = n;
      let { value: r } = n;
      if (l2 === "IDE Plugin")
        r = this.getIdePluginLabelText(m$1.idePluginState) ?? "false";
      else if (l2 === "Java Hotswap") {
        const x = yn.jdkInfo?.jrebel, v = Bo();
        x && v === "success" ? r = "JRebel is in use" : r = this.getHotswapAgentLabelText(v);
      } else l2 === "Vaadin" && r.indexOf(`
`) !== -1 && (r = r.substring(0, r.indexOf(`
`)));
      return `${l2}: ${r}`;
    }).join(`
`);
    return e && Do({
      type: Re.INFORMATION,
      message: "Environment information copied to clipboard",
      dismissId: "versionInfoCopied"
    }), s.trim();
  }
};
m = h([
  Vl("copilot-info-panel")
], m);
let w = class extends lc {
  createRenderRoot() {
    return this;
  }
  connectedCallback() {
    super.connectedCallback(), this.style.display = "flex";
  }
  render() {
    return ee` <button
      @click=${() => {
      y$1.emit("system-info-with-callback", {
        callback: T$1,
        notify: true
      });
    }}
      aria-label="Copy to Clipboard"
      class="icon"
      title="Copy to Clipboard">
      <span>${o.copy}</span>
    </button>`;
  }
};
w = h([
  Vl("copilot-info-actions")
], w);
const T = {
  header: "Info",
  expanded: false,
  panelOrder: 15,
  panel: "right",
  floating: false,
  tag: "copilot-info-panel",
  actionsTag: "copilot-info-actions",
  eager: true
  // Render even when collapsed as error handling depends on this
}, W = {
  init(e) {
    e.addPanel(T);
  }
};
window.Vaadin.copilot.plugins.push(W);
function f(e, t) {
  return e ? ee`<span aria-label=${t} class="icon success-text" title=${t}>${o.check}</span>` : ee`<span aria-label=${t} class="icon error-text" title=${t}>${o.x}</span>`;
}
export {
  w as Actions,
  m as CopilotInfoPanel
};
