package com.example.assignment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.MediaController;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.assignment.databinding.ActivityMediaBinding;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MediaActivity extends AppCompatActivity {

    private String TAG = MediaActivity.class.getSimpleName();

    private ActivityMediaBinding binding;
    int currentPage = 0;
    Timer timer;
    final long DELAY_MS = 500;//delay in milliseconds before task is to be executed
    final long PERIOD_MS = 2500; // time in milliseconds between successive task executions.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_media);

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(binding.videoView);

        binding.videoView.setMediaController(mediaController);

        Intent intent = getIntent();
        String contentType = intent.getStringExtra("type");
        if (contentType.equals("video")) {

            binding.viewPager.setVisibility(View.GONE);
            binding.videoView.setVisibility(View.VISIBLE);
            binding.stopBtn.setVisibility(View.VISIBLE);
            Uri uri = Uri.parse(intent.getStringExtra("data"));
            binding.videoView.setVideoURI(uri);
            binding.videoView.requestFocus();
            binding.videoView.start();
        } else {
            binding.videoView.setVisibility(View.GONE);
            if (binding.videoView.isPlaying()) {
                binding.videoView.stopPlayback();
            }
            binding.stopBtn.setVisibility(View.GONE);
            binding.viewPager.setVisibility(View.VISIBLE);


            ArrayList<String> arrayList = intent.getStringArrayListExtra("images");

            binding.viewPager.setAdapter(new ImagePagerAdapter(this,arrayList));
            int NUM_PAGES = arrayList.size() +1;
            /*After setting the adapter use the timer */
            Handler handler = new Handler();
            Runnable Update = () -> {
                if (currentPage == NUM_PAGES-1) {
                    currentPage = 0;
                }
                binding.viewPager.setCurrentItem(currentPage++, true);
            };

            timer = new Timer(); // This will create a new Thread
            timer.schedule(new TimerTask() { // task to be scheduled
                @Override
                public void run() {
                    handler.post(Update);
                }
            }, DELAY_MS, PERIOD_MS);
        }

        binding.stopBtn.setOnClickListener(v -> binding.videoView.stopPlayback());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding.videoView.getVisibility() == View.VISIBLE)
            binding.videoView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.videoView.stopPlayback();
    }
}