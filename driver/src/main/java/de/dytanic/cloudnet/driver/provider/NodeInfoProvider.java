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

package de.dytanic.cloudnet.driver.provider;

import de.dytanic.cloudnet.common.concurrent.CompletableTask;
import de.dytanic.cloudnet.common.concurrent.ITask;
import de.dytanic.cloudnet.driver.command.CommandInfo;
import de.dytanic.cloudnet.driver.network.cluster.NetworkClusterNode;
import de.dytanic.cloudnet.driver.network.cluster.NetworkClusterNodeInfoSnapshot;
import de.dytanic.cloudnet.driver.network.rpc.annotation.RPCValidation;
import java.util.Collection;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * This interface provides access to the cluster
 */
@RPCValidation
public interface NodeInfoProvider {

  /**
   * @return the {@link CommandInfo} for each registered command
   */
  @NonNull Collection<CommandInfo> consoleCommands();

  /**
   * @param commandLine the whole console input containing the command
   * @return the {@link CommandInfo} if there is a registered command - null otherwise
   */
  @Nullable CommandInfo consoleCommand(@NonNull String commandLine);

  /**
   * Gets all tab complete results for the specific command line. If the line contains at least one space, it will get
   * the command and then the tab complete results out of it. If the line doesn't contain any spaces, it will return the
   * names of all registered commands that begin with the {@code commandLine} (case-insensitive).
   *
   * @param commandLine the command with arguments to get the results from
   * @return a collection containing all unsorted results
   */
  @NonNull Collection<String> consoleTabCompleteResults(@NonNull String commandLine);

  /**
   * Sends the given commandLine to the node, executes the commandLine and returns the response
   *
   * @param commandLine the commandLine to be sent
   * @return the reponse of the node
   */
  @NonNull Collection<String> sendCommandLine(@NonNull String commandLine);

  /**
   * Sends the given commandLine to a specific node in the cluster, executes the commandLine and returns the response
   *
   * @param commandLine the commandLine to be sent
   * @return the response of the node
   */
  @NonNull Collection<String> sendCommandLineToNode(@NonNull String nodeUniqueId, @NonNull String commandLine);

  /**
   * @return all nodes from the config of the node where the method is called on
   */
  @NonNull NetworkClusterNode[] nodes();

  /**
   * @param uniqueId the uniqueId of the target node
   * @return {@link NetworkClusterNode} from the config of the node where the method is called on, null if there is no
   * entry in the config
   */
  @Nullable NetworkClusterNode node(@NonNull String uniqueId);

  /**
   * @return all {@link NetworkClusterNodeInfoSnapshot} of nodes that are still connected
   */
  @NonNull NetworkClusterNodeInfoSnapshot[] nodeInfoSnapshots();

  /**
   * @param uniqueId the uniqueId of the target node
   * @return the {@link NetworkClusterNodeInfoSnapshot} for the given uniqueId, null if there is no snapshot
   */
  @Nullable NetworkClusterNodeInfoSnapshot nodeInfoSnapshot(@NonNull String uniqueId);

  /**
   * @return the {@link CommandInfo} for each registered command
   */
  default @NonNull ITask<Collection<CommandInfo>> consoleCommandsAsync() {
    return CompletableTask.supply(this::consoleCommands);
  }

  /**
   * @param commandLine the whole console input containing the command
   * @return the {@link CommandInfo} if there is a registered command - null otherwise
   */
  default @NonNull ITask<CommandInfo> consoleCommandAsync(@NonNull String commandLine) {
    return CompletableTask.supply(() -> this.consoleCommand(commandLine));
  }

  /**
   * Gets all tab complete results for the specific command line. If the line contains at least one space, it will get
   * the command and then the tab complete results out of it. If the line doesn't contain any spaces, it will return the
   * names of all registered commands that begin with the {@code commandLine} (case-insensitive).
   *
   * @param commandLine the command with arguments to get the results from
   * @return a collection containing all unsorted results
   */
  default @NonNull ITask<Collection<String>> consoleTabCompleteResultsAsync(@NonNull String commandLine) {
    return CompletableTask.supply(() -> this.consoleTabCompleteResults(commandLine));
  }

  /**
   * Sends the given commandLine to the node, executes the commandLine and returns the response
   *
   * @param commandLine the commandLine to be sent
   * @return the reponse of the node
   */
  @NonNull
  default ITask<Collection<String>> sendCommandLineAsync(@NonNull String commandLine) {
    return CompletableTask.supply(() -> this.sendCommandLine(commandLine));
  }

  /**
   * Sends the given commandLine to a specific node in the cluster, executes the commandLine and returns the response
   *
   * @param line the commandLine to be sent
   * @return the response of the node
   */
  @NonNull
  default ITask<Collection<String>> sendCommandLineToNodeAsync(@NonNull String nodeUniqueId, @NonNull String line) {
    return CompletableTask.supply(() -> this.sendCommandLineToNode(nodeUniqueId, line));
  }

  /**
   * @return all nodes from the config of the node where the method is called on
   */
  default @NonNull ITask<NetworkClusterNode[]> nodesAsync() {
    return CompletableTask.supply(this::nodes);
  }

  /**
   * @param uniqueId the uniqueId of the target node
   * @return {@link NetworkClusterNode} from the config of the node where the method is called on, null if there is no
   * entry in the config
   */
  default @NonNull ITask<NetworkClusterNode> nodeAsync(@NonNull String uniqueId) {
    return CompletableTask.supply(() -> this.node(uniqueId));
  }

  /**
   * @return all {@link NetworkClusterNodeInfoSnapshot} of nodes that are still connected
   */
  default @NonNull ITask<NetworkClusterNodeInfoSnapshot[]> nodeInfoSnapshotsAsync() {
    return CompletableTask.supply(this::nodeInfoSnapshots);
  }

  /**
   * @param uniqueId the uniqueId of the target node
   * @return the {@link NetworkClusterNodeInfoSnapshot} for the given uniqueId, null if there is no snapshot
   */
  default @NonNull ITask<NetworkClusterNodeInfoSnapshot> nodeInfoSnapshotAsync(@NonNull String uniqueId) {
    return CompletableTask.supply(() -> this.nodeInfoSnapshot(uniqueId));
  }
}
