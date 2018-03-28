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

#ifndef CHROME_BROWSER_UI_WEBUI_PROXY_CONFIG_UI_H_
#define CHROME_BROWSER_UI_WEBUI_PROXY_CONFIG_UI_H_

#include "content/public/browser/web_ui_controller.h"

// The WebUI for chrome://proxy/.
class ProxyConfigUI : public content::WebUIController {
 public:
  ProxyConfigUI(const ProxyConfigUI&) = delete;
  ProxyConfigUI& operator=(const ProxyConfigUI&) = delete;
  explicit ProxyConfigUI(content::WebUI* web_ui);
};

#endif  // CHROME_BROWSER_UI_WEBUI_PROXY_CONFIG_UI_H_
