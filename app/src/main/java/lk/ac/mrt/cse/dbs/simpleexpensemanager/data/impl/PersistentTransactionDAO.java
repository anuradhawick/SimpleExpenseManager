package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.TransactionDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Transaction;

/**
 * Created by Anuradha Sanjeewa on 03/12/2015.
 */
public class PersistentTransactionDAO extends SQLiteOpenHelper implements TransactionDAO {
    public static final String DATABASE_NAME = "130647R.db";

    public static final String TR_TABLE_NAME = "transactions";
    public static final String ACC_NO_COLUMN_ID = "accountNo";
    public static final String EX_TYPE_COLUMN_NAME = "type";
    public static final String AMT_COLUMN_NAME = "amount";
    public static final String DATE_COLUMN_NAME = "date";

    public PersistentTransactionDAO(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                "CREATE TABLE transactions " +
                        "(accountNo TEXT NOT NULL, type INTEGER NOT NULL,date TEXT NOT NULL,amount REAL NOT NULL)"
        );
        sqLiteDatabase.execSQL(
                "CREATE TABLE accounts " +
                        "(accountNo TEXT PRIMARY KEY NOT NULL, bankName TEXT NOT NULL,accountHolderName TEXT NOT NULL,balance REAL NOT NULL)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS transactions");
        onCreate(sqLiteDatabase);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS accounts");
        onCreate(sqLiteDatabase);
    }

    @Override
    public void logTransaction(Date date, String accountNo, ExpenseType expenseType, double amount) {
        String query = "INSERT INTO transactions VALUES (?,?,?,?)";
        SQLiteDatabase db = this.getWritableDatabase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = sdf.format(date);
        try {
            switch (expenseType) {
                case EXPENSE:
                    db.execSQL(query, new String[]{accountNo, String.valueOf(1), dateString, String.valueOf(amount)});
                    break;
                case INCOME:
                    db.execSQL(query, new String[]{accountNo, String.valueOf(2), dateString, String.valueOf(amount)});
                    break;
            }
        } catch (Exception e) {

        } finally {
            db.close();
        }

    }

    @Override
    public List<Transaction> getAllTransactionLogs() {
        List<Transaction> lst = new ArrayList<Transaction>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM transactions ORDER BY date(`date`) DESC", null);
        res.moveToFirst();
        while (res.isAfterLast() == false) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date d = null;
            try {
                d = sdf.parse(res.getString(res.getColumnIndex(DATE_COLUMN_NAME)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            lst.add(new Transaction(d, res.getString(res.getColumnIndex(ACC_NO_COLUMN_ID)), (res.getInt(res.getColumnIndex(EX_TYPE_COLUMN_NAME)) == 1) ? ExpenseType.EXPENSE : ExpenseType.INCOME, res.getDouble(res.getColumnIndex(AMT_COLUMN_NAME))));
            res.moveToNext();
        }
        db.close();
        return lst;
    }

    @Override
    public List<Transaction> getPaginatedTransactionLogs(int limit) {
        List<Transaction> lst = new ArrayList<Transaction>();
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor res = db.rawQuery("SELECT * FROM transactions ORDER BY date(`date`) DESC LIMIT " + limit, null);
            res.moveToFirst();
            while (res.isAfterLast() == false) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date d = null;
                try {
                    d = sdf.parse(res.getString(res.getColumnIndex(DATE_COLUMN_NAME)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                lst.add(new Transaction(d, res.getString(res.getColumnIndex(ACC_NO_COLUMN_ID)), (res.getInt(res.getColumnIndex(EX_TYPE_COLUMN_NAME)) == 1) ? ExpenseType.EXPENSE : ExpenseType.INCOME, res.getDouble(res.getColumnIndex(AMT_COLUMN_NAME))));
                res.moveToNext();
            }
        } catch (Exception e) {
            return lst;
        } finally {
            db.close();
        }

        return lst;
    }
}
