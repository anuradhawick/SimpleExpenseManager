package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.sql.SQLException;
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
public class PersistentTransactionDAO extends AbstractDBHelper implements TransactionDAO {

    public PersistentTransactionDAO(Context context) {
        super(context);
    }

    @Override
    public void logTransaction(Date date, String accountNo, ExpenseType expenseType, double amount) {
        String query = "INSERT INTO transactions VALUES (?,?,?,?)";
        SQLiteDatabase db = this.getWritableDatabase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = sdf.format(date);
        try {
            db.execSQL(query, new String[]{accountNo, (expenseType == ExpenseType.EXPENSE) ? String.valueOf(1) : String.valueOf(2), dateString, String.valueOf(amount)});
        } catch (Exception e) {
            // Do nothing
        } finally {
            db.close();
        }

    }

    @Override
    public List<Transaction> getAllTransactionLogs() {
        List<Transaction> lst = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM transactions ORDER BY date(`date`) DESC", null);
        res.moveToFirst();
        while (res.isAfterLast() == false) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date d = null;
            try {
                d = sdf.parse(res.getString(res.getColumnIndex(DATE_COLUMN_NAME)));
            } catch (ParseException e) {
                // Highly Unlikely Exception
            }
            lst.add(new Transaction(d, res.getString(res.getColumnIndex(ACC_NO_COLUMN_ID)), (res.getInt(res.getColumnIndex(EX_TYPE_COLUMN_NAME)) == 1) ? ExpenseType.EXPENSE : ExpenseType.INCOME, res.getDouble(res.getColumnIndex(AMT_COLUMN_NAME))));
            res.moveToNext();
        }
        db.close();
        return lst;
    }

    @Override
    public List<Transaction> getPaginatedTransactionLogs(int limit) {
        List<Transaction> lst = new ArrayList<>();
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
                    // Highly Unlikely Exception
                }
                lst.add(new Transaction(d, res.getString(res.getColumnIndex(ACC_NO_COLUMN_ID)), (res.getInt(res.getColumnIndex(EX_TYPE_COLUMN_NAME)) == 1) ? ExpenseType.EXPENSE : ExpenseType.INCOME, res.getDouble(res.getColumnIndex(AMT_COLUMN_NAME))));
                res.moveToNext();
            }
        } catch (Exception e) {
            // Do nothing
        } finally {
            db.close();
        }
        return lst;
    }
}
