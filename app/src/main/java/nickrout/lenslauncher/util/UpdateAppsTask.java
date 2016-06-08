package nickrout.lenslauncher.util;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.util.ArrayList;

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
    private UpdateAppsTaskListener mUpdateAppsTaskListener;

    private Settings mSettings;
    private ArrayList<App> mApps;
    private ArrayList<Bitmap> mAppIcons;

    public UpdateAppsTask(PackageManager packageManager,
                          Context context,
                          Application application,
                          boolean isLoad,
                          UpdateAppsTaskListener updateAppsTaskListener) {
        this.mPackageManager = packageManager;
        this.mContext = context;
        this.mApplication = application;
        this.mIsLoad = isLoad;
        this.mSettings = new Settings(context);
        this.mUpdateAppsTaskListener = updateAppsTaskListener;
    }

    @Override
    protected void onPreExecute() {
        mUpdateAppsTaskListener.onUpdateAppsTaskPreExecute(mIsLoad);
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        ArrayList<App> apps = AppUtil.getApps(mPackageManager, mContext, mApplication, mSettings.getString(Settings.KEY_ICON_PACK_LABEL_NAME), mSettings.getSortType());

        App[] appArray = new App[apps.size()];
        Bitmap[] appIconsArray = new Bitmap[apps.size()];

        mApps = new ArrayList<>();
        mAppIcons = new ArrayList<>();
        for (int i = 0; i < apps.size(); i++) {
            App app = apps.get(i);
            Bitmap appIcon = app.getIcon();
            if (appIcon != null) {
                int appOrderNumber = AppPersistent.getAppOrderNumber(app.getPackageName().toString(), app.getName().toString());
                appArray[appOrderNumber] = app;
                appIconsArray[appOrderNumber] = appIcon;
            }
        }

        for (int i = 0; i < appArray.length; i++)
            mApps.add(appArray[i]);

        for (int i = 0; i < appIconsArray.length; i++)
            mAppIcons.add(appIconsArray[i]);

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