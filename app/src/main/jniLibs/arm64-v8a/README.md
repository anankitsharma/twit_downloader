Drop-in location for 16 KB–aligned native libraries

Place the following 16 KB–aligned ELF shared libraries here to override the dependency-provided ones:

- libffmpeg.so
- libffprobe.so
- libaria2c.so
- libpython.so

Requirements

- Build with Android NDK r27+ (r28 recommended)
- Linker flags: -Wl,-z,max-page-size=16384 (and optionally -Wl,-z,common-page-size=16384)

Verification

Use the NDK tool to verify PT_LOAD alignment is 16384:

llvm-readobj --program-headers libffmpeg.so | findstr Alignment:

Gradle will package local libs from this folder in preference to dependency libs.

