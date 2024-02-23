package app.grapheneos.setupwizard.android

import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import java.util.concurrent.Executors

private val cachedThreadPool by lazy {
    Executors.newCachedThreadPool()
}
private val foregroundHandler by lazy {
    Handler(Looper.getMainLooper())
}

fun background(@WorkerThread run: () -> Unit) {
    cachedThreadPool.execute(run)
}

fun foreground(@MainThread run: () -> Unit) {
    foregroundHandler.post(run)
}

fun foreground(delay: Long, @MainThread run: () -> Unit) {
    foregroundHandler.postDelayed(run, delay)
}
