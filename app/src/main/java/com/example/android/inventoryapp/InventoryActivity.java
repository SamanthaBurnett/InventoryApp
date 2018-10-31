package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class InventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String LOG_TAG = InventoryActivity.class.getSimpleName();

    private static final int ITEM_LOADER = 0;

    ItemCursorAdapter ca;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // FAB that will launch EditActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InventoryActivity.this, EditActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the item data
        ListView itemListView = findViewById(R.id.list);
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View empty = getLayoutInflater().inflate(R.layout.empty_view, null, false);
        addContentView(empty, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        itemListView.setEmptyView(empty);

        ca = new ItemCursorAdapter(this, null);
        itemListView.setAdapter(ca);

        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                Intent intent = new Intent(InventoryActivity.this, EditActivity.class);

                Uri currentItemUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, id);

                intent.setData(currentItemUri);

                startActivity(intent);
            }
        });

        getSupportLoaderManager().initLoader(ITEM_LOADER, null, this);
    }

    private void insertItem() {
        // Create a ContentValues object where column names are the keys,
        // and dummy product's attributes are the values.

        //Create the row to be inserted
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PROD_NAME, "55' Smart TV");
        values.put(InventoryEntry.COLUMN_PROD_PRICE, 15);
        values.put(InventoryEntry.COLUMN_PROD_QUANTITY, 5);
        values.put(InventoryEntry.COLUMN_SUPPL_NAME, "Iyana Industries");
        values.put(InventoryEntry.COLUMN_SUPPL_NUM, "914-555-1016");

        // Insert a new row for dummy product into the provider using the ContentResolver.
        // Use the {@link InventoryEntry#CONTENT_URI} to indicate that we want to insert
        // into the inventory database table.
        // Receive the new content URI that will allow us to access the dummy data in the future.
        Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

    }

    /**
     * Helper method to delete all items in the database.
     */
    private void deleteAllItems() {
        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
        Log.v("InventoryActivity", rowsDeleted + " rows deleted from database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertItem();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllItems();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle args) {
        String[] projection = {
                InventoryEntry.SKU,
                InventoryEntry.COLUMN_PROD_NAME,
                InventoryEntry.COLUMN_PROD_PRICE,
                InventoryEntry.COLUMN_PROD_QUANTITY
        };

        return new CursorLoader(this,
                InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        ca.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ca.swapCursor(null);
    }

    // Helper method to update the database when sale button is clicked
    public void updateAfterSale(int id, int newQuantity) {
        ContentValues cv = new ContentValues();
        cv.put(InventoryEntry.COLUMN_PROD_QUANTITY, newQuantity);

        Uri uri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
        getContentResolver().update(uri, cv, null, null);

        Log.v(LOG_TAG, "Database updated");
    }
}
