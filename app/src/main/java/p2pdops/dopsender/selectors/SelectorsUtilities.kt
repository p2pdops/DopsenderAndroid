package p2pdops.dopsender.selectors

import android.net.Uri
import android.provider.MediaStore
import p2pdops.dopsender.modals.FileType

fun getExtensionsFilter(fileType: FileType): Array<String> = when (fileType) {
    FileType.Documents -> arrayOf("pdf", "csv", "doc", "docx", "xls", "xlsx")
    FileType.Apps -> arrayOf("apk", "aab")
    FileType.Images -> arrayOf("jpg", "jpeg", "png")
    FileType.Videos -> arrayOf("mp4", "avi", "3gp", "ts", "webm", "mkv", "mov")
    FileType.Audios -> arrayOf("mp3", "wav", "ogg")
    FileType.Compressed -> arrayOf("zip", "rar", "obb", "7z")
}

fun getFilesUri(fileType: FileType): Uri = when (fileType) {
    FileType.Documents -> MediaStore.Files.getContentUri("external")
    FileType.Compressed -> MediaStore.Files.getContentUri("external")
    FileType.Apps -> MediaStore.Files.getContentUri("external")
    FileType.Images -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    FileType.Videos -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    FileType.Audios -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
}

/* SELECT * FROM files where(
prefix : "_data LIKE '%", iteration: ~type~, separator: "' OR _data LIKE '%", postfix: "'"
)*/
fun getFilesSelection(fileType: FileType): String? = getExtensionsFilter(fileType).joinToString(
    "' OR _data LIKE '%", "_data LIKE '%", "'"
)
