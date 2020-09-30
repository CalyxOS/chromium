#ifndef THIRD_PARTY_BLINK_RENDERER_CORE_TIMEZONE_EXTERNAL_TIMEZONE_CONTROLLER_H_
#define THIRD_PARTY_BLINK_RENDERER_CORE_TIMEZONE_EXTERNAL_TIMEZONE_CONTROLLER_H_

#include <memory>

#include "third_party/blink/renderer/core/core_export.h"

namespace blink {

class CORE_EXPORT ExternalTimeZoneController final {
 public:
  class TimeZoneOverride {
    friend ExternalTimeZoneController;
    TimeZoneOverride() = default;

   public:
    ~TimeZoneOverride() { ClearTimeZoneOverride(); }
  };

  static std::unique_ptr<TimeZoneOverride> SetTimeZoneOverride(
      const std::string& timezone_id);

  static bool HasTimeZoneOverride();

 private:
  static void ClearTimeZoneOverride();

  ExternalTimeZoneController();
};

}  // namespace blink

#endif  // THIRD_PARTY_BLINK_RENDERER_CORE_TIMEZONE_EXTERNAL_TIMEZONE_CONTROLLER_H_
