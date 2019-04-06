/*
 * Copyright 2017 mk
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

package pl.mk5.gdx.fireapp.android.database;

import com.badlogic.gdx.utils.Array;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

import pl.mk5.gdx.fireapp.GdxFIRDatabase;
import pl.mk5.gdx.fireapp.database.ChildEventType;
import pl.mk5.gdx.fireapp.database.ConnectionStatus;
import pl.mk5.gdx.fireapp.database.DatabaseConsumer;
import pl.mk5.gdx.fireapp.database.Filter;
import pl.mk5.gdx.fireapp.database.FilterType;
import pl.mk5.gdx.fireapp.database.FilteringStateEnsurer;
import pl.mk5.gdx.fireapp.database.OrderByClause;
import pl.mk5.gdx.fireapp.database.OrderByMode;
import pl.mk5.gdx.fireapp.distributions.DatabaseDistribution;
import pl.mk5.gdx.fireapp.exceptions.DatabaseReferenceNotSetException;
import pl.mk5.gdx.fireapp.functional.Consumer;
import pl.mk5.gdx.fireapp.functional.Function;
import pl.mk5.gdx.fireapp.promises.ConverterPromise;
import pl.mk5.gdx.fireapp.promises.FutureListenerPromise;
import pl.mk5.gdx.fireapp.promises.FuturePromise;
import pl.mk5.gdx.fireapp.promises.ListenerPromise;
import pl.mk5.gdx.fireapp.promises.Promise;

/**
 * Android Firebase database API implementation.
 * <p>
 *
 * @see DatabaseDistribution
 */
public class Database implements DatabaseDistribution {

    private static final String MISSING_REFERENCE = "Please call GdxFIRDatabase#inReference() first";

    private DatabaseReference databaseReference;
    private String databasePath;
    private final Array<Filter> filters;
    private OrderByClause orderByClause;


