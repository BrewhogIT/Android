package com.bignerdranch.android.photogallery;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;

public class PhotoGalleryFragment extends Fragment {
    private RecyclerView mPhotoRecyclerView;
    private final String TAG = "PhotoGalleryFragment";
    private List<GalleryItem> mItems = new ArrayList<>();
    private boolean isLoading = false;
    private int adapterPosition = 0;
    private ThumbnailDownloader <PhotoHolder> mThumbnailDownloader;

    public static PhotoGalleryFragment newInstance(){
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        updateItems();

        ActivityManager am = (ActivityManager)getActivity().getSystemService(ACTIVITY_SERVICE);

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler,am);
        mThumbnailDownloader.setThumbnailDownloadListener(
                new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
                    @Override
                    public void onThumbnailDownloaded(PhotoHolder photoHolder, Bitmap bitmap) {
                        Drawable drawable = new BitmapDrawable(getResources(),bitmap);
                        photoHolder.bindDrawable(drawable);
                    }
                }
        );
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG,"Background thread started");
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
        setupAdapter();
        return v;
    }

    private class PhotoHolder extends RecyclerView.ViewHolder{
        private ImageView mItemImageView;

        public PhotoHolder(@NonNull View itemView) {
            super(itemView);
            mItemImageView = itemView.findViewById(R.id.item_image_view);
        }

        public void bindDrawable(Drawable drawable){
            mItemImageView.setImageDrawable(drawable);
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
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallery_item,viewGroup,false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoHolder photoHolder, int i) {
            GalleryItem galleryItem = mGalleryItems.get(i);
            Drawable placeholder = getResources().getDrawable(R.drawable.bill_up_close);
            photoHolder.bindDrawable(placeholder);

            mThumbnailDownloader.queueThumbnail(photoHolder,galleryItem.getUrl());
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    private class FetchItemTask extends AsyncTask<Void,Void,List<GalleryItem>> {
        private String mQuery;
        private ProgressDialog mProgressDialog;

        public FetchItemTask(String query) {
            mQuery = query;
            mProgressDialog = new ProgressDialog(getActivity());
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog.setMessage("Loading");
            mProgressDialog.show();
        }

        @Override
        protected List<GalleryItem> doInBackground(Void... voids) {
            if (mQuery == null){
                return new FlickrFetchr().fetchRecentPhotos();
            }else {
                return new FlickrFetchr().searchPhotos(mQuery);
            }
        }


        @Override
        protected void onPostExecute(List<GalleryItem> item) {
            mItems = item;
            setupAdapter();
            isLoading = false;
            if (mProgressDialog.isShowing()){
                mProgressDialog.dismiss();
            }
        }
    }

    private void setupAdapter(){
        if (isAdded()){
            PhotoAdapter adapter =(PhotoAdapter) mPhotoRecyclerView.getAdapter();
            if (adapter == null || !isLoading){
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
            updateItems();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.fragment_photo_gallery,menu);

        final MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.d(TAG,"QueryTextSubmit " + s);
                QueryPreferences.setStoredQuery(getActivity(),s);
                updateItems();
                searchView.onActionViewCollapsed();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(TAG,"QueryTextChange " + s);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query,false);
            }
        });

        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (PollService.isServiceAlarmOn(getActivity())){
                toggleItem.setTitle(R.string.stop_polling);
            }else{
                toggleItem.setTitle(R.string.start_polling);
            }
        } else {
            if (PollServiceSchedule.isJobPlanned(getActivity())){
                toggleItem.setTitle(R.string.stop_polling);
            }else{
                toggleItem.setTitle(R.string.start_polling);
            }
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(getActivity(),null);
                updateItems();
                return true;
            case R.id.menu_item_toggle_polling:

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                    PollService.setServiceAlarm(getActivity(),shouldStartAlarm);
                } else {
                    boolean shouldStartPollServiceSchedule = !PollServiceSchedule.isJobPlanned(getActivity());
                    PollServiceSchedule.setPollServiceSchedule(getActivity(),shouldStartPollServiceSchedule);
                }

                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateItems(){
        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchItemTask(query).execute();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG,"Background thread destroyed");
    }
}
