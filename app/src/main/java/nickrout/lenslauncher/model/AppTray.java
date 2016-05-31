package nickrout.lenslauncher.model;

import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;
import com.orm.util.NamingHelper;

/**
 * Created by rish on 31/5/16.
 */
public class AppTray extends SugarRecord {

    private String mPackageName;

    public AppTray() {

    }

    public AppTray(String mPackageName) {
        this.mPackageName = mPackageName;
    }

    public static boolean isInAppTray(String mPackageName) {
        AppTray appTray = Select.from(AppTray.class).where(Condition.prop(NamingHelper.toSQLNameDefault("mPackageName")).eq(mPackageName)).first();
        if (appTray != null) {
            return true;
        } else {
            return false;
        }
    }

    public static void addToAppTray(App app) {
        AppTray appTray = new AppTray(app.getPackageName().toString());
        appTray.save();
    }

    public static void removeFromAppTray(String mPackageName) {
        AppTray appTray = Select.from(AppTray.class).where(Condition.prop(NamingHelper.toSQLNameDefault("mPackageName")).eq(mPackageName)).first();
        appTray.delete();
    }
}