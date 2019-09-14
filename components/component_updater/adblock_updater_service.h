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

#ifndef COMPONENTS_ADBLOCK_UPDATER_SERVICE_H
#define COMPONENTS_ADBLOCK_UPDATER_SERVICE_H

#include <stdint.h>

#include <memory>
#include <string>
#include <vector>

#include "base/memory/ref_counted.h"
#include "base/observer_list.h"
#include "base/version.h"
#include "build/build_config.h"
#include "components/component_updater/update_scheduler.h"
#include "url/gurl.h"
#include "components/component_updater/download_filters_task.h"
#include "components/subresource_filter/content/browser/ruleset_service.h"
#include "services/network/public/cpp/shared_url_loader_factory.h"

namespace adblock_updater {

// Called when a non-blocking call in this module completes.
using Callback = base::OnceCallback<void(Error error)>;

class Observer {
   public:
    virtual ~Observer() {}

    // Called by the update service when a state change happens.
    virtual void OnEvent(Event event) = 0;
};

// The AdBlock update service is in charge of downloading and saving the
// AdBlock filters.
//
// All methods are safe to call ONLY from the browser's main thread.
class AdBlockUpdaterService {
 public:
  AdBlockUpdaterService(
    scoped_refptr<network::SharedURLLoaderFactory> shared_url_network_factory,
    std::unique_ptr<component_updater::UpdateScheduler> scheduler,
    subresource_filter::RulesetService* ruleset_service,
    bool enabled,
    std::string filters_url);
  ~AdBlockUpdaterService();

  // Adds an observer for this class. An observer should not be added more
  // than once. The caller retains the ownership of the observer object.
  void AddObserver(Observer* observer);

  // Removes an observer. It is safe for an observer to be removed while
  // the observers are being notified.
  void RemoveObserver(Observer* observer);

  // Will schedule automatic updates, run in background.
  void Start();

  // To be called for an user-triggered update.
  // Will not result in an actual update if the last update was too recently triggered.
  bool OnDemandUpdate(Callback on_finished);

 private:
  void NotifyObservers(Event event);
  void OnDemandScheduledUpdate(component_updater::UpdateScheduler::OnFinishedCallback on_finished);
  bool OnDemandUpdateAsNeeded(bool is_foreground, Callback on_finished);
  void OnUpdateComplete(Callback callback, scoped_refptr<DownloadFiltersTask> task, Error error);

  base::ObserverList<Observer>::Unchecked observer_list_;
  base::ThreadChecker thread_checker_;
  base::TimeTicks last_update_;

  raw_ptr<subresource_filter::RulesetService> ruleset_service_;
  std::string filters_url_;

  scoped_refptr<network::SharedURLLoaderFactory> shared_url_network_factory_;
  std::unique_ptr<component_updater::UpdateScheduler> scheduler_;

  bool enabled_;
  bool is_updating_ = false;
  bool scheduled_ = false;
  std::set<scoped_refptr<DownloadFiltersTask>> tasks_;
};

}  // namespace adblock_updater

#endif  // COMPONENTS_ADBLOCK_UPDATER_SERVICE_H
