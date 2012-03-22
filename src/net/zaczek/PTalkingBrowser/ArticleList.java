package net.zaczek.PTalkingBrowser;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ArticleList extends ListActivity {
	private static final int DLG_WAIT = 1;
	
	private static final int ABOUT_ID = 1;
	private static final int EXIT_ID = 2;
	private ArrayAdapter<ArticleRef> adapter;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.articlelist);

		Intent intent = getIntent();
		String url = intent.getStringExtra("url");
		
		fillData(url);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent i = new Intent(this, Article.class);
		ArticleRef a = adapter.getItem(position);
		i.putExtra("url", a.url);
		startActivity(i);
	}

	private void fillData(String url) {
		if (task == null) {
			task = new FillDataTask(url);
			task.execute();
		}
	}
	
	private FillDataTask task;
	
	private class ArticleRef
	{
		public ArticleRef(String url, String text) {
			this.url = url;
			this.text = text;
		}
		
		public String url;
		public String text;
		
		@Override
		public String toString() {
			return text;
		}
	}
	
	private class FillDataTask extends AsyncTask<Void, Void, Void> {
		private String msg;
		private String url;
		ArrayList<ArticleRef> articles;
		
		public FillDataTask(String url) {
			this.url = url;
		}
		
		@Override
		protected void onPreExecute() {
			showDialog(DLG_WAIT);
			super.onPreExecute();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			try {
				articles = new ArrayList<ArticleRef>();
				
				Document doc = Jsoup.connect(url).get();
				Elements links = doc.select("h1 a");

				for(Element lnk : links) {
					articles.add(new ArticleRef(lnk.attr("abs:href"), lnk.text()));
				}
				
			} catch (Exception ex) {
				msg = ex.toString();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			dismissDialog(DLG_WAIT);
			
			if (!TextUtils.isEmpty(msg)) {
				Toast.makeText(ArticleList.this, msg, Toast.LENGTH_SHORT).show();
			}
			
			task = null;
			adapter = new ArrayAdapter<ArticleRef>(ArticleList.this, android.R.layout.simple_list_item_1, articles);
			setListAdapter(adapter);
			
			super.onPostExecute(result);
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		ProgressDialog dialog;
		switch (id) {
		case DLG_WAIT:
			dialog = new ProgressDialog(this);
			dialog.setMessage("Loading");
			break;
		default:
			dialog = null;
		}
		return dialog;
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
