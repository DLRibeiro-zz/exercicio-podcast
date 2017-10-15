package br.ufpe.cin.if710.podcast.musicService;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.File;

import br.ufpe.cin.if710.podcast.db.PodcastDBHelper;
import br.ufpe.cin.if710.podcast.db.PodcastProviderContract;

/**
 * Created by danil on 14/10/2017.
 */

public class PodcastOnCompleteListener implements MediaPlayer.OnCompletionListener {
    private String episodeID;
    private Uri fileUri;
    private Context context;
    public PodcastOnCompleteListener(Context c, Uri fileUri, String episodeID){
        this.context = c;
        this.episodeID = episodeID;
        this.fileUri = fileUri;
    }
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        ContentResolver cr = this.context.getContentResolver();
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
        cv.put(PodcastProviderContract.EPISODE_FILE_URI, "");
        cv.put(PodcastProviderContract.DOWNLOADED, 0);
        cv.put(PodcastProviderContract.PLAY_STATUS, 0);
        cr.update(PodcastProviderContract.EPISODE_LIST_URI, cv, "WHERE " + PodcastProviderContract._ID + " =?", querySelectionArgs);
        File audio = new File(context.getFilesDir(), episodeID+".mp3");
        audio.delete();
    }
}
