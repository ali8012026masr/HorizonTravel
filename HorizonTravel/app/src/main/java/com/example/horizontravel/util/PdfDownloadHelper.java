package com.example.horizontravel.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import android.graphics.pdf.PdfDocument;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class PdfDownloadHelper {
    private PdfDownloadHelper() {
    }

    public static void saveToDownloads(@NonNull Context context, @NonNull String fileName,
                                       @NonNull PdfDocument document) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveToPublicDownloads(context, fileName, document);
        } else {
            File downloads = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if (downloads == null) {
                throw new IOException("Downloads directory is unavailable");
            }
            if (!downloads.exists() && !downloads.mkdirs()) {
                throw new IOException("Could not create Downloads directory");
            }
            File file = new File(downloads, fileName);
            try (OutputStream outputStream = new FileOutputStream(file)) {
                document.writeTo(outputStream);
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private static void saveToPublicDownloads(Context context, String fileName,
                                              PdfDocument document) throws IOException {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
        values.put(MediaStore.Downloads.IS_PENDING, 1);

        Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            throw new IOException("Could not create Downloads file");
        }

        try {
            OutputStream outputStream = resolver.openOutputStream(uri);
            if (outputStream == null) {
                throw new IOException("Could not open Downloads file");
            }
            try (OutputStream stream = outputStream) {
                document.writeTo(stream);
            }

            ContentValues completed = new ContentValues();
            completed.put(MediaStore.Downloads.IS_PENDING, 0);
            resolver.update(uri, completed, null, null);
        } catch (IOException exception) {
            resolver.delete(uri, null, null);
            throw exception;
        }
    }
}
