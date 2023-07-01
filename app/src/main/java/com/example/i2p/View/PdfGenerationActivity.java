package com.example.i2p.View;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.i2p.R;
import com.example.i2p.Adapters.ImageOrderAdapter;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.element.Image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.List;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;


public class PdfGenerationActivity extends AppCompatActivity implements ImageOrderAdapter.ImageOrderListener {
    private static final int ADJUST_ORDER_REQUEST_CODE = 1002;
    private List<Uri> selectedImages; // List to store the selected image URIs

    private Uri createdPdfUri; // URI of the generated PDF file
    private List<Uri> adjustedOrderImages; // List to store the adjusted order of images
    private ImageOrderAdapter imageOrderAdapter; // Adapter for the RecyclerView
    private RecyclerView recyclerView; // RecyclerView to display the selected images

    private EditText editTextFileName; // EditText for entering the file name
    private Button buttonSavePDF; // Button to save the generated PDF

    private ProgressBar progressBar; // ProgressBar to show the progress of PDF generation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_generation);
        selectedImages = getIntent().getParcelableArrayListExtra("selectedImages"); // Retrieve the selected image URIs from the previous activity
        imageOrderAdapter = new ImageOrderAdapter(selectedImages, this); // Create a new ImageOrderAdapter with the selected images and listener
        editTextFileName = findViewById(R.id.editTextFileName); // EditText for entering the file name
        buttonSavePDF = findViewById(R.id.buttonSavePDF); // Button to save the generated PDF
        progressBar = findViewById(R.id.progressBar); // ProgressBar to show the progress of PDF generation

        Button adjustOrderButton = findViewById(R.id.buttonAdjustOrder);
        adjustOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the activity to adjust the order of images
                Intent adjustOrderIntent = new Intent(PdfGenerationActivity.this, AdjustOrderActivity.class);
                adjustOrderIntent.putParcelableArrayListExtra("selectedImages", (ArrayList<? extends Parcelable>) selectedImages);
                startActivityForResult(adjustOrderIntent, ADJUST_ORDER_REQUEST_CODE);
            }
        });

        Button generatePdfButton = findViewById(R.id.generatepdf);
        generatePdfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generatePdf(selectedImages);
            }
        });

        buttonSavePDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePdfWithFileName();
            }
        });

    }

    private void generatePdf(List<Uri> adjustedOrder) {
        if (adjustedOrder.size() > 0) {
            progressBar.setVisibility(View.VISIBLE);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String pdfFileName = editTextFileName.getText().toString().trim();
                        if (pdfFileName.isEmpty()) {
                            pdfFileName = "GeneratedPDF_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                        }
                        pdfFileName += ".pdf";

                        File pdfFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), pdfFileName);

                        OutputStream outputStream = new FileOutputStream(pdfFile);

                        PdfWriter writer = new PdfWriter(outputStream);
                        com.itextpdf.kernel.pdf.PdfDocument pdfDocument = new com.itextpdf.kernel.pdf.PdfDocument(writer);
                        Document document = new Document(pdfDocument, PageSize.A4);

                        for (int i = 0; i < adjustedOrder.size(); i++) {
                            Uri imageUri = adjustedOrder.get(i);
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

                            if (bitmap != null) {
                                Image image = new Image(ImageDataFactory.create(toByteArray(bitmap)));

                                // Scale the image to fit the page size
                                image.setAutoScale(true);

                                // Add the image to the document
                                document.add(image);

                                // Add AreaBreak after each image except the last one
                                if (i < adjustedOrder.size() - 1) {
                                    document.add(new com.itextpdf.layout.element.AreaBreak());
                                }
                            }
                        }

                        document.close();
                        outputStream.close();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(PdfGenerationActivity.this, "PDF generated successfully", Toast.LENGTH_SHORT).show();

                                createdPdfUri = Uri.fromFile(pdfFile);

                                MediaScannerConnection.scanFile(PdfGenerationActivity.this, new String[]{pdfFile.getAbsolutePath()}, null, null);

                                displayPdfPreview(pdfFile);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(PdfGenerationActivity.this, "Error generating PDF", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).start();
        }
    }

    private byte[] toByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 70, stream);
        return stream.toByteArray();
    }

    private void savePdfWithFileName() {
        String fileName = editTextFileName.getText().toString().trim();

        if (!fileName.isEmpty()) {
            if (createdPdfUri != null) {
                // Get the directory path for saving the PDF
                File pdfDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "PDFs");

                if (!pdfDirectory.exists()) {
                    pdfDirectory.mkdirs();
                }

                // Create a new file with the specified file name
                File pdfFile = new File(pdfDirectory, fileName + ".pdf");

                try {
                    // Copy the generated PDF to the new file location
                    InputStream inputStream = getContentResolver().openInputStream(createdPdfUri);
                    OutputStream outputStream = new FileOutputStream(pdfFile);

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    outputStream.close();
                    inputStream.close();

                    // Show a success message to the user
                    Toast.makeText(this, "PDF saved successfully", Toast.LENGTH_SHORT).show();

                    // Scan the new PDF file so it becomes available in the device's file system
                    MediaScannerConnection.scanFile(this, new String[]{pdfFile.getAbsolutePath()}, null, null);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error saving PDF", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No PDF file available", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please enter a file name", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayPdfPreview(File pdfFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = FileProvider.getUriForFile(this, "com.example.i2p.fileprovider", pdfFile);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADJUST_ORDER_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                ArrayList<Uri> adjustedOrder = data.getParcelableArrayListExtra("adjustedOrder");

                if (adjustedOrder != null) {
                    // Use the adjusted image order to generate the PDF
                    Button generatePdfButton = findViewById(R.id.generatepdf);
                    generatePdfButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            generatePdf(adjustedOrder);
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onImageOrderChanged(int fromPosition, int toPosition) {
        // Swap the positions of the images in the adjustedOrderImages list
        Uri image = adjustedOrderImages.remove(fromPosition);
        adjustedOrderImages.add(toPosition, image);

        // Notify the adapter of the data change
        imageOrderAdapter.notifyItemMoved(fromPosition, toPosition);
    }
}