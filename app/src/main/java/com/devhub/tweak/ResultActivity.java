package com.devhub.tweak;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.bitmap.BitmapDrawableResource;
import com.devhub.tweak.databinding.ActivityResultBinding;

import java.io.File;
import java.io.FileInputStream;

public class ResultActivity extends AppCompatActivity {

    ActivityResultBinding binding;
    private ImageView fullImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        fullImage = findViewById(R.id.editedImage);
        Log.i("status","image is received");
        if(getIntent().getData() != null) {
            Log.i("info","edited image is null");
            binding.editedImage.setImageURI(getIntent().getData());
        }
        Log.i("IFTrue","Image not set");

        //to move back oh the home screen
        binding.homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);

            }
        });

        binding.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable drawable = (BitmapDrawable) fullImage.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(),bitmap,"title",null);
                Uri uri = Uri.parse(bitmapPath);

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.putExtra(Intent.EXTRA_TEXT,"Checkout this image!");
                startActivity(Intent.createChooser(intent,"Share"));
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
    }
}