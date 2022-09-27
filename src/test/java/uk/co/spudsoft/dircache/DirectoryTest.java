/*
 * Copyright (C) 2022 njt
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
package uk.co.spudsoft.dircache;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author njt
 */
public class DirectoryTest {

  /**
   * Test of getChildren method, of class Directory.
   */
  @Test
  public void testGetChildren() {
    DirCacheTree.Directory instance = new DirCacheTree.Directory(Path.of("first"), LocalDateTime.of(1971, Month.MAY, 06, 10, 10), Arrays.asList());
    List<DirCacheTree.Node> expResult = Arrays.asList();
    assertEquals(Arrays.asList(), instance.getChildren());
  }

  /**
   * Test of get method, of class Directory.
   */
  @Test
  public void testGet() {
    LocalDateTime ts = LocalDateTime.of(1971, Month.MAY, 06, 10, 10);
    DirCacheTree.Directory instance = new DirCacheTree.Directory(Path.of("first"), ts, Arrays.asList(new DirCacheTree.File(Path.of("first", "second"), ts, 1), new DirCacheTree.File(Path.of("first", "third"), ts, 2)));
    assertEquals(instance.getChildren().get(0), instance.get("second"));
    assertEquals(instance.getChildren().get(1), instance.get("third"));
  }

  /**
   * Test of getDir method, of class Directory.
   */
  @Test
  public void testGetDir() {
    LocalDateTime ts = LocalDateTime.of(1971, Month.MAY, 06, 10, 10);
    DirCacheTree.Directory instance = new DirCacheTree.Directory(Path.of("first"), ts, Arrays.asList(new DirCacheTree.File(Path.of("first", "second"), ts, 1), new DirCacheTree.File(Path.of("first", "third"), ts, 2)));
    assertNull(instance.getDir("second"));
  }

  /**
   * Test of hashCode method, of class Directory.
   */
  @Test
  public void testHashCode() {
    LocalDateTime ts = LocalDateTime.of(1971, Month.MAY, 06, 10, 10);
    DirCacheTree.Directory instance1 = new DirCacheTree.Directory(Path.of("first"), ts, Arrays.asList(new DirCacheTree.File(Path.of("first", "second"), ts, 1), new DirCacheTree.File(Path.of("first", "third"), ts, 2)));
    DirCacheTree.Directory instance2 = new DirCacheTree.Directory(Path.of("first"), ts, Arrays.asList(new DirCacheTree.File(Path.of("first", "second"), ts, 1), new DirCacheTree.File(Path.of("first", "third"), ts, 3)));
    DirCacheTree.Directory instance3 = new DirCacheTree.Directory(Path.of("first"), ts, Arrays.asList(new DirCacheTree.File(Path.of("first", "second"), ts, 1), new DirCacheTree.File(Path.of("first", "third"), ts, 2)));

    assertEquals(instance1.hashCode(), instance3.hashCode());
    assertNotEquals(instance1.hashCode(), instance2.hashCode());
  }

  /**
   * Test of equals method, of class Directory.
   */
  @Test
  public void testEquals() {
    LocalDateTime ts = LocalDateTime.of(1971, Month.MAY, 06, 10, 10);
    DirCacheTree.Directory instance1 = new DirCacheTree.Directory(Path.of("first"), ts, Arrays.asList(new DirCacheTree.File(Path.of("first", "second"), ts, 1), new DirCacheTree.File(Path.of("first", "third"), ts, 2)));
    DirCacheTree.Directory instance2 = new DirCacheTree.Directory(Path.of("first"), ts, Arrays.asList(new DirCacheTree.File(Path.of("first", "second"), ts, 1), new DirCacheTree.File(Path.of("first", "third"), ts, 3)));
    DirCacheTree.Directory instance3 = new DirCacheTree.Directory(Path.of("first"), ts, Arrays.asList(new DirCacheTree.File(Path.of("first", "second"), ts, 1), new DirCacheTree.File(Path.of("first", "third"), ts, 2)));
    DirCacheTree.Directory instance4 = new DirCacheTree.Directory(Path.of("bob"),   ts, Arrays.asList(new DirCacheTree.File(Path.of("first", "second"), ts, 1), new DirCacheTree.File(Path.of("first", "third"), ts, 2)));
    DirCacheTree.Directory instance5 = new DirCacheTree.Directory(Path.of("first"), LocalDateTime.of(1971, Month.MAY, 06, 10, 11), Arrays.asList(new DirCacheTree.File(Path.of("first", "second"), ts, 1), new DirCacheTree.File(Path.of("first", "third"), ts, 2)));
    
    assertEquals(instance1, instance1);
    assertEquals(instance1, instance3);
    assertNotEquals(instance1, instance2);
    assertNotEquals(instance1, null);
    assertNotEquals(instance1, "bob");
    assertNotEquals(instance1, instance4);
    assertNotEquals(instance1, instance5);
  }
  
}
