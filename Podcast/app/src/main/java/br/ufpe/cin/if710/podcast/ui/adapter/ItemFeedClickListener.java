package br.ufpe.cin.if710.podcast.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import br.ufpe.cin.if710.podcast.MyApplication;
import br.ufpe.cin.if710.podcast.db.PodcastDownload;
import br.ufpe.cin.if710.podcast.domain.ItemFeedPostDB;
import br.ufpe.cin.if710.podcast.ui.EpisodeDetailActivity;

/**
 * Created by danil on 08/10/2017.
 */

public class ItemFeedClickListener implements View.OnClickListener {
    Context context;
    ItemFeedPostDB itemFeed;
    String type;

    public ItemFeedClickListener(Context context, ItemFeedPostDB itemfeed, String type) {
        this.itemFeed = itemfeed;
        this.context = context;
        this.type = type;
    }

    @Override
    public void onClick(View view) {
        if (type.equals("Download")) {
            PodcastDownload.startActionMedia(this.context, this.itemFeed.getDownloadLink(), "" + this.itemFeed.getID());
        }else if(type.equals("Title")){
            Intent intent = new Intent(context, EpisodeDetailActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("title", this.itemFeed.getTitle());
            intent.putExtra("description",this.itemFeed.getDescription());
            intent.putExtra("downloadLink", this.itemFeed.getDownloadLink());
            intent.putExtra("link", this.itemFeed.getLink());
            intent.putExtra("pubdate", this.itemFeed.getPubDate());
            context.startActivity(intent);
        }else if(type.equals("Play")){
            MyApplication ma = (MyApplication) context;
            if(ma.isServiceBound()){
                ma.getPodcastPlayer().playMusic(itemFeed.getPlaystatus());
            }
        }else if(type.equals("Pause")){
            MyApplication ma = (MyApplication) context;
            if(ma.isServiceBound()){
                ma.getPodcastPlayer().pauseMusic();
            }
        }
    }
}
