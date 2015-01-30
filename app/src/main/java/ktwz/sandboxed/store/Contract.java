package ktwz.sandboxed.store;

import android.provider.BaseColumns;

/**
 * Created by wil on 1/30/15.
 */
public final class Contract {

    public static abstract class APICallEntry implements BaseColumns {
        public static final String TABLE_NAME = "apicalls";
        public static final String COLUMN_NAME_PACKAGE = "package";
        public static final String COLUMN_NAME_CLASS = "class";
        public static final String COLUMN_NAME_ACCESS = "access";

    }

}
