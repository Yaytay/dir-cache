# About dir-cache

[![Latest release](https://img.shields.io/github/release/yaytay/dir-cache.svg)](https://github.com/yaytay/dir-cache/latest)
[![License](https://img.shields.io/github/license/yaytay/dir-cache)](https://github.com/yaytay/dir-cache/blob/master/LICENCE.md)
[![Issues](https://img.shields.io/github/issues/yaytay/dir-cache)](https://github.com/yaytay/dir-cache/issues)
[![Build Status](https://github.com/yaytay/dir-cache/actions/workflows/buildtest.yml/badge.svg)](https://github.com/Yaytay/dir-cache/actions/workflows/buildtest.yml)
[![CodeCov](https://codecov.io/gh/Yaytay/dir-cache/branch/main/graph/badge.svg?token=ACHVK20T9Q)](https://codecov.io/gh/Yaytay/dir-cache)

The dir-cache is a minimal jar (the only runtime dependency is slf4j) to provide an in-memory model of a directory tree on disc.
Any changes to the directory tree on disc will be picked up in the in-memory model quickly.

When files are written to on disc this usually results in multiple notifications to a WatchService, the DirCache delays responding to 
notifications until there have been no notifications for a configurable period of time (of the order of a second or two is recommended).
The exception to this is where a notification is just a deletion, which is reflected immediately.

The recommended usage of DirCache is for the caller to cache the results of reading a file and to call the DirCache to check whether 
the file has changed (been deleted or has a newer Modified timestamp).
If the DirCache reports that the file has changed then the caller should attempt to read it and update it's in-memory version of it.

Note that DirCache (and the use of it) is full of inevitable race conditions.
Callers are strongly recommended to stick with their view of a file until DirCache says it has changed - but then they must be prepared
for the file to no longer exist (even if DirCache says it does), for the file to be locked and inaccessible or (worst of all) for the file to be in the process of being written to.
If the file does not exist any more then the situation is simple to handle, the file doesn't exist and DirCache will catch up.
If the file is in the process of being written (or is locked) there isn't anything DirCache can do to help - the recommended approach would be to throw a temporary
error and encourage the user to retry.
 

# How to build dir-cache
dir-cache uses Maven as its build tool.

The Maven version must be 3.6.2 or later and the JDK version must be 11 or later (the jar built will be targetted to JDK 11).

# Usage
The basic usage pattern is:
1. Create the DirCache.
   This is a blocking operation that should not be called on a non-blocking thread.
2. If the DirCache was created using the constructor use the start method to begin monitoring for changes.
3. If the you want to know when the DirCache is updated you can register a callback.


```java
    DirCache dirCache = DirCache.cache(root, Duration.of(100, ChronoUnit.MILLIS), Pattern.compile("^uk.*"))
            .setCallback(() -> {
              logger.info("It changed");
              counter.incrementAndGet();
            });
    logger.debug("Result: {}", MAPPER.writeValueAsString(dirCache.getRoot()));
    LocalDateTime firstWalkTime = dirCache.getLastWalkTime();
    assertNull(dirCache.getRoot().getDir("a").getDir("aa").get("bob"));
    assertEquals(0, counter.get());
```

# Logging
The DirCache uses slf4g for logging and can be quite verbose at the DEBUG or TRACE level, it is recommended that these levels 
only be used when explicitly tracking down issues with files.
