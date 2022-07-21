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
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author njt
 */
public class FileTest {
  
  /**
   * Test of getSize method, of class File.
   */
  @Test
  public void testGetSize() {
    File instance = new File(Path.of("first"), LocalDateTime.of(1971, Month.MAY, 06, 10, 10), 27);
    assertEquals(27, instance.getSize());
  }

  /**
   * Test of hashCode method, of class File.
   */
  @Test
  public void testHashCode() {
    File instance1 = new File(Path.of("first"), LocalDateTime.of(1971, Month.MAY, 06, 10, 10), 27);
    File instance2 = new File(Path.of("first"), LocalDateTime.of(1971, Month.MAY, 06, 10, 10), 28);
    File instance3 = new File(Path.of("first"), LocalDateTime.of(1971, Month.MAY, 06, 10, 10), 27);
    
    assertEquals(instance1.hashCode(), instance3.hashCode());
    assertNotEquals(instance1.hashCode(), instance2.hashCode());
  }

  /**
   * Test of equals method, of class File.
   */
  @Test
  public void testEquals() {
    File instance1 = new File(Path.of("first"), LocalDateTime.of(1971, Month.MAY, 06, 10, 10), 27);
    File instance2 = new File(Path.of("first"), LocalDateTime.of(1971, Month.MAY, 06, 10, 10), 28);
    File instance3 = new File(Path.of("first"), LocalDateTime.of(1971, Month.MAY, 06, 10, 10), 27);
    File instance4 = new File(Path.of("second"), LocalDateTime.of(1971, Month.MAY, 06, 10, 10), 27);
    File instance5 = new File(Path.of("first"), LocalDateTime.of(1971, Month.MAY, 06, 10, 11), 27);
    
    assertEquals(instance1, instance1);
    assertEquals(instance1, instance3);
    assertNotEquals(instance1, instance2);
    assertNotEquals(instance1, null);
    assertNotEquals(instance1, "bob");
    assertNotEquals(instance1, instance4);
    assertNotEquals(instance1, instance5);
  }
  
}
