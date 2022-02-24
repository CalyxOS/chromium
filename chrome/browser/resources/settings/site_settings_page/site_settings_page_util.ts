// Copyright 2022 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import {assertNotReached} from 'chrome://resources/js/assert.js';

import {ContentSettingsTypes} from '../site_settings/constants.js';

import {loadTimeData} from 'chrome://resources/js/load_time_data.js';
import {Route, Router, SettingsRoutes} from '../router.js';

function createPath_(content: string) : Node {
  let path = document.createElementNS("http://www.w3.org/2000/svg", "path");
  path.setAttribute("d", content);
  path.setAttribute("fill", "#5F6368");
  return path;
}

export function setupContentSettingsRoutes(r: Partial<SettingsRoutes>) {
  let iconset = document.createElement("iron-iconset-svg");
  iconset.setAttribute("name", "br-settings");
  iconset.setAttribute("size", "24");

  const svg_ns = "http://www.w3.org/2000/svg";
  let svg = document.createElementNS(svg_ns, "svg");
  iconset.appendChild(svg);

  let defs = document.createElementNS(svg_ns, "defs");
  svg.appendChild(defs);

  let routes: any = r;
  for (let index=0; index < loadTimeData.getInteger("br_cs_count"); index++) {
    // create the enum
    let obj = JSON.parse(loadTimeData.getString("br_cs_" + index));
    let name = obj["name"];
    let tag_name = obj["tag_ui"]; if (!tag_name) tag_name = name;
    routes["SITE_SETTINGS_" + name.toUpperCase()] = r.SITE_SETTINGS!.createChild(tag_name);

    // add the icons (on)
    let g_on = document.createElementNS(svg_ns, "g");
    g_on.setAttribute("id", `${name}`);
    g_on.appendChild(createPath_("M8 16h8v2H8v-2zm0-4h8v2H8v-2zm6-10H6c-1.1 0-2 .9-2 2v16c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm4 18H6V4h7v5h5v11z"));
    defs.appendChild(g_on);

    // add the icons (off)
    let g_off = document.createElementNS(svg_ns, "g");
    g_off.setAttribute("id", `${name}-off`);
    g_off.appendChild(createPath_("M13.002 4.001H7.106L5.252 2.148c.232-.094.485-.147.75-.147h8l6 6v8.896l-2-2V9.001h-5v-5z"));
    g_off.appendChild(createPath_("M16.002 12.001h-.896l.896.896v-.896zM.6 3.45l1.414-1.414 19.94 19.94-1.414 1.414L.6 3.45zM3.986 20.01V6.84l2 2V20.01h11.172l1.765 1.766c-.28.15-.599.234-.937.234H5.976c-1.1 0-1.99-.9-1.99-2z"));
    g_off.appendChild(createPath_("M9.158 12.01H7.986v2h3.172l-2-2zM13.158 16.01H7.986v2h7.172l-2-2z"));
    defs.appendChild(g_off);
  }

  document.head.appendChild(iconset);
}

/**
 * Determine localization string for i18n for a given content settings type.
 * Sorted alphabetically by |ContentSettingsType|.
 */
