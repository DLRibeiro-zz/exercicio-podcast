package br.ufpe.cin.if710.podcast.broadcast;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import br.ufpe.cin.if710.podcast.R;

public class PodcastListUpdateReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(context)
                .setContentTitle("A lista de Podcast foi atualizada")
                .setContentText("A lista de Podcasts foi atualizada  ;)")
                .setSmallIcon(R.drawable.ic_done).build();
        nm.notify(1, notification);
    }
}
