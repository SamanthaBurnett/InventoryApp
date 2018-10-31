package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class InventoryProvider extends ContentProvider {
    // Tag for log messages
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    // URI match to query full table
    private static final int INVENTORY = 100;

    // URI match to query single item in inventory
    private static final int ITEM = 101;

    // Matches content URI to one of the above codes
    private static final UriMatcher uMatch = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer
    static {
        uMatch.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.INVENTORY_PATH, INVENTORY);
        uMatch.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.INVENTORY_PATH + "/#", ITEM);
    }

    // Database helper object
    private InventoryHelper mHelper;

    // Initialize provider and helper
    @Override
    public boolean onCreate() {
        mHelper = new InventoryHelper(getContext());
        return true;
    }

    // Query table based on URI
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase db = mHelper.getReadableDatabase();

        Cursor c;

        int match = uMatch.match(uri);

        switch(match) {
            case INVENTORY:
                c = db.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case ITEM:
                selection = InventoryContract.InventoryEntry.SKU + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                c = db.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor
        c.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return c;
    }

    //Insert new data given the content value
    @Override
    public Uri insert(Uri uri, ContentValues cv) {
        final int match = uMatch.match(uri);

        switch (match) {
            case INVENTORY:
                return insertItem(uri, cv);
            default:
                throw new IllegalArgumentException("Cannot insert " + uri);
        }
    }

    // Insert new item given content values
    private Uri insertItem(Uri uri, ContentValues cv) {
        // Get values to insert and check if values that aren't allowed
        // to be null are null or not
        String pName = cv.getAsString(InventoryContract.InventoryEntry.COLUMN_PROD_NAME);
        if (pName == null) {
            throw new IllegalArgumentException("Item name required");
        }

        Integer pPrice = cv.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PROD_PRICE);
        if (pPrice == null) {
            throw new IllegalArgumentException("Item price required");
        }

        Integer pQuantity = cv.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PROD_QUANTITY);
        if (pQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be below zero");
        }

        String supplier = cv.getAsString(InventoryContract.InventoryEntry.COLUMN_SUPPL_NAME);
        if (supplier == null) {
            throw new IllegalArgumentException("Supplier name required");
        }

        String sNum = cv.getAsString(InventoryContract.InventoryEntry.COLUMN_SUPPL_NUM);
        if (sNum == null) {
            throw new IllegalArgumentException("Supplier number required");
        }

        // Get writable database
        SQLiteDatabase db = mHelper.getWritableDatabase();

        // Insert new item in inventory
        long id = db.insert(InventoryContract.InventoryEntry.TABLE_NAME, null, cv);

        // If ID = -1 then the insert failed.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    // Update the data
    @Override
    public int update(Uri uri, ContentValues cv, String selection, String[] selectionArgs) {
        final int match = uMatch.match(uri);
        switch (match) {
            case INVENTORY:
                return updateInventory(uri, cv, selection, selectionArgs);
            case ITEM:
                selection = InventoryContract.InventoryEntry.SKU + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateInventory(uri, cv, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Cannot update " + uri);
        }
    }

    // Update item(s) in inventory given content values and return the number of affected rows
    private int updateInventory(Uri uri, ContentValues cv, String selection, String[] selectionArgs) {
        if (cv.containsKey(InventoryContract.InventoryEntry.COLUMN_PROD_NAME)) {
            String pName = cv.getAsString(InventoryContract.InventoryEntry.COLUMN_PROD_NAME);
            if (pName == null) {
                throw new IllegalArgumentException("Item name required");
            }
        }

        if (cv.containsKey(InventoryContract.InventoryEntry.COLUMN_PROD_PRICE)) {
            Integer pPrice = cv.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PROD_PRICE);
            if (pPrice == null) {
                throw new IllegalArgumentException("Item price required");
            }
        }

        if (cv.containsKey(InventoryContract.InventoryEntry.COLUMN_PROD_QUANTITY)) {
            Integer pQuantity = cv.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PROD_QUANTITY);
            if (pQuantity < 0) {
                throw new IllegalArgumentException("Quantity cannot be below zero");
            }
        }

        if (cv.containsKey(InventoryContract.InventoryEntry.COLUMN_SUPPL_NAME)) {
            String supplier = cv.getAsString(InventoryContract.InventoryEntry.COLUMN_SUPPL_NAME);
            if (supplier == null) {
                throw new IllegalArgumentException("Supplier name required");
            }
        }

        if (cv.containsKey(InventoryContract.InventoryEntry.COLUMN_SUPPL_NUM)) {
            String sNum = cv.getAsString(InventoryContract.InventoryEntry.COLUMN_SUPPL_NUM);
            if (sNum == null) {
                throw new IllegalArgumentException("Supplier number required");
            }
        }

        // If there's nothing to update then don't
        if (cv.size() == 0) {
            return 0;
        }

        // If there is the get a writable database and update
        SQLiteDatabase db = mHelper.getWritableDatabase();

        int rowsUpdated = db.update(InventoryContract.InventoryEntry.TABLE_NAME, cv, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;
    }

    // Delete data
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mHelper.getWritableDatabase();

        int rowsDeleted;
        final int match = uMatch.match(uri);

        switch (match) {
            case INVENTORY:
                rowsDeleted = db.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ITEM:
                selection = InventoryContract.InventoryEntry.SKU + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = db.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed.
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows deleted
        return rowsDeleted;
    }

    // Get MIME type.
    @Override
    public String getType(Uri uri) {
        final int match = uMatch.match(uri);

        switch (match) {
            case INVENTORY:
                return InventoryContract.InventoryEntry.CONTENT_LIST_TYPE;
            case ITEM:
                return InventoryContract.InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

}