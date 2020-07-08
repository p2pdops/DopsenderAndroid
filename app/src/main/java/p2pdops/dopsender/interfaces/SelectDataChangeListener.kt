package p2pdops.dopsender.interfaces

import java.io.File

interface SelectDataChangeListener {

    fun onChangeSelections(selectedImages: ArrayList<File>)
    fun onChangeSize(size: Long)
}