package br.ufpe.cin.if710.podcast;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import br.ufpe.cin.if710.podcast.musicService.PodcastPlayer;
import br.ufpe.cin.if710.podcast.ui.MainActivity;

/**
 * Created by danil on 08/10/2017.
 */

public class MyApplication extends Application{

    private boolean mainActivityOnForeGround;
    private boolean isBound;
    private PodcastPlayer podcastPlayer;

    public boolean isMainActivityOnForeGround(){
        return this.mainActivityOnForeGround;
    }

    public void setMainActivityOnForeGround(Context c, boolean bol){
        if(c instanceof MainActivity){
            this.mainActivityOnForeGround = bol;
        }
    }

    private ServiceConnection sConn = new ServiceConnection(){
        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            podcastPlayer = ((PodcastPlayer.PodcastBinder) service).getService();
            isBound = true;
        }
    };

    public PodcastPlayer getPodcastPlayer() {
        return podcastPlayer;
    }

    public void setPodcastPlayer(PodcastPlayer podcastPlayer) {
        this.podcastPlayer = podcastPlayer;
    }

    public ServiceConnection getsConn() {
        return sConn;
    }

    public void setsConn(ServiceConnection sConn) {
        this.sConn = sConn;
    }

    public boolean isServiceBound(){
        return this.isBound;
    }

    public void setServiceBound(boolean bol){
        this.isBound = bol;
    }
}
