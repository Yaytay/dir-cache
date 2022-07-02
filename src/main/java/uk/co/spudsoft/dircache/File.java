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
public class File extends Node {
  
  private final long size;

  /**
   * Constructor.
   * @param path The path represented by this Node.
   * @param modified The modified timestamp.
   * @param size The size of the file, in bytes.
   */
  public File(Path path, LocalDateTime modified, long size) {
    super(path, modified);
    this.size = size;
  }

  /**
   * Get the size of the file on disc, in bytes.
   * @return the size of the file on disc, in bytes.
   */
  public long getSize() {
    return size;
  }
    
}
