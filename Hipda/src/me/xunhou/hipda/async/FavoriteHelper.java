package me.xunhou.hipda.async;

import me.xunhou.hipda.utils.HiUtils;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import android.content.Context;
import android.widget.Toast;


public class FavoriteHelper {

	private FavoriteHelper() {	
	}
	private static class SingletonHolder {
		public static final FavoriteHelper INSTANCE = new FavoriteHelper();
	}
	public static FavoriteHelper getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public void addFavorite(final Context ctx, final String tid, final String title) {
		StringRequest sReq = new HiStringRequest(ctx, HiUtils.FavoriteAddUrl+tid, 
				new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				if (response.contains("此主题已成功添加到收藏夹中") 
						|| response.contains("您曾经收藏过这个主题")) {
					Toast.makeText(ctx, title+" 收藏添加成功", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(ctx, title+" 收藏添加失败, 请重试", Toast.LENGTH_LONG).show();
				}
			}
		} , 
		new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Toast.makeText(ctx, title+" 收藏添加失败, 请重试."+error.toString(), Toast.LENGTH_LONG).show();
			}
		});
		VolleyHelper.getInstance().add(sReq);
	} 

	public void removeFavorite(final Context ctx, final String tid, final String title) {
		StringRequest sReq = new HiStringRequest(ctx, HiUtils.FavoriteRemoveUrl+tid, 
				new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				if (response.contains("此主题已成功从您的收藏夹中移除")) {
					Toast.makeText(ctx, title+" 收藏删除成功", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(ctx, title+" 收藏删除失败, 请重试", Toast.LENGTH_LONG).show();
				}
			}
		} , 
		new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Toast.makeText(ctx, title+" 收藏删除失败, 请重试."+error.toString(), Toast.LENGTH_LONG).show();
			}
		});
		VolleyHelper.getInstance().add(sReq);
	} 
}
