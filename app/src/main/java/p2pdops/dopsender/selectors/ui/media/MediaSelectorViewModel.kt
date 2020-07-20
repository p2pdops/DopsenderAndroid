package p2pdops.dopsender.selectors.ui.media

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File

class MediaSelectorViewModel : ViewModel() {

    val allSelectionChange: MutableLiveData<Int> = MutableLiveData(1)

    val selectedFiles: MutableLiveData<ArrayList<File>> = MutableLiveData(ArrayList())

    fun selectFile(file: File) {
        selectedFiles.value?.let {
            if (!it.contains(file)) {
                it.add(file)
            }
            return@let it
        }.apply { selectedFiles.postValue(this) }
    }

    fun selectFiles(files: List<File>) {
        selectedFiles.value!!.removeAll(files)
        selectedFiles.value!!.addAll(files)
        selectedFiles.postValue(selectedFiles.value)
    }

    fun deSelectFile(file: File) {
        selectedFiles.value?.let {
            if (it.contains(file))
                it.remove(file)
            return@let it
        }.apply {
            selectedFiles.postValue(this)
        }
    }

    fun deSelectFiles(files: List<File>) {
        selectedFiles.value!!.removeAll(files)
        selectedFiles.postValue(selectedFiles.value)
    }

    fun allSelectedChanged() {
        allSelectionChange.postValue(allSelectionChange.value?.plus(1))
    }

}