package com.rgdgr8.rggallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rgdgr8.rggallery.databinding.ListItemBinding;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GalleryFragment extends Fragment {
    private static final String TAG = "GalleryFrag";
    private static final String SEARCH_QUERY_BUNDLE_ARG = "search";
    private ImageFetchingTask task;
    private ImageAdapter adapter;
    private ThumbNailDownloader<GalleryItem> mThumbNailDownloader;
    public static String mSearchViewQuery = "";


    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setRetainInstance(true);
        setHasOptionsMenu(true);

        task = new ImageFetchingTask();
        task.execute();

        Handler handler = new Handler(getActivity().getMainLooper());
        mThumbNailDownloader = new ThumbNailDownloader<>(handler);
        mThumbNailDownloader.start();
        mThumbNailDownloader.getLooper();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Log.d(TAG, "onCreateOptionsMenu: ");
        inflater.inflate(R.menu.gallery_menu, menu);

        MenuItem pollItem = menu.findItem(R.id.poll);
        if(PollService.isAlarmOn(getActivity())){
            pollItem.setTitle(getResources().getString(R.string.dont_notify_when_available));
            pollItem.setIcon(R.drawable.notifications_off);
            Toast.makeText(getActivity(), "Polling turned on", Toast.LENGTH_SHORT).show();
        }else {
            pollItem.setTitle(getResources().getString(R.string.notify_when_available));
            pollItem.setIcon(R.drawable.notifications_active);
            Toast.makeText(getActivity(), "Polling turned off", Toast.LENGTH_SHORT).show();
        }

        SearchView mSearchView = (SearchView) menu.findItem(R.id.search).getActionView();

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchViewQuery = query;
                mSearchView.clearFocus();
                ImageFetcher.tempStoreRecentItemsListAndClearItemsList();
                task = new ImageFetchingTask();
                task.execute();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mSearchViewQuery = "";
                ImageFetcher.restoreItemsList();
                setUpAdapter();
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.poll:
                boolean setAlarm = !PollService.isAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(),setAlarm);
                getActivity().invalidateOptionsMenu();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setUpAdapter() {
        if (isAdded()) {
            if (adapter == null) {
                adapter = new ImageAdapter();
            } else {
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @org.jetbrains.annotations.NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        com.rgdgr8.rggallery.databinding.FragmentGalleryBinding mGalleryBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_gallery, container, false);
        mGalleryBinding.rv.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mGalleryBinding.rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull @NotNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1)) {
                    Log.d(TAG, "onScrollStateChanged: hit bottom");
                    task = new ImageFetchingTask();
                    task.execute();
                }
            }
        });
        setUpAdapter();
        mGalleryBinding.rv.setAdapter(adapter);
        return mGalleryBinding.getRoot();
    }

    private class ImageFetchingTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                if (mSearchViewQuery.equals("")) {
                     ImageFetcher.getRecentImages(false);
                } else {
                     ImageFetcher.getSearchedImages(mSearchViewQuery,false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            if (ImageFetcher.getItemList() == null) {
                Log.e(TAG, "onPostExecute: No items fetched");
            }
            String firstId = ImageFetcher.getItemList().get(0).getId();
            Log.d(TAG, "onPostExecute: firstId = "+firstId);
            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                    .putString(PollService.SP_FIRST_ID_KEY,firstId).apply();
            setUpAdapter();
        }
    }

    public class ImageHolder extends RecyclerView.ViewHolder {
        private ListItemBinding mLib;

        public ListItemBinding getBinding() {
            return mLib;
        }

        public ImageHolder(ListItemBinding lib) {
            super(lib.getRoot());
            mLib = lib;
            mLib.setViewModel(new GalleryItemViewModel(getActivity()));
        }

        public void bind(GalleryItem galleryItem) {
            mLib.getViewModel().setGalleryItem(galleryItem);
            mLib.executePendingBindings();
        }

        public void bindWithPicasso(GalleryItem galleryItem) {
            //TODO: To call this function you must remove the data binding from app:srcCompat in list_item.xml
            Picasso.get()
                    .load(galleryItem.getUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(mLib.galleryImage);
        }
    }

    private class ImageAdapter extends RecyclerView.Adapter<ImageHolder> {
        private List<GalleryItem> mGalleryItems;

        public ImageAdapter() {
            mGalleryItems = ImageFetcher.getItemList();
        }

        @NonNull
        @NotNull
        @Override
        public ImageHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            ListItemBinding lib = DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.list_item, parent, false);
            return new ImageHolder(lib);
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull GalleryFragment.ImageHolder holder, int position) {
            Log.d(TAG, "onBindViewHolder: " + position);
            holder.bind(mGalleryItems.get(position));
            mThumbNailDownloader.queueMessage(position, holder);
            //holder.bindWithPicasso(mGalleryItems.get(position));
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbNailDownloader.clearQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbNailDownloader.quit();
        ImageFetcher.restoreItemsList();
    }
}
