package com.example.i2p.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.i2p.Model.SelectedImagesList;
import com.example.i2p.R;


/**
 * Adapter class for displaying images in a RecyclerView.
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private Context mContext;
    private SelectedImagesList images;
    private OnImageClickListener imageClickListener;

    /**
     * Constructor for the ImageAdapter.
     *
     * @param context The context of the activity or fragment.
     */
    public ImageAdapter(Context context) {
        mContext = context;
    }

    /**
     * Sets the list of images to be displayed in the adapter.
     *
     * @param images The list of image URIs.
     */
    public void setImages(SelectedImagesList images) {
        this.images = images;
        notifyDataSetChanged();
    }

    /**
     * Sets the listener for image click events.
     *
     * @param listener The listener to be set.
     */
    public void setOnImageClickListener(OnImageClickListener listener) {
        imageClickListener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for the RecyclerView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.imageselected, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        // Load the image using Glide library
        Uri imageUri = images.get(position);
        if (mContext != null) {
            Glide.with(mContext).load(imageUri).into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return images != null ? images.size() : 0;
    }

    /**
     * ViewHolder class for the images in the RecyclerView.
     */
    public class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        Button removeButton;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.list_item);
            removeButton = itemView.findViewById(R.id.removeButton);

            // Set click listener for the remove button
            removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && imageClickListener != null) {
                        imageClickListener.onRemoveClick(position);
                    }
                }
            });
        }
    }

    /**
     * Interface for handling image click events.
     */
    public interface OnImageClickListener {
        /**
         * Called when the remove button for an image is clicked.
         *
         * @param position The position of the clicked image in the list.
         */
        void onRemoveClick(int position);
    }
}
