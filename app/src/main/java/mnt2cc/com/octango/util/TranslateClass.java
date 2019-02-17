package mnt2cc.com.octango.util;

import android.app.Service;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class TranslateClass extends AsyncTask<Integer, Void, String[]> {

    //使える言語一覧
    public static final String LANG_EN = "en";
    public static final String LANG_JA = "ja";

    private static final String url = "https://script.google.com/macros/s/AKfycbw73oepd_CauzQNYeFYuscR0ejUQ-1k-uYmFuhXLcVCEkr11mcY/exec";


    private String text, sourceLang, targetLang;
    private TranslateInterface callback;
    public TranslateClass(String text, String sourceLang, String targetLang, TranslateInterface callback){
        this.text = text;
        this.sourceLang = sourceLang;
        this.targetLang = targetLang;
        this.callback = callback;
    }

    @Override
    protected String[] doInBackground(Integer... _){
        String s = this.translate(this.text, this.sourceLang, this.targetLang);
        return new String[]{this.text, s};
    }

    @Override
    protected void onPostExecute(String[] result) {
        this.callback.onTranslateDone(result[0], result[1]);
    }

    /*
     *
     */
    private String translate(String text, String sourceLang, String targetLang){
        try{
            String encoded = encodeURL(text);
            URL apiUrl = createURL(encoded, sourceLang, targetLang);
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String s;
            StringBuilder sb = new StringBuilder();
            while ((s = bufferedReader.readLine()) != null){
                sb.append(s);
            }

            return sb.toString();

        }catch (UnsupportedEncodingException e){
            e.printStackTrace();

        }catch (MalformedURLException e){
            e.printStackTrace();

        }catch (IOException e){
            e.printStackTrace();
        }

        return "";

    }

    private static URL createURL(String text, String sourceLang, String targetLang) throws MalformedURLException {
        URL apiUrl = new URL(url + "?text=" + text + "&sourceLanguage=" + sourceLang + "&targetLanguage="+ targetLang);
        return apiUrl;
    }

    private static String encodeURL(String text) throws UnsupportedEncodingException {
        String encoding = "UTF-8";
        return URLEncoder.encode(text, encoding);
    }
}
