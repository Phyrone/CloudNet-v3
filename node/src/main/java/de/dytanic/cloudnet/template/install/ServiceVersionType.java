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

package de.dytanic.cloudnet.template.install;

import de.dytanic.cloudnet.common.JavaVersion;
import de.dytanic.cloudnet.driver.service.ServiceEnvironment;
import de.dytanic.cloudnet.template.install.execute.InstallStep;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;

public class ServiceVersionType extends ServiceEnvironment {

  private final List<InstallStep> installSteps;
  private final Collection<ServiceVersion> versions;

  public ServiceVersionType(
    @NonNull String name,
    @NonNull String environmentType,
    @NonNull List<InstallStep> installSteps,
    @NonNull Collection<ServiceVersion> versions
  ) {
    super(name, environmentType);

    this.installSteps = installSteps;
    this.versions = versions;
  }

  public @NonNull Optional<ServiceVersion> version(@NonNull String name) {
    return this.versions.stream()
      .filter(serviceVersion -> serviceVersion.name().equalsIgnoreCase(name))
      .findFirst();
  }

  public boolean canInstall(@NonNull ServiceVersion serviceVersion) {
    return !this.installSteps.contains(InstallStep.BUILD) || serviceVersion.canRun();
  }

  public boolean canInstall(@NonNull ServiceVersion serviceVersion, JavaVersion javaVersion) {
    return !this.installSteps.contains(InstallStep.BUILD) || serviceVersion.canRun(javaVersion);
  }

  public @NonNull List<InstallStep> installSteps() {
    return this.installSteps;
  }

  public @NonNull Collection<ServiceVersion> versions() {
    return this.versions;
  }
}
