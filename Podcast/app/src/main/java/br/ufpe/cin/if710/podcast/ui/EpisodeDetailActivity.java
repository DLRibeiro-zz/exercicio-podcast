package br.ufpe.cin.if710.podcast.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

import br.ufpe.cin.if710.podcast.R;

public class EpisodeDetailActivity extends Activity {
    TextView title;
    TextView desc;
    TextView link, downloadLink;
    TextView pubDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        super.onCreate(savedInstanceState);
        String title_ = intent.getStringExtra("title");
        String desc_ = intent.getStringExtra("desc");
        String link_ = intent.getStringExtra("link");
        String downloadLink_ = intent.getStringExtra("downloadLink");
        String pubDate_ = intent.getStringExtra("pubDate");
        setContentView(R.layout.activity_episode_detail);
        title = (TextView) findViewById(R.id.podcast_title);
        desc = (TextView) findViewById(R.id.podcast_desc);
        link = (TextView) findViewById(R.id.podcast_link);
        downloadLink = (TextView) findViewById(R.id.podcast_download_link);
        pubDate = (TextView) findViewById(R.id.podcast_pub_date);
        title.setText(title_);
        desc.setText(desc_);
        link.setText(link_);
        downloadLink.setText(downloadLink_);
        pubDate.setText(pubDate_);
    }


}
