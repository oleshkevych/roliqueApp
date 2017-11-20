package io.rolique.roliqueapp.screens.timesheetViewer.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.data.model.CheckIn;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.util.DateUtil;
import io.rolique.roliqueapp.util.ui.UiUtil;
import io.rolique.roliqueapp.widget.fixedHeaderTable.adapter.TableAdapter;
import timber.log.Timber;

/**
 * This class implements the main functionalities of the TableAdapter in
 * Mutuactivos.
 *
 * @author Brais Gab�n
 */
public class SampleTableAdapter implements TableAdapter {

    private final int width;
    private final int height;

    private final int VIEW_TYPE_HEADER = 0;
    private final int VIEW_TYPE_ITEM = 1;
    private final int VIEW_TYPE_COUNT = 2;
    private final LayoutInflater inflater;

    private List<User> mUsers;
    private Date mDate;
    private SimpleDateFormat mDateFormat;
    private List<List<Pair<String, Integer>>> mTableCheckIns;

    public interface OnClickListener {
        void onColumnClick(User user);
    }

    private final OnClickListener mOnClickListener;

    /**
     * Constructor
     *
     * @param context The current context.
     */
    public SampleTableAdapter(Context context, List<User> users, OnClickListener onClickListener) {
        inflater = LayoutInflater.from(context);
        mUsers = users;
        mOnClickListener = onClickListener;
        mDate = new Date();
        mDateFormat = new SimpleDateFormat("EEE, d.MM.yy", Locale.getDefault());
        mTableCheckIns = new ArrayList<>(mUsers.size());

        initValuesTable(mDate, mUsers, false);
        Resources resources = context.getResources();
        width = resources.getDimensionPixelSize(R.dimen.table_width);
        height = resources.getDimensionPixelSize(R.dimen.table_height);
    }

    private void initValuesTable(Date date, List<User> users, boolean isUpdate) {
        Date dateStart = new Date();
        for (int j = 0; j < users.size(); j++) {
            List<Pair<String, Integer>> pairs = new ArrayList<>(9);
            for (int i = 0; i < 9; i++) {
                CheckIn checkIn = users.get(j).getCheckInByDayOfYear(DateUtil.getDayOfYear(getDateByDayOfWeek(i + 1, date)));
                if (checkIn == null)
                    pairs.add(new Pair<>("", R.drawable.item_check_in_empty));
                else switch (checkIn.getType()) {
                    case CheckIn.CHECK_IN:
                        Date messageDate = DateUtil.transformDate(checkIn.getTime());
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(messageDate);
                        String hour = DateUtil.getStringDate(calendar.get(Calendar.HOUR_OF_DAY));
                        String minutes = DateUtil.getStringDate(calendar.get(Calendar.MINUTE));
                        int color = R.drawable.item_check_in_ok;
                        if ((calendar.get(Calendar.HOUR_OF_DAY) > 10) ||
                                (calendar.get(Calendar.HOUR_OF_DAY) == 10 && calendar.get(Calendar.MINUTE) > 47))
                            color = R.drawable.item_check_in_late;
                        pairs.add(i, new Pair<>(String.format("%s:%s", hour, minutes), color));
                        break;
                    case CheckIn.BUSINESS_TRIP:
                        pairs.add(new Pair<>(getCheckInTime(checkIn.getTime()), R.drawable.item_check_in_business_trip));
                        break;
                    case CheckIn.DAY_OFF:
                        pairs.add(new Pair<>(getCheckInTime(checkIn.getTime()), R.drawable.item_check_in_day_off));
                        break;
                    case CheckIn.REMOTELY:
                        pairs.add(new Pair<>(getCheckInTime(checkIn.getTime()), R.drawable.item_check_in_remotely));
                }
            }
            if (mTableCheckIns.size() == j)
                mTableCheckIns.add(pairs);
            else
                mTableCheckIns.set(j, pairs);
        }
        if (isUpdate)
            notifyDataSetChanged();
        Timber.e("Done: " + (new Date().getTime() - dateStart.getTime()));
    }

