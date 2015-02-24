package me.xunhou.hipda.adapter;

import java.util.List;

import com.android.volley.toolbox.NetworkImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import me.xunhou.hipda.async.VolleyHelper;
import me.xunhou.hipda.bean.ContentAbs;
import me.xunhou.hipda.bean.ContentAttach;
import me.xunhou.hipda.bean.ContentGoToFloor;
import me.xunhou.hipda.bean.ContentImg;
import me.xunhou.hipda.bean.ContentQuote;
import me.xunhou.hipda.bean.ContentText;
import me.xunhou.hipda.bean.DetailBean;
import me.xunhou.hipda.bean.HiSettingsHelper;
import me.xunhou.hipda.ui.HiNwkImgView;
import me.xunhou.hipda.ui.TextViewWithEmoticon;
import net.jejer.hipda.R;
import android.app.FragmentManager;
import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
//import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ThreadDetailAdapter extends ArrayAdapter<DetailBean> {
	//private final String LOG_TAG = getClass().getSimpleName();

	private Context mCtx;
	private LayoutInflater mInflater;
	private Button.OnClickListener mGoToFloorListener;
	private View.OnClickListener mAvatarListener;
	private FragmentManager mFragmentManager;

	public ThreadDetailAdapter(Context context, FragmentManager fm, int resource,
			List<DetailBean> objects, Button.OnClickListener gotoFloorListener, View.OnClickListener avatarListener) {
		super(context, resource, objects);
		// TODO Auto-generated constructor stub
		mCtx = context;
		mFragmentManager = fm;
		mInflater = LayoutInflater.from(context);
		mGoToFloorListener = gotoFloorListener;
		mAvatarListener = avatarListener;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		DetailBean detail = getItem(position);

		ViewHolder holder;

		if (convertView == null || convertView.getTag() == null) {
			convertView = mInflater.inflate(R.layout.item_thread_detail, null); 

			holder = new ViewHolder(); 
			holder.avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);  
			holder.author = (TextView) convertView.findViewById(R.id.author);  
			holder.time = (TextView) convertView.findViewById(R.id.time); 
			holder.floor = (TextView) convertView.findViewById(R.id.floor); 
			holder.postStatus = (TextView) convertView.findViewById(R.id.post_status); 
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder)convertView.getTag();
		}

		holder.author.setText(detail.getAuthor());  
		holder.time.setText(detail.getTimePost());
		holder.floor.setText(detail.getFloor()+"#");
		holder.postStatus.setText(detail.getPostStatus());
//		holder.avatar.setImageUrl(detail.getAvatarUrl(), VolleyHelper.getInstance().getAvatarLoader());
		ImageLoader.getInstance().displayImage(detail.getAvatarUrl(), holder.avatar);
//		holder.avatar.setDefaultImageResId(R.drawable.google_user);
//		holder.avatar.setErrorImageResId(R.drawable.google_user);
		holder.avatar.setTag(R.id.avatar_tag_uid, detail.getUid());
		holder.avatar.setTag(R.id.avatar_tag_username, detail.getAuthor());
		holder.avatar.setOnClickListener(mAvatarListener);

		LinearLayout contentView = (LinearLayout)convertView.findViewById(R.id.content_layout);
		contentView.removeAllViews();
		for (int i = 0; i < detail.getContents().getSize(); i++) {
			ContentAbs content = detail.getContents().get(i);
			if (content instanceof ContentText) {
				TextViewWithEmoticon tv = new TextViewWithEmoticon(mCtx);
				tv.setFragmentManager(mFragmentManager);
				tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17+HiSettingsHelper.getInstance().getPostTextsizeAdj());
				tv.setMovementMethod(LinkMovementMethod.getInstance());
				tv.setText(content.getContent());
				//setAutoLinkMask have conflict with setMovementMethod
				//tv.setAutoLinkMask(Linkify.WEB_URLS);
				tv.setFocusable(false);
				contentView.addView(tv);
			} else if (content instanceof ContentImg) {
				HiNwkImgView niv = new HiNwkImgView(mCtx);
				niv.setUrl(content.getContent());
				niv.setFocusable(false);
				contentView.addView(niv);
				//Log.v(LOG_TAG, "NetworkImageView Added");
			} else if (content instanceof ContentAttach) {
				TextViewWithEmoticon tv = new TextViewWithEmoticon(mCtx);
				tv.setFragmentManager(mFragmentManager);
				tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17+HiSettingsHelper.getInstance().getPostTextsizeAdj());
				tv.setMovementMethod(LinkMovementMethod.getInstance());
				tv.setText(content.getContent());
				tv.setFocusable(false);
				contentView.addView(tv);
			} else if (content instanceof ContentQuote) {
				TextView tv = new TextView(mCtx);
				tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17+HiSettingsHelper.getInstance().getPostTextsizeAdj());
				tv.setAutoLinkMask(Linkify.WEB_URLS);
				tv.setText(content.getContent());
				tv.setFocusable(false);	// make convertView long clickable.
				contentView.addView(tv);
			} else if (content instanceof ContentGoToFloor) {
				Button btnGotoFloor = new Button(mCtx);
				btnGotoFloor.setBackgroundColor(mCtx.getResources().getColor(R.color.hipda));
				btnGotoFloor.setText(content.getContent());
				btnGotoFloor.setTag(((ContentGoToFloor)content).getFloor());
				btnGotoFloor.setOnClickListener(mGoToFloorListener);
				btnGotoFloor.setFocusable(false);	// make convertView long clickable.
				contentView.addView(btnGotoFloor);
			}
		}

		return convertView;
	}

	private static class ViewHolder {
		ImageView avatar;
		TextView author;
		TextView floor;
		TextView postStatus;
		TextView time;
	}
}
