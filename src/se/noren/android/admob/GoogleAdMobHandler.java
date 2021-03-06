package se.noren.android.admob;

import android.app.Activity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class GoogleAdMobHandler {

	public static AdView addAdsToLinearLayout(Activity activity, int index, int linearLayoutResource) {
	    // Create the adView
        String MY_AD_UNIT_ID = "a14ee4c32acdfe1";
        AdView adView = new AdView(activity, AdSize.BANNER, MY_AD_UNIT_ID);

        // Lookup your LinearLayout assuming it�s been given
        // the attribute android:id="@+id/mainLayout"
        LinearLayout layout = (LinearLayout) activity.findViewById(linearLayoutResource);

        // Add the adView to it
        layout.addView(adView, index, new ViewGroup.LayoutParams(480, 75));

        // Initiate a generic request to load it with an ad
        AdRequest adRequest = new AdRequest();
        adRequest.addTestDevice(AdRequest.TEST_EMULATOR);               // Emulator
//        adRequest.addTestDevice("TEST_DEVICE_ID");                      // Test Android Device
        adView.loadAd(adRequest);
        
        return adView;
	}
}
