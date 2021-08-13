// Copyright 2015 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#ifndef USERSCRIPTS_RENDERER_WEB_UI_INJECTION_HOST_H_
#define USERSCRIPTS_RENDERER_WEB_UI_INJECTION_HOST_H_

#include "injection_host.h"

class WebUIInjectionHost : public InjectionHost {
 public:
  WebUIInjectionHost(const WebUIInjectionHost&) = delete;
  WebUIInjectionHost& operator=(const WebUIInjectionHost&) = delete;
  WebUIInjectionHost(const HostID& host_id);
  ~WebUIInjectionHost() override;

 private:
  // InjectionHost:
  const std::string* GetContentSecurityPolicy() const override;
  const GURL& url() const override;
  const std::string& name() const override;

 private:
  GURL url_;
};

#endif  // USERSCRIPTS_RENDERER_WEB_UI_INJECTION_HOST_H_
