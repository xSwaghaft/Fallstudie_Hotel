import { l as lc, b as le, m as m$1, L as Lr, y as y$1 } from "./indexhtml-jcdUVBfh.js";
class l extends lc {
  constructor() {
    super(...arguments), this.eventBusRemovers = [], this.messageHandlers = {}, this.handleESC = (e) => {
      const s = le.getPanelByTag(this.tagName);
      !m$1.active && s && !s.individual || e.key === "Escape" && Lr(this);
    };
  }
  createRenderRoot() {
    return this;
  }
  onEventBus(e, s) {
    this.eventBusRemovers.push(y$1.on(e, s));
  }
  connectedCallback() {
    super.connectedCallback(), this.addESCListener();
  }
  disconnectedCallback() {
    super.disconnectedCallback(), this.eventBusRemovers.forEach((e) => e()), this.removeESCListener();
  }
  addESCListener() {
    document.addEventListener("keydown", this.handleESC);
  }
  removeESCListener() {
    document.removeEventListener("keydown", this.handleESC);
  }
  onCommand(e, s) {
    this.messageHandlers[e] = s;
  }
  handleMessage(e) {
    return this.messageHandlers[e.command] ? (this.messageHandlers[e.command].call(this, e), true) : false;
  }
}
export {
  l
};
