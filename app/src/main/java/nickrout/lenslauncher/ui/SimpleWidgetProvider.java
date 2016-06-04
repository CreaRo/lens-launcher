package nickrout.lenslauncher.ui;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

import java.util.Random;

import nickrout.lenslauncher.R;

/**
 * Created by rish on 3/6/16.
 */
public class SimpleWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int count = appWidgetIds.length;

        for (int i = 0; i < count; i++) {
            int widgetId = appWidgetIds[i];
            String number = String.format("%03d", (new Random().nextInt(900) + 100));

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_provider);
            LensView lensView = new LensView(context);
            lensView.measure(200, 200);
            lensView.layout(0, 0, 200, 200);
            lensView.setDrawingCacheEnabled(true);
            Bitmap bitmap = lensView.getDrawingCache();
            remoteViews.setImageViewBitmap(R.id.widget_image, bitmap);

            Intent intent = new Intent(context, SimpleWidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        }
    }
}
