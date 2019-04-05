package ca.mun.outshine;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import ca.mun.outshine.service.Constants;
import ca.mun.outshine.service.HealthService;
import ca.mun.outshine.util.TimeUtil;
import devlight.io.library.ArcProgressStackView;

public class MyDayFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = "MyDayFragment";

    // variable
    int steps;
    int distance;
    int calories;
    int activeTime;
    int elapsedDays = 0;
    int goalSteps = 10000;
    int goalDistance = 5000; // unit = meter
    int goalCalories = 2000;
    int goalActiveTime = 40;
    int progress;
    float progressFloat;

    // widgets
    private TextView calendarText;
    private TextView stepsText, distanceText, caloriesText, activeTimeText;
    private ImageButton prevDay, nextDay;

    // arc
    private ArcProgressStackView arcProgressSteps;
    private ArcProgressStackView arcProgressDistance;
    private ArcProgressStackView arcProgressCalories;
    private ArcProgressStackView arcProgressActiveTime;
    private final static int COUNT = 4;
    private int[] mStartColors = new int[COUNT];
    private int[] mEndColors = new int[COUNT];
    private int[] mBgColors = new int[COUNT];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_day, container, false);

        calendarText = v.findViewById(R.id.calendar_text);
        calendarText.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(Calendar.getInstance().getTimeInMillis()));
        calendarText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readDate();
                launchHealthService();
            }
        });

        stepsText = v.findViewById(R.id.steps_txt);
        distanceText = v.findViewById(R.id.distance_txt);
        caloriesText = v.findViewById(R.id.calories_txt);
        activeTimeText = v.findViewById(R.id.active_time_txt);

        prevDay = v.findViewById(R.id.btn_prev_day);
        prevDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goPrevDay();
                isCalendarToday();
            }
        });

        nextDay = v.findViewById(R.id.btn_next_day);
        // if calender is pointing to today -> disable the button
        isCalendarToday();
        nextDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goNextDay();
            }
        });

        // get refresh fitness data
        // for the very first time, elapsedDays is 0, resulting in today's measurements
        launchHealthService();

        // arc
        arcProgressSteps = v.findViewById(R.id.apsv_steps);
        arcProgressDistance = v.findViewById(R.id.apsv_distance);
        arcProgressCalories = v.findViewById(R.id.apsv_calories);
        arcProgressActiveTime = v.findViewById(R.id.apsv_active_time);
        initArc();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        // register broadcast based on ACTION string
        IntentFilter filter = new IntentFilter(Constants.HEALTH_ACTION);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(healthReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        // unregister the listener when the application is paused
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(healthReceiver);
    }

    private void launchHealthService() {
        // Construct Intent specifying the Service
        Intent i = new Intent(getActivity(), HealthService.class);
        // Add extras to the bundle
        i.putExtra(Constants.HEALTH_LOG, true);
        i.putExtra(Constants.HEALTH_DAY, this.elapsedDays);
        // Start the service
        getActivity().startService(i);
    }

    // callback when data is received from broadcast
    public BroadcastReceiver healthReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.HEALTH_ACTION)) {
                int resultCode = intent.getIntExtra(Constants.HEALTH_RESULT_CODE, Activity.RESULT_CANCELED);
                if (resultCode == Activity.RESULT_OK) {
                    String resultType = intent.getStringExtra(Constants.HEALTH_RESULT_TYPE);
                    switch (resultType) {
                        case Constants.HEALTH_TYPE_STEPS:
                            steps = intent.getIntExtra(Constants.HEALTH_RESULT_VALUE, 0);
                            progressFloat = (float) steps * 100 / goalSteps;
                            progress = Math.round(progressFloat);
                            // Log.d(TAG, "progressStp: " + progress);
                            arcProgressSteps.getModels().get(0).setProgress(progress);
                            arcProgressSteps.animateProgress();
                            stepsText.setText(String.valueOf(steps));
                            distance = steps * 65 / 100;
                            progressFloat = (float) distance * 100 / goalDistance;
                            progress = Math.round(progressFloat);
                            // Log.d(TAG, "progressDis: " + progress);
                            arcProgressDistance.getModels().get(0).setProgress(progress);
                            arcProgressDistance.animateProgress();
                            if (distance > 1000) {
                                double km = distance / 1000;
                                distanceText.setText(String.format("%.1f", km) + " (Km)");
                            } else {
                                distanceText.setText(distance + " (m)");
                            }
                            break;
                        case Constants.HEALTH_TYPE_CALORIES:
                            calories = intent.getIntExtra(Constants.HEALTH_RESULT_VALUE, 0);
                            progressFloat = (float) calories * 100 / goalCalories;
                            progress = Math.round(progressFloat);
                            // Log.d(TAG, "progressCal: " + progress);
                            arcProgressCalories.getModels().get(0).setProgress(progress);
                            arcProgressCalories.animateProgress();
                            caloriesText.setText(String.valueOf(calories));
                            break;
                        case Constants.HEALTH_TYPE_ACTIVE_TIME:
                            activeTime = intent.getIntExtra(Constants.HEALTH_RESULT_VALUE, 0);
                            progressFloat = (float) activeTime * 100 / goalActiveTime;
                            progress = Math.round(progressFloat);
                            // Log.d(TAG, "progressAct: " + progress);
                            arcProgressActiveTime.getModels().get(0).setProgress(progress);
                            arcProgressActiveTime.animateProgress();
                            activeTimeText.setText(String.valueOf(activeTime));
                            break;
                    }
                    // Log.d(TAG, "BroadcastReceiver onReceive: " + steps + "\t" + distance + "\t" + calories + "\t" + activeTime);
                }
            }
        }
    };

    private void initArc() {
        // Get colors
        final String[] startColors = getResources().getStringArray(R.array.start_colors);
        final String[] endColors = getResources().getStringArray(R.array.end_colors);
        final String[] bgColors = getResources().getStringArray(R.array.bg_colors);
        // Parse colors
        for (int i = 0; i < COUNT; i++) {
            mStartColors[i] = Color.parseColor(startColors[i]);
            mEndColors[i] = Color.parseColor(endColors[i]);
            mBgColors[i] = Color.parseColor(bgColors[i]);
        }
        // Set models
        final ArrayList<ArcProgressStackView.Model> modelsStep = new ArrayList<>();
        final ArrayList<ArcProgressStackView.Model> modelsDistance = new ArrayList<>();
        final ArrayList<ArcProgressStackView.Model> modelsCalories = new ArrayList<>();
        final ArrayList<ArcProgressStackView.Model> modelsActiveTime = new ArrayList<>();
        modelsStep.add(new ArcProgressStackView.Model("Steps", 0, mBgColors[0], mStartColors[0]));
        modelsStep.get(0).setColors(new int[]{mStartColors[0], mEndColors[0]});
        modelsDistance.add(new ArcProgressStackView.Model("Distance", 0, mBgColors[0], mStartColors[1]));
        modelsDistance.get(0).setColors(new int[]{mStartColors[1], mEndColors[1]});
        modelsCalories.add(new ArcProgressStackView.Model("Calories", 0, mBgColors[0], mStartColors[2]));
        modelsCalories.get(0).setColors(new int[]{mStartColors[2], mEndColors[2]});
        modelsActiveTime.add(new ArcProgressStackView.Model("Active Time", 0, mBgColors[0], mStartColors[3]));
        modelsActiveTime.get(0).setColors(new int[]{mStartColors[3], mEndColors[3]});
        arcProgressSteps.setModels(modelsStep);
        arcProgressDistance.setModels(modelsDistance);
        arcProgressCalories.setModels(modelsCalories);
        arcProgressActiveTime.setModels(modelsActiveTime);
        arcProgressSteps.animateProgress();
        arcProgressDistance.animateProgress();
        arcProgressCalories.animateProgress();
        arcProgressActiveTime.animateProgress();
    }

    private void goPrevDay() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy");
        try {
            c.setTime(sdf.parse(calendarText.getText().toString()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.add(Calendar.DATE, -1);
        elapsedDays = TimeUtil.elapsedDays(c);
        launchHealthService();
        String dateString = DateFormat.getDateInstance(DateFormat.MEDIUM).format(c.getTime());
        calendarText.setText(dateString);
    }

    private void goNextDay() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy");
        try {
            c.setTime(sdf.parse(calendarText.getText().toString()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // if calender is  not pointing to today -> load next day
        if (!isCalendarToday()) {
            c.add(Calendar.DATE, +1);
            elapsedDays = TimeUtil.elapsedDays(c);
            launchHealthService();
            String dateString = DateFormat.getDateInstance(DateFormat.MEDIUM).format(c.getTime());
            calendarText.setText(dateString);
        }
    }

    private boolean isCalendarToday() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.AM_PM, Calendar.AM);
        long today = c.getTimeInMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy");
        try {
            c.setTime(sdf.parse(calendarText.getText().toString()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (today == c.getTimeInMillis()) {
            nextDay.setVisibility(View.INVISIBLE);
            return true;
        } else {
            nextDay.setVisibility(View.VISIBLE);
            return false;
        }
    }

    // invoke when click on calendar text view
    // get the selected date within onDateSet listener
    // then update text view and read fitness data for that date
    private void readDate() {
        DatePickerFragment datePicker = new DatePickerFragment();
        datePicker.setListeningActivity(MyDayFragment.this);
        datePicker.show(this.getFragmentManager(), "date");
    }

    // get selected date from date picker
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, dayOfMonth);
        elapsedDays = TimeUtil.elapsedDays(c);
        // get refresh fitness data
        launchHealthService();
        String dateString = DateFormat.getDateInstance(DateFormat.MEDIUM).format(c.getTime());
        calendarText.setText(dateString);
        isCalendarToday();
    }

}
