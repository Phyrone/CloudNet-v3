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

package eu.cloudnetservice.cloudnet.node.network.listener.message;

import eu.cloudnetservice.cloudnet.common.language.I18n;
import eu.cloudnetservice.cloudnet.common.log.LogManager;
import eu.cloudnetservice.cloudnet.common.log.Logger;
import eu.cloudnetservice.cloudnet.driver.event.EventListener;
import eu.cloudnetservice.cloudnet.driver.event.EventManager;
import eu.cloudnetservice.cloudnet.driver.event.events.channel.ChannelMessageReceiveEvent;
import eu.cloudnetservice.cloudnet.driver.network.buffer.DataBuf;
import eu.cloudnetservice.cloudnet.driver.network.cluster.NetworkClusterNode;
import eu.cloudnetservice.cloudnet.driver.network.cluster.NetworkClusterNodeInfoSnapshot;
import eu.cloudnetservice.cloudnet.driver.network.def.NetworkConstants;
import eu.cloudnetservice.cloudnet.node.CloudNet;
import eu.cloudnetservice.cloudnet.node.cluster.NodeServerProvider;
import eu.cloudnetservice.cloudnet.node.cluster.sync.DataSyncRegistry;
import eu.cloudnetservice.cloudnet.node.event.cluster.NetworkClusterNodeInfoUpdateEvent;
import eu.cloudnetservice.cloudnet.node.provider.NodeClusterNodeProvider;
import lombok.NonNull;

public final class NodeChannelMessageListener {

  private static final Logger LOGGER = LogManager.logger(NodeChannelMessageListener.class);

  private final EventManager eventManager;
  private final DataSyncRegistry dataSyncRegistry;
  private final NodeClusterNodeProvider nodeInfoProvider;
  private final NodeServerProvider nodeServerProvider;

  public NodeChannelMessageListener(
    @NonNull EventManager eventManager,
    @NonNull DataSyncRegistry dataSyncRegistry,
    @NonNull NodeClusterNodeProvider nodeInfoProvider,
    @NonNull NodeServerProvider nodeServerProvider
  ) {
    this.eventManager = eventManager;
    this.dataSyncRegistry = dataSyncRegistry;
    this.nodeInfoProvider = nodeInfoProvider;
    this.nodeServerProvider = nodeServerProvider;
  }

  @EventListener
  public void handleChannelMessage(@NonNull ChannelMessageReceiveEvent event) {
    if (event.channel().equals(NetworkConstants.INTERNAL_MSG_CHANNEL)) {
      switch (event.message()) {
        // update a single node info snapshot
        case "update_node_info_snapshot" -> {
          var snapshot = event.content().readObject(NetworkClusterNodeInfoSnapshot.class);
          // get the associated node server
          var server = this.nodeServerProvider.node(snapshot.node().uniqueId());
          if (server != null) {
            server.updateNodeInfoSnapshot(snapshot);
            this.eventManager.callEvent(new NetworkClusterNodeInfoUpdateEvent(event.networkChannel(), snapshot));
          }
        }

        // handles the sync requests of cluster data
        case "sync_cluster_data" -> {
          // handle the sync and send back the data to override on the caller
          var result = this.dataSyncRegistry.handle(event.content(), event.content().readBoolean());
          if (result != null && event.query()) {
            event.binaryResponse(result);
          }
        }

        // handle adding a new cluster node on other nodes
        case "register_known_node" -> {
          // register the node
          var node = event.content().readObject(NetworkClusterNode.class);
          this.nodeInfoProvider.addNodeSilently(node);

          // inform the user
          LOGGER.info(I18n.trans("command-cluster-add-node-success", node.uniqueId()));
        }

        // handle the removal of a cluster node from other nodes
        case "remove_known_node" -> {
          // unregister the node
          var node = event.content().readObject(NetworkClusterNode.class);
          this.nodeInfoProvider.removeNodeSilently(node);

          // remove the node
          LOGGER.info(I18n.trans("command-cluster-remove-node-success", node.uniqueId()));
        }

        // handles the shutdown of a cluster node
        case "cluster_node_shutdown" -> CloudNet.instance().stop();

        // request of the full cluster data set
        case "request_initial_cluster_data" -> {
          var server = this.nodeServerProvider.node(event.networkChannel());
          if (server != null) {
            // do not force the sync - the user can decide which changes should be used
            server.syncClusterData(CloudNet.instance().config().forceInitialClusterDataSync());
          }
        }

        // execute a command
        case "send_command_line" -> {
          var response = this.nodeServerProvider.localNode().sendCommandLine(event.content().readString());
          event.binaryResponse(DataBuf.empty().writeObject(response));
        }

        // change the local draining state
        case "change_draining_state" -> this.nodeServerProvider.localNode().drain(event.content().readBoolean());

        // none of our business
        default -> {
        }
      }
    }
  }
}