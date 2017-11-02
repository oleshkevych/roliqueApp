package io.rolique.roliqueapp.widget.fixedHeaderTable.adapter;

import android.database.DataSetObservable;
import android.database.DataSetObserver;

import io.rolique.roliqueapp.widget.fixedHeaderTable.TableFixHeaders;

/**
 * Common base class of common implementation for an {@link TableAdapter} that
 * can be used in {@link TableFixHeaders}.
 */
public abstract class BaseTableAdapter implements TableAdapter {

    private final DataSetObservable mDataSetObservable = new DataSetObservable();

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }

    /**
     * Notifies the attached observers that the underlying data has been changed
     * and any View reflecting the data set should refresh itself.
     */
    public void notifyDataSetChanged() {
        mDataSetObservable.notifyChanged();
    }

    /**
     * Notifies the attached observers that the underlying data is no longer
     * valid or available. Once invoked this adapter is no longer valid and
     * should not report further data set changes.
     */
    public void notifyDataSetInvalidated() {
        mDataSetObservable.notifyInvalidated();
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public int getItemViewType(int row, int column) {
        return 0;
    }
}
