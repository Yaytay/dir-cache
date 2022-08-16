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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.spudsoft.dircache.DirCache;
import uk.co.spudsoft.dircache.File;

/**
 *
 * @author jtalbut
 */
public class DirCacheImplTest {

  private static final Logger logger = LoggerFactory.getLogger(DirCacheImplTest.class);
  private static final ObjectMapper MAPPER = JsonMapper.builder().findAndAddModules().build();

  @Test
  public void testGetRoot() throws IOException {
    DirCache dirCache = DirCache.cache(Path.of("target/test-classes"), Duration.ZERO, Pattern.compile("^uk.*"));
    logger.debug("Result: {}", MAPPER.writeValueAsString(dirCache.getRoot()));
    assertNull(dirCache.getRoot().getDir("a").getDir("aa").getDir("aaa"));
  }

  @Test
  public void testChanges() throws Exception {
    Path root = Path.of("target/test-classes");

    try (DirCache dirCache = DirCache.cache(root, Duration.ZERO, Pattern.compile("^uk.*"))) {
      logger.debug("Result: {}", MAPPER.writeValueAsString(dirCache.getRoot()));
      
      // Dirs before files
      assertEquals("a", dirCache.getRoot().getChildren().get(0).getName());
      assertEquals("c", dirCache.getRoot().getChildren().get(1).getName());
      assertEquals("b", dirCache.getRoot().getChildren().get(2).getName());
      
      LocalDateTime firstWalkTime = dirCache.getLastWalkTime();
      assertNull(dirCache.getRoot().getDir("a").getDir("aa").get("bob"));

      Files.createFile(Path.of("target/test-classes/a/aa/bob"));
      await().atMost(5, SECONDS).until(() -> firstWalkTime.isBefore(dirCache.getLastWalkTime()));
      logger.debug("Result: {}", MAPPER.writeValueAsString(dirCache.getRoot()));
      assertNotNull(dirCache.getRoot().getDir("a").getDir("aa").get("bob"));
      LocalDateTime secondWalkTime = dirCache.getLastWalkTime();

      Files.delete(Path.of("target/test-classes/a/aa/bob"));
      await().atMost(5, SECONDS).until(() -> secondWalkTime.isBefore(dirCache.getLastWalkTime()));
      logger.debug("Result: {}", MAPPER.writeValueAsString(dirCache.getRoot()));
      assertNull(dirCache.getRoot().getDir("a").getDir("aa").get("bob"));
      LocalDateTime thirdWalkTime = dirCache.getLastWalkTime();
    } 
  }

  @Test
  public void testChangesWithCallback() throws Exception {
    Path root = Path.of("target/test-classes");
    AtomicInteger counter = new AtomicInteger();

    DirCache dirCache = DirCache.cache(root, Duration.of(100, ChronoUnit.MILLIS), Pattern.compile("^uk.*"))
            .setCallback(() -> {
              logger.info("It changed");
              counter.incrementAndGet();
            });
    logger.debug("Result: {}", MAPPER.writeValueAsString(dirCache.getRoot()));
    LocalDateTime firstWalkTime = dirCache.getLastWalkTime();
    assertNull(dirCache.getRoot().getDir("a").getDir("aa").get("bob"));
    assertEquals(0, counter.get());

    logger.debug("Creating file");
    Files.createFile(Path.of("target/test-classes/a/aa/bob"));
    await().atMost(5, SECONDS).until(() -> firstWalkTime.isBefore(dirCache.getLastWalkTime()));
    logger.debug("Result: {}", MAPPER.writeValueAsString(dirCache.getRoot()));
    assertNotNull(dirCache.getRoot().getDir("a").getDir("aa").get("bob"));
    LocalDateTime secondWalkTime = dirCache.getLastWalkTime();
    assertEquals(1, counter.get());

    int countOfDeleted = delete(Path.of("target/test-classes/a/aa").toFile());
    logger.debug("Deleted dir ({} deletes)", countOfDeleted);
    await().atMost(5, SECONDS).until(() -> secondWalkTime.isBefore(dirCache.getLastWalkTime()));
    logger.debug("Result: {}", MAPPER.writeValueAsString(dirCache.getRoot()));
    assertNull(dirCache.getRoot().getDir("a").getDir("aa"));
    LocalDateTime thirdWalkTime = dirCache.getLastWalkTime();
    assertThat(counter.get(), greaterThan(1));
    
    dirCache.stop();
    
    Thread.sleep(2000);
  }

  private int delete(java.io.File f) throws IOException {
    int count = 0;
    if (f.isDirectory()) {
      for (java.io.File c : f.listFiles()) {
        count += delete(c);
      }
    }
    if (!f.delete()) {
      throw new FileNotFoundException("Failed to delete file: " + f);
    }
    return count + 1;
  }

  @Test
  public void testComparator() {
    File file1 = new File(Path.of("target/test-classes/a"), LocalDateTime.MIN, 0);
    File file2 = new File(Path.of("target/test-classes/b"), LocalDateTime.MIN, 0);
    assertEquals(0, DirCacheImpl.compareNodes(null, null));
    assertEquals(-1, DirCacheImpl.compareNodes(null, file1));
    assertEquals(1, DirCacheImpl.compareNodes(file1, null));
    assertEquals(-1, DirCacheImpl.compareNodes(file1, file2));
    assertEquals(1, DirCacheImpl.compareNodes(file2, file1));
    assertEquals(0, DirCacheImpl.compareNodes(file1, file1));
  }
  
}
