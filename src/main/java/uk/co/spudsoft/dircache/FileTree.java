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

import java.util.List;

/**
 *
 * @author njt
 */
public interface FileTree {
  
  interface FileTreeNode<N extends FileTreeNode<?>> {

    /**
     * Get the name of the directory entry.
     * @return the name of the directory entry.
     */
    String getName();

    /**
     * Get the children of this internal node.
     * @return the children of this internal node.
     */
    List<N> getChildren();
    
    /**
     * Return true if the node is a leaf node (has no children).
     * 
     * Note that the rather clunky name is to avoid serializers considering this to be a property.
     * 
     * @return true if the node is a leaf node (has no children). 
     */
    boolean leaf();
    
  }
  
}
