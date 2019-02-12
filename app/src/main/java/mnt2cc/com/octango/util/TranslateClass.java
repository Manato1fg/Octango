package mnt2cc.com.octango.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class TranslateClass {

    //
    private static TranslateClass instance = null;

    //使える言語一覧
    public static final String LANG_EN = "en";
    public static final String LANG_JA = "ja";

    private static final String url = "https://script.google.com/macros/s/AKfycbw73oepd_CauzQNYeFYuscR0ejUQ-1k-uYmFuhXLcVCEkr11mcY/exec";


    private TranslateClass(){
    }

    /*
     *
     */
    public void translate(final String text, final String sourceLang, final String targetLang, final TranslateInterface callback){
        final TranslateClass _this = this;
        new Thread(new Runnable() {
            @Override
            public void run() {

                try{
                    String encoded = _this.encodeURL(text);
                    URL apiUrl = _this.createURL(encoded, sourceLang, targetLang);
                    HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    //result must be one line.
                    String result = bufferedReader.readLine();

                    callback.onTranslateDone(result);

                }catch (UnsupportedEncodingException e){
                    e.printStackTrace();
                    callback.onTranslateDone("An error occurred");

                }catch (MalformedURLException e){
                    e.printStackTrace();
                    callback.onTranslateDone("An error occurred");

                }catch (IOException e){
                    e.printStackTrace();
                    callback.onTranslateDone("An error occurred");
                }

            }
        }).start();

    }

    private URL createURL(String text, String sourceLang, String targetLang) throws MalformedURLException {
        URL apiUrl = new URL(url + "?text=" + text + "&sourceLanguage=" + sourceLang + "&targetLanguage="+ targetLang);
        return apiUrl;
    }

    private String encodeURL(String text) throws UnsupportedEncodingException {
        String encoding = "UTF-8";
        return URLEncoder.encode(text, encoding);
    }

    public static TranslateClass getInstance(){
        if(instance == null){
            instance = new TranslateClass();
        }

        return instance;
    }
}
