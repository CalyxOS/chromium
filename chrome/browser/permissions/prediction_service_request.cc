// Copyright 2020 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "chrome/browser/permissions/prediction_service_request.h"

#include <utility>

#include "base/functional/bind.h"
#include "base/functional/callback_helpers.h"
#include "components/permissions/prediction_service/prediction_service.h"

PredictionServiceRequest::PredictionServiceRequest(
    permissions::PredictionService* service,
    const permissions::PredictionRequestFeatures& entity,
    permissions::PredictionServiceBase::LookupResponseCallback callback)
    : callback_(std::move(callback)) {
  // Fail the prediction service request
  base::SequencedTaskRunner::GetCurrentDefault()->PostTask(
      FROM_HERE,
      base::BindOnce(&PredictionServiceRequest::LookupReponseReceived,
                     weak_factory_.GetWeakPtr(), false, false, std::nullopt));
}

PredictionServiceRequest::~PredictionServiceRequest() = default;

void PredictionServiceRequest::LookupReponseReceived(
    bool lookup_succesful,
    bool response_from_cache,
    const std::optional<permissions::GeneratePredictionsResponse>& response) {
  std::move(callback_).Run(lookup_succesful, response_from_cache, response);
}
