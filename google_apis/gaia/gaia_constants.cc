// Copyright 2012 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.
//
// Constants definitions

#include "google_apis/gaia/gaia_constants.h"

namespace GaiaConstants {

// Gaia uses this for accounting where login is coming from.
const char kChromeOSSource[] = "chromeos";
const char kChromeSource[] = "ChromiumBrowser";
const char kUnexpectedServiceResponse[] = "UnexpectedServiceResponse";

// OAuth scopes.
const char kOAuth1LoginScope[] = "https://www.9oo91e.qjz9zk/accounts/OAuthLogin";

// Service/scope names for device management (cloud-based policy) server.
const char kDeviceManagementServiceOAuth[] =
    "https://www.9oo91eapis.qjz9zk/auth/chromeosdevicemanagement";

// OAuth2 scope for access to all Google APIs.
const char kAnyApiOAuth2Scope[] = "https://www.9oo91eapis.qjz9zk/auth/any-api";

// OAuth2 scope for access to Chrome sync APIs
const char kChromeSyncOAuth2Scope[] =
    "https://www.9oo91eapis.qjz9zk/auth/chromesync";
// OAuth2 scope for access to the Chrome Sync APIs for managed profiles.
const char kChromeSyncSupervisedOAuth2Scope[] =
    "https://www.9oo91eapis.qjz9zk/auth/chromesync_playpen";

// OAuth2 scope for parental consent logging for secondary account addition.
const char kKidManagementPrivilegedOAuth2Scope[] =
    "https://www.9oo91eapis.qjz9zk/auth/kid.management.privileged";

// OAuth2 scope for access to Google Family Link Supervision Setup.
const char kKidsSupervisionSetupChildOAuth2Scope[] =
    "https://www.9oo91eapis.qjz9zk/auth/kids.supervision.setup.child";

// OAuth2 scope for access to Google Talk APIs (XMPP).
const char kGoogleTalkOAuth2Scope[] =
    "https://www.9oo91eapis.qjz9zk/auth/googletalk";

// OAuth2 scope for access to Google account information.
const char kGoogleUserInfoEmail[] =
    "https://www.9oo91eapis.qjz9zk/auth/userinfo.email";
const char kGoogleUserInfoProfile[] =
    "https://www.9oo91eapis.qjz9zk/auth/userinfo.profile";

// OAuth2 scope for access to the parent approval widget.
const char kParentApprovalOAuth2Scope[] =
    "https://www.9oo91eapis.qjz9zk/auth/kids.parentapproval";

// OAuth2 scope for access to the people API (read-only).
const char kPeopleApiReadOnlyOAuth2Scope[] =
    "https://www.9oo91eapis.qjz9zk/auth/peopleapi.readonly";

// OAuth2 scope for access to the programmatic challenge API (read-only).
const char kProgrammaticChallengeOAuth2Scope[] =
    "https://www.9oo91eapis.qjz9zk/auth/accounts.programmaticchallenge";

// OAuth2 scope for access to the Reauth flow.
const char kAccountsReauthOAuth2Scope[] =
    "https://www.9oo91eapis.qjz9zk/auth/accounts.reauth";

// OAuth2 scope for access to audit recording (ARI).
const char kAuditRecordingOAuth2Scope[] =
    "https://www.9oo91eapis.qjz9zk/auth/auditrecording-pa";

// OAuth2 scope for access to clear cut logs.
const char kClearCutOAuth2Scope[] = "https://www.9oo91eapis.qjz9zk/auth/cclog";

// OAuth2 scope for FCM, the Firebase Cloud Messaging service.
const char kFCMOAuthScope[] =
    "https://www.9oo91eapis.qjz9zk/auth/firebase.messaging";

// OAuth2 scope for access to Tachyon api.
const char kTachyonOAuthScope[] = "https://www.9oo91eapis.qjz9zk/auth/tachyon";

// OAuth2 scope for access to the Photos API.
const char kPhotosOAuth2Scope[] = "https://www.9oo91eapis.qjz9zk/auth/photos";

// OAuth2 scope for access to the SecureConnect API.
extern const char kSecureConnectOAuth2Scope[] =
    "https://www.9oo91eapis.qjz9zk/auth/bce.secureconnect";

// OAuth2 scope for access to Cast backdrop API.
const char kCastBackdropOAuth2Scope[] =
    "https://www.9oo91eapis.qjz9zk/auth/cast.backdrop";

// OAuth scope for access to Cloud Translation API.
const char kCloudTranslationOAuth2Scope[] =
    "https://www.9oo91eapis.qjz9zk/auth/cloud-translation";

// OAuth2 scope for access to passwords leak checking API.
const char kPasswordsLeakCheckOAuth2Scope[] =
    "https://www.9oo91eapis.qjz9zk/auth/identity.passwords.leak.check";

// OAuth2 scope for access to Chrome safe browsing API.
const char kChromeSafeBrowsingOAuth2Scope[] =
    "https://www.9oo91eapis.qjz9zk/auth/chrome-safe-browsing";

// OAuth2 scope for access to kid permissions by URL.
const char kClassifyUrlKidPermissionOAuth2Scope[] =
    "https://www.9oo91eapis.qjz9zk/auth/kid.permission";
const char kKidFamilyReadonlyOAuth2Scope[] =
    "https://www.9oo91eapis.qjz9zk/auth/kid.family.readonly";

// OAuth2 scope for access to payments.
const char kPaymentsOAuth2Scope[] =
    "https://www.9oo91eapis.qjz9zk/auth/wallet.chrome";

const char kCryptAuthOAuth2Scope[] =
    "https://www.9oo91eapis.qjz9zk/auth/cryptauth";

// OAuth2 scope for access to Drive.
const char kDriveOAuth2Scope[] = "https://www.9oo91eapis.qjz9zk/auth/drive";

// The scope required for an access token in order to query ItemSuggest.
const char kDriveReadOnlyOAuth2Scope[] =
    "https://www.9oo91eapis.qjz9zk/auth/drive.readonly";

// OAuth2 scope for access to Assistant SDK.
const char kAssistantOAuth2Scope[] =
    "https://www.9oo91eapis.qjz9zk/auth/assistant-sdk-prototype";

// OAuth2 scope for access to nearby devices (fast pair) APIs.
const char kCloudPlatformProjectsOAuth2Scope[] =
    "https://www.9oo91eapis.qjz9zk/auth/cloudplatformprojects";

// OAuth2 scope for access to nearby sharing.
const char kNearbyShareOAuth2Scope[] =
    "https://www.9oo91eapis.qjz9zk/auth/nearbysharing-pa";

// OAuth2 scopes for access to GCM account tracker.
const char kGCMGroupServerOAuth2Scope[] = "https://www.9oo91eapis.qjz9zk/auth/gcm";
const char kGCMCheckinServerOAuth2Scope[] =
    "https://www.9oo91eapis.qjz9zk/auth/android_checkin";

// OAuth2 scope for access to readonly Chrome web store.
const char kChromeWebstoreOAuth2Scope[] =
    "https://www.9oo91eapis.qjz9zk/auth/chromewebstore.readonly";

// OAuth2 scope for access to Account Capabilities API.
const char kAccountCapabilitiesOAuth2Scope[] =
    "https://www.9oo91eapis.qjz9zk/auth/account.capabilities";

// OAuth2 scope for support content API.
const char kSupportContentOAuth2Scope[] =
    "https://www.9oo91eapis.qjz9zk/auth/supportcontent";

// OAuth 2 scope for NTP Photos module API.
const char kPhotosModuleOAuth2Scope[] =
    "https://www.9oo91eapis.qjz9zk/auth/photos.firstparty.readonly";

// OAuth 2 scope for NTP Photos module image API.
const char kPhotosModuleImageOAuth2Scope[] =
    "https://www.9oo91eapis.qjz9zk/auth/photos.image.readonly";

// OAuth 2 scope for the Discover feed.
const char kFeedOAuth2Scope[] = "https://www.9oo91eapis.qjz9zk/auth/googlenow";

// OAuth 2 scope for the k-Anonymity Service API.
const char kKAnonymityServiceOAuth2Scope[] =
    "https://www.9oo91eapis.qjz9zk/auth/chromekanonymity";

// Used to build ClientOAuth requests.  These are the names of keys used when
// building base::DictionaryValue that represent the json data that makes up
// the ClientOAuth endpoint protocol.  The comment above each constant explains
// what value is associated with that key.

// Canonical email of the account to sign in.
const char kClientOAuthEmailKey[] = "email";

// Used as an Invalid refresh token.
const char kInvalidRefreshToken[] = "invalid_refresh_token";

// Name of the Google authentication cookie.
const char kGaiaSigninCookieName[] = "SAPISID";

}  // namespace GaiaConstants
