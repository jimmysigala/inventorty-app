package com.example.android.inventorty_app;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventorty_app.data.InventoryContract;
import com.example.android.inventorty_app.data.InventoryContract.InventoryEntry;
import com.example.android.inventorty_app.data.InventoryDbHelper;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private InventoryDbHelper mDbHelper;
    private static final int INVENTORY_LOADER = 0;
    InventoryCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



    setContentView(R.layout.activity_main);

    // Only shows when there are 0 posts
    ListView inventoryListView = (ListView) findViewById(R.id.list);
    View emptyView = findViewById(R.id.empty_view);
        inventoryListView.setEmptyView(emptyView);


    mCursorAdapter = new InventoryCursorAdapter(this, null);
    inventoryListView.setAdapter(mCursorAdapter);



    mDbHelper = new InventoryDbHelper(this);


        inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

            Intent intent = new Intent(MainActivity.this, EditorActivity.class);
            Uri currentItemUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, id);
            intent.setData(currentItemUri);
            startActivity(intent);

            }
        });

        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        startEditorActivity();
        return true;
    }

    private void startEditorActivity() {
        Intent intent = new Intent(MainActivity.this, EditorActivity.class);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_NAME,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_IMAGE


        };

        return new CursorLoader(this, InventoryContract.InventoryEntry.CONTENT_URI,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }


}
