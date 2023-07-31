package com.arvifox.remysaw

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object BuildUtils {

    fun sdkAtLeast(v: Int): Boolean = Build.VERSION.SDK_INT >= v

}

object RemyNotification {

    private const val CHANNEL_ID = "com.arvifox.notification.channel"

    fun checkNotificationChannel(manager: NotificationManagerCompat) {
        if (BuildUtils.sdkAtLeast(Build.VERSION_CODES.O)) {
            var notificationChannel = manager.getNotificationChannel(CHANNEL_ID)
            if (notificationChannel == null) {
                notificationChannel = NotificationChannel(
                    CHANNEL_ID, "Remy channel", NotificationManager.IMPORTANCE_LOW,
                )
                manager.createNotificationChannel(notificationChannel)
            }
        }
    }

    fun getBuilder(context: Context): NotificationCompat.Builder =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setLargeIcon(
                (ContextCompat.getDrawable(
                    context,
                    R.drawable.news_photo
                ) as BitmapDrawable).bitmap

            )
}