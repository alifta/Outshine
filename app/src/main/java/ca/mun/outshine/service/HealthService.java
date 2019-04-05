package ca.mun.outshine.service;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ca.mun.outshine.util.TimeUtil;

import static java.text.DateFormat.getTimeInstance;

public class HealthService extends IntentService {

    private static final String TAG = "HealthService";

    private Context context;

    private boolean logging;
    private boolean untilNow;
    private int elapsedDays;
    private String healthType;

    int steps;
    int calories;
    int activeTime;

    private static List<DataType> dataTypes = Arrays.asList(
            DataType.TYPE_STEP_COUNT_DELTA,
            DataType.TYPE_CALORIES_EXPENDED,
            DataType.TYPE_MOVE_MINUTES);

    private static List<DataType> dataAggregates = Arrays.asList(
            DataType.AGGREGATE_STEP_COUNT_DELTA,
            DataType.AGGREGATE_CALORIES_EXPENDED,
            DataType.AGGREGATE_MOVE_MINUTES);

    FitnessOptions fitnessOptions =
            FitnessOptions.builder()
                    .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_MOVE_MINUTES, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.AGGREGATE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.AGGREGATE_MOVE_MINUTES, FitnessOptions.ACCESS_READ)
                    .build();

    // default constructor
    public HealthService() {
        // name the worker thread for debugging.
        super("HealthService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        // get the the needed context by calling getApplicationContext()
        this.context = getApplicationContext();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // main action that happens when the service is called
        // fetch data passed into the intent on start
        logging = intent.getBooleanExtra(Constants.HEALTH_LOG, true);
        untilNow = intent.getBooleanExtra(Constants.HEALTH_NOW, false);
        elapsedDays = intent.getIntExtra(Constants.HEALTH_DAY, 0);

        if (untilNow) {
            healthType = intent.getStringExtra(Constants.HEALTH_TYPE);

            switch (healthType) {
                case Constants.HEALTH_TYPE_STEPS:
                    readSteps(elapsedDays);
                    break;
                case Constants.HEALTH_TYPE_CALORIES:
                    readCalories(elapsedDays);
                    break;
                case Constants.HEALTH_TYPE_ACTIVE_TIME:
                    readActiveTime(elapsedDays);
                    break;
            }
        } else {
            // read steps, calories, and active-time
            // and broadcast them back to fragment
            readSteps(elapsedDays);
            readCalories(elapsedDays);
            readActiveTime(elapsedDays);
        }

    }

    // read steps data from fitness api
    public void readSteps(int date) {
        // begin by creating the query.
        DataReadRequest readRequest = queryFitnessData(date, 0, untilNow);

        // invoke the History API to fetch the data with the query
        Fitness.getHistoryClient(context, GoogleSignIn.getLastSignedInAccount(context))
                .readData(readRequest)
                .addOnSuccessListener(
                        new OnSuccessListener<DataReadResponse>() {
                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                if (dataReadResponse.getBuckets().size() > 0) {
                                    steps = dataReadResponse.getBuckets().get(0).getDataSets().get(0)
                                            .getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();

                                    Intent i = new Intent(Constants.HEALTH_ACTION);
                                    // Put extras into the intent as usual
                                    i.putExtra(Constants.HEALTH_RESULT_CODE, Activity.RESULT_OK);
                                    i.putExtra(Constants.HEALTH_RESULT_TYPE, Constants.HEALTH_TYPE_STEPS);
                                    i.putExtra(Constants.HEALTH_RESULT_VALUE, steps);

                                    LocalBroadcastManager.getInstance(context).sendBroadcast(i);
                                    // printData(dataReadResponse);
                                    if (logging) Log.d(TAG, "Steps: " + steps);
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "There was a problem reading the data.", e);
                            }
                        });
    }

    // read calories data from fitness api
    private void readCalories(int date) {
        DataReadRequest readRequest = queryFitnessData(date, 1, untilNow);
        Fitness.getHistoryClient(context, GoogleSignIn.getLastSignedInAccount(context))
                .readData(readRequest)
                .addOnSuccessListener(
                        new OnSuccessListener<DataReadResponse>() {
                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                if (dataReadResponse.getBuckets().size() > 0) {
                                    calories = (int) dataReadResponse.getBuckets().get(0).getDataSets().get(0)
                                            .getDataPoints().get(0).getValue(Field.FIELD_CALORIES).asFloat();

                                    Intent i = new Intent(Constants.HEALTH_ACTION);
                                    i.putExtra(Constants.HEALTH_RESULT_CODE, Activity.RESULT_OK);
                                    i.putExtra(Constants.HEALTH_RESULT_TYPE, Constants.HEALTH_TYPE_CALORIES);
                                    i.putExtra(Constants.HEALTH_RESULT_VALUE, calories);

                                    LocalBroadcastManager.getInstance(context).sendBroadcast(i);
                                    if (logging) Log.d(TAG, "Calories: " + calories);
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "There was a problem reading the data.", e);
                            }
                        });
    }

    // read active-time data from fitness api
    private void readActiveTime(int date) {
        DataReadRequest readRequest = queryFitnessData(date, 2, untilNow);
        Fitness.getHistoryClient(context, GoogleSignIn.getLastSignedInAccount(context))
//        Fitness.getHistoryClient(context, GoogleSignIn.getAccountForExtension(context, fitnessOptions))
                .readData(readRequest)
                .addOnSuccessListener(
                        new OnSuccessListener<DataReadResponse>() {
                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                if (dataReadResponse.getBuckets().size() > 0) {
                                    activeTime = dataReadResponse.getBuckets().get(0).getDataSets().get(0)
                                            .getDataPoints().get(0).getValue(Field.FIELD_DURATION).asInt();

                                    Intent i = new Intent(Constants.HEALTH_ACTION);
                                    i.putExtra(Constants.HEALTH_RESULT_CODE, Activity.RESULT_OK);
                                    i.putExtra(Constants.HEALTH_RESULT_TYPE, Constants.HEALTH_TYPE_ACTIVE_TIME);
                                    i.putExtra(Constants.HEALTH_RESULT_VALUE, activeTime);

                                    LocalBroadcastManager.getInstance(context).sendBroadcast(i);
                                    if (logging) Log.d(TAG, "Active Time: " + activeTime);
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "There was a problem reading the data.", e);
                            }
                        });
    }

    // create readRequest data type for fitness api
    private DataReadRequest queryFitnessData(int date, int type, boolean untilNow) {
        // type 0: steps (and distance)
        // type 1: calories
        // type 2: active-time
        long[] time = TimeUtil.timeRange(date, untilNow);
        return new DataReadRequest.Builder()
                .aggregate(dataTypes.get(type), dataAggregates.get(type))
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(time[0], time[1], TimeUnit.MILLISECONDS)
                .build();
    }

    private void printData(DataReadResponse dataReadResult) {
        // Parse result
        // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
        // as buckets containing DataSets, instead of just DataSets.
        if (dataReadResult.getBuckets().size() > 0) {
            Log.i(
                    TAG, "Number of returned buckets of DataSets is: " + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet);
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            Log.i(TAG, "Number of returned DataSets is: " + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpDataSet(dataSet);
            }
        }
    }

    private void dumpDataSet(DataSet dataSet) {
        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = getTimeInstance();

        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for (Field field : dp.getDataType().getFields()) {
                Log.i(TAG, "\tField: " + field.getFormat() + field.getName() + " Value: " + dp.getValue(field));
            }
        }
    }

}
