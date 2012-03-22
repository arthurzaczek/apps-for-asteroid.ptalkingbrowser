package net.zaczek.PTalkingBrowser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class Article extends Activity {
	private static final int ABOUT_ID = 1;
	private static final int EXIT_ID = 2;
	
	private TextView txtArticle;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.article);
		
		txtArticle = (TextView)findViewById(R.id.txtArticle);
		
		Intent intent = getIntent();
		String url = intent.getStringExtra("url");
		
		fillData(url);
	}

	private void fillData(String url) {
		txtArticle.setText("Loding: " + url);
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
}
