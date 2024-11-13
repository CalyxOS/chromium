// The goal of this file is to make SkPixmap.h treat SkASSERT as an actual assertion.

#ifndef overrides_SkPixmap_DEFINED
#define overrides_SkPixmap_DEFINED

// Include SkPixmap.h's own included header files here and now so that their contents will not be
// included again by the time we include SkPixmap.h itself. This ensures that our alteration of
// SkASSERT *will not affect them*. We only wish for it to affect the assertions made within
// SkPixmap.h. This is important, because some assertions cannot even be performed outside of
// SK_DEBUG due to unavailable variables, leading to confusing "undeclared identifier"
// compilation errors.
#include "include/core/SkColor.h"
#include "include/core/SkColorType.h"
#include "include/core/SkImageInfo.h"
#include "include/core/SkRect.h"
#include "include/core/SkRefCnt.h"
#include "include/core/SkSamplingOptions.h"
#include "include/core/SkSize.h"
#include "include/private/base/SkAPI.h"
#include "include/private/base/SkAssert.h"

#if !defined(SK_DEBUG)
    // Redefine SkASSERT to the release variant so that the checks will actually be performed.
    // (We will undo this later so that solely SkPixmap.h is affected.)
    #undef SkASSERT
    #define SkASSERT(cond) SkASSERT_RELEASE(cond)
#endif  // !defined(SK_DEBUG)

// SkPixmap.h uses unsafe buffers, and the compiler configuration specified in its .gn files
// doesn't apply within overrides/. So just do it here with pragma.
#pragma clang unsafe_buffer_usage begin

// Include the original SkPixmap.h.
#include "src/third_party/skia/include/core/SkPixmap.h"

#pragma clang unsafe_buffer_usage end

#if !defined(SK_DEBUG)
    // Revert the redefinition of SkASSERT.
    #undef SkASSERT
    #define SkASSERT(cond) static_cast<void>(0)
#endif  // !defined(SK_DEBUG)

#endif  // overrides_SkPixmap_DEFINED
