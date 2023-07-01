package com.example.i2p.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.i2p.R;
import com.example.i2p.Adapters.ImageOrderAdapter;
import com.example.i2p.Adapters.ItemTouchHelperCallback;

import java.util.ArrayList;
import java.util.List;

public class AdjustOrderActivity extends AppCompatActivity implements ImageOrderAdapter.ImageOrderListener {

    private List<Uri> selectedImages; // List to store the selected image URIs
    private ImageOrderAdapter imageOrderAdapter; // Adapter for the RecyclerView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjust_order);

        selectedImages = getIntent().getParcelableArrayListExtra("selectedImages"); // Retrieve the selected image URIs from the previous activity

        RecyclerView recyclerView = findViewById(R.id.recyclerView); // RecyclerView to display the selected images
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // Set the layout manager for the RecyclerView

        imageOrderAdapter = new ImageOrderAdapter(selectedImages, this); // Create a new ImageOrderAdapter with the selected images and listener
        recyclerView.setAdapter(imageOrderAdapter); // Set the adapter for the RecyclerView

        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(imageOrderAdapter); // Callback for drag and drop functionality
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback); // ItemTouchHelper for handling drag and drop
        itemTouchHelper.attachToRecyclerView(recyclerView); // Attach ItemTouchHelper to the RecyclerView

        Button doneButton = findViewById(R.id.buttonDone); // Button to finish adjusting the image order
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pass the updated image order back to the PdfGenerationActivity
                ArrayList<Uri> adjustedOrder = imageOrderAdapter.getImageUris(); // Get the updated image order from the adapter
                Intent resultIntent = new Intent();
                resultIntent.putParcelableArrayListExtra("adjustedOrder", adjustedOrder); // Put the updated image order as an extra in the result intent
                setResult(RESULT_OK, resultIntent); // Set the result of the activity as RESULT_OK with the result intent
                finish(); // Finish the activity and return to the previous activity
            }
        });
    }

    @Override
    public void onImageOrderChanged(int fromPosition, int toPosition) {
        imageOrderAdapter.onItemMove(fromPosition, toPosition); // Called when the image order is changed in the adapter
    }
}
