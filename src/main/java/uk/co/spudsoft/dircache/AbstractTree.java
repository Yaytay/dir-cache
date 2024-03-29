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
import java.util.List;

/**
 * A generic implementation of a Tree structure.
 * 
 * The intention is for this class to be used for modelling directories and files,
 * as such it would be best if this class to separated the structures for leaf nodes and internal nodes, 
 * but that would make it impossible for subclasses to provide their own base Node class 
 * (it would require inheritance from both the AbstractNode and the AbstractInternalNode).
 * The result is that AbstractNode is used for both Leaf and Internal nodes and thus it is possible for a 
 * node to have the structure of both a leaf node and an internal node.
 * It is the responsibility of subclasses to enforce the correct distinction - a subclass used for internal nodes should call the constructor
 * that accepts children (with a non-null children) and the a subclass used for leaf nodes should call the constructor that does
 * not accept children.
 * 
 * @author jtalbut
 */
public abstract class AbstractTree implements FileTree {

  /**
   * Every node in the tree is an AbstractNode.
   * @param <N> The derived Node type to be used in the specific implementation.
   */
  public abstract static class AbstractNode<N extends AbstractNode<?>> implements FileTree.FileTreeNode<N> {
    protected final String name;
    protected final List<N> children;

    /**
     * Internal node constructor.
     * 
     * @param name The name of the node.  The name of an internal node may be empty.
     * @param children The child nodes.
     * All nodes have a name and all nodes except the root node should have a non-zero length name.
     * The children should be null for a leaf node, and not-null (though possibly empty) for an internal node.
     */
    public AbstractNode(@NotNull String name, @NotNull List<N> children) {
      assert name != null;
      assert children != null;
      this.name = name;
      this.children = children;
    }

    /**
     * Leaf node constructor.
     * 
     * @param name The name of the node.  The name of a leaf node may not be empty.
     * All nodes have a name and all nodes except the root node should have a non-zero length name.
     */
    public AbstractNode(@NotNull String name) {
      assert name != null;
      assert name.length() > 0;
      this.name = name;
      this.children = null;
    }

    /**
     * Get the name of the directory entry.
     * @return the name of the directory entry.
     */
    @Override
    @NotNull
    public String getName() {
      return name;
    }

    /**
     * Get the children of this internal node.
     * @return the children of this internal node.
     */
    @Override
    public List<N> getChildren() {
      return children;
    }
    
    /**
     * Return true if the node is a leaf node (has no children).
     * 
     * Note that the rather clunky name is to avoid serializers considering this to be a property.
     * 
     * @return true if the node is a leaf node (has no children). 
     */
    @Override
    public boolean leaf() {
      return children == null;
    }
  }

}
