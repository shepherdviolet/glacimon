/*
 * Copyright (C) 2022-2022 S.Violet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project GitHub: https://github.com/shepherdviolet/glacimon
 * Email: shepherdviolet@163.com
 */

package com.github.shepherdviolet.glacimon.java.concurrent;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Get source code from ThreadFactoryBuilder (Guava)
 *
 * Enhanced:
 * 1.Add thread factory id before thread id in thread name (%s or %d)
 *
 * ---------------------------------------------------------------------
 *
 * A ThreadFactory builder, providing any combination of these features:
 * <ul>
 * <li>whether threads should be marked as {@linkplain Thread#setDaemon daemon} threads
 * <li>a {@linkplain GuavaThreadFactoryBuilder#setNameFormat naming format}
 * <li>a {@linkplain Thread#setPriority thread priority}
 * <li>an {@linkplain Thread#setUncaughtExceptionHandler uncaught exception handler}
 * <li>a {@linkplain ThreadFactory#newThread backing thread factory}
 * </ul>
 * <p>If no backing thread factory is provided, a default backing thread factory is used as if by
 * calling {@code setThreadFactory(}{@link Executors#defaultThreadFactory()}{@code )}.
 *
 * @author Kurt Alfred Kluever
 * @since 4.0
 */
public final class GuavaThreadFactoryBuilder {

  private static final AtomicLong threadFactoryIdCount = new AtomicLong(0L);

  private String nameFormat = null;
  private Boolean daemon = null;
  private Integer priority = null;
  private UncaughtExceptionHandler uncaughtExceptionHandler = null;
  private ThreadFactory backingThreadFactory = null;

  /**
   * Creates a new {@link ThreadFactory} builder.
   */
  public GuavaThreadFactoryBuilder() {}

  /**
   * Sets the naming format to use when naming threads ({@link Thread#setName}) which are created
   * with this ThreadFactory.
   *
   * @param nameFormat a {@link String#format(String, Object...)}-compatible format String, to which
   *     a unique integer (0, 1, etc.) will be supplied as the single parameter. This integer will
   *     be unique to the built instance of the ThreadFactory and will be assigned sequentially. For
   *     example, {@code "rpc-pool-%s"} will generate thread names like {@code "rpc-pool-0-0"},
   *     {@code "rpc-pool-0-1"}, {@code "rpc-pool-0-2"}, etc.
   * @return this for the builder pattern
   */
  public GuavaThreadFactoryBuilder setNameFormat(String nameFormat) {
    // fail fast if the format is bad or null
    String unused = format(nameFormat, 0);
    this.nameFormat = nameFormat;
    return this;
  }

  /**
   * Sets daemon or not for new threads created with this ThreadFactory.
   *
   * @param daemon whether or not new Threads created with this ThreadFactory will be daemon threads
   * @return this for the builder pattern
   */
  public GuavaThreadFactoryBuilder setDaemon(boolean daemon) {
    this.daemon = daemon;
    return this;
  }

  /**
   * Sets the priority for new threads created with this ThreadFactory.
   *
   * @param priority the priority for new Threads created with this ThreadFactory
   * @return this for the builder pattern
   */
  public GuavaThreadFactoryBuilder setPriority(int priority) {
    // Thread#setPriority() already checks for validity. These error messages
    // are nicer though and will fail-fast.
    if (priority < Thread.MIN_PRIORITY) {
      throw new IllegalArgumentException("Thread priority " + priority + " must be >= " + Thread.MIN_PRIORITY);
    }
    if (priority > Thread.MAX_PRIORITY) {
      throw new IllegalArgumentException("Thread priority " + priority + " must be <= " + Thread.MAX_PRIORITY);
    }
    this.priority = priority;
    return this;
  }

  /**
   * Sets the {@link UncaughtExceptionHandler} for new threads created with this ThreadFactory.
   *
   * @param uncaughtExceptionHandler the uncaught exception handler for new Threads created with
   *     this ThreadFactory
   * @return this for the builder pattern
   */
  public GuavaThreadFactoryBuilder setUncaughtExceptionHandler(
      UncaughtExceptionHandler uncaughtExceptionHandler) {
    if (uncaughtExceptionHandler == null) {
      throw new NullPointerException();
    }
    this.uncaughtExceptionHandler = uncaughtExceptionHandler;
    return this;
  }

  /**
   * Sets the backing {@link ThreadFactory} for new threads created with this ThreadFactory. Threads
   * will be created by invoking #newThread(Runnable) on this backing {@link ThreadFactory}.
   *
   * @param backingThreadFactory the backing {@link ThreadFactory} which will be delegated to during
   *     thread creation.
   * @return this for the builder pattern
   *
   **/
  public GuavaThreadFactoryBuilder setThreadFactory(ThreadFactory backingThreadFactory) {
    if (backingThreadFactory == null) {
      throw new NullPointerException();
    }
    this.backingThreadFactory = backingThreadFactory;
    return this;
  }

  /**
   * Returns a new thread factory using the options supplied during the building process. After
   * building, it is still possible to change the options used to build the ThreadFactory and/or
   * build again. State is not shared amongst built instances.
   *
   * @return the fully constructed {@link ThreadFactory}
   */
  public ThreadFactory build() {
    return doBuild(this);
  }

  /**
   * Split out so that the anonymous ThreadFactory can't contain a reference back to the builder.
   * At least, I assume that's why.
   * @param builder builder
   * @return ThreadFactory
   */
  private static ThreadFactory doBuild(GuavaThreadFactoryBuilder builder) {
    // Both %d %s are supported
    final String nameFormat = builder.nameFormat != null ? builder.nameFormat.replaceAll("%d", "%s") : null;
    final Boolean daemon = builder.daemon;
    final Integer priority = builder.priority;
    final UncaughtExceptionHandler uncaughtExceptionHandler = builder.uncaughtExceptionHandler;
    final ThreadFactory backingThreadFactory =
        (builder.backingThreadFactory != null)
            ? builder.backingThreadFactory
            : Executors.defaultThreadFactory();
    final long threadFactoryId = threadFactoryIdCount.getAndIncrement();
    final AtomicLong count = (nameFormat != null) ? new AtomicLong(0) : null;
    return new ThreadFactory() {
      @Override
      public Thread newThread(Runnable runnable) {
        Thread thread = backingThreadFactory.newThread(runnable);
        if (nameFormat != null) {
          thread.setName(format(nameFormat, threadFactoryId + "-" + count.getAndIncrement()));
        }
        if (daemon != null) {
          thread.setDaemon(daemon);
        }
        if (priority != null) {
          thread.setPriority(priority);
        }
        if (uncaughtExceptionHandler != null) {
          thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
        }
        return thread;
      }
    };
  }

  private static String format(String format, Object... args) {
    return String.format(Locale.ROOT, format, args);
  }
}
