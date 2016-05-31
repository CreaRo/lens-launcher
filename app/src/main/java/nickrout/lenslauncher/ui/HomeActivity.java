package nickrout.lenslauncher.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import nickrout.lenslauncher.R;
import nickrout.lenslauncher.model.App;
import nickrout.lenslauncher.model.AppPersistent;
import nickrout.lenslauncher.model.AppTray;
import nickrout.lenslauncher.util.ObservableObject;
import nickrout.lenslauncher.util.UpdateAppsTask;

/**
 * Created by nickrout on 2016/04/02.
 */
public class HomeActivity extends BaseActivity implements Observer, UpdateAppsTask.UpdateAppsTaskListener {

    private final static String TAG = "HomeActivity";

    @Bind(R.id.lens_view_apps)
    LensView mLensView;

    @Bind(R.id.home_side_bar)
    TextView mSideBar;

    private PackageManager mPackageManager;
    private MaterialDialog mProgressDialog;
    private boolean shortcutPage = false;

    ArrayList<App> mApps;
    ArrayList<Bitmap> mAppIcons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        ObservableObject.getInstance().addObserver(this);
        mPackageManager = getPackageManager();
        loadApps(true);

    }

    @OnClick(R.id.home_side_bar)
    public void onSideBarClick() {
        if (!shortcutPage) {
            setupAppTray();
            shortcutPage = true;
        } else {
            if (mApps == null) {
                loadApps(true);
            } else {
                mLensView.setApps(this.mApps, this.mAppIcons);
            }
            shortcutPage = false;
        }
    }

    private void setupAppTray() {
        ArrayList<App> appTray = new ArrayList<>();
        ArrayList<Bitmap> appTrayIcons = new ArrayList<>();
        if (mApps == null) {
            loadApps(true);
        } else {
            for (App app : mApps) {
                if (AppTray.isInAppTray(app.getPackageName().toString())) {
                    appTray.add(app);
                    appTrayIcons.add(app.getIcon());
                }
            }
            mLensView.setApps(appTray, appTrayIcons);
        }
    }

    private void loadApps(boolean isLoad) {
        new UpdateAppsTask(mPackageManager, getApplicationContext(), getApplication(), isLoad, HomeActivity.this).execute();
    }

    @Override
    public void onBackPressed() {
        // Do Nothing
    }

    public static class AppsUpdatedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ObservableObject.getInstance().update();
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new MaterialDialog.Builder(this)
                    .content(R.string.progress_loading_apps)
                    .progress(true, 0)
                    .cancelable(false)
                    .canceledOnTouchOutside(false)
                    .show();
        }
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    @Override
    public void update(Observable observable, Object data) {
        loadApps(false);
    }

    @Override
    public void onUpdateAppsTaskPreExecute(boolean mIsLoad) {
        if (mIsLoad) {
            showProgressDialog();
        }
    }

    @Override
    public void onUpdateAppsTaskPostExecute(ArrayList<App> mApps, ArrayList<Bitmap> mAppIcons) {
        for (int i = 0; i < mApps.size(); i++) {
            if (!AppPersistent.getAppVisibilityForPackage(mApps.get(i).getPackageName().toString())) {
                mApps.remove(i);
                mAppIcons.remove(i);
                i--;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (HomeActivity.this.isDestroyed()) {
                return;
            }
        } else {
            if (HomeActivity.this.isFinishing()) {
                return;
            }
        }
        dismissProgressDialog();
        mLensView.setPackageManager(mPackageManager);
        this.mApps = mApps;
        this.mAppIcons = mAppIcons;

        if (shortcutPage) {
            setupAppTray();
        } else {
            mLensView.setApps(mApps, mAppIcons);
        }
    }
}