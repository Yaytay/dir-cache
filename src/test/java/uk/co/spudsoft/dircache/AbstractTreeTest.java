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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author jtalbut
 */
public class AbstractTreeTest {
  
  private static class TestTree extends AbstractTree {
    
    private static class Node extends AbstractTree.AbstractNode {

      public Node(String name) {
        super(name);
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
  }
  
}
