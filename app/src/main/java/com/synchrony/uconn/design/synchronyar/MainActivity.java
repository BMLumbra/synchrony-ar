/*===============================================================================
Copyright (c) 2016-2017 PTC Inc. All Rights Reserved.


Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other
countries.
===============================================================================*/

package com.synchrony.uconn.design.synchronyar;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

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

import java.util.Scanner;
import java.io.*;


public class MainActivity extends AppCompatActivity implements SampleApplicationControl {
    private Context context = MainActivity.this;

    private SampleApplicationSession vuforiaAppSession;

    private DataSet mCurrentDataSet;

    // Our OpenGL view:
    private SampleApplicationGLView mGlView;

    // Our renderer:
    private SynchronyRenderer mRenderer;

    private GestureDetector mGestureDetector;

    private RelativeLayout mUILayout;

    LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);

    // Alert Dialog used to display SDK errors
    private AlertDialog mErrorDialog;

    private Catalogue catalogue = new Catalogue(this);

    //Scanner for creating products
    private Scanner sc;

    private Product currentProduct = null;

    private Cart cart = new Cart();

    InfoOverlay infoOverlay = null;

    public static final int RESULT_CART_INFO = 0;
    private static final int PERMISSION_REQUEST_START_VUFORIA = 0;
    private static final String LOG_TAG = "MainActivity";

    // Called when the activity first starts or the user navigates back to an
    // activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);

        loadProductTxt();
        requestNecessaryPermissions();
    }

    private void loadProductTxt() {
        try {
            InputStream input = getAssets().open("products.txt");
            sc = new Scanner(input);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Unable to load products.txt file");
        }
    }

    private void requestNecessaryPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE },
                PERMISSION_REQUEST_START_VUFORIA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_START_VUFORIA) {
            boolean allGranted = (grantResults.length > 0)
                                 && allArrayValuesMatch(grantResults, PackageManager.PERMISSION_GRANTED);

            if (allGranted) {
                initializeVuforia();
                infoOverlay = new InfoOverlay(this, mUILayout, cart, currentProduct);
            } else {
                showPermissionsDeniedDialog();
            }
        }
    }

    private boolean allArrayValuesMatch(int[] array, int value) {
        boolean allMatch = true;
        for (int e : array) {
            if (e != value) {
                allMatch = false;
                break;
            }
        }

        return allMatch;
    }

    private void initializeVuforia() {
        vuforiaAppSession = new SampleApplicationSession(this);

        startLoadingAnimation();

        vuforiaAppSession
                .initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mGestureDetector = new GestureDetector(this, new GestureListener());
    }

    private void showPermissionsDeniedDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(context)
                        .setMessage(R.string.permission_request_denied_message)
                        .setTitle(R.string.permission_request_denied_title)
                        .create()
                        .show();
            }
        });
    }

    // Process Single Tap event to trigger auto-focus
    private class GestureListener extends
            GestureDetector.SimpleOnGestureListener {
        // Used to set auto-focus one second after a manual focus is triggered
        private final Handler autoFocusHandler = new Handler();

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            boolean result = CameraDevice.getInstance().setFocusMode(
                    CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);
            if (!result) {
                Log.e("SingleTapUp", "Unable to trigger focus");
            }

            // Generates a Handler to trigger continuous auto-focus
            // after 1 second
            autoFocusHandler.postDelayed(new Runnable() {
                public void run() {
                    final boolean autoFocusResult = CameraDevice.getInstance().setFocusMode(
                            CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);

                    if (!autoFocusResult) {
                        Log.e("SingleTapUp", "Unable to re-enable continuous auto-focus");
                    }
                }
            }, 1000L);

            return true;
        }
    }

    // Called when the activity will start interacting with the user.
    @Override
    protected void onResume() {
        Log.d(LOG_TAG, "onResume");
        super.onResume();

        showProgressIndicator(true);

        if (vuforiaAppSession != null) {
            vuforiaAppSession.onResume();
        }
    }

    // Callback for configuration changes the activity handles itself
    @Override
    public void onConfigurationChanged(Configuration config) {
        Log.d(LOG_TAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);

        vuforiaAppSession.onConfigurationChanged();
    }

    // Called when the system is about to start resuming a previous activity.
    @Override
    protected void onPause() {
        Log.d(LOG_TAG, "onPause");
        super.onPause();
        if (mGlView != null) {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }

        if (vuforiaAppSession != null) {
            try {
                vuforiaAppSession.pauseAR();
            } catch (SampleApplicationException e) {
                Log.e(LOG_TAG, e.getString());
            }
        }
    }

    // The final call you receive before your activity is destroyed.
    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        super.onDestroy();

        try {
            vuforiaAppSession.stopAR();
        } catch (SampleApplicationException e) {
            Log.e(LOG_TAG, e.getString());
        }

        System.gc();
    }

    // Initializes AR application components.
    private void initApplicationAR() {
        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();

        mGlView = new SampleApplicationGLView(this);
        mGlView.init(translucent, depthSize, stencilSize);

        mRenderer = new SynchronyRenderer(this, vuforiaAppSession, catalogue);
        mGlView.setRenderer(mRenderer);
    }

    private void startLoadingAnimation() {
        LayoutInflater inflater = LayoutInflater.from(this);
        ViewGroup view = findViewById(android.R.id.content);
        mUILayout = (RelativeLayout) inflater.inflate(R.layout.camera_overlay,
                view, false);

        mUILayout.setVisibility(View.VISIBLE);
        mUILayout.setBackgroundColor(Color.BLACK);

        // Gets a reference to the loading dialog
        loadingDialogHandler.mLoadingDialogContainer = mUILayout
                .findViewById(R.id.loading_indicator);

        // Shows the loading indicator at start
        loadingDialogHandler
                .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);

        final FloatingActionButton checkoutButton = mUILayout.findViewById(R.id.go_to_checkout_button);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                checkoutButton.setVisibility(View.INVISIBLE);
            }
        });
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCheckoutActivity();
            }
        });

        // Adds the inflated layout to the view
        addContentView(mUILayout, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
    }

    // Methods to load and destroy tracking data.
    @Override
    public boolean doLoadTrackersData() {

        String pName;
        String pBrand;

        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) tManager
                .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null) {
            return false;
        }

        if (mCurrentDataSet == null) {
            mCurrentDataSet = objectTracker.createDataSet();
        }

        if (mCurrentDataSet == null) {
            return false;
        }

        if (!mCurrentDataSet.load("Synchrony_DB_OT.xml",
                STORAGE_TYPE.STORAGE_APPRESOURCE)) {
            return false;
        }

        if (!objectTracker.activateDataSet(mCurrentDataSet)) {
            return false;
        }

        int numTrackables = mCurrentDataSet.getNumTrackables();


        for (int count = 0; count < numTrackables; count++) {
            Trackable trackable = mCurrentDataSet.getTrackable(count);
            if (isExtendedTrackingActive()) {
                trackable.startExtendedTracking();
            }


            //removes underscore
            pName = sc.next();
            pName = pName.replace('_', ' ');
            pBrand = sc.next();
            pBrand = pBrand.replace('_', ' ');

            Product p = new Product(trackable.getId(), pName, pBrand, "", sc.nextDouble(), sc.nextInt());
            int colorID;

            while (sc.hasNext("\\s*;\\s*")) {
                sc.next(); // Semicolon
                sc.next(); //Color ID label
                colorID = sc.nextInt(); //Color ID value

                //Adds url to product
                while ((!sc.hasNext("\\s*ColorID\\s*")) && (!sc.hasNext("\\s*;\\s*"))) {
                    p.addImgURL(colorID, sc.next());
                }
                sc.next(); // Semicolon
            }

            catalogue.addProduct(p);
        }
        sc.close();
        return true;
    }

    @Override
    public boolean doUnloadTrackersData() {
        // Indicate if the trackers were unloaded correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) tManager
                .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null) {
            return false;
        }

        if (mCurrentDataSet != null && mCurrentDataSet.isActive()) {
            if (objectTracker.getActiveDataSet(0).equals(mCurrentDataSet)
                && !objectTracker.deactivateDataSet(mCurrentDataSet)) {
                result = false;
            } else if (!objectTracker.destroyDataSet(mCurrentDataSet)) {
                result = false;
            }

            mCurrentDataSet = null;
        }

        return result;
    }

    @Override
    public void onVuforiaResumed() {
        if (mGlView != null) {
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }
    }

    @Override
    public void onInitARDone(SampleApplicationException exception) {
        if (exception == null) {
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

        } else {
            Log.e(LOG_TAG, exception.getString());
            if (exception.getCode() == SampleApplicationException.LOADING_TRACKERS_FAILURE) {
                showInitializationErrorMessage(
                        getString(R.string.INIT_OBJECT_DATASET_NOT_FOUND_TITLE),
                        getString(R.string.INIT_OBJECT_DATASET_NOT_FOUND));

            } else {
                showInitializationErrorMessage(getString(R.string.INIT_ERROR),
                        exception.getString());
            }
        }
    }

    @Override
    public void onVuforiaStarted() {
        // Set camera focus mode
        if (!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO)) {
            // If continuous auto-focus mode fails, attempt to set to a different mode
            if (!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO)) {
                CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_NORMAL);
            }
        }
        showProgressIndicator(false);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FloatingActionButton checkoutButton = mUILayout.findViewById(R.id.go_to_checkout_button);
                checkoutButton.setVisibility(View.VISIBLE);
            }
        });
    }

    public void showProgressIndicator(boolean show) {
        if (loadingDialogHandler != null) {
            if (show) {
                loadingDialogHandler
                        .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);
            } else {
                loadingDialogHandler
                        .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
            }
        }
    }

    // Shows initialization error messages as System dialogs
    public void showInitializationErrorMessage(String title, String message) {
        final String errorMessage = message;
        final String messageTitle = title;
        runOnUiThread(new Runnable() {
            public void run() {
                if (mErrorDialog != null) {
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
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        finish();
                                    }
                                });

                mErrorDialog = builder.create();
                mErrorDialog.show();
            }
        });
    }

    @Override
    public void onVuforiaUpdate(State state) {
    }

    @Override
    public boolean doInitTrackers() {
        // Indicate if the trackers were initialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker;

        // Trying to initialize the image tracker
        tracker = tManager.initTracker(ObjectTracker.getClassType());
        if (tracker == null) {
            Log.e(LOG_TAG,
                    "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else {
            Log.i(LOG_TAG, "Tracker successfully initialized");
        }
        return result;
    }

    @Override
    public boolean doStartTrackers() {
        // Indicate if the trackers were started correctly
        Tracker objectTracker = TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (objectTracker != null) {
            objectTracker.start();
        }

        return true;
    }

    @Override
    public boolean doStopTrackers() {
        // Indicate if the trackers were stopped correctly
        Tracker objectTracker = TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (objectTracker != null) {
            objectTracker.stop();
        }

        return true;
    }

    @Override
    public boolean doDeinitTrackers() {
        // Indicate if the trackers were deinitialized correctly
        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(ObjectTracker.getClassType());

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Process the Gestures
        return mGestureDetector.onTouchEvent(event);
    }

    boolean isExtendedTrackingActive() {
        return false;
    }

    public void setCurrentProduct(Product currentProduct) {
        this.currentProduct = currentProduct;
        infoOverlay.setProduct(currentProduct);
    }

    public void displayInfoOverlay() {
        infoOverlay.display();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FloatingActionButton checkoutButton = mUILayout.findViewById(R.id.go_to_checkout_button);
                checkoutButton.setVisibility(View.GONE);
            }
        });
    }

    public void removeInfoOverlay() {
        infoOverlay.remove();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FloatingActionButton checkoutButton = mUILayout.findViewById(R.id.go_to_checkout_button);
                checkoutButton.setVisibility(View.VISIBLE);
            }
        });
    }

    public void showInfoActivity(View v) {
        Intent infoActivityIntent = new Intent(MainActivity.this, UserInterface.class);
        infoActivityIntent.putExtra("currentProduct", currentProduct);
        startActivity(infoActivityIntent);
    }

    public void showCheckoutActivity() {
        Intent infoActivityIntent = new Intent(MainActivity.this, CheckoutActivity.class);
        infoActivityIntent.putExtra("cart", cart);
        startActivityForResult(infoActivityIntent, RESULT_CART_INFO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_CART_INFO && resultCode == RESULT_OK) {
            if (data.getExtras() != null) {
                cart = data.getExtras().getParcelable("cart");
                infoOverlay.updateCart(cart);
            } else {
                cart = null;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
