package com.example.i2p.Model;

import android.net.Uri;
import java.util.ArrayList;
import java.util.List;

public class SelectedImagesList {
    List<Uri> selectedImages = new ArrayList<>();

    public void add(Uri photoUri){
        // Adds the given photoUri to the selectedImages list
        selectedImages.add(photoUri);
    }

    public void remove(int position){
        // Removes the item at the given position from the selectedImages list
        selectedImages.remove(position);
    }

    public Uri get(int position){
        // Returns the item at the given position from the selectedImages list
        return selectedImages.get(position);
    }

    public int size(){
        // Returns the number of items in the selectedImages list
        return selectedImages.size();
    }

    public boolean isEmpty(){
        // Checks if the selectedImages list is empty
        return selectedImages.isEmpty();
    }

    public List<Uri> getList(){
        // Returns the entire selectedImages list
        return selectedImages;
    }
}
