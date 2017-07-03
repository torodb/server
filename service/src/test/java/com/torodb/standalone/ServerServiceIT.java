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

import static com.torodb.standalone.utils.RequireClientSupplier.BackendType.POSTGRES;

import com.google.common.collect.Iterables;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.torodb.standalone.utils.ClientSupplier;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import com.torodb.standalone.utils.RequireClientSupplier;
import org.bson.BsonDocument;
import org.bson.BsonInt32;


/**
 *
 */
@RunWith(JUnitPlatform.class)
@RequireClientSupplier(backend = POSTGRES, newForEachCase = false)
public class ServerServiceIT {

  @Test
  public void someInserts(ClientSupplier clientSupplier) {
    MongoClient client = clientSupplier.get();
    MongoCollection<Document> collection = client.getDatabase("dbTest")
        .getCollection("colTest");
    for (int i = 0; i < 10; i++) {
      collection.insertOne(new Document("i", i));
    }

    Assertions.assertEquals(10, collection.count());
  }


  @Test
  @Disabled
  public void createCollection(ClientSupplier clientSupplier) {
    MongoClient client = clientSupplier.get();

    client.getDatabase("aDb")
        .createCollection("aCol");

    Assertions.assertTrue(
        Iterables.contains(
            client.getDatabase("aDb").listCollectionNames(),
            "aCol"
        ),
        "The created collection has not been found on the server"
    );
  }

  @Test
  @Disabled
  public void ping(ClientSupplier clientSupplier) {
    MongoClient client = clientSupplier.get();

    client.getDatabase("admin")
        .runCommand(new BsonDocument("ping", new BsonInt32(1)));
  }
}
