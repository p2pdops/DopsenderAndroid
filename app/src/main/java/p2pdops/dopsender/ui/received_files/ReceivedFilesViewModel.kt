package p2pdops.dopsender.ui.received_files

import android.os.Environment
import androidx.lifecycle.*
import kotlinx.coroutines.*
import p2pdops.dopsender.modals.FileType
import java.io.File

class ReceivedFilesViewModel : ViewModel() {

    private val dopsenderFolderPath =
        Environment.getExternalStorageDirectory().absolutePath + "/Dopsender/"

    private var docs: Array<File?> = arrayOf()
    private var apps: Array<File?> = arrayOf()
    private var images: Array<File?> = arrayOf()
    private var videos: Array<File?> = arrayOf()
    private var audios: Array<File?> = arrayOf()
    private var compressed: Array<File?> = arrayOf()

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                docs = File(dopsenderFolderPath + FileType.Documents).listFiles() ?: arrayOf()
                apps = File(dopsenderFolderPath + FileType.Apps).listFiles() ?: arrayOf()
                images = File(dopsenderFolderPath + FileType.Images).listFiles() ?: arrayOf()
                videos = File(dopsenderFolderPath + FileType.Videos).listFiles() ?: arrayOf()
                audios = File(dopsenderFolderPath + FileType.Audios).listFiles() ?: arrayOf()
                compressed =
                    File(dopsenderFolderPath + FileType.COMPRESSED).listFiles() ?: arrayOf()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

    }
}