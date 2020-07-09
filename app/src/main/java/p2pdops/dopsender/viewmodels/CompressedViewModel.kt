package p2pdops.dopsender.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File

class CompressedViewModel : ViewModel() {


    private val TAG = "CompressedViewModel"

    val selectedCompressed: MutableLiveData<ArrayList<File>> = MutableLiveData(ArrayList())

    fun addSelectedAudio(file: File) {
        selectedCompressed.value?.let {
            if (!it.contains(file)) {
                it.add(file)
            }
            return@let it
        }.apply { selectedCompressed.postValue(this) }
    }

    fun selectedAllCompressed(files: List<File>) {
        selectedCompressed.value!!.removeAll(files)
        selectedCompressed.value!!.addAll(files)
        selectedCompressed.postValue(selectedCompressed.value)
    }

    fun removeSelectedAudio(file: File) {
        selectedCompressed.value?.let {
            if (it.contains(file))
                it.remove(file)
            return@let it
        }.apply {
            selectedCompressed.postValue(this)
        }
    }

    fun deSelectAllCompressed(files: List<File>) {
        selectedCompressed.value!!.removeAll(files)
        selectedCompressed.postValue(selectedCompressed.value)
    }


}