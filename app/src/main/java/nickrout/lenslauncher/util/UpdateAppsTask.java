package nickrout.lenslauncher.util;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Arrays;

import nickrout.lenslauncher.model.App;
import nickrout.lenslauncher.model.AppPersistent;

/**
 * Created by rish on 26/5/16.
 */
public class UpdateAppsTask extends AsyncTask<Void, Void, Void> {

    private PackageManager mPackageManager;
    private Context mContext;
    private Application mApplication;
    private boolean mIsLoad;
    private boolean mShouldPreserveOrder;
    private UpdateAppsTaskListener mUpdateAppsTaskListener;

    private Settings mSettings;
    private ArrayList<App> mApps;
    private ArrayList<Bitmap> mAppIcons;

    public UpdateAppsTask(PackageManager packageManager,
                          Context context,
                          Application application,
                          boolean isLoad,
                          UpdateAppsTaskListener updateAppsTaskListener,
                          boolean shouldPreserveOrder) {
        this.mPackageManager = packageManager;
        this.mContext = context;
        this.mApplication = application;
        this.mIsLoad = isLoad;
        this.mSettings = new Settings(context);
        this.mUpdateAppsTaskListener = updateAppsTaskListener;
        this.mShouldPreserveOrder = shouldPreserveOrder;
    }

    @Override
    protected void onPreExecute() {
        mUpdateAppsTaskListener.onUpdateAppsTaskPreExecute(mIsLoad);
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        ArrayList<App> apps = AppUtil.getApps(mPackageManager, mContext, mApplication, mSettings.getString(Settings.KEY_ICON_PACK_LABEL_NAME), mSettings.getSortType());

        App[] appsArray = new App[apps.size()];
        Bitmap[] appIconsArray = new Bitmap[apps.size()];

        int lastAddedIndex = 0;

        for (int i = 0; i < apps.size(); i++) {
            App app = apps.get(i);
            Bitmap appIcon = app.getIcon();
            if (appIcon != null) {
                if (mShouldPreserveOrder) {
                    int appOrderNumber = AppPersistent.getAppOrderNumber(app.getPackageName().toString(), app.getName().toString());
                    if (appOrderNumber != 0) {
                        appsArray[appOrderNumber] = app;
                        appIconsArray[appOrderNumber] = appIcon;
                    } else {
                        while (appsArray[lastAddedIndex] != null) {
                            lastAddedIndex++;
                        }
                        appsArray[lastAddedIndex] = app;
                        appIconsArray[lastAddedIndex] = appIcon;
                    }
                } else {
                    appsArray[i] = app;
                    appIconsArray[i] = appIcon;
                }
            }
        }

        mApps = new ArrayList<>(Arrays.asList(appsArray));

        mAppIcons = new ArrayList<>(Arrays.asList(appIconsArray));

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        ArrayList<App> singletonApps = new ArrayList<>();
        singletonApps.addAll(mApps);
        AppsSingleton.getInstance().setApps(singletonApps);
        mUpdateAppsTaskListener.onUpdateAppsTaskPostExecute(mApps, mAppIcons);
        super.onPostExecute(result);
    }

    public interface UpdateAppsTaskListener {
        void onUpdateAppsTaskPreExecute(boolean isLoad);

        void onUpdateAppsTaskPostExecute(ArrayList<App> apps, ArrayList<Bitmap> appIcons);
    }
}