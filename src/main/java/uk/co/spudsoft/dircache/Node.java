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
package uk.co.spudsoft.dircache;

import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 *
 * @author jtalbut
 */
public class Node {
  
  private final Path path;
  private final LocalDateTime modified;
  private final String name;
  
  /**
   * Constructor.
   * 
   * @param path The path represented by this Node.
   * @param modified The modified timestamp.
   */
  protected Node(Path path, LocalDateTime modified) {
    this.path = path;
    this.modified = modified;
    this.name = path.getFileName().toString();
  }

  public Path getPath() {
    return path;
  }
  
  /**
   * Get the modified timestamp.
   * @return the modified timestamp.
   */
  public LocalDateTime getModified() {
    return modified;
  }

  /**
   * Get the name of the directory entry.
   * @return the name of the directory entry.
   */
  public String getName() {
    return name;
  }
  
}
