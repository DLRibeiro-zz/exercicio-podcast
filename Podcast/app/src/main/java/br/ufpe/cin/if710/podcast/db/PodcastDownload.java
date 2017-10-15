package br.ufpe.cin.if710.podcast.db;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import br.ufpe.cin.if710.podcast.MyApplication;
import br.ufpe.cin.if710.podcast.broadcast.PodcastListUpdateReciever;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.domain.XmlFeedParser;
import br.ufpe.cin.if710.podcast.ui.MainActivity;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class PodcastDownload extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_INFO = "br.ufpe.cin.if710.podcast.db.action.INFO";
    private static final String ACTION_MEDIA = "br.ufpe.cin.if710.podcast.db.action.MEDIA";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "br.ufpe.cin.if710.podcast.db.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "br.ufpe.cin.if710.podcast.db.extra.PARAM2";

    private File fileDir;

    public PodcastDownload() {
        super("PodcastDownload");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionInfo(Context context) {
        Intent intent = new Intent(context, PodcastDownload.class);
        intent.setAction(ACTION_INFO);
        SharedPreferences sharedPref = context.getSharedPreferences("feedLink", MODE_PRIVATE);
        String feedLink = sharedPref.getString("feedLink", "");
        intent.putExtra("feedLink", feedLink);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionMedia(Context context, String download, String episodeID) {
        Intent intent = new Intent(context, PodcastDownload.class);
        intent.setAction(ACTION_MEDIA);
        intent.putExtra("downloadLink", download);
        intent.putExtra("episodeID", episodeID);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        this.fileDir = getApplicationContext().getFilesDir();
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_INFO.equals(action)) {
                String feedLink = intent.getStringExtra("feedLink");
                handleActionInfo(feedLink);
            } else if (ACTION_MEDIA.equals(action)) {
                String downloadLink = intent.getStringExtra("downloadLink");
                String episodeID = intent.getStringExtra("episodeID");
                handleActionMedia(downloadLink, episodeID);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionInfo(String feedLink) {
        ContentResolver cr = getApplicationContext().getContentResolver();
        List<ItemFeed> itemList = new ArrayList<>();
        try {
            itemList = XmlFeedParser.parse(new String(getDataFromURL(feedLink), "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        String[] queryProjection = new String[1];
        String[] querySelectionArgs = new String[1];
        Cursor checkingExistance;
        for (ItemFeed item : itemList) {
            queryProjection[0] = PodcastDBHelper.EPISODE_DOWNLOAD_LINK;
            querySelectionArgs[0] = item.getDownloadLink();
            checkingExistance = cr.query(PodcastProviderContract.EPISODE_LIST_URI, queryProjection, "WHERE " + PodcastDBHelper.EPISODE_DOWNLOAD_LINK + " = ?", querySelectionArgs, null);
            if (checkingExistance.getCount() == 0) {
                cr.insert(PodcastProviderContract.EPISODE_LIST_URI, item.toContentValue());
            }
        }
        if (((MyApplication) getApplicationContext()).isMainActivityOnForeGround()) {
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent().setAction("br.ufpe.cin.if710.podcast.db.action.LIST"));
        } else {
            sendBroadcast(new Intent(getApplicationContext(), PodcastListUpdateReciever.class));
        }
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionMedia(String downloadLink, String episodeID) {
        byte[] fileBytes = null;
        File audio = null;
        FileOutputStream fileOut;
        try {
            fileBytes = this.getDataFromURL(downloadLink);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fileOut = openFileOutput(episodeID+".mp3",Context.MODE_PRIVATE);
            fileOut.write(fileBytes);
            fileOut.close();
            audio = new File(getFilesDir(), episodeID+".mp3");
            ContentResolver cr = getApplicationContext().getContentResolver();
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
            cv.put(PodcastProviderContract.EPISODE_FILE_URI, String.valueOf(android.net.Uri.parse(String.valueOf(audio.toURI()))));
            cv.put(PodcastProviderContract.DOWNLOADED, 1);
            cv.put(PodcastProviderContract.PLAY_STATUS, 0);
            cr.update(PodcastProviderContract.EPISODE_LIST_URI, cv, "WHERE " + PodcastProviderContract._ID + " =?", querySelectionArgs);
            if (((MyApplication) getApplicationContext()).isMainActivityOnForeGround()) {
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent().setAction("br.ufpe.cin.if710.podcast.db.action.LIST"));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private byte[] getDataFromURL(String feed) throws IOException {
        InputStream in = null;
        byte[] response;
        try {
            URL url = new URL(feed);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            in = conn.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int count; (count = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, count);
            }
            response = out.toByteArray();
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return response;
    }
}
