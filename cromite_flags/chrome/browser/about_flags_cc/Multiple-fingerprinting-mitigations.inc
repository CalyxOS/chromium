#ifdef FLAG_SECTION

    {"fingerprinting-canvas-image-data-noise",
     "Disable Canvas image data fingerprint deception",
     "Slightly modifies at most 20 pixels in Canvas image data extracted via JS APIs",
     kOsAll, SINGLE_DISABLE_VALUE_TYPE(switches::kFingerprintingCanvasImageDataNoise)},
    {"fingerprinting-client-rects-noise",
     "Disable get*ClientRects() fingerprint deception",
     "Scale the output values of Range::getClientRects() and Element::getBoundingClientRect() with a randomly selected factor in the range -0.0003% to 0.0003%, which are recomputed on every document initialization.",
     kOsAll, SINGLE_DISABLE_VALUE_TYPE(switches::kFingerprintingClientRectsNoise)},
    {"fingerprinting-canvas-measuretext-noise",
     "Disable Canvas::measureText() fingerprint deception",
     "Scale the output values of Canvas::measureText() with a randomly selected factor in the range -0.0003% to 0.0003%, which are recomputed on every document initialization.",
     kOsAll, SINGLE_DISABLE_VALUE_TYPE(switches::kFingerprintingCanvasMeasureTextNoise)},

#endif
