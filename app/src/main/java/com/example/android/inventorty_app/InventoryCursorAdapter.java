package com.example.android.inventorty_app;

/**
 * Created by Jimmy on 6/22/2017.
 */

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventorty_app.data.InventoryContract;
import com.example.android.inventorty_app.data.InventoryContract.InventoryEntry;
import com.example.android.inventorty_app.data.InventoryDbHelper;

public class InventoryCursorAdapter extends CursorAdapter {

    private InventoryDbHelper mDbHelper;

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Find fields to populate in inflated template
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        ImageView imageImageView = (ImageView) view.findViewById(R.id.image);
        Button sold = (Button) view.findViewById(R.id.sold);

        // Extract properties from cursor
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);
        int imageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_IMAGE);
        int idColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);


        final String rowId = cursor.getString(idColumnIndex);

        final Integer quantity = cursor.getInt(quantityColumnIndex);
        String name = cursor.getString(nameColumnIndex);
        Float price = cursor.getFloat(priceColumnIndex);
        String image = cursor.getString(imageColumnIndex);


        // Populate fields
        nameTextView.setText(name);
        priceTextView.setText(Float.toString(price));
        quantityTextView.setText(Integer.toString(quantity));

        if (image != null) {
            imageImageView.setImageURI(Uri.parse(image));
        }


        sold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ContentResolver resolver = v.getContext().getContentResolver();
                ContentValues values = new ContentValues();
                if (quantity > 0) {
                    Integer itemId = Integer.parseInt(rowId);
                    Integer newQuantity = quantity - 1;

                    values.put(InventoryEntry.COLUMN_QUANTITY, newQuantity);

                    Uri currentItemUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, itemId);
                    resolver.update(currentItemUri, values, null, null);

                }
            }
        });
    }

}

