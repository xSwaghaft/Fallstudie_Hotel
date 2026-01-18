import { injectGlobalWebcomponentCss } from 'Frontend/generated/jar-resources/theme-util.js';

import { injectGlobalCss } from 'Frontend/generated/jar-resources/theme-util.js';

import { css, unsafeCSS, registerStyles } from '@vaadin/vaadin-themable-mixin';
import $cssFromFile_0 from 'Frontend/themes/hotel/styles.css?inline';
import $cssFromFile_1 from 'Frontend/themes/hotel/views/booking-management.css?inline';
import $cssFromFile_2 from 'Frontend/themes/hotel/views/my-bookings.css?inline';
import $cssFromFile_3 from 'Frontend/themes/hotel/views/dashboard.css?inline';
import $cssFromFile_4 from 'Frontend/themes/hotel/views/feedback.css?inline';
import $cssFromFile_5 from 'Frontend/themes/hotel/views/guest-portal.css?inline';
import $cssFromFile_6 from 'Frontend/themes/hotel/views/image-management.css?inline';
import $cssFromFile_7 from 'Frontend/themes/hotel/views/login.css?inline';
import $cssFromFile_8 from 'Frontend/themes/hotel/guest.css?inline';
import $cssFromFile_9 from 'Frontend/themes/hotel/views/my-reviews.css?inline';
import $cssFromFile_10 from 'Frontend/themes/hotel/views/registration.css?inline';
import $cssFromFile_11 from 'Frontend/themes/hotel/views/reports.css?inline';
import $cssFromFile_12 from 'Frontend/themes/hotel/views/reset-password.css?inline';
import $cssFromFile_13 from 'Frontend/themes/hotel/views/card-factory.css?inline';
import $cssFromFile_14 from 'Frontend/themes/hotel/views/room-management.css?inline';
import $cssFromFile_15 from 'Frontend/themes/hotel/views/user-management.css?inline';
import '@vaadin/polymer-legacy-adapter/style-modules.js';
import '@vaadin/vertical-layout/theme/lumo/vaadin-vertical-layout.js';
import '@vaadin/app-layout/theme/lumo/vaadin-app-layout.js';
import '@vaadin/form-layout/theme/lumo/vaadin-form-layout.js';
import '@vaadin/dialog/theme/lumo/vaadin-dialog.js';
import 'Frontend/generated/jar-resources/flow-component-renderer.js';
import '@vaadin/grid/theme/lumo/vaadin-grid.js';
import '@vaadin/grid/theme/lumo/vaadin-grid-column.js';
import '@vaadin/grid/theme/lumo/vaadin-grid-sorter.js';
import '@vaadin/checkbox/theme/lumo/vaadin-checkbox.js';
import 'Frontend/generated/jar-resources/gridConnector.ts';
import '@vaadin/tooltip/theme/lumo/vaadin-tooltip.js';
import '@vaadin/horizontal-layout/theme/lumo/vaadin-horizontal-layout.js';
import '@vaadin/field-highlighter/theme/lumo/vaadin-field-highlighter.js';
import '@vaadin/common-frontend/ConnectionIndicator.js';
import '@vaadin/accordion/theme/lumo/vaadin-accordion.js';
import '@vaadin/details/theme/lumo/vaadin-details.js';
import '@vaadin/accordion/theme/lumo/vaadin-accordion-panel.js';
import '@vaadin/button/theme/lumo/vaadin-button.js';
import '@vaadin/app-layout/theme/lumo/vaadin-drawer-toggle.js';
import '@vaadin/avatar/theme/lumo/vaadin-avatar.js';
import '@vaadin/avatar-group/theme/lumo/vaadin-avatar-group.js';
import '@vaadin/card/theme/lumo/vaadin-card.js';
import '@vaadin/charts/theme/lumo/vaadin-chart.js';
import '@vaadin/checkbox-group/theme/lumo/vaadin-checkbox-group.js';
import '@vaadin/combo-box/theme/lumo/vaadin-combo-box.js';
import 'Frontend/generated/jar-resources/comboBoxConnector.js';
import '@vaadin/multi-select-combo-box/theme/lumo/vaadin-multi-select-combo-box.js';
import '@vaadin/confirm-dialog/theme/lumo/vaadin-confirm-dialog.js';
import '@vaadin/context-menu/theme/lumo/vaadin-context-menu.js';
import 'Frontend/generated/jar-resources/contextMenuConnector.js';
import 'Frontend/generated/jar-resources/contextMenuTargetConnector.js';
import '@vaadin/custom-field/theme/lumo/vaadin-custom-field.js';
import '@vaadin/date-picker/theme/lumo/vaadin-date-picker.js';
import 'Frontend/generated/jar-resources/datepickerConnector.js';
import '@vaadin/date-time-picker/theme/lumo/vaadin-date-time-picker.js';
import '@vaadin/time-picker/theme/lumo/vaadin-time-picker.js';
import 'Frontend/generated/jar-resources/vaadin-time-picker/timepickerConnector.js';
import 'Frontend/generated/jar-resources/dndConnector.js';
import '@vaadin/form-layout/theme/lumo/vaadin-form-item.js';
import '@vaadin/form-layout/theme/lumo/vaadin-form-row.js';
import '@vaadin/grid/theme/lumo/vaadin-grid-column-group.js';
import 'Frontend/generated/jar-resources/vaadin-grid-flow-selection-column.js';
import '@vaadin/icon/theme/lumo/vaadin-icon.js';
import '@vaadin/icons/vaadin-iconset.js';
import '@vaadin/list-box/theme/lumo/vaadin-list-box.js';
import '@vaadin/item/theme/lumo/vaadin-item.js';
import '@vaadin/login/theme/lumo/vaadin-login-form.js';
import '@vaadin/login/theme/lumo/vaadin-login-overlay.js';
import '@vaadin/markdown/theme/lumo/vaadin-markdown.js';
import 'Frontend/generated/jar-resources/menubarConnector.js';
import '@vaadin/menu-bar/theme/lumo/vaadin-menu-bar.js';
import '@vaadin/message-input/theme/lumo/vaadin-message-input.js';
import 'Frontend/generated/jar-resources/messageListConnector.js';
import '@vaadin/message-list/theme/lumo/vaadin-message-list.js';
import '@vaadin/notification/theme/lumo/vaadin-notification.js';
import '@vaadin/scroller/theme/lumo/vaadin-scroller.js';
import '@vaadin/popover/theme/lumo/vaadin-popover.js';
import 'Frontend/generated/jar-resources/vaadin-popover/popover.ts';
import '@vaadin/progress-bar/theme/lumo/vaadin-progress-bar.js';
import '@vaadin/radio-group/theme/lumo/vaadin-radio-button.js';
import '@vaadin/radio-group/theme/lumo/vaadin-radio-group.js';
import 'Frontend/generated/jar-resources/ReactRouterOutletElement.tsx';
import '@vaadin/select/theme/lumo/vaadin-select.js';
import 'Frontend/generated/jar-resources/selectConnector.js';
import 'Frontend/generated/jar-resources/tooltip.ts';
import 'Frontend/generated/jar-resources/disableOnClickFunctions.js';
import '@vaadin/side-nav/theme/lumo/vaadin-side-nav.js';
import '@vaadin/side-nav/theme/lumo/vaadin-side-nav-item.js';
import '@vaadin/split-layout/theme/lumo/vaadin-split-layout.js';
import '@vaadin/tabs/theme/lumo/vaadin-tab.js';
import '@vaadin/tabsheet/theme/lumo/vaadin-tabsheet.js';
import '@vaadin/tabs/theme/lumo/vaadin-tabs.js';
import 'Frontend/generated/jar-resources/vaadin-big-decimal-field.js';
import '@vaadin/text-field/theme/lumo/vaadin-text-field.js';
import '@vaadin/email-field/theme/lumo/vaadin-email-field.js';
import '@vaadin/integer-field/theme/lumo/vaadin-integer-field.js';
import '@vaadin/number-field/theme/lumo/vaadin-number-field.js';
import '@vaadin/password-field/theme/lumo/vaadin-password-field.js';
import '@vaadin/text-area/theme/lumo/vaadin-text-area.js';
import 'Frontend/generated/jar-resources/lit-renderer.ts';
import '@vaadin/grid/theme/lumo/vaadin-grid-tree-toggle.js';
import '@vaadin/upload/theme/lumo/vaadin-upload.js';
import '@vaadin/virtual-list/theme/lumo/vaadin-virtual-list.js';
import 'Frontend/generated/jar-resources/virtualListConnector.js';
import '@vaadin/vaadin-lumo-styles/sizing.js';
import '@vaadin/vaadin-lumo-styles/spacing.js';
import '@vaadin/vaadin-lumo-styles/style.js';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';

