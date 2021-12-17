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

package de.dytanic.cloudnet.driver.network.http;

import lombok.NonNull;

public class MethodHttpHandlerAdapter implements IMethodHttpHandler {

  @Override
  public void handlePost(@NonNull String path, @NonNull IHttpContext context) throws Exception {
  }

  @Override
  public void handleGet(@NonNull String path, @NonNull IHttpContext context) throws Exception {
  }

  @Override
  public void handlePut(@NonNull String path, @NonNull IHttpContext context) throws Exception {
  }

  @Override
  public void handleHead(@NonNull String path, @NonNull IHttpContext context) throws Exception {
  }

  @Override
  public void handleDelete(@NonNull String path, @NonNull IHttpContext context) throws Exception {
  }

  @Override
  public void handlePatch(@NonNull String path, @NonNull IHttpContext context) throws Exception {
  }

  @Override
  public void handleTrace(@NonNull String path, @NonNull IHttpContext context) throws Exception {
  }

  @Override
  public void handleOptions(@NonNull String path, @NonNull IHttpContext context) throws Exception {
  }

  @Override
  public void handleConnect(@NonNull String path, @NonNull IHttpContext context) throws Exception {
  }
}
