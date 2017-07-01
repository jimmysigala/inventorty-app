package com.example.android.inventorty_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventorty_app.data.InventoryContract;
import com.example.android.inventorty_app.data.InventoryContract.InventoryEntry;


/**
 * Created by Jimmy on 6/20/2017.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_ITEM_LOADER = 0;
    /**
     * EditText field to enter the post's title
     */
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mEmailEditText;
    private ImageView mImageView;
    private Uri mImageURI;
    final int READ_EXTERNAL_STORAGE = 1;
    private String imageString;
    Uri image;
    String subject = "Item Restock";
    String body = "Need to restock item name: ";


    private Uri mCurrentItemUri;
    private boolean mItemHasChanged = false;

    // used to see if any changes made to prompt user to keep editing to discard
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {


        String[] projection = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_NAME,
                InventoryContract.InventoryEntry.COLUMN_IMAGE,
                InventoryContract.InventoryEntry.COLUMN_PRICE,
                InventoryContract.InventoryEntry.COLUMN_QUANTITY
        };

        return new CursorLoader(this, mCurrentItemUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {


        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {

            int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_NAME);
            int imageColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_IMAGE);
            int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_QUANTITY);


            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String image = cursor.getString(imageColumnIndex);
            Float price = cursor.getFloat(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);


            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mPriceEditText.setText(Float.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));
            if (image != null) {
                mImageView.setImageURI(Uri.parse(image));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mNameEditText.setText("");
        mPriceEditText.setText(Float.toString(0));
        mQuantityEditText.setText(Integer.toString(0));
        mImageView.setImageDrawable(null);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == READ_EXTERNAL_STORAGE && resultCode == Activity.RESULT_OK) {
            image = data.getData();
            imageString = image.toString();
            mImageURI = Uri.parse(image.toString());
            mImageView.setImageURI(image);
            mImageView.invalidate();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Use getIntent() abd getData() to get associated URI
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        if (mCurrentItemUri == null) {
            setTitle("Insert an item");
            invalidateOptionsMenu(); // Hide delete button
        } else {
            setTitle("Item");
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_item_name);
        mQuantityEditText = (EditText) findViewById(R.id.edit_item_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_item_price);
        mImageView = (ImageView) findViewById(R.id.edit_item_image);
        Button decQuantity = (Button) findViewById(R.id.decQuantity);
        Button incQuantity = (Button) findViewById(R.id.incQuantity);
        Button order = (Button) findViewById(R.id.order);
        mEmailEditText = (EditText) findViewById(R.id.email);


        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        // mImageView.setOnTouchListener(mTouchListener);

        // set click listener to select an image
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI),
                        READ_EXTERNAL_STORAGE);
            }
        });


        decQuantity.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DecQuantity();
            }
        });

        incQuantity.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                incQuantity();
            }
        });


        order.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, new String[] {mEmailEditText.getText().toString().trim()});
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                intent.putExtra(Intent.EXTRA_TEXT, body + mNameEditText.getText().toString().trim());
                startActivity(Intent.createChooser(intent, "Send Email"));
            }
        });
    }


    // Prompt user if changes should be discarded
    @Override
    public void onBackPressed() {
        // If the inventory hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the inventory.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    // Hides delete button when adding a new post
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // hide delete button if new item
        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    private void saveItem() {

        String nameString = mNameEditText.getText().toString().trim();
        String price = mPriceEditText.getText().toString().trim();
        String quantity = mQuantityEditText.getText().toString().trim();

        if (!isNumeric(price) || (!isNumeric(quantity)))
        {
            Toast.makeText(this, R.string.numeric, Toast.LENGTH_SHORT).show();
            return;
        }


        if (mCurrentItemUri == null &&
                TextUtils.isEmpty(nameString) ||
                TextUtils.isEmpty(price) || TextUtils.isEmpty(quantity)) {

            Toast.makeText(this, R.string.fields, Toast.LENGTH_SHORT).show();
            return;
        }

        if (Integer.valueOf(quantity) < 0 || Float.valueOf(price) < 0) {
            Toast.makeText(this, R.string.negative, Toast.LENGTH_SHORT).show();
            return;

        }

        // Create a ContentValues object where column names are the keys
        ContentValues values = new ContentValues();

        values.put(InventoryContract.InventoryEntry.COLUMN_NAME, nameString);

        if (mImageURI != null) {
            values.put(InventoryEntry.COLUMN_IMAGE, imageString);
        }
        values.put(InventoryContract.InventoryEntry.COLUMN_PRICE, price);
        values.put(InventoryContract.InventoryEntry.COLUMN_QUANTITY, quantity);


        if (mCurrentItemUri == null) {

            Uri uri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, values);
            if (uri == null) {
                Toast.makeText(this, "Error with saving item", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Item saved", Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Update successful", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public static boolean isNumeric(String s)
    {
        try {
           Float.parseFloat(s);
        }
        catch(NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private void DecQuantity() {
        //ContentValues values = new ContentValues();
        String quantity = mQuantityEditText.getText().toString().trim();
        int x = Integer.parseInt(quantity);
        if (x > 0) {
            x--;
            quantity = String.valueOf(x);
            mQuantityEditText.setText(quantity);
        }
    }

    private void incQuantity() {
        //ContentValues values = new ContentValues();
        String quantity = mQuantityEditText.getText().toString().trim();
        int x = Integer.parseInt(quantity);
        x++;
        quantity = String.valueOf(x);
        mQuantityEditText.setText(quantity);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void deleteItem() {

        if (mCurrentItemUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPostUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }

            finish();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveItem();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
