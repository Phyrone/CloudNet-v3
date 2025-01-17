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

package eu.cloudnetservice.ext.platforminject.runtime.platform.fabric;

import static eu.cloudnetservice.ext.platforminject.runtime.util.BindingUtil.fixedBindingWithBound;

import dev.derklaro.aerogel.SpecifiedInjector;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.ext.platforminject.api.defaults.BasePlatformPluginManager;
import eu.cloudnetservice.ext.platforminject.api.util.FunctionalUtil;
import lombok.NonNull;

public final class FabricPlatformPluginManager extends BasePlatformPluginManager<Integer, Object> {

  public FabricPlatformPluginManager() {
    super(System::identityHashCode, FunctionalUtil.identity());
  }

  @Override
  protected @NonNull InjectionLayer<SpecifiedInjector> createInjectionLayer(@NonNull Object platformData) {
    return InjectionLayer.specifiedChild(
      BASE_INJECTION_LAYER,
      "plugin",
      (layer, injector) -> injector.installSpecified(fixedBindingWithBound(platformData, platformData.getClass())));
  }
}
