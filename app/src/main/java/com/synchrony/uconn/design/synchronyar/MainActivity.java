/*===============================================================================
Copyright (c) 2016-2017 PTC Inc. All Rights Reserved.


Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other
countries.
===============================================================================*/

package com.synchrony.uconn.design.synchronyar;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.vuforia.CameraDevice;
import com.vuforia.DataSet;
import com.vuforia.ObjectTracker;
import com.vuforia.STORAGE_TYPE;
import com.vuforia.State;
import com.vuforia.Trackable;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vuforia;
import com.vuforia.samples.SampleApplication.SampleApplicationControl;
import com.vuforia.samples.SampleApplication.SampleApplicationException;
import com.vuforia.samples.SampleApplication.SampleApplicationSession;
import com.vuforia.samples.SampleApplication.utils.LoadingDialogHandler;
import com.vuforia.samples.SampleApplication.utils.SampleApplicationGLView;
import com.vuforia.samples.SampleApplication.utils.Texture;

import java.util.Locale;
import java.util.Vector;


public class MainActivity extends AppCompatActivity implements SampleApplicationControl
{
    private Context context = MainActivity.this;

    private static final String LOGTAG = "MainActivity";

    SampleApplicationSession vuforiaAppSession;

    private DataSet mCurrentDataset;

    // Our OpenGL view:
    private SampleApplicationGLView mGlView;

    // Our renderer:
    private SynchronyRenderer mRenderer;

    // The textures we will use for rendering:
    private Vector<Texture> mTextures;

    private GestureDetector mGestureDetector;

    //private View mFlashOptionView;

    private RelativeLayout mUILayout;


    LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);

    // Alert Dialog used to display SDK errors
    private AlertDialog mErrorDialog;

    boolean mIsDroidDevice = false;

    CoordinatorLayout infoOverlay = null;

    Product currentProduct = null;

    Cart cart = new Cart();

    int PERMISSION_REQUEST_START_VUFORIA = 0;

    // Called when the activity first starts or the user navigates back to an
    // activity.
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(LOGTAG, "onCreate");
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            requestNecessaryPermissions();
        }
        else {
            vuforiaAppSession = new SampleApplicationSession(this);

            startLoadingAnimation();

            vuforiaAppSession
                    .initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            // Load any sample specific textures:
            mTextures = new Vector<>();
            loadTextures();

            mGestureDetector = new GestureDetector(this, new GestureListener());

            mIsDroidDevice = Build.MODEL.toLowerCase().startsWith(
                    "droid");
        }
    }

    private void requestNecessaryPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE},
            PERMISSION_REQUEST_START_VUFORIA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_START_VUFORIA) {
            boolean allGranted = false;
            if (grantResults.length > 0) {
                allGranted = true;
                for (int e : grantResults) {
                    if (e != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }
            }
            if (allGranted) {
                vuforiaAppSession = new SampleApplicationSession(this);

                startLoadingAnimation();

                vuforiaAppSession
                        .initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                // Load any sample specific textures:
                mTextures = new Vector<>();
                loadTextures();

                mGestureDetector = new GestureDetector(this, new GestureListener());

                mIsDroidDevice = Build.MODEL.toLowerCase().startsWith(
                        "droid");
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(R.string.permission_request_denied_message)
                        .setTitle(R.string.permission_request_denied_title);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }

    // We want to load specific textures from the APK, which we will later use
    // for rendering.
    private void loadTextures()
    {
        mTextures.add(Texture.loadTextureFromApk(
                "CubeWireframe.png", getAssets()));
    }


    // Process Single Tap event to trigger autofocus
    private class GestureListener extends
            GestureDetector.SimpleOnGestureListener
    {
        // Used to set autofocus one second after a manual focus is triggered
        private final Handler autofocusHandler = new Handler();


        @Override
        public boolean onDown(MotionEvent e)
        {
            return true;
        }


        @Override
        public boolean onSingleTapUp(MotionEvent e)
        {
            boolean result = CameraDevice.getInstance().setFocusMode(
                    CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);
            if (!result)
                Log.e("SingleTapUp", "Unable to trigger focus");

            // Generates a Handler to trigger continuous auto-focus
            // after 1 second
            autofocusHandler.postDelayed(new Runnable()
            {
                public void run()
                {
                    final boolean autofocusResult = CameraDevice.getInstance().setFocusMode(
                            CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);

                    if (!autofocusResult)
                        Log.e("SingleTapUp", "Unable to re-enable continuous auto-focus");
                }
            }, 1000L);

            return true;
        }
    }


    // Called when the activity will start interacting with the user.
    @Override
    protected void onResume()
    {
        Log.d(LOGTAG, "onResume");
        super.onResume();

        showProgressIndicator(true);

        // This is needed for some Droid devices to force portrait
        if (mIsDroidDevice)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        if (vuforiaAppSession != null) {
            vuforiaAppSession.onResume();
        }
    }


    // Callback for configuration changes the activity handles itself
    @Override
    public void onConfigurationChanged(Configuration config)
    {
        Log.d(LOGTAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);

        vuforiaAppSession.onConfigurationChanged();
    }


    // Called when the system is about to start resuming a previous activity.
    @Override
    protected void onPause()
    {
        Log.d(LOGTAG, "onPause");
        super.onPause();
        if (mGlView != null)
        {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }

        // Turn off the flash
        /*boolean flash = false;
        /*boolean flash = false;
        if (mFlashOptionView != null && flash)
        {
            // OnCheckedChangeListener is called upon changing the checked state
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            {
                ((Switch) mFlashOptionView).setChecked(false);
            } else
            {
                ((CheckBox) mFlashOptionView).setChecked(false);
            }
        }*/

        if (vuforiaAppSession != null) {
            try {
                vuforiaAppSession.pauseAR();
            } catch (SampleApplicationException e) {
                Log.e(LOGTAG, e.getString());
            }
        }
    }


    // The final call you receive before your activity is destroyed.
    @Override
    protected void onDestroy()
    {
        Log.d(LOGTAG, "onDestroy");
        super.onDestroy();

        try
        {
            vuforiaAppSession.stopAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }

        // Unload texture:
        mTextures.clear();
        mTextures = null;

        System.gc();
    }


    // Initializes AR application components.
    private void initApplicationAR()
    {
        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();

        mGlView = new SampleApplicationGLView(this);
        mGlView.init(translucent, depthSize, stencilSize);

        mRenderer = new SynchronyRenderer(this, vuforiaAppSession);
        mRenderer.setTextures(mTextures);
        mGlView.setRenderer(mRenderer);

    }


    private void startLoadingAnimation()
    {
        LayoutInflater inflater = LayoutInflater.from(this);
        mUILayout = (RelativeLayout) inflater.inflate(R.layout.camera_overlay,
                null, false);

        mUILayout.setVisibility(View.VISIBLE);
        mUILayout.setBackgroundColor(Color.BLACK);

        // Gets a reference to the loading dialog
        loadingDialogHandler.mLoadingDialogContainer = mUILayout
                .findViewById(R.id.loading_indicator);

        // Shows the loading indicator at start
        loadingDialogHandler
                .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);


        // Adds the inflated layout to the view
        addContentView(mUILayout, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));

    }


    // Methods to load and destroy tracking data.
    @Override
    public boolean doLoadTrackersData()
    {
        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) tManager
                .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
            return false;

        if (mCurrentDataset == null)
            mCurrentDataset = objectTracker.createDataSet();

        if (mCurrentDataset == null)
            return false;

        if (!mCurrentDataset.load("synchrony_test_OT.xml",
                STORAGE_TYPE.STORAGE_APPRESOURCE))
            return false;

        if (!objectTracker.activateDataSet(mCurrentDataset))
            return false;

        int numTrackables = mCurrentDataset.getNumTrackables();
        for (int count = 0; count < numTrackables; count++)
        {
            Trackable trackable = mCurrentDataset.getTrackable(count);
            if(isExtendedTrackingActive())
            {
                trackable.startExtendedTracking();
            }

            String name = trackable.getName();
            trackable.setUserData(name);
            Log.d(LOGTAG, "UserData:Set the following user data "
                    + trackable.getUserData());
        }

        return true;
    }


    @Override
    public boolean doUnloadTrackersData()
    {
        // Indicate if the trackers were unloaded correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) tManager
                .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
            return false;

        if (mCurrentDataset != null && mCurrentDataset.isActive())
        {
            if (objectTracker.getActiveDataSet(0).equals(mCurrentDataset)
                    && !objectTracker.deactivateDataSet(mCurrentDataset))
            {
                result = false;
            } else if (!objectTracker.destroyDataSet(mCurrentDataset))
            {
                result = false;
            }

            mCurrentDataset = null;
        }

        return result;
    }

    @Override
    public void onVuforiaResumed()
    {
        if (mGlView != null)
        {
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }
    }

    @Override
    public void onInitARDone(SampleApplicationException exception)
    {

        if (exception == null)
        {
            initApplicationAR();

            mRenderer.setActive(true);

            // Now add the GL surface view. It is important
            // that the OpenGL ES surface view gets added
            // BEFORE the camera is started and video
            // background is configured.
            addContentView(mGlView, new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));

            // Sets the UILayout to be drawn in front of the camera
            mUILayout.bringToFront();

            // Sets the layout background to transparent
            mUILayout.setBackgroundColor(Color.TRANSPARENT);

            vuforiaAppSession.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT);

        } else
        {
            Log.e(LOGTAG, exception.getString());
            if(exception.getCode() == SampleApplicationException.LOADING_TRACKERS_FAILURE)
            {
                showInitializationErrorMessage(
                        getString(R.string.INIT_OBJECT_DATASET_NOT_FOUND_TITLE),
                        getString(R.string.INIT_OBJECT_DATASET_NOT_FOUND));

            }
            else
            {
                showInitializationErrorMessage( getString(R.string.INIT_ERROR),
                        exception.getString() );
            }
        }
    }

    @Override
    public void onVuforiaStarted()
    {
        // Set camera focus mode
        if(!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO))
        {
            // If continuous autofocus mode fails, attempt to set to a different mode
            if(!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO))
            {
                CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_NORMAL);
            }
        }
        showProgressIndicator(false);
    }

    public void showProgressIndicator(boolean show)
    {
        if (loadingDialogHandler != null)
        {
            if (show)
            {
                loadingDialogHandler
                        .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);
            }
            else
            {
                loadingDialogHandler
                        .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
            }
        }
    }

    // Shows initialization error messages as System dialogs
    public void showInitializationErrorMessage(String title, String message)
    {
        final String errorMessage = message;
        final String messageTitle = title;
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                if (mErrorDialog != null)
                {
                    mErrorDialog.dismiss();
                }

                // Generates an Alert Dialog to show the error message
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        MainActivity.this);
                builder
                        .setMessage(errorMessage)
                        .setTitle(messageTitle)
                        .setCancelable(false)
                        .setIcon(0)
                        .setPositiveButton(getString(R.string.button_OK),
                                new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        finish();
                                    }
                                });

                mErrorDialog = builder.create();
                mErrorDialog.show();
            }
        });
    }


    @Override
    public void onVuforiaUpdate(State state)
    {
    }


    @Override
    public boolean doInitTrackers()
    {
        // Indicate if the trackers were initialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker;

        // Trying to initialize the image tracker
        tracker = tManager.initTracker(ObjectTracker.getClassType());
        if (tracker == null)
        {
            Log.e(
                    LOGTAG,
                    "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else
        {
            Log.i(LOGTAG, "Tracker successfully initialized");
        }
        return result;
    }


    @Override
    public boolean doStartTrackers()
    {
        // Indicate if the trackers were started correctly
        Tracker objectTracker = TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.start();

        return true;
    }


    @Override
    public boolean doStopTrackers()
    {
        // Indicate if the trackers were stopped correctly
        Tracker objectTracker = TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.stop();

        return true;
    }


    @Override
    public boolean doDeinitTrackers()
    {
        // Indicate if the trackers were deinitialized correctly
        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(ObjectTracker.getClassType());

        return true;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        // Process the Gestures
        return mGestureDetector.onTouchEvent(event);
    }


    boolean isExtendedTrackingActive()
    {
        return false;
    }

    public void setCurrentProduct(Product currentProduct) {
        this.currentProduct = currentProduct;
    }

    public void displayInfoOverlay() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (infoOverlay == null) {
                    LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                    infoOverlay = (CoordinatorLayout) inflater.inflate(R.layout.info_overlay, mUILayout, false);
                }
                TextView infoOverlayPrice = (TextView) infoOverlay.findViewById(R.id.info_overlay_price);
                infoOverlayPrice.setText(String.format(Locale.US, "$%.2f", currentProduct.getPrice()));
                TextView infoOverlayAvailability = (TextView) infoOverlay.findViewById(R.id.info_overlay_availability);
                if (currentProduct.inStock()) {
                    infoOverlayAvailability.setText(getResources().getText(R.string.info_overlay_availability_yes));
                    infoOverlayAvailability.setTextColor(getResources().getColor(R.color.overlay_text_available));
                } else {
                    infoOverlayAvailability.setText(getResources().getText(R.string.info_overlay_availability_no));
                    infoOverlayAvailability.setTextColor(getResources().getColor(R.color.overlay_text_unavailable));
                }

                ImageButton addToCartButton = infoOverlay.findViewById(R.id.info_overlay_cart_button);
                addToCartButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Snackbar addedToCartMessage;
                        if (cart.addToCart(currentProduct)) {
                            addedToCartMessage = Snackbar.make(infoOverlay, R.string.added_to_cart, Snackbar.LENGTH_LONG);
                        } else {
                            addedToCartMessage = Snackbar.make(infoOverlay, R.string.not_added_to_cart, Snackbar.LENGTH_LONG);
                        }
                        addedToCartMessage.setAction(R.string.checkout, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showCheckoutActivity();
                            }
                        });
                        addedToCartMessage.show();
                    }
                });

                if (mUILayout.findViewById(R.id.info_overlay_coordinator) == null) {
                    mUILayout.addView(infoOverlay, new LayoutParams(LayoutParams.MATCH_PARENT,
                            LayoutParams.MATCH_PARENT));
                }
            }
        });
    }

    public void displayInfoOverlay(final String itemName, final double price, final boolean inStock)
    {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (infoOverlay == null) {
                    LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                    infoOverlay = (CoordinatorLayout) inflater.inflate(R.layout.info_overlay, mUILayout, false);
                }
                TextView infoOverlayPrice = (TextView) infoOverlay.findViewById(R.id.info_overlay_price);
                infoOverlayPrice.setText(String.format(Locale.US, "$%.2f", price));
                TextView infoOverlayAvailability = (TextView) infoOverlay.findViewById(R.id.info_overlay_availability);
                if (inStock) {
                    infoOverlayAvailability.setText(getResources().getText(R.string.info_overlay_availability_yes));
                    infoOverlayAvailability.setTextColor(getResources().getColor(R.color.overlay_text_available));
                } else {
                    infoOverlayAvailability.setText(getResources().getText(R.string.info_overlay_availability_no));
                    infoOverlayAvailability.setTextColor(getResources().getColor(R.color.overlay_text_unavailable));
                }

                if (mUILayout.findViewById(R.id.info_overlay_coordinator) == null) {
                    mUILayout.addView(infoOverlay, new LayoutParams(LayoutParams.MATCH_PARENT,
                            LayoutParams.MATCH_PARENT));
                }
            }
        });
    }

    public void removeInfoOverlay()
    {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (mUILayout.findViewById(R.id.info_overlay_coordinator) != null) {
                    mUILayout.removeView(infoOverlay);
                }
            }
        });
    }

    public void showInfoActivity(View v)
    {
        Intent infoActivityIntent = new Intent(MainActivity.this, UserInterface.class);
        infoActivityIntent.putExtra("currentProduct", currentProduct);
        startActivity(infoActivityIntent);
    }

    public void showCheckoutActivity()
    {
        Intent infoActivityIntent = new Intent(MainActivity.this, CheckoutActivity.class);
        infoActivityIntent.putExtra("cart", cart);
        startActivity(infoActivityIntent);
    }
}
