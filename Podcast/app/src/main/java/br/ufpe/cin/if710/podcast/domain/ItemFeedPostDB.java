package br.ufpe.cin.if710.podcast.domain;

/**
 * Created by danil on 08/10/2017.
 */

public class ItemFeedPostDB extends ItemFeed {
    private final boolean downloadStatus;
    private final int playstatus;
    private final String episode_file_uri;
    private final int ID;
    public ItemFeedPostDB(String title, String link, String pubDate, String description, String downloadLink,boolean downloadedStatus, int playstatus, String episode_file_uri, int ID) {
        super(title, link, pubDate, description, downloadLink);
        this.downloadStatus = downloadedStatus;
        this.playstatus = playstatus;
        this.episode_file_uri = episode_file_uri;
        this.ID = ID;
    }

    public boolean isDownloadStatus() {
        return downloadStatus;
    }

    public String getEpisode_file_uri() {
        return episode_file_uri;
    }

    public int getPlaystatus() {
        return playstatus;
    }

    public int getID() {
        return ID;
    }
}
