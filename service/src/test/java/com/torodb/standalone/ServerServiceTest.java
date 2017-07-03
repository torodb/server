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

package com.torodb.standalone;

import static com.torodb.standalone.utils.RequireClientSupplier.BackendType.DERBY;

import com.mongodb.MongoClient;
import com.torodb.standalone.utils.ClientSupplier;
import com.torodb.standalone.utils.RequireClientSupplier;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;


@RunWith(JUnitPlatform.class)
@RequireClientSupplier(backend = DERBY)
public class ServerServiceTest {

  @Test
  public void isRunning(ClientSupplier clientSupplier) {

  }

  @Test
  @Disabled
  public void ping(ClientSupplier clientSupplier) {
    MongoClient client = clientSupplier.get();

    client.getDatabase("admin")
        .runCommand(new BsonDocument("ping", new BsonInt32(1)));
  }

}
