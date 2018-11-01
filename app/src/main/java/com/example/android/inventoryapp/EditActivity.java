package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class EditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String LOG_TAG = EditActivity.class.getSimpleName();

    // Prevents edit activity from exiting until user fills in fields
    boolean checkExisting = true;

    EditText supplNum;

    // Where user will be able to edit the product's name
    private EditText pNameET;

    // Where user will be able to edit the product's price
    private EditText pPriceET;

    // Where user will be able to edit the product's quantity
    private ImageButton decrement;

    private TextView pQuantityTV;

    private ImageButton increment;

    // Where user will be able to edit the supplier's name
    private EditText sNameET;

    // Where user will be able to edit the supplier's number
    private EditText sNumET;

    // Delete button
    private Button deleteBtn;

    // Checks if any edits have been made
    private boolean changed = false;

    private View.OnTouchListener listener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            changed = true;
            return false;
        }
    };

    // Identifier for the item data loader
    private static final int EXISTING_ITEM_LOADER = 0;

    // Content URI for the current item
    private Uri currentItemUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_detail);

        Intent intent = getIntent();
        currentItemUri = intent.getData();

        if (currentItemUri == null) {
            setTitle(getString(R.string.add_product));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a item that hasn't been created yet.)
            invalidateOptionsMenu();

            // Disable call button
            ImageButton callBtn = findViewById(R.id.make_call);
            callBtn.setEnabled(false);

            // Hide delete button as product doesn't exist yet
            deleteBtn = findViewById(R.id.delete_btn);
            deleteBtn.setVisibility(View.INVISIBLE);
        } else {
            setTitle(getString(R.string.edit_product));

            // Initialize a loader to read the item data from the database
            // and display the current values in the editor
            getSupportLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        pNameET = findViewById(R.id.edit_prod_name);
        pPriceET = findViewById(R.id.edit_prod_price);
        decrement = findViewById(R.id.decrement);
        pQuantityTV = findViewById(R.id.edit_quantity);
        increment = findViewById(R.id.increment);
        sNameET = findViewById(R.id.edit_supp_name);
        sNumET = findViewById(R.id.edit_supp_num);

        // Set Listeners on the editable fields
        pNameET.setOnTouchListener(listener);
        pPriceET.setOnTouchListener(listener);
        sNameET.setOnTouchListener(listener);
        sNumET.setOnTouchListener(listener);

        ImageButton callBtn = findViewById(R.id.make_call);

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                supplNum = findViewById(R.id.edit_supp_num);

                String toDial = "tel:" + supplNum.getText().toString().trim();

                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse(toDial));

                startActivity(callIntent);
            }
        });

        deleteBtn = findViewById(R.id.delete_btn);

        // Add listener to delete button
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });
    }

    // Decrease quantity and ensure it doesn't go below 0
    public void decrease(View view) {
        changed = true;
        TextView quantity = findViewById(R.id.edit_quantity);
        int newQuantity = Integer.parseInt(quantity.getText().toString().trim());
        if (newQuantity > 0) {
            newQuantity--;
        } else {
            //Notify user the quantity cannot be below 0
            Toast.makeText(this, getString(R.string.below_0_warning), Toast.LENGTH_SHORT).show();
        }

        // Update quantity
        quantity.setText(Integer.toString(newQuantity));
    }

    // Increase quantity
    public void increase(View view) {
        changed = true;
        TextView quantity = findViewById(R.id.edit_quantity);
        int newQuantity = Integer.parseInt(quantity.getText().toString().trim());
        newQuantity++;

        quantity.setText(Integer.toString(newQuantity));
    }

    private void saveItem() {
        String pName = pNameET.getText().toString().trim();
        String pPrice = pPriceET.getText().toString().trim();
        String pQuantity = pQuantityTV.getText().toString().trim();
        String sName = sNameET.getText().toString().trim();
        String sNum = sNumET.getText().toString().trim();

        if (currentItemUri == null && TextUtils.isEmpty(pName) && TextUtils.isEmpty(pPrice) && TextUtils.isEmpty(pQuantity)
                && TextUtils.isEmpty(sName) && TextUtils.isEmpty(sNum)) {
            return;
        }

        ContentValues cv = new ContentValues();
        // Makes checks before saving
        if (TextUtils.isEmpty(pName)) {
            Toast.makeText(this, "Product name required", Toast.LENGTH_SHORT).show();
            checkExisting = false;
        } else if (TextUtils.isEmpty(pPrice)) {
            Toast.makeText(this, "Product price required", Toast.LENGTH_SHORT).show();
            checkExisting = false;
        } else if (TextUtils.isEmpty(sName)) {
            Toast.makeText(this, "Supplier name required", Toast.LENGTH_SHORT).show();
            checkExisting = false;
        } else if (TextUtils.isEmpty(sNum)) {
            Toast.makeText(this, "Supplier number required", Toast.LENGTH_SHORT).show();
            checkExisting = false;
        } else {
            // If all required fields are filled in then put item in database
            cv.put(InventoryContract.InventoryEntry.COLUMN_PROD_NAME, pName);

            int price = Integer.parseInt(pPrice);
            cv.put(InventoryContract.InventoryEntry.COLUMN_PROD_PRICE, price);

            int q = 0;
            if (!TextUtils.isEmpty(pQuantity)) {
                q = Integer.parseInt(pQuantity);
                cv.put(InventoryContract.InventoryEntry.COLUMN_PROD_QUANTITY, q);
            } else {
                cv.put(InventoryContract.InventoryEntry.COLUMN_PROD_QUANTITY, q);
            }

            cv.put(InventoryContract.InventoryEntry.COLUMN_SUPPL_NAME, sName);
            cv.put(InventoryContract.InventoryEntry.COLUMN_SUPPL_NUM, sNum);
            // Check whether or not this is an existing item
            if (currentItemUri == null) {
                //This is a new item so insert it into the provider
                Uri newUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, cv);

                // Show toast based on success of save
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, getString(R.string.error_saving), Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, it was successfully saved
                    Toast.makeText(this, getString(R.string.success_saving),Toast.LENGTH_SHORT).show();
                }
            } else {
                // There is an existing item so update it
                int rowsAffected = getContentResolver().update(currentItemUri, cv, null, null);

                // Show toast based on success of update
                if (rowsAffected == 0) {
                    // There was an error
                    Toast.makeText(this, getString(R.string.error_udating), Toast.LENGTH_SHORT).show();
                } else {
                    // Successfully update
                    Toast.makeText(this, getString(R.string.success_updating), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu options
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new item, hide the "Delete" menu item.
        if (currentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                //Save item to database
                if (changed) {
                    saveItem();
                }
                //Exit activity
                if (checkExisting) {
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity
                // which is the {@link MainActivity}.
                if (!changed) {
                    NavUtils.navigateUpFromSameTask(EditActivity.this);
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
                                NavUtils.navigateUpFromSameTask(EditActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with handling back button press
        if (!changed) {
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

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_warning);
        builder.setPositiveButton(R.string.discard_item, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Allows user to verify whether or not they would like to delete an item
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_warning);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        //
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the item in the database.
     */
    private void deleteItem() {
        // Only perform the delete if this is an existing item.
        if (currentItemUri != null) {
            // Call the ContentResolver to delete the item at the given content URI.
            int rowsDeleted = getContentResolver().delete(currentItemUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.delete_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.delete_success),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all item attributes, define a projection that contains
        // all columns from the table
        String[] projection = {
                InventoryEntry.SKU,
                InventoryEntry.COLUMN_PROD_NAME,
                InventoryEntry.COLUMN_PROD_PRICE,
                InventoryEntry.COLUMN_PROD_QUANTITY,
                InventoryEntry.COLUMN_SUPPL_NAME,
                InventoryEntry.COLUMN_SUPPL_NUM};

        // This loader will execute the ContentProvider's query method on a background thread
        return new android.support.v4.content.CursorLoader(
                this,   // Parent activity context
                currentItemUri,         // Query the content URI for the current item
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                               // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find columns that are necessary tyo update views
            int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PROD_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PROD_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PROD_QUANTITY);
            int sNameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_SUPPL_NAME);
            int sNumColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_SUPPL_NUM);

            // Extract values from the cursor for the given column index
            String prodName = cursor.getString(nameColumnIndex);
            int prodPrice = cursor.getInt(priceColumnIndex);
            int prodQuantity = cursor.getInt(quantityColumnIndex);
            String supName = cursor.getString(sNameColumnIndex);
            String supNum = cursor.getString(sNumColumnIndex);

            //Update views
            pNameET.setText(prodName);
            pPriceET.setText(Integer.toString(prodPrice));
            pQuantityTV.setText(Integer.toString(prodQuantity));
            sNameET.setText(supName);
            sNumET.setText(supNum);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        pNameET.setText("");
        pPriceET.setText("");
        pQuantityTV.setText("0");
        sNameET.setText("");
        sNumET.setText("");
    }

}