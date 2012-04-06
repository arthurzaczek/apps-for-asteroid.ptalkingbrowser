package net.zaczek.PTalkingBrowser;

import java.util.ArrayList;

import net.zaczek.PTalkingBrowser.tts.ParrotTTSObserver;
import net.zaczek.PTalkingBrowser.tts.ParrotTTSPlayer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class Article extends Activity implements ParrotTTSObserver {
	private static final int DLG_WAIT = 1;

	private static final int ABOUT_ID = 1;
	private static final int EXIT_ID = 2;

	private TextView txtArticle;
	private ParrotTTSPlayer mTTSPlayer = null;

	private StringBuilder text;
	private ArrayList<ArticleRef> moreArticles;
	private ArrayList<String> sentences;
	private int mPosition = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.article);

		txtArticle = (TextView) findViewById(R.id.txtArticle);
		
		mTTSPlayer = new ParrotTTSPlayer(this, this);

		Intent intent = getIntent();
		String url = intent.getStringExtra("url");
		fillData(url);
	}

	private void fillData(String url) {
		if (task == null) {
			task = new FillDataTask(url);
			task.execute();
		}
	}

	private FillDataTask task;

	private class FillDataTask extends AsyncTask<Void, Void, Void> {
		private String msg;
		private String url;

		public FillDataTask(String url) {
			this.url = url;
			text = new StringBuilder();
			moreArticles = new ArrayList<ArticleRef>();
			txtArticle.setText("Loading " + url);
		}

		@Override
		protected void onPreExecute() {
			showDialog(DLG_WAIT);
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Document doc = Jsoup.connect(url).get();
				String selector = "div[id=storyText] h1, div[id=storyText] h2, div[id=storyText] p:not([class=readMore])";

				Elements elements = doc.select(selector);
				for (Element e : elements) {
					text.append(e.text());
					if (text.charAt(text.length() - 1) != '.') {
						text.append(".");
					}
					text.append("\n");
				}

				// More Articles
				String moreSelector = "p[class=readMore] a";
				Elements links = doc.select(moreSelector);
				for (Element lnk : links) {
					moreArticles.add(new ArticleRef(lnk.attr("abs:href"), lnk.text()));
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
				Toast.makeText(Article.this, msg, Toast.LENGTH_SHORT).show();
			}

			task = null;
			txtArticle.setText(text);

			split(text);
			play();

			super.onPostExecute(result);
		}
	}
	
	private void play() {
		if(sentences != null && sentences.size() > mPosition) {
			mTTSPlayer.play(sentences.get(mPosition));
		}
	}

	private void split(StringBuilder text) {
		sentences = new ArrayList<String>();
		for (String s : TextUtils.split(text.toString(), ".")) {
			if (s.length() > ParrotTTSPlayer.MAX_LENGHT) {
				for (String k : TextUtils.split(s, ",")) {
					sentences.add(k);
				}
			} else {
				sentences.add(s);
			}
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
	
	@Override
	public void onTTSFinished() {
		mPosition++;
		play();
	}

	@Override
	public void onTTSAborted() {
		
	}
}
