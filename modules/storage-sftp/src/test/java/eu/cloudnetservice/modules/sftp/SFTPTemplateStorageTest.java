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

package eu.cloudnetservice.modules.sftp;

import eu.cloudnetservice.cloudnet.common.io.FileUtil;
import eu.cloudnetservice.cloudnet.driver.network.HostAndPort;
import eu.cloudnetservice.cloudnet.driver.service.ServiceTemplate;
import eu.cloudnetservice.cloudnet.driver.template.FileInfo;
import eu.cloudnetservice.modules.sftp.config.SFTPTemplateStorageConfig;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.common.keyprovider.ClassLoadableResourceKeyPairProvider;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(OrderAnnotation.class)
public final class SFTPTemplateStorageTest {

  private static final int PORT;
  private static final Path BASE_PATH = Path.of("").resolve("build").toAbsolutePath();
  private static final ServiceTemplate TEMPLATE = ServiceTemplate.builder()
    .prefix("global")
    .name("proxy")
    .storage("sftp")
    .build();

  private static final Path TEMPLATE_PATH = FileUtil.resolve(BASE_PATH, "home", "CloudNet", "global", "proxy");

  private static SshServer server;
  private static SFTPTemplateStorage storage;

  static {
    try (var socket = new ServerSocket(0)) {
      PORT = socket.getLocalPort();
    } catch (IOException exception) {
      throw new ExceptionInInitializerError(exception);
    }
  }

