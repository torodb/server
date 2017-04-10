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

import static com.torodb.standalone.utils.RequireClientSupplier.BackendType.POSTGRES;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation the annotated JUnit 5 test to be able to resolve {@link ClientSupplier}
 * parameters.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(ClientSupplierExtension.class)
public @interface RequireClientSupplier {

  BackendType backend() default POSTGRES;

  /**
   * If each case requires its own {@link ClientSupplier} instace.
   *
   * If this is false, all cases will share the same instance and therefore the supplied client will
   * <em>see</em> changes produced by previous tests. A mehod annotated with {@link BeforeEach} in
   * the test class can be used to clean the state of the database.
   * 
   * @return if each test requires its own fresh client.
   */
  boolean newForEachCase() default true;

  public static enum BackendType {
    POSTGRES,
    DERBY
  }

}
