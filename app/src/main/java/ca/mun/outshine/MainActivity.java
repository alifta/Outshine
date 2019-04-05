package ca.mun.outshine;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

import ca.mun.outshine.service.SessionManager;
import ca.mun.outshine.service.WifiAlarmReceiver;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main";
    // Drawer
    private DrawerLayout drawerLayout;
    private NavigationView drawerView;
    private View drwaerHeader;
    private TextView drawerUserName;
    // Toolbar
    private Toolbar toolbar;
    // Toolbar titles respected to selected drawerLayout menu item
    private String[] toolbarTitles;
    // Floating Action Bottom
    // private FloatingActionButton fab;
    // Bottom Navigation Tabs
    private BottomNavigationView bottomNavigation;
    // TODO This may not be necessary
    private FrameLayout fragmentFrame;
    // Index to identify current menu item
    public static int drawerItemIndex = 0;
    public static int bottomNavigationItemIndex = 0;
    // Fragments
    // private MyDayFragment myDayFragment;
    // private ChallengesFragment challengesFragment;
    // private SettingsFragment settingsFragment;
    // Tags used to attach the fragments
    private static final String TAG_MY_DAY = "myday";
    private static final String TAG_CHALLENGES = "challenges";
    private static final String TAG_SETTINGS = "settings";
    private static final String TAG_PROFILE = "toolbar_menu_profile";
    private static final String TAG_GOALS = "goals";
    private static final String TAG_NOTIFICATIONS = "toolbar_notifications_menu";
    public static String CURRENT_TAG = TAG_MY_DAY;
    // Handler of loading huge fragments on a different thread
    private Handler mHandler;
    // Flag to load home fragment when user presses back key
    private boolean shouldLoadHomeFragOnBackPress = true;

    // WIFI
    private AlarmManager wifiAlarmManager;
    private PendingIntent wifiAlarmIntent;

    // Permissions
    private PermissionChecker permissionChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check if not logged in -> login / otherwise -> continue
        if (!SessionManager.getInstance(this).isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        // Firebase user
        // FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Layout
        setContentView(R.layout.activity_main);

        // Huge fragment handler
        mHandler = new Handler();
        // Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        // Drawer view
        drawerView = findViewById(R.id.drawer_view);
        // Drawer header
        drwaerHeader = drawerView.getHeaderView(0);
        drawerUserName = drwaerHeader.findViewById(R.id.drawer_user_name);
        // Load drawerView header data
        loadDrawerHeader();
        // Initializing drawerView menu
        setUpDrawerView();
        /*
        // FAB
        fab = findViewById(R.id.fab);
        // FAB action
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */
        // Initializing frame
        fragmentFrame = findViewById(R.id.fragment_container);
        // Defining fragments
        // myDayFragment = new MyDayFragment();
        // challengesFragment = new ChallengesFragment();
        // settingsFragment = new SettingsFragment();
        // Bottom navigation (Tabs)
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationListener);
        // Always begin with My-Day fragment
        // setFragment(myDayFragment);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new MyDayFragment()).commit();
        // Load screen titles from string resources
        toolbarTitles = getResources().getStringArray(R.array.nav_item_activity_titles);
        if (savedInstanceState == null) {
            drawerItemIndex = 0;
            CURRENT_TAG = TAG_MY_DAY;
            loadFragment();
        }

        // Checking WiFi permission, using custom defined class
         permissionChecker = new PermissionChecker(this);
         permissionChecker.checkWifi();
         permissionChecker.checkFitness();

        scheduleWifiAlarm(1);
    }

    // Check user login status on the start
    @Override
    public void onStart() {
        super.onStart();
        // If not logged in, go to login
        if (!SessionManager.getInstance(this).isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        // When user press BACK
        // If drawerLayout is open, just close it and stay in same fragment
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
            return;
        }
        // If drawerLayout is closer, loads MAIN fragment, if fragment is anything than MAIN
        if (shouldLoadHomeFragOnBackPress) {
            // checking if user is on other navigation menu rather than MAIN screen
            if (drawerItemIndex != 0) {
                drawerItemIndex = 0;
                CURRENT_TAG = TAG_MY_DAY;
                loadFragment();
                return;
            }
        }
        super.onBackPressed();
    }

    // Inflate the menu; this adds items to the action bar if it is present.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Show menu only when Profile fragment is selected
        if (drawerItemIndex == 1) {
            getMenuInflater().inflate(R.menu.toolbar_menu_profile, menu);
        }
        // When fragment is Notifications menu
        if (drawerItemIndex == 3) {
            getMenuInflater().inflate(R.menu.toolbar_notifications_menu, menu);
        }
        return true;
    }

    // Handle action bar item clicks here.
    // The action bar will automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // Noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            SessionManager.getInstance(this).logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            FirebaseAuth.getInstance().signOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Load drawer header
    public void loadDrawerHeader() {
        // Name
        drawerUserName.setText(SessionManager.getInstance(this).getUserEmail());
        // Later can add loading profile image with Glide API and etc.
        // Showing dot next to notifications label
        // drawerView.getMenu().getItem(3).setActionView(R.layout.drawer_dot);
    }

    // Configure drawer menu
    public void setUpDrawerView() {
        // Setting Navigation View Item Selected Listener to handle item click of the navigation
        drawerView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    // This method will trigger on item Click of navigation menu
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // Check to see which item was being clicked and perform appropriate action
                        switch (menuItem.getItemId()) {
                            // Replacing the main content with ContentFragment Which is our Inbox View;
                            case R.id.nav_my_day:
                                drawerItemIndex = 0;
                                CURRENT_TAG = TAG_MY_DAY;
                                break;
                            case R.id.nav_profile:
                                drawerItemIndex = 1;
                                CURRENT_TAG = TAG_PROFILE;
                                break;
                            case R.id.nav_goals:
                                drawerItemIndex = 2;
                                CURRENT_TAG = TAG_GOALS;
                                break;
                            case R.id.nav_notifications:
                                drawerItemIndex = 3;
                                CURRENT_TAG = TAG_NOTIFICATIONS;
                                break;
                            case R.id.nav_about:
                                // Launch new intent instead of loading fragment
                                startActivity(new Intent(MainActivity.this,
                                        AboutActivity.class));
                                drawerLayout.closeDrawers();
                                return true;
                            case R.id.nav_policy:
                                startActivity(new Intent(MainActivity.this,
                                        PolicyActivity.class));
                                drawerLayout.closeDrawers();
                                return true;
                            default:
                                drawerItemIndex = 0;
                        }
                        //Checking if the item is in checked state or not, if not make it in checked state
                        if (menuItem.isChecked()) {
                            menuItem.setChecked(false);
                        } else {
                            menuItem.setChecked(true);
                        }
                        menuItem.setChecked(true);
                        // Load fragment based on selected drawer menu item
                        loadFragment();
                        return true;
                    }
                });

        // Set desired action to happen during opening and closing the drawer
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer opens
                super.onDrawerOpened(drawerView);
            }
        };

        // Setting the actionbarToggle to drawer layout
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        // Calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    // Returns selected fragment by user
    private void loadFragment() {
        // Selecting appropriate drawer menu item
        selectDrawerMenu();
        // Setting screen title
        setToolbarTitle();
        // If user select the current navigation menu again, don't do anything
        // Just close the navigation drawer
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            drawerLayout.closeDrawers();
            return;
        }
        // Sometimes, when fragment has huge data, screen seems hanging when switching between menus
        // So using runnable, the fragment is loaded with cross fade effect
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // Update the main content by replacing fragments
                Fragment fragment = getFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.fragment_container, fragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };
        // If mPendingRunnable is not null, then add to the message queue
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }
        // Closing drawer on item click
        drawerLayout.closeDrawers();
        // refresh screen menu
        invalidateOptionsMenu();
    }

    private void selectDrawerMenu() {
        drawerView.getMenu().getItem(drawerItemIndex).setChecked(true);
    }

    private void setToolbarTitle() {
        getSupportActionBar().setTitle(toolbarTitles[drawerItemIndex]);
    }

    private Fragment getFragment() {
        switch (drawerItemIndex) {
            case 0:
                // MyDay fragment
                MyDayFragment myDayFragment = new MyDayFragment();
                return myDayFragment;
            case 1:
                // Profile fragment
                ProfileFragment profileFragment = new ProfileFragment();
                return profileFragment;
            case 2:
                // Goals fragment
                GoalsFragment goalsFragment = new GoalsFragment();
                return goalsFragment;
            case 3:
                // Notifications fragment
                NotificationsFragment notificationsFragment = new NotificationsFragment();
                return notificationsFragment;
            default:
                return new MyDayFragment();
        }
    }

    // Bottom navigation listener
    private BottomNavigationView.OnNavigationItemSelectedListener navigationListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    switch (item.getItemId()) {
                        case R.id.bottom_navigation_my_day:
                            selectedFragment = new MyDayFragment();
                            break;
                        case R.id.bottom_navigation_challenges:
                            selectedFragment = new ChallengesFragment();
                            break;
                        case R.id.bottom_navigation_settings:
                            selectedFragment = new SettingsFragment();
                            break;
                        default:
                            selectedFragment = new MyDayFragment();
                    }
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment).commit();
                    return true;
                }
            };

