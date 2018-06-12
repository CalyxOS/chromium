// Copyright 2013 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "components/signin/internal/identity_manager/profile_oauth2_token_service_delegate_android.h"

#include "base/android/jni_android.h"
#include "base/android/jni_array.h"
#include "base/android/jni_string.h"
#include "base/bind.h"
#include "base/containers/contains.h"
#include "base/feature_list.h"
#include "base/logging.h"
#include "base/memory/raw_ptr.h"
#include "base/metrics/histogram_functions.h"
#include "base/metrics/histogram_macros.h"
#include "components/signin/public/base/account_consistency_method.h"
#include "components/signin/public/identity_manager/account_info.h"
#include "google_apis/gaia/gaia_auth_util.h"
#include "google_apis/gaia/oauth2_access_token_fetcher.h"
#include "services/network/public/cpp/shared_url_loader_factory.h"

using base::android::AttachCurrentThread;
using base::android::ConvertJavaStringToUTF8;
using base::android::ConvertUTF8ToJavaString;
using base::android::JavaParamRef;
using base::android::JavaRef;
using base::android::ScopedJavaLocalRef;

namespace {

using TokenResponseBuilder = OAuth2AccessTokenConsumer::TokenResponse::Builder;

// Callback from FetchOAuth2TokenWithUsername().
// Arguments:
// - the error, or NONE if the token fetch was successful.
// - the OAuth2 access token.
// - the expiry time of the token (may be null, indicating that the expiry
//   time is unknown.
typedef base::OnceCallback<
    void(const GoogleServiceAuthError&, const std::string&, const base::Time&)>
    FetchOAuth2TokenCallback;

class AndroidAccessTokenFetcher : public OAuth2AccessTokenFetcher {
 public:
  AndroidAccessTokenFetcher(
      ProfileOAuth2TokenServiceDelegateAndroid* oauth2_token_service_delegate,
      OAuth2AccessTokenConsumer* consumer,
      const std::string& account_id);

  AndroidAccessTokenFetcher(const AndroidAccessTokenFetcher&) = delete;
  AndroidAccessTokenFetcher& operator=(const AndroidAccessTokenFetcher&) =
      delete;

  ~AndroidAccessTokenFetcher() override;

  // Overrides from OAuth2AccessTokenFetcher:
  void Start(const std::string& client_id,
             const std::string& client_secret,
             const std::vector<std::string>& scopes) override;
  void CancelRequest() override;

  // Handles an access token response.
  void OnAccessTokenResponse(const GoogleServiceAuthError& error,
                             const std::string& access_token,
                             const base::Time& expiration_time);

 private:
  std::string CombineScopes(const std::vector<std::string>& scopes);

  raw_ptr<ProfileOAuth2TokenServiceDelegateAndroid>
      oauth2_token_service_delegate_;
  std::string account_id_;
  bool request_was_cancelled_;
  base::WeakPtrFactory<AndroidAccessTokenFetcher> weak_factory_;
};

AndroidAccessTokenFetcher::AndroidAccessTokenFetcher(
    ProfileOAuth2TokenServiceDelegateAndroid* oauth2_token_service_delegate,
    OAuth2AccessTokenConsumer* consumer,
    const std::string& account_id)
    : OAuth2AccessTokenFetcher(consumer),
      oauth2_token_service_delegate_(oauth2_token_service_delegate),
      account_id_(account_id),
      request_was_cancelled_(false),
      weak_factory_(this) {}

AndroidAccessTokenFetcher::~AndroidAccessTokenFetcher() {}

void AndroidAccessTokenFetcher::Start(const std::string& client_id,
                                      const std::string& client_secret,
                                      const std::vector<std::string>& scopes) {
  CancelRequest();
}

void AndroidAccessTokenFetcher::CancelRequest() {
  request_was_cancelled_ = true;
}

void AndroidAccessTokenFetcher::OnAccessTokenResponse(
    const GoogleServiceAuthError& error,
    const std::string& access_token,
    const base::Time& expiration_time) {
  if (request_was_cancelled_) {
    // Ignore the callback if the request was cancelled.
    return;
  }
  if (error.state() == GoogleServiceAuthError::NONE) {
    FireOnGetTokenSuccess(TokenResponseBuilder()
                              .WithAccessToken(access_token)
                              .WithExpirationTime(expiration_time)
                              .build());
  } else {
    FireOnGetTokenFailure(error);
  }
}

// static
std::string AndroidAccessTokenFetcher::CombineScopes(
    const std::vector<std::string>& scopes) {
  // The Android AccountManager supports multiple scopes separated by a space:
  // https://code.google.com/p/google-api-java-client/wiki/OAuth2#Android
  std::string scope;
  for (std::vector<std::string>::const_iterator it = scopes.begin();
       it != scopes.end(); ++it) {
    if (!scope.empty())
      scope += " ";
    scope += *it;
  }
  return scope;
}

}  // namespace

