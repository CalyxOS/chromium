/*
    This file is part of Bromite.

    Bromite is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Bromite is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Bromite. If not, see <https://www.gnu.org/licenses/>.
*/

import {$} from 'chrome://resources/js/util.js';
import {addSingletonGetter} from 'chrome://resources/js/cr.m.js';

/**
 * Main entry point called once the page has loaded.
 */
function onLoad() {
  ProxyConfigView.getInstance();
}

document.addEventListener('DOMContentLoaded', onLoad);

/**
 * This class handles the presentation of the proxy-config view. Used as a
 * singleton.
 */
var ProxyConfigView = (function() {
  'use strict';

  // --------------------------------------------------------------------------

  var kIdStateDivUninitialized = 'state-pending';
  var kIdStateDivMain = 'state-main';
  var kIdApplyButton = 'apply';
  var kIdResetButton = 'reset';
  var kIdClearButton = 'clear';

  var kIdModeEmpty = 'empty';
  var kIdModeDirect = 'direct';
  var kIdModeAutoDetect = 'auto-detect';
  var kIdModeUsePacURL = 'use-pac-url';

  var kIdModeUseSingleList = 'use-single-list';
  var kIdModeUseListPerScheme = 'use-list-per-scheme';

  var kIdPacURL = 'pac-url';
  var kIdPacMandatory = 'pac-mandatory';
  var kIdBypassRules = 'bypass-rules';
  var kIdReverseBypass = 'reverse-bypass';
  var kIdSingleProxies = 'single-proxies';
  var kIdHttpProxies = 'http-proxies';
  var kIdHttpsProxies = 'https-proxies';
  var kIdFtpProxies = 'ftp-proxies';
  var kIdFallbackProxies = 'fallback-proxies';

  /**
   * @constructor
   */
  function ProxyConfigView() {
    this.currentConfig = null;

    $(kIdResetButton).onclick = this.onReset_.bind(this);
    $(kIdApplyButton).onclick = this.onApply_.bind(this);
    $(kIdClearButton).onclick = this.onClear_.bind(this);

    // Tell ProxyConfigMessageHandler to notify the UI of future state changes
    // from this point on.
    chrome.send('enableNotifyUIWithState');
  }

  addSingletonGetter(ProxyConfigView);
  window.ProxyConfigView = ProxyConfigView;

  ProxyConfigView.prototype = {
    /**
     * Updates the UI to reflect the current state. The state transitions are
     * sent by the browser controller (ProxyConfigMessageHandler):
     *
     *   * PENDING - This is the initial state when proxy configuration is opened
     *         for the first time, or there was an error during initialization.
     *         This state is short-lived and likely not observed; will
     *         immediately transition to AVAILABLE).
     *
     *   * AVAILABLE - The reported proxy configuration is active; this state is entered
     *         on first page load (or right after PENDING if configuration was not
     *         available on page load) and every time some configuration change was applied.
     *         It can transition to either AVAILABLE or UNSET.
     *
     *   * UNSET - Proxy configuration is reported to be currently not set.
     *
     */
    onProxyConfigChanged: function(state) {
      // may happen only on first load; leave the loading page as another update is expected
      // when proxy configuration has finished loading
      if (state.pending) {
        $(kIdStateDivMain).hidden = true;
        $(kIdStateDivUninitialized).hidden = false;
        return;
      }

      if (!state.hasOwnProperty('config')) {
        // configuration has been unset, use an empty one
        this.eraseCurrentConfig_();
      } else {
       // save the configuration as current and reset all controls to it
        this.currentConfig = state.config;
      }

      this.renderConfig_();

      this.toggleButtons_(false);
      $(kIdStateDivUninitialized).hidden = true;
      $(kIdStateDivMain).hidden = false;
    },

    /**
     * Set current configuration to an empty (default) one.
     */
    eraseCurrentConfig_: function() {
        this.currentConfig = {
          "auto_detect": false,
          "pending": false,
          "rules": {
            "bypass_rules": "",
            "reverse_bypass": false,
            "type": "none"
          }
        };
    },

    /**
     * Serialize the user-selected configuration in an object.
     */
    serializeConfig_: function() {
      if ($(kIdModeEmpty).checked) {
        return {
          "auto_detect": false,
          "rules": {
            "type": "none"
          }
        };
      } else if ($(kIdModeDirect).checked) {
        return {
          "auto_detect": false,
          "rules": {
            "type": "direct"
          }
        };
      } else if ($(kIdModeAutoDetect).checked) {
        return {
          "auto_detect": true
        };
      } else if ($(kIdModeUsePacURL).checked) {
        return {
          "auto_detect": false,
          "pac_url": $(kIdPacURL).value.trim(),
          "pac_mandatory": $(kIdPacMandatory).checked,
          "rules": {}
        };
      } else if ($(kIdModeUseListPerScheme).checked || $(kIdModeUseSingleList).checked) {
        var config = {
          "auto_detect": false,
          "rules": {
            "bypass_rules": $(kIdBypassRules).value.trim(),
            "reverse_bypass": $(kIdReverseBypass).checked,
            "type": "list"
          }
        };

        if ($(kIdModeUseListPerScheme).checked) {
          config.rules.type = "list_per_scheme";

          config.rules.proxies_for_http = $(kIdHttpProxies).value.trim();
          config.rules.proxies_for_https = $(kIdHttpsProxies).value.trim();
          config.rules.proxies_for_ftp = $(kIdFtpProxies).value.trim();
          config.rules.fallback_proxies = $(kIdFallbackProxies).value.trim();
        } else {
          config.rules.single_proxies = $(kIdSingleProxies).value.trim();
        }

        return config;
      }

      throw new Error('unexpected mode');
    },

    /**
     * Updates the UI to display the current proxy configuration.
     */
    renderConfig_: function() {
      if (this.currentConfig.auto_detect) {
        $(kIdModeAutoDetect).checked = true;
      } else if (this.currentConfig.hasOwnProperty('pac_url')) {
        $(kIdPacURL).value = this.currentConfig.pac_url;
        $(kIdPacMandatory).checked = this.currentConfig.pac_mandatory;
        $(kIdModeUsePacURL).checked = true;
      } else if (this.currentConfig.rules.type == "none") {
        $(kIdModeEmpty).checked = true;
      } else if (this.currentConfig.rules.type == "direct") {
        $(kIdModeDirect).checked = true;
      } else {
        $(kIdBypassRules).value = this.currentConfig.rules.bypass_rules;
        $(kIdReverseBypass).checked = this.currentConfig.rules.reverse_bypass;

        switch (this.currentConfig.rules.type) {
          case "list":
            $(kIdModeUseSingleList).checked = true;
            $(kIdSingleProxies).value = this.currentConfig.rules.single_proxies;
          break;
          case "list_per_scheme":
            $(kIdModeUseListPerScheme).checked = true;
            $(kIdHttpProxies).value = this.currentConfig.rules.proxies_for_http;
            $(kIdHttpsProxies).value = this.currentConfig.rules.proxies_for_https;
            $(kIdFtpProxies).value = this.currentConfig.rules.proxies_for_ftp;
            $(kIdFallbackProxies).value = this.currentConfig.rules.fallback_proxies;
          break;
        }
      }
    },

    /**
     * Apply the configuration currently displayed.
     */
    onApply_: function() {
      var config = this.serializeConfig_();

      // disable buttons; will be enabled back when UI receives a state update
      this.toggleButtons_(true);
      chrome.send('apply', [config]);
    },

    /**
     * Apply the configuration currently displayed.
     */
    onClear_: function() {
      // disable buttons; will be enabled back when UI receives a state update
      this.toggleButtons_(true);
      this.eraseCurrentConfig_();
      chrome.send('clear', []);
    },

    /**
     * Toggle the disabled status of the action buttons.
     */
    toggleButtons_: function(disabled) {
      $(kIdApplyButton).disabled = disabled;
      $(kIdResetButton).disabled = disabled;
      $(kIdClearButton).disabled = disabled;
    },

    /**
     * Reset currently displayed configuration to the last known configuration in use.
     */
    onReset_: function() {
      this.renderConfig_();
    }
  };

  return ProxyConfigView;
})();
