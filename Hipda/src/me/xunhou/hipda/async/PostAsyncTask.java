package me.xunhou.hipda.async;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import me.xunhou.hipda.bean.HiSettingsHelper;
import me.xunhou.hipda.utils.HiUtils;
import me.xunhou.hipda.utils.HttpUtils;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.widget.Toast;

public class PostAsyncTask extends AsyncTask<String, Void, Void> {
	//private final String LOG_TAG = getClass().getSimpleName();
	public static final int MODE_REPLY_THREAD = 0;
	public static final int MODE_REPLY_POST = 1;
	public static final int MODE_QUOTE_POST = 2;
	public static final int MODE_NEW_THREAD = 3;

	private int mMode;
	private String mResult;
	private Context mCtx;
	private Map<String, List<String>> mInfo;


	public PostAsyncTask(Context ctx, int mode, Map<String, List<String>> info) {
		mCtx = ctx;
		mMode = mode;
		mInfo = info;
	}

	@Override
	protected Void doInBackground(String... params) {

		String reply_text = params[0];
		String tid = params[1];
		String pid = params[2];
		String fid = params[3];
		String subject = params[4];

		if (mInfo == null) {
			mInfo = new PrePostAsyncTask(mCtx, null, mMode).doInBackground(tid, pid);
		}


		String tail_text = HiSettingsHelper.getInstance().getTailText();
		if (!tail_text.isEmpty() && HiSettingsHelper.getInstance().isAddTail()) {
			String tail_url = HiSettingsHelper.getInstance().getTailUrl();
			if (!tail_url.isEmpty()) {
				if ((!tail_url.startsWith("http")) && (!tail_url.startsWith("https"))) {
					tail_url = "http://" + tail_url;
				}
				reply_text = reply_text + "  [url=" + tail_url + "][size=1]" + tail_text + "[/size][/url]";
			} else {
				reply_text = reply_text + "  [size=1]" + tail_text + "[/size]";
			}
		}

		CookieStore cookieStore = HttpUtils.restoreCookie(mCtx);
		HttpContext localContext = new BasicHttpContext();
		AndroidHttpClient client = AndroidHttpClient.newInstance(HiUtils.UserAgent);
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

		String url = HiUtils.ReplyUrl + tid + "&replysubmit=yes";
		// do send
		switch (mMode) {
		case MODE_REPLY_THREAD:
			doPost(client, localContext, url, reply_text);
			break;
		case MODE_REPLY_POST:
		case MODE_QUOTE_POST:
			doPost(client, localContext, url, mInfo.get("text").get(0) + "\n\n\n    " + reply_text);
			break;
		case MODE_NEW_THREAD:
			url = HiUtils.NewThreadUrl + fid + "&topicsubmit=yes";
			doPost(client, localContext, url, reply_text, subject);
		}


		client.close();
		return null;
	}

	@Override
	protected void onPostExecute(Void avoid) {
		Toast.makeText(mCtx, mResult, Toast.LENGTH_LONG).show();
	}

	private String doPost(AndroidHttpClient client, HttpContext ctx, String url, String... text) {
		HttpPost req = new HttpPost(url);

		Map<String, String> post_param = new HashMap<String, String>();
		post_param.put("formhash", mInfo.get("formhash").get(0));
		post_param.put("posttime", String.valueOf(System.currentTimeMillis()));
		post_param.put("wysiwyg", "0");
		post_param.put("checkbox", "0");
		post_param.put("message", text[0]);
		for (String attach : mInfo.get("attaches")) {
			post_param.put("attachnew["+attach+"][description]", ""+attach);
		}
		if (mMode == MODE_NEW_THREAD) {
			post_param.put("subject", text[1]);
		}

		try {
			String encoded = HttpUtils.buildHttpString(post_param);
			//Log.v("POST DATA", encoded);
			StringEntity entity = new StringEntity(encoded, HiSettingsHelper.getInstance().getEncode());
			entity.setContentType("application/x-www-form-urlencoded");
			req.setEntity(entity);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}

		String rsp_str;
		int rsp_code;
		try {
			HttpResponse rsp = client.execute(req, ctx);
			HttpEntity rsp_ent = (HttpEntity)rsp.getEntity();
			rsp_code = rsp.getStatusLine().getStatusCode();
			rsp_str = EntityUtils.toString(rsp_ent, HiSettingsHelper.getInstance().getEncode());
			//Log.i("weather_info_response", rsp_str);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		if (rsp_code == 302) {
			mResult = "Post Success!";
		} else {
			mResult = "Post Fail!";
			Document doc = Jsoup.parse(rsp_str);
			Elements error = doc.select("div.alert_info");
			if (!error.isEmpty()) {
				mResult += error.text();
			} 
		}

		return rsp_str;
	}

}
