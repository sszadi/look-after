package com.hfad.lookafter.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.hfad.lookafter.Book;
import com.hfad.lookafter.FavouriteManager;
import com.hfad.lookafter.R;
import com.hfad.lookafter.database.ConnectionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ContentActivity extends Activity {

    public static final String EXTRA_BOOKN0 = "bookNo";
    private Cursor cursor;
    private Book book;
    private int bookNo;
    private Menu menu;
    private ConnectionManager connectionManager = new ConnectionManager();
    private AssetManager assetManager;
    private FavouriteManager favouriteManager;
    private ContentValues bookValues;
    private AlertDialog alert;

    //TODO: View pager

    @BindView(R.id.pic)
    ImageView image;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.content)
    TextView content;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books);

        assetManager = getAssets();
        ButterKnife.bind(this);

        bookNo = (Integer) getIntent().getExtras().get(EXTRA_BOOKN0);
        new BookData().execute(bookNo);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_books, menu);
        invalidateOptionsMenu();
        return true;
    }

    private void createBook(String author, String title, String cover_id, String content, boolean isFavourite) {
        book = new Book(author, title, cover_id, content, isFavourite);
        displayData();
    }

    private void displayData() {
        showImage();
        title.setText(book.getAuthor() + ' ' + book.getTitle());
    }

    public void showImage() {
        try {
            InputStream is = assetManager.open(book.getCoverResourcePath());
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            image.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readContentFromFile() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(getAssets().open(book.getContentResourcePath())));

            String line = null;
            while ((line = reader.readLine()) != null) {
                content.append(line);
                content.append("\n");
            }
        } catch (IOException e) {
            Log.e("IOException", "readingContentFromFile()");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e("IOException", "readingContentFromFile()");
                }
            }
        }
    }

    private class BookData extends AsyncTask<Integer, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Integer... books) {
            int bookNo = books[0];
            try {
                cursor = connectionManager.getBookByBookNo(bookNo);
                publishProgress();
                return true;
            } catch (SQLiteException ex) {
                ex.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(Void... params) {

            readDataFromCursor();
        }

        protected void onPostExecute(Boolean success) {
            if (!success) {
                connectionManager.showPrompt(ContentActivity.this);
            }
        }
    }

    public void readDataFromCursor() {
        if (cursor.moveToFirst()) {

            String author = cursor.getString(0);
            String title = cursor.getString(1);
            String cover_id = cursor.getString(2);
            String content = cursor.getString(3);
            boolean isFavourite = (cursor.getInt(4) == 1);

            createBook(author, title, cover_id, content, isFavourite);
            readContentFromFile();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_favourite:
                onFavouriteClicked();
                return true;

            case R.id.action_settings:
                //TODO: przypomnienia
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onFavouriteClicked() {
        bookNo = (Integer) getIntent().getExtras().get("bookNo");
        if (book.isFavourite()) displayDialog();
        else new UpdateBookTask().execute(bookNo);
    }

    private class UpdateBookTask extends AsyncTask<Integer, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            Boolean isFavourite = book.isFavourite();
            favouriteManager = new FavouriteManager(getApplicationContext(), book, isFavourite);
            bookValues = favouriteManager.setFavourite();
        }

        @Override
        protected Boolean doInBackground(Integer... books) {
            int bookNo = books[0];
            try {
                connectionManager.uploadData(bookValues, bookNo);
                return true;
            } catch (SQLiteException e) {
                return false;
            }
        }

        protected void onPostExecute(Boolean success) {
            if (!success) {
                connectionManager.showPrompt(ContentActivity.this);
            }
        }
    }

    private void displayDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());

        builder.setMessage("Usunąć książkę z ulubionych?");

        builder.setPositiveButton("Usuń", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                new UpdateBookTask().execute(bookNo);
            }
        });

        builder.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                dialog.dismiss();
            }
        });
        alert = builder.create();
        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alert.show();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem favMenuItem = menu.findItem(R.id.action_favourite);
        if (book != null) {
            if (book.isFavourite()) {
                favMenuItem.setTitle(R.string.action_unfavourite);
            } else {
                favMenuItem.setTitle(R.string.action_favourite);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }
}

