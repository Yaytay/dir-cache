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
package uk.co.spudsoft.dircache.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import static java.nio.file.FileVisitOption.FOLLOW_LINKS;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.spudsoft.dircache.DirCache;
import uk.co.spudsoft.dircache.Directory;
import uk.co.spudsoft.dircache.File;
import uk.co.spudsoft.dircache.Node;

/**
 * Default implementation of the {@link uk.co.spudsoft.dircache.DirCache} interface.
 *
 * @author jtalbut
 */
public class DirCacheImpl implements DirCache {

  private static final Logger logger = LoggerFactory.getLogger(DirCacheImpl.class);

  private final Object readLock = new Object();
  private final Object scanLock = new Object();
  private final WatchService watcher = FileSystems.getDefault().newWatchService();
  private final Map<Path, WatchKey> watches = new HashMap<>();
  private final AtomicBoolean stopped = new AtomicBoolean(false);
  private final Path rootPath;
  private final long stabilizationgLagMillis;
  private final Pattern ignore;
  private final Thread thread;
  private LocalDateTime lastWalkTime;
  private Directory rootNode;
  
  private Runnable callback;  

  /**
   * Constructor.
   * Create a dir cache of a given path, monitoring for any changes that occur.
   * 
   * Note that all the Nodes of a DirCache are immutable, whenever a change occurs the cache is rebuilt from the root.
   * As with most caches, the cache must be read more than written to make sense.
   * 
   * @param root The root of the directory cache, which should be a directory.
   * @param stabilizationgLag Time to wait after a file has changed before notifying the caller.
   * Note that the file structure is picked up by the DirCache immediately, it is only the callbacks that are delayed.
   * @param ignore Regular expression of files to ignore.
   * @throws FileNotFoundException if the root Path cannot be found, or if it is not a directory.
   * @throws IOException if attempts to walk the directory tree fail.
   */
  public DirCacheImpl(Path root, Duration stabilizationgLag, Pattern ignore) throws FileNotFoundException, IOException {
    this.rootPath = root;
    this.stabilizationgLagMillis = stabilizationgLag.toMillis();
    this.ignore = ignore;
    walk();
    thread = new Thread(this::thread, "DirCache: " + root.toString());
  }

  @Override
  public Directory getRoot() {
    return rootNode;
  }

  @Override
  public DirCacheImpl start() {
    thread.start();
    return this;
  }

  @Override
  public DirCacheImpl stop() {
    stopped.set(true);
    try {
      watcher.close();
    } catch (IOException ex) {
      logger.info("Failed to close dir cache file watcher: ", ex);
    }
    return this;
  }

  @Override
  public void close() {
    stop();
  }

  @Override
  public DirCacheImpl setCallback(Runnable callback) {
    this.callback = callback;
    return this;
  }

  @Override
  public LocalDateTime getLastWalkTime() {
    return lastWalkTime;
  }
  
  private void thread() {
    boolean active = false;

    while (!stopped.get()) {
      // wait for key to be signaled
      WatchKey key;
      try {
        if (active) {
          key = watcher.poll(stabilizationgLagMillis, TimeUnit.MILLISECONDS);
        } else {
          key = watcher.take();
          active = true;
        }
      } catch (ClosedWatchServiceException x) {
        stopped.set(true);
        continue ;
      } catch (InterruptedException x) {
        continue ;
      }

      boolean wasDeleteOrTimeout = false;
      boolean wasOnlyDeletes = true;
      if (key == null) {
        wasDeleteOrTimeout = true;
      } else {
        for (WatchEvent<?> event : key.pollEvents()) {
          // Pick up deletes immediately, everything else can wait
          if (event.kind() == ENTRY_DELETE) {
            wasDeleteOrTimeout = true;
          } else {
            wasOnlyDeletes = false;
          }
        }
        if (!key.reset()) {
          watches.remove((Path) key.watchable());
        }
      }
      
      if (wasDeleteOrTimeout) {
        walk();
        if (wasOnlyDeletes) {
          active = false;
          if (callback != null) {
            callback.run();
          }
        }
      }
    }
  }

  private static class PathAndNodeList {

    public final Path path;
    private final LocalDateTime lastModified;
    public final List<Node> nodeList;

    PathAndNodeList(Path path, LocalDateTime lastModified) {
      this.path = path;
      this.lastModified = lastModified;
      this.nodeList = new ArrayList<>();
    }
  }

  private class Visitor implements FileVisitor<Path> {

    private final List<Path> dirsFound = new ArrayList<>();
    private final Stack<PathAndNodeList> dirStack = new Stack<>();
    private Directory root;

    public Directory getRoot() {
      return root;
    }

    public List<Path> getDirsFound() {
      return dirsFound;
    }
    
    private LocalDateTime getLastModified(BasicFileAttributes attrs) {
      return LocalDateTime.ofInstant(attrs.lastModifiedTime().toInstant(), ZoneOffset.UTC);
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
      if (ignore.matcher(dir.getFileName().toString()).matches()) {
        logger.trace("preVisitDirectory({}, {}) - IGNORED", dir, attrs.lastModifiedTime());
        return FileVisitResult.SKIP_SUBTREE;
      } else {
        logger.trace("preVisitDirectory({}, {})", dir, attrs.lastModifiedTime());
        dirStack.add(new PathAndNodeList(dir, getLastModified(attrs)));
        dirsFound.add(dir);
        if (!watches.containsKey(dir)) {
          try {
            WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            watches.put(dir, key);
          } catch (IOException ex) {
            logger.warn("Failed to configure path watch for {}: ", dir, ex);
          }
        }
        return FileVisitResult.CONTINUE;
      }
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      if (!ignore.matcher(file.getFileName().toString()).matches()) {
        logger.trace("visitFile({}, {}) in {}", file, attrs.lastModifiedTime(), dirStack.peek());
        PathAndNodeList parent = dirStack.peek();
        File thisFile = new File(file, getLastModified(attrs), attrs.size());
        parent.nodeList.add(thisFile);
      }
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
      logger.trace("visitFileFailed({}, {})", file, exc);
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
      logger.trace("postVisitDirectory({}, {})", dir, exc);
      PathAndNodeList panl = dirStack.pop();
      assert (dir.equals(panl.path));
      Directory thisDir = new Directory(dir, panl.lastModified, panl.nodeList);
      if (dirStack.isEmpty()) {
        root = thisDir;
      } else {
        PathAndNodeList parent = dirStack.peek();
        parent.nodeList.add(thisDir);
      }

      return FileVisitResult.CONTINUE;
    }
  }

  private void walk() {
    Visitor visitor = new Visitor();
    LocalDateTime walkTime = LocalDateTime.now();
    synchronized (scanLock) {
      try {
        Files.walkFileTree(rootPath, EnumSet.of(FOLLOW_LINKS), Integer.MAX_VALUE, visitor);
      } catch (Throwable ex) {
        logger.warn("Failed to update dir cache of {}: ", rootPath, ex);
        return ;
      }
      
      synchronized (readLock) {
        this.rootNode = visitor.getRoot();
        this.lastWalkTime = walkTime;
      }
      Set<Path> dirsFound = new HashSet<>(visitor.getDirsFound());
      for (Iterator<Entry<Path, WatchKey>> iter = watches.entrySet().iterator(); iter.hasNext();) {
        Entry<Path, WatchKey> watching = iter.next();
        if (!dirsFound.contains(watching.getKey())) {
          logger.trace("Path {} no longer exists and is being removed from watches", watching.getKey());
          iter.remove();
        }
      }
    }
  }

}
