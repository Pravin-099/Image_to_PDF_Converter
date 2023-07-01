package com.example.i2p.Adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.i2p.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImageOrderAdapter extends RecyclerView.Adapter<ImageOrderAdapter.ImageOrderViewHolder> implements ItemTouchHelperAdapter {

    // List of image URIs
    private List<Uri> imageUris;

    // Listener for image order changes
    private ImageOrderListener listener;

    public ImageOrderAdapter(List<Uri> imageUris, ImageOrderListener listener) {
        this.imageUris = imageUris;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item in the RecyclerView
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_order, parent, false);
        return new ImageOrderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageOrderViewHolder holder, int position) {
        // Bind the image URI to the ImageView using Picasso library for image loading
        Uri imageUri = imageUris.get(position);
        Picasso.get()
                .load(imageUri)
                .fit()
                .centerCrop()
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        // Return the total number of items in the RecyclerView
        return imageUris.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        // Reorder the image URIs when an item is moved
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(imageUris, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(imageUris, i, i - 1);
            }
        }
        // Notify the adapter about the item move
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        // Not implemented for image ordering
    }

    // ViewHolder class for each item in the RecyclerView
    public class ImageOrderViewHolder extends RecyclerView.ViewHolder {

        // ImageView to display the image
        ImageView imageView;

        public ImageOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize the ImageView from the item layout
            imageView = itemView.findViewById(R.id.imageView);
        }
    }

    // Interface for image order changes
    public interface ImageOrderListener {
        void onImageOrderChanged(int fromPosition, int toPosition);
    }

    // Method to get a copy of the image URIs as an ArrayList
    public ArrayList<Uri> getImageUris() {
        return new ArrayList<>(imageUris);
    }
}
