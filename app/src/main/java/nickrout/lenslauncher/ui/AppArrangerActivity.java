package nickrout.lenslauncher.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.thesurix.gesturerecycler.GestureAdapter;
import com.thesurix.gesturerecycler.GestureManager;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import nickrout.lenslauncher.R;
import nickrout.lenslauncher.adapter.ArrangerDragDropAdapterSwipe;
import nickrout.lenslauncher.model.App;
import nickrout.lenslauncher.util.AppSorter;
import nickrout.lenslauncher.util.AppsSingleton;
import nickrout.lenslauncher.util.ObservableObject;
import nickrout.lenslauncher.util.Settings;
import nickrout.lenslauncher.util.UpdateAppsTask;

public class AppArrangerActivity extends BaseActivity
        implements Observer, UpdateAppsTask.UpdateAppsTaskListener {

    private static final String TAG = "AppArrangerActivity";

    @Bind(R.id.arranger_recycler_view)
    RecyclerView mRecyclerView;

    @Bind(R.id.arranger_sort_fab)
    FloatingActionButton mSortFab;

    private MaterialDialog mProgressDialog;
    private MaterialDialog mSortTypeChooserDialog;
    private ArrangerDragDropAdapterSwipe mArrangerDragDropAdapter;
    private GestureManager mGestureManager;

    private Settings mSettings;

    private boolean mMustShowDialog = false;
    private int mScrolledItemIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_arranger);
        ButterKnife.bind(this);
        mSettings = new Settings(this);
        setUpViews();
        ArrayList<App> apps = AppsSingleton.getInstance().getApps();
        if (apps == null) {
            loadApps(true);
        } else {
            setupRecycler(apps);
            if (AppsSingleton.getInstance().doesNeedUpdate()) {
                loadApps(false);
            }
        }
        ObservableObject.getInstance().addObserver(this);
    }

    private void setUpViews() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mSortFab.show();
                } else {
                    mSortFab.hide();
                }
            }
        });
    }

    @OnClick(R.id.arranger_sort_fab)
    public void onSortClicked() {

        final List<AppSorter.SortType> sortTypes = new ArrayList<>(EnumSet.allOf(AppSorter.SortType.class));
        final List<String> sortTypeStrings = new ArrayList<>();
        for (int i = 0; i < sortTypes.size(); i++)
            sortTypeStrings.add(getApplicationContext().getString(sortTypes.get(i).getDisplayNameResId()));

        AppSorter.SortType selectedSortType = mSettings.getSortType();
        int selectedIndex = sortTypes.indexOf(selectedSortType);

        mSortTypeChooserDialog = new MaterialDialog.Builder(AppArrangerActivity.this)
                .title(R.string.setting_sort_apps)
                .items(sortTypeStrings)
                .alwaysCallSingleChoiceCallback()
                .itemsCallbackSingleChoice(selectedIndex, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        mSettings.save(sortTypes.get(which));
                        /* Send broadcast to refresh the app drawer in background. */
                        mMustShowDialog = true;
                        Intent refreshAppsIntent = new Intent(AppArrangerActivity.this, HomeActivity.AppsUpdatedReceiver.class);
                        sendBroadcast(refreshAppsIntent);
                        return true;
                    }
                })
                .build();
        mSortTypeChooserDialog.show();

    }

    private void loadApps(boolean isLoad) {
        new UpdateAppsTask(getPackageManager(), getApplicationContext(), getApplication(), isLoad, AppArrangerActivity.this).execute();
    }

    @Override
    public void onUpdateAppsTaskPreExecute(boolean isLoad) {
        mMustShowDialog = false;
        if (isLoad) {
            showProgressDialog();
        }
    }

    @Override
    public void onUpdateAppsTaskPostExecute(ArrayList<App> apps, ArrayList<Bitmap> appIcons) {
        dismissProgressDialog();
        if (mRecyclerView.getLayoutManager() != null) {
            mScrolledItemIndex = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        }
        setupRecycler(apps);
        AppsSingleton.getInstance().setNeedsUpdate(false);
    }

    private void setupRecycler(ArrayList<App> apps) {
        mArrangerDragDropAdapter = new ArrangerDragDropAdapterSwipe(AppArrangerActivity.this, apps);
        mArrangerDragDropAdapter.setData(apps);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.scrollToPosition(mScrolledItemIndex);
        mScrolledItemIndex = 0;
        mRecyclerView.setAdapter(mArrangerDragDropAdapter);

        mGestureManager = new GestureManager.Builder(mRecyclerView)
                .setSwipeEnabled(false)
                .setLongPressDragEnabled(true)
                .setGestureFlags(ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, ItemTouchHelper.UP | ItemTouchHelper.DOWN)
                .build();

        mArrangerDragDropAdapter.setDataChangeListener(new GestureAdapter.OnDataChangeListener<App>() {
            @Override
            public void onItemRemoved(App item, int position) {
                Snackbar.make(mRecyclerView, "Month removed from position " + position, Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onItemReorder(App item, int fromPos, int toPos) {
                Snackbar.make(mRecyclerView, "Month moved from position " + fromPos + " to " + toPos, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void showProgressDialog() {
        mProgressDialog = new MaterialDialog.Builder(this)
                .content(R.string.progress_loading_apps)
                .progress(true, 0)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .show();
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveToPersistenceAndUpdateHome();
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        ObservableObject.getInstance().deleteObserver(this);
        super.onDestroy();
    }

    private void saveToPersistenceAndUpdateHome() {

//        final List<App> appData = mArrangerDragDropAdapter.getApps();
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                for (int i = 0; i < appData.size(); i++)
//                    AppPersistent.setAppOrderNumber(appData.get(i).getPackageName().toString(), i);
//            }
//        });
//        thread.start();
//        /* Send broadcast to refresh the app drawer in background. */
        Intent refreshHomeIntent = new Intent(AppArrangerActivity.this, HomeActivity.AppsUpdatedReceiver.class);
        sendBroadcast(refreshHomeIntent);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void update(Observable observable, Object data) {
        loadApps(mMustShowDialog);
    }
}