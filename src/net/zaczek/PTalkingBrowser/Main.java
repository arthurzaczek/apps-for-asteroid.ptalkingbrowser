package net.zaczek.PTalkingBrowser;

import java.util.ArrayList;

import net.zaczek.PTalkingBrowser.Data.DataManager;
import net.zaczek.PTalkingBrowser.tts.ParrotTTSObserver;
import net.zaczek.PTalkingBrowser.tts.ParrotTTSPlayer;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class Main extends ListActivity implements ParrotTTSObserver, OnItemSelectedListener {
	private static final String TAG = "PTalkingBrowser";

	private static final int SYNC_ID = 1;
	private static final int ABOUT_ID = 2;
	private static final int EXIT_ID = 3;

	private static final int DLG_WAIT = 1;
	
	private ArrayAdapter<WebSiteRef> adapter;
	private ParrotTTSPlayer mTTSPlayer;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mTTSPlayer = new ParrotTTSPlayer(this, this);
		getListView().setOnItemSelectedListener(this);
		fillData();
	}
	
	@Override
	protected void onResume() {
		if(mTTSPlayer != null) mTTSPlayer.destroy();
		mTTSPlayer = new ParrotTTSPlayer(this, this);
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		mTTSPlayer.destroy();
		super.onPause();
	}
	
	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view,
			int pos, long id) {
		try {
			mTTSPlayer.play(adapter.getItem(pos).text);
		} catch (Exception ex) {
			Log.e(TAG, ex.toString());
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {

	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent i = new Intent(this, ArticleList.class);
		WebSiteRef website = adapter.getItem(position);
		i.putExtra("website", website);
		startActivity(i);
	}

	private void fillData() {
		try {
			ArrayList<WebSiteRef> data = DataManager.readWebSites();
			if(data.size() == 0) {
				data.add(new WebSiteRef("Please sync..."));
			}
			adapter = new ArrayAdapter<WebSiteRef>(this, android.R.layout.simple_list_item_1, data);
			setListAdapter(adapter);
		} catch (Exception ex) {
			Toast.makeText(this, ex.toString(), Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, SYNC_ID, 0, "Sync");
		menu.add(0, ABOUT_ID, 0, "About");
		menu.add(0, EXIT_ID, 0, "Exit");
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case SYNC_ID:
			sync();
			break;
		case ABOUT_ID:
			startActivity(new Intent(this, About.class));
			return true;
		case EXIT_ID:
			finish();
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}
	
	private class SyncTask extends AsyncTask<Void, Void, Void> {
		private String msg;

		@Override
		protected void onPreExecute() {
			showDialog(DLG_WAIT);
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				DataManager.downloadWebSites();
			} catch (Exception ex) {
				msg = ex.toString();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			dismissDialog(DLG_WAIT);
			if (!TextUtils.isEmpty(msg)) {
				Toast.makeText(Main.this, msg, Toast.LENGTH_SHORT).show();
			}
			fillData();
			syncTask = null;
			super.onPostExecute(result);
		}
	}

	private SyncTask syncTask;

	private void sync() {
		Log.d(TAG, "Syncing websites");
		if (syncTask == null) {
			syncTask = new SyncTask();
			syncTask.execute();
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DLG_WAIT:
			ProgressDialog pDialog = new ProgressDialog(this);
			pDialog.setMessage("Syncing WebSites");
			return pDialog;
		}
		return null;
	}

	
	@Override
	public void onTTSFinished() {
		
	}

	@Override
	public void onTTSAborted() {
		
	}
}