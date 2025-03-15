/*
 * Copyright (C) 2022 jtalbut
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.spudsoft.dircache.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import static com.jayway.awaitility.Awaitility.await;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.spudsoft.dircache.DirCache;

/**
 *
 * @author jtalbut
 */
public class DirCachePollingTest {

  private static final Logger logger = LoggerFactory.getLogger(DirCachePollingTest.class);
  private static final ObjectMapper MAPPER = JsonMapper.builder().findAndAddModules().build();

  @Test
  public void testPolling() throws Exception {
    Path root = Path.of("target/DirCachePollingTest/testPolling");
    DirCacheImplTest.copyTestFiles(root.resolve("a"));
    
    DirCache dirCache = DirCache.cache(root, null, Pattern.compile("^uk.*"), Duration.of(100, ChronoUnit.MILLIS));
    logger.debug("Result: {}", MAPPER.writeValueAsString(dirCache.getRoot()));
    LocalDateTime firstWalkTime = dirCache.getLastWalkTime();
    assertNull(dirCache.getRoot().getDir("a").getDir("aa").get("fred"));

    logger.debug("Creating file");
    Files.createFile(root.resolve("a/aa/fred"));
    // Have to wait a few poll periods to ensure it got it stable, and also to check that it does callback on every poll
    Thread.sleep(1000);
    await().atMost(5, SECONDS).until(() -> firstWalkTime.isBefore(dirCache.getLastWalkTime()));
    logger.debug("Result: {}", MAPPER.writeValueAsString(dirCache.getRoot()));
    assertNotNull(dirCache.getRoot().getDir("a").getDir("aa").get("fred"));
    LocalDateTime secondWalkTime = dirCache.getLastWalkTime();
    

    int countOfDeleted = delete(root.resolve("a/aa").toFile());
    logger.debug("Deleted dir ({} deletes)", countOfDeleted);
    await().atMost(5, SECONDS).until(() -> secondWalkTime.isBefore(dirCache.getLastWalkTime()));
    logger.debug("Result: {}", MAPPER.writeValueAsString(dirCache.getRoot()));
    assertNull(dirCache.getRoot().getDir("a").getDir("aa"));
    
    dirCache.stop();
    
    Thread.sleep(2000);
  }

  @Test
  public void testPollingWithCallback() throws Exception {
    Path root = Path.of("target/DirCachePollingTest/testPollingWithCallback");
    DirCacheImplTest.copyTestFiles(root.resolve("a"));
    AtomicInteger counter = new AtomicInteger();    
    
    DirCache dirCache = DirCache.cache(root, null, Pattern.compile("^uk.*"), Duration.of(100, ChronoUnit.MILLIS));
    dirCache
            .setCallback(() -> {
              try {
                logger.info("It changed from poll to: {}", MAPPER.writeValueAsString(dirCache.getRoot()));
              } catch(Throwable ex) {
                
              }
              counter.incrementAndGet();
            });
    logger.debug("Result: {}", MAPPER.writeValueAsString(dirCache.getRoot()));
    LocalDateTime firstWalkTime = dirCache.getLastWalkTime();
    assertNull(dirCache.getRoot().getDir("a").getDir("aa").get("fred"));
    assertEquals(0, counter.get());

    logger.debug("Creating file");
    Files.createFile(root.resolve("a/aa/fred"));
    // Have to wait a few poll periods to ensure it got it stable, and also to check that it does callback on every poll
    Thread.sleep(1000);
    await().atMost(5, SECONDS).until(() -> firstWalkTime.isBefore(dirCache.getLastWalkTime()));
    logger.debug("Result: {}", MAPPER.writeValueAsString(dirCache.getRoot()));
    assertNotNull(dirCache.getRoot().getDir("a").getDir("aa").get("fred"));
    LocalDateTime secondWalkTime = dirCache.getLastWalkTime();
    int countAfterCreateingFile = counter.get();
    assertThat(countAfterCreateingFile, greaterThan(0));
    assertThat(countAfterCreateingFile, lessThan(3));
    

    int countOfDeleted = delete(root.resolve("a/aa").toFile());
    logger.debug("Deleted dir ({} deletes)", countOfDeleted);
    await().atMost(5, SECONDS).until(() -> secondWalkTime.isBefore(dirCache.getLastWalkTime()));
    logger.debug("Result: {}", MAPPER.writeValueAsString(dirCache.getRoot()));
    assertNull(dirCache.getRoot().getDir("a").getDir("aa"));
    assertThat(counter.get(), greaterThan(countAfterCreateingFile));
    
    dirCache.stop();
    
    Thread.sleep(2000);
  }
  
  private int delete(File f) throws IOException {
    int count = 0;
    if (f.isDirectory()) {
      for (File c : f.listFiles()) {
        count += delete(c);
      }
    }
    if (!f.delete()) {
      throw new FileNotFoundException("Failed to delete file: " + f);
    }
    return count + 1;
  }
  
}
