package com.rgdgr8.rggallery;

import android.os.AsyncTask;
import android.os.Bundle;
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

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GalleryFragment extends Fragment {
    private static final String TAG = "GalleryFrag";
    private FragmentGalleryBinding mGalleryBinding;
    private ImageFetchingTask task;
    private ImageAdapter adapter;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        task = new ImageFetchingTask();
        task.execute();
    }

    public void setUpAdapter(){
        if(isAdded()){
            if(adapter==null) {
                adapter = new ImageAdapter();
                mGalleryBinding.rv.setAdapter(adapter);
            }else {
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @org.jetbrains.annotations.NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        mGalleryBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_gallery,container,false);
        mGalleryBinding.rv.setLayoutManager(new GridLayoutManager(getActivity(),3));
        mGalleryBinding.rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull @NotNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(!recyclerView.canScrollVertically(1)){
                    if(task.getStatus()== AsyncTask.Status.RUNNING) return;
                    task = new ImageFetchingTask();
                    task.execute();
                }
            }
        });
        //adapter set in onPostExecute()
        return mGalleryBinding.getRoot();
    }

    private class ImageFetchingTask extends AsyncTask<Void,Void,Void>{
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
            if(ImageFetcher.getItemList()==null){
                Log.e(TAG, "onPostExecute: No items fetched");
                return;
            }
            setUpAdapter();
        }
    }

    private class ImageHolder extends RecyclerView.ViewHolder {
        private ListItemBinding mLib;
        public ImageHolder(ListItemBinding lib) {
            super(lib.getRoot());
            mLib = lib;
            mLib.setViewModel(new GalleryItemViewModel());
        }

        public void bind(GalleryItem galleryItem){
            mLib.getViewModel().setGalleryItem(galleryItem);
            mLib.executePendingBindings();
        }
    }

    private class ImageAdapter extends RecyclerView.Adapter<ImageHolder>{
        private List<GalleryItem> mGalleryItems;
        public ImageAdapter(){
            mGalleryItems = ImageFetcher.getItemList();
        }

        @NonNull
        @NotNull
        @Override
        public ImageHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            ListItemBinding lib = DataBindingUtil.inflate(LayoutInflater.from(getActivity()),R.layout.list_item,parent,false);
            return new ImageHolder(lib);
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull GalleryFragment.ImageHolder holder, int position) {
            holder.bind(mGalleryItems.get(position));
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }
}
