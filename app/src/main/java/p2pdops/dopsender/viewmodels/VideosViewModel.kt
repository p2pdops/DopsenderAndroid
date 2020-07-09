package p2pdops.dopsender.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File

class VideosViewModel : ViewModel() {


    private val TAG = "VideosViewModel"

    val selectedVideos: MutableLiveData<ArrayList<File>> = MutableLiveData(ArrayList())

    fun addSelectedVideo(file: File) {
        selectedVideos.value?.let {
            if (!it.contains(file)) {
                it.add(file)
            }
            return@let it
        }.apply { selectedVideos.postValue(this) }
    }

    fun selectedAllVideos(files: List<File>) {
        selectedVideos.value!!.removeAll(files)
        selectedVideos.value!!.addAll(files)
        selectedVideos.postValue(selectedVideos.value)
    }

    fun removeSelectedVideo(file: File) {
        selectedVideos.value?.let {
            if (it.contains(file))
                it.remove(file)
            return@let it
        }.apply {
            selectedVideos.postValue(this)
        }
    }

    fun deSelectVideoAll(files: List<File>) {
        selectedVideos.value!!.removeAll(files)
        selectedVideos.postValue(selectedVideos.value)
    }


}