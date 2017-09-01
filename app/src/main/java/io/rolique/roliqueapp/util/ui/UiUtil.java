package io.rolique.roliqueapp.util.ui;

import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.Calendar;
import java.util.List;

import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.util.DateUtil;

/**
 * Created by Volodymyr Oleshkevych on 8/24/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class UiUtil {

    public static void setImage(ImageView image, String path) {
        Glide.with(image.getContext())
                .load(path)
                .placeholder(R.color.white)
                .crossFade()
                .into(image);
    }

    public static String getStringTimeForView(String time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateUtil.transformDate(time));
        if (DateUtil.isToday(DateUtil.transformDate(time))) {
            String hour = DateUtil.getStringDate(calendar.get(Calendar.HOUR));
            String minutes = DateUtil.getStringDate(calendar.get(Calendar.MINUTE));
            return String.format("%s:%s", hour, minutes);
        } else {
            String dayOfMonth = DateUtil.getStringDate(calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateUtil.getStringDate(calendar.get(Calendar.MONTH));
            return String.format("%s.%s", dayOfMonth, month);
        }
    }

    public static String getUserName(String senderId, List<User> users) {
        for (User user: users)
            if (user.getId().equals(senderId))
                return String.format("%s %s", user.getFirstName(), user.getLastName());
        return "unknown user";
    }

}
