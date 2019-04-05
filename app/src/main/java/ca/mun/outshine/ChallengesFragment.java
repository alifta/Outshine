package ca.mun.outshine;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ca.mun.outshine.model.Challenge;
import ca.mun.outshine.model.ChallengeAdapter;
import ca.mun.outshine.service.Constants;
import ca.mun.outshine.service.HealthService;
import ca.mun.outshine.util.ChallengeUtil;
import ca.mun.outshine.util.TimeUtil;

public class ChallengesFragment extends Fragment implements
        View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "ChallengesFragment";

    // fragment view
    View view;

    // widgets
    private FloatingActionButton fab;
    private SwipeRefreshLayout refreshLayout;

    // firebase
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseFirestore db;
    private DocumentSnapshot lastQueriedDocument;

    // recycler view
    ArrayList<Challenge> challengeList;
    private RecyclerView recyclerView;
    private ChallengeAdapter recyclerAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private int score;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // create the view
        view = inflater.inflate(R.layout.fragment_challenges, container, false);

        // firebase authenticate
        setupFirebaseAuth();

        // firestore
        initFirestore();

        // widgets
        fab = view.findViewById(R.id.challenge_fab);
        fab.setOnClickListener(this);

        // initialize recycler view and its adaptor and layout manager
        initRecyclerView();

        refreshLayout = view.findViewById(R.id.challenge_refresh_layout);
        refreshLayout.setOnRefreshListener(this);
        // coloring
        refreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        // showing Swipe Refresh animation on activity create
        // as animation won't start on onCreate, post runnable is used
//        refreshLayout.post(new Runnable() {
//            @Override
//            public void run() {
//                refreshLayout.setRefreshing(true);
//                getChallenges();
//                refreshLayout.setRefreshing(false);
//            }
//        });

        // initialize challenge list
//        createTestChallengeList(); // todo: remove later

        loadRecyclerViewData();

