package com.felkertech.n.plugins;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.tv.TvContract;
import android.os.IBinder;
import android.util.Log;

import com.example.android.sampletvinput.syncadapter.SyncUtils;
import com.felkertech.n.boilerplate.Utils.SettingsManager;
import com.felkertech.n.cumulustv.ChannelDatabase;
import com.felkertech.n.cumulustv.JSONChannel;

import org.json.JSONException;
import org.json.JSONObject;

public class DataReceiver extends BroadcastReceiver {
    public static String INTENT_EXTRA_JSON = "JSON";
    public static String INTENT_EXTRA_ACTION = "Dowhat";
    public static String INTENT_EXTRA_ACTION_WRITE = "Write";
    public static String INTENT_EXTRA_ACTION_DELETE = "Delete";
    public static String INTENT_EXTRA_SOURCE = "Source";
    public static String TAG = "cumulus:DataReceiver";
    public DataReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Heard");
        if(intent != null) {
            String jsonString = intent.getStringExtra(INTENT_EXTRA_JSON);
            String action = intent.getStringExtra(INTENT_EXTRA_ACTION);
            if(action.equals(INTENT_EXTRA_ACTION_WRITE)) {
                Log.d(TAG, "Received " + jsonString);
                SettingsManager sm = new SettingsManager(context);
                ChannelDatabase cdn = new ChannelDatabase(context);
                try {
                    JSONObject jo = new JSONObject(jsonString);
                    JSONChannel jsonChannel = new JSONChannel(jo);
                    jsonChannel.setSource(intent.getStringExtra(INTENT_EXTRA_SOURCE));
                    if (cdn.channelExists(jsonChannel)) {
                        //Channel exists, so let's update
                        cdn.update(jsonChannel);
                        Log.d(TAG, "Channel updated");
                    } else {
                        cdn.add(jsonChannel);
                        Log.d(TAG, "Channel added");
                    }
                    //Now sync
                    final String info = TvContract.buildInputId(new ComponentName("com.felkertech.n.cumulustv", ".SampleTvInput"));
                    SyncUtils.requestSync(info);
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage() + "; Error while adding");
                    e.printStackTrace();
                }
            } else if(action.equals(INTENT_EXTRA_ACTION_DELETE)) {
                ChannelDatabase cdn = new ChannelDatabase(context);
                try {
                    JSONObject jo = new JSONObject(jsonString);
                    JSONChannel jsonChannel = new JSONChannel(jo);
                    cdn.delete(jsonChannel);
                    Log.d(TAG, "Channel successfully deleted");
                    //Now sync
                    final String info = TvContract.buildInputId(new ComponentName("com.felkertech.n.cumulustv", ".SampleTvInput"));
                    SyncUtils.requestSync(info);
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage() + "; Error while adding");
                    e.printStackTrace();
                }
            }
        }
    }
}
