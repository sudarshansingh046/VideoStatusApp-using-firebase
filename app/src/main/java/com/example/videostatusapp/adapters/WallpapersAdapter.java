package com.example.videostatusapp.adapters;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.videostatusapp.BuildConfig;
import com.example.videostatusapp.R;
import com.example.videostatusapp.activities.WallpapersActivity;
import com.example.videostatusapp.models.Wallpaper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Belal on 4/21/2018.
 */

public class WallpapersAdapter extends RecyclerView.Adapter<WallpapersAdapter.WallpaperViewHolder> {

    private Context mCtx;
    private List<Wallpaper> wallpaperList;

    public WallpapersAdapter(Context mCtx, List<Wallpaper> wallpaperList) {
        this.mCtx = mCtx;
        this.wallpaperList = wallpaperList;
    }

    @Override
    public WallpaperViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.recyclerview_wallpapers, parent, false);
        return new WallpaperViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final WallpaperViewHolder holder, int position) {
        Wallpaper w = wallpaperList.get(position);
        holder.textViewTitle.setText(w.title);
        holder.textViewdescription.setText(w.desc);
        holder.videoView.setVideoPath(w.url);
        holder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                holder.videoprogressbar.setVisibility(View.GONE);

                float videoRatio = mp.getVideoWidth() / (float) mp.getVideoHeight();
                float screenRatio = holder.videoView.getWidth() / (float) holder.videoView.getHeight();

                float scale = videoRatio / screenRatio;
                if (scale >= 10f) {
                    holder.videoView.setScaleX(scale);
                } else {
                    holder.videoView.setScaleY(1.2f / scale);
                }
                mp.start();
            }
        });
        holder.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.start();
            }
        });

        /*Glide.with(mCtx)
                .load(w.url)
                .into();*/

        if (w.isFavourite) {
            holder.checkBoxFav.setChecked(true);
        }
    }

    @Override
    public int getItemCount() {
        return wallpaperList.size();
    }

    class WallpaperViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

        VideoView videoView;
        TextView textViewTitle, textViewdescription;
        ProgressBar videoprogressbar;
        CheckBox checkBoxFav;
        ImageButton buttonShare, buttonDownload;


        public WallpaperViewHolder(View itemView) {
            super(itemView);
            videoView = itemView.findViewById(R.id.videoview2);
            textViewTitle = itemView.findViewById(R.id.textvideotitle);
            textViewdescription = itemView.findViewById(R.id.textvideodescription);
            videoprogressbar = itemView.findViewById(R.id.videoprogressbar);


            checkBoxFav = itemView.findViewById(R.id.checkbox_favourite);
            buttonShare = itemView.findViewById(R.id.button_share);
            buttonDownload = itemView.findViewById(R.id.button_download);

            checkBoxFav.setOnCheckedChangeListener(this);
            buttonShare.setOnClickListener(this);
            buttonDownload.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            switch (view.getId()) {
                case R.id.button_share:
                    shareWallpaper(wallpaperList.get(getAdapterPosition()).url);

                    break;
                case R.id.button_download:

                    downloadWallpaper(wallpaperList.get(getAdapterPosition()).url);

                    break;

            }

        }

        private void shareWallpaper(String w) {
            ((Activity) mCtx).findViewById(R.id.progressbar).setVisibility(View.VISIBLE);

            Intent share = new Intent(android.content.Intent.ACTION_SEND);
            share.setType("text/plain");
            share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            share.putExtra(Intent.EXTRA_SUBJECT, "Title Of The Post");
            share.putExtra(Intent.EXTRA_TEXT, w);

            mCtx.startActivity(Intent.createChooser(share, "Share link!"));


        }






        private void downloadWallpaper(final String wallpaper) {
            ((Activity) mCtx).findViewById(R.id.progressbar).setVisibility(View.VISIBLE);

            if(ActivityCompat.checkSelfPermission(mCtx, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(mCtx, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                // this will request for permission when user has not granted permission for the app
                ActivityCompat.requestPermissions((Activity)mCtx, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }

            else{
                //Download Script

                DownloadManager downloadManager = (DownloadManager) mCtx.getSystemService(Context.DOWNLOAD_SERVICE);
                Uri uri = Uri.parse(wallpaper);
                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setVisibleInDownloadsUi(true);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir("/video_status", uri.getLastPathSegment());
                downloadManager.enqueue(request);
            }
        }



        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(mCtx, "Please login first...", Toast.LENGTH_LONG).show();
                compoundButton.setChecked(false);
                return;
            }


            int position = getAdapterPosition();
            Wallpaper w = wallpaperList.get(position);


            DatabaseReference dbFavs = FirebaseDatabase.getInstance().getReference("users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("favourites")
                    .child(w.category);

            if (b) {
                dbFavs.child(w.id).setValue(w);
            } else {
                dbFavs.child(w.id).setValue(null);
            }
        }
    }
}


