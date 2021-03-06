package net.zaczek.PTalkingBrowser;

import java.util.ArrayList;

import net.zaczek.PTalkingBrowser.Data.DataManager;

import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.parrot.asteroid.Manager;
import com.parrot.asteroid.ManagerObserverInterface;
import com.parrot.asteroid.tts.TTSManager;
import com.parrot.asteroid.tts.TTSManagerFactory;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class Article extends Activity implements ManagerObserverInterface {
	private static final String TAG = "PTalkingBrowser";

	private static final int DLG_WAIT = 1;

	private static final int ABOUT_ID = 1;
	private static final int SHOW_TEXT_ID = 2;
	private static final int EXIT_ID = 3;

	private TextView txtArticle;
	private TextView lbTitle;
	private TTSManager mTTS = null;
	private WakeLock wl;

	private StringBuilder text;
	private ArrayList<ArticleRef> moreArticles;
	private WebSiteRef webSite;
	private ArticleRef article;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.article);

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK,
				"ListenToPageAndStayAwake");

		txtArticle = (TextView) findViewById(R.id.txtArticle);
		lbTitle = (TextView) findViewById(R.id.lbTitle);

		mTTS = TTSManagerFactory.getTTSManager(this);
		mTTS.addManagerObserver(this);

		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		Intent intent = getIntent();
		article = intent.getParcelableExtra("article");
		webSite = intent.getParcelableExtra("website");
		fillData();
	}

	@Override
	protected void onResume() {
		wl.acquire();
		super.onResume();
	}

	@Override
	protected void onPause() {
		wl.release();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (mTTS != null) {
			mTTS.stop();
			mTTS.deleteManagerObserver(this);
		}
		super.onDestroy();
	}

	private void fillData() {
		if (task == null) {
			task = new FillDataTask();
			task.execute();
		}
	}

	private FillDataTask task;

	private class FillDataTask extends AsyncTask<Void, Void, Void> {
		private String msg;
		private String url;

		public FillDataTask() {
			this.url = article.url;
			text = new StringBuilder();
			moreArticles = new ArrayList<ArticleRef>();
			lbTitle.setText(article.text);
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
				Response response = DataManager.jsoupConnect(url).execute();
				int status = response.statusCode();
				if (status == 200) {
					Document doc = response.parse();
					Elements elements = doc.select(webSite.article_selector);
					for (Element e : elements) {
						text.append(e.text());
						if (text.charAt(text.length() - 1) != '.') {
							text.append(".");
						}
						text.append("\n");
					}

					// More Articles
					if (!TextUtils.isEmpty(webSite.readmore_selector)) {
						Elements links = doc.select(webSite.readmore_selector);
						for (Element lnk : links) {
							moreArticles.add(new ArticleRef(lnk
									.attr("abs:href"), lnk.text()));
						}
					}
				} else {
					msg = response.statusMessage();
				}
			} catch (Exception ex) {
				Log.e(TAG, "Error reading article", ex);
				msg = ex.getMessage();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			dismissDialog(DLG_WAIT);

			if (!TextUtils.isEmpty(msg)) {
				Toast.makeText(Article.this, msg, Toast.LENGTH_SHORT).show();
				txtArticle.setText(msg);
			} else {
				txtArticle.setText(String.format("%d chars", text.length()));
				play();
			}
			task = null;
			super.onPostExecute(result);
		}
	}

	private void play() {
		if (text != null) {
			mTTS.speak(text.toString(), TTSManager.QUEUE_FLUSH, null);
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
		menu.add(0, SHOW_TEXT_ID, 0, "Show Text");
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
		case SHOW_TEXT_ID:
			txtArticle.setText(text);
			return true;
		case EXIT_ID:
			finish();
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onManagerReady(boolean arg0, Manager arg1) {

	}
}
