package br.ufpe.cin.if710.podcast.domain;

import android.content.ContentValues;

import br.ufpe.cin.if710.podcast.db.PodcastDBHelper;

public class ItemFeed {
    private final String title;
    private final String link;
    private final String pubDate;
    private final String description;
    private final String downloadLink;


    public ItemFeed(String title, String link, String pubDate, String description, String downloadLink) {
        this.title = title;
        this.link = link;
        this.pubDate = pubDate;
        this.description = description;
        this.downloadLink = downloadLink;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getPubDate() {
        return pubDate;
    }

    public String getDescription() {
        return description;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    @Override
    public String toString() {
        return title;
    }

    public ContentValues toContentValue(){
        ContentValues cv = new ContentValues();
        cv.put(PodcastDBHelper.EPISODE_TITLE, this.title);
        cv.put(PodcastDBHelper.EPISODE_LINK, this.link);
        cv.put(PodcastDBHelper.EPISODE_DESC,this.description);
        cv.put(PodcastDBHelper.EPISODE_DOWNLOAD_LINK, this.downloadLink);
        cv.put(PodcastDBHelper.DOWNLOADED, 0);
        cv.put(PodcastDBHelper.PLAY_STATUS, 0);
        cv.put(PodcastDBHelper.EPISODE_DATE, this.pubDate);
        cv.put(PodcastDBHelper.EPISODE_FILE_URI,"");
        return cv;
    }
}