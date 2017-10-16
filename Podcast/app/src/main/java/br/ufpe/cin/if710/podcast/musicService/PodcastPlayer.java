package br.ufpe.cin.if710.podcast.musicService;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.IntDef;

import java.io.IOException;
import java.net.URI;

import br.ufpe.cin.if710.podcast.db.PodcastDBHelper;
import br.ufpe.cin.if710.podcast.db.PodcastProviderContract;
import br.ufpe.cin.if710.podcast.ui.MainActivity;

public class PodcastPlayer extends Service {
    private final String TAG = "PodcastPlayer";
    private static final int NOTIFICATION_ID = 2;
    private MediaPlayer mPlayer;
    private Uri fileUri;
    private String episodeID;
    public PodcastPlayer() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPlayer = new MediaPlayer();
        mPlayer.setLooping(false);
        this.episodeID = "";
        // cria notificacao na area de notificacoes para usuario voltar p/ Activity
        final Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        final Notification notification = new Notification.Builder(
                getApplicationContext())
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setOngoing(true).setContentTitle("Podcast Service rodando")
                .setContentText("Clique para acessar o player!")
                .setContentIntent(pendingIntent).build();

        // inicia em estado foreground, para ter prioridade na memoria
        // evita que seja facilmente eliminado pelo sistema
        startForeground(NOTIFICATION_ID, notification);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (null != mPlayer) {
            mPlayer.stop();
            mPlayer.release();
        }
    }

    public void playMusic(int position) {
        if (!mPlayer.isPlaying()) {
            mPlayer.seekTo(position);
            mPlayer.start();
        }
    }

    public long pauseMusic() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            this.setCurrentPositionOnDB(mPlayer.getCurrentPosition());
            mPlayer.reset();
        }
        return mPlayer.getCurrentPosition();
    }

    public void setCurrentPositionOnDB(int currentPositionOnDB){
        ContentResolver cr = this.getContentResolver();
        String[] querySelectionArgs = {episodeID};
        Cursor q = cr.query(PodcastProviderContract.EPISODE_LIST_URI, null, "WHERE " + PodcastDBHelper._ID + " = ?", querySelectionArgs, null);
        q.moveToFirst();
        ContentValues cv = new ContentValues();
        cv.put(PodcastProviderContract.EPISODE_TITLE, q.getString(q.getColumnIndex(PodcastProviderContract.EPISODE_TITLE)));
        cv.put(PodcastProviderContract.EPISODE_DESC, q.getString(q.getColumnIndex(PodcastProviderContract.EPISODE_DESC)));
        cv.put(PodcastProviderContract.EPISODE_DATE, q.getString(q.getColumnIndex(PodcastProviderContract.EPISODE_DATE)));
        cv.put(PodcastProviderContract.EPISODE_DOWNLOAD_LINK, q.getString((q.getColumnIndex(PodcastProviderContract.EPISODE_DOWNLOAD_LINK))));
        cv.put(PodcastProviderContract._ID, q.getInt(q.getColumnIndex(PodcastProviderContract._ID)));
        cv.put(PodcastProviderContract.EPISODE_LINK, q.getString(q.getColumnIndex(PodcastProviderContract.EPISODE_LINK)));
        cv.put(PodcastProviderContract.EPISODE_FILE_URI, q.getString(q.getColumnIndex(PodcastDBHelper.EPISODE_FILE_URI)));
        cv.put(PodcastProviderContract.DOWNLOADED, 1);
        cv.put(PodcastProviderContract.PLAY_STATUS, currentPositionOnDB);
        cr.update(PodcastProviderContract.EPISODE_LIST_URI, cv, "WHERE " + PodcastProviderContract._ID + " =?", querySelectionArgs);
    }

    public void setDataSource(String fileUri, String episodeID) throws IOException {
        if(!this.episodeID.equals("") && this.mPlayer.isPlaying()){
            this.pauseMusic();
        }else if(!this.episodeID.equals("") && !this.mPlayer.isPlaying()){
            this.mPlayer.reset();
        }
        this.episodeID = episodeID;
        mPlayer.setDataSource(getApplicationContext(), android.net.Uri.parse(fileUri));
        this.fileUri = android.net.Uri.parse(fileUri);
        mPlayer.setOnCompletionListener(new PodcastOnCompleteListener(getApplicationContext(),this.fileUri,episodeID));
    }

    public void setDataSource(android.net.Uri fileUri,String episodeID) throws IOException {
        if(!this.episodeID.equals("") && this.mPlayer.isPlaying()){
            this.pauseMusic();
        }else if(!this.episodeID.equals("") && !this.mPlayer.isPlaying()){
            this.mPlayer.reset();
        }
        this.episodeID = episodeID;
        mPlayer.setDataSource(getApplicationContext(), fileUri);
        this.fileUri = fileUri;
        mPlayer.setOnCompletionListener(new PodcastOnCompleteListener(getApplicationContext(),fileUri,episodeID));
    }

    private final IBinder pBinder = new PodcastBinder();

    public class PodcastBinder extends Binder {
        public PodcastPlayer getService(){
            return PodcastPlayer.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return pBinder;
    }
}
