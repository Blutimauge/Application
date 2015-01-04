package it.unozerouno.givemetime.view.intro;

import it.unozerouno.givemetime.R;
import it.unozerouno.givemetime.view.intro.fragments.CalendarPickerFragment;
import it.unozerouno.givemetime.view.intro.fragments.WelcomeAndDisclaimer;
import it.unozerouno.givemetime.view.intro.fragments.LastTutorialPage;
import it.unozerouno.givemetime.view.main.MainActivity;
import it.unozerouno.givemetime.view.utilities.LoginPreferences;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

public class ScreenSlidePagerActivity extends FragmentActivity{

    // number of pages to show
    private static final int NUM_PAGES = 3;

    // pager widget
    private ViewPager mPager;

    //pager adapter
    private PagerAdapter mPagerAdapter;

    public PagerAdapter getmPagerAdapter() {
        return mPagerAdapter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // if it's the first time that the user launches the app, continue. Else, launch main activity
        if (LoginPreferences.isFirst(this)){
        	if (LoginPreferences.isDeviceOnline(this)){
	            // set the layout
	            setContentView(R.layout.activity_screen_slide);
	
	            // instantiate the ViewPager and the adapter
	            mPager = (ViewPager) findViewById(R.id.pager);
	            mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
	            mPager.setAdapter(mPagerAdapter);
	            // remove comment if you want to use the zoomOutPageTransformer animation
	            // mPager.setPageTransformer(true, new ZoomOutPageTransformer());
            } else {
            	AlertDialog.Builder alert = new AlertDialog.Builder(ScreenSlidePagerActivity.this);
            	alert.setTitle("No Internet Connection");
            	alert.setMessage("Turn on your internet connection in order to let GiveMeTime the opportunity to fetch your calendars.")
            		 .setCancelable(false)
            		 .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
            	alert.show();
            }
        } else {
            // launch main activity
            Intent intent = new Intent();
            intent.setClass(this, MainActivity.class);
            startActivity(intent);
            // close this activity in order to avoid to return to it pressing the back button in MainActivity
            this.finish();
        }
    }

    @Override
    public void onBackPressed() {
        if(mPager.getCurrentItem() == 0){
            super.onBackPressed();
        } else {
           mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }
   
    
    
    // adapter as an internal class
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter{

        public ScreenSlidePagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {

            // choose the right fragment to display
            switch (i){
                case 0: return new WelcomeAndDisclaimer();
                case 1: return new CalendarPickerFragment();
                case 2: return new LastTutorialPage();
                default: return null;
            }


        }
        
        

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
