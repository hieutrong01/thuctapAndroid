package com.scanny.scanner.db;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.scanny.scanner.models.DBModel;

import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "DocumentDB";
    private static final int DATABASE_VERSION = 1;
    private static final String KEY_FIRST_IMAGE = "firstimage";
    private static final String KEY_ID = "id";
    private static final String KEY_IMG_NAME = "imgname";
    private static final String KEY_IMG_NOTE = "imgnote";
    private static final String KEY_IMG_PATH = "imgpath";
    private static final String KEY_TABLE_DATE = "date";
    private static final String KEY_TABLE_NAME = "name";
    private static final String KEY_TAG = "tag";
    private static final String TABLE_NAME = "alldocs";
    private static final String TAG = "DBHelper";
    private static final String KEY_NAME = "key_name";
    private SecretKey secretKey;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            initializeKey();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initializeKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                    KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .setRandomizedEncryptionRequired(false)  // Tùy chọn, tùy thuộc vào yêu cầu của bạn
                    .setKeySize(256)  // Tùy chọn, tùy thuộc vào yêu cầu của bạn
                    .build();

            keyGenerator.init(keyGenParameterSpec);
            secretKey = keyGenerator.generateKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void refreshKey() {
        // Tạo khóa mới
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            initializeKey();
        }
    }
    private boolean isKeyExpired() {
        // Kiểm tra xem khóa đã hết hạn hay chưa (tùy thuộc vào logic của bạn)
        // Trong trường hợp này, giả sử mỗi lần addGroup là 10 lần, sau đó khóa được đánh dấu là đã hết hạn.
        // Bạn cần xác định tiêu chí khi mà khóa cần được đổi mới.
        // Dưới đây là một ví dụ đơn giản:
        // return numberOfGroups % 10 == 0;
        return false;
    }
    private String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedData = cipher.doFinal(data.getBytes("UTF-8"));
            return Base64.encodeToString(encryptedData, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String decrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(new byte[cipher.getBlockSize()]));
            byte[] decryptedData = cipher.doFinal(Base64.decode(data, Base64.DEFAULT));
            return new String(decryptedData, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("CREATE TABLE alldocs(id INTEGER PRIMARY KEY,name TEXT,date TEXT,tag TEXT,firstimage TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS alldocs");
        onCreate(sQLiteDatabase);
    }

    public void createDocTable(String str) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        writableDatabase.execSQL("CREATE TABLE '" + str + "'(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_IMG_NAME + " TEXT," + KEY_IMG_NOTE + " TEXT," + KEY_IMG_PATH + " TEXT)");
    }

    public void addGroup(DBModel dBModel,String code) {
        // Kiểm tra và khởi tạo khóa mới nếu cần
        if (isKeyExpired()) {
            refreshKey();

        }
        // Mã hóa dữ liệu với mã nhập từ người dùng
        String encryptedCode = encrypt(code);

        // Tiếp tục xử lý dữ liệu, ví dụ: lưu vào cơ sở dữ liệu
        SQLiteDatabase writableDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", encryptedCode);
        contentValues.put("date", dBModel.getGroup_date());
        contentValues.put(KEY_TAG, dBModel.getGroup_tag());
        contentValues.put(KEY_FIRST_IMAGE, dBModel.getGroup_first_img());
        writableDatabase.insert(TABLE_NAME, (String) null, contentValues);
        writableDatabase.close();
    }

    public void addGroupDoc(String name, String path, String img_name, String note) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_IMG_PATH, path);
        contentValues.put(KEY_IMG_NAME, img_name);
        contentValues.put(KEY_IMG_NOTE, note);
        writableDatabase.insert(name.replace(" ", ""), null, contentValues);
        writableDatabase.close();
    }

    public long moveGroupDoc(String name, String path, String img_name, String note) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_IMG_PATH, path);
        contentValues.put(KEY_IMG_NAME, img_name);
        contentValues.put(KEY_IMG_NOTE, note);
        long value = writableDatabase.insert(name.replace(" ", ""), null, contentValues);
        writableDatabase.close();
        return value;
    }

    public ArrayList<DBModel> getAllGroups() {
        ArrayList<DBModel> arrayList = new ArrayList<>();
        Cursor rawQuery = getReadableDatabase().rawQuery("SELECT  * FROM alldocs", (String[]) null);
        if (rawQuery.moveToFirst()) {
            do {
                DBModel dBModel = new DBModel();
                dBModel.setId(Integer.parseInt(rawQuery.getString(0)));
                dBModel.setGroup_name(rawQuery.getString(1));
                dBModel.setGroup_date(rawQuery.getString(2));
                dBModel.setGroup_tag(rawQuery.getString(3));
                dBModel.setGroup_first_img(rawQuery.getString(4));
                arrayList.add(dBModel);
            } while (rawQuery.moveToNext());
        }
        return arrayList;
    }

    public ArrayList<DBModel> getOnlyAllGroups() {
        ArrayList<DBModel> arrayList = new ArrayList<>();
        Cursor rawQuery = getReadableDatabase().rawQuery("SELECT  * FROM alldocs", (String[]) null);
        if (rawQuery.moveToFirst()) {
            do {
                DBModel dBModel = new DBModel();
                dBModel.setId(Integer.parseInt(rawQuery.getString(0)));
                dBModel.setGroup_name(rawQuery.getString(1));
                dBModel.setGroup_date(rawQuery.getString(2));
                dBModel.setGroup_tag(rawQuery.getString(3));
                dBModel.setGroup_first_img(rawQuery.getString(4));
                if (dBModel.getGroup_first_img().isEmpty()) arrayList.add(dBModel);
            } while (rawQuery.moveToNext());
        }
        return arrayList;
    }

    public ArrayList<DBModel> getGroupsByTag(String str) {
        ArrayList<DBModel> arrayList = new ArrayList<>();
        Cursor rawQuery = getReadableDatabase().rawQuery("SELECT  * FROM alldocs WHERE tag = ?", new String[]{str});
        if (rawQuery.moveToFirst()) {
            do {
                DBModel dBModel = new DBModel();
                dBModel.setId(Integer.parseInt(rawQuery.getString(0)));
                dBModel.setGroup_name(rawQuery.getString(1));
                dBModel.setGroup_date(rawQuery.getString(2));
                dBModel.setGroup_tag(rawQuery.getString(3));
                dBModel.setGroup_first_img(rawQuery.getString(4));
                arrayList.add(dBModel);
            } while (rawQuery.moveToNext());
        }
        return arrayList;
    }

    public ArrayList<DBModel> getGroupDocs(String str) {
        ArrayList<DBModel> arrayList = new ArrayList<>();
        String str2 = "SELECT  * FROM " + str;
        SQLiteDatabase readableDatabase = getReadableDatabase();
        Log.e(TAG, str2);
        Cursor rawQuery = readableDatabase.rawQuery(str2, (String[]) null);
        if (rawQuery.moveToFirst()) {
            do {
                DBModel dBModel = new DBModel();
                dBModel.setId(Integer.parseInt(rawQuery.getString(0)));
                dBModel.setGroup_doc_name(rawQuery.getString(1));
                dBModel.setGroup_doc_note(rawQuery.getString(2));
                dBModel.setGroup_doc_img(rawQuery.getString(3));
                arrayList.add(dBModel);
            } while (rawQuery.moveToNext());
        }
        return arrayList;
    }

    public ArrayList<DBModel> getShareGroupDocs(String groupName) {
        ArrayList<DBModel> arrayList = new ArrayList<>();
        String str2 = "SELECT  * FROM " + groupName;
        SQLiteDatabase readableDatabase = getReadableDatabase();
        Log.e(TAG, str2);
        Cursor rawQuery = readableDatabase.rawQuery(str2, (String[]) null);
        if (rawQuery.moveToFirst()) {
            do {
                DBModel dBModel = new DBModel();
                dBModel.setId(Integer.parseInt(rawQuery.getString(0)));
                dBModel.setGroup_doc_name(rawQuery.getString(1));
                dBModel.setGroup_doc_note(rawQuery.getString(2));
                dBModel.setGroup_doc_img(rawQuery.getString(3));
                if (dBModel.getGroup_doc_name() != null) arrayList.add(dBModel);
            } while (rawQuery.moveToNext());
        }
        return arrayList;
    }


    public String getSingleNote(String str, String str2) {
        Cursor rawQuery = getReadableDatabase().rawQuery("SELECT  * FROM " + str.replace(" ", "") + " WHERE " + KEY_IMG_NAME + " = '" + str2 + "'", (String[]) null);
        if (rawQuery == null) {
            return "";
        }
        rawQuery.moveToFirst();
        String string = rawQuery.getString(2);
        rawQuery.close();
        return string;
    }

    public void deleteGroup(String str) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        writableDatabase.delete(TABLE_NAME, "name = ?", new String[]{String.valueOf(str)});
        writableDatabase.execSQL("DROP TABLE IF EXISTS '" + str.replace(" ", "") + "'");
        writableDatabase.close();
    }

    public void deleteSingleDoc(String str, String str2) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        writableDatabase.delete(str.replace(" ", ""), "imgname = ?", new String[]{String.valueOf(str2)});
        writableDatabase.close();
    }

    private boolean isGroupNameExist(Activity activity, String str) {
        Cursor rawQuery = getReadableDatabase().rawQuery("SELECT * FROM alldocs WHERE name = ?", new String[]{str});
        int count = rawQuery.getCount();
        Log.e(TAG, "isGroupNameExist: " + count);
        if (count > 0) {
            Toast.makeText(activity, "Document Name Already Exist", Toast.LENGTH_SHORT).show();
        }
        rawQuery.close();
        if (count >= 1) {
            return true;
        }
        return false;
    }

    public void updateGroupName(Activity activity, String str, String str2) {
        if (!isGroupNameExist(activity, str2)) {
            SQLiteDatabase writableDatabase = getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("name", str2);
            writableDatabase.update(TABLE_NAME, contentValues, "name = ?", new String[]{String.valueOf(str)});
            writableDatabase.execSQL("ALTER TABLE " + str.replace(" ", "") + " RENAME TO " + str2.replace(" ", ""));
            writableDatabase.close();
        }
    }

    public void updateGroupFirstImg(String str, String str2) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_FIRST_IMAGE, str2);
        writableDatabase.update(TABLE_NAME, contentValues, "name = ?", new String[]{String.valueOf(str)});
        writableDatabase.close();
    }

    public void updateGroupListDoc(String str, String str2, String str3) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_IMG_PATH, str3);
        writableDatabase.update(str.replace(" ", ""), contentValues, "imgname = ?", new String[]{String.valueOf(str2)});
        writableDatabase.close();
    }

    public void updateGroupListDocNote(String str, String str2, String str3) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_IMG_NOTE, str3);
        writableDatabase.update(str.replace(" ", ""), contentValues, "imgname = ?", new String[]{String.valueOf(str2)});
        writableDatabase.close();
    }
}