ProfileOAuth2TokenServiceDelegateAndroid::
    ProfileOAuth2TokenServiceDelegateAndroid(
        AccountTrackerService* account_tracker_service)
    : ProfileOAuth2TokenServiceDelegate(/*use_backoff=*/false),
      account_tracker_service_(account_tracker_service),
      fire_refresh_token_loaded_(RT_LOAD_NOT_START) {
  DVLOG(1) << "ProfileOAuth2TokenServiceDelegateAndroid::ctor";
  DCHECK(account_tracker_service_);
}

ProfileOAuth2TokenServiceDelegateAndroid::
    ~ProfileOAuth2TokenServiceDelegateAndroid() {}

ScopedJavaLocalRef<jobject>
ProfileOAuth2TokenServiceDelegateAndroid::GetJavaObject() {
  return nullptr;
}

bool ProfileOAuth2TokenServiceDelegateAndroid::RefreshTokenIsAvailable(
    const CoreAccountId& account_id) const {
  return false;
}

std::vector<CoreAccountId>
ProfileOAuth2TokenServiceDelegateAndroid::GetAccounts() const {
  return accounts_;
}

std::vector<CoreAccountId>
ProfileOAuth2TokenServiceDelegateAndroid::GetValidAccounts() {
  std::vector<CoreAccountId> ids;
  for (const CoreAccountId& id : GetAccounts()) {
    if (ValidateAccountId(id))
      ids.emplace_back(id);
  }
  return ids;
}

void ProfileOAuth2TokenServiceDelegateAndroid::SetAccounts(
    const std::vector<CoreAccountId>& accounts) {
  accounts_ = accounts;
}

std::unique_ptr<OAuth2AccessTokenFetcher>
ProfileOAuth2TokenServiceDelegateAndroid::CreateAccessTokenFetcher(
    const CoreAccountId& account_id,
    scoped_refptr<network::SharedURLLoaderFactory> url_factory,
    OAuth2AccessTokenConsumer* consumer) {
  DVLOG(1)
      << "ProfileOAuth2TokenServiceDelegateAndroid::CreateAccessTokenFetcher"
      << " account= " << account_id;
  ValidateAccountId(account_id);
  std::string account_name = MapAccountIdToAccountName(account_id);
  DCHECK(!account_name.empty())
      << "Cannot find account name for account id " << account_id;
  return std::make_unique<AndroidAccessTokenFetcher>(this, consumer,
                                                     account_name);
}

void ProfileOAuth2TokenServiceDelegateAndroid::OnAccessTokenInvalidated(
    const CoreAccountId& account_id,
    const std::string& client_id,
    const OAuth2AccessTokenManager::ScopeSet& scopes,
    const std::string& access_token) {
}

void ProfileOAuth2TokenServiceDelegateAndroid::
    ReloadAllAccountsFromSystemWithPrimaryAccount(
        const absl::optional<CoreAccountId>& primary_account_id) {
}

void ProfileOAuth2TokenServiceDelegateAndroid::UpdateAccountList(
    const absl::optional<CoreAccountId>& signed_in_account_id,
    const std::vector<CoreAccountId>& prev_ids,
    const std::vector<CoreAccountId>& curr_ids) {
  DVLOG(1) << "ProfileOAuth2TokenServiceDelegateAndroid::UpdateAccountList:"
           << " sigined_in_account_id="
           << (signed_in_account_id.has_value()
                   ? signed_in_account_id->ToString()
                   : std::string())
           << " prev_ids=" << prev_ids.size()
           << " curr_ids=" << curr_ids.size();
  // Clear any auth errors so that client can retry to get access tokens.
  ClearAuthError(absl::nullopt);

  std::vector<CoreAccountId> refreshed_ids;
  std::vector<CoreAccountId> revoked_ids;
  bool keep_accounts = UpdateAccountList(
      signed_in_account_id, prev_ids, curr_ids, &refreshed_ids, &revoked_ids);

  ScopedBatchChange batch(this);

  // Save the current accounts in the token service before calling
  // FireRefreshToken* methods.
  SetAccounts(keep_accounts ? curr_ids : std::vector<CoreAccountId>());

  for (const CoreAccountId& refreshed_id : refreshed_ids)
    FireRefreshTokenAvailable(refreshed_id);
  for (const CoreAccountId& revoked_id : revoked_ids)
    FireRefreshTokenRevoked(revoked_id);
  if (fire_refresh_token_loaded_ == RT_WAIT_FOR_VALIDATION) {
    fire_refresh_token_loaded_ = RT_LOADED;
    FireRefreshTokensLoaded();
  } else if (fire_refresh_token_loaded_ == RT_LOAD_NOT_START) {
    fire_refresh_token_loaded_ = RT_HAS_BEEN_VALIDATED;
  }

  // Clear accounts that no longer exist on device from AccountTrackerService.
  std::vector<AccountInfo> accounts_info =
      account_tracker_service_->GetAccounts();
  for (const AccountInfo& info : accounts_info) {
    if (!base::Contains(curr_ids, info.account_id))
      account_tracker_service_->RemoveAccount(info.account_id);
  }
}

