package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract;
import com.example.android.inventoryapp.InventoryActivity;

import static com.example.android.inventoryapp.data.InventoryContract.InventoryEntry.TABLE_NAME;

public class ItemCursorAdapter extends CursorAdapter {

    public ItemCursorAdapter(Context context, Cursor c) {

        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Information that will be displayed.
        TextView prodName = view.findViewById(R.id.prod_name);
        TextView prodPrice = view.findViewById(R.id.price);
        final TextView quantity = view.findViewById(R.id.quantity);
        Button saleBtn = view.findViewById(R.id.sale_btn);

        // Handles sale button being pressed
        saleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ID so the database can be properly updated when the sale button is pressed
                // final int rowID = InventoryContract.InventoryEntry.SKU;

                int newQuantity = Integer.parseInt(quantity.getText().toString().trim());

                if (newQuantity > 0) {
                    newQuantity--;
                } else {
                    Toast.makeText(v.getContext(), "Out of Stock! Cannot be sold!", Toast.LENGTH_SHORT).show();
                }

                quantity.setText(Integer.toString(newQuantity));

               /* InventoryActivity ia = (InventoryActivity) context;
                InventoryActivity.updateAfterSale();*/
            }
        });

        // Extract information
        String pName = cursor.getString(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_PROD_NAME));
        String pPrice = cursor.getString(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_PROD_PRICE));
        String pQuantity = cursor.getString(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_PROD_QUANTITY));

        // Populate text views
        prodName.setText(pName);
        prodPrice.setText(pPrice);
        quantity.setText(pQuantity);
    }
/*
    // Update the database when the sale button is pressed
    private void update(int quantity) {
        ContentValues cv = new ContentValues();
        cv.put(InventoryContract.InventoryEntry.COLUMN_PROD_QUANTITY, int quantity);
        int rowsDeleted = getContentResolver().delete(InventoryContract.InventoryEntry.CONTENT_URI, null, null);
        //db.update(TABLE_NAME, cv, Column + "= ?", new String[] {rowId});
    }*/
}