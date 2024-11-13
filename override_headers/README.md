Place files in this directory in the same path as a Chromium code file path to override that file.

The "include" directory is a hack to allow third_party/skia itself to use our modified SkPixmap.h,
since third_party/skia itself references SkPixmap.h by a relative path.
