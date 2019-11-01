package com.bignerdranch.android.beatbox;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.databinding.DataBindingUtil;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.Button;

import com.bignerdranch.android.beatbox.databinding.FragmentBeatBoxBinding;
import com.bignerdranch.android.beatbox.databinding.ListItemSoundBinding;

import java.util.List;

public class BeatBoxFragment extends Fragment{
    private BeatBox mBeatBox;
    private View mViewToReveal;
    private static final String TAG = "BeatBoxFragment";

    public static BeatBoxFragment newInstance(){
        return new BeatBoxFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mBeatBox = new BeatBox(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentBeatBoxBinding binding = DataBindingUtil
                .inflate(inflater,R.layout.fragment_beat_box,container,false);

        binding.recyclerView.setLayoutManager(
                new GridLayoutManager(getActivity(),3));
        binding.recyclerView.setAdapter(new SoundAdapter(mBeatBox.getSounds()));

        mViewToReveal = binding.redField;
        return binding.getRoot();
    }

    private class SoundHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ListItemSoundBinding mBinding;
        private Button mButton;

        public SoundHolder(@NonNull ListItemSoundBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
            mBinding.setViewModel(new SoundViewModel(mBeatBox));
            mButton = mBinding.button;
            mButton.setOnClickListener(this);
        }

        public void bind(Sound sound){
            mBinding.getViewModel().setSound(sound);
            mBinding.executePendingBindings();
        }

        @Override
        public void onClick(View clickSource) {

            //Нахождение экранных координат view
            int[] clickCoords = new int[2];
            clickSource.getLocationOnScreen(clickCoords);

            //Вычисляем центральную точку view
            clickCoords[0] += clickSource.getWidth() / 2;
            clickCoords[1] += clickSource.getHeight() / 2;
            Log.i(TAG,"Button cords is: X- " + clickCoords[0] + " Y- " + clickCoords[1] );

            performRevealAnimation(mViewToReveal,clickCoords[0],clickCoords[1]);
        }

        private void performRevealAnimation(final View view
                , int screenCenterX, int screenCenterY){

            // Нахождение центра относительно представления,
            // к которому будет применяться анимация
            int[] animatingViewCoords = new int[2];
            view.getLocationOnScreen(animatingViewCoords);
            int centerX = screenCenterX - animatingViewCoords[0];
            int centerY = screenCenterY - animatingViewCoords[1];
            Log.i(TAG,"View cords is: X- " + centerX + " Y- " + centerY);

            //Определение максимального радиуса
            Point size = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(size);
            int maxRadius = size.y;

            //Создание и запуск анимации
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                view.setVisibility(View.VISIBLE);

                Animator animator = ViewAnimationUtils
                        .createCircularReveal(view,centerX,centerY
                                ,0,maxRadius);

                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(View.INVISIBLE);
                    }
                });
                   animator.start();
            }

        }
    }

    private class SoundAdapter extends RecyclerView.Adapter<SoundHolder>{
        private List<Sound> mSounds;

        public SoundAdapter(List<Sound> sounds) {
            mSounds = sounds;
        }

        @NonNull
        @Override
        public SoundHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            ListItemSoundBinding binding = DataBindingUtil
                    .inflate(inflater,R.layout.list_item_sound,viewGroup,false);
            return new SoundHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull SoundHolder soundHolder, int i) {
            Sound sound = mSounds.get(i);
            soundHolder.bind(sound);
        }

        @Override
        public int getItemCount() {
            return mSounds.size();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBeatBox.release();
    }
}