    /**
     * Constructor of android database distribution
     */
    public Database() {
        filters = new Array<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListenerPromise<ConnectionStatus> onConnect() {
        return FutureListenerPromise.whenListener(new Consumer<FutureListenerPromise<ConnectionStatus>>() {
            @Override
            public void accept(FutureListenerPromise<ConnectionStatus> promise) {
                new QueryConnectionStatus(Database.this, getDatabasePath())
                        .with(promise)
                        .execute();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DatabaseDistribution inReference(String databasePath) {
        databaseReference = FirebaseDatabase.getInstance().getReference(databasePath);
        this.databasePath = databasePath;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Promise<Void> setValue(final Object value) {
        checkDatabaseReference();
        return FuturePromise.when(new DatabaseConsumer<FuturePromise<Void>>(databasePath, orderByClause, filters) {
            @Override
            public void accept(FuturePromise<Void> voidFuturePromise) {
                new QuerySetValue(Database.this, getDatabasePath())
                        .withArgs(value)
                        .with(voidFuturePromise)
                        .execute();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T, E extends T> Promise<E> readValue(final Class<T> dataType) {
        checkDatabaseReference();
        FilteringStateEnsurer.checkFilteringState(filters, orderByClause, dataType);
        return ConverterPromise.whenWithConvert(new DatabaseConsumer<ConverterPromise<T, E>>(databasePath, orderByClause, filters) {
            @Override
            public void accept(ConverterPromise<T, E> teConverterPromise) {
                teConverterPromise.with(GdxFIRDatabase.instance().getMapConverter(), dataType);
                new QueryReadValue(Database.this, getDatabasePath())
                        .with(getFilters())
                        .with(getOrderByClause())
                        .withArgs(dataType)
                        .with(teConverterPromise)
                        .execute();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T, R extends T> ListenerPromise<R> onDataChange(final Class<T> dataType) {
        checkDatabaseReference();
        FilteringStateEnsurer.checkFilteringState(filters, orderByClause, dataType);
        return ConverterPromise.whenWithConvert(new DatabaseConsumer<ConverterPromise<T, R>>(databasePath, orderByClause, filters) {
            @Override
            public void accept(ConverterPromise<T, R> trConverterPromise) {
                trConverterPromise.with(GdxFIRDatabase.instance().getMapConverter(), dataType);
                new QueryOnDataChange(Database.this, getDatabasePath())
                        .with(getFilters())
                        .with(getOrderByClause())
                        .with(trConverterPromise)
                        .withArgs(dataType)
                        .execute();
            }
        });
    }

    @Override
    public <T, R extends T> ListenerPromise<R> onChildChange(final Class<T> dataType, final ChildEventType... eventsType) {
        checkDatabaseReference();
        FilteringStateEnsurer.checkFilteringState(filters, orderByClause, dataType);
        return ConverterPromise.whenWithConvert(new DatabaseConsumer<ConverterPromise<T, R>>(databasePath, orderByClause, filters) {
            @Override
            public void accept(ConverterPromise<T, R> trConverterPromise) {
                trConverterPromise.with(GdxFIRDatabase.instance().getMapConverter(), dataType);
                new QueryOnChildChange<>(Database.this, getDatabasePath())
                        .with(getFilters())
                        .with(getOrderByClause())
                        .with((FuturePromise<Object>) trConverterPromise) // TODO - suppose to be wrong cast here?
                        .withArgs(dataType, eventsType)
                        .execute();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <V> DatabaseDistribution filter(FilterType filterType, V... filterArguments) {
        filters.add(new Filter(filterType, filterArguments));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DatabaseDistribution orderBy(OrderByMode orderByMode, String argument) {
        orderByClause = new OrderByClause(orderByMode, argument);
        return this;
    }

    @Override
    public DatabaseDistribution orderBy(OrderByMode orderByMode) {
        return orderBy(orderByMode, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DatabaseDistribution push() {
        databaseReference = databaseReference().push();
        databasePath = databasePath + "/" + databaseReference.getKey();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Promise<Void> removeValue() {
        checkDatabaseReference();
        return FuturePromise.when(new DatabaseConsumer<FuturePromise<Void>>(databasePath, orderByClause, filters) {
            @Override
            public void accept(FuturePromise<Void> voidFuturePromise) {
                new QueryRemoveValue(Database.this, getDatabasePath())
                        .with(voidFuturePromise)
                        .execute();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Promise<Void> updateChildren(final Map<String, Object> data) {
        checkDatabaseReference();
        return FuturePromise.when(new DatabaseConsumer<FuturePromise<Void>>(databasePath, orderByClause, filters) {
            @Override
            public void accept(FuturePromise<Void> voidFuturePromise) {
                new QueryUpdateChildren(Database.this, getDatabasePath())
                        .withArgs(data)
                        .with(voidFuturePromise)
                        .execute();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T, R extends T> Promise<Void> transaction(final Class<T> dataType, final Function<R, R> transaction) {
        checkDatabaseReference();
        return FuturePromise.when(new DatabaseConsumer<FuturePromise<Void>>(databasePath, orderByClause, filters) {
            @Override
            public void accept(FuturePromise<Void> voidFuturePromise) {
                // TODO - converter
                new QueryRunTransaction(Database.this, getDatabasePath())
                        .withArgs(dataType, transaction)
                        .with(voidFuturePromise)
                        .execute();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPersistenceEnabled(boolean enabled) {
        FirebaseDatabase.getInstance().setPersistenceEnabled(enabled);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void keepSynced(boolean synced) {
        databaseReference().keepSynced(synced);
    }

    /**
     * Simple getter of {@link DatabaseReference} which this {@link Database} instance will be deal with.
     *
     * @return FirebaseSDK Database reference. Every action will be deal with it.
     * @throws DatabaseReferenceNotSetException It is thrown when user forgot to call {@link #inReference(String)}
     */
    DatabaseReference databaseReference() {
        checkDatabaseReference();
        return databaseReference;
    }

    /**
     * Reset {@link #databaseReference} and {@link #databasePath} to initial state.
     * After each flow-terminate operation{@link #databaseReference} and {@link #databasePath} should be reset the initial value,
     * it forces the users to call {@link #inReference(String)} after each flow-terminate operation.
     * <p>
     * Flow-terminate operations are: <uL>
     * <li>{@link #setValue(Object)}</li>
     * <li>{@link #setValue(Object)}</li>
     * <li>{@link #readValue(Class)}</li>
     * <li>{@link #onDataChange(Class)}</li>
     * <li>{@link #updateChildren(Map)}</li>
     * <li>{@link #transaction(Class, Function)}</li>
     * </uL>
     */
    void terminateOperation() {
        databaseReference = null;
        databasePath = null;
        orderByClause = null;
        filters.clear();
    }

    /**
     * Getter for databasePath.
     *
     * @return Database path, may be null
     */
    String getDatabasePath() {
        return databasePath;
    }

    private void checkDatabaseReference() {
        if (databaseReference == null)
            throw new DatabaseReferenceNotSetException(MISSING_REFERENCE);
    }
}
