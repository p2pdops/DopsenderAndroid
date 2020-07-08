package p2pdops.dopsender.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File

class DocsViewModel : ViewModel() {


    private val TAG = "DocsViewModel"

    val selectedDocs: MutableLiveData<ArrayList<File>> = MutableLiveData(ArrayList())

    fun addSelectedAudio(file: File) {
        selectedDocs.value?.let {
            if (!it.contains(file)) {
                it.add(file)
            }
            return@let it
        }.apply { selectedDocs.postValue(this) }
    }

    fun selectedAllDocs(files: List<File>) {
        selectedDocs.value!!.removeAll(files)
        selectedDocs.value!!.addAll(files)
        selectedDocs.postValue(selectedDocs.value)
    }

    fun removeSelectedAudio(file: File) {
        selectedDocs.value?.let {
            if (it.contains(file))
                it.remove(file)
            return@let it
        }.apply {
            selectedDocs.postValue(this)
        }
    }

    fun deSelectAllDocs(files: List<File>) {
        selectedDocs.value!!.removeAll(files)
        selectedDocs.postValue(selectedDocs.value)
    }


}