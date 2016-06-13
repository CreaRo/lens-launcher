package nickrout.lenslauncher.adapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.thesurix.gesturerecycler.GestureAdapter;
import com.thesurix.gesturerecycler.GestureViewHolder;

import butterknife.Bind;
import butterknife.ButterKnife;
import nickrout.lenslauncher.R;
import nickrout.lenslauncher.model.App;
import nickrout.lenslauncher.model.AppPersistent;
import nickrout.lenslauncher.util.AppUtil;

/**
 * Created by rish on 8/6/16.
 */
public class ArrangerDragDropAdapter extends GestureAdapter<App, GestureViewHolder> {

    private static final String TAG = "TAG";
    private final Context mContext;

    public ArrangerDragDropAdapter(final Context context) {
        mContext = context;
    }

    @Override
    public GestureViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_app_arranger, parent, false);
        final AppViewHolder holder = new AppViewHolder(itemView, mContext);
        holder.setOnClickListeners();
        return holder;
    }

    @Override
    public void onBindViewHolder(final GestureViewHolder holder, final int position) {
        super.onBindViewHolder(holder, position);
        final App app = getData().get(position);

        final AppViewHolder appHolder = (AppViewHolder) holder;
        appHolder.setAppElement(app);
    }

    @Override
    public int getItemCount() {
        return getData().size();
    }

    public class AppViewHolder extends GestureViewHolder implements PopupMenu.OnMenuItemClickListener {

        @Bind(R.id.element_app_container)
        CardView mContainer;

        @Bind(R.id.element_app_label)
        TextView mLabel;

        @Bind(R.id.element_app_icon)
        ImageView mIcon;

        @Bind(R.id.element_app_hide)
        ImageView mToggleAppVisibility;

        @Bind(R.id.element_app_menu)
        ImageView mMenu;

        private App mApp;
        private Context mContext;

        public AppViewHolder(final View view, Context context) {
            super(view);
            ButterKnife.bind(this, view);
            mContext = context;
        }

        @Override
        public boolean canDrag() {
            return true;
        }

        @Override
        public boolean canSwipe() {
            return false;
        }

        @Override
        public void onItemClear() {
            super.onItemClear();
        }

        @Override
        public void onItemSelect() {
            super.onItemSelect();
        }

        @Nullable
        @Override
        public View getDraggableView() {
            return mIcon;
        }

        public void setAppElement(App app) {
            this.mApp = app;
            mLabel.setText(mApp.getLabel());
            mIcon.setImageBitmap(mApp.getIcon());
            boolean isAppVisible = AppPersistent.getAppVisibility(mApp.getPackageName().toString(), mApp.getName().toString());
            if (isAppVisible) {
                mToggleAppVisibility.setImageResource(R.drawable.ic_visibility_grey_24dp);
            } else {
                mToggleAppVisibility.setImageResource(R.drawable.ic_visibility_off_grey_24dp);
            }
            if (mApp.getPackageName().toString().equals(mContext.getPackageName()))
                mToggleAppVisibility.setVisibility(View.INVISIBLE);
            else
                mToggleAppVisibility.setVisibility(View.VISIBLE);
            mContainer.postInvalidate();
        }

        public void toggleAppVisibility(App app) {
            this.mApp = app;
            boolean isAppVisible = AppPersistent.getAppVisibility(mApp.getPackageName().toString(), mApp.getName().toString());
            AppPersistent.setAppVisibility(
                    mApp.getPackageName().toString(),
                    mApp.getName().toString(),
                    !isAppVisible);
            if (isAppVisible) {
                Snackbar.make(mContainer, mApp.getLabel() + " is now hidden", Snackbar.LENGTH_LONG).show();
                mToggleAppVisibility.setImageResource(R.drawable.ic_visibility_off_grey_24dp);
            } else {
                Snackbar.make(mContainer, mApp.getLabel() + " is now visible", Snackbar.LENGTH_LONG).show();
                mToggleAppVisibility.setImageResource(R.drawable.ic_visibility_grey_24dp);
            }
        }

        public void setOnClickListeners() {
            mToggleAppVisibility.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mApp != null)
                        toggleAppVisibility(mApp);
                    else
                        Snackbar.make(mContainer, mContext.getString(R.string.error_app_not_found), Snackbar.LENGTH_LONG).show();
                    printAllPersistent();
                }
            });

            mMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(mContext, view);
                    popupMenu.setOnMenuItemClickListener(AppViewHolder.this);
                    popupMenu.inflate(R.menu.menu_app);
                    popupMenu.show();
                }
            });
        }

        public void printAllPersistent() {
            for (AppPersistent appPersistent : AppPersistent.listAll(AppPersistent.class)) {
                if (!appPersistent.isAppVisible())
                    Log.d(TAG, appPersistent.toString());
            }
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_item_element_open:
                    AppUtil.launchComponent(mApp.getPackageName().toString(), mApp.getName().toString(), mContext);
                    return true;
                case R.id.menu_item_element_uninstall:
                    try {
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + mApp.getPackageName()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(mContext, R.string.error_app_not_found, Toast.LENGTH_SHORT).show();
                    }
                    return true;
            }
            return false;
        }
    }
}