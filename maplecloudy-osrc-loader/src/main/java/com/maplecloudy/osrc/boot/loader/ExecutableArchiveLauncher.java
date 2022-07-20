/*
 * Copyright 2012-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.maplecloudy.osrc.boot.loader;

import com.maplecloudy.osrc.boot.loader.archive.Archive;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.jar.Manifest;

/**
 * Base class for executable archive {@link Launcher}s.
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @author Madhura Bhave
 * @since 1.0.0
 */
public abstract class ExecutableArchiveLauncher extends Launcher {
  private static final String SERVICE_CLASS_ATTRIBUTE = "Service-Class";
  private static final String TASK_CLASS_ATTRIBUTE = "Task-Class";
  private static final String START_CLASS_ATTRIBUTE = "Start-Class";

  protected static final String BOOT_CLASSPATH_INDEX_ATTRIBUTE = "Spring-Boot-Classpath-Index";

  private final Archive archive;

  private final ClassPathIndexFile classPathIndex;

  public ExecutableArchiveLauncher() {
    try {
      this.archive = createArchive();
      this.classPathIndex = getClassPathIndex(this.archive);
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }

  protected ExecutableArchiveLauncher(Archive archive) {
    try {
      this.archive = archive;
      this.classPathIndex = getClassPathIndex(this.archive);
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }

  protected ClassPathIndexFile getClassPathIndex(Archive archive)
      throws IOException {
    return null;
  }

  @Override
  protected String getMainClass(String[] args) throws Exception {
    Manifest manifest = this.archive.getManifest();
    String mainClass = null;
    if (manifest == null) {
      System.out.println(
          "No 'Service-Class' and 'Task-class' manifest entry specified "
              + "in " + this);
      System.exit(-1);
    }
    String serviceClass = manifest.getMainAttributes()
        .getValue(SERVICE_CLASS_ATTRIBUTE);
    String taskClass = manifest.getMainAttributes()
        .getValue(TASK_CLASS_ATTRIBUTE);

    List<String> entryList = new ArrayList<>();
    if (serviceClass != null) {
      entryList.add(serviceClass);
    }
    if (taskClass != null) {
      entryList.addAll(Arrays.asList(taskClass.split(",")));
    }

    if (entryList.size() == 0) {
      System.out.println(
          "No 'Service-Class' and 'Task-class' manifest entry " + "specified "
              + "in " + this);
      System.exit(-1);
    }
    if (args.length > 0) {
      String osrcMain = null;
      boolean flag = false;
      for (String arg : args) {
        if (arg.startsWith("--osrc.main=")) {
          int index = arg.lastIndexOf("=");
          osrcMain = arg.substring(index + 1).trim();
          flag = true;
          break;
        }
      }
      if (flag) {
        if ((osrcMain == null || "".equals(osrcMain))) {
          StringBuffer buffer = new StringBuffer();
          buffer.append("Error:\n");
          buffer.append("  the osrc.main can not be null!");
          System.out.println(buffer.toString());
          System.exit(-1);
        }
        if (entryList.contains(osrcMain)) {
          mainClass = osrcMain;
          String[] strings = doChinFilters(args, osrcMain);
          args = strings;
        } else {
          StringBuffer buffer = new StringBuffer();
          buffer.append("Error:\n");
          buffer.append("  the osrc.main " + osrcMain + " is not exists! \n");
          if (serviceClass != null) {
            buffer.append("  the available service is " + serviceClass + ".\n");
          }
          if (taskClass != null) {
            buffer.append("  the available task is " + String
                .join(",", Arrays.asList(taskClass.split(","))) + ".");
          }
          System.out.println(buffer.toString());
          System.exit(-1);
        }
      } else {
        if (serviceClass == null) {
          System.out.println(
              "  the available task is " + String.join(",", entryList) + ".");
          System.exit(-1);
        } else {
          mainClass = serviceClass;
        }
      }
    } else {
      if (serviceClass == null) {
        System.out.println(
            "  the available task is " + String.join(",", entryList) + ".");
        System.exit(-1);
      } else {
        mainClass = serviceClass;
      }
    }
    return mainClass;
  }

  @Override
  protected ClassLoader createClassLoader(Iterator<Archive> archives)
      throws Exception {
    List<URL> urls = new ArrayList<>(guessClassPathSize());
    while (archives.hasNext()) {
      urls.add(archives.next().getUrl());
    }
    if (this.classPathIndex != null) {
      urls.addAll(this.classPathIndex.getUrls());
    }
    return createClassLoader(urls.toArray(new URL[0]));
  }

  private int guessClassPathSize() {
    if (this.classPathIndex != null) {
      return this.classPathIndex.size() + 10;
    }
    return 50;
  }

  @Override
  protected Iterator<Archive> getClassPathArchivesIterator() throws Exception {
    Archive.EntryFilter searchFilter = this::isSearchCandidate;
    Iterator<Archive> archives = this.archive.getNestedArchives(searchFilter,
        (entry) -> isNestedArchive(entry) && !isEntryIndexed(entry));
    if (isPostProcessingClassPathArchives()) {
      archives = applyClassPathArchivePostProcessing(archives);
    }
    return archives;
  }

  private boolean isEntryIndexed(Archive.Entry entry) {
    if (this.classPathIndex != null) {
      return this.classPathIndex.containsEntry(entry.getName());
    }
    return false;
  }

  private Iterator<Archive> applyClassPathArchivePostProcessing(
      Iterator<Archive> archives) throws Exception {
    List<Archive> list = new ArrayList<>();
    while (archives.hasNext()) {
      list.add(archives.next());
    }
    postProcessClassPathArchives(list);
    return list.iterator();
  }

  /**
   * Determine if the specified entry is a candidate for further searching.
   *
   * @param entry the entry to check
   * @return {@code true} if the entry is a candidate for further searching
   * @since 2.3.0
   */
  protected boolean isSearchCandidate(Archive.Entry entry) {
    return true;
  }

  /**
   * Determine if the specified entry is a nested item that should be added to the
   * classpath.
   *
   * @param entry the entry to check
   * @return {@code true} if the entry is a nested item (jar or directory)
   */
  protected abstract boolean isNestedArchive(Archive.Entry entry);

  /**
   * Return if post processing needs to be applied to the archives. For back
   * compatibility this method returns {@code true}, but subclasses that don't override
   * {@link #postProcessClassPathArchives(List)} should provide an implementation that
   * returns {@code false}.
   *
   * @return if the {@link #postProcessClassPathArchives(List)} method is implemented
   * @since 2.3.0
   */
  protected boolean isPostProcessingClassPathArchives() {
    return true;
  }

  /**
   * Called to post-process archive entries before they are used. Implementations can
   * add and remove entries.
   *
   * @param archives the archives
   * @throws Exception if the post processing fails
   * @see #isPostProcessingClassPathArchives()
   */
  protected void postProcessClassPathArchives(List<Archive> archives)
      throws Exception {
  }

  @Override
  protected boolean isExploded() {
    return this.archive.isExploded();
  }

  @Override
  protected final Archive getArchive() {
    return this.archive;
  }

  public String[] doChinFilters(String[] filters, String target) {
    String[] res = null;
    if (filters.length > 0) {
      List<String> tempList = Arrays.asList(filters);
      List<String> arrList = new ArrayList<String>(tempList);
      Iterator<String> it = arrList.iterator();
      while (it.hasNext()) {
        String x = it.next();
        if (x.indexOf(target) != -1) {
          it.remove();
        }
      }
      res = new String[arrList.size()];
      arrList.toArray(res);
    }
    return res;
  }
}
