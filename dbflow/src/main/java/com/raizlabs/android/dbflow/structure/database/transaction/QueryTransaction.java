package com.raizlabs.android.dbflow.structure.database.transaction;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.language.CursorResult;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: Provides an easy way to query for data asynchronously.
 */
public class QueryTransaction<TResult extends Model> implements ITransaction {

    /**
     * Simple interface that provides callback on result.
     *
     * @param <TResult> The result that we got from querying.
     */
    public interface QueryResultCallback<TResult extends Model> {

        /**
         * Called when the query completes.
         *
         * @param transaction The transaction that ran.
         * @param tResult     The result of the query. Use this object to get data that you need.
         */
        void onQueryResult(QueryTransaction transaction, @NonNull CursorResult<TResult> tResult);
    }

    final ModelQueriable<TResult> modelQueriable;
    final QueryResultCallback<TResult> queryResultCallback;

    QueryTransaction(Builder<TResult> builder) {
        modelQueriable = builder.modelQueriable;
        queryResultCallback = builder.queryResultCallback;
    }

    @Override
    public void execute(DatabaseWrapper databaseWrapper) {
        final CursorResult<TResult> cursorResult = modelQueriable.queryResults();
        if (queryResultCallback != null) {
            Transaction.TRANSACTION_HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    queryResultCallback.onQueryResult(QueryTransaction.this, cursorResult);
                }
            });
        }
    }

    /**
     * Provides easy way to build a {@link QueryTransaction}.
     *
     * @param <TResult>
     */
    public static final class Builder<TResult extends Model> {

        final ModelQueriable<TResult> modelQueriable;
        QueryResultCallback<TResult> queryResultCallback;

        public Builder(@NonNull ModelQueriable<TResult> modelQueriable) {
            this.modelQueriable = modelQueriable;
        }

        /**
         * Called when transaction completes and use this to get results.
         */
        public Builder<TResult> queryResult(QueryResultCallback<TResult> queryResultCallback) {
            this.queryResultCallback = queryResultCallback;
            return this;
        }

        /**
         * @return A new {@link QueryTransaction}. Subsequent calls to this method produce new
         * instances.
         */
        public QueryTransaction<TResult> build() {
            return new QueryTransaction<>(this);
        }
    }
}
