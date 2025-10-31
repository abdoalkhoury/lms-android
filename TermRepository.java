package org.svuonline.lms.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.svuonline.lms.data.db.DatabaseHelper;
import org.svuonline.lms.data.model.AcademicYear;
import org.svuonline.lms.data.model.Term;

import java.util.ArrayList;
import java.util.List;

public class TermRepository {

    private final DatabaseHelper dbHelper;

    public TermRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // === Term Methods ===

    /**
     * إضافة فصل دراسي جديد
     */
    public long addTerm(Term term) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("academic_year_id", term.getAcademicYearId());
        values.put("name", term.getName());
        values.put("start_date", term.getStartDate());
        values.put("end_date", term.getEndDate());

        long result = db.insert("TERM", null, values);
        db.close();
        return result;
    }

    /**
     * تحديث فصل دراسي
     */
    public boolean updateTerm(Term term) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("academic_year_id", term.getAcademicYearId());
        values.put("name", term.getName());
        values.put("start_date", term.getStartDate());
        values.put("end_date", term.getEndDate());

        int result = db.update("TERM", values,
                "term_id = ?", new String[]{String.valueOf(term.getTermId())});
        db.close();
        return result > 0;
    }

    /**
     * حذف فصل دراسي
     */
    public boolean deleteTerm(long termId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result = db.delete("TERM",
                "term_id = ?", new String[]{String.valueOf(termId)});
        db.close();
        return result > 0;
    }

    /**
     * الحصول على جميع الفصول الدراسية لسنة معينة
     */
    public List<Term> getTermsByAcademicYear(long academicYearId) {
        List<Term> terms = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("TERM",
                null, "academic_year_id = ?", new String[]{String.valueOf(academicYearId)},
                null, null, "start_date ASC");

        if (cursor.moveToFirst()) {
            do {
                Term term = new Term();
                term.setTermId(cursor.getLong(cursor.getColumnIndexOrThrow("term_id")));
                term.setAcademicYearId(cursor.getLong(cursor.getColumnIndexOrThrow("academic_year_id")));
                term.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                term.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow("start_date")));
                term.setEndDate(cursor.getString(cursor.getColumnIndexOrThrow("end_date")));
                terms.add(term);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return terms;
    }

    /**
     * الحصول على فصل دراسي بواسطة ID
     */
    public Term getTermById(long termId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("TERM",
                null, "term_id = ?", new String[]{String.valueOf(termId)},
                null, null, null);

        Term term = null;
        if (cursor.moveToFirst()) {
            term = new Term();
            term.setTermId(cursor.getLong(cursor.getColumnIndexOrThrow("term_id")));
            term.setAcademicYearId(cursor.getLong(cursor.getColumnIndexOrThrow("academic_year_id")));
            term.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            term.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow("start_date")));
            term.setEndDate(cursor.getString(cursor.getColumnIndexOrThrow("end_date")));
        }
        cursor.close();
        db.close();
        return term;
    }

    /**
     * الحصول على جميع الفصول الدراسية
     */
    public List<Term> getAllTerms() {
        List<Term> terms = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("TERM",
                null, null, null, null, null, "start_date DESC");

        if (cursor.moveToFirst()) {
            do {
                Term term = new Term();
                term.setTermId(cursor.getLong(cursor.getColumnIndexOrThrow("term_id")));
                term.setAcademicYearId(cursor.getLong(cursor.getColumnIndexOrThrow("academic_year_id")));
                term.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                term.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow("start_date")));
                term.setEndDate(cursor.getString(cursor.getColumnIndexOrThrow("end_date")));
                terms.add(term);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return terms;
    }
}
