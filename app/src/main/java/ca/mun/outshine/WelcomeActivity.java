package ca.mun.outshine;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;

import ca.mun.outshine.service.SessionManager;

public class WelcomeActivity extends AppCompatActivity {

    private int[] layouts;
    private TextView[] dots;
    private Button btnBack, btnNext;
    private ViewPager viewPager;
    private LinearLayout dotsLayout;
    private MyViewPagerAdapter myViewPagerAdapter;
    // Permissions
    private PermissionChecker permissionChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check if it is not first launch -> and logged in -> main / otherwise -> login
        if (!SessionManager.getInstance(this).isFirstTime()) {
            if (SessionManager.getInstance(this).isLoggedIn()) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        }
        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        // Now we set content of the activity
        setContentView(R.layout.activity_welcome);
        // Views
        viewPager = findViewById(R.id.slider_view_pager);
        dotsLayout = findViewById(R.id.slider_dots);
        btnBack = findViewById(R.id.slider_btn_back);
        btnNext = findViewById(R.id.slider_btn_next);
        // Layouts of all welcome sliders
        layouts = new int[]{
                R.layout.slide1,
                R.layout.slide2,
                R.layout.slide3,
                R.layout.slide4};
        // Adding bottom dots
        addBottomDots(0);
        // Making notification bar transparent
        changeStatusBarColor();
        // Setting view adaptor
        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
        // Setting button listener
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Checking for last page
                // If last page LOGIN screen will be launched
                int prev = getItem(-1);
                int current = getItem(+1);
                if (current < layouts.length) {
                    // Move to previous screen
                    viewPager.setCurrentItem(prev);
                } else {
                    launchLogin();
                }
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Checking for last page
                // If last page REGISTER screen will be launched
                int current = getItem(+1);
                if (current < layouts.length) {
                    // Move to next screen
                    viewPager.setCurrentItem(current);
                } else {
                    launchRegister();
                }
            }
        });

        // Checking WiFi and Fitness permission, using custom defined class
        permissionChecker = new PermissionChecker(this);
        permissionChecker.checkWifi();
        permissionChecker.checkFitness();
    }

    private void launchLogin() {
        SessionManager.getInstance(this).setFirstTime(false);
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void launchRegister() {
        SessionManager.getInstance(this).setFirstTime(false);
        startActivity(new Intent(this, RegisterActivity.class));
        finish();
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];
        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);
        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }
        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }

    // Making notification bar transparent
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            // getSupportActionBar().hide(); //  Not needed if theme is noActionBar
        }
    }

    // Viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);
            // Changing the next button text 'NEXT' ->'REGISTER'
            // Changing the next button text 'PREV' ->'LOGIN'
            if (position == 0) {
                btnBack.setVisibility(View.GONE);
            } else if (position == layouts.length - 1) {
                // Last page. make button text to GOT IT
                btnBack.setText(getString(R.string.login));
                btnNext.setText(getString(R.string.register));
            } else {
                // Still pages are left, everything stays same
                btnBack.setText(getString(R.string.back));
                btnNext.setText(getString(R.string.next));
                btnBack.setVisibility(View.VISIBLE);
                btnNext.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            // Do nothing
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
            // Do nothing
        }
    };

    // View pager adapter
    public class MyViewPagerAdapter extends PagerAdapter {

        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
            // Do nothing
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}