//        readTest();
        updateTest();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // go to login if not signed in
        FirebaseAuth.getInstance().addAuthStateListener(authListener);

        // add a update listener for any changes in challenge table in db

    }

    public void onResume() {
        super.onResume();
        // register broadcast based on ACTION string
        IntentFilter filter = new IntentFilter(Constants.HEALTH_ACTION);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(healthReceiver, filter);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(authListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // unregister the listener when the application is paused
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(healthReceiver);
    }

    // happen when swipe to refresh
    @Override
    public void onRefresh() {
        // fetch data or possibly upload to cloud
        loadRecyclerViewData();
    }

    @Override
    public void onClick(View view) {
        // listen to click on fab
        switch (view.getId()) {

            case R.id.challenge_fab: {
                // insert a new challenge
                createNewChallenge();

                break;
            }
        }
    }

    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: started.");

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    // Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    // finish();
                    getActivity().finish();
                }
            }
        };
    }

    private void signOut() {
        Log.d(TAG, "signing out ...");
        FirebaseAuth.getInstance().signOut();
    }

    private void initFirestore() {
        db = FirebaseFirestore.getInstance();
    }

    private void loadRecyclerViewData() {
        // Showing refresh animation before making http call
        refreshLayout.setRefreshing(true);

        // do the fetching data process
        getChallenges();

        // Stopping swipe refresh
        refreshLayout.setRefreshing(false);
    }

    public void getChallenges() {
        CollectionReference challengesRef = db.collection("Challenges");

        Query query = null;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (lastQueriedDocument != null) {
            query = challengesRef
                    .whereEqualTo("competitors." + uid + ".user_id", uid);
//                    .orderBy("time_ends", Query.Direction.ASCENDING)
//                    .startAfter(lastQueriedDocument);
        } else {
            query = challengesRef
                    .whereEqualTo("competitors." + uid + ".user_id", uid);
//                    .orderBy("time_ends", Query.Direction.ASCENDING);
        }

        // only when not using startAfter
        challengeList.clear();

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d("Firestore", document.getId() + " => " + document.getData());
                        Challenge challenge = document.toObject(Challenge.class);
                        challengeList.add(challenge);
//                        Log.d(TAG, "onComplete: got a new note. Position: " + (challengeList.size() - 1));
                    }

                    if (task.getResult().size() != 0) {
                        lastQueriedDocument = task.getResult().getDocuments()
                                .get(task.getResult().size() - 1);
                    }

                    recyclerAdapter.notifyDataSetChanged();
                } else {
                    Log.d(TAG, "readChallenge.onFailed: Query Failed. Check Logs.");
                }
            }
        });
    }

    public void createNewChallenge() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

        // create a new challenge in db
        DocumentReference newChallengeRef = db.collection("Challenges").document();

        // create a new challenge object
        // here its initialized mostly with random values
        Challenge challenge = ChallengeUtil.getRandom();
        challenge.setId(newChallengeRef.getId());

        int limit = 1; // runs to 1 time if limit = 1
        // Map<String, Object> competitors = new HashMap<>();
        for (int i = 0; i < limit; i++) {
            Map<String, Object> competitor = new HashMap<>();
            competitor.put("user_id", userId);
            competitor.put("user_name", userName);
            competitor.put("user_photo_url", "");
            competitor.put("active_score", ChallengeUtil.getRandomScore(500));
            competitor.put("total_score", ChallengeUtil.getRandomScore(2000));
            competitor.put("active_score_update_time", ChallengeUtil.getRandomTime());
            competitor.put("total_score_update_time", ChallengeUtil.getRandomTime());
            // competitors.put(userId, competitor);
            challenge.addCompetitor(userId, competitor);
        }
        // challenge.setCompetitors(competitors);

        newChallengeRef.set(challenge)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Created new challenge");
                            getChallenges();
                        } else {
                            Log.d(TAG, "Add new challenge failed. Check log.");
                        }
                    }
                });

    }

    public void initRecyclerView() {
        recyclerView = view.findViewById(R.id.challenge_recycler_view);
        recyclerView.setHasFixedSize(true); // increase performance, only use if we know the size

        // create the local challenge list object holding recycler view elements
        challengeList = new ArrayList<>();

        if (recyclerAdapter == null) {
            recyclerAdapter = new ChallengeAdapter(getActivity(), challengeList);
        }

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(recyclerAdapter);

        // onClick action
        recyclerAdapter.setOnItemClickListener(new ChallengeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                changeItem(position, "Clicked");
            }
        });
    }

    public void insertItem(int position, Challenge item) {
        challengeList.add(position, item);
        recyclerAdapter.notifyItemInserted(position);
    }

    public void removeItem(int position) {
        challengeList.remove(position);
        recyclerAdapter.notifyItemRemoved(position);
    }

    public void changeItem(int position, String text) {
        challengeList.get(position).setName("Clicked");
        recyclerAdapter.notifyItemChanged(position);
    }

    // todo: remove later
    public void createTestChallengeList() {
        challengeList.add(new Challenge(
                "april challenge",
                "steps",
                "CSGS",
                "Public",
                Calendar.getInstance().getTime(),
                Calendar.getInstance().getTime(),
                new HashMap<String, Object>()
        ));
        challengeList.add(new Challenge(
                "may challenge",
                "distance",
                "CSGS",
                "Public",
                Calendar.getInstance().getTime(),
                Calendar.getInstance().getTime(),
                new HashMap<String, Object>()
        ));
        challengeList.add(new Challenge(
                "june challenge",
                "calories",
                "CSGS",
                "Public",
                Calendar.getInstance().getTime(),
                Calendar.getInstance().getTime(),
                new HashMap<String, Object>()
        ));
    }

    // test read
    public void readTest() {
        db.collection("Challenges")
                .whereEqualTo("competitors.u03.Status", true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("Firestore", document.getId() + " => " + document.getData());
                            }
                        }
                    }
                });
    }

    public void updateTest() {
// search for public challenges created by "admin" to enroll the user in them

        db.collection("Challenges")
                .whereEqualTo("privacy", "public")
                .whereEqualTo("creator", "admin")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("CCC", document.getId() + " => " + document.getData());

                                Challenge challenge = document.toObject(Challenge.class);

                                FirebaseAuth auth = FirebaseAuth.getInstance();
                                FirebaseUser user = auth.getCurrentUser();

                                Log.d("CCC", "challenge object" + challenge.toString());

                                Map<String, Object> competitor = new HashMap<>();
                                competitor.put("user_id", user.getUid());
                                competitor.put("user_name", user.getDisplayName());
                                competitor.put("user_photo_url", "");

                                // read total score
                                Date d = challenge.getTime_starts();
                                Calendar c = Calendar.getInstance();
                                c.setTime(d);
                                int elapsedDays = TimeUtil.elapsedDays(c);

                                Intent i = new Intent(getActivity(), HealthService.class);
                                i.putExtra(Constants.HEALTH_LOG, true);
                                i.putExtra(Constants.HEALTH_DAY, elapsedDays);
                                // adding an extra flag to say we want from challenge_start_time until now
                                i.putExtra(Constants.HEALTH_NOW, true);
                                i.putExtra(Constants.HEALTH_TYPE, challenge.getType());
                                getActivity().startService(i);

                                competitor.put("total_score", score);
                                competitor.put("total_score_update_time", TimeUtil.todayMidnight());

                                Log.d("CCC", "onCompetitorCreated " + competitor.toString());

                                // competitors.put(userId, competitor);
                                challenge.addCompetitor(user.getUid(), competitor);

                                db.collection("Challenges")
                                        .document(challenge.getId())
                                        .set(challenge)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("CCC", "DocumentSnapshot successfully updated!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("CCC", "Error updating document", e);
                                            }
                                        });

                            }
                        }
                    }
                });

    }

    // callback when data is received from broadcast
    public BroadcastReceiver healthReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            int progress;
            if (intent.getAction().equals(Constants.HEALTH_ACTION)) {
                int resultCode = intent.getIntExtra(Constants.HEALTH_RESULT_CODE, Activity.RESULT_CANCELED);
                if (resultCode == Activity.RESULT_OK) {
                    score = intent.getIntExtra(Constants.HEALTH_RESULT_VALUE, 0);
                    Log.d("CCC", "Received Score: " + score);
                }
            }
        }
    };


}
