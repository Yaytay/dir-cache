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
import java.util.Objects;

/**
 * A Node in a DirCache representing either a file or a directory on disc.
 * It is reasonable to use Node objects has Hash keys, but be aware that the identify of subclasses may be expensive to compute.
 *
 * @author jtalbut
 */
public abstract class Node {
  
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

  /**
   * Get the {@link java.nio.file.Path} that relates to this Node.
   * @return the {@link java.nio.file.Path} that relates to this Node.
   */
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

  protected int privateMembersHashCode() {
    int hash = 5;
    hash = 23 * hash + Objects.hashCode(this.path);
    hash = 23 * hash + Objects.hashCode(this.modified);
    hash = 23 * hash + Objects.hashCode(this.name);
    return hash;
  }

  protected boolean privateMembersEqual(final Node other) {
    if (!Objects.equals(this.name, other.name)) {
      return false;
    }
    if (!Objects.equals(this.path, other.path)) {
      return false;
    }
    return Objects.equals(this.modified, other.modified);
  }
  
  
}
