package net.zaczek.PTalkingBrowser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import net.zaczek.PTalkingBrowser.tts.ParrotTTSObserver;
import net.zaczek.PTalkingBrowser.tts.ParrotTTSPlayer;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
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

	private static final int ABOUT_ID = 1;
	private static final int EXIT_ID = 2;
	
	private ArrayAdapter<String> adapter;
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
	public void onItemSelected(AdapterView<?> adapterView, View view,
			int pos, long id) {
		try {
			String url = adapter.getItem(pos);
			mTTSPlayer.play(url);
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
		String url = adapter.getItem(position);
		i.putExtra("url", url);
		startActivity(i);
	}

	private void fillData() {
		try {
			ArrayList<String> urls = new ArrayList<String>();
			File root = Environment.getExternalStorageDirectory();
			File dir = new File(root, "PTalkingBrowser");
			dir.mkdir();
			File file = new File(dir, "urls.txt");
			FileReader reader = new FileReader(file);
			try {
				BufferedReader in = new BufferedReader(reader);
				while (true) {
					String url = in.readLine();
					if (url == null)
						break;
					url = url.trim();
					if (TextUtils.isEmpty(url))
						continue;
					urls.add(url);
				}
			} finally {
				reader.close();
			}
			
			adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, urls);
			setListAdapter(adapter);
		} catch (Exception ex) {
			Toast.makeText(this, ex.toString(), Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, ABOUT_ID, 0, "About");
		menu.add(0, EXIT_ID, 0, "Exit");
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case ABOUT_ID:
			startActivity(new Intent(this, About.class));
			return true;
		case EXIT_ID:
			finish();
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}
	
	@Override
	public void onTTSFinished() {
		
	}

	@Override
	public void onTTSAborted() {
		
	}
}