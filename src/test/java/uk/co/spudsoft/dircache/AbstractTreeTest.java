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

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author jtalbut
 */
public class AbstractTreeTest {
  
  private static class TestTree extends AbstractTree {
    
    private static class Node extends AbstractTree.AbstractNode<Node> {

      public Node(String name) {
        super(name);
      }

      public Node(String name, List<Node> children) {
        super(name, children);
      }
      
    }
    
  }
  
  @Test
  public void testUselessConstructor() {
    TestTree tree = new TestTree();
    assertNotNull(tree);
  }
  
  @Test
  public void testAssertion() {
    assertThrows(AssertionError.class, () -> {
      new TestTree.Node(null);
    });
    assertThrows(AssertionError.class, () -> {
      new TestTree.Node(null, null);
    });
    assertThrows(AssertionError.class, () -> {
      new TestTree.Node("childless", null);
    });
  }
  
  @Test
  public void testLeaf() {
    
    TestTree.Node file = new TestTree.Node("file");
    assertTrue(file.leaf());
    TestTree.Node dir = new TestTree.Node("file", Arrays.asList());
    assertFalse(dir.leaf());
    
  }
  
}
