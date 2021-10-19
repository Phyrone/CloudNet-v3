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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A listener for all tasks, that should handle the process
 *
 * @param <T> the type of the listener, which should accept if the operation from the ITask instance is complete
 * @see ITask
 */
public interface ITaskListener<T> {

  default void onComplete(@NotNull ITask<T> task, @Nullable T t) {
  }

  default void onCancelled(@NotNull ITask<T> task) {
  }

  default void onFailure(@NotNull ITask<T> task, @NotNull Throwable th) {
  }
}