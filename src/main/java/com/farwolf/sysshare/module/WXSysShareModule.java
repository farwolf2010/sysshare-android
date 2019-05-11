package com.farwolf.sysshare.module;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.alibaba.fastjson.JSONArray;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.farwolf.perssion.Perssion;
import com.farwolf.perssion.PerssionCallback;
import com.farwolf.util.Picture;
import com.farwolf.weex.annotation.WeexModule;
import com.farwolf.weex.base.WXModuleBase;
import com.farwolf.weex.util.Const;
import com.taobao.weex.annotation.JSMethod;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

@WeexModule(name="sysShare")
public class WXSysShareModule extends WXModuleBase {


    @JSMethod
    public void share(HashMap param){


        String type=param.get("type")+"";
        if("image".equals(type)){
            shareImg(param.get("image")+"");
        }else{
            String content=param.get("content")+"";
            shareText(content);
        }


    }

    void shareText(String content){
        Intent textIntent = new Intent(Intent.ACTION_SEND);
        textIntent.putExtra(Intent.EXTRA_TEXT, content);
        textIntent.setType("text/plain");
        ((Activity)getContext()).startActivity(Intent.createChooser(textIntent, "分享"));
    }
    void shareImgs(JSONArray urls){
        ArrayList<Uri> imageUris = new ArrayList<>();
        for(int i=0;i<urls.size();i++){
            Uri uri1 = Uri.parse(urls.getString(i));
            imageUris.add(uri1);
        }
        Intent mulIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        mulIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
        mulIntent.setType("image/jpeg");
        ((Activity)getContext()).startActivity(Intent.createChooser(mulIntent, "分享"));
    }
    void shareImg(final String url){



        Perssion.check((Activity) mWXSDKInstance.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE,new PerssionCallback(){
            @Override
            public void onGranted() {

                new Task(url).execute();
            }});
    }

    class Task extends AsyncTask<String, Integer, Bitmap> {

        public Task(String url) {
            this.url = url;
        }

        String url="";
        protected Bitmap doInBackground(String... params) {
            if(url.startsWith("http"))
            return GetImageInputStream(url);
            if(url.startsWith(Const.PREFIX_SDCARD))
                return   Picture.getBitmap(url.replace(Const.PREFIX_SDCARD,""));
            return null;

        }

        protected void onPostExecute(Bitmap result) {

            Uri imageUri = Uri.parse(MediaStore.Images.Media.insertImage(getActivity().getContentResolver(),result, url, null));
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.setType("image/*");
            ((Activity)getContext()).startActivity(Intent.createChooser(shareIntent, "分享到"));
        }

    }



 public Bitmap GetImageInputStream(String imageurl) {
        URL url;
        HttpURLConnection connection = null;
        Bitmap bitmap = null;
        try {
            url = new URL(imageurl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(6000); //超时设置
            connection.setDoInput(true);
            connection.setUseCaches(false); //设置不使用缓存
            InputStream inputStream = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    void download(String url){
        Glide.with(getContext().getApplicationContext())
                .load(url)
                 .into(new SimpleTarget<Drawable>() {
                     @Override
                     public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {

                     }
                 });
    }
}
