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
#include "components/component_updater/download_filters_task.h"

#include <utility>

#include "base/files/file_util.h"
#include "base/location.h"
#include "base/logging.h"
#include "net/base/load_flags.h"
#include "url/gurl.h"
#include "services/network/public/cpp/resource_request.h"
#include "services/network/public/mojom/url_response_head.mojom.h"

namespace adblock_updater {

// maximum 10MB for the filters file
const int kMaxBodySize = 1024 * 1024 * 50;

const int kMaxRetriesOnNetworkChange = 3;

const net::NetworkTrafficAnnotationTag traffic_annotation =
    net::DefineNetworkTrafficAnnotation("filters_update", R"(
        semantics {
          sender: "Bromite AdBlock filters updater"
          description:
            "The AdBlock filters updater is responsible for updating the subresource filters."
          trigger: "Manual or automatic AdBlock filters updates."
          data:
            "Subresource filters rulesets, binary format"
          destination: WEBSITE
          internal {
            contacts {
              email: "uazo@users.noreply.github.com"
            }
            contacts {
              email: "uazo@users.noreply.github.com"
            }
          }
          user_data {
            type: NONE
          }
          last_reviewed: "2023-01-01"
        }
        policy {
          cookies_allowed: NO
          setting:
            "You enable or disable this feature via 'Adblock Enable' pref."
          policy_exception_justification: "Not implemented."
        })");

DownloadFiltersTask::DownloadFiltersTask(scoped_refptr<network::SharedURLLoaderFactory> shared_url_network_factory,
                       bool is_foreground, const std::string& filters_url, base::Time min_last_modified,
                       Callback callback)
    : shared_url_network_factory_(shared_url_network_factory),
      is_foreground_(is_foreground),
      complete_callback_(std::move(callback)) {
  DCHECK(!filters_url.empty());
  filters_url_ = GURL(filters_url);
  min_last_modified_ = min_last_modified;

  if (filters_url.empty()) {
    return;
  }

  createSimpleURLLoader(!min_last_modified_.is_null());
}

void DownloadFiltersTask::createSimpleURLLoader(bool headers_only) {
  // always reset response-related fields
  response_code_ = -1;
  final_url_ = GURL();
  download_start_time_ = base::TimeTicks();

  auto resource_request = std::make_unique<network::ResourceRequest>();
  resource_request->url = filters_url_;
  resource_request->credentials_mode = network::mojom::CredentialsMode::kOmit;
  resource_request->load_flags = net::LOAD_BYPASS_CACHE | net::LOAD_DISABLE_CACHE | net::LOAD_DO_NOT_SAVE_COOKIES;
  resource_request->credentials_mode = network::mojom::CredentialsMode::kOmit;
  if (headers_only)
    // will chain two requests - first one is to check last modified header alone
    resource_request->method = "HEAD";
  else
    resource_request->method = "GET";

  simple_url_loader_ = network::SimpleURLLoader::Create(
      std::move(resource_request), traffic_annotation);
  simple_url_loader_->SetRetryOptions(
      kMaxRetriesOnNetworkChange,
      network::SimpleURLLoader::RetryMode::RETRY_ON_NETWORK_CHANGE);
  simple_url_loader_->SetAllowPartialResults(false);
  simple_url_loader_->SetOnResponseStartedCallback(base::BindOnce(
      &DownloadFiltersTask::OnResponseStarted, base::Unretained(this)));
}

DownloadFiltersTask::~DownloadFiltersTask() {
  DCHECK(thread_checker_.CalledOnValidThread());
}

void DownloadFiltersTask::Run() {
  DCHECK(thread_checker_.CalledOnValidThread());

  // will not be initialized if the URL was empty
  if (!simple_url_loader_) {
    TaskComplete(Error::INVALID_ARGUMENT);
    return;
  }

  download_start_time_ = base::TimeTicks::Now();
  if (min_last_modified_.is_null()) {
    internalDownload();
  } else {
    simple_url_loader_->DownloadHeadersOnly(
      shared_url_network_factory_.get(),
      base::BindOnce(&DownloadFiltersTask::OnHeadersDownloadComplete, base::Unretained(this))
    );
  }
}

void DownloadFiltersTask::internalDownload() {
  simple_url_loader_->DownloadToTempFile(
      shared_url_network_factory_.get(),
      base::BindOnce(&DownloadFiltersTask::OnDownloadComplete, base::Unretained(this)),
      kMaxBodySize);
}

void DownloadFiltersTask::OnHeadersDownloadComplete(scoped_refptr<net::HttpResponseHeaders> headers) {
  // something went wrong
  if (headers == nullptr) {
    OnDownloadComplete(base::FilePath());
    return;
  }

  // ignoring 'headers' as 'Last-Modified' has already been picked up by OnResponseStarted
  const base::TimeDelta dt =
          last_modified_ - min_last_modified_;

  if (dt.InSeconds() > 0) {
    // prepare for next simple URL loader and trigger download
    createSimpleURLLoader(false);
    internalDownload();
    return;
  }

  // the remote filters are not more recent than known ones
  TaskComplete(Error::UPDATE_NOT_NEEDED);
}

void DownloadFiltersTask::OnResponseStarted(
    const GURL& final_url,
    const network::mojom::URLResponseHead& response_head) {

  final_url_ = final_url;
  response_code_ = response_head.headers ? response_head.headers->response_code() : -1;

  if (!response_head.headers->GetLastModifiedValue(&last_modified_))
    LOG(WARNING) << "DownloadFiltersTask: fetching URL '" << final_url.spec() << "' with method " << (min_last_modified_.is_null() ? "GET" : "HEAD") << " (no Last-Modified header)";
  else
    LOG(INFO) << "DownloadFiltersTask: fetching URL '" << final_url.spec() << "' with method " << (min_last_modified_.is_null() ? "GET" : "HEAD");
}

void DownloadFiltersTask::OnDownloadComplete(base::FilePath file_path) {
  DCHECK_CALLED_ON_VALID_THREAD(thread_checker_);
  int net_error = simple_url_loader_->NetError();
  int64_t content_size = simple_url_loader_->GetContentSize();

  const base::TimeTicks download_end_time(base::TimeTicks::Now());
  const base::TimeDelta download_time =
      download_end_time >= download_start_time_
          ? download_end_time - download_start_time_
          : base::TimeDelta();

  // Consider a 5xx response from the server as an indication to terminate
  // the request and avoid overloading the server in this case.
  // is not accepting requests for the moment.
  int error = -1;
  if (!file_path.empty() && response_code_ == 200) {
    DCHECK_EQ(0, net_error);
    error = 0;
  } else if (response_code_ != -1) {
    error = response_code_;
  } else {
    error = net_error;
  }

  LOG(INFO) << "DownloadFiltersTask: downloaded " << content_size << " bytes in "
          << download_time.InMilliseconds() << "ms from '" << final_url_.spec()
          << "' to '" << file_path << "' with net_error " << net_error << " and error " << error;

  if (error) {
    TaskComplete(Error::DOWNLOAD_ERROR);
    return;
  }

  file_path_ = file_path;
  TaskComplete(Error::NONE);
}

void DownloadFiltersTask::Cancel() {
  DCHECK(thread_checker_.CalledOnValidThread());

  LOG(INFO) << "DownloadFiltersTask: update cancelled";

  // deletion of the simple_url_loader_ will cause cancellation of its active request, if any

  TaskComplete(Error::UPDATE_CANCELED);
}

void DownloadFiltersTask::TaskComplete(Error error) {
  DCHECK(thread_checker_.CalledOnValidThread());

  base::SequencedTaskRunner::GetCurrentDefault()->PostTask(
      FROM_HERE, base::BindOnce(std::move(complete_callback_),
                                scoped_refptr<DownloadFiltersTask>(this), error));
}

base::Time DownloadFiltersTask::last_modified() {
  return last_modified_;
}

base::FilePath DownloadFiltersTask::file_path() {
  return file_path_;
}

}  // namespace adblock_updater
