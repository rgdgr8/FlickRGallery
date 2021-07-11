package com.rgdgr8.rggallery;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rgdgr8.rggallery.databinding.FragmentGalleryBinding;
import com.rgdgr8.rggallery.databinding.ListItemBinding;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GalleryFragment extends Fragment {
    private static final String TAG = "GalleryFrag";
    private ImageFetchingTask task;
    private ImageAdapter adapter;
    private ThumbNailDownloader<GalleryItem> mThumbNailDownloader;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        task = new ImageFetchingTask();
        task.execute();
        Handler handler = new Handler(getActivity().getMainLooper());
        mThumbNailDownloader = new ThumbNailDownloader<>(handler);
        mThumbNailDownloader.start();
        mThumbNailDownloader.getLooper();
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
        com.rgdgr8.rggallery.databinding.FragmentGalleryBinding mGalleryBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_gallery, container, false);
        mGalleryBinding.rv.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mGalleryBinding.rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull @NotNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState==RecyclerView.SCROLL_STATE_IDLE){
                    Log.d(TAG, "onScrollStateChanged: Idle");
                    ThumbNailDownloader.PRE_DOWNLOAD = true;
                    mThumbNailDownloader.queueMessage();
                }
                else if (newState==RecyclerView.SCROLL_STATE_DRAGGING){
                    Log.d(TAG, "onScrollStateChanged: Dragging");
                    ThumbNailDownloader.PRE_DOWNLOAD = false;
                }
                else if (!recyclerView.canScrollVertically(1)) {
                    Log.d(TAG, "onScrollStateChanged: ");
                    if (task.getStatus() == AsyncTask.Status.RUNNING) return;
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
                ImageFetcher.fetchItems();
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
            mThumbNailDownloader.queueMessage(position,holder);
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
    }
}
