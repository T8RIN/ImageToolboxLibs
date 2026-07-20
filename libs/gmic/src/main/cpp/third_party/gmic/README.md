# G'MIC upstream source

- Version: 4.0.2
- Source: https://gmic.eu/files/source/gmic_4.0.2.tar.gz
- License choice for this module: CeCILL-C v1

The files come from the official source archive. `gmic.cpp` has one Android portability adjustment: thread IDs use `pthread_self()` because NDK does not expose the Linux `SYS_gettid` macro in this context. The upstream `COPYING` file is included alongside them.
