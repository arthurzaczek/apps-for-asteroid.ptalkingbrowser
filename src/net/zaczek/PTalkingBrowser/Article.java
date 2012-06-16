package net.zaczek.PTalkingBrowser;

import java.util.ArrayList;

import net.zaczek.PTalkingBrowser.Data.DataManager;
import net.zaczek.PTalkingBrowser.tts.ParrotTTSObserver;
import net.zaczek.PTalkingBrowser.tts.ParrotTTSPlayer;

import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class Article extends Activity implements ParrotTTSObserver {
	private static final String TAG = "PTalkingBrowser";

	private static final int DLG_WAIT = 1;

	private static final int ABOUT_ID = 1;
	private static final int EXIT_ID = 2;

	private TextView txtArticle;
	private TextView lbTitle;
	private ParrotTTSPlayer mTTSPlayer = null;
	private AudioManager am;
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

		mTTSPlayer = new ParrotTTSPlayer(this, this);
		am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

		Intent intent = getIntent();
		article = intent.getParcelableExtra("article");
		webSite = intent.getParcelableExtra("website");
		fillData();
	}

	@Override
	protected void onResume() {
		if (mTTSPlayer != null)
			mTTSPlayer.destroy();
		mTTSPlayer = new ParrotTTSPlayer(this, this);
		wl.acquire();
		super.onResume();
	}

	@Override
	protected void onPause() {
		mTTSPlayer.destroy();
		wl.release();
		super.onPause();
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
			mTTSPlayer.play(text.toString());
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_DOWN:
			am.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
			return true;
		case KeyEvent.KEYCODE_DPAD_UP:
			am.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
			return true;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
		case KeyEvent.KEYCODE_MEDIA_NEXT:
			return true;
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
			return true;
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
			return true;
		default:
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	public void onTTSFinished() {
	}

	@Override
	public void onTTSAborted() {

	}
}
