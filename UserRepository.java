package org.svuonline.lms.data.repository;

import static org.svuonline.lms.utils.DateTimeHelper.getCurrentDateTime;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.svuonline.lms.R;
import org.svuonline.lms.data.db.DBContract;
import org.svuonline.lms.data.db.DatabaseHelper;
import org.svuonline.lms.data.model.User;
import org.svuonline.lms.ui.data.CourseCardData;

import java.util.ArrayList;
import java.util.List;

/**
 * مستودع البيانات لإدارة عمليات المستخدمين في قاعدة البيانات.
 */
public class UserRepository {
    private static final String TAG = "UserRepository";
    private final DatabaseHelper dbHelper;

    /**
     * نتيجة عملية تسجيل الدخول
     */
    public static class LoginResult {
        private boolean success;
        private int userId;
        private String userRole;

        public LoginResult(boolean success, int userId, String userRole) {
            this.success = success;
            this.userId = userId;
            this.userRole = userRole;
        }

        public boolean isSuccess() { return success; }
        public long getUserId() { return userId; }
        public String getUserRole() { return userRole; }

        public void setSuccess(boolean success) { this.success = success; }
        public void setUserId(int userId) { this.userId = userId; }
        public void setUserRole(String userRole) { this.userRole = userRole; }
    }

    /**
     * باني المستودع.
     *
     * @param context السياق للوصول إلى قاعدة البيانات
     */
    public UserRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    /**
     * تسجيل الدخول مع استرجاع دور المستخدم
     *
     * @param emailOrUsername البريد الإلكتروني أو اسم المستخدم
     * @param passwordHash    هاش كلمة المرور
     * @return كائن LoginResult يحتوي على نتيجة التسجيل ومعرف المستخدم ودوره
     */
    public LoginResult loginUserWithRole(String emailOrUsername, String passwordHash) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            String input = emailOrUsername.toLowerCase();

            if (!input.contains("@")) {
                input = input + "@svuonline.org";
            }

            String[] columns = {
                    DBContract.Users.COL_USER_ID,
                    DBContract.Users.COL_ROLE,
                    DBContract.Users.COL_ACCOUNT_STATUS
            };
            String selection = DBContract.Users.COL_EMAIL + " = ? AND " +
                    DBContract.Users.COL_PASSWORD_HASH + " = ? AND " +
                    "LOWER(" + DBContract.Users.COL_ACCOUNT_STATUS + ") = ?";
            String[] selectionArgs = {input, passwordHash, "active"};

