// Copyright 2020 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "chrome/browser/ssl/sct_reporting_service_factory.h"

#include "base/callback_helpers.h"
#include "chrome/browser/browser_process.h"
#include "chrome/browser/profiles/profile.h"
#include "chrome/browser/ssl/sct_reporting_service.h"

// static
SCTReportingServiceFactory* SCTReportingServiceFactory::GetInstance() {
  return base::Singleton<SCTReportingServiceFactory>::get();
}

// static
SCTReportingService* SCTReportingServiceFactory::GetForBrowserContext(
    content::BrowserContext* context) {
  return static_cast<SCTReportingService*>(
      GetInstance()->GetServiceForBrowserContext(context, true));
}

SCTReportingServiceFactory::SCTReportingServiceFactory()
    : ProfileKeyedServiceFactory(
          "sct_reporting::Factory",
          ProfileSelections::BuildForRegularAndIncognito()) {}

SCTReportingServiceFactory::~SCTReportingServiceFactory() = default;

KeyedService* SCTReportingServiceFactory::BuildServiceInstanceFor(
    content::BrowserContext* profile) const {
  return nullptr;
}

// Force this to be created during BrowserContext creation, since we can't
// lazily create it.
bool SCTReportingServiceFactory::ServiceIsCreatedWithBrowserContext() const {
  return true;
}