injectGlobalCss($cssFromFile_0.toString(), 'CSSImport end', document);
injectGlobalWebcomponentCss($cssFromFile_0.toString());

injectGlobalCss($cssFromFile_1.toString(), 'CSSImport end', document);
injectGlobalWebcomponentCss($cssFromFile_1.toString());

injectGlobalCss($cssFromFile_2.toString(), 'CSSImport end', document);
injectGlobalWebcomponentCss($cssFromFile_2.toString());

injectGlobalCss($cssFromFile_3.toString(), 'CSSImport end', document);
injectGlobalWebcomponentCss($cssFromFile_3.toString());

injectGlobalCss($cssFromFile_4.toString(), 'CSSImport end', document);
injectGlobalWebcomponentCss($cssFromFile_4.toString());

injectGlobalCss($cssFromFile_5.toString(), 'CSSImport end', document);
injectGlobalWebcomponentCss($cssFromFile_5.toString());

injectGlobalCss($cssFromFile_6.toString(), 'CSSImport end', document);
injectGlobalWebcomponentCss($cssFromFile_6.toString());

injectGlobalCss($cssFromFile_7.toString(), 'CSSImport end', document);
injectGlobalWebcomponentCss($cssFromFile_7.toString());

injectGlobalCss($cssFromFile_8.toString(), 'CSSImport end', document);
injectGlobalWebcomponentCss($cssFromFile_8.toString());

