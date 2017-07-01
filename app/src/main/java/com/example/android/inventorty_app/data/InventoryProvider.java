package com.example.android.inventorty_app.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.inventorty_app.data.InventoryContract.InventoryEntry;

import static android.R.attr.id;

/**
 * Created by Jimmy on 6/22/2017.
 */

public class InventoryProvider extends ContentProvider {
    private static final int Inventory = 100;
    private static final int ITEM_ID = 101;
    private InventoryDbHelper mDbHelper;
    private static final UriMatcher sUriMatcher = new UriMatcher((UriMatcher.NO_MATCH));

    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_ITEMS, Inventory);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_ITEMS + "/#", ITEM_ID);

    }

    //Initialize the provider and the database helper object.
    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }


    //Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {

            case Inventory:
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case ITEM_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        String name = contentValues.getAsString(InventoryContract.InventoryEntry.COLUMN_NAME);
        if (name == null) {
            //throw new IllegalArgumentException("Item requires a name");
        }
        Integer quantity = contentValues.getAsInteger(InventoryEntry.COLUMN_QUANTITY);
        if (quantity != null && quantity < 0) {
            // throw new IllegalArgumentException("Item requires valid quantity");
        }

        Float price = contentValues.getAsFloat(InventoryContract.InventoryEntry.COLUMN_PRICE);
        if (price == null || price < 0) {
            // throw new IllegalArgumentException("Item requires a valid price");
        }

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case Inventory:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }


    // Updates the data at the given selection and selection arguments, with the new ContentValues.
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case Inventory:
                return updateItem(uri, contentValues, selection, selectionArgs);
            case ITEM_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }

    }

    // Delete the data at the given selection and selection arguments.
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        int rowsDeleted;

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case Inventory:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            case ITEM_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    // Returns the MIME type of data for the content URI.
    @Override
    public String getType(Uri uri) {


        final int match = sUriMatcher.match(uri);
        switch (match) {
            case Inventory:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    private Uri insertItem(Uri uri, ContentValues contentValues) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long idRow = db.insert(InventoryContract.InventoryEntry.TABLE_NAME, null, contentValues);

        if (idRow == -1) {
            Log.e(InventoryContract.InventoryEntry.class.getSimpleName(), "Failed to insert row for " + uri);
            return null;
        }

        //int duration = Toast.LENGTH_SHORT;
        // Toast toast = Toast.makeText(getContext(), "Added Row " + idRow, duration);
        // toast.show();

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }


    private int updateItem(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        if (contentValues.containsKey(InventoryEntry.COLUMN_NAME)) {
            String name = contentValues.getAsString(InventoryEntry.COLUMN_NAME);
            if (name == null) {
                // throw new IllegalArgumentException("Item requires a name");
            }
        }


        if (contentValues.containsKey(InventoryEntry.COLUMN_PRICE)) {
            // Check that the price is greater than or equal to 0
            Integer price = contentValues.getAsInteger(InventoryEntry.COLUMN_PRICE);
            if (price == null || price < 0) {
                //  throw new IllegalArgumentException("Item requires valid price");
            }
        }

        if (contentValues.containsKey(InventoryEntry.COLUMN_QUANTITY)) {
            // Check that the price is greater than or equal to 0
            Integer quantity = contentValues.getAsInteger(InventoryEntry.COLUMN_QUANTITY);
            if (quantity == null || quantity < 0) {
                //  throw new IllegalArgumentException("Item requires valid quantity");
            }
        }

        if (contentValues.containsKey(InventoryEntry.COLUMN_PRICE)) {
            // Check that the price is greater than or equal to 0
            Float price = contentValues.getAsFloat(InventoryEntry.COLUMN_PRICE);
            if (price == null || price < 0) {
                // throw new IllegalArgumentException("Item requires valid quantity");
            }
        }


        // If there are no values to update, then don't try to update the database
        if (contentValues.size() == 0) {
            return 0;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int rowsUpdated = db.update(InventoryEntry.TABLE_NAME, contentValues, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}