package com.arjuj.newsreader;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> titles=new ArrayList<>();
    ArrayList<String> urlList=new ArrayList<>();
    ArrayAdapter arrayAdapter;
    ListView listView;
    public ProgressBar progressBar;

    SQLiteDatabase articlesDB;

    public void updateListView(){
        Cursor c=articlesDB.rawQuery("SELECT * FROM articleNew",null);
        int urlIndex=c.getColumnIndex("url");
        int titleIndex=c.getColumnIndex("title");

        if(c.moveToFirst()){
            titles.clear();
            urlList.clear();

            do{
                titles.add(c.getString(titleIndex));
                urlList.add(c.getString(urlIndex));

            }while(c.moveToNext());


            arrayAdapter.notifyDataSetChanged();


        }

    }

    public class DownloadTask extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String...urls) {

            String result="";
            URL url;
            HttpURLConnection urlConnection=null;
            try{

                url=new URL(urls[0]);
                urlConnection=(HttpURLConnection)url.openConnection();
                InputStream inputStream=urlConnection.getInputStream();
                InputStreamReader inputStreamReader=new InputStreamReader(inputStream);
                int data=inputStreamReader.read();

                while(data!=-1){
                    char current=(char)data;
                    result+=current;
                    data=inputStreamReader.read();
                }
                JSONArray jsonArray=new JSONArray(result);

                int numberOfItems=30;

                if(jsonArray.length()<30){
                    numberOfItems=jsonArray.length();
                }

                articlesDB.execSQL("DELETE FROM articleNew");

                progressBar.setVisibility(View.VISIBLE);

                for(int i=0;i<numberOfItems;i++){
                    String articleId=jsonArray.getString(i);
                    url=new URL("https://hacker-news.firebaseio.com/v0/item/"+articleId+".json?print=pretty");

                    urlConnection=(HttpURLConnection)url.openConnection();
                    inputStream=urlConnection.getInputStream();
                    inputStreamReader=new InputStreamReader(inputStream);
                    data=inputStreamReader.read();
                    String articleInfo="";

                    while(data!=-1){
                        char current=(char)data;
                        articleInfo+=current;
                        data=inputStreamReader.read();
                    }
                    JSONObject jsonObject=new JSONObject(articleInfo);
                    if(!jsonObject.isNull("title")&&!jsonObject.isNull("url")){
                        String articleTitle=jsonObject.getString("title");
                        String articleURL=jsonObject.getString("url");

                        String sql="INSERT INTO articleNew(articleId,title,url) VALUES (?,?,?)";
                        SQLiteStatement statement=articlesDB.compileStatement(sql);
                        statement.bindString(1,articleId);
                        statement.bindString(2,articleTitle);
                        statement.bindString(3,articleURL);

                        statement.execute();
                    }
                }
                progressBar.setVisibility(View.INVISIBLE);


                Log.i("URL Content",result);
                return  result;

            }catch(Exception e) {
                e.printStackTrace();

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            updateListView();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.txt_layout);

        progressBar=(ProgressBar)findViewById(R.id.pro);



        articlesDB=this.openOrCreateDatabase("articleNew",MODE_PRIVATE,null);
        articlesDB.execSQL("CREATE TABLE IF NOT EXISTS articleNew(id INTEGER PRIMARY KEY,articleId INTEGER,title VARCHAR,url VARCHAR)");



        DownloadTask task=new DownloadTask();
        try{
            task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
        }catch(Exception e){
            e.printStackTrace();
        }

        listView=(ListView)findViewById(R.id.listView);
        arrayAdapter=new ArrayAdapter(this,R.layout.simple_list_item_1,titles);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent=new Intent(getApplicationContext(),Main2Activity.class);
                intent.putExtra("url",urlList.get(i));
                startActivity(intent);
                overridePendingTransition( R.anim.slide_in_left, R.anim.slide_out_left );
            }
        });

        updateListView();

    }
}
