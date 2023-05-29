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
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jtalbut
 */
public class MapTest {
  
  private static final Logger logger = LoggerFactory.getLogger(MapTest.class);
 
  private static class SimpleDirTree extends AbstractTree {
    
    public static class SimpleNode extends AbstractNode<SimpleNode> {
      private final String path;

      public SimpleNode(String path, String name) {
        super(name);
        this.path = path;
      }

      public SimpleNode(String path, String name, List<SimpleNode> children) {
        super(name, children);
        this.path = path;
      }
      
      public String getPath() {
        return path;
      }
            
    }
    
    public static class SimpleFile extends SimpleNode {
      private final int hour;

      public SimpleFile(int hour, String path, String name) {
        super(path, name);
        this.hour = hour;
      }

      public int getHour() {
        return hour;
      }
      
    }
    
    public static class SimpleDirectory extends SimpleNode {

      public SimpleDirectory(List<SimpleNode> children, String path, String name) {
        super(path, name, children);
      }
      
      public int getChildCount() {
        return children.size();
      }
      
    }    
    
  };

  
  @Test
  public void testMapping() {
    
    LocalDateTime ts = LocalDateTime.of(1971, Month.MAY, 06, 10, 10);
    DirCacheTree.Directory instance1 = new DirCacheTree.Directory(Path.of("firstDir"), ts, Arrays.asList(new DirCacheTree.File(Path.of("firstDir", "firstFirstFile"), ts, 1), new DirCacheTree.File(Path.of("firstDir", "firstSecondFile"), ts, 2)));
    DirCacheTree.Directory instance2 = new DirCacheTree.Directory(Path.of("secondDir"), ts, Arrays.asList(new DirCacheTree.File(Path.of("secondDir", "secondFirstFile"), ts, 1), new DirCacheTree.File(Path.of("secondDir", "secondSecondFile"), ts, 3)));
    DirCacheTree.Directory instance3 = new DirCacheTree.Directory(Path.of("thirdDir"), ts, Arrays.asList(new DirCacheTree.File(Path.of("thirdDir", "thirdFirstFile"), ts, 1), new DirCacheTree.File(Path.of("thirdDir", "thirdSecondFile"), ts, 2)));
    DirCacheTree.File instance4 = new DirCacheTree.File(Path.of("fourth"), ts, 1);
    DirCacheTree.Directory instance5 = new DirCacheTree.Directory(Path.of("fifthDir"), LocalDateTime.of(1971, Month.MAY, 06, 10, 11), Arrays.asList(new DirCacheTree.File(Path.of("fifthDir", "fifthFirstFile"), ts, 1), new DirCacheTree.File(Path.of("fifthDir", "fifthSecondFile"), ts, 2)));

    DirCacheTree.Directory root = new DirCacheTree.Directory(Path.of("."), ts, Arrays.asList(instance1, instance2, instance3, instance4, instance5));
    
    SimpleDirTree.SimpleDirectory simpleRoot = root.<SimpleDirTree, SimpleDirTree.SimpleNode, SimpleDirTree.SimpleDirectory>map(
            (d, l) -> {
              Path p = d.getPath();
              return new SimpleDirTree.SimpleDirectory(l, p.toString(), p.getFileName().toString());
            }
            , f -> {
              return new SimpleDirTree.SimpleFile(f.getModified().getHour(), f.getPath().toString(), f.getName());
            }
    );
    
    logger.debug("DirCacheTree: {}", root);
    for (DirCacheTree.Node node : root.children) {
      logger.debug("DirCacheTree.Node: {}", node);
    }
    logger.debug("SimpleDirTree: {}", simpleRoot);
    for (SimpleDirTree.SimpleNode node : simpleRoot.children) {
      logger.debug("SimpleDirTree.Node: {}", node);
    }
    
    List<String> names = root.flatten(f -> f.name);
    logger.debug("Names: {}", names);
    assertEquals(9, names.size());
    assertEquals("firstFirstFile", names.get(0));
    assertEquals("firstSecondFile", names.get(1));
    assertEquals("secondFirstFile", names.get(2));
    assertEquals("secondSecondFile", names.get(3));
    assertEquals("thirdFirstFile", names.get(4));
    assertEquals("thirdSecondFile", names.get(5));
    assertEquals("fourth", names.get(6));
    assertEquals("fifthFirstFile", names.get(7));
    assertEquals("fifthSecondFile", names.get(8));
    
  }
  
  
}
