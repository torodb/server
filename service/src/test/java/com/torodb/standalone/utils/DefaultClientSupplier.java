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
import com.mongodb.MongoClient;
import com.torodb.testing.docker.sql.SqlService;

class DefaultClientSupplier implements ClientSupplier {

  private final SqlService sqlService;
  private final ServerTestInstance serverInstance;
  private final MongoClient client;
  private boolean closed = false;

  DefaultClientSupplier(SqlService sqlService, ServerTestInstance serverInstance) {
    this.sqlService = sqlService;
    this.serverInstance = serverInstance;

    assert sqlService.isRunning() : "The sql server is not running";
    assert serverInstance.isRunning() : "The ToroDB Server is not running";

    HostAndPort serverAddress = serverInstance.getAddress();
    this.client = new MongoClient(serverAddress.getHost(), serverAddress.getPort());
  }

  @Override
  public void close() {
    if (!closed) {
      closed = true;
      client.close();
      serverInstance.stopAsync();
      serverInstance.awaitTerminated();

      sqlService.stopAsync();
      sqlService.awaitTerminated();
    }
  }

  @Override
  public MongoClient get() {
    Preconditions.checkState(!closed, "The client supplier has been closed");
    assert client != null;
    return client;
  }

}
