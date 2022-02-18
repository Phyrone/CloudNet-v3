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

package eu.cloudnetservice.cloudnet.driver.event.events.network;

import eu.cloudnetservice.cloudnet.driver.event.Cancelable;
import eu.cloudnetservice.cloudnet.driver.network.NetworkChannel;
import eu.cloudnetservice.cloudnet.driver.network.protocol.Packet;
import lombok.NonNull;

/**
 * An event fired before a packet gets sent to another network component. Writes or reads to or from the buffer will be
 * reflected down the line.
 *
 * @since 4.0
 */
public final class NetworkChannelPacketSendEvent extends NetworkEvent implements Cancelable {

  private final Packet packet;
  private boolean cancelled;

  /**
   * Creates a new instance of this network event.
   *
   * @param channel the channel which is associated with this event.
   * @param packet  the packet which will get send.
   * @throws NullPointerException if either the channel or packet is null.
   */
  public NetworkChannelPacketSendEvent(@NonNull NetworkChannel channel, @NonNull Packet packet) {
    super(channel);
    this.packet = packet;
  }

  /**
   * Get the packet which is being sent.
   *
   * @return the packet which is being sent.
   */
  public @NonNull Packet packet() {
    return this.packet;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean cancelled() {
    return this.cancelled;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void cancelled(boolean cancelled) {
    this.cancelled = cancelled;
  }
}