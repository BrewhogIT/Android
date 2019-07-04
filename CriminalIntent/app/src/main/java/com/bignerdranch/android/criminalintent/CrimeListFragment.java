package com.bignerdranch.android.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CrimeListFragment extends Fragment {
    private RecyclerView mCrimeRecyclerView;
    private View mLinerLayuotView;
    private Button mCreateButton;
    private CrimeAdapter mAdapter;
    private int position;
    private  boolean mSubtitleVisible;
    private Callbacks mCallbacks;
    private final static String SAVED_SUBTITLE_VISIBLE = "subtitle";

    public interface Callbacks{
        void onCrimeSelected(Crime crime);
    }

    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private Crime mCrime;
        private ImageView mSolvedImageView;

        public CrimeHolder(View view) {
            super(view);

            mTitleTextView = itemView.findViewById(R.id.crime_title);
            mDateTextView = itemView.findViewById(R.id.crime_date);
            mSolvedImageView = itemView.findViewById(R.id.crime_solved);
            itemView.setOnClickListener(this);

        }

        public void bind(Crime crime){
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            mDateTextView.setText(mCrime.getStringDate());
            mSolvedImageView.setVisibility(mCrime.isSolved() ? View.VISIBLE : View.GONE);
            position = getAdapterPosition();
        }

        @Override
        public void onClick(View v) {
            position = getAdapterPosition();
            mCallbacks.onCrimeSelected(mCrime);
        }
    }

    private class RequiresPoliceCrimeHolder extends CrimeHolder{
        private Button mPoliceButton;

        public RequiresPoliceCrimeHolder(View view) {
            super(view);

            mPoliceButton = itemView.findViewById(R.id.police_button);
            mPoliceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(),"Calling police ...",Toast.LENGTH_SHORT).show();
                }
            });
        }


    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder>
    implements SimpleItemTouchHelperCallback.ItemTouchHelperAdapter{
        private List<Crime> mCrimes;
        private final int NEED_POLICE_VIEW = 1;
        private final int NOT_NEED_POLICE_VIEW = 0;

        public CrimeAdapter(List<Crime> crimes){
            mCrimes = crimes;
        }

        public void setCrimes(List<Crime> crimes){
            mCrimes = crimes;
        }

        @Override
        public void onItemDismiss(int position){
            UUID id = mCrimes.get(position).getId();
            CrimeLab.get(getActivity()).delCrime(id);
            notifyItemRemoved(position);
            updateUI();
        }

        @Override
        public boolean onItemMove(int fromPosition, int toPosition){
            if (fromPosition < toPosition){
                for (int i = fromPosition; i < toPosition; i++){
                    Collections.swap(mCrimes,i,i+1);
                }
            }else{
                for (int i = fromPosition; i > toPosition; i--){
                    Collections.swap(mCrimes,i,i-1);
                }
            }
            notifyItemMoved(fromPosition,toPosition);
            return true;
        }

        @NonNull
        @Override
        public CrimeHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View correctView;
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            if (i == NEED_POLICE_VIEW){
                correctView = layoutInflater.inflate(R.layout.list_item_crime_police, viewGroup, false);
                return new RequiresPoliceCrimeHolder(correctView);
            }

            correctView = layoutInflater.inflate(R.layout.list_item_crime, viewGroup, false);
            return new CrimeHolder(correctView);
        }

        @Override
        public void onBindViewHolder(@NonNull CrimeHolder crimeHolder, int i) {
            Crime crime = mCrimes.get(i);
            crimeHolder.bind(crime);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        @Override
        public int getItemViewType(int position) {
            Crime crime = mCrimes.get(position);
            if (crime.isRequiresPolice()){
                return NEED_POLICE_VIEW;
            }
            return NOT_NEED_POLICE_VIEW;
        }
    }

    public void updateUI(){
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List <Crime> crimes = crimeLab.getCrimes();

        if (mAdapter == null){
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        }else{
            mAdapter.setCrimes(crimes);
            mAdapter.notifyItemChanged(position);
        }

        if (crimes.size() == 0){
            mLinerLayuotView.setVisibility(View.VISIBLE);
        }else{
            mLinerLayuotView.setVisibility(View.INVISIBLE);
        }

        updateSubtitle();
    }

    private void updateSubtitle(){
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int crimeCount = crimeLab.getCrimes().size();
        String subTitle = getResources().getQuantityString(R.plurals.subtitle_plural,crimeCount,crimeCount);

        if (!mSubtitleVisible){
            subTitle = null;
        }

        AppCompatActivity activity = (AppCompatActivity)getActivity();
        activity.getSupportActionBar().setTitle(subTitle);
    }

    public void createNewCrime() {
        Crime crime = new Crime();
        CrimeLab.get(getActivity()).addCrime(crime);

        mCallbacks.onCrimeSelected(crime);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list,container,false);
        mCrimeRecyclerView = view.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mLinerLayuotView = view.findViewById(R.id.for_empty);
        mCreateButton = view.findViewById(R.id.create_button);
        mCreateButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                createNewCrime();
            }
        });

        if (savedInstanceState != null){
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        updateUI();

        SimpleItemTouchHelperCallback callback = new SimpleItemTouchHelperCallback(mAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mCrimeRecyclerView);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE,mSubtitleVisible);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list,menu);

        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if (mSubtitleVisible){
            subtitleItem.setTitle(R.string.hide_subtitle);
        }else{
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.new_crime:
                createNewCrime();
                updateUI();
                return true;
            case R.id.show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
             default:
                 return super.onOptionsItemSelected(item);
        }
    }
}
