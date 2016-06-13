/*
 *     This file is part of ToroDB.
 *
 *     ToroDB is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ToroDB is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with ToroDB. If not, see <http://www.gnu.org/licenses/>.
 *
 *     Copyright (c) 2014, 8Kdata Technology
 *     
 */

package com.torodb.backend.jobs;

import com.torodb.core.backend.BackendConnection;
import com.torodb.core.dsl.backend.AddCollectionDDLJob;
import com.torodb.core.transaction.BackendException;
import com.torodb.core.transaction.RollbackException;
import com.torodb.core.transaction.metainf.MetaCollection;
import com.torodb.core.transaction.metainf.MetaDatabase;

public class AddCollectionDDLJobImpl implements AddCollectionDDLJob {

    private final MetaDatabase db;
    private final MetaCollection newCol;

    public AddCollectionDDLJobImpl(MetaDatabase db, MetaCollection newCol) {
        super();
        this.db = db;
        this.newCol = newCol;
    }
    
    @Override
    public void execute(BackendConnection connection) throws BackendException, RollbackException {
        connection.addCollection(db, newCol);
    }

    @Override
    public MetaDatabase getDatabase() {
        return db;
    }

    @Override
    public MetaCollection getCollection() {
        return newCol;
    }

}
