/*
 * ToroDB Server
 * Copyright © 2014 8Kdata Technology (www.8kdata.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.torodb.standalone.utils;

import com.google.common.base.Preconditions;
import com.google.common.net.HostAndPort;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.Service;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.torodb.core.backend.BackendBundle;
import com.torodb.core.bundle.BundleConfig;
import com.torodb.core.guice.EssentialModule;
import com.torodb.core.logging.ComponentLoggerFactory;
import com.torodb.core.logging.DefaultLoggerFactory;
import com.torodb.mongodb.core.MongoDbCoreBundle;
import com.torodb.mongodb.wp.MongoDbWpBundle;
import com.torodb.mongodb.wp.MongoDbWpConfig;
import com.torodb.standalone.ServerConfig;
import com.torodb.standalone.ServerService;
import com.torodb.testing.core.CloseableService;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Clock;
import java.util.Random;
import java.util.function.Function;


/**
 *
 */
public class ServerTestInstance extends AbstractIdleService implements CloseableService {

  private final Function<BundleConfig, BackendBundle> backendBundleGenerator;
  private Service serverService;
  private HostAndPort address;

  public ServerTestInstance(Function<BundleConfig, BackendBundle> backendBundleGenerator) {
    this.backendBundleGenerator = backendBundleGenerator;
  }

  @Override
  protected void startUp() throws Exception {
    int port = getAvailablePort();
    this.address = HostAndPort.fromParts("localhost", port);
    ServerConfig config = createServerConfig(port, backendBundleGenerator);

    serverService = new ServerService(config);
    serverService.startAsync();
    serverService.awaitRunning();
  }

  @Override
  protected void shutDown() throws Exception {
    serverService.stopAsync();
    serverService.awaitTerminated();
  }
  
  public HostAndPort getAddress() {
    Preconditions.checkState(isRunning(), "The service is not running");
    return address;
  }

  private int getAvailablePort() {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    } catch (IOException ex) {
      int ephemeralMax = 61000;
      int ephemeralMin = 32768;
      int range = ephemeralMax - ephemeralMin;

      return new Random(System.identityHashCode(this)).nextInt(range) + ephemeralMin;
    }
  }

  private static ServerConfig createServerConfig(int port, 
      Function<BundleConfig, BackendBundle> backendBundleGenerator) {

    HostAndPort selfAddress = HostAndPort.fromParts("localhost", port);

    return new ServerConfig(
        createEssentialInjector(),
        backendBundleGenerator,
        selfAddress,
        (bundleConf, mongoDbCoreBundle) -> createConfigBuilder(bundleConf, mongoDbCoreBundle, port),
        new ComponentLoggerFactory("LIFECYCLE")
    );
  }

  private static Injector createEssentialInjector() {
    return Guice.createInjector(new EssentialModule(
        DefaultLoggerFactory.getInstance(),
        () -> true,
        Clock.systemUTC())
    );
  }

  private static MongoDbWpBundle createConfigBuilder(BundleConfig bundleConfig,
      MongoDbCoreBundle mongoDbCoreBundle, int selfPort) {
    return new MongoDbWpBundle(
        new MongoDbWpConfig(mongoDbCoreBundle, selfPort, bundleConfig)
    );
  }

}
