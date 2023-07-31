package com.arvifox.remysaw.ext

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.annotation.DimenRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

fun Context.safeStartActivity(intent: Intent) {
    try {
        startActivity(intent)
    } catch (exception: ActivityNotFoundException) {
        Toast.makeText(
            this,
            "Error",
            Toast.LENGTH_LONG
        ).show()
    }
}

fun Fragment.safeStartActivity(intent: Intent) {
    try {
        startActivity(intent)
    } catch (exception: ActivityNotFoundException) {
        Toast.makeText(
            requireContext(),
            "Error",
            Toast.LENGTH_LONG
        ).show()
    }
}

inline fun <T> CoroutineScope.lazyAsync(crossinline producer: suspend () -> T) = lazy {
    async { producer() }
}

fun Fragment.dp2px(dp: Int): Int =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        requireContext().resources.displayMetrics
    ).toInt()

fun Fragment.dpRes2px(@DimenRes res: Int): Int =
    requireContext().resources.getDimensionPixelSize(res)

fun <T : Parcelable> Fragment.requireParcelable(key: String): T {
    return requireNotNull(requireArguments().getParcelable(key), { "Argument [$key] not found" })
}

fun Fragment.runDelayed(
    durationInMillis: Long,
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
    block: () -> Unit,
): Job = viewLifecycleOwner.lifecycle.coroutineScope.launch(dispatcher) {
    delay(durationInMillis)
    if (isActive) {
        block.invoke()
    }
}

fun Fragment.hideSoftKeyboard() {
    requireActivity().hideSoftKeyboard()
}

fun Fragment.openSoftKeyboard(view: View) {
    requireActivity().openSoftKeyboard(view)
}

fun Fragment.setStatusBarColor(color: Color) {
    this.requireActivity().window.statusBarColor = color.toArgb()
}

fun Fragment.setNavbarColor(color: Color) {
    this.requireActivity().window.navigationBarColor = color.toArgb()
}
