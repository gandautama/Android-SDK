package com.baasbox.android.v4.utils;

import android.content.Context;
import android.support.v4.content.Loader;
import com.baasbox.android.*;
import com.baasbox.android.impl.Logger;

import java.util.List;

/**
 * Created by Andrea Tortorella on 27/01/14.
 */
public class BaasDocumentLoader extends Loader<BaasResult<List<BaasDocument>>> {
    private BaasResult<List<BaasDocument>> mDocuments;
    private final String mCollection;
    private final Filter mFilter;
    private RequestToken mCurrentLoad;

    /**
     * Stores away the application context associated with context.
     * Since Loaders can be used across multiple activities it's dangerous to
     * store the context directly; always use {@link #getContext()} to retrieve
     * the Loader's Context, don't use the constructor argument directly.
     * The Context returned by {@link #getContext} is safe to use across
     * Activity instances.
     *
     * @param context used to retrieve the application context.
     */
    public BaasDocumentLoader(Context context, String collection, Filter filter) {
        super(context);
        if (collection==null) throw new IllegalArgumentException("collection cannot be null");
        if (BaasBox.getDefault()==null){
            throw new IllegalStateException("BaasBox has not been initialized");
        }
        this.mCollection=collection;
        this.mFilter= filter==null?Filter.ANY:filter;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (mDocuments!=null){
            deliverResult(mDocuments);
        } else if (takeContentChanged()|| mCurrentLoad==null){
            forceLoad();
        } else {
            //do nothing
        }
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
        if (mCurrentLoad!=null){
            mCurrentLoad.abort();
        }

        mCurrentLoad = BaasDocument.fetchAll(mCollection,mFilter,handler);
    }

    private final BaasHandler<List<BaasDocument>> handler =
            new BaasHandler<List<BaasDocument>>() {
                @Override
                public void handle(BaasResult<List<BaasDocument>> result) {
                    complete(result);
                }
            };

    @Override
    protected void onReset() {
        super.onReset();
        if (mCurrentLoad!=null){
            mCurrentLoad.abort();
        }
        mDocuments =null;
        mCurrentLoad =null;
    }

    void complete(final  BaasResult<List<BaasDocument>> result){
        mCurrentLoad = null;
        mDocuments = result;
        if (isAbandoned()){
            Logger.debug("abandonded logger");
        } else if (isStarted()){
            Logger.debug("received documents");
            deliverResult(mDocuments);
        } else {
            Logger.debug("not to do");
        }
    }


}