  @BeforeAll
  static void setupServer() throws IOException {
    // init sftp
    var fsFactory = new VirtualFileSystemFactory();
    fsFactory.setDefaultHomeDir(BASE_PATH);

    // init the ssh server
    server = SshServer.setUpDefaultServer();
    server.setPort(PORT);
    server.setHost("127.0.0.1");
    // setup auth
    server.setKeyPairProvider(new ClassLoadableResourceKeyPairProvider("hostkey.pem"));
    server.setPasswordAuthenticator((user, password, $1) -> user.equals("test") && password.equals("ThisIsATest!"));
    // factories
    server.setFileSystemFactory(fsFactory);
    server.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory()));
    // start the server
    server.start();

    // init the storage
    storage = new SFTPTemplateStorage(new SFTPTemplateStorageConfig(
      new HostAndPort("127.0.0.1", PORT),
      "sftp",
      "test",
      "ThisIsATest!",
      null,
      null,
      null,
      "/home/CloudNet",
      1));
  }

  @AfterAll
  static void stopServer() throws IOException {
    storage.close();
    server.close(true).await();

    FileUtil.delete(BASE_PATH.resolve("home"));
  }

  @Test
  @Order(0)
  void testTemplateCreation() {
    Assertions.assertTrue(storage.create(TEMPLATE));
    Assertions.assertTrue(Files.exists(TEMPLATE_PATH));
    Assertions.assertTrue(Files.isDirectory(TEMPLATE_PATH));
  }

  @Test
  @Order(10)
  void testHasTemplate() {
    Assertions.assertTrue(storage.has(TEMPLATE));
    Assertions.assertFalse(storage.has(ServiceTemplate.builder()
      .prefix("hello")
      .name("world")
      .storage("sftp")
      .build()));
  }

  @Test
  @Order(20)
  void testNewOutputStream() throws IOException {
    try (var stream = storage.newOutputStream(TEMPLATE, "test.txt")) {
      Assertions.assertNotNull(stream);
      stream.write("Hello".getBytes(StandardCharsets.UTF_8));
    }

    Assertions.assertTrue(Files.exists(TEMPLATE_PATH.resolve("test.txt")));
    Assertions.assertEquals("Hello", String.join("\n", Files.readAllLines(TEMPLATE_PATH.resolve("test.txt"))));
  }

  @Test
  @Order(30)
  void testAppendOutputStream() throws IOException {
    try (var stream = storage.appendOutputStream(TEMPLATE, "test.txt")) {
      Assertions.assertNotNull(stream);
      stream.write("World".getBytes(StandardCharsets.UTF_8));
    }

    Assertions.assertTrue(Files.exists(TEMPLATE_PATH.resolve("test.txt")));
    Assertions.assertEquals("HelloWorld", String.join("\n", Files.readAllLines(TEMPLATE_PATH.resolve("test.txt"))));
  }

  @Test
  @Order(40)
  void testNewInputStream() throws IOException {
    try (var stream = storage.newInputStream(TEMPLATE, "test.txt")) {
      Assertions.assertNotNull(stream);
      Assertions.assertArrayEquals(stream.readAllBytes(), "HelloWorld".getBytes(StandardCharsets.UTF_8));
    }
  }

  @Test
  @Order(50)
  void testCreateFile() {
    Assertions.assertTrue(storage.createFile(TEMPLATE, "hello.txt"));
    Assertions.assertTrue(Files.exists(TEMPLATE_PATH.resolve("hello.txt")));
    Assertions.assertFalse(Files.isDirectory(TEMPLATE_PATH.resolve("hello.txt")));
  }

  @Test
  @Order(60)
  void testHasFile() {
    Assertions.assertTrue(storage.hasFile(TEMPLATE, "hello.txt"));
    Assertions.assertFalse(storage.hasFile(TEMPLATE, "world.txt"));
  }

  @Test
  @Order(70)
  void testFileGetFileInfo() {
    var info = storage.fileInfo(TEMPLATE, "hello.txt");
    Assertions.assertNotNull(info);
    Assertions.assertEquals(0, info.size());
    Assertions.assertEquals("hello.txt", info.path());
    Assertions.assertEquals("hello.txt", info.name());
  }

  @Test
  @Order(80)
  void testDeleteFile() {
    Assertions.assertTrue(storage.deleteFile(TEMPLATE, "hello.txt"));
    Assertions.assertFalse(storage.hasFile(TEMPLATE, "hello.txt"));
    Assertions.assertFalse(Files.exists(TEMPLATE_PATH.resolve("hello.txt")));
  }

  @Test
  @Order(90)
  void testCreateDirectory() {
    Assertions.assertTrue(storage.createDirectory(TEMPLATE, "hello"));
    Assertions.assertTrue(Files.exists(TEMPLATE_PATH.resolve("hello")));
    Assertions.assertTrue(Files.isDirectory(TEMPLATE_PATH.resolve("hello")));
  }

  @Test
  @Order(100)
  void testCreateFileInDirectory() {
    Assertions.assertTrue(storage.createFile(TEMPLATE, "hello/test.txt"));
    Assertions.assertTrue(Files.exists(TEMPLATE_PATH.resolve("hello").resolve("test.txt")));
    Assertions.assertFalse(Files.isDirectory(TEMPLATE_PATH.resolve("hello").resolve("test.txt")));
  }

  @Test
  @Order(110)
  void testFileListingNonDeep() {
    var files = storage.listFiles(TEMPLATE, "hello", false);
    Assertions.assertNotNull(files);
    Assertions.assertEquals(1, files.length);
    Assertions.assertEquals(0, files[0].size());
    Assertions.assertEquals("test.txt", files[0].name());
    Assertions.assertEquals("/home/CloudNet/global/proxy/hello/test.txt", files[0].path());
  }

  @Test
  @Order(120)
  void testFileListingDeep() {
    var files = storage.listFiles(TEMPLATE, "", true);
    Assertions.assertNotNull(files);
    Assertions.assertEquals(3, files.length);

    // there must be one directory
    var dir = Arrays.stream(files).filter(FileInfo::directory).findFirst().orElse(null);
    Assertions.assertNotNull(dir);
    Assertions.assertEquals("hello", dir.name());
  }

  @Test
  @Order(130)
  void testTemplateListing() {
    var templates = storage.templates();
    Assertions.assertEquals(1, templates.size());
    Assertions.assertEquals(TEMPLATE, templates.iterator().next());
  }

  @Test
  @Order(140)
  void testTemplateDelete() throws IOException {
    Assertions.assertTrue(storage.delete(TEMPLATE));
    Assertions.assertEquals(0, Files.list(TEMPLATE_PATH).count());
  }
}