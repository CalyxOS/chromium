# Userscripts support for Bromite

UserScript support is under user setting currently disabled by default: when disabled, no code that can impact navigation safety is active.

Activation allows the use of userscripts in Bromite. It is possible to add them in two ways:
- by selecting files from the file picker in the settings
- downloading the scripts and opening it from downloads (only if ends with .user.js)
The new imported scripts are disabled by default: they can be activated via the menu visible on the ui.

Userscript support is currently the one provided by the desktop version. The enabled headers are:

- `@name`
- `@version`
- `@description`
- `@url` or `@homepage`
- `@include`, `@exclude`, `@match`, `@exclude_match` for the url pattern (only http e https)
- `@run-at`
    - `document-start`
        Start the script after the documentElement is created, but before anything else happens
	- `document-end`
        Start the script after the entire document is parsed. Same as DOMContentLoaded
	- `document-idle`
        Start the script sometime after DOMContentLoaded, as soon as the document is "idle". Currently this uses the simple heuristic of: min(DOM_CONTENT_LOADED + TIMEOUT, ONLOAD), but no particular injection point is guaranteed

The url-patterns are so defined:
```
// <url-pattern> := <scheme>://<host><port><path> | '<all_urls>'
// <scheme> := '*' | 'http' | 'https'
// <host> := '*' | <IPv4 address> | [<IPv6 address>] |
//           '*.' <anychar except '/' and '*'>+
// <port> := [':' ('*' | <port number between 0 and 65535>)]
// <path> := '/' <any chars>
//
// * Host is not used when the scheme is 'file'.
// * The path can have embedded '*' characters which act as glob wildcards.
// * '<all_urls>' is a special pattern that matches any valid URL that contains
//   a valid scheme (as specified by valid_schemes_).
// * The '*' scheme pattern excludes file URLs.
//
// Examples of valid patterns:
// - http://*/*
// - http://*/foo*
// - https://*.google.com/foo*bar
// - file://monkey*
// - http://127.0.0.1/*
// - http://[2607:f8b0:4005:805::200e]/*
//
// Examples of invalid patterns:
// - http://* -- path not specified
// - http://*foo/bar -- * not allowed as substring of host component
// - http://foo.*.bar/baz -- * must be first component
// - http:/bar -- scheme separator not found
// - foo://* -- invalid scheme
// - chrome:// -- we don't support chrome internal URLs
```

---
## **Beware of the scripts you enter: they can be a source of security problems, you are injecting code into your navigation**.
---
## Technical aspects

`user_scripts/common` and `user_scripts/renderer` is the closest to the current chromium code: few changes there, mostly eliminated the superfluous extension related code.

In `user_scripts/browser` you find the actual management (in the browser process) and in `android` basically the settings ui.

At startup it tries to read all files in the `userscripts folder` in `/data/user/0/org.bromite.bromite/app_chrome/userscripts`: this could be a critical process because a crash would prevent the browser from opening, and that's why there is a crash counter that automatically disables the feature after three attempts if it encounters a problem during startup.

The java ui allows userscript management: addition, deletion and activation/deactivation. Any errors while reading the scripts are presented to the user: scripts with errors cannot be activated. There is also the visualization of the script source and the open of its homepage (if foreseen) in incognito browsing.

There is also support for an on-line help at https://github.com/bromite/bromite/wiki/UserScripts.


Entry points are `components/user_scripts/browser/userscripts_browser_client.cc` and `components/user_scripts/renderer/user_scripts_renderer_client.cc`: the two attach to the browser and the renderer process.

for userscripts_browser_client.cc
- `chrome/browser/profiles/chrome_browser_main_extra_parts_profiles.cc`
builds the browser side. also gpu process passes here, but the call is avoided.

- `chrome/browser/profiles/profile_manager.cc`
set the profile

for renderer/user_scripts_renderer_client.cc
- `chrome/renderer/chrome_content_renderer_client.cc`
at the renderer side

the two sides have a life of their own and can communicate only via ipc, the renderer does not have access to the disk while the browser does only via its own task runner file (`components/user_scripts/browser/file_task_runner.cc`).

## BROWSER PROCESS
Once the profile is set, it istance the `components/user_scripts/browser/user_script_loader.cc` and starts it.
This loads all the files in the folder into the runner file and interprets them. Control then passes to
`components/user_scripts/browser/user_script_prefs.cc` which verifies through the default profile what the user wants active.
At that point it passes through IPC to the renderer only the list of active scripts.

## RENDERER PROCESS
Each time a frame is created, the script pattern is checked and it is injected into the three stages (START, IDLE, END).
The logic is all in `components/user_scripts/renderer/user_script_set.cc`.

## Simple example
Here you find a working example that eliminates the google popup, useful in always incognito:
```
// ==UserScript==
// @name         Remove Google Consent
// @namespace    google
// @version      0.0.1
// @description  Autohide Accepts Cookies
// @author       uazo
// @match        https://*.google.com/search?*
// @grant        none
// @run-at       document-start
// ==/UserScript==

(function() {
    'use strict';

    var prepareStyleSheet = function() {
        var style = document.createElement('style');
        //style.setAttribute('media', 'screen');
        style.appendChild(document.createTextNode(''));
        document.head.appendChild(style);
        style.sheet.insertRule('body { overflow:scroll !important;position:unset !important }');
    };

	var hideConsent = function() {
		document.getElementById("lb").style.display = "none";
	};

    var checkElementThenRun = function(selector, func) {
        var el = document.querySelector(selector);
        if ( el == null ) {
            if (window.requestAnimationFrame != undefined) {
                window.requestAnimationFrame(function(){ checkElementThenRun(selector, func)});
            } else {
                document.addEventListener('readystatechange', function(e) {
                    if (document.readyState == 'complete') {
                        func();
                    }
                });
            }
        } else {
            func();
        }
    }

    document.cookie = 'CONSENT=YES+IT.it+V13+BX;domain=.google.com';
    checkElementThenRun('head', prepareStyleSheet);
    checkElementThenRun('#lb', hideConsent);
})();
```

See also: https://github.com/bromite/bromite/pull/857
