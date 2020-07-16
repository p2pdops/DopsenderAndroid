package p2pdops.dopsender.zshare_helpers

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior

fun closeSheet(view: View) {
    BottomSheetBehavior.from(view).isHideable = true
    BottomSheetBehavior.from(view).state = BottomSheetBehavior.STATE_HIDDEN
}

fun openSheet(view: View) {
    BottomSheetBehavior.from(view).state = BottomSheetBehavior.STATE_HALF_EXPANDED
    BottomSheetBehavior.from(view).isHideable = false
}