//    public void setupWifi() {
//        // background wifi functions
//        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        // if wifi disabled -> ask user to enable it
//        if (!wifiManager.isWifiEnabled()) {
//            Toast.makeText(this, "WiFi is disabled.", Toast.LENGTH_SHORT).show();
//            wifiManager.setWifiEnabled(true);
//        }
//        // register wifi broadcast receiver and scan for the first time
//        registerWifiReceiver();
//        wifiManager.startScan();
//    }
//
//    private void registerWifiReceiver() {
//        this.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
//    }
//
//    private void unregisterWifiReceiver() {
//        this.unregisterReceiver(wifiReceiver);
//    }
//
//    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            List<ScanResult> resultList = wifiManager.getScanResults();
//            // unregisterWifiReceiver();
//            StringBuilder sb = new StringBuilder();
//            for (ScanResult scanResult: resultList) {
//                sb.append(scanResult.SSID);
//                sb.append(" - ");
//                sb.append(scanResult.capabilities);
//                sb.append("\\n");
//            }
//            Log.d("WIFI", sb.toString());
//        }
//    };

    // Activate wifi scanning in the background
    public void scheduleWifiAlarm(int interval) {
        Intent intent = new Intent(getApplicationContext(), WifiAlarmReceiver.class);
        wifiAlarmIntent = PendingIntent.getBroadcast(this, WifiAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        wifiAlarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        // To set the alarm right away
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        // Change "calendar" for customized staring time
        // calendar.set(Calendar.MINUTE, 5);

        // Different interval
        // 1 min = 1000 * 60 * 1, 15 min = AlarmManager.INTERVAL_FIFTEEN_MINUTES
        wifiAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                1000 * 60 * 1, wifiAlarmIntent);
    }

    public void cancelWifiAlarm() {
        Intent intent = new Intent(getApplicationContext(), WifiAlarmReceiver.class);
        wifiAlarmIntent = PendingIntent.getBroadcast(this, WifiAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        wifiAlarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        wifiAlarmManager.cancel(wifiAlarmIntent);
    }

}
