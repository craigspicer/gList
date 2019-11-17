package com.nullparams.glist.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.nullparams.glist.ListActivity;
import com.nullparams.glist.R;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private Context context = this;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getData().get("uniqueId") == null) {

            String messageTitle = remoteMessage.getNotification().getTitle();
            String messageBody = remoteMessage.getNotification().getBody();

            sendCloudMessagingNotification(messageTitle, messageBody);

        } else {

            String uniqueId = remoteMessage.getData().get("uniqueId");
            String listName = remoteMessage.getData().get("listName");
            String listAuthor = remoteMessage.getData().get("listAuthor");
            String version = remoteMessage.getData().get("version");
            String clickAction = remoteMessage.getData().get("click_action");

            sendNotification(clickAction, uniqueId, listName, listAuthor, version);
        }
    }

    private void sendCloudMessagingNotification(String title, String body) {

        String url = "https://play.google.com/store/apps/details?id=com.nullparams.glist";

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setData(Uri.parse(url));
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        String channelId = "General Notifications";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setColor(getResources().getColor(R.color.Accent))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "General Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(0, notificationBuilder.build());
    }

    private void sendNotification(String clickAction, String uniqueId, String listName, String listAuthor, String version) {

        if (clickAction.equals("com.nullparams.glist.FirebasePushNotifications.TARGETNOTIFICATIONSHARE")) {

            Intent intent = new Intent(context, ListActivity.class);
            intent.putExtra("uniqueId", uniqueId);
            intent.putExtra("listName", listName);
            intent.putExtra("listAuthor", listAuthor);
            intent.putExtra("version", version);
            intent.putExtra("collectionId", "Shared_lists");
            intent.putExtra("callingFragment", "SharedFragment");
            intent.putExtra("fromNotification", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            String channelId = "General Notifications";
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, channelId);

            notificationBuilder.setSmallIcon(R.drawable.ic_notification);
            notificationBuilder.setContentTitle("You've received a shared list");
            notificationBuilder.setContentText(listName);
            notificationBuilder.setColor(getResources().getColor(R.color.Accent));
            notificationBuilder.setAutoCancel(true);
            notificationBuilder.setSound(defaultSoundUri);
            notificationBuilder.setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId,
                        "General Notifications",
                        NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }
            notificationManager.notify(0, notificationBuilder.build());
        }
    }
}
