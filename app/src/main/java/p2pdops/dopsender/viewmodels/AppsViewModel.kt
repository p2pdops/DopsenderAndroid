package p2pdops.dopsender.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import p2pdops.dopsender.modals.AppData
import java.io.File

class AppsViewModel : ViewModel() {


    private val TAG = "AppsViewModel"

    val selectedApps: MutableLiveData<ArrayList<AppData>> = MutableLiveData(ArrayList())

    fun addSelectedApps(file: AppData) {
        selectedApps.value?.let {
            if (!it.contains(file)) {
                it.add(file)
            }
            return@let it
        }.apply { selectedApps.postValue(this) }
    }

    fun selectedAllApps(files: List<AppData>) {
        selectedApps.value!!.removeAll(files)
        selectedApps.value!!.addAll(files)
        selectedApps.postValue(selectedApps.value)
    }

    fun removeSelectedApps(file: AppData) {
        selectedApps.value?.let {
            if (it.contains(file))
                it.remove(file)
            return@let it
        }.apply {
            selectedApps.postValue(this)
        }
    }

    fun deSelectAllApps(files: List<AppData>) {
        selectedApps.value!!.removeAll(files)
        selectedApps.postValue(selectedApps.value)
    }

}