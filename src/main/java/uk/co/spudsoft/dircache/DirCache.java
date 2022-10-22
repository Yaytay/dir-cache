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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.regex.Pattern;
import uk.co.spudsoft.dircache.impl.DirCacheImpl;

/**
 * A self-updating cache of the structure of files in a directory on disc.
 * 
 * @author jtalbut
 */
public interface DirCache extends AutoCloseable {
  
  /**
   * Override of {@link java.lang.AutoCloseable#close()} to specify that is does not throw.
   * 
   * Close the DirCache, freeing up resources and preventing any future use of it.
   * 
   */
  @Override
  void close();
  
  /**
   * Create a dir cache of a given path, monitoring for any changes that occur.
   * 
   * Note that all the Nodes of a DirCache are immutable, whenever a change occurs the cache is rebuilt from the root.
   * As with most caches, the cache must be read more than written to make sense.
   * 
   * @param root The root of the directory cache, which should be a directory.
   * @param stabilizationgLag Time to wait after a file has changed before notifying the caller.
   * Note that the file structure is picked up by the DirCache immediately, it is only the callbacks that are delayed.
   * @param ignore Regular expression of files to ignore.
   * @return a newly created DirCache instance.
   * @throws FileNotFoundException if the root Path cannot be found, or if it is not a directory.
   * @throws IOException if attempts to walk the directory tree fail.
   */
  static DirCache cache(Path root, Duration stabilizationgLag, Pattern ignore) throws FileNotFoundException, IOException {
    return new DirCacheImpl(root, stabilizationgLag, ignore).start();
  }
  
  /**
   * Return the Directory at the root of the tree.
   * This will be the Directory object representing the original root Path.
   * @return the Directory at the root of the tree.
   */
  DirCacheTree.Directory getRoot();
  
  /**
   * Start the DirCache monitoring.
   * This does not usually need to be called as the factory method does it.
   * @return this, so that the call may be fluent.
   */
  DirCache start();
  
  /**
   * Stop the DirCache monitoring.
   * Once stop has been called the DirCache cannot be restarted, create a new one.
   * @return this, so that the call may be fluent.
   */
  DirCache stop();

  /**
   * Set the callback to be called after each (stable) directory change.
   * The callback is optional, clients can simply depend upon the cached structure.
   * The primary use of the callback is expected to be the invalidation of secondary caches (of processed files).
   * 
   * @param callback the callback to be called after each (stable) directory change.
   * @return this, so that the call may be fluent.
   */
  DirCache setCallback(Runnable callback);

  /**
   * Get the timestamp of the last file walk.
   * @return the timestamp of the last file walk.
   */
  LocalDateTime getLastWalkTime();

  /**
   * Perform a synchronous refresh of the view of the filesystem.
   * This should be used sparingly, it's intended for use when a client has made a specific change the filesystem that we know needs to be picked up.
   */
  void refresh();
  
}
