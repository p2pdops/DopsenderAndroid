package p2pdops.dopsender.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File

class ImagesViewModel : ViewModel() {


    private val TAG = "ImagesViewModel"

    val selectedImages: MutableLiveData<ArrayList<File>> = MutableLiveData(ArrayList())

    fun addSelectedImage(file: File) {
        selectedImages.value?.let {
            if (!it.contains(file)) {
                it.add(file)
            }
            return@let it
        }.apply { selectedImages.postValue(this) }
    }

    fun selectedImages(files: List<File>) {
        selectedImages.value!!.removeAll(files)
        selectedImages.value!!.addAll(files)
        selectedImages.postValue(selectedImages.value)
    }

    fun removeSelectedImage(file: File) {
        selectedImages.value?.let {
            if (it.contains(file))
                it.remove(file)
            return@let it
        }.apply {
            selectedImages.postValue(this)
        }
    }

    fun deSelectImageAll(files: List<File>) {
        selectedImages.value!!.removeAll(files)
        selectedImages.postValue(selectedImages.value)
    }


}