package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class InventoryContract {

    private InventoryContract() {

    }

    // The name of the content provider
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";

    // This will be used when the content provider will be deciding whether or not to
    // query the entire database
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Path to data about inventory
    public static final String INVENTORY_PATH = "inventory";

    public static final class InventoryEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, INVENTORY_PATH);

        // Mime type of inventory
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + INVENTORY_PATH;

        // Mime type of single item in inventory
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + INVENTORY_PATH;

        // Name of Inventory database table
        public final static String TABLE_NAME = "inventory";

        /**
         * Unique ID number for the item (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String SKU = BaseColumns._ID;

        /**
         * Name of product.
         *
         * Type: TEXT
         */
        public final static String COLUMN_PROD_NAME ="product_name";

        /**
         * Price of product.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_PROD_PRICE ="product_price";

        /**
         * Quantity of product in stock..
         *
         * Type: INTEGER
         */
        public final static String COLUMN_PROD_QUANTITY ="product_quantity";

        /**
         * Name of product supplier.
         *
         * Type: TEXT
         */
        public final static String COLUMN_SUPPL_NAME ="product_supplier";

        /**
         * Phone number of product supplier.
         *
         * Type: TEXT
         */
        public final static String COLUMN_SUPPL_NUM ="supplier_number";
    }
}