            try (Cursor cursor = db.query(
                    DBContract.Users.TABLE_NAME,
                    columns,
                    selection,
                    selectionArgs,
                    null, null, null)) {
                if (cursor.moveToFirst()) {
                    int userId = cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.Users.COL_USER_ID));
                    String userRole = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_ROLE));
                    return new LoginResult(true, userId, userRole);
                } else {
                    return new LoginResult(false, -1, "");
                }
            }
        }
    }

    /**
     * تسجيل جميع المستخدمين في قاعدة البيانات لأغراض التصحيح.
     */
    public void logAllUsers() {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.rawQuery("SELECT email, password_hash, role, account_status FROM " +
                     DBContract.Users.TABLE_NAME, null)) {
            if (cursor.moveToFirst()) {
                do {
                    String email = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_EMAIL));
                    String passwordHash = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_PASSWORD_HASH));
                    String role = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_ROLE));
                    String accountStatus = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_ACCOUNT_STATUS));

                } while (cursor.moveToNext());
            } else {
            }
        }
    }

    /**
     * استرجاع بيانات المستخدم بناءً على معرفه.
     *
     * @param userId معرف المستخدم
     * @return كائن المستخدم أو null إذا لم يتم العثور على المستخدم
     */
    /*public User getUserById(long userId) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            String[] columns = {
                    DBContract.Users.COL_USER_ID,
                    DBContract.Users.COL_NAME_EN,
                    DBContract.Users.COL_NAME_AR,
                    DBContract.Users.COL_EMAIL,
                    DBContract.Users.COL_ROLE,
                    DBContract.Users.COL_ACCOUNT_STATUS,
                    DBContract.Users.COL_PHONE,
                    DBContract.Users.COL_FACEBOOK_URL,
                    DBContract.Users.COL_WHATSAPP_NUMBER,
                    DBContract.Users.COL_TELEGRAM_HANDLE,
                    DBContract.Users.COL_PROFILE_PICTURE,
                    DBContract.Users.COL_BIO_EN,
                    DBContract.Users.COL_BIO_AR,
                    DBContract.Users.COL_PROGRAM_ID // إضافة معرف البرنامج الأكاديمي
            };
            String selection = DBContract.Users.COL_USER_ID + " = ?";
            String[] selectionArgs = {String.valueOf(userId)};

            try (Cursor cursor = db.query(
                    DBContract.Users.TABLE_NAME,
                    columns,
                    selection,
                    selectionArgs,
                    null, null, null)) {
                if (cursor.moveToFirst()) {
                    User user = new User(
                            cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.Users.COL_USER_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_NAME_EN)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_NAME_AR)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_EMAIL)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_ROLE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_ACCOUNT_STATUS)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_PHONE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_FACEBOOK_URL)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_WHATSAPP_NUMBER)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_TELEGRAM_HANDLE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_PROFILE_PICTURE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_BIO_EN)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_BIO_AR)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.Users.COL_PROGRAM_ID))
                    );
                    return user;
                } else {
                    return null;
                }
            }
        }
    }*/

    /**
     * استرجاع اسم البرنامج الأكاديمي بناءً على معرفه وإعداد اللغة.
     *
     * @param programId معرف البرنامج الأكاديمي
     * @param isArabic  إذا كان true، يتم استرجاع الاسم العربي؛ وإلا يتم استرجاع الاسم الإنجليزي
     * @return اسم البرنامج الأكاديمي أو سلسلة فارغة إذا لم يتم العثور عليه
     */
    public String getProgramNameById(int programId, boolean isArabic) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            String column = isArabic ? DBContract.AcademicProgram.COL_NAME_AR : DBContract.AcademicProgram.COL_NAME_EN;
            String[] columns = {column};
            String selection = DBContract.AcademicProgram.COL_PROGRAM_ID + " = ?";
            String[] selectionArgs = {String.valueOf(programId)};

            try (Cursor cursor = db.query(
                    DBContract.AcademicProgram.TABLE_NAME,
                    columns,
                    selection,
                    selectionArgs,
                    null, null, null)) {
                if (cursor.moveToFirst()) {
                    String programName = cursor.getString(cursor.getColumnIndexOrThrow(column));
                    return programName != null ? programName : "";
                } else {
                    return "";
                }
            }
        }
    }


    /**
     * جلب قائمة المقررات التي سجل فيها المستخدم (Passed أو Registered).
     *
     * @param userId   معرف المستخدم
     * @param isArabic إذا كان true، يتم استرجاع أسماء المقررات والبرامج بالعربية
     * @return قائمة بكائنات CourseCardData
     */
    public List<CourseCardData> getUserCourses(long userId, boolean isArabic) {
        List<CourseCardData> courseList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] columns = {
                "c." + DBContract.Course.COL_COURSE_ID,
                "c." + DBContract.Course.COL_CODE,
                "c." + DBContract.Course.COL_NAME_EN,
                "c." + DBContract.Course.COL_NAME_AR,
                "c." + DBContract.Course.COL_COLOR,
                "c." + DBContract.Course.COL_PROGRAM_ID,
                "e." + DBContract.Enrollment.COL_IS_FAVORITE,
                "e." + DBContract.Enrollment.COL_COURSE_STATUS
        };

        String selection = "e." + DBContract.Enrollment.COL_USER_ID + " = ?" +
                " AND e." + DBContract.Enrollment.COL_COURSE_ID + " = c." + DBContract.Course.COL_COURSE_ID +
                " AND e." + DBContract.Enrollment.COL_COURSE_STATUS + " IN ('Passed', 'Registered')";
        String[] selectionArgs = {String.valueOf(userId)};

        try (Cursor cursor = db.query(
                DBContract.Course.TABLE_NAME + " c, " +
                        DBContract.Enrollment.TABLE_NAME + " e",
                columns,
                selection,
                selectionArgs,
                null, null, null)) {
            if (cursor.moveToFirst()) {
                do {
                    long courseId = cursor.getLong(cursor.getColumnIndexOrThrow(DBContract.Course.COL_COURSE_ID));
                    String courseCode = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Course.COL_CODE));
                    String courseNameEn = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Course.COL_NAME_EN));
                    String courseNameAr = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Course.COL_NAME_AR));
                    String color = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Course.COL_COLOR));
                    int programId = cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.Course.COL_PROGRAM_ID));
                    boolean isFavorite = cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.Enrollment.COL_IS_FAVORITE)) == 1;
                    String courseStatus = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Enrollment.COL_COURSE_STATUS));

                    // تحديد اسم المقرر بناءً على اللغة
                    String courseName = isArabic && courseNameAr != null && !courseNameAr.isEmpty() ? courseNameAr : courseNameEn;

                    // جلب اسم البرنامج الأكاديمي
                    String programName = getProgramNameById(programId, isArabic);

                    // تحديد اللون
                    int courseColor = getColorFromString(color);

                    // تحديد الحالات
                    boolean isPassed = courseStatus.equals("Passed");
                    boolean isRegistered = courseStatus.equals("Registered");

                    CourseCardData course = new CourseCardData(
                            courseId,
                            courseCode,
                            programName,
                            courseName,
                            false, // isNew (غير متوفر في البيانات، يمكن تعديله إذا لزم الأمر)
                            isRegistered,
                            isPassed,
                            false, // isRemaining (لأننا نستبعد الحالة Remaining)
                            courseColor
                    );
                    courseList.add(course);
                } while (cursor.moveToNext());
            }
        } catch (Exception ignored) {
        }

        return courseList;
    }

    /**
     * تحويل اسم اللون إلى معرف اللون.
     */
    private int getColorFromString(String color) {
        if (color == null) return R.color.Custom_MainColorBlue;
        switch (color) {
            case "Custom_MainColorPurple":
                return R.color.Custom_MainColorPurple;
            case "Custom_MainColorDarkPink":
                return R.color.Custom_MainColorDarkPink;
            case "Custom_MainColorBlue":
                return R.color.Custom_MainColorBlue;
            case "Custom_MainColorGreen":
                return R.color.Custom_MainColorGreen;
            case "Custom_MainColorGolden":
                return R.color.Custom_MainColorGolden;
            case "Custom_MainColorOrange":
                return R.color.Custom_MainColorOrange;
            case "Custom_MainColorTeal":
                return R.color.Custom_MainColorTeal;
            default:
                return R.color.Custom_MainColorBlue; // لون افتراضي
        }
    }

    /** حدّث بيانات المستخدم كاملة مع الـ bio باللغتين */
    public boolean updateUser(long userId,
                              String phone,
                              String whatsapp,
                              String facebook,
                              String telegram,
                              String email,
                              String bioEn,
                              String bioAr,
                              String profilePictureUri) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBContract.Users.COL_PHONE, phone);
        cv.put(DBContract.Users.COL_WHATSAPP_NUMBER, whatsapp);
        cv.put(DBContract.Users.COL_FACEBOOK_URL, facebook);
        cv.put(DBContract.Users.COL_TELEGRAM_HANDLE, telegram);
        cv.put(DBContract.Users.COL_EMAIL, email);
        cv.put(DBContract.Users.COL_BIO_EN, bioEn);
        cv.put(DBContract.Users.COL_BIO_AR, bioAr);
        cv.put(DBContract.Users.COL_PROFILE_PICTURE, profilePictureUri);

        int rows = db.update(
                DBContract.Users.TABLE_NAME,
                cv,
                DBContract.Users.COL_USER_ID + " = ?",
                new String[]{ String.valueOf(userId) }
        );
        return rows > 0;
    }

    /**
     * Reset all favorite courses for a user by setting isFavorite to 0.
     *
     * @param userId The ID of the user
     * @return true if the update was successful, false otherwise
     */
    public boolean resetFavoriteCourses(long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBContract.Enrollment.COL_IS_FAVORITE, 0);

        int rowsAffected = db.update(
                DBContract.Enrollment.TABLE_NAME,
                values,
                DBContract.Enrollment.COL_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );

        return rowsAffected > 0;
    }

    /**
     * Fetch the list of favorite courses for the user.
     *
     * @param userId   The ID of the user
     * @param isArabic If true, retrieve course and program names in Arabic
     * @return List of CourseCardData objects for favorite courses
     */
    public List<CourseCardData> getFavoriteCourses(long userId, boolean isArabic) {
        List<CourseCardData> favoriteList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] columns = {
                "c." + DBContract.Course.COL_COURSE_ID,
                "c." + DBContract.Course.COL_CODE,
                "c." + DBContract.Course.COL_NAME_EN,
                "c." + DBContract.Course.COL_NAME_AR,
                "c." + DBContract.Course.COL_COLOR,
                "c." + DBContract.Course.COL_PROGRAM_ID,
                "e." + DBContract.Enrollment.COL_IS_FAVORITE,
                "e." + DBContract.Enrollment.COL_COURSE_STATUS
        };

        String selection = "e." + DBContract.Enrollment.COL_USER_ID + " = ?" +
                " AND e." + DBContract.Enrollment.COL_COURSE_ID + " = c." + DBContract.Course.COL_COURSE_ID +
                " AND e." + DBContract.Enrollment.COL_IS_FAVORITE + " = 1";
        String[] selectionArgs = {String.valueOf(userId)};

        try (Cursor cursor = db.query(
                DBContract.Course.TABLE_NAME + " c, " +
                        DBContract.Enrollment.TABLE_NAME + " e",
                columns,
                selection,
                selectionArgs,
                null, null, null)) {
            if (cursor.moveToFirst()) {
                do {
                    long courseId = cursor.getLong(cursor.getColumnIndexOrThrow(DBContract.Course.COL_COURSE_ID));
                    String courseCode = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Course.COL_CODE));
                    String courseNameEn = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Course.COL_NAME_EN));
                    String courseNameAr = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Course.COL_NAME_AR));
                    String color = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Course.COL_COLOR));
                    int programId = cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.Course.COL_PROGRAM_ID));
                    boolean isFavorite = cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.Enrollment.COL_IS_FAVORITE)) == 1;
                    String courseStatus = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Enrollment.COL_COURSE_STATUS));

                    // Determine course name based on language
                    String courseName = isArabic && courseNameAr != null && !courseNameAr.isEmpty() ? courseNameAr : courseNameEn;

                    // Fetch program name
                    String programName = getProgramNameById(programId, isArabic);

                    // Determine color
                    int courseColor = getColorFromString(color);

                    // Determine status flags
                    boolean isPassed = courseStatus.equals("Passed");
                    boolean isRegistered = courseStatus.equals("Registered");
                    boolean isRemaining = courseStatus.equals("Remaining");

                    CourseCardData course = new CourseCardData(
                            courseId,
                            courseCode,
                            programName,
                            courseName,
                            false, // isNew (not available in data, can be modified if needed)
                            isRegistered,
                            isPassed,
                            isRemaining,
                            courseColor
                    );
                    favoriteList.add(course);
                } while (cursor.moveToNext());
            }
        } catch (Exception ignored) {
        }

        return favoriteList;
    }

    /**
     * جلب جميع المستخدمين مع إمكانية التصفية
     */
    /**
     * جلب جميع المستخدمين مع إمكانية التصفية
     */
    public List<User> getAllUsers(String roleFilter, String statusFilter, String searchQuery) {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        StringBuilder queryBuilder = new StringBuilder();
        List<String> selectionArgs = new ArrayList<>();

        queryBuilder.append("SELECT * FROM ").append(DBContract.Users.TABLE_NAME).append(" WHERE 1=1");

        // تصفية حسب الدور - استخدام LIKE للدعم المختلف للكتابة
        // داخل دالة getAllUsers، استبدل جزء تصفية الدور بهذا:
        if (roleFilter != null && !roleFilter.isEmpty()) {
            if (roleFilter.contains(",")) {
                // فلاتر متعددة
                String[] roles = roleFilter.split(",");
                queryBuilder.append(" AND (");
                for (int i = 0; i < roles.length; i++) {
                    if (i > 0) queryBuilder.append(" OR ");
                    queryBuilder.append("LOWER(").append(DBContract.Users.COL_ROLE).append(") LIKE LOWER(?)");
                    selectionArgs.add("%" + roles[i].trim() + "%");
                }
                queryBuilder.append(")");
            } else {
                // فلتر واحد
                queryBuilder.append(" AND LOWER(").append(DBContract.Users.COL_ROLE).append(") LIKE LOWER(?)");
                selectionArgs.add("%" + roleFilter + "%");
            }
        }

        // تصفية حسب الحالة
        if (statusFilter != null && !statusFilter.isEmpty()) {
            queryBuilder.append(" AND LOWER(").append(DBContract.Users.COL_ACCOUNT_STATUS).append(") = LOWER(?)");
            selectionArgs.add(statusFilter);
        }

        // بحث بالاسم أو البريد
        if (searchQuery != null && !searchQuery.isEmpty()) {
            queryBuilder.append(" AND (")
                    .append(DBContract.Users.COL_NAME_EN).append(" LIKE ? OR ")
                    .append(DBContract.Users.COL_NAME_AR).append(" LIKE ? OR ")
                    .append(DBContract.Users.COL_EMAIL).append(" LIKE ? OR ")
                    .append(DBContract.Users.COL_ROLE).append(" LIKE ?)");
            String searchPattern = "%" + searchQuery + "%";
            selectionArgs.add(searchPattern);
            selectionArgs.add(searchPattern);
            selectionArgs.add(searchPattern);
            selectionArgs.add(searchPattern);
        }

        queryBuilder.append(" ORDER BY ").append(DBContract.Users.COL_NAME_EN);

        // كود تصحيح
        Log.d("UserRepository", "SQL Query: " + queryBuilder.toString());
        Log.d("UserRepository", "Query Args: " + selectionArgs.toString());

        try (Cursor cursor = db.rawQuery(queryBuilder.toString(), selectionArgs.toArray(new String[0]))) {
            Log.d("UserRepository", "Cursor count: " + cursor.getCount());

            if (cursor.moveToFirst()) {
                do {
                    User user = extractUserFromCursor(cursor);
                    users.add(user);
                    Log.d("UserRepository", "Found user: " + user.getNameEn() + " - Role: " + user.getRole());
                } while (cursor.moveToNext());
            } else {
                Log.d("UserRepository", "No users found with current filters");
            }
        } catch (Exception e) {
            Log.e("UserRepository", "Error getting users: " + e.getMessage());
            e.printStackTrace();
        }

        return users;
    }

    /**
     * دالة مساعدة للتصحيح: عرض جميع المستخدمين في قاعدة البيانات
     */
    public void debugAllUsers() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.rawQuery("SELECT * FROM " + DBContract.Users.TABLE_NAME, null)) {
            Log.d("UserRepository", "=== ALL USERS IN DATABASE ===");
            if (cursor.moveToFirst()) {
                do {
                    long userId = cursor.getLong(cursor.getColumnIndexOrThrow(DBContract.Users.COL_USER_ID));
                    String nameEn = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_NAME_EN));
                    String role = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_ROLE));
                    String status = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_ACCOUNT_STATUS));

                    Log.d("UserRepository", "User: " + nameEn + " | Role: " + role + " | Status: " + status + " | ID: " + userId);
                } while (cursor.moveToNext());
            }
            Log.d("UserRepository", "=== END ALL USERS ===");
        } catch (Exception e) {
            Log.e("UserRepository", "Error in debug: " + e.getMessage());
        }
    }

    /**
     * استخراج بيانات المستخدم من Cursor
     */
    private User extractUserFromCursor(Cursor cursor) {
        return new User(
                cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.Users.COL_USER_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_NAME_EN)),
                cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_NAME_AR)),
                cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_EMAIL)),
                cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_ROLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_ACCOUNT_STATUS)),
                cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_PHONE)),
                cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_FACEBOOK_URL)),
                cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_WHATSAPP_NUMBER)),
                cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_TELEGRAM_HANDLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_PROFILE_PICTURE)),
                cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_BIO_EN)),
                cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_BIO_AR)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.Users.COL_PROGRAM_ID))
        );
    }

    /**
     * إضافة مستخدم جديد
     */
    public boolean addUser(User user, String passwordHash) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DBContract.Users.COL_NAME_EN, user.getNameEn());
        values.put(DBContract.Users.COL_NAME_AR, user.getNameAr());
        values.put(DBContract.Users.COL_EMAIL, user.getEmail());
        values.put(DBContract.Users.COL_PASSWORD_HASH, passwordHash);
        values.put(DBContract.Users.COL_ROLE, user.getRole());
        values.put(DBContract.Users.COL_ACCOUNT_STATUS, user.getAccountStatus());
        values.put(DBContract.Users.COL_PHONE, user.getPhone());
        values.put(DBContract.Users.COL_PROGRAM_ID, user.getProgramId());
        values.put(DBContract.Users.COL_PROFILE_PICTURE, user.getProfilePicture());

        long result = db.insert(DBContract.Users.TABLE_NAME, null, values);
        return result != -1;
    }

    /**
     * تحديث بيانات المستخدم
     */
    public boolean updateUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        // الحصول على التاريخ الحالي للتحديث
        String currentDateTime = getCurrentDateTime();

        values.put(DBContract.Users.COL_NAME_EN, user.getNameEn());
        values.put(DBContract.Users.COL_NAME_AR, user.getNameAr());
        values.put(DBContract.Users.COL_EMAIL, user.getEmail());
        values.put(DBContract.Users.COL_ROLE, user.getRole());
        values.put(DBContract.Users.COL_ACCOUNT_STATUS, user.getAccountStatus());
        values.put(DBContract.Users.COL_PHONE, user.getPhone());
        values.put(DBContract.Users.COL_FACEBOOK_URL, user.getFacebookUrl());
        values.put(DBContract.Users.COL_WHATSAPP_NUMBER, user.getWhatsappNumber());
        values.put(DBContract.Users.COL_TELEGRAM_HANDLE, user.getTelegramHandle());
        values.put(DBContract.Users.COL_PROFILE_PICTURE, user.getProfilePicture());
        values.put(DBContract.Users.COL_BIO_EN, user.getBioEn());
        values.put(DBContract.Users.COL_BIO_AR, user.getBioAr());
        values.put(DBContract.Users.COL_PROGRAM_ID, user.getProgramId());
        values.put(DBContract.Users.COL_UPDATED_AT, currentDateTime);

        int result = db.update(DBContract.Users.TABLE_NAME, values,
                DBContract.Users.COL_USER_ID + " = ?",
                new String[]{String.valueOf(user.getUserId())});

        db.close();
        return result > 0;
    }

    /**
     * جلب مستخدم بواسطة ID
     */
    public User getUserById(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User user = null;

        Cursor cursor = db.query(DBContract.Users.TABLE_NAME,
                null,
                DBContract.Users.COL_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.Users.COL_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_NAME_EN)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_NAME_AR)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_ROLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_ACCOUNT_STATUS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_PHONE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_FACEBOOK_URL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_WHATSAPP_NUMBER)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_TELEGRAM_HANDLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_PROFILE_PICTURE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_BIO_EN)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_BIO_AR)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.Users.COL_PROGRAM_ID))
            );

            // إضافة التواريخ إذا كانت موجودة
            try {
                user.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_CREATED_AT)));
                user.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_UPDATED_AT)));
            } catch (Exception e) {
                // تجاهل الخطأ إذا الحقول غير موجودة
            }
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();

        return user;
    }

    /**
     * تغيير كلمة مرور المستخدم
     */
    public boolean updateUserPassword(long userId, String newPasswordHash) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DBContract.Users.COL_PASSWORD_HASH, newPasswordHash);
        values.put(DBContract.Users.COL_UPDATED_AT, getCurrentDateTime());

        int result = db.update(DBContract.Users.TABLE_NAME, values,
                DBContract.Users.COL_USER_ID + " = ?",
                new String[]{String.valueOf(userId)});

        db.close();
        return result > 0;
    }

    /**
     * حذف مستخدم بواسطة ID
     */
    public boolean deleteUser(long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            // حذف المستخدم وجميع البيانات المرتبطة به (بسبب CASCADE في قاعدة البيانات)
            int result = db.delete(DBContract.Users.TABLE_NAME,
                    DBContract.Users.COL_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)});

            return result > 0;
        } catch (Exception e) {
            Log.e("DeleteUser", "Error deleting user: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }

    /**
     * التحقق مما إذا كان يمكن حذف المستخدم (لا توجد بيانات مرتبطة مهمة)
     */
    public boolean canDeleteUser(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            // التحقق من وجود مقررات مرتبطة بالمستخدم كمدرس
            String coursesQuery = "SELECT COUNT(*) FROM " + DBContract.Course.TABLE_NAME +
                    " WHERE " + DBContract.Course.COL_CREATED_BY + " = ?";
            Cursor cursor = db.rawQuery(coursesQuery, new String[]{String.valueOf(userId)});

            boolean canDelete = true;
            if (cursor.moveToFirst()) {
                int courseCount = cursor.getInt(0);
                if (courseCount > 0) {
                    canDelete = false; // لا يمكن حذف المستخدم لأنه مدرس لمقررات
                }
            }
            cursor.close();

            return canDelete;

        } catch (Exception e) {
            Log.e("CanDeleteUser", "Error checking if user can be deleted: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }
}