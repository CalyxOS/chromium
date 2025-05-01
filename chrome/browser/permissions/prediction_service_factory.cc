// Copyright 2020 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "chrome/browser/permissions/prediction_service_factory.h"

#include "base/no_destructor.h"
#include "chrome/browser/browser_process.h"
#include "chrome/browser/profiles/profile.h"
#include "components/permissions/prediction_service/prediction_service.h"
#include "services/network/public/cpp/cross_thread_pending_shared_url_loader_factory.h"

// static
permissions::PredictionService* PredictionServiceFactory::GetForProfile(
    Profile* profile) {
  return nullptr;
}

// static
PredictionServiceFactory* PredictionServiceFactory::GetInstance() {
  static base::NoDestructor<PredictionServiceFactory> instance;
  return instance.get();
}

PredictionServiceFactory::PredictionServiceFactory()
    : ProfileKeyedServiceFactory(
          "PredictionService",
          ProfileSelections::BuildNoProfilesSelected()) {}

PredictionServiceFactory::~PredictionServiceFactory() = default;

std::unique_ptr<KeyedService>
PredictionServiceFactory::BuildServiceInstanceForBrowserContext(
    content::BrowserContext* context) const {
  return nullptr;
}
