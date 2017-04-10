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

import static com.torodb.standalone.utils.RequireClientSupplier.BackendType.DERBY;
import static com.torodb.standalone.utils.RequireClientSupplier.BackendType.POSTGRES;

import com.google.common.net.HostAndPort;
import com.torodb.backend.BackendConfigImplBuilder;
import com.torodb.backend.derby.DerbyDbBackendBundle;
import com.torodb.backend.derby.driver.DerbyDbBackendConfigBuilder;
import com.torodb.backend.postgresql.PostgreSqlBackendBundle;
import com.torodb.core.backend.BackendBundle;
import com.torodb.core.bundle.BundleConfig;
import com.torodb.testing.core.junit5.AnnotationFinder;
import com.torodb.testing.core.junit5.CloseableParameterResolver;
import com.torodb.testing.docker.postgres.EnumVersion;
import com.torodb.testing.docker.postgres.PostgresConfig;
import com.torodb.testing.docker.postgres.PostgresService;
import com.torodb.testing.docker.postgres.PostgresVersion;
import com.torodb.testing.docker.sql.SqlService;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.function.Function;


/**
 * The JUnit 5 extension that resolves parameters of type {@link ClientSupplier}.
 */
class ClientSupplierExtension extends CloseableParameterResolver<ClientSupplier> {

  private static final PostgresVersion POSTGRES_VERSION = EnumVersion.LATEST;

  @Override
  protected Class<ClientSupplier> getParameterClass() {
    return ClientSupplier.class;
  }

  @Override
  protected ClientSupplier createParamValue(ExtensionContext context) {
    RequireClientSupplier annotation = findInjectorAnnotation(context);

    SqlService sqlService = createSqlService(annotation);
    sqlService.startAsync();
    sqlService.awaitRunning();

    boolean correct = false;
    try {
      ServerTestInstance testInstance = new ServerTestInstance(
          createBackendBundleGenerator(annotation, sqlService)
      );
      testInstance.startAsync();
      testInstance.awaitRunning();

      ClientSupplier result = new DefaultClientSupplier(sqlService, testInstance);

      correct = true;

      return result;
    } finally {
      if (!correct) {
        sqlService.stopAsync();
        sqlService.awaitTerminated();
      }
    }
  }

  @Override
  protected boolean cleanAfterTest(ExtensionContext context) {
    return findInjectorAnnotation(context).newForEachCase();
  }

  private RequireClientSupplier findInjectorAnnotation(ExtensionContext context) {
    return AnnotationFinder.resolve(context, RequireClientSupplier.class);
  }

  private PostgresService createSqlService(RequireClientSupplier annotation) {
    return PostgresService.defaultService(EnumVersion.LATEST);
  }

  private static Function<BundleConfig, BackendBundle> createBackendBundleGenerator(
      RequireClientSupplier annotation, SqlService sqlService) {
    switch (annotation.backend()) {
      case DERBY: {
        return (BundleConfig bc) -> createDerbyBackendBundle(bc, sqlService);
      }
      case POSTGRES: {
        return (BundleConfig bc) -> createPostgresBackendBundle(bc, sqlService);
      }
      default: {
        throw new AssertionError("Unexpected backend type " + annotation.backend());
      }
    }
  }

  private static BackendBundle createDerbyBackendBundle(BundleConfig bundleConfig,
      SqlService sqlService) {
    HostAndPort address = sqlService.getAddress();
    return new DerbyDbBackendBundle(new DerbyDbBackendConfigBuilder(bundleConfig)
        .setDbHost(address.getHost())
        .setDbPort(address.getPort())
        .build()
    );
  }

  private static BackendBundle createPostgresBackendBundle(BundleConfig bundleConfig,
      SqlService sqlService) {

    HostAndPort address = sqlService.getAddress();

    PostgresConfig sqlConfig = PostgresConfig.getDefaultConfig(POSTGRES_VERSION);

    return new PostgreSqlBackendBundle(new BackendConfigImplBuilder(bundleConfig)
        .setDbHost(address.getHost())
        .setDbPort(address.getPort())
        .setUsername(sqlConfig.getUsername())
        .setPassword(sqlConfig.getPassword())
        .setDbName(sqlConfig.getDb())
        .build()
    );
  }
}
