package com.example.i2p.ViewModel;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;

import com.example.i2p.Adapters.ImageAdapter;
import com.example.i2p.Model.SelectedImagesList;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageSelectionHandler {

    private static final int REQUEST_CODE_PERMISSION = 1;
    private static final int REQUEST_CODE_CAMERA = 2;
    private static final int REQUEST_CODE_GALLERY = 3;

    private Context context;
    private ImageAdapter imageAdapter;
    private SelectedImagesList selectedImages;

    private String currentPhotoPath;

    public ImageSelectionHandler(Context context, ImageAdapter imageAdapter, SelectedImagesList selectedImages) {
        this.context = context;
        this.imageAdapter = imageAdapter;
        this.selectedImages = selectedImages;
    }

    /**
     * Checks and requests the required permissions.
     * If the permission is already granted, proceeds with image selection.
     * Otherwise, requests the permission.
     */
    public void checkAndRequestPermission() {
        if (checkPermission()) {
            // Permission is already granted, proceed with image selection
        } else {
            requestPermission();
        }
    }

    /**
     * Checks if the required permissions are granted.
     *
     * @return true if both camera and storage permissions are granted, false otherwise.
     */
    private boolean checkPermission() {
        int cameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        int storagePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return cameraPermission == PackageManager.PERMISSION_GRANTED &&
                storagePermission == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Requests the required permissions.
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions((AppCompatActivity) context,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_CODE_PERMISSION);
    }

    /**
     * Opens the camera to capture an image.
     * If the camera is not available, displays a toast message.
     */
    public void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            File photoFile;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("ImageSelectionActivity", "Error creating image file: " + ex.getMessage());
                return;
            }

            // Continue if the File was successfully created
            Uri photoUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            ((AppCompatActivity) context).startActivityForResult(intent, REQUEST_CODE_CAMERA);
        } else {
            Toast.makeText(context, "Camera is not available", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Retrieves the URI of the camera photo.
     *
     * @return the URI of the camera photo, or null if it doesn't exist.
     */
    public Uri getCameraPhotoUri() {
        String imagePath = currentPhotoPath;
        if (imagePath != null) {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                return Uri.fromFile(imageFile);
            }
        }
        return null;
    }

    /**
     * Creates a file to store the captured image.
     *
     * @return the created image file.
     * @throws IOException if an error occurs while creating the file.
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";

        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File imageFile = new File(storageDir, imageFileName);
        currentPhotoPath = imageFile.getAbsolutePath();

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATA, imageFile.getAbsolutePath());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        return imageFile;
    }

    /**
     * Opens the gallery to select images.
     * If the gallery is not available, displays a toast message.
     */
    public void openGallery() {
        try {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            ((AppCompatActivity) context).startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE_GALLERY);
        } catch (Exception e) {
            Toast.makeText(context, "Gallery is not available", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Initiates the image selection process.
     * If the required permissions are granted, opens the gallery to select images.
     * Otherwise, requests the permission.
     */
    public void selectImages() {
        if (checkPermission()) {
            // Permission is granted, proceed with image selection
            openGallery();
        } else {
            requestPermission();
        }
    }

    /**
     * Removes an image from the selected images list.
     *
     * @param position the position of the image to remove.
     */
    public void removeImage(int position) {
        selectedImages.remove(position);
        imageAdapter.notifyDataSetChanged();
    }

    /**
     * Handles the result of activity results (e.g., image capture or image selection).
     *
     * @param requestCode the request code.
     * @param resultCode  the result code.
     * @param data        the intent data.
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAMERA) {
                Uri photoUri = getCameraPhotoUri();

                if (photoUri != null) {
                    if (validateImageFormat(photoUri)) {
                        selectedImages.add(photoUri);
                        imageAdapter.setImages(selectedImages);
                    } else {
                        Toast.makeText(context, "Invalid image format", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Error: Null photoUri", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_CODE_GALLERY) {
                if (data != null) {
                    if (data.getClipData() != null) {
                        int count = data.getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri imageUri = data.getClipData().getItemAt(i).getUri();
                            if (validateImageFormat(imageUri)) {
                                selectedImages.add(imageUri);
                            } else {
                                Log.d("ImageSelectionActivity", "Invalid image format: " + imageUri);
                            }
                        }
                    } else if (data.getData() != null) {
                        Uri imageUri = data.getData();
                        if (validateImageFormat(imageUri)) {
                            selectedImages.add(imageUri);
                        } else {
                            Toast.makeText(context, "Invalid image format", Toast.LENGTH_SHORT).show();
                        }
                    }
                    imageAdapter.setImages(selectedImages);
                } else {
                    Toast.makeText(context, "Error: Null data", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Validates the format of the selected image.
     *
     * @param imageUri the URI of the selected image.
     * @return true if the image format is valid, false otherwise.
     */
    private boolean validateImageFormat(Uri imageUri) {
        ContentResolver contentResolver = context.getContentResolver();
        try {
            InputStream inputStream = contentResolver.openInputStream(imageUri);
            if (inputStream != null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, null, options);
                return options.outMimeType != null && options.outMimeType.startsWith("image/");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Handles the result of permission requests.
     *
     * @param requestCode  the request code.
     * @param permissions  the requested permissions.
     * @param grantResults the grant results for the corresponding permissions.
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                selectImages();
            } else {
                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
