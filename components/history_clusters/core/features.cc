// Copyright 2021 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "components/history_clusters/core/features.h"

#include "base/containers/contains.h"
#include "base/feature_list.h"
#include "base/metrics/field_trial_params.h"
#include "base/strings/string_piece_forward.h"
#include "base/strings/string_split.h"
#include "build/build_config.h"
#include "ui/base/l10n/l10n_util.h"

namespace history_clusters {

namespace {

constexpr auto enabled_by_default_desktop_only =
#if BUILDFLAG(IS_ANDROID) || BUILDFLAG(IS_IOS)
    base::FEATURE_DISABLED_BY_DEFAULT;
#else
    base::FEATURE_ENABLED_BY_DEFAULT;
#endif

}  // namespace

namespace internal {

BASE_FEATURE(kJourneys,
             "Journeys",                               // disabled by default
             enabled_by_default_desktop_only);         // in Brave, too; we are Android

BASE_FEATURE(kJourneysLabels,
             "JourneysLabel",
             base::FEATURE_ENABLED_BY_DEFAULT);

BASE_FEATURE(kJourneysImages,
             "JourneysImages",
             base::FEATURE_ENABLED_BY_DEFAULT);

const base::FeatureParam<bool> kJourneysImagesCover{
    &kJourneysImages, "JourneysImagesCover", true};

BASE_FEATURE(kPersistedClusters,
             "HistoryClustersPersistedClusters",       // disabled by default
             base::FEATURE_DISABLED_BY_DEFAULT);       // in Brave

BASE_FEATURE(kOmniboxAction,
             "JourneysOmniboxAction",                  // disabled by default
             base::FEATURE_DISABLED_BY_DEFAULT);       // in Brave, too

BASE_FEATURE(kOmniboxHistoryClusterProvider,
             "JourneysOmniboxHistoryClusterProvider",  // disabled by default
             enabled_by_default_desktop_only);         // in Brave (too; we are Android)

BASE_FEATURE(kNonUserVisibleDebug,
             "JourneysNonUserVisibleDebug",
             base::FEATURE_DISABLED_BY_DEFAULT);

BASE_FEATURE(kUserVisibleDebug,
             "JourneysUserVisibleDebug",
             base::FEATURE_DISABLED_BY_DEFAULT);

BASE_FEATURE(kPersistContextAnnotationsInHistoryDb,
             "JourneysPersistContextAnnotationsInHistoryDb", // disabled by default
             base::FEATURE_DISABLED_BY_DEFAULT);             // in Brave

BASE_FEATURE(kHistoryClustersInternalsPage,
             "HistoryClustersInternalsPage",                 // disabled by default
             base::FEATURE_DISABLED_BY_DEFAULT);             // in Brave, too

BASE_FEATURE(kHistoryClustersUseContinueOnShutdown,
             "HistoryClustersUseContinueOnShutdown",
             base::FEATURE_ENABLED_BY_DEFAULT);

BASE_FEATURE(kHistoryClustersKeywordFiltering,
             "HistoryClustersKeywordFiltering",
             base::FEATURE_DISABLED_BY_DEFAULT);

BASE_FEATURE(kHistoryClustersVisitDeduping,
             "HistoryClustersVisitDeduping",
             base::FEATURE_DISABLED_BY_DEFAULT);

BASE_FEATURE(kJourneysIncludeSyncedVisits,
             "JourneysIncludeSyncedVisits",
             base::FEATURE_DISABLED_BY_DEFAULT);

BASE_FEATURE(kJourneysPersistCachesToPrefs,
             "JourneysPersistCachesToPrefs",
             base::FEATURE_DISABLED_BY_DEFAULT);

BASE_FEATURE(kHistoryClustersNavigationContextClustering,
             "HistoryClustersNavigationContextClustering", // disabled by default
             base::FEATURE_DISABLED_BY_DEFAULT);           // in Brave

// Killswitch only.
BASE_FEATURE(kJourneysNamedNewTabGroups,
             "JourneysNamedNewTabGroups",              // disabled by default
             base::FEATURE_DISABLED_BY_DEFAULT);       // in Brave

BASE_FEATURE(kJourneysZeroStateFiltering,
             "JourneysZeroStateFiltering",             // disabled by default
             base::FEATURE_DISABLED_BY_DEFAULT);       // in Brave, too

}  // namespace internal

BASE_FEATURE(kSidePanelJourneys,
             "SidePanelJourneys",                      // disabled by default
             base::FEATURE_DISABLED_BY_DEFAULT);       // in Brave
// If enabled, and the main flag is also enabled, the Journeys omnibox
// entrypoints open Journeys in Side Panel rather than the History WebUI.
const base::FeatureParam<bool> kSidePanelJourneysOpensFromOmnibox{
    &kSidePanelJourneys, "SidePanelJourneysOpensFromOmnibox", true};

BASE_FEATURE(kRenameJourneys,
             "RenameJourneys",
             enabled_by_default_desktop_only);

}  // namespace history_clusters
