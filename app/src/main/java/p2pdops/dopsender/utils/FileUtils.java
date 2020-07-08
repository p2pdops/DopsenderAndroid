package p2pdops.dopsender.utils;

import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.File;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.time.Duration;
import java.util.Locale;

public class FileUtils {


    public static Long getFileSize(String path) {
        return new File(path).length();
    }


    public static String humanMillis(Duration duration) {
        return duration.toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
    }

    public static Long getLastModified(String path) {
        return new File(path).lastModified();
    }

    public static Uri encodeOffsetLimit(Uri uri, int offset, int limit) {
        return uri.buildUpon().encodedQuery("limit=" + offset + "," + limit).build();
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
}
