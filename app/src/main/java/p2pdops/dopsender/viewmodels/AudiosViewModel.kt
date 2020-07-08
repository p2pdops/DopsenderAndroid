package p2pdops.dopsender.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File

class AudiosViewModel : ViewModel() {


    private val TAG = "AudiosViewModel"

    val selectedAudios: MutableLiveData<ArrayList<File>> = MutableLiveData(ArrayList())

    fun addSelectedAudio(file: File) {
        selectedAudios.value?.let {
            if (!it.contains(file)) {
                it.add(file)
            }
            return@let it
        }.apply { selectedAudios.postValue(this) }
    }

    fun selectedAllAudios(files: List<File>) {
        selectedAudios.value!!.removeAll(files)
        selectedAudios.value!!.addAll(files)
        selectedAudios.postValue(selectedAudios.value)
    }

    fun removeSelectedAudio(file: File) {
        selectedAudios.value?.let {
            if (it.contains(file))
                it.remove(file)
            return@let it
        }.apply {
            selectedAudios.postValue(this)
        }
    }

    fun deSelectAllAudios(files: List<File>) {
        selectedAudios.value!!.removeAll(files)
        selectedAudios.postValue(selectedAudios.value)
    }


}