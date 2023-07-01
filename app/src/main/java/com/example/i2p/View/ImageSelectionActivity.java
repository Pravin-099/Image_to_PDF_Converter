package com.example.i2p.View;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.i2p.Adapters.ImageAdapter;
import com.example.i2p.Model.SelectedImagesList;
import com.example.i2p.R;
import com.example.i2p.ViewModel.ImageSelectionHandler;

import java.util.ArrayList;
import java.util.List;

public class ImageSelectionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;  // RecyclerView to display the selected images
    private ImageAdapter imageAdapter;  // Adapter for the RecyclerView
    private SelectedImagesList selectedImages;  // List to store the selected images

    private ImageSelectionHandler imageSelectionHandler;  // Handles image selection logic

    private static final int REQUEST_CODE_PERMISSION = 1;  // Constant to identify permission request

    private String currentPhotoPath;  // Stores the file path of the current photo taken

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerViewImages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        imageAdapter = new ImageAdapter(this);
        recyclerView.setAdapter(imageAdapter);

        selectedImages = new SelectedImagesList();

        imageSelectionHandler = new ImageSelectionHandler(this, imageAdapter, selectedImages);

        if (checkPermission()) {
            // Permission is already granted, proceed with image selection
        } else {
            imageSelectionHandler.checkAndRequestPermission();
        }

        Button cameraButton = findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageSelectionHandler.openCamera();
            }
        });

        Button galleryButton = findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageSelectionHandler.openGallery();
            }
        });

        imageAdapter.setOnImageClickListener(new ImageAdapter.OnImageClickListener() {
            @Override
            public void onRemoveClick(int position) {
                imageSelectionHandler.removeImage(position);
            }
        });

        Button donePdfButton = findViewById(R.id.done);
        donePdfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedImages.isEmpty()) {
                    Toast.makeText(ImageSelectionActivity.this, "No Images Selected", Toast.LENGTH_SHORT).show();
                } else {
                    navigateToPdfGenerationActivity(selectedImages.getList());
                }
            }
        });
    }

    // Method to navigate to the PdfGenerationActivity
    private void navigateToPdfGenerationActivity(List<Uri> selectedImages) {
        Intent intent = new Intent(this, PdfGenerationActivity.class);
        intent.putParcelableArrayListExtra("selectedImages", (ArrayList<? extends Parcelable>) selectedImages);
        startActivity(intent);
    }

    // Check if the required permissions are granted
    private boolean checkPermission() {
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return cameraPermission == PackageManager.PERMISSION_GRANTED &&
                storagePermission == PackageManager.PERMISSION_GRANTED;
    }

    // Request the required permissions
    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_CODE_PERMISSION);
    }

    // Handle the result of permission requests
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                imageSelectionHandler.selectImages();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Handle the result of activity results (e.g., image capture or image selection)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imageSelectionHandler.onActivityResult(requestCode, resultCode, data);
    }
}

