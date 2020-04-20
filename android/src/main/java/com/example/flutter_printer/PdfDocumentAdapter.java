package com.example.flutter_printer;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.util.Log;
import androidx.annotation.RequiresApi;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class PdfDocumentAdapter extends PrintDocumentAdapter {
    private final String pdfPath;
    private final String pdfName;
    private boolean isSuccess;
    public PdfDocumentAdapter(String pdfPath, String pdfName) {
        this.pdfPath = pdfPath;
        this.pdfName = pdfName;
    }

    public boolean getSuccess() {
        return isSuccess;
    }

    @Override
    public void onLayout(PrintAttributes printAttributes, PrintAttributes printAttributes1,
                         CancellationSignal cancellationSignal, LayoutResultCallback layoutResultCallback,
                         Bundle bundle) {
        if(cancellationSignal.isCanceled()) {
            layoutResultCallback.onLayoutCancelled();
            isSuccess = false;
        } else {
            PrintDocumentInfo.Builder builder =
                    new PrintDocumentInfo.Builder(pdfName);
            builder.setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                    .build();
            layoutResultCallback.onLayoutFinished(builder.build(),
                    !printAttributes1.equals(printAttributes));
            isSuccess = true;
        }
    }

    @Override
    public void onWrite(PageRange[] pageRanges, ParcelFileDescriptor parcelFileDescriptor,
                        CancellationSignal cancellationSignal, WriteResultCallback writeResultCallback) {
        InputStream in = null;
        OutputStream out = null;
        try {
            File file = new File(pdfPath);
            in = new FileInputStream(file);
            out = new FileOutputStream(parcelFileDescriptor.getFileDescriptor());

            byte[] buf = new byte[16384];
            int size;
            while ((size = in.read(buf)) >=0 && !cancellationSignal.isCanceled()) {
                out.write(buf, 0, size);
            }
            if(cancellationSignal.isCanceled()) {
                writeResultCallback.onWriteCancelled();
            } else {
                writeResultCallback.onWriteFinished(new PageRange[]{
                        PageRange.ALL_PAGES
                });
            }
            isSuccess = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            isSuccess = false;
        } catch (IOException e) {
            e.printStackTrace();
            isSuccess = false;
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException e) {
                Log.e("printError", "onWrite:" + e);
                isSuccess = false;
            }
        }
    }
}
