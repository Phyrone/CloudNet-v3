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

package eu.cloudnetservice.modules.s3;

import de.dytanic.cloudnet.CloudNet;
import de.dytanic.cloudnet.cluster.sync.DataSyncHandler;
import de.dytanic.cloudnet.common.document.gson.JsonDocument;
import de.dytanic.cloudnet.driver.module.ModuleLifeCycle;
import de.dytanic.cloudnet.driver.module.ModuleTask;
import de.dytanic.cloudnet.driver.module.driver.DriverModule;
import de.dytanic.cloudnet.driver.template.TemplateStorage;
import eu.cloudnetservice.modules.s3.config.S3TemplateStorageConfig;
import java.nio.file.Files;
import org.jetbrains.annotations.NotNull;

public final class S3TemplateStorageModule extends DriverModule {

  private S3TemplateStorage storage;
  private volatile S3TemplateStorageConfig config;

  @ModuleTask(event = ModuleLifeCycle.LOADED)
  public void handleInit() {
    if (Files.exists(this.getConfigPath())) {
      this.config = JsonDocument.newDocument(this.getConfigPath()).toInstanceOf(S3TemplateStorageConfig.class);
      // init the storage
      this.storage = new S3TemplateStorage(this);
      this.getServiceRegistry().registerService(TemplateStorage.class, config.getName(), this.storage);
      // register the cluster sync handler
      CloudNet.getInstance().getDataSyncRegistry().registerHandler(DataSyncHandler.<S3TemplateStorageConfig>builder()
        .key("s3-storage-config")
        .nameExtractor($ -> "S3 Template Storage Config")
        .convertObject(S3TemplateStorageConfig.class)
        .writer(this::writeConfig)
        .singletonCollector(() -> this.config)
        .currentGetter($ -> this.config)
        .build());
    } else {
      JsonDocument.newDocument(new S3TemplateStorageConfig()).write(this.getConfigPath());
    }
  }

  @ModuleTask(event = ModuleLifeCycle.STOPPED)
  public void handleStop() {
    this.storage.close();
    this.getServiceRegistry().unregisterService(TemplateStorage.class, this.storage.getName());
  }

  public void writeConfig(@NotNull S3TemplateStorageConfig config) {
    this.config = config;
    JsonDocument.newDocument(config).write(this.getConfigPath());
  }

  public @NotNull S3TemplateStorageConfig getConfig() {
    return this.config;
  }
}