    private String getCheckInTime(String stringDate) {
        Date messageDate = DateUtil.transformDate(stringDate);
        Calendar messageCalendar = Calendar.getInstance();
        messageCalendar.setTime(messageDate);
        String hour = DateUtil.getStringDate(messageCalendar.get(Calendar.HOUR_OF_DAY));
        String minutes = DateUtil.getStringDate(messageCalendar.get(Calendar.MINUTE));
        return String.format("%s:%s", hour, minutes);
    }

    /**
     * Quick access to the LayoutInflater instance that this Adapter retreived
     * from its Context.
     *
     * @return The shared LayoutInflater.
     */
    public LayoutInflater getInflater() {
        return inflater;
    }

    @Override
    public View getView(int row, int column, View converView, ViewGroup parent) {
        if (converView == null) {
            converView = inflater.inflate(getLayoutResource(row, column),
                    parent, false);
        }
        setText(converView, row, column);
        return converView;
    }

    private void setText(View view, final int row, int column) {
        TextView textView = view.findViewById(R.id.table_item_text);
        textView.setText(getCellString(row, column));
        if (row >= 0 && column >= 0)
            textView.setBackground(ContextCompat.getDrawable(view.getContext(), mTableCheckIns.get(row).get(column).second));
        if (column == -1 && row >= 0) {
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnClickListener.onColumnClick(mUsers.get(row));
                }
            });
        }
    }

    @Override
    public int getRowCount() {
        return mUsers.size();
    }

    @Override
    public int getColumnCount() {
        return 7;
    }

    @Override
    public int getWidth(int column) {
        return column == -1 ? (int) Math.round(width * 1.4) : width;
    }

    @Override
    public int getHeight(int row) {
        return height;
    }

    //set data to table start
    public String getCellString(int row, int column) {
        if (row == -1 && column == -1)
            return inflater.getContext().getString(R.string.activity_timesheet_viewer_user_column);
        if (row == -1)
            return getStringDate(column + 1);
        if (column == -1)
            return UiUtil.getUserNameForView(mUsers.get(row));
        return mTableCheckIns.get(row).get(column).first;
    }

    private String getStringDate(int dayOfWeek) {
        Date dateToShow = getDateByDayOfWeek(dayOfWeek, mDate);
        return mDateFormat.format(dateToShow);
    }

    @NonNull
    private Date getDateByDayOfWeek(int dayOfWeek, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        return new Date(date.getTime() + (dayOfWeek - currentDayOfWeek) * 24 * 60 * 60 * 1000);
    }
//set data to table end

    public void updateValues(Date date, List<User> users) {
        mUsers = UiUtil.getSortedUsersList(users);
        mDate = date;
        initValuesTable(date, mUsers, true);
    }

    public int getLayoutResource(int row, int column) {
        final int layoutResource;
        switch (getItemViewType(row, column)) {
            case VIEW_TYPE_HEADER:
                layoutResource = R.layout.item_style_table_header;
                break;
            case VIEW_TYPE_ITEM:
                layoutResource = R.layout.item_style_table;
                break;
            default:
                throw new RuntimeException("wtf?");
        }
        return layoutResource;
    }

    @Override
    public int getItemViewType(int row, int column) {
        if (row < 0 || column < 0) {
            return VIEW_TYPE_HEADER;
        } else {
            return VIEW_TYPE_ITEM;
        }
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public int getBackgroundResId(int row, int column) {
        final int backgroundResource;
        switch (getItemViewType(row, column)) {
            case VIEW_TYPE_HEADER:
                backgroundResource = R.drawable.item_style_table_header;
                break;
            case VIEW_TYPE_ITEM:
                backgroundResource = R.drawable.item_style_table;
                break;
            default:
                throw new RuntimeException("wtf?");
        }

        return backgroundResource;
    }

    @Override
    public int getBackgroundHighlightResId(int row, int column) {
        return R.drawable.item_style_table;
    }

    @Override
    public boolean isRowSelectable(int row) {
        return true;
    }

    @Override
    public long getItemId(int row, int column) {
        return 0;
    }

    @Override
    public Object getItem(int row, int column) {
        return null;
    }

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

}
