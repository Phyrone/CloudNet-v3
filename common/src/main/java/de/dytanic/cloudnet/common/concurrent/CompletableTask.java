/*
 * Copyright 2019-2021 CloudNetService team & contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.dytanic.cloudnet.common.concurrent;

import de.dytanic.cloudnet.common.function.ThrowableFunction;
import de.dytanic.cloudnet.common.function.ThrowableSupplier;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.UnmodifiableView;

public class CompletableTask<V> extends CompletableFuture<V> implements ITask<V> {

  private static final ExecutorService SERVICE = Executors.newCachedThreadPool();

  private volatile Collection<ITaskListener<V>> listeners;

  public CompletableTask() {
    // handles the uni completion stage 'done' (or success)
    this.thenAccept(result -> {
      // check if there are registered listeners
      if (this.listeners != null) {
        for (var listener : this.listeners) {
          listener.onComplete(this, result);
        }
        // depopulate the listeners - no more completions are possible
        this.depopulateListeners();
      }
    });
    // handles the uni completion stages 'cancel' and 'exceptionally'
    this.exceptionally(throwable -> {
      // check if there are registered listeners
      if (this.listeners != null) {
        // check if the future was cancelled
        if (throwable instanceof CancellationException) {
          // post the cancel result
          for (var listener : this.listeners) {
            listener.onCancelled(this);
          }
        } else {
          // exception completion - post that
          for (var listener : this.listeners) {
            listener.onFailure(this, throwable);
          }
        }
        // depopulate the listeners - no more completions are possible
        this.depopulateListeners();
      }
      // must be a function...
      return null;
    });
  }

  public static <V> @NonNull CompletableTask<V> supply(@NonNull Runnable runnable) {
    return supply(() -> {
      runnable.run();
      return null;
    });
  }

  public static <V> @NonNull CompletableTask<V> supply(@NonNull ThrowableSupplier<V, Throwable> supplier) {
    var task = new CompletableTask<V>();
    SERVICE.execute(() -> {
      try {
        task.complete(supplier.get());
      } catch (Throwable throwable) {
        task.completeExceptionally(throwable);
      }
    });
    return task;
  }


  @Override
  public @NonNull ITask<V> addListener(@NonNull ITaskListener<V> listener) {
    this.initListeners().add(listener);
    return this;
  }

  @Override
  public @NonNull ITask<V> clearListeners() {
    // we don't need to initialize the listeners field here
    if (this.listeners != null) {
      this.listeners.clear();
    }

    return this;
  }

  @Override
  public @UnmodifiableView @NonNull Collection<ITaskListener<V>> listeners() {
    return this.listeners == null ? Collections.emptyList() : Collections.unmodifiableCollection(this.listeners);
  }

  @Override
  public @UnknownNullability V getDef(@Nullable V def) {
    try {
      return this.join();
    } catch (CancellationException | CompletionException exception) {
      return def;
    }
  }

  @Override
  public @UnknownNullability V get(long time, @NonNull TimeUnit timeUnit, @Nullable V def) {
    try {
      return this.get(time, timeUnit);
    } catch (CancellationException | ExecutionException | InterruptedException | TimeoutException exception) {
      return def;
    }
  }

  @Override
  public @NonNull <T> ITask<T> map(@NonNull ThrowableFunction<V, T, Throwable> mapper) {
    // if this task is already done we can just compute the value
    if (this.isDone()) {
      return CompletedTask.create(() -> mapper.apply(this.getNow(null)));
    }
    // create a new task mapping the current task
    var task = new CompletableTask<T>();
    // handle the result of this task and post the result to the downstream task
    this.addListener(new ITaskListener<V>() {
      @Override
      public void onComplete(@NonNull ITask<V> t, @Nullable V v) {
        try {
          task.complete(mapper.apply(v));
        } catch (Throwable throwable) {
          task.completeExceptionally(throwable);
        }
      }

      @Override
      public void onCancelled(@NonNull ITask<V> t) {
        task.cancel(true);
      }

      @Override
      public void onFailure(@NonNull ITask<V> t, @NonNull Throwable th) {
        task.completeExceptionally(th);
      }
    });
    // the new task listens now to this task
    return task;
  }

  protected @NonNull Collection<ITaskListener<V>> initListeners() {
    // ConcurrentLinkedQueue gives us O(1) insertion using CAS - results under moderate
    // load in the fastest insert and read times
    return Objects.requireNonNullElseGet(this.listeners, () -> this.listeners = new ConcurrentLinkedQueue<>());
  }

  protected void depopulateListeners() {
    // ensures a better gc
    this.listeners.clear();
    this.listeners = null;
  }
}
