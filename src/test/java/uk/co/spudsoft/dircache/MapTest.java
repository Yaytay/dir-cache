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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jtalbut
 */
public class MapTest {

  private static final Logger logger = LoggerFactory.getLogger(MapTest.class);

  private static class SimpleNode implements FileTree.FileTreeNode {
    private final String path;
    private final String name;
    private final FileTree.NodeType type;

    SimpleNode(String path, String name, FileTree.NodeType type) {
      this.path = path;
      this.name = name;
      this.type = type;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public FileTree.NodeType getType() {
      return type;
    }

    public String getPath() {
      return path;
    }
  }

  private static final class SimpleFile extends SimpleNode {
    private final int hour;

    SimpleFile(int hour, String path, String name) {
      super(path, name, FileTree.NodeType.file);
      this.hour = hour;
    }

    public int getHour() {
      return hour;
    }
  }

  private static final class SimpleDirectory extends SimpleNode implements FileTree.FileTreeDir<SimpleNode> {
    private final List<SimpleNode> children;

    SimpleDirectory(List<SimpleNode> children, String path, String name) {
      super(path, name, FileTree.NodeType.dir);
      this.children = children;
    }

    @Override
    public List<SimpleNode> getChildren() {
      return children;
    }

    public int getChildCount() {
      return children.size();
    }
  }

  @Test
  public void testMapping() {

    LocalDateTime ts = LocalDateTime.of(1971, Month.MAY, 6, 10, 10);

    DirCacheTree.Directory instance1 = new DirCacheTree.Directory(
            Path.of("firstDir"),
            ts,
            Arrays.asList(
                    new DirCacheTree.File(Path.of("firstDir", "firstFirstFile"), ts, 1),
                    new DirCacheTree.File(Path.of("firstDir", "firstSecondFile"), ts, 2)
            )
    );

    DirCacheTree.Directory instance2 = new DirCacheTree.Directory(
            Path.of("secondDir"),
            ts,
            Arrays.asList(
                    new DirCacheTree.File(Path.of("secondDir", "secondFirstFile"), ts, 1),
                    new DirCacheTree.File(Path.of("secondDir", "secondSecondFile"), ts, 3)
            )
    );

    DirCacheTree.Directory instance3 = new DirCacheTree.Directory(
            Path.of("thirdDir"),
            ts,
            Arrays.asList(
                    new DirCacheTree.File(Path.of("thirdDir", "thirdFirstFile"), ts, 1),
                    new DirCacheTree.File(Path.of("thirdDir", "thirdSecondFile"), ts, 2)
            )
    );

    DirCacheTree.File instance4 = new DirCacheTree.File(Path.of("fourth"), ts, 1);

    DirCacheTree.Directory instance5 = new DirCacheTree.Directory(
            Path.of("fifthDir"),
            LocalDateTime.of(1971, Month.MAY, 6, 10, 11),
            Arrays.asList(
                    new DirCacheTree.File(Path.of("fifthDir", "fifthFirstFile"), ts, 1),
                    new DirCacheTree.File(Path.of("fifthDir", "fifthSecondFile"), ts, 2)
            )
    );

    DirCacheTree.Directory root = new DirCacheTree.Directory(
            Path.of("."),
            ts,
            Arrays.asList(instance1, instance2, instance3, instance4, instance5)
    );

    SimpleNode mappedRoot = root.<SimpleNode>map(
            (d, children) -> {
              Path p = d.getPath();
              String name = p.getFileName() == null ? p.toString() : p.getFileName().toString();
              return (SimpleNode) new SimpleDirectory(children, p.toString(), name);
            },
            f -> (SimpleNode) new SimpleFile(f.getModified().getHour(), f.getPath().toString(), f.getName())
    );

    SimpleDirectory simpleRoot = assertInstanceOf(SimpleDirectory.class, mappedRoot);

    logger.debug("DirCacheTree: {}", root);
    for (DirCacheTree.Node node : root.getChildren()) {
      logger.debug("DirCacheTree.Node: {}", node);
    }

    logger.debug("SimpleDirTree: {}", simpleRoot);
    for (SimpleNode node : simpleRoot.getChildren()) {
      logger.debug("SimpleDirTree.Node: {}", node);
    }

    List<String> names = root.flatten(DirCacheTree.Node::getName);
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
