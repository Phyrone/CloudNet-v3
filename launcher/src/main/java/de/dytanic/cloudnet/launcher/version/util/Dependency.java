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

package de.dytanic.cloudnet.launcher.version.util;

import java.nio.file.Path;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Dependency {

  private final String repository;
  private final String group;
  private final String name;
  private final String version;
  private final String fileVersion;

  private String classifier;

  public Dependency(String repository, String group, String name, String version, String fileVersion) {
    this.repository = repository;
    this.group = group;
    this.name = name;
    this.version = version;
    this.fileVersion = fileVersion;
  }

  public Dependency(String repository, String group, String name, String version, String fileVersion, String classifier) {
    this.repository = repository;
    this.group = group;
    this.name = name;
    this.version = version;
    this.classifier = classifier;
    this.fileVersion = fileVersion;
  }

  public Path toPath() {
    var fileName = String
      .format("%s-%s%s.jar", this.name, this.fileVersion, (this.classifier != null ? "-" + this.classifier : ""));

    return Path.of(this.group.replace(".", "/"), this.name, this.version, fileName);
  }

  public String getRepository() {
    return this.repository;
  }

  public String getGroup() {
    return this.group;
  }

  public String getName() {
    return this.name;
  }

  public String getVersion() {
    return this.version;
  }

  public String getClassifier() {
    return this.classifier;
  }

}