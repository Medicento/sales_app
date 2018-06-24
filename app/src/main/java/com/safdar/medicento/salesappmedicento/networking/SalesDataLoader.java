package com.safdar.medicento.salesappmedicento.networking;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.safdar.medicento.salesappmedicento.R;
import com.safdar.medicento.salesappmedicento.networking.util.SalesDataExtractor;

public class SalesDataLoader extends AsyncTaskLoader {

    private String mUrl;
    private String mAction;
    private Context mContext;
    public SalesDataLoader(@NonNull Context context, String url, String action) {
        super(context);
        mUrl = url;
        mAction = action;
        mContext = context;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Nullable
    @Override
    public Object loadInBackground() {
        if (mUrl == null) {
            return null;
        }
        //TODO: change mContext to getContext()
        if (mAction.equals(mContext.getString(R.string.fetch_area_action))) {
            return SalesDataExtractor.initiateConnection(mUrl, mAction, mContext);
        } else if (mAction.equals(mContext.getString(R.string.fetch_pharmacy_action))) {
            return SalesDataExtractor.initiateConnection(mUrl, mAction, mContext);
        } else if (mAction.equals(mContext.getString(R.string.login_action))) {
            return SalesDataExtractor.initiateConnection(mUrl, mAction, mContext);
        } else if (mAction.equals(mContext.getString(R.string.fetch_medicine_action))) {
            return SalesDataExtractor.initiateConnection(mUrl, mAction, mContext);
        }
        return null;
    }
}