package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by zhenghao on 2017-07-02.
 */

public class DatePickerFragment extends DialogFragment {

    public static final String EXTRA_DATE = "com.bignerdranch.android.criminalintent.date";

    private static final String ARG_DATE = "date";

    private DatePicker mDatePicker;

    public static DatePickerFragment newInstance(Date date) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, date);

        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        /*
        DatePickerFragment使用Date中的信息来初始化DatePicker对象。然而，DatePicker对
        象的初始化需整数形式的月、日、年。Date是个时间戳，无法直接提供整数形式的月、日、年。
        要达到目的，必须首先创建一个Calendar对象，然后用Date对象配置它，再从Calendar对
        象中取回所需信息。
        在onCreateDialog(...)方法内，从argument中获取Date对象，然后使用它和Calendar对
        象完成DatePicker的初始化工作

         */

        Date date = (Date) getArguments().getSerializable(ARG_DATE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_date, null);

        mDatePicker = (DatePicker) v.findViewById(R.id.dialog_date_date_picker);
        mDatePicker.init(year, month, day, null);


        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.date_picker_title)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int year = mDatePicker.getYear();
                                int month = mDatePicker.getMonth();
                                int day = mDatePicker.getDayOfMonth();
                                Date date = new GregorianCalendar(year, month, day).getTime();
                                sendResult(Activity.RESULT_OK, date);

                            }
                        })
                .create();
    }

    /*
    Activity.onActivityResult(...)方法是ActivityManager在子activity销毁后调用的父
    activity 方法。处理activity 间的数据返回时， ActivityManager 会自动调用Activity.
    onActivityResult(...)方法。父activity接收到Activity.onActivityResult(...)方法调用后，
    其FragmentManager会调用对应fragment的Fragment.onActivityResult(...)方法。
    处理由同一activity托管的两个fragment间的数据返回时，可借用Fragment.onActivity
    Result(...)方法。因此，直接调用目标fragment的Fragment.onActivityResult(...)方法，
    就能实现数据的回传。该方法恰好有我们需要的如下信息。
     */
    private void sendResult(int resultCode, Date date) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_DATE, date);

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);

    }

}
