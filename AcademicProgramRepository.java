package org.svuonline.lms.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.svuonline.lms.data.db.DBContract;
import org.svuonline.lms.data.db.DatabaseHelper;
import org.svuonline.lms.data.model.AcademicProgram;

import java.util.ArrayList;
import java.util.List;

public class AcademicProgramRepository {
    public final DatabaseHelper dbHelper;
    public AcademicProgramRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public String getProgramNameById(long programId, boolean isArabic) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String column = isArabic ? DBContract.AcademicProgram.COL_NAME_AR : DBContract.AcademicProgram.COL_NAME_EN;
        String name = "";
        Cursor cursor = db.query(
                DBContract.AcademicProgram.TABLE_NAME,
                new String[]{column},
                DBContract.AcademicProgram.COL_PROGRAM_ID + " = ?",
                new String[]{String.valueOf(programId)},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndexOrThrow(column));
        }
        cursor.close();
        db.close();
        return name;
    }

    public int getProgramIdByName(String programName, boolean isArabic) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String column = isArabic ? DBContract.AcademicProgram.COL_NAME_AR : DBContract.AcademicProgram.COL_NAME_EN;
        int programId = 0;

        Cursor cursor = db.query(
                DBContract.AcademicProgram.TABLE_NAME,
                new String[]{DBContract.AcademicProgram.COL_PROGRAM_ID},
                column + " = ?",
                new String[]{programName},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            programId = cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.AcademicProgram.COL_PROGRAM_ID));
        }
        cursor.close();
        db.close();
        return programId;
    }

    /**
     * جلب جميع البرامج الأكاديمية من قاعدة البيانات
     */
    public List<AcademicProgram> getAllPrograms() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<AcademicProgram> programs = new ArrayList<>();

        Cursor cursor = db.query(
                DBContract.AcademicProgram.TABLE_NAME,
                null, // جميع الأعمدة
                null, null, null, null,
                DBContract.AcademicProgram.COL_PROGRAM_ID + " ASC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                AcademicProgram program = new AcademicProgram(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.AcademicProgram.COL_PROGRAM_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DBContract.AcademicProgram.COL_CODE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DBContract.AcademicProgram.COL_NAME_EN)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DBContract.AcademicProgram.COL_NAME_AR)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.AcademicProgram.COL_PROGRAM_DURATION))
                );
                programs.add(program);
            }
            cursor.close();
        }
        db.close();
        return programs;
    }

    /**
     * جلب أسماء البرامج حسب اللغة
     */
    public List<String> getProgramNames(boolean isArabic) {
        List<AcademicProgram> programs = getAllPrograms();
        List<String> programNames = new ArrayList<>();

        for (AcademicProgram program : programs) {
            programNames.add(isArabic ? program.getNameAr() : program.getNameEn());
        }

        return programNames;
    }

    // === Academic Program Methods ===

    /**
     * إضافة برنامج أكاديمي جديد
     */
    public long addAcademicProgram(AcademicProgram program) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("code", program.getCode());
        values.put("name_en", program.getNameEn());
        values.put("name_ar", program.getNameAr());
        values.put("program_duration", program.getProgramDuration());

        long result = db.insert("ACADEMIC_PROGRAM", null, values);
        db.close();
        return result;
    }

    /**
     * تحديث برنامج أكاديمي
     */
    public boolean updateAcademicProgram(AcademicProgram program) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("code", program.getCode());
        values.put("name_en", program.getNameEn());
        values.put("name_ar", program.getNameAr());
        values.put("program_duration", program.getProgramDuration());

        int result = db.update("ACADEMIC_PROGRAM", values,
                "program_id = ?", new String[]{String.valueOf(program.getProgramId())});
        db.close();
        return result > 0;
    }

    /**
     * حذف برنامج أكاديمي
     */
    public boolean deleteAcademicProgram(int programId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result = db.delete("ACADEMIC_PROGRAM",
                "program_id = ?", new String[]{String.valueOf(programId)});
        db.close();
        return result > 0;
    }

    /**
     * الحصول على جميع البرامج الأكاديمية
     */
    public List<AcademicProgram> getAllAcademicPrograms() {
        List<AcademicProgram> programs = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("ACADEMIC_PROGRAM",
                null, null, null, null, null, "code ASC");

        if (cursor.moveToFirst()) {
            do {
                AcademicProgram program = new AcademicProgram();
                program.setProgramId(cursor.getInt(cursor.getColumnIndexOrThrow("program_id")));
                program.setCode(cursor.getString(cursor.getColumnIndexOrThrow("code")));
                program.setNameEn(cursor.getString(cursor.getColumnIndexOrThrow("name_en")));
                program.setNameAr(cursor.getString(cursor.getColumnIndexOrThrow("name_ar")));
                program.setProgramDuration(cursor.getInt(cursor.getColumnIndexOrThrow("program_duration")));
                programs.add(program);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return programs;
    }
}