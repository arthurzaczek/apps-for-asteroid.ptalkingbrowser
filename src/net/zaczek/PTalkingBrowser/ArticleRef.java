package net.zaczek.PTalkingBrowser;

public class ArticleRef {
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
