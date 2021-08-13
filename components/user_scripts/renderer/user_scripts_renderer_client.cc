#include "user_scripts_renderer_client.h"

#include <memory>
#include <utility>

#include "base/logging.h"
#include "base/lazy_instance.h"
#include "content/public/renderer/render_frame.h"
#include "content/public/renderer/render_thread.h"
#include "content/public/renderer/render_frame_visitor.h"
#include "chrome/renderer/chrome_render_thread_observer.h"

#include "../common/user_scripts_features.h"
#include "user_scripts_dispatcher.h"
#include "extension_frame_helper.h"

namespace user_scripts {

// was ChromeExtensionsRendererClient
UserScriptsRendererClient::UserScriptsRendererClient() {}

UserScriptsRendererClient::~UserScriptsRendererClient() {}

// static
UserScriptsRendererClient* UserScriptsRendererClient::GetInstance() {
  static base::LazyInstance<UserScriptsRendererClient>::Leaky client =
      LAZY_INSTANCE_INITIALIZER;
  return client.Pointer();
}

void UserScriptsRendererClient::RenderThreadStarted() {
  if (base::FeatureList::IsEnabled(features::kEnableLoggingUserScripts))
    LOG(INFO) << "UserScripts: RenderThreadStarted";

  content::RenderThread* thread = content::RenderThread::Get();
  dispatcher_ = std::make_unique<UserScriptsDispatcher>();

  dispatcher_->OnRenderThreadStarted(thread);
  thread->AddObserver(dispatcher_.get());
}

void UserScriptsRendererClient::ConfigurationUpdated() {
  if (base::FeatureList::IsEnabled(features::kEnableLoggingUserScripts))
    LOG(INFO) << "UserScripts: Configuration Updated";

  struct WatchFrame : public content::RenderFrameVisitor {
    bool Visit(content::RenderFrame* frame) override {
      if (frame)
        UserScriptsRendererClient::GetInstance()->RenderFrameCreated(frame, NULL);
      return true;  // Continue visiting.
    }
  };
  WatchFrame visitor = {};
  content::RenderFrame::ForEach(&visitor);
}

void UserScriptsRendererClient::RenderFrameCreated(
    content::RenderFrame* render_frame,
    service_manager::BinderRegistry* registry) {

  auto params = ChromeRenderThreadObserver::GetDynamicParams();
  enabled_ = params.allow_userscript;
  if (!enabled_) return;

  ExtensionFrameHelper* frame_helper = ExtensionFrameHelper::Get(render_frame);
  if (!frame_helper) {
    new user_scripts::ExtensionFrameHelper(render_frame);
    dispatcher_->OnRenderFrameCreated(render_frame);
  }
}

void UserScriptsRendererClient::RunScriptsAtDocumentStart(content::RenderFrame* render_frame) {
  if (!enabled_) return;

  ExtensionFrameHelper* frame_helper = ExtensionFrameHelper::Get(render_frame);
  if (!frame_helper)
    return;  // The frame is invisible to user scripts.

  frame_helper->RunScriptsAtDocumentStart();
  // |frame_helper| and |render_frame| might be dead by now.
}

void UserScriptsRendererClient::RunScriptsAtDocumentEnd(content::RenderFrame* render_frame) {
  if (!enabled_) return;

  ExtensionFrameHelper* frame_helper = ExtensionFrameHelper::Get(render_frame);
  if (!frame_helper)
    return;  // The frame is invisible to user scripts.

  frame_helper->RunScriptsAtDocumentEnd();
  // |frame_helper| and |render_frame| might be dead by now.
}

void UserScriptsRendererClient::RunScriptsAtDocumentIdle(content::RenderFrame* render_frame) {
  if (!enabled_) return;

  ExtensionFrameHelper* frame_helper = ExtensionFrameHelper::Get(render_frame);
  if (!frame_helper)
    return;  // The frame is invisible to user scripts.

  frame_helper->RunScriptsAtDocumentIdle();
  // |frame_helper| and |render_frame| might be dead by now.
}

}
