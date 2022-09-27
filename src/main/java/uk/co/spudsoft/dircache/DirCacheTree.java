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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author jtalbut
 */
public class DirCacheTree extends AbstractTree {

  private DirCacheTree() {
  }
  
  public static class Node extends AbstractNode<Node> {
    private final Path path;
    private final LocalDateTime modified;

    public Node(Path path, LocalDateTime modified) {
      super(path.getFileName().toString());
      this.path = path;
      this.modified = modified;
    }

    public Node(Path path, LocalDateTime modified, List<Node> children) {
      super(path.getFileName().toString(), children);
      this.path = path;
      this.modified = modified;
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

    protected int privateMembersHashCode() {
      int hash = 5;
      hash = 23 * hash + Objects.hashCode(this.path);
      hash = 23 * hash + Objects.hashCode(this.modified);
      hash = 23 * hash + Objects.hashCode(this.name);
      hash = 23 * hash + Objects.hashCode(this.children);
      return hash;
    }

    protected boolean privateMembersEqual(final Node other) {
      if (!Objects.equals(this.name, other.name)) {
        return false;
      }
      if (!Objects.equals(this.path, other.path)) {
        return false;
      }
      if (!Objects.equals(this.modified, other.modified)) {
        return false;
      }
      return Objects.equals(this.children, other.children);
    }
    
  }
  
  public static class Directory extends Node {
   
    private final Map<String, Node> childrenByName;

    /**
     * Constructor.
     * @param path The path represented by this Node.
     * @param modified The modified timestamp.
     * @param children The children of the Directory - in the order returned by FileWalker (which will be dirs first, then probably sorted by name).
     */
    public Directory(Path path, LocalDateTime modified, List<Node> children) {
      super(path, modified, List.copyOf(children));
      this.childrenByName = new HashMap<>(children.size() * 2);
      this.children.forEach(n -> childrenByName.put(n.getName(), n));
    }

    /**
     * Get the children of the Directory.
     * @return the children of the Directory.
     */
    @Override
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

    /**
     * Map this Directory and all its children (recursively) into a different subclass of {@link AbstractTree}.
     * 
     * @param <O> The type of the target AbstractTree.
     * @param <N> The subtype of AbstractTree.AbstractNode used for generic nodes in the mapped tree.
     * @param <D> The subtype of AbstractTree.AbstractNode used for generic nodes in the mapped tree.
     * @param dirMapper Method for mapping a Directory and it's already mapped children to a mapped Directory.
     * @param fileMapper Method for mapping a File to a mapped File.
     * @return The result of called dirMapper on this Directory with all of its children mapped.
     */
    public <O extends AbstractTree, N extends O.AbstractNode<N>, D extends N> D map(
            BiFunction<Directory, List<N>, D> dirMapper
            , Function<File, N> fileMapper
    ) {
      List<N> mappedChildren = children.stream().map(n -> {
                if (n instanceof File) {
                  File f = (File) n;
                  return fileMapper.apply(f);
                } else {
                  Directory d = (Directory) n;
                  return d.map(dirMapper, fileMapper);
                }
              }).collect(Collectors.toList());
      
      return dirMapper.apply(this, mappedChildren);
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
  
  public static class File extends Node {

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

    @Override
    public int hashCode() {
      int hash = super.privateMembersHashCode();
      hash = 23 * hash + (int) (this.size ^ (this.size >>> 32));
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
      final File other = (File) obj;
      if (this.size != other.size) {
        return false;
      }
      return super.privateMembersEqual(other);
    }

    
  }
  
}
