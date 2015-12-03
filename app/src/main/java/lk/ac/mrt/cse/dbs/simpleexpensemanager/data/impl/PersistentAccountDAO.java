package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.AccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;

/**
 * Created by Anuradha Sanjeewa on 03/12/2015.
 */
public class PersistentAccountDAO extends SQLiteOpenHelper implements AccountDAO {
    public static final String DATABASE_NAME = "130647R.db";

    public static final String ACC_TABLE_NAME = "accounts";
    public static final String ACC_NO_COLUMN_ID = "accountNo";
    public static final String BANK_NAME_COLUMN_NAME = "bankName";
    public static final String ACC_HOLDER_COLUMN_NAME = "accountHolderName";
    public static final String BALANCE_COLUMN_NAME = "balance";

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                "CREATE TABLE accounts " +
                        "(accountNo TEXT PRIMARY KEY NOT NULL, bankName TEXT NOT NULL,accountHolderName TEXT NOT NULL,balance REAL NOT NULL)"
        );
        sqLiteDatabase.execSQL(
                "CREATE TABLE transactions " +
                        "(accountNo TEXT NOT NULL, type INTEGER NOT NULL,date TEXT NOT NULL,amount REAL NOT NULL)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS accounts");
        onCreate(sqLiteDatabase);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS transactions");
        onCreate(sqLiteDatabase);
    }

    public PersistentAccountDAO(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public List<String> getAccountNumbersList() {
        // Return the list of account numbers
        List<String> accNums = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT accountNo FROM accounts";
        Cursor res = db.rawQuery(query, null);
        res.moveToFirst();
        while (res.isAfterLast() == false) {
            accNums.add(res.getString(res.getColumnIndex(ACC_NO_COLUMN_ID)));
            res.moveToNext();
        }
        db.close();
        return accNums;
    }

    @Override
    public List<Account> getAccountsList() {
        //Get the list of accounts as objects
        List<Account> accLs = new ArrayList<Account>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM accounts";
        Cursor res = db.rawQuery(query, null);
        res.moveToFirst();
        while (res.isAfterLast() == false) {
            accLs.add(new Account(res.getString(res.getColumnIndex(ACC_NO_COLUMN_ID)), res.getString(res.getColumnIndex(BANK_NAME_COLUMN_NAME)), res.getString(res.getColumnIndex(ACC_HOLDER_COLUMN_NAME)), res.getDouble(res.getColumnIndex(BALANCE_COLUMN_NAME))));
        }
        db.close();
        return accLs;
    }

    @Override
    public Account getAccount(String accountNo) throws InvalidAccountException {
        // Check if the account exist and return accordingly
        List<Account> accLs = new ArrayList<Account>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM accounts WHERE accountNo = ?";
        Cursor res = db.rawQuery(query, new String[]{accountNo});
        if (res.getCount() == 1) {
            return new Account(res.getString(res.getColumnIndex(ACC_NO_COLUMN_ID)), res.getString(res.getColumnIndex(BANK_NAME_COLUMN_NAME)), res.getString(res.getColumnIndex(ACC_HOLDER_COLUMN_NAME)), res.getDouble(res.getColumnIndex(BALANCE_COLUMN_NAME)));
        }
        db.close();
        String msg = "Account " + accountNo + " is invalid.";
        throw new InvalidAccountException(msg);
    }

    @Override
    public void addAccount(Account account) {
        // Save the account object to the DB
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "INSERT INTO accounts VALUES (?,?,?,?)";
        db.execSQL(query, new String[]{account.getAccountNo(), account.getBankName(), account.getAccountHolderName(), String.valueOf(account.getBalance())});

        db.close();
    }

    @Override
    public void removeAccount(String accountNo) throws InvalidAccountException {
        // Remove the accound from DB given the account number
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(ACC_TABLE_NAME, ACC_NO_COLUMN_ID + "=?", new String[]{accountNo});
        } catch (Exception e) {
            String msg = "Account " + accountNo + " is invalid.";
            throw new InvalidAccountException(msg);
        } finally {
            db.close();
        }
    }

    @Override
    public void updateBalance(String accountNo, ExpenseType expenseType, double amount) throws InvalidAccountException {
        // Update the DB entry
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            Cursor res = db.rawQuery("SELECT * FROM accounts WHERE accountNo = ?", new String[]{accountNo});
            if (res.getCount() == 0) {
                String msg = "Account " + accountNo + " is invalid.";
                throw new InvalidAccountException(msg);
            }
            // specific implementation based on the transaction type
            res.moveToFirst();
            double bal = res.getDouble(res.getColumnIndex(BALANCE_COLUMN_NAME));
            switch (expenseType) {
                case EXPENSE:
                    db.execSQL("UPDATE accounts SET balance = ? WHERE accountNo = ?", new String[]{String.valueOf(bal - amount), accountNo});
                    break;
                case INCOME:
                    db.execSQL("UPDATE accounts SET balance = ? WHERE accountNo = ?", new String[]{String.valueOf(bal + amount), accountNo});
                    break;
            }
        } catch (Exception e) {

        } finally {
            db.close();
        }

    }

}
