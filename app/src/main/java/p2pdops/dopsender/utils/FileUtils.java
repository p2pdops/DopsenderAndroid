package p2pdops.dopsender.utils;

import android.os.Environment;
import android.os.StatFs;

import androidx.annotation.NonNull;

import java.io.File;
import java.time.Duration;

public class FileUtils {

    public static String humanizeMillis(Duration duration) {
        return duration.toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
    }

    public static Long getLastModified(String path) {
        return new File(path).lastModified();
    }

    public static String getNameByPath(@NonNull String path) {
        String result = "%20";
        int i = path.lastIndexOf('/');
        if (i > 0) result = path.substring(i + 1);
        return result;
    }

    public static String getExtensionByPath(@NonNull String path) {
        String result = "%20";
        int i = path.lastIndexOf('.');
        if (i > 0) result = path.substring(i + 1);
        return result;
    }


    public static boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return (availableBlocks * blockSize);
    }

    public static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        return (totalBlocks * blockSize);
    }
}
