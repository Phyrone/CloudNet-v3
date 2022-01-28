/*
 * Copyright 2019-2022 CloudNetService team & contributors
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

package eu.cloudnetservice.cloudnet.node.template.install.execute.defaults;

import eu.cloudnetservice.cloudnet.node.template.install.InstallInformation;
import eu.cloudnetservice.cloudnet.node.template.install.execute.InstallStepExecutor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import lombok.NonNull;

public class DeployStepExecutor implements InstallStepExecutor {

  @Override
  public @NonNull Set<Path> execute(
    @NonNull InstallInformation info,
    @NonNull Path workingDirectory,
    @NonNull Set<Path> inputPaths
  ) throws IOException {
    for (var path : inputPaths) {
      if (Files.isDirectory(path)) {
        continue;
      }

      var relativePath = workingDirectory.relativize(path).toString().replace("\\", "/");
      try (var outputStream = info.templateStorage().newOutputStream(relativePath)) {
        Files.copy(path, Objects.requireNonNull(outputStream));
      }
    }

    return inputPaths;
  }
}