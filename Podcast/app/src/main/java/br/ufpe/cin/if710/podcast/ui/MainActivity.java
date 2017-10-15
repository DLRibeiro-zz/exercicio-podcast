package br.ufpe.cin.if710.podcast.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import br.ufpe.cin.if710.podcast.MyApplication;
import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.db.PodcastDBHelper;
import br.ufpe.cin.if710.podcast.db.PodcastDownload;
import br.ufpe.cin.if710.podcast.db.PodcastProvider;
import br.ufpe.cin.if710.podcast.db.PodcastProviderContract;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.domain.ItemFeedPostDB;
import br.ufpe.cin.if710.podcast.domain.XmlFeedParser;
import br.ufpe.cin.if710.podcast.musicService.PodcastPlayer;
import br.ufpe.cin.if710.podcast.ui.adapter.XmlFeedAdapter;

public class MainActivity extends Activity {

    //ao fazer envio da resolucao, use este link no seu codigo!
    private final String DEFAULT_RSS_FEED = "http://leopoldomt.com/if710/fronteirasdaciencia.xml";
    //TODO teste com outros links de podcast
    private String RSS_FEED;
    ContentResolver cr;
    private ListView items;
    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            makeCRQuery();
            Toast.makeText(context, "A lista foi atualizada", Toast.LENGTH_SHORT);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        items = (ListView) findViewById(R.id.items);
    }

    public void makeCRQuery() {

        CursorLoader cursorLoader = new CursorLoader(
                this,
                PodcastProviderContract.EPISODE_LIST_URI,
                PodcastProviderContract.ALL_COLUMNS,
                null, null, null
        );
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this, R.layout.itemlista,
                cursorLoader.loadInBackground(),
                new String[]{PodcastProviderContract.EPISODE_TITLE, PodcastProviderContract.EPISODE_DATE},
                new int[]{R.id.item_title, R.id.item_date},
                0
        );
        List<ItemFeedPostDB> itemsPostDB = MainActivity.getItemsFromCursor(adapter.getCursor());
        this.items.setAdapter(new XmlFeedAdapter(getApplicationContext(),R.layout.itemlista,itemsPostDB));
    }

    public static List<ItemFeedPostDB> getItemsFromCursor(Cursor cursor) {
        ArrayList<ItemFeedPostDB> alItems = new ArrayList<>();
        while (cursor.moveToNext()) {
            String item_title = cursor.getString(cursor.getColumnIndex(PodcastProviderContract.EPISODE_TITLE));
            String item_pub_date = cursor.getString(cursor.getColumnIndex(PodcastProviderContract.EPISODE_DATE));
            String item_desc = cursor.getString(cursor.getColumnIndex(PodcastProviderContract.EPISODE_DESC));
            boolean item_downloaded;
            if (cursor.getInt(cursor.getColumnIndex(PodcastProviderContract.DOWNLOADED)) == 0) {
                item_downloaded = false;
            } else {
                item_downloaded = true;
            }
            int item_id = cursor.getInt(cursor.getColumnIndex(PodcastProviderContract._ID));
            String item_link = cursor.getString(cursor.getColumnIndex(PodcastProviderContract.EPISODE_LINK));
            String item_download_link = cursor.getString(cursor.getColumnIndex(PodcastProviderContract.EPISODE_DOWNLOAD_LINK));
            String item_file_uri = cursor.getString(cursor.getColumnIndex(PodcastProviderContract.EPISODE_FILE_URI));
            int playstatus = cursor.getInt(cursor.getColumnIndex(PodcastProviderContract.PLAY_STATUS));
            ItemFeedPostDB item = new ItemFeedPostDB(item_title, item_link, item_pub_date, item_desc, item_download_link, item_downloaded, playstatus, item_file_uri,item_id);
            alItems.add(item);
        }
        return alItems;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent podcastIntent = new Intent(this, PodcastPlayer.class);
        startService(podcastIntent);
        MyApplication ma = (MyApplication) getApplicationContext();
        ma.setMainActivityOnForeGround(this, true);
        SharedPreferences sharedPref = this.getSharedPreferences("feedLink", MODE_PRIVATE);
        String feedLink = sharedPref.getString("feedLink", DEFAULT_RSS_FEED);
        this.RSS_FEED = feedLink;
        this.makeCRQuery();
        PodcastDownload.startActionInfo(this);
        LocalBroadcastManager lm = LocalBroadcastManager.getInstance(this);
        lm.registerReceiver(broadcastReceiver, new IntentFilter("br.ufpe.cin.if710.podcast.db.action.LIST"));
        if(!ma.isServiceBound()){
            Intent bindingIntent = new Intent(this, PodcastPlayer.class);
            ma.setServiceBound(bindService(bindingIntent,ma.getsConn(),Context.BIND_AUTO_CREATE));
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        MyApplication ma = (MyApplication) getApplicationContext();
        ma.setMainActivityOnForeGround(this, false);
//        XmlFeedAdapter adapter = (XmlFeedAdapter) items.getAdapter();
//        adapter.clear();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        if(ma.isServiceBound()){
            unbindService(ma.getsConn());
            ma.setServiceBound(false);
        }
    }

    private class DownloadXmlTask extends AsyncTask<String, Void, List<ItemFeed>> {
        @Override
        protected void onPreExecute() {
            Toast.makeText(getApplicationContext(), "iniciando...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected List<ItemFeed> doInBackground(String... params) {
            List<ItemFeed> itemList = new ArrayList<>();
            try {
                itemList = XmlFeedParser.parse(new String(getDataFromURL(params[0]), "UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            return itemList;
        }

        @Override
        protected void onPostExecute(List<ItemFeed> feed) {
            Toast.makeText(getApplicationContext(), "terminando...", Toast.LENGTH_SHORT).show();

            //Adapter Personalizado
//            XmlFeedAdapter adapter = new XmlFeedAdapter(getApplicationContext(), R.layout.itemlista, feed);

            //atualizar o list view
//            items.setAdapter(adapter);
            items.setTextFilterEnabled(true);
            /*
            items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    XmlFeedAdapter adapter = (XmlFeedAdapter) parent.getAdapter();
                    ItemFeed item = adapter.getItem(position);
                    String msg = item.getTitle() + " " + item.getLink();
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                }
            });
            /**/
        }
    }

    //TODO Opcional - pesquise outros meios de obter arquivos da internet
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