injectGlobalCss($cssFromFile_9.toString(), 'CSSImport end', document);
injectGlobalWebcomponentCss($cssFromFile_9.toString());

injectGlobalCss($cssFromFile_10.toString(), 'CSSImport end', document);
injectGlobalWebcomponentCss($cssFromFile_10.toString());

injectGlobalCss($cssFromFile_11.toString(), 'CSSImport end', document);
injectGlobalWebcomponentCss($cssFromFile_11.toString());

injectGlobalCss($cssFromFile_12.toString(), 'CSSImport end', document);
injectGlobalWebcomponentCss($cssFromFile_12.toString());

injectGlobalCss($cssFromFile_13.toString(), 'CSSImport end', document);
injectGlobalWebcomponentCss($cssFromFile_13.toString());

injectGlobalCss($cssFromFile_14.toString(), 'CSSImport end', document);
injectGlobalWebcomponentCss($cssFromFile_14.toString());

injectGlobalCss($cssFromFile_15.toString(), 'CSSImport end', document);
injectGlobalWebcomponentCss($cssFromFile_15.toString());
const loadOnDemand = (key) => { return Promise.resolve(0); }
window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
window.Vaadin.Flow.loadOnDemand = loadOnDemand;
window.Vaadin.Flow.resetFocus = () => {
 let ae=document.activeElement;
 while(ae&&ae.shadowRoot) ae = ae.shadowRoot.activeElement;
 return !ae || ae.blur() || ae.focus() || true;
}