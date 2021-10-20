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

package eu.cloudnetservice.cloudnet.ext.signs.configuration;

public class SignGroupConfiguration implements Cloneable {

  protected String targetGroup;

  protected SignLayoutsHolder emptyLayout;
  protected SignLayoutsHolder onlineLayout;
  protected SignLayoutsHolder fullLayout;

  public SignGroupConfiguration() {
  }

  public SignGroupConfiguration(String targetGroup, SignLayoutsHolder emptyLayout, SignLayoutsHolder onlineLayout,
    SignLayoutsHolder fullLayout) {
    this.targetGroup = targetGroup;
    this.emptyLayout = emptyLayout;
    this.onlineLayout = onlineLayout;
    this.fullLayout = fullLayout;
  }

  public String getTargetGroup() {
    return this.targetGroup;
  }

  public SignLayoutsHolder getEmptyLayout() {
    return this.emptyLayout;
  }

  public SignLayoutsHolder getOnlineLayout() {
    return this.onlineLayout;
  }

  public SignLayoutsHolder getFullLayout() {
    return this.fullLayout;
  }

  @Override
  public SignGroupConfiguration clone() {
    try {
      return (SignGroupConfiguration) super.clone();
    } catch (CloneNotSupportedException e) {
      return new SignGroupConfiguration(this.targetGroup, this.emptyLayout, this.onlineLayout, this.fullLayout);
    }
  }
}