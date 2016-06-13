package nickrout.lenslauncher.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thesurix.gesturerecycler.GestureAdapter;
import com.thesurix.gesturerecycler.GestureManager;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import nickrout.lenslauncher.R;
import nickrout.lenslauncher.adapter.ArrangerDragDropAdapter;
import nickrout.lenslauncher.model.App;
import nickrout.lenslauncher.util.AppSorter;
import nickrout.lenslauncher.util.AppsSingleton;
import nickrout.lenslauncher.util.BroadcastReceivers;
import nickrout.lenslauncher.util.Settings;

/**
 * Created by nicholasrout on 2016/06/08.
 */
public class AppsFragment extends Fragment implements SettingsActivity.AppsInterface {

    private static final String TAG = "AppsFragment";

    @Bind(R.id.recycler_apps)
    RecyclerView mRecycler;

    @Bind(R.id.progress_apps)
    MaterialProgressBar mProgress;

    private Settings mSettings;
    private ArrangerDragDropAdapter mAdapter;
    private int mScrolledItemIndex;
    private GestureManager mGestureManager;

    public AppsFragment() {
    }

    public static AppsFragment newInstance() {
        AppsFragment appsFragment = new AppsFragment();
        // Include potential bundle extras here
        return appsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apps, container, false);
        ButterKnife.bind(this, view);
        mSettings = new Settings(getActivity());
        setupRecycler(AppsSingleton.getInstance().getApps());
        return view;
    }

    private void sendRefreshAppsBroadcast() {
        if (getActivity() == null) {
            return;
        }
        Intent refreshAppsIntent = new Intent(getActivity(), BroadcastReceivers.AppsUpdatedReceiver.class);
        getActivity().sendBroadcast(refreshAppsIntent);
    }

    private void setupRecycler(ArrayList<App> apps) {
        if (getActivity() == null || apps.size() == 0) {
            return;
        }
        if (mRecycler.getLayoutManager() != null) {
            mScrolledItemIndex = ((LinearLayoutManager) mRecycler.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        }
        mProgress.setVisibility(View.INVISIBLE);
        mRecycler.setVisibility(View.VISIBLE);
        mAdapter = new ArrangerDragDropAdapter(getActivity());
        mAdapter.setData(apps);
        mRecycler.setAdapter(mAdapter);
        mRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecycler.setItemAnimator(new DefaultItemAnimator());
        mRecycler.setHasFixedSize(true);
        mRecycler.scrollToPosition(mScrolledItemIndex);
        mScrolledItemIndex = 0;

        mGestureManager = new GestureManager.Builder(mRecycler)
                .setSwipeEnabled(false)
                .setLongPressDragEnabled(true)
                .setGestureFlags(ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, ItemTouchHelper.UP | ItemTouchHelper.DOWN)
                .build();

        mGestureManager.setManualDragEnabled(true);

        mAdapter.setDataChangeListener(new GestureAdapter.OnDataChangeListener<App>() {
            @Override
            public void onItemRemoved(App item, int position) {

            }

            @Override
            public void onItemReorder(App item, int fromPos, int toPos) {
                Snackbar.make(mRecycler, "App moved from " + fromPos + " " + toPos, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDefaultsReset() {
        if (mSettings.getSortType() != AppSorter.SortType.values()[Settings.DEFAULT_SORT_TYPE]) {
            mSettings.save(Settings.KEY_SORT_TYPE, Settings.DEFAULT_SORT_TYPE);
            sendRefreshAppsBroadcast();
        }
    }

    @Override
    public void onAppsUpdated(ArrayList<App> apps) {
        setupRecycler(apps);
    }
}
