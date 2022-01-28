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

package eu.cloudnetservice.cloudnet.node.service;

import eu.cloudnetservice.cloudnet.driver.network.NetworkChannel;
import eu.cloudnetservice.cloudnet.driver.provider.service.SpecificCloudServiceProvider;
import eu.cloudnetservice.cloudnet.driver.service.ServiceConfiguration;
import eu.cloudnetservice.cloudnet.driver.service.ServiceDeployment;
import eu.cloudnetservice.cloudnet.driver.service.ServiceId;
import eu.cloudnetservice.cloudnet.driver.service.ServiceInfoSnapshot;
import eu.cloudnetservice.cloudnet.driver.service.ServiceLifeCycle;
import eu.cloudnetservice.cloudnet.driver.service.ServiceRemoteInclusion;
import eu.cloudnetservice.cloudnet.driver.service.ServiceTemplate;
import java.nio.file.Path;
import java.util.Queue;
import lombok.NonNull;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

public interface CloudService extends SpecificCloudServiceProvider {

  @Override
  @NonNull ServiceInfoSnapshot serviceInfo();

  @NonNull String runtime();

  @NonNull Queue<ServiceRemoteInclusion> waitingIncludes();

  @NonNull Queue<ServiceTemplate> waitingTemplates();

  @NonNull Queue<ServiceDeployment> waitingDeployments();

  @NonNull ServiceLifeCycle lifeCycle();

  @NonNull CloudServiceManager cloudServiceManager();

  @NonNull ServiceConfiguration serviceConfiguration();

  @NonNull ServiceId serviceId();

  @NonNull String connectionKey();

  @NonNull Path directory();

  @NonNull Path pluginDirectory();

  @Nullable NetworkChannel networkChannel();

  @Internal
  void networkChannel(@Nullable NetworkChannel channel);

  @NonNull ServiceInfoSnapshot lastServiceInfoSnapshot();

  @NonNull ServiceConsoleLogCache serviceConsoleLogCache();

  boolean alive();

  void publishServiceInfoSnapshot();

  @Internal
  void updateServiceInfoSnapshot(@NonNull ServiceInfoSnapshot serviceInfoSnapshot);
}