package org.svuonline.lms.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * كلاس لإدارة قاعدة البيانات.
 * يقوم بإنشاء الجداول وفق الهيكل المُعرّف في DBContract وتحديثها عند تغيّر إصدار قاعدة البيانات.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "lms_system.db";  // اسم قاعدة البيانات
    private static final int DATABASE_VERSION = 2;                     // رقم إصدار قاعدة البيانات

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // تفعيل دعم المفاتيح الأجنبية
        db.execSQL("PRAGMA foreign_keys=ON;");

        // إنشاء جدول المستخدمين
        String SQL_CREATE_USERS = "CREATE TABLE " + DBContract.Users.TABLE_NAME + " ("
                + DBContract.Users.COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DBContract.Users.COL_NAME_EN + " TEXT NOT NULL, "
                + DBContract.Users.COL_NAME_AR + " TEXT NOT NULL, "
                + DBContract.Users.COL_EMAIL + " TEXT NOT NULL UNIQUE, "
                + DBContract.Users.COL_PASSWORD_HASH + " TEXT NOT NULL, "
                + DBContract.Users.COL_ROLE + " TEXT NOT NULL, "
                + DBContract.Users.COL_PROGRAM_ID + " INTEGER, "
                + DBContract.Users.COL_NOTIFICATIONS_ENABLED + " INTEGER DEFAULT 1, "
                + DBContract.Users.COL_ACCOUNT_STATUS + " TEXT DEFAULT 'active', "
                + DBContract.Users.COL_PHONE + " TEXT, "
                + DBContract.Users.COL_FACEBOOK_URL + " TEXT, "
                + DBContract.Users.COL_WHATSAPP_NUMBER + " TEXT, "
                + DBContract.Users.COL_TELEGRAM_HANDLE + " TEXT, "
                + DBContract.Users.COL_PROFILE_PICTURE + " TEXT, " // سيخزن اسم الملف فقط مثل "abdo_photo.jpg"
                + DBContract.Users.COL_BIO_EN + " TEXT, "
                + DBContract.Users.COL_BIO_AR + " TEXT, "
                + DBContract.Users.COL_CREATED_AT + " TEXT, "
                + DBContract.Users.COL_UPDATED_AT + " TEXT, "
                + "FOREIGN KEY (" + DBContract.Users.COL_PROGRAM_ID + ") REFERENCES " + DBContract.AcademicProgram.TABLE_NAME + "(" + DBContract.AcademicProgram.COL_PROGRAM_ID + ") ON DELETE SET NULL"
                + ");";
        db.execSQL(SQL_CREATE_USERS);

        // إنشاء جدول السنة الدراسية
        String SQL_CREATE_ACADEMIC_YEAR = "CREATE TABLE " + DBContract.AcademicYear.TABLE_NAME + " ("
                + DBContract.AcademicYear.COL_YEAR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DBContract.AcademicYear.COL_NAME + " TEXT NOT NULL, "
                + DBContract.AcademicYear.COL_START_DATE + " TEXT NOT NULL, "
                + DBContract.AcademicYear.COL_END_DATE + " TEXT NOT NULL"
                + ");";
        db.execSQL(SQL_CREATE_ACADEMIC_YEAR);

        // إنشاء جدول الفصول الدراسية (Term)
        String SQL_CREATE_TERM = "CREATE TABLE " + DBContract.Term.TABLE_NAME + " ("
                + DBContract.Term.COL_TERM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DBContract.Term.COL_ACADEMIC_YEAR_ID + " INTEGER NOT NULL, "
                + DBContract.Term.COL_NAME + " TEXT NOT NULL, "
                + DBContract.Term.COL_START_DATE + " TEXT NOT NULL, "
                + DBContract.Term.COL_END_DATE + " TEXT NOT NULL, "
                + "FOREIGN KEY (" + DBContract.Term.COL_ACADEMIC_YEAR_ID + ") REFERENCES " + DBContract.AcademicYear.TABLE_NAME + "(" + DBContract.AcademicYear.COL_YEAR_ID + ") ON DELETE CASCADE"
                + ");";
        db.execSQL(SQL_CREATE_TERM);

        // إنشاء جدول البرامج الأكاديمية
        String SQL_CREATE_ACADEMIC_PROGRAM = "CREATE TABLE " + DBContract.AcademicProgram.TABLE_NAME + " ("
                + DBContract.AcademicProgram.COL_PROGRAM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DBContract.AcademicProgram.COL_CODE + " TEXT NOT NULL, "
                + DBContract.AcademicProgram.COL_NAME_EN + " TEXT NOT NULL, "
                + DBContract.AcademicProgram.COL_NAME_AR + " TEXT NOT NULL, "
                + DBContract.AcademicProgram.COL_PROGRAM_DURATION + " INTEGER NOT NULL"
                + ");";
        db.execSQL(SQL_CREATE_ACADEMIC_PROGRAM);

        // إنشاء جدول المقررات الدراسية
        String SQL_CREATE_COURSE = "CREATE TABLE " + DBContract.Course.TABLE_NAME + " ("
                + DBContract.Course.COL_COURSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DBContract.Course.COL_PROGRAM_ID + " INTEGER NOT NULL, "
                + DBContract.Course.COL_TERM_ID + " INTEGER NOT NULL, "
                + DBContract.Course.COL_CODE + " TEXT NOT NULL, "
                + DBContract.Course.COL_NAME_EN + " TEXT NOT NULL, "
                + DBContract.Course.COL_NAME_AR + " TEXT NOT NULL, "
                + DBContract.Course.COL_CREATED_BY + " INTEGER NOT NULL, "
                + DBContract.Course.COL_CREATED_AT + " TEXT, "
                + DBContract.Course.COL_CREDIT_HOURS + " INTEGER, "
                + DBContract.Course.COL_COLOR + " TEXT, "
                + DBContract.Course.COL_STATUS + " TEXT, "
                + "FOREIGN KEY (" + DBContract.Course.COL_PROGRAM_ID + ") REFERENCES " + DBContract.AcademicProgram.TABLE_NAME + "(" + DBContract.AcademicProgram.COL_PROGRAM_ID + ") ON DELETE CASCADE, "
                + "FOREIGN KEY (" + DBContract.Course.COL_TERM_ID + ") REFERENCES " + DBContract.Term.TABLE_NAME + "(" + DBContract.Term.COL_TERM_ID + ") ON DELETE CASCADE, "
                + "FOREIGN KEY (" + DBContract.Course.COL_CREATED_BY + ") REFERENCES " + DBContract.Users.TABLE_NAME + "(" + DBContract.Users.COL_USER_ID + ") ON DELETE RESTRICT"
                + ");";
        db.execSQL(SQL_CREATE_COURSE);

        // إنشاء جدول أقسام المقررات الدراسية
        String SQL_CREATE_COURSE_SECTION = "CREATE TABLE " + DBContract.CourseSection.TABLE_NAME + " ("
                + DBContract.CourseSection.COL_SECTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DBContract.CourseSection.COL_COURSE_ID + " INTEGER NOT NULL, "
                + DBContract.CourseSection.COL_TITLE_EN + " TEXT NOT NULL, "
                + DBContract.CourseSection.COL_TITLE_AR + " TEXT NOT NULL, "
                + DBContract.CourseSection.COL_DISPLAY_ORDER + " INTEGER NOT NULL, "
                + "FOREIGN KEY (" + DBContract.CourseSection.COL_COURSE_ID + ") REFERENCES " + DBContract.Course.TABLE_NAME + "(" + DBContract.Course.COL_COURSE_ID + ") ON DELETE CASCADE"
                + ");";
        db.execSQL(SQL_CREATE_COURSE_SECTION);

        // إنشاء جدول أدوات الأقسام
        String SQL_CREATE_SECTION_TOOL = "CREATE TABLE " + DBContract.SectionTool.TABLE_NAME + " ("
                + DBContract.SectionTool.COL_TOOL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DBContract.SectionTool.COL_SECTION_ID + " INTEGER NOT NULL, "
                + DBContract.SectionTool.COL_NAME_EN + " TEXT NOT NULL, "
                + DBContract.SectionTool.COL_NAME_AR + " TEXT NOT NULL, "
                + DBContract.SectionTool.COL_ACTION_TYPE + " TEXT NOT NULL, "
                + "FOREIGN KEY (" + DBContract.SectionTool.COL_SECTION_ID + ") REFERENCES " + DBContract.CourseSection.TABLE_NAME + "(" + DBContract.CourseSection.COL_SECTION_ID + ") ON DELETE CASCADE"
                + ");";
        db.execSQL(SQL_CREATE_SECTION_TOOL);

        // إنشاء جدول الموارد (الملفات المرفوعة)
        String SQL_CREATE_RESOURCE = "CREATE TABLE " + DBContract.Resource.TABLE_NAME + " ("
                + DBContract.Resource.COL_RESOURCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DBContract.Resource.COL_TOOL_ID + " INTEGER NOT NULL, "
                + DBContract.Resource.COL_FILE_NAME + " TEXT NOT NULL, "
                + DBContract.Resource.COL_FILE_PATH + " TEXT, "
                + DBContract.Resource.COL_UPLOADED_BY + " INTEGER NOT NULL, "
                + DBContract.Resource.COL_UPLOADED_AT + " TEXT, "
                + "FOREIGN KEY (" + DBContract.Resource.COL_TOOL_ID + ") REFERENCES " + DBContract.SectionTool.TABLE_NAME + "(" + DBContract.SectionTool.COL_TOOL_ID + ") ON DELETE CASCADE, "
                + "FOREIGN KEY (" + DBContract.Resource.COL_UPLOADED_BY + ") REFERENCES " + DBContract.Users.TABLE_NAME + "(" + DBContract.Users.COL_USER_ID + ") ON DELETE RESTRICT"
                + ");";
        db.execSQL(SQL_CREATE_RESOURCE);

        // إنشاء جدول تسجيل المقررات (Enrollment)
        String SQL_CREATE_ENROLLMENT = "CREATE TABLE " + DBContract.Enrollment.TABLE_NAME + " ("
                + DBContract.Enrollment.COL_ENROLLMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DBContract.Enrollment.COL_USER_ID + " INTEGER NOT NULL, "
                + DBContract.Enrollment.COL_COURSE_ID + " INTEGER NOT NULL, "
                + DBContract.Enrollment.COL_COURSE_STATUS + " TEXT, "
                + DBContract.Enrollment.COL_IS_FAVORITE + " INTEGER DEFAULT 0, "
                + DBContract.Enrollment.COL_ENROLLED_AT + " TEXT, "
                + "FOREIGN KEY (" + DBContract.Enrollment.COL_USER_ID + ") REFERENCES " + DBContract.Users.TABLE_NAME + "(" + DBContract.Users.COL_USER_ID + ") ON DELETE CASCADE, "
                + "FOREIGN KEY (" + DBContract.Enrollment.COL_COURSE_ID + ") REFERENCES " + DBContract.Course.TABLE_NAME + "(" + DBContract.Course.COL_COURSE_ID + ") ON DELETE CASCADE"
                + ");";
        db.execSQL(SQL_CREATE_ENROLLMENT);

        // إنشاء جدول الواجبات
        String SQL_CREATE_ASSIGNMENT = "CREATE TABLE " + DBContract.Assignment.TABLE_NAME + " ("
                + DBContract.Assignment.COL_ASSIGNMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DBContract.Assignment.COL_TOOL_ID + " INTEGER NOT NULL, "
                + DBContract.Assignment.COL_TITLE_EN + " TEXT NOT NULL, "
                + DBContract.Assignment.COL_TITLE_AR + " TEXT NOT NULL, "
                + DBContract.Assignment.COL_OPEN_DATE + " TEXT NOT NULL, "
                + DBContract.Assignment.COL_DUE_DATE + " TEXT NOT NULL, "
                + DBContract.Assignment.COL_ASSIGNMENT_FILE + " TEXT, "
                + DBContract.Assignment.COL_CREATED_BY + " INTEGER NOT NULL, "
                + "FOREIGN KEY (" + DBContract.Assignment.COL_TOOL_ID + ") REFERENCES " + DBContract.SectionTool.TABLE_NAME + "(" + DBContract.SectionTool.COL_TOOL_ID + ") ON DELETE CASCADE, "
                + "FOREIGN KEY (" + DBContract.Assignment.COL_CREATED_BY + ") REFERENCES " + DBContract.Users.TABLE_NAME + "(" + DBContract.Users.COL_USER_ID + ") ON DELETE RESTRICT"
                + ");";
        db.execSQL(SQL_CREATE_ASSIGNMENT);

        // إنشاء جدول تسليم الواجبات
        String SQL_CREATE_ASSIGNMENT_SUBMISSION = "CREATE TABLE " + DBContract.AssignmentSubmission.TABLE_NAME + " ("
                + DBContract.AssignmentSubmission.COL_SUBMISSION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DBContract.AssignmentSubmission.COL_ASSIGNMENT_ID + " INTEGER NOT NULL, "
                + DBContract.AssignmentSubmission.COL_USER_ID + " INTEGER NOT NULL, "
                + DBContract.AssignmentSubmission.COL_SUBMITTED_AT + " TEXT, "
                + DBContract.AssignmentSubmission.COL_FILE_PATH + " TEXT, "
                + DBContract.AssignmentSubmission.COL_STATUS + " TEXT, "
                + DBContract.AssignmentSubmission.COL_GRADE + " REAL, "
                + DBContract.AssignmentSubmission.COL_GRADED_BY + " INTEGER, "
                + DBContract.AssignmentSubmission.COL_GRADED_AT + " TEXT, "
                + "FOREIGN KEY (" + DBContract.AssignmentSubmission.COL_ASSIGNMENT_ID + ") REFERENCES " + DBContract.Assignment.TABLE_NAME + "(" + DBContract.Assignment.COL_ASSIGNMENT_ID + ") ON DELETE CASCADE, "
                + "FOREIGN KEY (" + DBContract.AssignmentSubmission.COL_USER_ID + ") REFERENCES " + DBContract.Users.TABLE_NAME + "(" + DBContract.Users.COL_USER_ID + ") ON DELETE CASCADE, "
                + "FOREIGN KEY (" + DBContract.AssignmentSubmission.COL_GRADED_BY + ") REFERENCES " + DBContract.Users.TABLE_NAME + "(" + DBContract.Users.COL_USER_ID + ") ON DELETE SET NULL"
                + ");";
        db.execSQL(SQL_CREATE_ASSIGNMENT_SUBMISSION);

        // إنشاء جدول الإشعارات
        String SQL_CREATE_NOTIFICATION = "CREATE TABLE " + DBContract.Notification.TABLE_NAME + " ("
                + DBContract.Notification.COL_NOTIFICATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DBContract.Notification.COL_USER_ID + " INTEGER NOT NULL, "
                + DBContract.Notification.COL_CONTENT_EN + " TEXT NOT NULL, "
                + DBContract.Notification.COL_CONTENT_AR + " TEXT NOT NULL, "
                + DBContract.Notification.COL_IS_READ + " INTEGER DEFAULT 0, "
                + DBContract.Notification.COL_RELATED_TYPE + " TEXT, "
                + DBContract.Notification.COL_RELATED_ID + " INTEGER, "
                + DBContract.Notification.COL_CREATED_AT + " TEXT, "
                + "FOREIGN KEY (" + DBContract.Notification.COL_USER_ID + ") REFERENCES " + DBContract.Users.TABLE_NAME + "(" + DBContract.Users.COL_USER_ID + ") ON DELETE CASCADE"
                + ");";
        db.execSQL(SQL_CREATE_NOTIFICATION);

        // إنشاء جدول الأحداث
        String SQL_CREATE_EVENT = "CREATE TABLE " + DBContract.Event.TABLE_NAME + " ("
                + DBContract.Event.COL_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DBContract.Event.COL_USER_ID + " INTEGER NOT NULL, "
                + DBContract.Event.COL_TITLE_EN + " TEXT NOT NULL, "
                + DBContract.Event.COL_TITLE_AR + " TEXT NOT NULL, "
                + DBContract.Event.COL_EVENT_DATE + " TEXT NOT NULL, "
                + DBContract.Event.COL_TYPE + " TEXT, "
                + DBContract.Event.COL_RELATED_ID + " INTEGER, "
                + "FOREIGN KEY (" + DBContract.Event.COL_USER_ID + ") REFERENCES " + DBContract.Users.TABLE_NAME + "(" + DBContract.Users.COL_USER_ID + ") ON DELETE CASCADE"
                + ");";
        db.execSQL(SQL_CREATE_EVENT);

        // إضافة فهارس لتحسين الأداء
        db.execSQL("CREATE INDEX idx_users_email ON " + DBContract.Users.TABLE_NAME + "(" + DBContract.Users.COL_EMAIL + ")");
        db.execSQL("CREATE INDEX idx_users_role ON " + DBContract.Users.TABLE_NAME + "(" + DBContract.Users.COL_ROLE + ")");
        db.execSQL("CREATE INDEX idx_course_program ON " + DBContract.Course.TABLE_NAME + "(" + DBContract.Course.COL_PROGRAM_ID + ")");
        db.execSQL("CREATE INDEX idx_enrollment_user_course ON " + DBContract.Enrollment.TABLE_NAME + "(" + DBContract.Enrollment.COL_USER_ID + ", " + DBContract.Enrollment.COL_COURSE_ID + ")");
        db.execSQL("CREATE INDEX idx_notification_user_read ON " + DBContract.Notification.TABLE_NAME + "(" + DBContract.Notification.COL_USER_ID + ", " + DBContract.Notification.COL_IS_READ + ")");
        db.execSQL("CREATE INDEX idx_assignment_dates ON " + DBContract.Assignment.TABLE_NAME + "(" + DBContract.Assignment.COL_DUE_DATE + ", " + DBContract.Assignment.COL_OPEN_DATE + ")");

        insertInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // حذف الجداول القديمة عند الترقية ثم إعادة إنشائها
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.Users.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.AcademicYear.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.Term.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.AcademicProgram.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.Course.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.CourseSection.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.SectionTool.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.Resource.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.Enrollment.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.Assignment.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.AssignmentSubmission.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.Notification.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.Event.TABLE_NAME);

        onCreate(db);
    }


    /**
     * دالة لإدخال البيانات الثابتة في جدول المستخدمين.
     * تم إدراج كلمات المرور كهاش (SHA-256) مباشرة.
     */
    private void insertInitialData(SQLiteDatabase db) {
        // بيانات المستخدم 1: Abdo AL-Khoury
        // abdo_216070/abdo123
        insertUser(db,
                "Abdo AL-Khoury",
                "عبده الخوري",
                "abdo_216070@svuonline.org",
                "d587b4ea91b5589fef1c147e7b886eb418ed6046d1646b8c13350c42a02fbee2",
                "Student",
                "1",
                1,
                "active",
                "+963936535969",
                "facebook.com/AbdoAL-Khoury1",
                "wa.me/+963963936535969",
                "t.me/AbdoAL-Khoury",
                "abdo_photo.jpg",
                "Information Technology Expert Passionate About Software Development | Creative Graphic Designer & Front-End Developer & Front-End Developer & Information Systems Analyst & Database Analyst",
                " خبير في تكنولوجيا المعلومات، شغوف بتطوير البرمجيات | مصمم جرافيك مبدع ومطور واجهات أمامية ومحلل أنظمة المعلومات ومحلل قواعد بيانات ",
                "2021-10-01",
                "2025-09-04"
        );

        // بيانات المستخدم 2: Jeeny Abdoush
        insertUser(db,
                "Jeeny Abdoush",
                "جيني عبدوش",
                "jeeny_192950@svuonline.org",
                "e264ee77720d70a358a05bf267081c283095dc7a8b5d0a4f48d5bede3f603323",
                "Student",
                "1",
                1,
                "active",
                "+963936547802",
                "facebook.com/jeeny.abdoush",
                "wa.me/+963936547802",
                "t.me/jeeny.abdoush",
                "jeeny_photo.jpg",
                "Mobile Application Developer | Android Specialist | UI/UX Designer & Systems Integrator",
                "مطور تطبيقات موبايل | مختص أندرويد | مصمم واجهات وتجارب مستخدم وتكامل أنظمة",
                "2020-02-01",
                "2025-09-01"
        );


        // بيانات المستخدم 3: Joseph Daoud
        insertUser(db,
                "Joseph Daoud",
                "جوزيف داود",
                "joseph_120633@svuonline.org",
                "7ec8c6dc2a42e8b23ced92fcfce9e1f1c699bd000f8fdaddc99ffd864e117406",
                "Student",
                "1",
                1,
                "active",
                "+963967662922",
                "facebook.com/joe.david",
                "wa.me/+963967662922",
                "t.me/josephdavid0",
                "joseph_photo.jpg",
                "Artificial Intelligence Engineer | Machine Learning Specialist | Front-End Programmer | Creative Graphic & UI Designer",
                "مهندس ذكاء اصطناعي | مختص تعلم آلي | مبرمج واجهات أمامية | مصمم جرافيك وواجهات مستخدم مبدع",
                "2020-01-01",
                "2025-09-02"
        );

        // بيانات المستخدم 4: Rami Ibrahim (المنسق)
        insertUser(db,
                "Rami Ibrahim",
                "رامي إبراهيم",
                "rami_031998@svuonline.org",
                "8f6f6503aa8be893a603a19301772e8de00e920e6dc4d85bc19d4aabf4f82460",
                "Coordinator",
                "1",
                1,
                "active",
                "+963966699666",
                "facebook.com/Rami-Ibrahim",
                "wa.me/+963966699666",
                "t.me/Rami-Ibrahim",
                "rami_photo.jpg",
                "Full-Stack Developer | Cloud Computing Enthusiast | Database Administrator & Cybersecurity Analyst",
                "مطور متكامل | مهتم بالحوسبة السحابية | مسؤول قواعد بيانات ومحلل أمن المعلومات",
                "2019-11-11",
                "2025-09-03"
        );

        // بيانات المستخدم 5: Jessica Murad (دكتور)
        insertUser(db,
                "Jessica Murad",
                "جيسيكا مراد",
                "jessica_022001@svuonline.org",
                "929252f4083cac47f039f617f4113481c2f344eac11793ec94cf878c87233d38",
                "Tutor",
                "1",
                1,
                "active",
                "+963988111222",
                "facebook.com/Jessica.Murad",
                "wa.me/+963988111222",
                "t.me/Jessica.Murad",
                "jessica_photo.jpg",
                "Game Developer | Virtual Reality & Augmented Reality Specialist | Multimedia Systems Engineer",
                "مطور ألعاب | مختص بالواقع الافتراضي والمعزز | مهندس نظم وسائط متعددة",
                "2021-03-01",
                "2025-09-05"
        );

        // بيانات المستخدم 6: Ahmad Al-Khatib
        insertUser(db,
                "Ahmad Al-Khatib",
                "أحمد الخطيب",
                "ahmad_112999@svuonline.org",
                "306098fa01257f8e4809cbdfca258d8c22c7fb12937cc2616ef06aa20fd8008e",
                "Student",
                "1",
                1,
                "inactive",
                "+963999555666",
                "facebook.com/Ahmad-Khatib2",
                "wa.me/+963999555666",
                "t.me/Ahmad-Khatib2",
                "ahmad_photo.jpg",
                "Cybersecurity Specialist | Ethical Hacker | Network Security Analyst & Risk Management Consultant",
                "خبير أمن سيبراني | هاكر أخلاقي | محلل أمن الشبكات ومستشار إدارة المخاطر",
                "2022-10-01",
                "2025-09-06"
        );

        // بيانات المستخدم 7: LMS (المشرف)
        // osamasara_director/osamasara123
        insertUser(db,
                "Mr. Osama Sarah",
                "الأستاذ أسامة سارة",
                "osamasara_director@svuonline.org",
                "4a52de52748bafbf788ef6d32d779a007e47915c378ffc51f3a1fbbc272a4efb",
                "admin",
                "1",
                0,
                "active",
                "+963999888888",
                "facebook.com/Osama-Sarah-3",
                "wa.me/+963999888888",
                "t.me/Osama-Sarah-3",
                "osama_photo.jpg",
                "Data Engineer | Big Data Specialist | Machine Learning Pipeline Developer & Cloud Data Architect | Database Developer & Business Systems Engineer",
                "مهندس بيانات | مختص البيانات الضخمة | مطور خطوط تعلم آلي ومهندس بيانات سحابية | مطور قواعد بيانات ومهندس نظم أعمال",
                "2019-09-01",
                "2025-09-07"
        );

        // إدخال بيانات جدول السنة الدراسية
        insertAcademicYear(db, "2025", "2024-08-30", "2026-08-30");

        // إدخال بيانات جدول الفصول الدراسية (Term)
        insertTerm(db, "1", "F24", "2024-08-30", "2026-02-30");

        // إدخال بيانات جدول البرامج الأكاديمية (AcademicProgram)
        insertAcademicProgram(db, "ITE", "Bachelor in Information Technology Engineering - ITE", "الإجازة في الهندسة المعلوماتية - ITE", 4);
        insertAcademicProgram(db, "BAIT", "Bachelor in Information Technology - BAIT", "الإجازة في تقانة المعلومات - BAIT", 4);


        // إدخال بيانات جدول المقررات (Course)
        insertCourse(db, "1", "1", "GCS301", "Computer Skills-ICDL", "مهارات الحاسوب", "7", "2010-01-01", 4, "Custom_MainColorPurple", "active");
        insertCourse(db, "1", "1", "BLA401", "Linear Algebra", "الجبر الخطي", "7", "2005-04-01", 5, "Custom_MainColorDarkPink", "active");
        insertCourse(db, "1", "1", "BSP501", "Signal Processing", "معالجة إشارة", "7", "2007-03-01", 5, "Custom_MainColorBlue", "active");
        insertCourse(db, "1", "1", "BTS501", "Telecommunication Systems", " نظم الاتصالات", "7", "2009-07-01", 5, "Custom_MainColorGreen", "active");
        insertCourse(db, "1", "1", "BPG601", "Programming III", "برمجة (3)", "7", "2008-02-01", 5, "Custom_MainColorTeal", "active");
        insertCourse(db, "1", "1", "BAI501", "Artificial Intelligence", "الذكاء الصنعي", "7", "2006-05-01", 6, "Custom_MainColorOrange", "active");
        insertCourse(db, "1", "1", "BMP601", "Mobile Applications Programming", "برمجة تطبيقات النقال", "7", "2011-06-01", 6, "Custom_MainColorGolden", "active");
        insertCourse(db, "1", "1", "GET601", "Ethics of Profession & Society", "أخلاقيات المهنة والمجتمع", "7", "2015-08-01", 6, "Custom_MainColorPurple", "active");
        insertCourse(db, "1", "1", "BIS601", "Information System Security", "أمن نظم المعلومات", "7", "2017-10-01", 6, "Custom_MainColorDarkPink", "active");
        insertCourse(db, "1", "1", "SAD601", "Algorithm Analysis & Design", "تحليل وتصميم الخوارزميات", "7", "2012-09-01", 6, "Custom_MainColorBlue", "active");
        insertCourse(db, "1", "1", "SDE601", "Data Mining", "التنقيب في البيانات", "7", "2013-12-01", 6, "Custom_MainColorGreen", "active");
        insertCourse(db, "1", "1", "SIR601", "Information Retrieval", "استرجاع المعلومات", "7", "2014-11-01", 6, "Custom_MainColorTeal", "active");
        insertCourse(db, "1", "1", "SSW601", "Semantic Web", "الويب الدلالي", "7", "2016-01-01", 6, "Custom_MainColorOrange", "active");
        insertCourse(db, "1", "1", "SSQ601", "Software Quality (in English)", "جودة البرمجيات (باللغة الإنكليزية)", "7", "2018-02-01", 5, "Custom_MainColorGolden", "active");


        // إدخال بيانات جدول أقسام المقررات (CourseSection)
        insertCourseSection(db, "1", "Course Materials", "موارد المقرر", 1);
        insertCourseSection(db, "1", "F24 Semester", "الفصل F24", 2);
        insertCourseSection(db, "2", "Course Materials", "موارد المقرر", 1);
        insertCourseSection(db, "2", "F21 Semester", "الفصل F21", 2);
        insertCourseSection(db, "3", "Course Materials", "موارد المقرر", 1);
        insertCourseSection(db, "3", "F24 Semester", "الفصل F24", 2);
        insertCourseSection(db, "3", "F21 Semester", "الفصل F21", 3);
        insertCourseSection(db, "3", "Tools", "الأدوات", 4);
        insertCourseSection(db, "4", "Course Materials", "موارد المقرر", 1);
        insertCourseSection(db, "4", "F24 Semester", "الفصل F24", 2);
        insertCourseSection(db, "4", "F21 Semester", "الفصل F21", 3);
        insertCourseSection(db, "4", "Tools", "الأدوات", 4);
        insertCourseSection(db, "5", "Course Materials", "موارد المقرر", 1);
        insertCourseSection(db, "5", "F24 Semester", "الفصل F24", 2);
        insertCourseSection(db, "6", "Course Materials", "موارد المقرر", 1);
        insertCourseSection(db, "6", "F24 Semester", "الفصل F24", 2);
        insertCourseSection(db, "6", "Tools", "الأدوات", 3);
        insertCourseSection(db, "7", "Course Materials", "موارد المقرر", 1);
        insertCourseSection(db, "7", "F24 Semester", "الفصل F24", 2);
        insertCourseSection(db, "7", "F21 Semester", "الفصل F21", 3);
        insertCourseSection(db, "7", "Tools", "الأدوات", 4);
        insertCourseSection(db, "8", "Course Materials", "موارد المقرر", 1);
        insertCourseSection(db, "8", "F24 Semester", "الفصل F24", 2);
        insertCourseSection(db, "8", "F21 Semester", "الفصل F21", 3);
        insertCourseSection(db, "9", "Course Materials", "موارد المقرر", 1);
        insertCourseSection(db, "9", "F24 Semester", "الفصل F24", 2);
        insertCourseSection(db, "9", "Tools", "الأدوات", 3);
        insertCourseSection(db, "10", "Course Materials", "موارد المقرر", 1);
        insertCourseSection(db, "10", "F24 Semester", "الفصل F24", 2);
        insertCourseSection(db, "10", "F21 Semester", "الفصل F21", 3);
        insertCourseSection(db, "10", "Tools", "الأدوات", 4);
        insertCourseSection(db, "11", "Course Materials", "موارد المقرر", 1);
        insertCourseSection(db, "11", "F24 Semester", "الفصل F24", 2);
        insertCourseSection(db, "11", "F21 Semester", "الفصل F21", 3);
        insertCourseSection(db, "12", "Course Materials", "موارد المقرر", 1);
        insertCourseSection(db, "12", "F24 Semester", "الفصل F24", 2);
        insertCourseSection(db, "13", "Course Materials", "موارد المقرر", 1);
        insertCourseSection(db, "13", "F24 Semester", "الفصل F24", 2);
        insertCourseSection(db, "14", "Course Materials", "موارد المقرر", 1);
        insertCourseSection(db, "14", "F24 Semester", "الفصل F24", 2);


        // إدخال بيانات جدول أدوات الأقسام (SectionTool)
        insertSectionTool(db, "1", "Course Identification", "تعريف المقرر", "file action");
        insertSectionTool(db, "1", "Book / PDF", "كتاب / PDF", "file action");
        insertSectionTool(db, "1", "Participants", "المشاركون", "Participants action");
        insertSectionTool(db, "1", "Recorded Sessions", "الجلسات المسجلة", "file action");
        insertSectionTool(db, "1", "References", "المراجع", "file action");
        insertSectionTool(db, "1", "Training Exam", "اختبار تدريبي", "file action");
        insertSectionTool(db, "2", "Semester Plan", "الخطة الدراسية للفصل", "file action");
        insertSectionTool(db, "2", "Assignment", "الوظيفة", "Assignment action");
        insertSectionTool(db, "2", "Slides / PowerPoint", "الشرائح / باوربوينت", "file action");
        insertSectionTool(db, "2", "Tools", "أدوات", "file action");
        insertSectionTool(db, "3", "Course Identification", "تعريف المقرر", "file action");
        insertSectionTool(db, "3", "Book / PDF", "كتاب / PDF", "file action");
        insertSectionTool(db, "3", "Participants", "المشاركون", "Participants action");
        insertSectionTool(db, "3", "Recorded Sessions", "الجلسات المسجلة", "file action");
        insertSectionTool(db, "3", "References", "المراجع", "file action");
        insertSectionTool(db, "3", "Training Exam", "اختبار تدريبي", "file action");
        insertSectionTool(db, "4", "Slides / PowerPoint", "الشرائح / باوربوينت", "file action");
        insertSectionTool(db, "5", "Course Identification", "تعريف المقرر", "file action");
        insertSectionTool(db, "5", "Book / PDF", "كتاب / PDF", "file action");
        insertSectionTool(db, "5", "Participants", "المشاركون", "Participants action");
        insertSectionTool(db, "5", "Recorded Sessions", "الجلسات المسجلة", "file action");
        insertSectionTool(db, "5", "References", "المراجع", "file action");
        insertSectionTool(db, "5", "Training Exam", "اختبار تدريبي", "file action");
        insertSectionTool(db, "6", "Semester Plan", "الخطة الدراسية للفصل", "file action");
        insertSectionTool(db, "6", "Assignment", "الواجب", "Assignment action");
        insertSectionTool(db, "6", "Slides / PowerPoint", "الشرائح / باوربوينت", "file action");
        insertSectionTool(db, "6", "Tools", "أدوات", "file action");
        insertSectionTool(db, "7", "Slides / PowerPoint", "الشرائح / باوربوينت", "file action");
        insertSectionTool(db, "8", "MATLAB", "ماتلاب", "file action");
        insertSectionTool(db, "8", "IDM", "آي دي إم", "file action");
        insertSectionTool(db, "8", "PacketTracer", "باكيت تريسر", "file action");
        insertSectionTool(db, "8", "VisualStudio", "فيجوال ستوديو", "file action");
        insertSectionTool(db, "8", "EMU", "إي إم يو", "file action");
        insertSectionTool(db, "8", "VLC", "في إل سي", "file action");
        insertSectionTool(db, "9", "Course Identification", "تعريف المقرر", "file action");
        insertSectionTool(db, "9", "Book / PDF", "كتاب / PDF", "file action");
        insertSectionTool(db, "9", "Participants", "المشاركون", "Participants action");
        insertSectionTool(db, "9", "Recorded Sessions", "الجلسات المسجلة", "file action");
        insertSectionTool(db, "9", "References", "المراجع", "file action");
        insertSectionTool(db, "9", "Training Exam", "اختبار تدريبي", "file action");
        insertSectionTool(db, "10", "Semester Plan", "الخطة الدراسية للفصل", "file action");
        insertSectionTool(db, "10", "Assignment", "الواجب", "Assignment action");
        insertSectionTool(db, "10", "Slides / PowerPoint", "الشرائح / باوربوينت", "file action");
        insertSectionTool(db, "10", "Tools", "أدوات", "file action");
        insertSectionTool(db, "11", "Slides / PowerPoint", "الشرائح / باوربوينت", "file action");
        insertSectionTool(db, "12", "MATLAB", "ماتلاب", "file action");
        insertSectionTool(db, "12", "IDM", "آي دي إم", "file action");
        insertSectionTool(db, "12", "PacketTracer", "باكيت تريسر", "file action");
        insertSectionTool(db, "12", "VisualStudio", "فيجوال ستوديو", "file action");
        insertSectionTool(db, "12", "EMU", "إي إم يو", "file action");
        insertSectionTool(db, "12", "VLC", "في إل سي", "file action");
        insertSectionTool(db, "13", "Course Identification", "تعريف المقرر", "file action");
        insertSectionTool(db, "13", "Book / PDF", "كتاب / PDF", "file action");
        insertSectionTool(db, "13", "Participants", "المشاركون", "Participants action");
        insertSectionTool(db, "13", "Recorded Sessions", "الجلسات المسجلة", "file action");
        insertSectionTool(db, "13", "References", "المراجع", "file action");
        insertSectionTool(db, "13", "Training Exam", "اختبار تدريبي", "file action");
        insertSectionTool(db, "14", "Semester Plan", "الخطة الدراسية للفصل", "file action");
        insertSectionTool(db, "14", "Assignment", "الواجب", "Assignment action");
        insertSectionTool(db, "14", "Slides / PowerPoint", "الشرائح / باوربوينت", "file action");
        insertSectionTool(db, "14", "Tools", "أدوات", "file action");
        insertSectionTool(db, "15", "Course Identification", "تعريف المقرر", "file action");
        insertSectionTool(db, "15", "Book / PDF", "كتاب / PDF", "file action");
        insertSectionTool(db, "15", "Participants", "المشاركون", "Participants action");
        insertSectionTool(db, "15", "Recorded Sessions", "الجلسات المسجلة", "file action");
        insertSectionTool(db, "15", "References", "المراجع", "file action");
        insertSectionTool(db, "15", "Training Exam", "اختبار تدريبي", "file action");
        insertSectionTool(db, "16", "Semester Plan", "الخطة الدراسية للفصل", "file action");
        insertSectionTool(db, "16", "Assignment", "الواجب", "Assignment action");
        insertSectionTool(db, "16", "Slides / PowerPoint", "الشرائح / باوربوينت", "file action");
        insertSectionTool(db, "16", "Tools", "أدوات", "file action");
        insertSectionTool(db, "17", "MATLAB", "ماتلاب", "file action");
        insertSectionTool(db, "17", "IDM", "آي دي إم", "file action");
        insertSectionTool(db, "17", "PacketTracer", "باكيت تريسر", "file action");
        insertSectionTool(db, "17", "VisualStudio", "فيجوال ستوديو", "file action");
        insertSectionTool(db, "17", "EMU", "إي إم يو", "file action");
        insertSectionTool(db, "17", "VLC", "في إل سي", "file action");
        insertSectionTool(db, "18", "Course Identification", "تعريف المقرر", "file action");
        insertSectionTool(db, "18", "Book / PDF", "كتاب / PDF", "file action");
        insertSectionTool(db, "18", "Participants", "المشاركون", "Participants action");
        insertSectionTool(db, "18", "Recorded Sessions", "الجلسات المسجلة", "file action");
        insertSectionTool(db, "18", "References", "المراجع", "file action");
        insertSectionTool(db, "18", "Training Exam", "اختبار تدريبي", "file action");
        insertSectionTool(db, "19", "Semester Plan", "الخطة الدراسية للفصل", "file action");
        insertSectionTool(db, "19", "Assignment", "الواجب", "Assignment action");
        insertSectionTool(db, "19", "Slides / PowerPoint", "الشرائح / باوربوينت", "file action");
        insertSectionTool(db, "19", "Tools", "أدوات", "file action");
        insertSectionTool(db, "20", "Slides / PowerPoint", "الشرائح / باوربوينت", "file action");
        insertSectionTool(db, "21", "MATLAB", "ماتلاب", "file action");
        insertSectionTool(db, "21", "IDM", "آي دي إم", "file action");
        insertSectionTool(db, "21", "PacketTracer", "باكيت تريسر", "file action");
        insertSectionTool(db, "21", "VisualStudio", "فيجوال ستوديو", "file action");
        insertSectionTool(db, "21", "EMU", "إي إم يو", "file action");
        insertSectionTool(db, "21", "VLC", "في إل سي", "file action");
        insertSectionTool(db, "22", "Course Identification", "تعريف المقرر", "file action");
        insertSectionTool(db, "22", "Book / PDF", "كتاب / PDF", "file action");
        insertSectionTool(db, "22", "Participants", "المشاركون", "Participants action");
        insertSectionTool(db, "22", "Recorded Sessions", "الجلسات المسجلة", "file action");
        insertSectionTool(db, "22", "References", "المراجع", "file action");
        insertSectionTool(db, "22", "Training Exam", "اختبار تدريبي", "file action");
        insertSectionTool(db, "23", "Semester Plan", "الخطة الدراسية للفصل", "file action");
        insertSectionTool(db, "23", "Assignment", "الواجب", "Assignment action");
        insertSectionTool(db, "23", "Slides / PowerPoint", "الشرائح / باوربوينت", "file action");
        insertSectionTool(db, "23", "Tools", "أدوات", "file action");
        insertSectionTool(db, "24", "Slides / PowerPoint", "الشرائح / باوربوينت", "file action");
        insertSectionTool(db, "25", "Course Identification", "تعريف المقرر", "file action");
        insertSectionTool(db, "25", "Book / PDF", "كتاب / PDF", "file action");
        insertSectionTool(db, "25", "Participants", "المشاركون", "Participants action");
        insertSectionTool(db, "25", "Recorded Sessions", "الجلسات المسجلة", "file action");
        insertSectionTool(db, "25", "References", "المراجع", "file action");
        insertSectionTool(db, "25", "Training Exam", "اختبار تدريبي", "file action");
        insertSectionTool(db, "26", "Semester Plan", "الخطة الدراسية للفصل", "file action");
        insertSectionTool(db, "26", "Assignment", "الواجب", "Assignment action");
        insertSectionTool(db, "26", "Slides / PowerPoint", "الشرائح / باوربوينت", "file action");
        insertSectionTool(db, "26", "Tools", "أدوات", "file action");
        insertSectionTool(db, "27", "MATLAB", "ماتلاب", "file action");
        insertSectionTool(db, "27", "IDM", "آي دي إم", "file action");
        insertSectionTool(db, "27", "PacketTracer", "باكيت تريسر", "file action");
        insertSectionTool(db, "27", "VisualStudio", "فيجوال ستوديو", "file action");
        insertSectionTool(db, "27", "EMU", "إي إم يو", "file action");
        insertSectionTool(db, "27", "VLC", "في إل سي", "file action");
        insertSectionTool(db, "28", "Course Identification", "تعريف المقرر", "file action");
        insertSectionTool(db, "28", "Book / PDF", "كتاب / PDF", "file action");
        insertSectionTool(db, "28", "Participants", "المشاركون", "Participants action");
        insertSectionTool(db, "28", "Recorded Sessions", "الجلسات المسجلة", "file action");
        insertSectionTool(db, "28", "References", "المراجع", "file action");
        insertSectionTool(db, "28", "Training Exam", "اختبار تدريبي", "file action");
        insertSectionTool(db, "29", "Semester Plan", "الخطة الدراسية للفصل", "file action");
        insertSectionTool(db, "29", "Assignment", "الواجب", "Assignment action");
        insertSectionTool(db, "29", "Slides / PowerPoint", "الشرائح / باوربوينت", "file action");
        insertSectionTool(db, "29", "Tools", "أدوات", "file action");
        insertSectionTool(db, "30", "Slides / PowerPoint", "الشرائح / باوربوينت", "file action");
        insertSectionTool(db, "31", "MATLAB", "ماتلاب", "file action");
        insertSectionTool(db, "31", "IDM", "آي دي إم", "file action");
        insertSectionTool(db, "31", "PacketTracer", "باكيت تريسر", "file action");
        insertSectionTool(db, "31", "VisualStudio", "فيجوال ستوديو", "file action");
        insertSectionTool(db, "31", "EMU", "إي إم يو", "file action");
        insertSectionTool(db, "31", "VLC", "في إل سي", "file action");
        insertSectionTool(db, "32", "Course Identification", "تعريف المقرر", "file action");
        insertSectionTool(db, "32", "Book / PDF", "كتاب / PDF", "file action");
        insertSectionTool(db, "32", "Participants", "المشاركون", "Participants action");
        insertSectionTool(db, "32", "Recorded Sessions", "الجلسات المسجلة", "file action");
        insertSectionTool(db, "32", "References", "المراجع", "file action");
        insertSectionTool(db, "32", "Training Exam", "اختبار تدريبي", "file action");
        insertSectionTool(db, "33", "Semester Plan", "الخطة الدراسية للفصل", "file action");
        insertSectionTool(db, "33", "Assignment", "الواجب", "Assignment action");
        insertSectionTool(db, "33", "Slides / PowerPoint", "الشرائح / باوربوينت", "file action");
        insertSectionTool(db, "33", "Tools", "أدوات", "file action");
        insertSectionTool(db, "34", "Slides / PowerPoint", "الشرائح / باوربوينت", "file action");
        insertSectionTool(db, "35", "Course Identification", "تعريف المقرر", "file action");
        insertSectionTool(db, "35", "Book / PDF", "كتاب / PDF", "file action");
        insertSectionTool(db, "35", "Participants", "المشاركون", "Participants action");
        insertSectionTool(db, "35", "Recorded Sessions", "الجلسات المسجلة", "file action");
        insertSectionTool(db, "35", "References", "المراجع", "file action");
        insertSectionTool(db, "35", "Training Exam", "اختبار تدريبي", "file action");
        insertSectionTool(db, "36", "Semester Plan", "الخطة الدراسية للفصل", "file action");
        insertSectionTool(db, "36", "Assignment", "الواجب", "Assignment action");
        insertSectionTool(db, "36", "Slides / PowerPoint", "الشرائح / باوربوينت", "file action");
        insertSectionTool(db, "36", "Tools", "أدوات", "file action");

        insertSectionTool(db, "37", "Course Identification", "تعريف المقرر", "file action");
        insertSectionTool(db, "37", "Book / PDF", "كتاب / PDF", "file action");
        insertSectionTool(db, "37", "Participants", "المشاركون", "Participants action");
        insertSectionTool(db, "37", "Recorded Sessions", "الجلسات المسجلة", "file action");
        insertSectionTool(db, "37", "References", "المراجع", "file action");
        insertSectionTool(db, "37", "Training Exam", "اختبار تدريبي", "file action");
        insertSectionTool(db, "38", "Semester Plan", "الخطة الدراسية للفصل", "file action");
        insertSectionTool(db, "38", "Assignment", "الواجب", "Assignment action");
        insertSectionTool(db, "38", "Slides / PowerPoint", "الشرائح / باوربوينت", "file action");
        insertSectionTool(db, "38", "Tools", "أدوات", "file action");

        insertSectionTool(db, "39", "Course Identification", "تعريف المقرر", "file action");
        insertSectionTool(db, "39", "Book / PDF", "كتاب / PDF", "file action");
        insertSectionTool(db, "39", "Participants", "المشاركون", "Participants action");
        insertSectionTool(db, "39", "Recorded Sessions", "الجلسات المسجلة", "file action");
        insertSectionTool(db, "39", "References", "المراجع", "file action");
        insertSectionTool(db, "39", "Training Exam", "اختبار تدريبي", "file action");
        insertSectionTool(db, "40", "Semester Plan", "الخطة الدراسية للفصل", "file action");
        insertSectionTool(db, "40", "Assignment", "الواجب", "Assignment action");
        insertSectionTool(db, "40", "Slides / PowerPoint", "الشرائح / باوربوينت", "file action");
        insertSectionTool(db, "40", "Tools", "أدوات", "file action");

        // إدخال بيانات جدول الموارد (Resource)
        String filePath = "https://drive.google.com/uc?export=download&id=1kvqxsOU92qeTcR-DP_jPalSrCtHvSu2N";
        int dateCounter = 0; // لتتبع التواريخ

// BMN203 (Course ID: 1, Section IDs: 1-2)
        insertResource(db, "1", "Course_Identification.pdf", filePath, "7", getDate(dateCounter++)); // Course Identification
        for (int i = 1; i <= 12; i++) {
            insertResource(db, "2", "Chapter_Number_" + i + ".pdf", filePath, "7", getDate(dateCounter++)); // Book / PDF
        }
        for (int i = 1; i <= 12; i++) {
            insertResource(db, "4", "Session_Number_" + i + ".lrec", filePath, "7", getDate(dateCounter++)); // Recorded Sessions
        }
        insertResource(db, "5", "Reference_1.pdf", filePath, "7", getDate(dateCounter++)); // References
        insertResource(db, "6", "Practice_Exam_1.pdf", filePath, "7", getDate(dateCounter++)); // Training Exam
        insertResource(db, "7", "Schedule_S24.pdf", filePath, "7", getDate(dateCounter++)); // Semester Plan
        insertResource(db, "9", "Presentation_1.pptx", filePath, "7", getDate(dateCounter++)); // Slides / PowerPoint
        insertResource(db, "9", "Presentation_2.pptx", filePath, "7", getDate(dateCounter++)); // Slides / PowerPoint
        insertResource(db, "10", "Tool_Guide_BMN203.pdf", filePath, "7", getDate(dateCounter++)); // Tools

        // BQM304 (Course ID: 2, Section IDs: 3-4)
        insertResource(db, "11", "Course_Identification.pdf", filePath, "7", getDate(dateCounter++)); // Course Identification
        for (int i = 1; i <= 12; i++) {
            insertResource(db, "12", "Chapter_Number_" + i + ".pdf", filePath, "7", getDate(dateCounter++)); // Book / PDF
        }
        for (int i = 1; i <= 12; i++) {
            insertResource(db, "14", "Session_Number_" + i + ".lrec", filePath, "7", getDate(dateCounter++)); // Recorded Sessions
        }
        insertResource(db, "15", "Reference_2.pdf", filePath, "7", getDate(dateCounter++)); // References
        insertResource(db, "16", "Practice_Exam_2.pdf", filePath, "7", getDate(dateCounter++)); // Training Exam
        insertResource(db, "17", "Presentation_3.pptx", filePath, "7", getDate(dateCounter++)); // Slides / PowerPoint

        // CCN401 (Course ID: 3, Section IDs: 5-8)
        insertResource(db, "18", "Course_Identification.pdf", filePath, "7", getDate(dateCounter++)); // Course Identification
        for (int i = 1; i <= 12; i++) {
            insertResource(db, "19", "Chapter_Number_" + i + ".pdf", filePath, "7", getDate(dateCounter++)); // Book / PDF
        }
        for (int i = 1; i <= 12; i++) {
            insertResource(db, "21", "Session_Number_" + i + ".lrec", filePath, "7", getDate(dateCounter++)); // Recorded Sessions
        }
        insertResource(db, "22", "Reference_3.pdf", filePath, "7", getDate(dateCounter++)); // References
        insertResource(db, "22", "Reference_4.pdf", filePath, "7", getDate(dateCounter++)); // References
        insertResource(db, "23", "Practice_Exam_3.pdf", filePath, "7", getDate(dateCounter++)); // Training Exam
        insertResource(db, "24", "Schedule_S24.pdf", filePath, "7", getDate(dateCounter++)); // Semester Plan
        insertResource(db, "26", "Presentation_4.pptx", filePath, "7", getDate(dateCounter++)); // Slides / PowerPoint
        insertResource(db, "26", "Presentation_5.pptx", filePath, "7", getDate(dateCounter++)); // Slides / PowerPoint
        insertResource(db, "28", "Presentation_6.pptx", filePath, "7", getDate(dateCounter++)); // Slides / PowerPoint

        // CCN403 (Course ID: 4, Section IDs: 9-12)
        insertResource(db, "29", "MATLAB.rar", filePath, "7", getDate(dateCounter++)); // MATLAB
        insertResource(db, "30", "IDM.rar", filePath, "7", getDate(dateCounter++)); // IDM
        insertResource(db, "31", "PacketTracer_1.pka", filePath, "7", getDate(dateCounter++)); // PacketTracer
        insertResource(db, "32", "VisualStudio_Guide.pdf", filePath, "7", getDate(dateCounter++)); // VisualStudio
        insertResource(db, "33", "EMU_Guide.pdf", filePath, "7", getDate(dateCounter++)); // EMU
        insertResource(db, "34", "VLC.exe", filePath, "7", getDate(dateCounter++)); // VLC
        insertResource(db, "35", "Course_Identification.pdf", filePath, "7", getDate(dateCounter++)); // Course Identification
        for (int i = 1; i <= 12; i++) {
            insertResource(db, "36", "Chapter_Number_" + i + ".pdf", filePath, "7", getDate(dateCounter++)); // Book / PDF
        }
        for (int i = 1; i <= 12; i++) {
            insertResource(db, "38", "Session_Number_" + i + ".lrec", filePath, "7", getDate(dateCounter++)); // Recorded Sessions
        }
        insertResource(db, "39", "Reference_5.pdf", filePath, "7", getDate(dateCounter++)); // References
        insertResource(db, "40", "Practice_Exam_4.pdf", filePath, "7", getDate(dateCounter++)); // Training Exam
        insertResource(db, "41", "Schedule_S24.pdf", filePath, "7", getDate(dateCounter++)); // Semester Plan
        insertResource(db, "43", "Presentation_7.pptx", filePath, "7", getDate(dateCounter++)); // Slides / PowerPoint
        insertResource(db, "43", "Presentation_8.pptx", filePath, "7", getDate(dateCounter++)); // Slides / PowerPoint
        insertResource(db, "44", "Tool_Guide_CCN403.pdf", filePath, "7", getDate(dateCounter++)); // Tools
        insertResource(db, "45", "Presentation_9.pptx", filePath, "7", getDate(dateCounter++)); // Slides / PowerPoint
        insertResource(db, "46", "MATLAB_2.rar", filePath, "7", getDate(dateCounter++)); // MATLAB (استخدام .rar)
        insertResource(db, "47", "IDM_2.pdf", filePath, "7", getDate(dateCounter++)); // IDM
        insertResource(db, "48", "PacketTracer_2.pka", filePath, "7", getDate(dateCounter++)); // PacketTracer
        insertResource(db, "49", "VisualStudio_2.rar", filePath, "7", getDate(dateCounter++)); // VisualStudio (استخدام .rar)
        insertResource(db, "50", "EMU_2.pdf", filePath, "7", getDate(dateCounter++)); // EMU
        insertResource(db, "51", "VLC_2.pdf", filePath, "7", getDate(dateCounter++)); // VLC

        // CEE203 (Course ID: 5, Section IDs: 13-14)
        insertResource(db, "52", "Course_Identification.pdf", filePath, "7", getDate(dateCounter++)); // Course Identification
        for (int i = 1; i <= 12; i++) {
            insertResource(db, "53", "Chapter_Number_" + i + ".pdf", filePath, "7", getDate(dateCounter++)); // Book / PDF
        }
        for (int i = 1; i <= 12; i++) {
            insertResource(db, "55", "Session_Number_" + i + ".lrec", filePath, "7", getDate(dateCounter++)); // Recorded Sessions
        }
        insertResource(db, "56", "Reference_6.pdf", filePath, "7", getDate(dateCounter++)); // References
        insertResource(db, "57", "Practice_Exam_5.pdf", filePath, "7", getDate(dateCounter++)); // Training Exam
        insertResource(db, "58", "Schedule_S24.pdf", filePath, "7", getDate(dateCounter++)); // Semester Plan
        insertResource(db, "60", "Presentation_10.pptx", filePath, "7", getDate(dateCounter++)); // Slides / PowerPoint
        insertResource(db, "60", "Presentation_11.pptx", filePath, "7", getDate(dateCounter++)); // Slides / PowerPoint
        insertResource(db, "61", "Tool_Guide_CEE203.pdf", filePath, "7", getDate(dateCounter++)); // Tools

        // CEE205 (Course ID: 6, Section IDs: 15-17)
        insertResource(db, "62", "Course_Identification.pdf", filePath, "7", getDate(dateCounter++)); // Course Identification
        for (int i = 1; i <= 12; i++) {
            insertResource(db, "63", "Chapter_Number_" + i + ".pdf", filePath, "7", getDate(dateCounter++)); // Book / PDF
        }
        for (int i = 1; i <= 12; i++) {
            insertResource(db, "65", "Session_Number_" + i + ".lrec", filePath, "7", getDate(dateCounter++)); // Recorded Sessions
        }
        insertResource(db, "66", "Reference_7.pdf", filePath, "7", getDate(dateCounter++)); // References
        insertResource(db, "67", "Practice_Exam_6.pdf", filePath, "7", getDate(dateCounter++)); // Training Exam
        insertResource(db, "68", "Schedule_S24.pdf", filePath, "7", getDate(dateCounter++)); // Semester Plan
        insertResource(db, "70", "Presentation_12.pptx", filePath, "7", getDate(dateCounter++)); // Slides / PowerPoint
        insertResource(db, "71", "Tool_Guide_CEE205.pdf", filePath, "7", getDate(dateCounter++)); // Tools
        insertResource(db, "72", "MATLAB_1.m", filePath, "7", getDate(dateCounter++)); // MATLAB
        insertResource(db, "73", "IDM.rar", filePath, "7", getDate(dateCounter++)); // IDM
        insertResource(db, "74", "PacketTracer_3.pka", filePath, "7", getDate(dateCounter++)); // PacketTracer
        insertResource(db, "75", "VisualStudio_3.zip", filePath, "7", getDate(dateCounter++)); // VisualStudio (استخدام .zip)
        insertResource(db, "76", "EMU_Guide.pdf", filePath, "7", getDate(dateCounter++)); // EMU
        insertResource(db, "77", "VLC.exe", filePath, "7", getDate(dateCounter++)); // VLC

        // GMA204 (Course ID: 7, Section IDs: 18-21)
        insertResource(db, "78", "Course_Identification.pdf", filePath, "7", getDate(dateCounter++)); // Course Identification
        for (int i = 1; i <= 12; i++) {
            insertResource(db, "79", "Chapter_Number_" + i + ".pdf", filePath, "7", getDate(dateCounter++)); // Book / PDF
        }
        for (int i = 1; i <= 12; i++) {
            insertResource(db, "81", "Session_Number_" + i + ".lrec", filePath, "7", getDate(dateCounter++)); // Recorded Sessions
        }
        insertResource(db, "82", "Reference_8.pdf", filePath, "7", getDate(dateCounter++)); // References
        insertResource(db, "82", "Reference_9.pdf", filePath, "7", getDate(dateCounter++)); // References
        insertResource(db, "83", "Practice_Exam_7.pdf", filePath, "7", getDate(dateCounter++)); // Training Exam
        insertResource(db, "84", "Schedule_S24.pdf", filePath, "7", getDate(dateCounter++)); // Semester Plan
        insertResource(db, "86", "Presentation_13.pptx", filePath, "7", getDate(dateCounter++)); // Slides / PowerPoint
        insertResource(db, "86", "Presentation_14.pptx", filePath, "7", getDate(dateCounter++)); // Slides / PowerPoint
        insertResource(db, "87", "Tool_Guide_GMA204.pdf", filePath, "7", getDate(dateCounter++)); // Tools
        insertResource(db, "88", "Presentation_15.pptx", filePath, "7", getDate(dateCounter++)); // Slides / PowerPoint
        insertResource(db, "89", "MATLAB.rar", filePath, "7", getDate(dateCounter++)); // MATLAB
        insertResource(db, "90", "IDM.rar", filePath, "7", getDate(dateCounter++)); // IDM
        insertResource(db, "91", "PacketTracer_4.pka", filePath, "7", getDate(dateCounter++)); // PacketTracer
        insertResource(db, "92", "VisualStudio_4.zip", filePath, "7", getDate(dateCounter++)); // VisualStudio (استخدام .zip)
        insertResource(db, "93", "EMU_Guide.pdf", filePath, "7", getDate(dateCounter++)); // EMU
        insertResource(db, "94", "VLC.exe", filePath, "7", getDate(dateCounter++)); // VLC

        // GMA205 (Course ID: 8, Section IDs: 22-24)
        insertResource(db, "95", "Course_Identification.pdf", filePath, "7", getDate(dateCounter++)); // Course Identification
        for (int i = 1; i <= 12; i++) {
            insertResource(db, "96", "Chapter_Number_" + i + ".pdf", filePath, "7", getDate(dateCounter++)); // Book / PDF
        }
        for (int i = 1; i <= 12; i++) {
            insertResource(db, "98", "Session_Number_" + i + ".lrec", filePath, "7", getDate(dateCounter++)); // Recorded Sessions
        }
        insertResource(db, "99", "Reference_10.pdf", filePath, "7", getDate(dateCounter++)); // References
        insertResource(db, "100", "Practice_Exam_8.pdf", filePath, "7", getDate(dateCounter++)); // Training Exam
        insertResource(db, "101", "Schedule_S24.pdf", filePath, "7", getDate(dateCounter++)); // Semester Plan
        insertResource(db, "103", "Presentation_16.pptx", filePath, "7", getDate(dateCounter++)); // Slides / PowerPoint
        insertResource(db, "104", "Tool_Guide_GMA205.pdf", filePath, "7", getDate(dateCounter++)); // Tools
        insertResource(db, "105", "Presentation_17.pptx", filePath, "7", getDate(dateCounter++)); // Slides / PowerPoint

        // INT101 (Course ID: 9, Section IDs: 25-27)
        insertResource(db, "106", "Course_Identification.pdf", filePath, "7", getDate(dateCounter++)); // Course Identification
        for (int i = 1; i <= 12; i++) {
            insertResource(db, "107", "Chapter_Number_" + i + ".pdf", filePath, "7", getDate(dateCounter++)); // Book / PDF
        }
        for (int i = 1; i <= 12; i++) {
            insertResource(db, "109", "Session_Number_" + i + ".lrec", filePath, "7", getDate(dateCounter++)); // Recorded Sessions
        }
        insertResource(db, "110", "Reference_11.pdf", filePath, "7", getDate(dateCounter++)); // References
        insertResource(db, "111", "Practice_Exam_9.pdf", filePath, "7", getDate(dateCounter++)); // Training Exam
        insertResource(db, "112", "Schedule_S24.pdf", filePath, "7", getDate(dateCounter++)); // Semester Plan
        insertResource(db, "114", "Presentation_18.pptx", filePath, "7", getDate(dateCounter++)); // Slides / PowerPoint
        insertResource(db, "114", "Presentation_19.pptx", filePath, "7", getDate(dateCounter++)); // Slides / PowerPoint
        insertResource(db, "115", "Tool_Guide_INT101.pdf", filePath, "7", getDate(dateCounter++)); // Tools
        insertResource(db, "116", "MATLAB.rar", filePath, "7", getDate(dateCounter++)); // MATLAB
        insertResource(db, "117", "IDM.rar", filePath, "7", getDate(dateCounter++)); // IDM
        insertResource(db, "118", "PacketTracer_5.pka", filePath, "7", getDate(dateCounter++)); // PacketTracer
        insertResource(db, "119", "VisualStudio_5.zip", filePath, "7", getDate(dateCounter++)); // VisualStudio (استخدام .zip)
        insertResource(db, "120", "EMU_Guide.pdf", filePath, "7", getDate(dateCounter++)); // EMU
        insertResource(db, "121", "VLC.exe", filePath, "7", getDate(dateCounter++)); // VLC

        // IPG101 (Course ID: 11, Section IDs: 32-34)
        insertResource(db, "122", "Course_Identification.pdf", filePath, "7", getDate(dateCounter++)); // Course Identification
        for (int i = 1; i <= 12; i++) {
            insertResource(db, "123", "Chapter_Number_" + i + ".pdf", filePath, "7", getDate(dateCounter++)); // Book / PDF
        }
        for (int i = 1; i <= 12; i++) {
            insertResource(db, "125", "Session_Number_" + i + ".lrec", filePath, "7", getDate(dateCounter++)); // Recorded Sessions
        }
        insertResource(db, "126", "Reference_12.pdf", filePath, "7", getDate(dateCounter++)); // References
        insertResource(db, "127", "Practice_Exam_10.pdf", filePath, "7", getDate(dateCounter++)); // Training Exam
        insertResource(db, "128", "Schedule_S24.pdf", filePath, "7", getDate(dateCounter++)); // Semester Plan
        insertResource(db, "130", "Presentation_20.pptx", filePath, "7", getDate(dateCounter++)); // Slides / PowerPoint
        insertResource(db, "130", "Presentation_21.pptx", filePath, "7", getDate(dateCounter++)); // Slides / PowerPoint
        insertResource(db, "131", "Tool_Guide_IPG101.pdf", filePath, "7", getDate(dateCounter++)); // Tools
        insertResource(db, "132", "Presentation_22.pptx", filePath, "7", getDate(dateCounter++)); // Slides / PowerPoint
        insertResource(db, "133", "MATLAB_3.rar", filePath, "7", getDate(dateCounter++)); // MATLAB (استخدام .rar)
        insertResource(db, "134", "IDM.rar", filePath, "7", getDate(dateCounter++)); // IDM
        insertResource(db, "135", "PacketTracer_6.pka", filePath, "7", getDate(dateCounter++)); // PacketTracer
        insertResource(db, "136", "VisualStudio_6.rar", filePath, "7", getDate(dateCounter++)); // VisualStudio (استخدام .rar)
        insertResource(db, "137", "EMU_Guide.pdf", filePath, "7", getDate(dateCounter++)); // EMU
        insertResource(db, "138", "VLC.exe", filePath, "7", getDate(dateCounter++)); // VLC

        // IPG204 (Course ID: 12, Section IDs: 35-36)
        insertResource(db, "139", "Course_Identification.pdf", filePath, "7", getDate(dateCounter++)); // Course Identification
        for (int i = 1; i <= 12; i++) {
            insertResource(db, "140", "Chapter_Number_" + i + ".pdf", filePath, "7", getDate(dateCounter++)); // Book / PDF
        }
        for (int i = 1; i <= 12; i++) {
            insertResource(db, "142", "Session_Number_" + i + ".lrec", filePath, "7", getDate(dateCounter++)); // Recorded Sessions
        }
        insertResource(db, "143", "Reference_13.pdf", filePath, "7", getDate(dateCounter++)); // References
        insertResource(db, "144", "Practice_Exam_11.pdf", filePath, "7", getDate(dateCounter++)); // Training Exam
        insertResource(db, "145", "Schedule_S24.pdf", filePath, "7", getDate(dateCounter++)); // Semester Plan
        insertResource(db, "147", "Presentation_23.pptx", filePath, "7", getDate(dateCounter++)); // Slides / PowerPoint
        insertResource(db, "148", "Tool_Guide_IPG204.pdf", filePath, "7", getDate(dateCounter++)); // Tools
        insertResource(db, "149", "Presentation_24.pptx", filePath, "7", getDate(dateCounter++)); // Slides / PowerPoint
        insertResource(db, "150", "Course_Identification.pdf", filePath, "7", getDate(dateCounter++)); // Course Identification
        for (int i = 1; i <= 12; i++) {
            insertResource(db, "151", "Chapter_Number_" + i + ".pdf", filePath, "7", getDate(dateCounter++)); // Book / PDF
        }
        for (int i = 1; i <= 12; i++) {
            insertResource(db, "153", "Session_Number_" + i + ".lrec", filePath, "7", getDate(dateCounter++)); // Recorded Sessions
        }
        insertResource(db, "154", "Reference_14.pdf", filePath, "7", getDate(dateCounter++)); // References
        insertResource(db, "155", "Practice_Exam_12.pdf", filePath, "7", getDate(dateCounter++)); // Training Exam
        insertResource(db, "156", "Schedule_S24_2.pdf", filePath, "7", getDate(dateCounter++)); // Semester Plan
        insertResource(db, "158", "Presentation_25.pptx", filePath, "7", getDate(dateCounter++)); // Slides / PowerPoint
        insertResource(db, "159", "Tool_Guide_IPG204_2.pdf", filePath, "7", getDate(dateCounter++)); // Tools

        // إدخال بيانات جدول تسجيل المقررات (Enrollment)
        insertEnrollment(db, "1", "1", "Passed", 0, "2024-06-01");
        insertEnrollment(db, "1", "2", "Passed", 1, "2024-11-01");
        insertEnrollment(db, "1", "3", "Passed", 0, "2024-11-01");
        insertEnrollment(db, "1", "4", "Passed", 0, "2024-06-01");
        insertEnrollment(db, "1", "5", "Passed", 1, "2024-11-01");
        insertEnrollment(db, "1", "6", "Registered", 0, "2023-01-01");
        insertEnrollment(db, "1", "7", "Registered", 1, "2023-01-01");
        insertEnrollment(db, "1", "8", "Passed", 1, "2024-06-01");
        insertEnrollment(db, "1", "9", "Remaining", 0, "2023-01-01");
        insertEnrollment(db, "1", "10", "Registered", 1, "2024-11-01");
        insertEnrollment(db, "1", "11", "Registered", 0, "2023-01-01");
        insertEnrollment(db, "1", "12", "Passed", 0, "2024-06-01");
        insertEnrollment(db, "1", "13", "Passed", 0, "2024-06-01");
        insertEnrollment(db, "1", "14", "Passed", 0, "2024-06-01");

        insertEnrollment(db, "2", "1", "Registered", 0, "2024-11-01");
        insertEnrollment(db, "2", "2", "Registered", 1, "2024-11-01");
        insertEnrollment(db, "2", "3", "Registered", 0, "2024-11-01");
        insertEnrollment(db, "2", "4", "Registered", 1, "2024-06-01");
        insertEnrollment(db, "2", "5", "Remaining", 1, "2024-06-01");
        insertEnrollment(db, "2", "6", "Passed", 0, "2023-01-01");
        insertEnrollment(db, "2", "7", "Remaining", 1, "2023-01-01");
        insertEnrollment(db, "2", "8", "Passed", 1, "2024-06-01");
        insertEnrollment(db, "2", "9", "Remaining", 0, "2023-01-01");
        insertEnrollment(db, "2", "10", "Registered", 1, "2024-11-01");
        insertEnrollment(db, "2", "11", "Remaining", 0, "2023-01-01");
        insertEnrollment(db, "2", "12", "Passed", 0, "2024-06-01");
        insertEnrollment(db, "2", "13", "Passed", 0, "2024-06-01");
        insertEnrollment(db, "2", "14", "Passed", 0, "2024-06-01");

        insertEnrollment(db, "3", "1", "Remaining", 0, "2024-06-01");
        insertEnrollment(db, "3", "2", "Remaining", 1, "2024-06-01");
        insertEnrollment(db, "3", "3", "Passed", 0, "2024-06-01");
        insertEnrollment(db, "3", "4", "Passed", 1, "2024-06-01");
        insertEnrollment(db, "3", "5", "Passed", 1, "2024-06-01");
        insertEnrollment(db, "3", "6", "Passed", 0, "2023-01-01");
        insertEnrollment(db, "3", "7", "Remaining", 1, "2023-01-01");
        insertEnrollment(db, "3", "8", "Registered", 1, "2024-06-01");
        insertEnrollment(db, "3", "9", "Remaining", 0, "2023-01-01");
        insertEnrollment(db, "3", "10", "Registered", 1, "2024-11-01");
        insertEnrollment(db, "3", "11", "Remaining", 0, "2023-01-01");
        insertEnrollment(db, "3", "12", "Registered", 0, "2024-06-01");
        insertEnrollment(db, "3", "13", "Passed", 0, "2024-06-01");
        insertEnrollment(db, "3", "14", "Passed", 0, "2024-06-01");

        insertEnrollment(db, "4", "1", "Registered", 0, "2024-06-01");
        insertEnrollment(db, "4", "2", "Registered", 1, "2024-06-01");
        insertEnrollment(db, "4", "3", "Registered", 0, "2024-06-01");
        insertEnrollment(db, "4", "4", "Registered", 1, "2024-06-01");

        insertEnrollment(db, "5", "3", "Registered", 0, "2024-06-01");
        insertEnrollment(db, "5", "4", "Registered", 1, "2024-06-01");
        insertEnrollment(db, "5", "5", "Registered", 1, "2024-06-01");
        insertEnrollment(db, "5", "6", "Registered", 0, "2023-01-01");
        insertEnrollment(db, "5", "7", "Registered", 1, "2023-01-01");
        insertEnrollment(db, "5", "8", "Registered", 1, "2024-06-01");
        insertEnrollment(db, "5", "9", "Registered", 0, "2023-01-01");
        insertEnrollment(db, "5", "10", "Registered", 1, "2024-11-01");
        insertEnrollment(db, "5", "11", "Registered", 0, "2023-01-01");
        insertEnrollment(db, "5", "12", "Registered", 0, "2024-06-01");

        insertEnrollment(db, "6", "1", "Passed", 0, "2024-06-01");
        insertEnrollment(db, "6", "2", "Passed", 1, "2024-06-01");
        insertEnrollment(db, "6", "3", "Passed", 0, "2024-06-01");
        insertEnrollment(db, "6", "4", "Passed", 1, "2024-06-01");
        insertEnrollment(db, "6", "5", "Passed", 1, "2024-06-01");
        insertEnrollment(db, "6", "6", "Passed", 0, "2023-01-01");
        insertEnrollment(db, "6", "7", "Passed", 1, "2023-01-01");

        // إدخال بيانات جدول الـ Assignment
     /*   insertAssignment(db, "8", "Human Resources Management HW", "وظيفة إدارة الموارد البشرية", "2024-11-10", "2025-12-15", "https://drive.google.com/uc?export=download&id=1Vk1auP04e77JdHsdivdnEpjd7aU3M8YO", "7");
        insertAssignment(db, "25", "Wireless Communications Networks HW", "وظيفة شبكات الاتصالات اللاسلكية", "2024-11-11", "2025-09-16", "https://drive.google.com/uc?export=download&id=1Vk1auP04e77JdHsdivdnEpjd7aU3M8YO", "7");
        insertAssignment(db, "42", "Mobile Applications for trading company", "تطبيق جوال لشركة بيع سلع", "2024-11-12", "2025-11-17", "https://drive.google.com/uc?export=download&id=1Vk1auP04e77JdHsdivdnEpjd7aU3M8YO", "7");
        insertAssignment(db, "59", "Signals and Systems HW", "وظيفة الإشارات والنظم", "2024-11-13", "2025-11-18", "https://drive.google.com/uc?export=download&id=19S5SKxKXVcDLk9PYegxKT5OYn2wOB0Gg", "7");
        insertAssignment(db, "69", "Digital Signal Processing HW", "وظيفة معالجة الإشارات الرقمية", "2024-11-14", "2025-05-19", "https://drive.google.com/uc?export=download&id=1RACvMKYAhXaTqR41giWMr9y7gfdEG-Im", "7");
        insertAssignment(db, "85", "Discrete Mathematics HW", "وظيفة الرياضيات المتقطعة", "2024-11-15", "2025-10-17", "https://drive.google.com/uc?export=download&id=1iRRaosjTwvQBMGo1wXQ5pnXyfCR8O-rj", "7");
        insertAssignment(db, "102", "Probability & Statistics HW", "وظيفة الاحتمالات والإحصاء", "2024-11-16", "2025-10-28", "https://drive.google.com/uc?export=download&id=1jOGuQbv0_Ebdlj9MMRw4bUeVGAvm9EhE", "7");
        insertAssignment(db, "113", "Introduction to Networks HW", "وظيفة مقدمة في الشبكات", "2024-11-17", "2025-09-22", "https://drive.google.com/uc?export=download&id=1YZPGv1E2eFeUt-AGaTxTbBs3z_F3p4Tc", "7");
        insertAssignment(db, "129", "Network & IT Infrastructure Security HW", "وظيفة أمن الشبكات والبنية التحتية المعلوماتية", "2024-11-18", "2025-08-23", "https://drive.google.com/uc?export=download&id=1bUrXxPP16kvLDgfRDBsDEXCOm8AsymcV", "7");
        insertAssignment(db, "146", "Introduction to Programming HW", "وظيفة مقدمة في البرمجة", "2024-11-19", "2025-07-24", "https://drive.google.com/uc?export=download&id=1TgjyfUxbExL9w_pSRxGLQj6euWf6c7JT", "7");
        insertAssignment(db, "157", "Object Oriented Programming HW", "وظيفة التصميم والبرمجة غرضية التوجه", "2024-11-20", "2025-01-25", "https://drive.google.com/uc?export=download&id=1TgjyfUxbExL9w_pSRxGLQj6euWf6c7JT", "7");
*/
        insertAssignment(db, "8", "Computer Skills-ICDL Assignment", "وظيفة مهارات الحاسوب", "2025-09-10", "2025-11-20", "https", "7");
        insertAssignment(db, "25", "Signal Processing Assignment", "وظيفة معالجة إشارة", "2025-09-12", "2025-11-22", "https", "7");
        insertAssignment(db, "42", "Telecommunication Systems Assignment", "وظيفة نظم الاتصالات", "2025-09-13", "2025-11-23", "https", "7");
        insertAssignment(db, "59", "Programming III Assignment", "وظيفة برمجة (3)", "2025-09-14", "2025-11-24", "https", "7");
        insertAssignment(db, "69", "Artificial Intelligence Assignment", "وظيفة الذكاء الصنعي", "2025-09-15", "2025-11-25", "https", "7");
        insertAssignment(db, "85", "Mobile Applications Programming Assignment", "وظيفة برمجة تطبيقات النقال", "2025-09-23", "2025-11-26", "https", "7");
        insertAssignment(db, "102", "Ethics of Profession & Society Assignment", "وظيفة أخلاقيات المهنة والمجتمع", "2025-09-17", "2025-11-27", "https", "7");
        insertAssignment(db, "129", "Algorithm Analysis & Design Assignment", "وظيفة تحليل وتصميم الخوارزميات", "2025-09-19", "2025-11-29", "https", "7");
        insertAssignment(db, "146", "Data Mining Assignment", "وظيفة التنقيب في البيانات", "2025-09-20", "2025-11-30", "https", "7");
        insertAssignment(db, "157", "Information Retrieval Assignment", "وظيفة استرجاع المعلومات", "2025-09-21", "2025-11-30", "https", "7");
        insertAssignment(db, "167", "Semantic Web Assignment", "وظيفة الويب الدلالي", "2025-09-22", "2025-11-30", "https", "7");
        insertAssignment(db, "177", "Software Quality (in English) Assignment", "وظيفة جودة البرمجيات (باللغة الإنكليزية)", "2025-09-23", "2025-11-30", "https", "7");
        /*  insertAssignment(db, "", "Linear Algebra Assignment", "وظيفة الجبر الخطي", "2025-09-11", "2025-11-21", "https", "7");
insertAssignment(db, "129", "Information System Security Assignment", "وظيفة أمن نظم المعلومات", "2025-09-18", "2025-11-28", "https", "7");
        */
        // إدخال إشعارات ثابتة لجميع المستخدمين
        // المستخدم 1: Abdo Al-Khoury (مقرر SDE601)
        /*insertNotification(db,
                "1", // userId
                "New Section for Data Mining Course SDE601 has been added. You can now view the details!",
                "تمت إضافة قسم جديد لمقرر التنقيب في البيانات SDE601. يمكنك الآن الاطلاع على التفاصيل!",
                0, // isRead
                "course_sections",
                "11", // relatedId (course_id = 11)
                "2025-08-25-23:34:50"
        );
        insertNotification(db,
                "1",
                "New content has been added to the resources section for CEE203. Explore it now!",
                "تمت إضافة محتوى جديد إلى قسم الموارد لـ CEE203. استكشفه الآن!",
                0,
                "course_tool",
                "5",
                "2025-05-02-17:10:09"
        );
        insertNotification(db,
                "1",
                "Some files for INT305 have been modified. See the course page for changes.",
                "تم تعديل بعض ملفات INT305. راجع صفحة المقرر للاطلاع على التغييرات.",
                0,
                "course_files",
                "10",
                "2025-05-03-20:30:04"
        );

        // المستخدم 2: Lana Kaddourah (مقرر BQM304)
        insertNotification(db,
                "2",
                "New Section for CCN401 has been added. You can now view the details!",
                "تمت إضافة قسم جديد لـ CCN401. يمكنك الآن الاطلاع على التفاصيل!",
                0,
                "course_sections",
                "3",
                "2025-05-01-22:50:24"
        );
        insertNotification(db,
                "2",
                "New content has been added to the resources section for INT305. Explore it now!",
                "تمت إضافة محتوى جديد إلى قسم الموارد لـ INT305. استكشفه الآن!",
                0,
                "course_tool",
                "10",
                "2025-05-02-14:18:04"
        );
        insertNotification(db,
                "2",
                "Some files for BMN203 have been modified. See the course page for changes.",
                "تم تعديل بعض ملفات BMN203. راجع صفحة المقرر للاطلاع على التغييرات.",
                0,
                "course_files",
                "1",
                "2025-05-03-10:10:19"
        );

        // المستخدم 3: Abeer Kharfan (مقرر CCN401)
        insertNotification(db,
                "3",
                "New Section for GMA205 has been added. You can now view the details!",
                "تمت إضافة قسم جديد لـ GMA205. يمكنك الآن الاطلاع على التفاصيل!",
                0,
                "course_sections",
                "8",
                "2025-05-01-13:02:12"
        );
        insertNotification(db,
                "3",
                "New content has been added to the resources section for IPG204. Explore it now!",
                "تمت إضافة محتوى جديد إلى قسم الموارد لـ IPG204. استكشفه الآن!",
                0,
                "course_tool",
                "12",
                "2025-05-02-15-25-00"
        );
        insertNotification(db,
                "3",
                "Some files for INT305 have been modified. See the course page for changes.",
                "تم تعديل بعض ملفات INT305. راجع صفحة المقرر للاطلاع على التغييرات.",
                0,
                "course_files",
                "10",
                "2025-05-03-19:08:04"
        );
        */

        // المستخدم 1: Abdo Al-Khoury (مقرر SDE601)
        insertNotification(db,
                "1", // userId
                "New Section for Data Mining Course SDE601 has been added. You can now view the details!",
                "تمت إضافة قسم جديد لمقرر التنقيب في البيانات SDE601. يمكنك الآن الاطلاع على التفاصيل!",
                0, // isRead
                "course_sections",
                "11", // relatedId (course_id = 11)
                "2025-08-25-23:34:50"
        );

        // المستخدم 1: Abdo Al-Khoury (مقرر SDE601)
        insertNotification(db,
                "1", // userId
                "New Section for Information Retrieval SIR601 has been added. You can now view the details!",
                "تمت إضافة قسم جديد لمقرر استرجاع البيانات SIR601. يمكنك الآن الاطلاع على التفاصيل!",
                0, // isRead
                "course_sections",
                "11", // relatedId (course_id = 11)
                "2025-08-25-23:34:50"
        );

// المستخدم 1: Abdo Al-Khoury (مقرر SIR601)
        insertNotification(db,
                "1", // userId
                "New content has been added to the resources section for for Information Retrieval Course SIR601. Explore it now!",
                "تمت إضافة محتوى جديد إلى قسم الموارد لمقرر استرجاع البيانات SIR601. استكشفه الآن!",
                0, // isRead
                "course_tool ",
                "12", // relatedId (course_id = 12)
                "2025-08-20-22:44:10"
        );

// المستخدم 1: Abdo Al-Khoury (مقرر SSW601)
        insertNotification(db,
                "1", // userId
                "Some files for Semantic Web Course SSW601 have been modified. See the course page for changes.",
                "تم تعديل بعض ملفات في مقرر الويب الدلالي SSW601. راجع صفحة المقرر للاطلاع على التغييرات.",
                0, // isRead
                " course_files",
                "13", // relatedId (course_id = 13)
                "2025-08-15-21:22:20"
        );

// المستخدم 2: Jeeny Abdoush (مقرر GCS301)
        insertNotification(db,
                "2", // userId
                "New Section for Computer Skills-ICDL Course GCS301 has been added. You can now view the details!",
                "تمت إضافة قسم جديد لمقرر مهارات الحاسوب GCS301. يمكنك الآن الاطلاع على التفاصيل!",
                0, // isRead
                "course_sections",
                "2", // relatedId (course_id = 2)
                "2025-08-10-20:20:20"
        );

// المستخدم 2: Jeeny Abdoush (مقرر BLA401)
        insertNotification(db,
                "2", // userId
                "New content has been added to the resources section for Data Mining Course SDE601. Explore it now!",
                " مادة التنقيب عن البيانات تمت إضافة محتوى جديد إلى قسم الموارد لـ SDE601. استكشفه الآن!",
                0, // isRead
                " course_tool",
                "11", // relatedId (course_id = 11)
                "2025-08-05-23:23:30"
        );

// المستخدم 2: Jeeny Abdoush (مقرر BSP501)
        insertNotification(db,
                "2", // userId
                "Some files for Signal Processing Course BSP501 have been modified. See the course page for changes.",
                "تم تعديل بعض ملفات في مقرر معالجة إشارة BSP501. راجع صفحة المقرر للاطلاع على التغييرات.",
                0, // isRead
                " course_files",
                "3", // relatedId (course_id = 3)
                "2025-08-01-23:55:40"
        );

// المستخدم 3: Joseph Daoud (مقرر BAI501)
        insertNotification(db,
                "3", // userId
                "New Section for Artificial Intelligence Course BAI501 has been added. You can now view the details!",
                "تمت إضافة قسم جديد لمقرر الذكاء الصنعي BAI501. يمكنك الآن الاطلاع على التفاصيل!",
                0, // isRead
                "course_sections",
                "6", // relatedId (course_id = 6)
                "2025-08-22-23:32:30"
        );

// المستخدم 3: Joseph Daoud (مقرر BMP601)
        insertNotification(db,
                "3", // userId
                "New content has been added to the resources section for Mobile Applications Programming Course BMP601. Explore it now!",
                "لمقرر برمجة تطبيقات النقال تمت إضافة محتوى جديد إلى قسم الموارد BMP601. استكشفه الآن!",
                0, // isRead
                " course_tool",
                "7", // relatedId (course_id = 7)
                "2025-08-14-22:40:20"
        );

// المستخدم 3: Joseph Daoud (مقرر GET601)
        insertNotification(db,
                "3", // userId
                "Some files for Semantic Web Course SSW601 have been modified. See the course page for changes.",
                "تم تعديل بعض ملفات في مقرر الويب الدلالي SSW601. راجع صفحة المقرر للاطلاع على التغييرات.",
                0, // isRead
                " course_files",
                "13", // relatedId (course_id = 13)
                "2025-08-08-20:50:50"
        );


        // إدخال بيانات جدول الأحداث (Event)
        // الأحداث السابقة (من الرد السابق)
        // 1. أحداث بداية وانتهاء الواجبات
        // واجب BMN203 (assignment_id = 1) لـ Omar (userId = 1)
        insertEvent(db,
                "1",
                "Start of Human Resources Management HW",
                "بداية واجب إدارة الموارد البشرية",
                "2024-11-10",
                "assignment_start",
                "1"
        );
        insertEvent(db,
                "1",
                "Due Date for Human Resources Management HW",
                "الموعد النهائي لواجب إدارة الموارد البشرية",
                "2025-10-15",
                "assignment_due",
                "1"
        );

        // واجب CCN401 (assignment_id = 2) لـ Lana (userId = 2)
        insertEvent(db,
                "2",
                "Start of Wireless Communications Networks HW",
                "بداية واجب شبكات الاتصالات اللاسلكية",
                "2024-11-11",
                "assignment_start",
                "2"
        );
        insertEvent(db,
                "2",
                "Due Date for Wireless Communications Networks HW",
                "الموعد النهائي لواجب شبكات الاتصالات اللاسلكية",
                "2025-08-16",
                "assignment_due",
                "2"
        );

        // واجب GMA205 (assignment_id = 7) لـ Abeer (userId = 3)
        insertEvent(db,
                "3",
                "Start of Probability & Statistics HW",
                "بداية واجب الاحتمالات والإحصاء",
                "2024-11-16",
                "assignment_start",
                "7"
        );
        insertEvent(db,
                "3",
                "Due Date for Probability & Statistics HW",
                "الموعد النهائي لواجب الاحتمالات والإحصاء",
                "2025-10-28",
                "assignment_due",
                "7"
        );

        // 2. أحداث تعديل المقررات
        insertEvent(db,
                "1",
                "Human Resources Management Course Updated",
                "تم تحديث مقرر إدارة الموارد البشرية",
                "2025-05-10",
                "course_update",
                "1"
        );
        insertEvent(db,
                "2",
                "Wireless Communications Networks Course Updated",
                "تم تحديث مقرر شبكات الاتصالات اللاسلكية",
                "2025-05-11",
                "course_update",
                "3"
        );
        insertEvent(db,
                "3",
                "Probability & Statistics Course Updated",
                "تم تحديث مقرر الاحتمالات والإحصاء",
                "2025-05-12",
                "course_update",
                "8"
        );

        // 3. أحداث بداية ونهاية الفصل الدراسي (F24, term_id = 1)
        insertEvent(db,
                "1",
                "Start of F24 Semester",
                "بداية الفصل الدراسي F24",
                "2025-08-30",
                "term_start",
                "1"
        );
        insertEvent(db,
                "1",
                "End of F24 Semester",
                "نهاية الفصل الدراسي F24",
                "2026-02-30",
                "term_end",
                "1"
        );
        insertEvent(db,
                "2",
                "Start of F24 Semester",
                "بداية الفصل الدراسي F24",
                "2025-08-30",
                "term_start",
                "1"
        );
        insertEvent(db,
                "2",
                "End of F24 Semester",
                "نهاية الفصل الدراسي F24",
                "2026-02-30",
                "term_end",
                "1"
        );
        insertEvent(db,
                "3",
                "Start of F24 Semester",
                "بداية الفصل الدراسي F24",
                "2025-08-30",
                "term_start",
                "1"
        );
        insertEvent(db,
                "3",
                "End of F24 Semester",
                "نهاية الفصل الدراسي F24",
                "2026-02-30",
                "term_end",
                "1"
        );

        // 4. أحداث بداية ونهاية العام الدراسي (2025, year_id = 1)
        insertEvent(db,
                "1",
                "Start of Academic Year 2025",
                "بداية العام الدراسي 2025",
                "2025-08-30",
                "academic_year_start",
                "1"
        );
        insertEvent(db,
                "1",
                "End of Academic Year 2025",
                "نهاية العام الدراسي 2025",
                "2026-08-30",
                "academic_year_end",
                "1"
        );
        insertEvent(db,
                "2",
                "Start of Academic Year 2025",
                "بداية العام الدراسي 2025",
                "2025-08-30",
                "academic_year_start",
                "1"
        );
        insertEvent(db,
                "2",
                "End of Academic Year 2025",
                "نهاية العام الدراسي 2025",
                "2026-08-30",
                "academic_year_end",
                "1"
        );
        insertEvent(db,
                "3",
                "Start of Academic Year 2025",
                "بداية العام الدراسي 2025",
                "2025-08-30",
                "academic_year_start",
                "1"
        );
        insertEvent(db,
                "3",
                "End of Academic Year 2025",
                "نهاية العام الدراسي 2025",
                "2026-08-30",
                "academic_year_end",
                "1"
        );

        // الأحداث الجديدة
        // 5. أحداث إضافة موارد جديدة (مرتبطة بجدول Resource)
        // مورد لـ BMN203 (resource_id = 1) لـ Omar (userId = 1)
        insertEvent(db,
                "1",
                "New Resource Added for BMN203",
                "تمت إضافة مورد جديد لمقرر إدارة الموارد البشرية",
                "2025-05-13",
                "resource_added",
                "1" // resource_id
        );

        // مورد لـ CCN401 (resource_id = 2) لـ Lana (userId = 2)
        insertEvent(db,
                "2",
                "New Resource Added for CCN401",
                "تمت إضافة مورد جديد لمقرر شبكات الاتصالات اللاسلكية",
                "2025-05-14",
                "resource_added",
                "2"
        );

        // مورد لـ GMA205 (resource_id = 7) لـ Abeer (userId = 3)
        insertEvent(db,
                "3",
                "New Resource Added for GMA205",
                "تمت إضافة مورد جديد لمقرر الاحتمالات والإحصاء",
                "2025-05-15",
                "resource_added",
                "7"
        );

        // 6. أحداث إضافة أقسام مقررات (مرتبطة بجدول CourseSection)
        // قسم لـ IPG101 (section_id = 11) لـ Abdo (userId = 4)
        insertEvent(db,
                "4",
                "New Section Added for IPG101",
                "تمت إضافة قسم جديد لمقرر مقدمة في البرمجة",
                "2025-05-01",
                "section_added",
                "11" // section_id
        );

        // قسم لـ INT101 (section_id = 9) لـ Eman (userId = 5)
        insertEvent(db,
                "5",
                "New Section Added for INT101",
                "تمت إضافة قسم جديد لمقرر مقدمة في الشبكات",
                "2025-05-02",
                "section_added",
                "9"
        );

        // 7. أحداث بداية ونهاية واجبات إضافية
        // واجب IPG101 (assignment_id = 10) لـ Abdo (userId = 4)
        insertEvent(db,
                "4",
                "Start of Introduction to Programming HW",
                "بداية واجب مقدمة في البرمجة",
                "2024-11-19",
                "assignment_start",
                "10"
        );
        insertEvent(db,
                "4",
                "Due Date for Introduction to Programming HW",
                "الموعد النهائي لواجب مقدمة في البرمجة",
                "2025-07-24",
                "assignment_due",
                "10"
        );

        // واجب INT101 (assignment_id = 8) لـ Eman (userId = 5)
        insertEvent(db,
                "5",
                "Start of Introduction to Networks HW",
                "بداية واجب مقدمة في الشبكات",
                "2024-11-17",
                "assignment_start",
                "8"
        );
        insertEvent(db,
                "5",
                "Due Date for Introduction to Networks HW",
                "الموعد النهائي لواجب مقدمة في الشبكات",
                "2025-09-22",
                "assignment_due",
                "8"
        );

        // 8. أحداث إدارية
        // تحديث حالة حساب Omar (userId = 1)
        insertEvent(db,
                "1",
                "Account Status Updated",
                "تم تحديث حالة الحساب",
                "2025-05-14",
                "account_status_updated",
                "1" // user_id
        );

        // تغيير كلمة مرور Lana (userId = 2)
        insertEvent(db,
                "2",
                "Password Changed",
                "تم تغيير كلمة المرور",
                "2025-05-15",
                "password_changed",
                "2"
        );

        // 9. أحداث امتحانات
        // امتحان نهائي لـ BMN203 (course_id = 1) لـ Omar (userId = 1)
        insertEvent(db,
                "1",
                "Final Exam for BMN203",
                "الامتحان النهائي لمقرر إدارة الموارد البشرية",
                "2025-07-15", // تاريخ وهمي قبل نهاية الفصل
                "exam_scheduled",
                "1" // course_id
        );

        // امتحان نهائي لـ CCN401 (course_id = 3) لـ Lana (userId = 2)
        insertEvent(db,
                "2",
                "Final Exam for CCN401",
                "الامتحان النهائي لمقرر شبكات الاتصالات اللاسلكية",
                "2025-07-16",
                "exam_scheduled",
                "3"
        );

        // امتحان نهائي لـ GMA205 (course_id = 8) لـ Abeer (userId = 3)
        insertEvent(db,
                "3",
                "Final Exam for GMA205",
                "الامتحان النهائي لمقرر الاحتمالات والإحصاء",
                "2025-07-17",
                "exam_scheduled",
                "8"
        );
    }

    /**
     * دالة لإدخال سجل مستخدم في جدول المستخدمين
     */
    /**
     * دالة لإدخال سجل مستخدم في جدول المستخدمين.
     */
    /**
     * دالة محسنة لإدخال سجل مستخدم باستخدام ContentValues
     */
    private void insertUser(SQLiteDatabase db,
                            String nameEn,
                            String nameAr,
                            String email,
                            String passwordHash,
                            String role,
                            String programId,
                            int notificationsEnabled,
                            String accountStatus,
                            String phone,
                            String facebookUrl,
                            String whatsappNumber,
                            String telegramHandle,
                            String profilePicture,
                            String bioEn,
                            String bioAr,
                            String createdAt,
                            String updatedAt) {

        ContentValues values = new ContentValues();
        values.put(DBContract.Users.COL_NAME_EN, nameEn);
        values.put(DBContract.Users.COL_NAME_AR, nameAr);
        values.put(DBContract.Users.COL_EMAIL, email);
        values.put(DBContract.Users.COL_PASSWORD_HASH, passwordHash);
        values.put(DBContract.Users.COL_ROLE, role);
        values.put(DBContract.Users.COL_PROGRAM_ID, programId.isEmpty() ? null : programId);
        values.put(DBContract.Users.COL_NOTIFICATIONS_ENABLED, notificationsEnabled);
        values.put(DBContract.Users.COL_ACCOUNT_STATUS, accountStatus);
        values.put(DBContract.Users.COL_PHONE, phone);
        values.put(DBContract.Users.COL_FACEBOOK_URL, facebookUrl);
        values.put(DBContract.Users.COL_WHATSAPP_NUMBER, whatsappNumber);
        values.put(DBContract.Users.COL_TELEGRAM_HANDLE, telegramHandle);
        values.put(DBContract.Users.COL_PROFILE_PICTURE, profilePicture);
        values.put(DBContract.Users.COL_BIO_EN, bioEn);
        values.put(DBContract.Users.COL_BIO_AR, bioAr);
        values.put(DBContract.Users.COL_CREATED_AT, createdAt);
        values.put(DBContract.Users.COL_UPDATED_AT, updatedAt);

        db.insert(DBContract.Users.TABLE_NAME, null, values);
    }

    /**
     * دالة لإدخال بيانات في جدول السنة الدراسية (AcademicYear).
     */
    private void insertAcademicYear(SQLiteDatabase db, String name, String startDate, String endDate) {
        String sql = "INSERT INTO " + DBContract.AcademicYear.TABLE_NAME + " ("
                + DBContract.AcademicYear.COL_NAME + ", "
                + DBContract.AcademicYear.COL_START_DATE + ", "
                + DBContract.AcademicYear.COL_END_DATE
                + ") VALUES ('"
                + name + "', '"
                + startDate + "', '"
                + endDate + "');";
        db.execSQL(sql);
    }

    /**
     * دالة لإدخال بيانات في جدول الفصول الدراسية (Term).
     */
    private void insertTerm(SQLiteDatabase db, String academicYearId, String name, String startDate, String endDate) {
        String sql = "INSERT INTO " + DBContract.Term.TABLE_NAME + " ("
                + DBContract.Term.COL_ACADEMIC_YEAR_ID + ", "
                + DBContract.Term.COL_NAME + ", "
                + DBContract.Term.COL_START_DATE + ", "
                + DBContract.Term.COL_END_DATE
                + ") VALUES ('"
                + academicYearId + "', '"
                + name + "', '"
                + startDate + "', '"
                + endDate + "');";
        db.execSQL(sql);
    }

    /**
     * دالة لإدخال بيانات في جدول البرامج الأكاديمية (AcademicProgram).
     */
    private void insertAcademicProgram(SQLiteDatabase db, String code, String nameEn, String nameAr, int programDuration) {
        String sql = "INSERT INTO " + DBContract.AcademicProgram.TABLE_NAME + " ("
                + DBContract.AcademicProgram.COL_CODE + ", "
                + DBContract.AcademicProgram.COL_NAME_EN + ", "
                + DBContract.AcademicProgram.COL_NAME_AR + ", "
                + DBContract.AcademicProgram.COL_PROGRAM_DURATION
                + ") VALUES ('"
                + code + "', '"
                + nameEn + "', '"
                + nameAr + "', "
                + programDuration + ");";
        db.execSQL(sql);
    }

    /**
     * دالة لإدخال بيانات في جدول المقررات الدراسية (Course).
     */
    private void insertCourse(SQLiteDatabase db,
                              String programId,
                              String termId,
                              String code,
                              String nameEn,
                              String nameAr,
                              String createdBy,
                              String createdAt,
                              int creditHours,
                              String color,
                              String status) {
        String sql = "INSERT INTO " + DBContract.Course.TABLE_NAME + " ("
                + DBContract.Course.COL_PROGRAM_ID + ", "
                + DBContract.Course.COL_TERM_ID + ", "
                + DBContract.Course.COL_CODE + ", "
                + DBContract.Course.COL_NAME_EN + ", "
                + DBContract.Course.COL_NAME_AR + ", "
                + DBContract.Course.COL_CREATED_BY + ", "
                + DBContract.Course.COL_CREATED_AT + ", "
                + DBContract.Course.COL_CREDIT_HOURS + ", "
                + DBContract.Course.COL_COLOR + ", "
                + DBContract.Course.COL_STATUS
                + ") VALUES ('"
                + programId + "', '"
                + termId + "', '"
                + code + "', '"
                + nameEn + "', '"
                + nameAr + "', '"
                + createdBy + "', '"
                + createdAt + "', "
                + creditHours + ", '"
                + color + "', '"
                + status + "');";
        db.execSQL(sql);
    }

    /**
     * دالة لإدخال بيانات في جدول أقسام المقررات الدراسية (CourseSection).
     */
    private void insertCourseSection(SQLiteDatabase db,
                                     String courseId,
                                     String titleEn,
                                     String titleAr,
                                     int displayOrder) {
        String sql = "INSERT INTO " + DBContract.CourseSection.TABLE_NAME + " ("
                + DBContract.CourseSection.COL_COURSE_ID + ", "
                + DBContract.CourseSection.COL_TITLE_EN + ", "
                + DBContract.CourseSection.COL_TITLE_AR + ", "
                + DBContract.CourseSection.COL_DISPLAY_ORDER
                + ") VALUES ('"
                + courseId + "', '"
                + titleEn + "', '"
                + titleAr + "', "
                + displayOrder + ");";
        db.execSQL(sql);
    }

    /**
     * دالة لإدخال بيانات في جدول أدوات الأقسام (SectionTool).
     */
    private void insertSectionTool(SQLiteDatabase db, String sectionId, String nameEn, String nameAr, String actionType) {
        String sql = "INSERT INTO " + DBContract.SectionTool.TABLE_NAME + " ("
                + DBContract.SectionTool.COL_SECTION_ID + ", "
                + DBContract.SectionTool.COL_NAME_EN + ", "
                + DBContract.SectionTool.COL_NAME_AR + ", "
                + DBContract.SectionTool.COL_ACTION_TYPE
                + ") VALUES ('"
                + sectionId + "', '"
                + nameEn + "', '"
                + nameAr + "', '"
                + actionType + "');";
        db.execSQL(sql);
    }

    /**
     * دالة لإدخال بيانات في جدول الموارد (Resource).
     */
    private void insertResource(SQLiteDatabase db, String toolId, String fileName, String filePath, String uploadedBy, String uploadedAt) {
        String sql = "INSERT INTO " + DBContract.Resource.TABLE_NAME + " ("
                + DBContract.Resource.COL_TOOL_ID + ", "
                + DBContract.Resource.COL_FILE_NAME + ", "
                + DBContract.Resource.COL_FILE_PATH + ", "
                + DBContract.Resource.COL_UPLOADED_BY + ", "
                + DBContract.Resource.COL_UPLOADED_AT
                + ") VALUES ('"
                + toolId + "', '"
                + fileName + "', '"
                + filePath + "', '"
                + uploadedBy + "', '"
                + uploadedAt + "');";
        db.execSQL(sql);
    }

    /**
     * دالة لإدخال بيانات في جدول تسجيل المقررات (Enrollment).
     */
    private void insertEnrollment(SQLiteDatabase db, String userId, String courseId, String courseStatus, int isFavorite, String enrolledAt) {
        String sql = "INSERT INTO " + DBContract.Enrollment.TABLE_NAME + " ("
                + DBContract.Enrollment.COL_USER_ID + ", "
                + DBContract.Enrollment.COL_COURSE_ID + ", "
                + DBContract.Enrollment.COL_COURSE_STATUS + ", "
                + DBContract.Enrollment.COL_IS_FAVORITE + ", "
                + DBContract.Enrollment.COL_ENROLLED_AT
                + ") VALUES ('"
                + userId + "', '"
                + courseId + "', '"
                + courseStatus + "', "
                + isFavorite + ", '"
                + enrolledAt + "');";
        db.execSQL(sql);
    }

    /**
     * دالة لإدخال بيانات في جدول الواجبات (Assignment).
     */
    private void insertAssignment(SQLiteDatabase db, String toolId, String titleEn, String titleAr, String openDate, String dueDate, String assignmentFile, String createdBy) {
        String sql = "INSERT INTO " + DBContract.Assignment.TABLE_NAME + " ("
                + DBContract.Assignment.COL_TOOL_ID + ", "
                + DBContract.Assignment.COL_TITLE_EN + ", "
                + DBContract.Assignment.COL_TITLE_AR + ", "
                + DBContract.Assignment.COL_OPEN_DATE + ", "
                + DBContract.Assignment.COL_DUE_DATE + ", "
                + DBContract.Assignment.COL_ASSIGNMENT_FILE + ", "
                + DBContract.Assignment.COL_CREATED_BY
                + ") VALUES ('"
                + toolId + "', '"
                + titleEn + "', '"
                + titleAr + "', '"
                + openDate + "', '"
                + dueDate + "', '"
                + assignmentFile + "', '"
                + createdBy + "');";
        db.execSQL(sql);
    }

    /**
     * دالة لإدخال بيانات في جدول الإشعارات (Notification).
     */
    private void insertNotification(SQLiteDatabase db,
                                    String userId,
                                    String contentEn,
                                    String contentAr,
                                    int isRead,
                                    String relatedType,
                                    String relatedId,
                                    String createdAt) {
        String sql = "INSERT INTO " + DBContract.Notification.TABLE_NAME + " ("
                + DBContract.Notification.COL_USER_ID + ", "
                + DBContract.Notification.COL_CONTENT_EN + ", "
                + DBContract.Notification.COL_CONTENT_AR + ", "
                + DBContract.Notification.COL_IS_READ + ", "
                + DBContract.Notification.COL_RELATED_TYPE + ", "
                + DBContract.Notification.COL_RELATED_ID + ", "
                + DBContract.Notification.COL_CREATED_AT
                + ") VALUES ('"
                + userId + "', '"
                + contentEn + "', '"
                + contentAr + "', "
                + isRead + ", "
                + (relatedType.isEmpty() ? "NULL" : "'" + relatedType + "'") + ", "
                + (relatedId.isEmpty() ? "NULL" : "'" + relatedId + "'") + ", '"
                + createdAt + "');";
        db.execSQL(sql);
    }

    /**
     * دالة لإدخال بيانات في جدول الأحداث (Event).
     */
    private void insertEvent(SQLiteDatabase db,
                             String userId,
                             String titleEn,
                             String titleAr,
                             String eventDate,
                             String type,
                             String relatedId) {
        String sql = "INSERT INTO " + DBContract.Event.TABLE_NAME + " ("
                + DBContract.Event.COL_USER_ID + ", "
                + DBContract.Event.COL_TITLE_EN + ", "
                + DBContract.Event.COL_TITLE_AR + ", "
                + DBContract.Event.COL_EVENT_DATE + ", "
                + DBContract.Event.COL_TYPE + ", "
                + DBContract.Event.COL_RELATED_ID
                + ") VALUES ('"
                + userId + "', '"
                + titleEn + "', '"
                + titleAr + "', '"
                + eventDate + "', "
                + (type.isEmpty() ? "NULL" : "'" + type + "'") + ", "
                + (relatedId.isEmpty() ? "NULL" : "'" + relatedId + "'") + ");";
        db.execSQL(sql);
    }


    /**
     * دالة مساعدة لتوليد التواريخ تسلسليًا من 2025-01-15.
     */
    private String getDate(int counter) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, Calendar.JANUARY, 15); // تعيين التاريخ الأساسي: 2025-01-15
        calendar.add(Calendar.DAY_OF_MONTH, counter); // إضافة الأيام بناءً على العداد
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(calendar.getTime());
    }
}