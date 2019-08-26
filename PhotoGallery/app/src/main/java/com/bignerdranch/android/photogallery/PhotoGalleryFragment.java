package com.bignerdranch.android.photogallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {
    private RecyclerView mPhotoRecyclerView;
    private final String TAG = "PhotoGalleryFragment";
    private List<GalleryItem> mItems = new ArrayList<>();
    private boolean isLoading = false;
    private int adapterPosition = 0;

    public static PhotoGalleryFragment newInstance(){
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchItemTask().execute();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mPhotoRecyclerView = v.findViewById(R.id.photo_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
        final RecyclerView.LayoutManager manager = mPhotoRecyclerView.getLayoutManager();

        mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int totalItemCount = manager.getItemCount();
                int lastVisiblePosition = ((GridLayoutManager) manager)
                        .findLastVisibleItemPosition();
                adapterPosition = ((GridLayoutManager) manager).findFirstVisibleItemPosition();

                if (!isLoading && (lastVisiblePosition >= totalItemCount - 1)) {
                    isLoading = true;
                    updatePhotoList();
                }
            }
        });

        ViewTreeObserver observer = mPhotoRecyclerView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mPhotoRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                int recyclerViewWidth = mPhotoRecyclerView.getWidth();
                int columnCount = recyclerViewWidth / 300;
                GridLayoutManager manager = new GridLayoutManager(getActivity(),columnCount);
                manager.scrollToPosition(adapterPosition);
                mPhotoRecyclerView.setLayoutManager(manager);

                Log.i(TAG, "onGlobalLayout is work");
            }
        });

        setupAdapter();
        return v;
    }

    private class PhotoHolder extends RecyclerView.ViewHolder{
        private TextView mTitleTextview;

        public PhotoHolder(@NonNull View itemView) {
            super(itemView);
            mTitleTextview = (TextView) itemView;
        }

        public void bindGalleryItem(GalleryItem item){
            mTitleTextview.setText(item.toString());
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>{

        List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @NonNull
        @Override
        public PhotoHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            TextView textView = new TextView(getActivity());
            textView.setLayoutParams(new ViewGroup.LayoutParams(300,ViewGroup.LayoutParams.WRAP_CONTENT));
            return new PhotoHolder(textView);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoHolder photoHolder, int i) {
            GalleryItem galleryItem = mGalleryItems.get(i);
            photoHolder.bindGalleryItem(galleryItem);
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    private class FetchItemTask extends AsyncTask<Void,Void,List<GalleryItem>> {

        @Override
        protected List<GalleryItem> doInBackground(Void... voids) {
            return new FlickrFetchr().fetchItems();
        }

        @Override
        protected void onPostExecute(List<GalleryItem> item) {
            mItems = item;
            setupAdapter();
            isLoading = false;
        }
    }

    private void setupAdapter(){
        if (isAdded()){
            PhotoAdapter adapter =(PhotoAdapter) mPhotoRecyclerView.getAdapter();
            if (adapter == null){
                mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
            }else{
                adapter.mGalleryItems.addAll(mItems);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void updatePhotoList(){
        if (FlickrFetchr.getMaxPage() > FlickrFetchr.getPageNumber()){
            FlickrFetchr.updatePage();
            new FetchItemTask().execute();
        }
    }
}
