/*
 * Copyright (C) 2023 njt
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
import java.util.List;

/**
 * A generic representation of a hierarchical file tree.
 *
 * <p>
 * This interface does not prescribe how nodes are stored or loaded; instead, it defines the recursive type relationships between:
 *
 * <ul>
 * <li>{@code N} – the concrete node type used by this tree</li>
 * <li>{@code D} – the concrete directory type used by this tree</li>
 * </ul>
 *
 * Implementations bind these type parameters so that directory nodes return children of the correct implementation-specific type.
 *
 * @param <N> the concrete node type for this tree
 */
public interface FileTree<N extends FileTree.FileTreeNode> {

  /**
   * The type of a Node, whether it represents a file or a directory.
   */
  enum NodeType {
    /**
     * The Node is a file.
     */
    file,
    /**
     * The Node is a directory, with children. Note that empty directories should be stripped out, so a directory always has
     * children.
     */
    dir
  }

  /**
   * A node in a {@link FileTree}. Nodes may represent files, directories, or any other tree element defined by the
   * implementation.
   *
   * <p>
   * The type parameter {@code N} ensures that implementations can define their own node subtype while preserving type safety
   * throughout the tree.
   *
   */
  interface FileTreeNode {

    /**
     * Returns the name of this node as it appears in its parent directory.
     *
     * @return the name of the directory entry
     */
    @NotNull
    String getName();

    /**
     * Return the type of the node, discriminator for polymorphic de-serialization.
     * @return the type of the node.
     */
    @NotNull
    NodeType getType();
  }

  /**
   * A directory node within a {@link FileTree}. Directory nodes contain zero or more child nodes of the implementation-specific
   * directory type.
   *
   * <p>
   * The type parameters ensure that:
   *
   * <ul>
   * <li>{@code N} is the node type used by this tree</li>
   * <li>{@code D} is the directory type used by this tree</li>
   * </ul>
   *
   * This allows each {@code FileTree} implementation to define its own directory and node classes while preserving strong typing
   * for {@link #getChildren()}.
   *
   * @param <N> the concrete node type
   */
  interface FileTreeDir<N extends FileTreeNode> extends FileTreeNode {

    /**
     * Returns the child directories of this directory node.
     *
     * <p>
     * The returned list contains only directory nodes of the same implementation-specific type {@code D}. Implementations may
     * choose whether the list is mutable, immutable, or lazily loaded.
     *
     * @return the list of child directory nodes
     */
    List<N> getChildren();
  }
}
