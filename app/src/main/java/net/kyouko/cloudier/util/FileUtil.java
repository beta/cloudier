package net.kyouko.cloudier.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Util class for reading and writing files.
 *
 * @author beta
 */
public class FileUtil {

    private static File createTemporaryImageFile(Context context) throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "IMAGE_" + timestamp;

        File storageDirectory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDirectory);
    }


    public static File writeToTemporaryImageFile(Context context, InputStream inputStream) {
        OutputStream outputStream = null;
        File file = null;
        try {
            file = createTemporaryImageFile(context);
            outputStream = new FileOutputStream(file);

            int read;
            byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        } catch (IOException e) {
            return null;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }

        return file;
    }

}
