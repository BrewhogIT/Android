package com.bignerdranch.android.geoquiz;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private Button mTrueButton;
    private Button mFalseButton;
    private Button mCheatButton;
    private ImageButton mNextButton;
    private ImageButton mPrevButton;
    private TextView mQuestionTextView;
    private TextView mAttemptsTextView;
    private Toast myToast;

    private int mCurrentIndex = 0;
    private int mCorrectAnswer = 0;
    private int mAttemptCount = 3;
    private boolean mIsCheater;

    private static final String TAG = "MainActivity";
    private static final String KEY_INDEX = "index";
    private static final String KEY_ANSWER ="answer";
    private static final String KEY_CHEATER ="cheater";
    private static final String KEY_ATTEMPTS = "attempts";
    private static final int REQUEST_CODE_CHEAT = 0;

    private Question[] mQuestionBank = new Question[]{
            new Question(R.string.question_australia,true),
            new Question(R.string.question_oceans,true),
            new Question(R.string.question_mideast, false),
            new Question(R.string.question_africa,false),
            new Question(R.string.question_americas,true),
            new Question(R.string.question_asia,true)
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate() called");
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null){
            mCurrentIndex = savedInstanceState.getInt(KEY_INDEX,0);
            mCorrectAnswer = savedInstanceState.getInt(KEY_ANSWER,0);
            mIsCheater = savedInstanceState.getBoolean(KEY_CHEATER,false);
            mAttemptCount = savedInstanceState.getInt(KEY_ATTEMPTS, 3);
        }
        mCheatButton = findViewById(R.id.cheat_button);
        mCheatButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
                Intent intent = CheatActivity.newIntent(MainActivity.this,answerIsTrue);
                startActivityForResult(intent,REQUEST_CODE_CHEAT);
            }
        });

        mTrueButton = (Button) findViewById(R.id.true_button);
        mTrueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(true);
                mTrueButton.setEnabled(false);
                mFalseButton.setEnabled(false);
                mNextButton.setEnabled(true);

                if (mCurrentIndex == mQuestionBank.length-1){
                    showResult();
                    mCorrectAnswer = 0;
                    mAttemptCount = 3;
                }
            }
        });

        mFalseButton = (Button) findViewById(R.id.false_button);
        mFalseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(false);
                mTrueButton.setEnabled(false);
                mFalseButton.setEnabled(false);
                mNextButton.setEnabled(true);

                if (mCurrentIndex == mQuestionBank.length-1){
                    showResult();
                    mCorrectAnswer = 0;
                    mAttemptCount = 3;
                }
            }
        });

        mQuestionTextView = (TextView)findViewById(R.id.question_text_view);
        updateQuestion();
        updateAttemptCount();
        mQuestionTextView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                updateQuestion();
                updateAttemptCount();
            }
        });

        mNextButton = (ImageButton)findViewById(R.id.next_button);
        mNextButton.setEnabled(false);
        mNextButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                updateQuestion();
                updateAttemptCount();
                mTrueButton.setEnabled(true);
                mFalseButton.setEnabled(true);
                mNextButton.setEnabled(false);
                mIsCheater = false;
            }
        });

        mPrevButton = (ImageButton)findViewById(R.id.prev_button);
        mPrevButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mCurrentIndex = (mCurrentIndex - 1) % mQuestionBank.length;
                if (mCurrentIndex < 0) mCurrentIndex = mQuestionBank.length - 1;
                updateQuestion();
                updateAttemptCount();
            }
        });

    }

    @Override
    public void onStart(){
        super.onStart();
        Log.d(TAG,"onStart() called");
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(TAG,"onResume() called");
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.d(TAG,"onPause() called");
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.d(TAG,"onStop() called");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d(TAG,"onDestroy() called");
    }

    @Override
    public void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        Log.i(TAG,"onSaveInstanceState() called");
        bundle.putInt(KEY_INDEX,mCurrentIndex);
        bundle.putInt(KEY_ANSWER,mCorrectAnswer);
        bundle.putBoolean(KEY_CHEATER,mIsCheater);
        bundle.putInt(KEY_ATTEMPTS, mAttemptCount);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE_CHEAT){
            return;
        }

        if (resultCode != RESULT_OK){
            return;
        }

        mIsCheater = CheatActivity.wasAnswerShown(data);
        mAttemptCount --;
    }

    private void updateQuestion(){
        int question = mQuestionBank[mCurrentIndex].getTextResId();
        mQuestionTextView.setText(question);
    }

    private void updateAttemptCount(){
        mAttemptsTextView = findViewById(R.id.attempt_text_view);
        String messageAboutAttemptCount = getResources().getString(R.string.attempt_count)+ String.valueOf(mAttemptCount);
        mAttemptsTextView.setText(messageAboutAttemptCount);

        if (mAttemptCount < 1){
            mCheatButton.setEnabled(false);
        }else {
            mCheatButton.setEnabled(true);
        }
    }

    private void checkAnswer(boolean userPressedTrue){
        boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
        int messageResId = 0;

        if (mIsCheater){
            messageResId = R.string.judgment_toast;
        }else{
            if (answerIsTrue == userPressedTrue){
                messageResId = R.string.correct_toast;
                mCorrectAnswer ++;
            }else{
                messageResId = R.string.incorrect_toast;
            }
        }

        myToast = Toast.makeText(this,messageResId,Toast.LENGTH_SHORT);
        myToast.setGravity(Gravity.TOP,0,160);
        myToast.show();
    }

    private void showResult(){
        int percent = mCorrectAnswer * 100 / mQuestionBank.length;
        String answer = percent + "%";

        myToast = Toast.makeText(this,answer,Toast.LENGTH_SHORT);
        myToast.setGravity(Gravity.TOP,0,160);
        myToast.show();
    }
}