export function getLocalizationStringForContentType(
    contentSettingsType: ContentSettingsTypes): string|null {
  switch (contentSettingsType) {
    case ContentSettingsTypes.ADS:
      return 'siteSettingsAdsMidSentence';
    case ContentSettingsTypes.AR:
      return 'siteSettingsArMidSentence';
    case ContentSettingsTypes.AUTO_PICTURE_IN_PICTURE:
      return 'siteSettingsAutoPictureInPictureMidSentence';
    case ContentSettingsTypes.AUTOMATIC_DOWNLOADS:
      return 'siteSettingsAutomaticDownloadsMidSentence';
    case ContentSettingsTypes.AUTOMATIC_FULLSCREEN:
      return 'siteSettingsAutomaticFullscreenMidSentence';
    case ContentSettingsTypes.BACKGROUND_SYNC:
      return 'siteSettingsBackgroundSyncMidSentence';
    case ContentSettingsTypes.BLUETOOTH_DEVICES:
      return 'siteSettingsBluetoothDevicesMidSentence';
    case ContentSettingsTypes.BLUETOOTH_SCANNING:
      return 'siteSettingsBluetoothScanningMidSentence';
    case ContentSettingsTypes.CAMERA:
      return 'siteSettingsCameraMidSentence';
    case ContentSettingsTypes.CLIPBOARD:
      return 'siteSettingsClipboardMidSentence';
    case ContentSettingsTypes.COOKIES:
      return 'siteSettingsCookiesMidSentence';
    case ContentSettingsTypes.FEDERATED_IDENTITY_API:
      return 'siteSettingsFederatedIdentityApiMidSentence';
    case ContentSettingsTypes.FILE_SYSTEM_WRITE:
      return 'siteSettingsFileSystemWriteMidSentence';
    case ContentSettingsTypes.GEOLOCATION:
      return 'siteSettingsLocationMidSentence';
    case ContentSettingsTypes.HID_DEVICES:
      return 'siteSettingsHidDevicesMidSentence';
    case ContentSettingsTypes.IDLE_DETECTION:
      return 'siteSettingsIdleDetectionMidSentence';
    case ContentSettingsTypes.IMAGES:
      return 'siteSettingsImagesMidSentence';
    case ContentSettingsTypes.JAVASCRIPT:
      return 'siteSettingsJavascriptMidSentence';
    case ContentSettingsTypes.LOCAL_FONTS:
      return 'siteSettingsFontAccessMidSentence';
    case ContentSettingsTypes.MIC:
      return 'siteSettingsMicMidSentence';
    case ContentSettingsTypes.MIDI_DEVICES:
      return 'siteSettingsMidiDevicesMidSentence';
    case ContentSettingsTypes.MIXEDSCRIPT:
      return 'siteSettingsInsecureContentMidSentence';
    case ContentSettingsTypes.NOTIFICATIONS:
      return 'siteSettingsNotificationsMidSentence';
    case ContentSettingsTypes.PAYMENT_HANDLER:
      return 'siteSettingsPaymentHandlerMidSentence';
    case ContentSettingsTypes.POPUPS:
      return 'siteSettingsPopupsMidSentence';
    case ContentSettingsTypes.PROTECTED_CONTENT:
      return 'siteSettingsProtectedContentMidSentence';
    case ContentSettingsTypes.PROTOCOL_HANDLERS:
      return 'siteSettingsHandlersMidSentence';
    case ContentSettingsTypes.SENSORS:
      return 'siteSettingsSensorsMidSentence';
    case ContentSettingsTypes.SERIAL_PORTS:
      return 'siteSettingsSerialPortsMidSentence';
    case ContentSettingsTypes.SOUND:
      return 'siteSettingsSoundMidSentence';
    case ContentSettingsTypes.STORAGE_ACCESS:
      return 'siteSettingsStorageAccessMidSentence';
    case ContentSettingsTypes.USB_DEVICES:
      return 'siteSettingsUsbDevicesMidSentence';
    case ContentSettingsTypes.WEB_PRINTING:
      return 'siteSettingsWebPrintingMidSentence';
    case ContentSettingsTypes.VR:
      return 'siteSettingsVrMidSentence';
    case ContentSettingsTypes.WINDOW_MANAGEMENT:
      return 'siteSettingsWindowManagementMidSentence';
    case ContentSettingsTypes.ZOOM_LEVELS:
      return 'siteSettingsZoomLevelsMidSentence';
    // The following members do not have a mid-sentence localization.
    case ContentSettingsTypes.ANTI_ABUSE:
    case ContentSettingsTypes.JAVASCRIPT_JIT:
    case ContentSettingsTypes.PDF_DOCUMENTS:
    case ContentSettingsTypes.PERFORMANCE:
    case ContentSettingsTypes.PRIVATE_NETWORK_DEVICES:
    case ContentSettingsTypes.SITE_DATA:
      return null;
    default:
      for (let index=0; index < loadTimeData.getInteger("br_cs_count"); index++) {
        let obj = JSON.parse(loadTimeData.getString("br_cs_" + index));
        let name = obj["name"];
        if (name == contentSettingsType) {
          return `brSiteSettings${name}MidSentence`;
        }
      }
      assertNotReached();
  }
}
