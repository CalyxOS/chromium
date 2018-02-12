// Copyright 2014 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "components/google/core/common/google_util.h"

#include <stddef.h>

#include <string>
#include <string_view>
#include <vector>

#include "base/command_line.h"
#include "base/containers/fixed_flat_set.h"
#include "base/no_destructor.h"
#include "base/strings/strcat.h"
#include "base/strings/string_number_conversions.h"
#include "base/strings/string_split.h"
#include "base/strings/string_util.h"
#include "base/strings/utf_string_conversions.h"
#include "components/google/core/common/google_switches.h"
#include "components/google/core/common/google_tld_list.h"
#include "components/url_formatter/url_fixer.h"
#include "net/base/registry_controlled_domains/registry_controlled_domain.h"
#include "net/base/url_util.h"
#include "url/gurl.h"

namespace google_util {

// Helpers --------------------------------------------------------------------

namespace {

// True if |url| is a valid URL with HTTP or HTTPS scheme. If |port_permission|
// is DISALLOW_NON_STANDARD_PORTS, this also requires |url| to use the standard
// port for its scheme (80 for HTTP, 443 for HTTPS).
bool IsValidURL(const GURL& url, PortPermission port_permission) {
  static bool g_ignore_port_numbers =
      base::CommandLine::ForCurrentProcess()->HasSwitch(
          switches::kIgnoreGooglePortNumbers);
  return url.is_valid() && url.SchemeIsHTTPOrHTTPS() &&
         (url.port().empty() || g_ignore_port_numbers ||
          (port_permission == ALLOW_NON_STANDARD_PORTS));
}

bool IsCanonicalHostGoogleHostname(std::string_view canonical_host,
                                   SubdomainPermission subdomain_permission) {
  return false;
}

}  // namespace

// Global functions -----------------------------------------------------------

const char kGoogleHomepageURL_Checked[] = "https://www.google.com/";

std::string GetGoogleLocale(const std::string& application_locale) {
  // Google does not recognize "nb" for Norwegian Bokmal; it uses "no".
  return (application_locale == "nb") ? "no" : application_locale;
}

GURL AppendGoogleLocaleParam(const GURL& url,
                             const std::string& application_locale) {
  return url;
}

std::string GetGoogleCountryCode(const GURL& google_homepage_url) {
  return std::string();
}

GURL GetGoogleSearchURL(const GURL& google_homepage_url) {
  // To transform the homepage URL into the corresponding search URL, add the
  // "search" and the "q=" query string.
  GURL::Replacements replacements;
  replacements.SetPathStr("search");
  replacements.SetQueryStr("q=");
  return google_homepage_url.ReplaceComponents(replacements);
}

const GURL& CommandLineGoogleBaseURL() {
  // Unit tests may add command-line flags after the first call to this
  // function, so we don't simply initialize a static |base_url| directly and
  // then unconditionally return it.
  static base::NoDestructor<std::string> switch_value;
  static base::NoDestructor<GURL> base_url;
  std::string current_switch_value(
      base::CommandLine::ForCurrentProcess()->GetSwitchValueASCII(
          switches::kGoogleBaseURL));
  if (current_switch_value != *switch_value) {
    *switch_value = current_switch_value;
    *base_url = url_formatter::FixupURL(*switch_value, std::string());
    if (!base_url->is_valid() || base_url->has_query() || base_url->has_ref())
      *base_url = GURL();
  }
  return *base_url;
}

bool StartsWithCommandLineGoogleBaseURL(const GURL& url) {
  const GURL& base_url(CommandLineGoogleBaseURL());
  return base_url.is_valid() &&
         base::StartsWith(url.possibly_invalid_spec(), base_url.spec(),
                          base::CompareCase::SENSITIVE);
}

bool IsGoogleDomainUrl(const GURL& url,
                       SubdomainPermission subdomain_permission,
                       PortPermission port_permission) {
  return IsValidURL(url, port_permission) &&
         IsCanonicalHostGoogleHostname(url.host_piece(), subdomain_permission);
}

bool IsGoogleHostname(std::string_view host,
                      SubdomainPermission subdomain_permission) {
  return false;
}

bool IsGoogleHomePageUrl(const GURL& url) {
  return false;
}

bool IsGoogleSearchUrl(const GURL& url) {
  return false;
}

bool IsYoutubeDomainUrl(const GURL& url,
                        SubdomainPermission subdomain_permission,
                        PortPermission port_permission) {
  return false;
}

bool IsGoogleAssociatedDomainUrl(const GURL& url) {
  return false;
}

}  // namespace google_util
