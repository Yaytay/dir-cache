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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A Node in a DirCache representing a directory on disc.
 * Directory objects may be used as hash keys, but doing so requires the calculation of hash codes and equality for all children.
 * 
 * @author jtalbut
 */
public class Directory extends Node {
  
  private final List<Node> children;
  private final Map<String, Node> childrenByName;

  /**
   * Constructor.
   * @param path The path represented by this Node.
   * @param modified The modified timestamp.
   * @param children The children of the Directory - in the order returned by FileWalker (which will be dirs first, then probably sorted by name).
   */
  public Directory(Path path, LocalDateTime modified, List<Node> children) {
    super(path, modified);
    this.children = List.copyOf(children);
    this.childrenByName = new HashMap<>(children.size() * 2);
    this.children.forEach(n -> childrenByName.put(n.getName(), n));
  }

  /**
   * Get the children of the Directory.
   * @return the children of the Directory.
   */
  public List<Node> getChildren() {
    return children;
  }
  
  /**
   * Get a child by name.
   * @param name The name of the child to get.
   * @return The child Node, or null if the child is not known.
   */
  public Node get(String name) {
    return childrenByName.get(name);
  }
  
  /**
   * Get a child directory by name, returning null if the child is not a directory.
   * @param name The name of the child directory to get.
   * @return The child Directory, or null if the child is not known or is not a directory.
   */
  public Directory getDir(String name) {
    Node child = childrenByName.get(name);
    if (child instanceof Directory) {
      return (Directory) child;
    } else {
      return null;
    }
  }

  @Override
  public int hashCode() {
    int hash = super.privateMembersHashCode();
    hash = 89 * hash + Objects.hashCode(this.children);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Directory other = (Directory) obj;
    if (!super.privateMembersEqual(other)) {
      return false;
    }
    return Objects.equals(this.children, other.children);
  }
  
  
}
