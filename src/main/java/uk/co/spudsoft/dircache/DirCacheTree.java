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

import jakarta.validation.constraints.NotNull;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jtalbut
 */
public class DirCacheTree implements FileTree<DirCacheTree.Node> {

  private static final Logger logger = LoggerFactory.getLogger(DirCacheTree.class);
  
  private DirCacheTree() {
  }
  
  public abstract static class Node implements FileTree.FileTreeNode {
    protected final String name;
    protected final Path path;
    protected final LocalDateTime modified;

    public Node(Path path, LocalDateTime modified) {
      this.name = path.getFileName().toString();
      this.path = path;
      this.modified = modified;
    }

    @Override
    public String getName() {
      return name;
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
        logger.debug("{} changed at {}", this.path, this.modified);
        return false;
      }
      return true;
    }
    
  }
  
  public static class Directory extends Node implements FileTree.FileTreeDir<Node> {
   
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
      this.children = Collections.unmodifiableList(children);
      this.childrenByName = new HashMap<>(children.size() * 2);
      children.forEach(n -> childrenByName.put(n.getName(), n));
    }
    
    /**
     * Get the discriminator to aid in polymorphic deserialization.
     * Always returns NodeType.dir.
     * @return NodeType.dir.
     */
    @Override
    public NodeType getType() {
      return NodeType.dir;
    }
    
    /**
     * Get the children of the Directory.
     * @return the children of the Directory.
     */
    @Override
    @NotNull
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
     * Map this Directory and all its children (recursively) into a different implementation of {@link FileTree}.
     * 
     * Either of the mapping methods may return null, which will not be included in the output structure.
     * This is the recommended approach if empty Directories are to be trimmed from the output.
     * 
     * @param <MN> MappedNode, the subtype of {@link FileTreeNode} used for generic nodes in the mapped tree.
     * @param dirMapper Method for mapping a Directory and it's already mapped children to a mapped Directory.
     * @param fileMapper Method for mapping a File to a mapped File.
     * @return The result of called dirMapper on this Directory with all of its children mapped.
     */
    public <MN extends FileTree.FileTreeNode> MN map(
            BiFunction<Directory, List<MN>, ? extends MN> dirMapper,
            Function<File, ? extends MN> fileMapper
    ) {
      List<MN> mappedChildren = children
              .stream()
              .map(n -> {
                if (n instanceof File) {
                  File f = (File) n;
                  return fileMapper.apply(f);
                } else {
                  Directory d = (Directory) n;
                  return d.map(dirMapper, fileMapper);
                }
              })
              .filter(n -> n != null)
              .collect(Collectors.toList());
      
      return dirMapper.apply(this, mappedChildren);
    }
    
    private <F> void flatten(List<F> mapped, Function<File, F> mapper) {
      children.stream().forEach(node -> {
        if (node instanceof File f) {
          F mappedFile = mapper.apply(f);
          if (mappedFile != null) {
            mapped.add(mappedFile);
          }
        } else if (node instanceof Directory d) {
          d.flatten(mapped, mapper);
        }
      });
    }
    
    /**
     * Map all the Files in this Directory and all its children (recursively) into a List of individually mapped items.
     * 
     * @param <F> The subtype of AbstractTree.AbstractNode used for generic nodes in the mapped tree.
     * @param fileMapper Method for mapping a File to a mapped File.
     * @return The result of called dirMapper on this Directory with all of its children mapped.
    */
    public <F> List<F> flatten(Function<File, F> fileMapper) {
      List<F> mappedChildren = new ArrayList<>();
      Directory.this.flatten(mappedChildren, fileMapper);
      return mappedChildren;
    }
    
    @Override
    public int hashCode() {
      int hash = super.privateMembersHashCode();
      hash = 89 * hash + Objects.hashCode(this.childrenByName);
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
      
      return Objects.equals(this.childrenByName, other.childrenByName);
    }
    

    @Override
    public String toString() {
      return path + " (" + childrenByName.size() + " children @ " + modified + ')';
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
     * Get the discriminator to aid in polymorphic deserialization.
     * Always returns NodeType.file.
     * @return NodeType.file.
     */
    @Override
    public NodeType getType() {
      return NodeType.file;
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
        logger.debug("{} changed size from {} to {}", this.path, other.size, this.size);
        return false;
      }
      return super.privateMembersEqual(other);
    }

    @Override
    public String toString() {
      return path + " (" + size + " bytes @ " + modified + ')';
    }
  }
  
}
