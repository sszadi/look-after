package com.hfad.lookafter;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;


public class FavouriteListActivity extends ListActivity {

    private Cursor favouriteCursor;
    private ListView listFavourites;
    private ConnectionManager connectionManager = new ConnectionManager();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite_list);
        listFavourites = getListView();
        generateFavouriteList(getApplicationContext());
        onItemClickListener();
    }

    private void generateFavouriteList(Context context) {
        new FavouriteListGenerator().execute(context);
    }

    private class FavouriteListGenerator extends AsyncTask<Context, Context, Boolean> {

        @Override
        protected Boolean doInBackground(Context... contexts) {
            Context context = contexts[0];
            String query = "SELECT _id, COVER_RESOURCE_ID, AUTHOR, TITLE FROM BOOKS WHERE FAVOURITE = 1";
            try {
                favouriteCursor = connectionManager.connect(context, query);
                publishProgress(context);
                return true;
            }catch (SQLiteException ex){
                ex.printStackTrace();
                return false;
            }
        }

        protected void onProgressUpdate(Context... contexts){
            Context context = contexts[0];
            CursorAdapter favouriteAdapter = new SimpleCursorAdapter(context, R.layout.activity_list_entry, favouriteCursor,
                    new String[]{"COVER_RESOURCE_ID","AUTHOR", "TITLE"}, new int[]{R.id.pic, R.id.author_entry, R.id.title_entry}, 0);
            setListAdapter(favouriteAdapter);
        }

        protected void onPostExecute(Boolean success){
            if(!success){
                connectionManager.showPrompt(FavouriteListActivity.this);
            }
        }
    }

    private void onItemClickListener() {
        listFavourites.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(FavouriteListActivity.this, BooksActivity.class);
                intent.putExtra(BooksActivity.EXTRA_BOOKN0, (int) id);
                startActivity(intent);
            }
        });
    }

     @Override
    public void onDestroy(){
         super.onDestroy();
         connectionManager.close();
     }

}