bool ProfileOAuth2TokenServiceDelegateAndroid::UpdateAccountList(
    const absl::optional<CoreAccountId>& signed_in_id,
    const std::vector<CoreAccountId>& prev_ids,
    const std::vector<CoreAccountId>& curr_ids,
    std::vector<CoreAccountId>* refreshed_ids,
    std::vector<CoreAccountId>* revoked_ids) {
  bool keep_accounts =
      signed_in_id.has_value() && base::Contains(curr_ids, *signed_in_id);
  if (keep_accounts) {
    // Revoke token for ids that have been removed from the device.
    for (const CoreAccountId& prev_id : prev_ids) {
      if (signed_in_id.has_value() && prev_id == *signed_in_id)
        continue;
      if (!base::Contains(curr_ids, prev_id)) {
        DVLOG(1)
            << "ProfileOAuth2TokenServiceDelegateAndroid::UpdateAccountList:"
            << "revoked=" << prev_id;
        revoked_ids->push_back(prev_id);
      }
    }

    if (signed_in_id.has_value()) {
      // Always fire the primary signed in account first.
      DVLOG(1) << "ProfileOAuth2TokenServiceDelegateAndroid::UpdateAccountList:"
               << "refreshed=" << *signed_in_id;
      refreshed_ids->push_back(*signed_in_id);
    }
    for (const CoreAccountId& curr_id : curr_ids) {
      if (signed_in_id.has_value() && curr_id == *signed_in_id)
        continue;
      DVLOG(1) << "ProfileOAuth2TokenServiceDelegateAndroid::UpdateAccountList:"
               << "refreshed=" << curr_id;
      refreshed_ids->push_back(curr_id);
    }
  } else {
    // Revoke all ids with signed in account first.
    if (signed_in_id.has_value() && base::Contains(prev_ids, *signed_in_id)) {
      DVLOG(1) << "ProfileOAuth2TokenServiceDelegateAndroid::UpdateAccountList:"
               << "revoked=" << *signed_in_id;
      revoked_ids->push_back(*signed_in_id);
    }
    for (const CoreAccountId& prev_id : prev_ids) {
      if (signed_in_id.has_value() && prev_id == *signed_in_id)
        continue;
      DVLOG(1) << "ProfileOAuth2TokenServiceDelegateAndroid::UpdateAccountList:"
               << "revoked=" << prev_id;
      revoked_ids->push_back(prev_id);
    }
  }
  return keep_accounts;
}

void ProfileOAuth2TokenServiceDelegateAndroid::FireRefreshTokensLoaded() {
  DVLOG(1)
      << "ProfileOAuth2TokenServiceDelegateAndroid::FireRefreshTokensLoaded";
  DCHECK_EQ(signin::LoadCredentialsState::LOAD_CREDENTIALS_IN_PROGRESS,
            load_credentials_state());
  set_load_credentials_state(
      signin::LoadCredentialsState::LOAD_CREDENTIALS_FINISHED_WITH_SUCCESS);
  ProfileOAuth2TokenServiceDelegate::FireRefreshTokensLoaded();
}

void ProfileOAuth2TokenServiceDelegateAndroid::RevokeAllCredentials() {
  DVLOG(1) << "ProfileOAuth2TokenServiceDelegateAndroid::RevokeAllCredentials";
  ScopedBatchChange batch(this);
  std::vector<CoreAccountId> accounts_to_revoke = GetAccounts();

  // Clear accounts in the token service before calling
  // |FireRefreshTokenRevoked|.
  SetAccounts(std::vector<CoreAccountId>());

  for (const CoreAccountId& account : accounts_to_revoke)
    FireRefreshTokenRevoked(account);
}

void ProfileOAuth2TokenServiceDelegateAndroid::LoadCredentials(
    const CoreAccountId& primary_account_id,
    bool is_syncing) {
  DCHECK_EQ(signin::LoadCredentialsState::LOAD_CREDENTIALS_NOT_STARTED,
            load_credentials_state());
  set_load_credentials_state(
      signin::LoadCredentialsState::LOAD_CREDENTIALS_IN_PROGRESS);
  if (primary_account_id.empty()) {
    FireRefreshTokensLoaded();
    return;
  }
  if (fire_refresh_token_loaded_ == RT_HAS_BEEN_VALIDATED) {
    fire_refresh_token_loaded_ = RT_LOADED;
    FireRefreshTokensLoaded();
  } else if (fire_refresh_token_loaded_ == RT_LOAD_NOT_START) {
    fire_refresh_token_loaded_ = RT_WAIT_FOR_VALIDATION;
  }
}

std::string ProfileOAuth2TokenServiceDelegateAndroid::MapAccountIdToAccountName(
    const CoreAccountId& account_id) const {
  return account_tracker_service_->GetAccountInfo(account_id).email;
}

CoreAccountId
ProfileOAuth2TokenServiceDelegateAndroid::MapAccountNameToAccountId(
    const std::string& account_name) const {
  CoreAccountId account_id =
      account_tracker_service_->FindAccountInfoByEmail(account_name).account_id;
  DCHECK(!account_id.empty() || account_name.empty())
      << "Can't find account id, account_name=" << account_name;
  return account_id;
}
