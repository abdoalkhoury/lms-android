package org.svuonline.lms.ui.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;

import org.svuonline.lms.R;
import org.svuonline.lms.data.model.User;
import org.svuonline.lms.data.repository.UserRepository;
import org.svuonline.lms.ui.fragments.admin_fragments.AcademicProgramManagementFragment;
import org.svuonline.lms.ui.fragments.admin_fragments.AcademicYearManagementFragment;
import org.svuonline.lms.ui.fragments.admin_fragments.Admin_DashboardFragment;
import org.svuonline.lms.ui.fragments.admin_fragments.Admin_UsersManagementFragment;
import org.svuonline.lms.ui.fragments.admin_fragments.TermManagementFragment;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.ErrorHandler;
import org.svuonline.lms.utils.ImagePathHelper;
import org.svuonline.lms.utils.Utils;

import java.io.File;
import java.util.Objects;

/**
 * النشاط الرئيسي لعرض لوحة تحكم المدير مع قائمة جانبية، تنقل سفلي، وصفحات فراغمنت.
 */
public class AdminDashboardActivity extends BaseActivity {

    // مفاتيح SharedPreferences
    private static final String PREFS_NAME = "AppPreferences";
    private static final String PREF_MODE_KEY = "selected_mode";
    private static final String MODE_DARK = "dark";
    private static final String MODE_LIGHT = "light";

    // عناصر واجهة المستخدم
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private TextView toolbarTitle;
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;
    private ShapeableImageView profileImage;

    // المستودعات
    private UserRepository userRepository;

    // بيانات النشاط
    private long userId;
    private SharedPreferences preferences;

    // استدعاء صف التحقق من الأخطاء
    private ErrorHandler errorHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        try {
            // تحقق فوري من حالة المصادقة
            logAuthState("onCreate Start");

            setContentView(R.layout.activity_admin_dashboard);

            // 1. التحقق من بيانات المستخدم أولاً
            if (!validateUserData()) {
                return;
            }

            // 2. تهيئة المستودعات
            initComponents();

            // 3. تهيئة الواجهة
            initViews();
            applyInsets();

            // 4. تحقق إضافي من التزامن
            verifyUserConsistency();

            // 5. تحميل البيانات وإعداد المستمعات
            initData();
            setupListeners();

            logAuthState("onCreate Completed");

        } catch (Exception e) {
            Log.e("AdminDebug", "CRITICAL ERROR in onCreate: " + e.getMessage(), e);
            showAuthErrorAndRedirect("خطأ حرج في بدء التطبيق");
        }
    }

    private void logAuthState(String stage) {
        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        long prefUserId = userPrefs.getLong("user_id", -1);

        Log.d("AuthDebug", "=== AUTH STATE: " + stage + " ===");
        Log.d("AuthDebug", "Prefs User ID: " + prefUserId);
        Log.d("AuthDebug", "Class User ID: " + this.userId);
        Log.d("AuthDebug", "Consistent: " + (prefUserId == this.userId));
        Log.d("AuthDebug", "=== END AUTH STATE ===");
    }

    private void verifyUserConsistency() {
        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        long prefUserId = userPrefs.getLong("user_id", -1);

        if (prefUserId != this.userId) {
            Log.w("AuthDebug", "USER ID MISMATCH DETECTED!");
            Log.w("AuthDebug", "Resetting class userId from " + this.userId + " to " + prefUserId);
            this.userId = prefUserId;
        }
    }

    /**
     * تحقق من صحة بيانات المستخدم مع ErrorHandler
     *      * @return صحيح إذا كانت البيانات صالحة، خطأ إذا لزم إنهاء النشاط
     */
    private boolean validateUserData() {
        try {
            SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);

            // قراءة مباشرة من preferences
            long storedUserId = userPrefs.getLong("user_id", -1);
            String storedUserRole = userPrefs.getString("user_role", "");

            Log.d("AuthDebug", "=== VALIDATION START ===");
            Log.d("AuthDebug", "From Preferences - ID: " + storedUserId + ", Role: " + storedUserRole);

            // تحقق صارم من البيانات
            if (storedUserId == -1) {
                Log.e("AuthDebug", "VALIDATION FAILED: User ID is -1");
                showAuthErrorAndRedirect("لم يتم العثور على بيانات المستخدم");
                return false;
            }

            if (storedUserRole == null || storedUserRole.isEmpty()) {
                Log.e("AuthDebug", "VALIDATION FAILED: User role is empty or null");
                showAuthErrorAndRedirect("دور المستخدم غير محدد");
                return false;
            }

            if (!"admin".equalsIgnoreCase(storedUserRole)) {
                Log.e("AuthDebug", "VALIDATION FAILED: User is not admin. Role: " + storedUserRole);
                showAuthErrorAndRedirect("ليس لديك صلاحية الدخول كمدير. الدور: " + storedUserRole);
                return false;
            }

            // تعيين القيم للمتغيرات العالمية بعد التحقق الناجح
            this.userId = storedUserId;

            Log.d("AuthDebug", "VALIDATION SUCCESS: User authenticated - ID: " + this.userId + ", Role: " + storedUserRole);
            return true;

        } catch (Exception e) {
            Log.e("AuthDebug", "VALIDATION ERROR: " + e.getMessage(), e);
            showAuthErrorAndRedirect("خطأ في التحقق من الهوية");
            return false;
        }
    }

    private void showAuthErrorAndRedirect(String message) {
        runOnUiThread(() -> {
            // عرض رسالة خطأ واضحة
            new android.app.AlertDialog.Builder(this)
                    .setTitle("خطأ في المصادقة")
                    .setMessage(message)
                    .setPositiveButton("حسناً", (dialog, which) -> redirectToLogin())
                    .setCancelable(false)
                    .show();
        });
    }

    /**
     * تهيئة المستودعات
     */
    private void initComponents() {
        try {
            userRepository = new UserRepository(this);
            preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            Log.d("AdminDebug", "Components initialized successfully");
        } catch (Exception e) {
            errorHandler.handleError("Error initializing components", ErrorHandler.ERROR_TYPE_UI, e);
            // إعادة المحاولة أو التعامل مع الخطأ
            userRepository = new UserRepository(this);
        }
    }

    /**
     * تهيئة عناصر الواجهة
     */
    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbarTop);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        profileImage = findViewById(R.id.profileImage);
        viewPager = findViewById(R.id.viewPager);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    /**
     * دالة لتطبيق المساحات الداخلية (Insets) بشكل برمجي على عناصر الواجهة الرئيسية.
     */
    private void applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout, (v, insets) -> {
            // الحصول على أبعاد شريط الحالة (من الأعلى) وشريط التنقل (من الأسفل)
            int systemBarsTop = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int systemBarsBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

            // 1. تطبيق padding علوي على شريط الأدوات (Toolbar)
            ViewGroup.MarginLayoutParams toolbarParams = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
            toolbarParams.topMargin = systemBarsTop;
            toolbar.setLayoutParams(toolbarParams);

            // 2. تطبيق padding سفلي على شريط التنقل السفلي (BottomNavigationView)
            bottomNavigationView.setPadding(0, 0, 0, systemBarsBottom);

            // 3. تطبيق padding سفلي على ViewPager2 أيضاً
            viewPager.setPadding(0, 0, 0, systemBarsBottom);

            return insets;
        });
    }


    /**
     * إعادة التوجيه إلى واجهة الطالب
     */
    private void redirectToStudentDashboard() {
        Intent intent = new Intent(this, StudentDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * تهيئة البيانات (لون النظام، بيانات المستخدم، ViewPager)
     */
    private void initData() {
        // إعداد لون شريط النظام
        Utils.setSystemBarColor(this, R.color.Custom_BackgroundColor, R.color.Custom_BackgroundColor, 0);

        // تحميل بيانات المستخدم
        loadUserData();

        // إعداد ViewPager
        setupViewPager();

        // تحديث عنوان الشريط
        updateToolbarTitle(0);
    }

    /**
     * إعداد مستمعات الأحداث (الشريط العلوي، التنقل السفلي، القائمة الجانبية)
     */
    private void setupListeners() {
        // إعداد الشريط العلوي
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // إعداد صورة الملف الشخصي
        profileImage.setOnClickListener(v -> openProfile());

        // إعداد التنقل المحسن
        setupEnhancedBottomNavigation();
        setupEnhancedDrawerNavigation();

        // إعداد معالج زر الرجوع
        setupBackHandler();
    }

    /**
     * تحميل بيانات المستخدم
     */
    private void loadUserData() {
        try {
            Log.d("AuthDebug", "=== LOADING USER DATA ===");
            Log.d("AuthDebug", "Using userId: " + userId);

            // تحقق إضافي قبل تحميل البيانات
            if (userId == -1) {
                Log.e("AuthDebug", "CRITICAL: userId is -1 in loadUserData");
                showAuthErrorAndRedirect("معرف المستخدم غير صالح");
                return;
            }

            User user = userRepository.getUserById(userId);
            if (user == null) {
                Log.e("AuthDebug", "CRITICAL: User not found in database. ID: " + userId);
                showAuthErrorAndRedirect("لم يتم العثور على بيانات المستخدم في قاعدة البيانات");
                return;
            }

            Log.d("AuthDebug", "User loaded successfully: " + user.getNameAr());

        // تحقق من أن userRepository ليس null
        if (userRepository == null) {
            errorHandler.logSystemState("AdminDashboard","UserRepository is null - initializing now");
            userRepository = new UserRepository(this);
        }

        user = userRepository.getUserById(userId);
        if (user == null) {
            Log.e("AdminDashboard", "User not found with ID: " + userId);
            redirectToLogin();
            return;
        }

        View header = navigationView.getHeaderView(0);
        ShapeableImageView navImage = header.findViewById(R.id.nav_header_profile_image);
        TextView tvName = header.findViewById(R.id.nav_header_username);
        TextView tvRole = header.findViewById(R.id.nav_header_program);
        MaterialButton appearanceBtn = header.findViewById(R.id.appearanceBtn);

        tvName.setText(isArabicLocale() ? user.getNameAr() : user.getNameEn());
        tvRole.setText(getString(R.string.admin_role)); // عرض دور المدير

        loadProfileImage(navImage, user.getProfilePicture());
        loadProfileImage(profileImage, user.getProfilePicture());

        appearanceBtn.setOnClickListener(v -> toggleDarkMode(appearanceBtn));
        updateAppearanceButton(appearanceBtn, isNightModeEnabled());
        header.setOnClickListener(v -> openProfile());
        } catch (Exception e) {
            Log.e("AuthDebug", "Error in loadUserData: " + e.getMessage(), e);
            showAuthErrorAndRedirect("خطأ في تحميل بيانات المستخدم");
            redirectToLogin();

        }
    }

    /**
     * تحميل الصورة الشخصية - نسخة محسنة تدعم multiple formats
     */
    private void loadProfileImage(ShapeableImageView imageView, String picRef) {
        if (picRef == null || picRef.isEmpty()) {
            imageView.setImageResource(R.drawable.profile);
            return;
        }

        if (picRef.startsWith("@drawable/")) {
            loadDrawableResource(imageView, picRef);
        } else {
            loadImageFromFileName(imageView, picRef);
        }
    }

    /**
     * تحميل من موارد drawable
     */
    private void loadDrawableResource(ShapeableImageView imageView, String drawableRef) {
        String resourceName = drawableRef.substring(10);
        int resId = getResources().getIdentifier(resourceName, "drawable", getPackageName());
        imageView.setImageResource(resId != 0 ? resId : R.drawable.profile);
    }

    /**
     * تحميل الصورة من اسم الملف مع دعم multiple extensions
     */
    private void loadImageFromFileName(ShapeableImageView imageView, String fileName) {
        String fullPath = ImagePathHelper.buildImagePath(this, fileName);

        if (fullPath.isEmpty()) {
            imageView.setImageResource(R.drawable.profile);
            return;
        }

        try {
            File imageFile = new File(fullPath);
            if (imageFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(fullPath);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    imageView.setImageResource(R.drawable.profile);
                }
            } else {
                imageView.setImageResource(R.drawable.profile);
            }
        } catch (Exception e) {
            imageView.setImageResource(R.drawable.profile);
        }
    }

    /**
     * بناء المسار الكامل للصورة
     * @param fileName اسم الملف المخزن في قاعدة البيانات
     * @return المسار الكامل للصورة
     */
    private String buildImagePath(String fileName) {
        // افتراض أن الصور مخزنة في مجلد profile_pictures داخل التخزين الداخلي
        return getFilesDir().getPath() + "/profile_pictures/" + fileName;
    }

    /**
     * تحميل الصورة من التخزين
     */
    private void loadImageFromStorage(ShapeableImageView imageView, String path) {
        try {
            File imageFile = new File(path);
            if (imageFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                imageView.setImageBitmap(bitmap);
            } else {
                // إذا الملف غير موجود، استخدام الصورة الافتراضية
                imageView.setImageResource(R.drawable.profile);
            }
        } catch (Exception e) {
            Log.e("ProfileImage", "Error loading image from storage: " + path, e);
            imageView.setImageResource(R.drawable.profile);
        }
    }

    /**
     * فتح صفحة الملف الشخصي
     */
    private void openProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("is_current_user", true);
        startActivity(intent);
    }

    /**
     * إعداد ViewPager
     */
    private void setupViewPager() {
        try {
            viewPager.setAdapter(new AdminDashboardPagerAdapter(this));
            viewPager.setUserInputEnabled(true);
            Log.d("AdminDebug", "ViewPager setup completed");
        } catch (Exception e) {
            Log.e("AdminDebug", "Error in setupViewPager: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }

    /**
     * إعداد معالج زر الرجوع
     */
    private void setupBackHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (viewPager.getCurrentItem() == 0) {
                    showExitConfirmDialog();
                } else {
                    viewPager.setCurrentItem(0);
                }
            }
        });
    }

    /**
     * تحديث عنوان الشريط
     * @param position موضع الصفحة
     */
    private void updateToolbarTitle(int position) {
        try {
            Log.d("ToolbarTitle", "Updating toolbar title for position: " + position);

            int titleRes;
            switch (position) {
                case 0:
                    titleRes = R.string.admin_dashboard;
                    break;
                case 1:
                    titleRes = R.string.user_management; // ✅ إدارة المستخدمين
                    break;
                case 2:
                    titleRes = R.string.academic_year_management;
                    break;
                case 3:
                    titleRes = R.string.term_management;
                    break;
                case 4:
                    titleRes = R.string.academic_program;
                    break;
                case 5:
                    titleRes = R.string.course_management;
                    break;
                case 6:
                    titleRes = R.string.reports;
                    break;
                default:
                    titleRes = R.string.admin_dashboard;
                    Log.w("ToolbarTitle", "Unknown position: " + position);
            }

            if (toolbarTitle != null) {
                toolbarTitle.setText(getString(titleRes));
                Log.d("ToolbarTitle", "Title updated successfully: " + getString(titleRes));
            }
        } catch (Exception e) {
            Log.e("ToolbarTitle", "Error updating toolbar title: " + e.getMessage());
            if (toolbarTitle != null) {
                toolbarTitle.setText(R.string.admin_dashboard);
            }
        }
    }

    /**
     * إظهار حوار تأكيد الخروج
     */
    private void showExitConfirmDialog() {
        Dialog dialog = new Dialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.item_dialog_confirm, null);
        MaterialCardView card = view.findViewById(R.id.cardDialogReset);
        card.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.Custom_MainColorBlue));
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        tvMessage.setText(R.string.exit_confirmation_message);

        dialog.setContentView(view);
        dialog.setCancelable(false);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        configureDialogWindow(dialog);

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            dialog.dismiss();
            finishAffinity();
        });

        dialog.show();
    }

    /**
     * تهيئة نافذة الحوار
     * @param dialog الحوار المراد تهيئته
     */
    private void configureDialogWindow(Dialog dialog) {
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        params.width = metrics.widthPixels - 2 * (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        params.height = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 170, getResources().getDisplayMetrics());
        params.gravity = Gravity.CENTER;
        dialog.getWindow().setAttributes(params);
    }

    /**
     * التحقق من اللغة العربية
     * @return صحيح إذا كانت اللغة عربية
     */
    private boolean isArabicLocale() {
        String selectedLanguage = preferences.getString("selected_language", "en");
        return "ar".equals(selectedLanguage);
    }

    /**
     * التحقق من تفعيل النمط الداكن
     * @return صحيح إذا كان النمط الداكن مفعلاً
     */
    private boolean isNightModeEnabled() {
        return preferences.getString(PREF_MODE_KEY, MODE_LIGHT).equals(MODE_DARK);
    }

    /**
     * تبديل النمط الداكن/الفاتح
     * @param button زر تبديل النمط
     */
    private void toggleDarkMode(MaterialButton button) {
        boolean isNightMode = isNightModeEnabled();
        preferences.edit().putString(PREF_MODE_KEY, isNightMode ? MODE_LIGHT : MODE_DARK).apply();
        updateAppearanceButton(button, !isNightMode);
        applyFadeAnimationAndRestart();
    }

    /**
     * تحديث زر تبديل النمط
     * @param button زر تبديل النمط
     * @param isNightMode حالة النمط الداكن
     */
    private void updateAppearanceButton(MaterialButton button, boolean isNightMode) {
        button.setIconResource(isNightMode ? R.drawable.darkmode : R.drawable.lightmode);
        button.setIconTint(ContextCompat.getColorStateList(
                this, isNightMode ? R.color.md_theme_onSurface_highContrast : R.color.Custom_MainColorGolden));
    }

    /**
     * تطبيق تأثير التلاشي وإعادة تشغيل النشاط
     */
    private void applyFadeAnimationAndRestart() {
        View root = findViewById(android.R.id.content);
        AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
        fadeOut.setDuration(300);
        fadeOut.setFillAfter(true);
        fadeOut.setAnimationListener(new AlphaAnimation.AnimationListener() {
            @Override
            public void onAnimationStart(android.view.animation.Animation animation) {
            }

            @Override
            public void onAnimationEnd(android.view.animation.Animation animation) {
                restartActivityWithAnimation();
            }

            @Override
            public void onAnimationRepeat(android.view.animation.Animation animation) {
            }
        });
        root.startAnimation(fadeOut);
    }

    /**
     * إعادة تشغيل النشاط مع تأثير
     */
    private void restartActivityWithAnimation() {
        Intent intent = getIntent();
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        startActivity(intent);
    }

    /**
     * إعادة التوجيه إلى صفحة تسجيل الدخول
     */
    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * التحقق من تزامن القائمة الجانبية والتنقل السفلي
     */
    private void checkNavigationSync() {
        int currentPosition = viewPager.getCurrentItem();
        Log.d("NavSync", "Current position: " + currentPosition);

        // تحديث كلا القائمتين
        updateDrawerSelection(currentPosition);

        // تحديث BottomNavigationView
        if (bottomNavigationView != null && bottomNavigationView.getMenu().size() > currentPosition) {
            bottomNavigationView.getMenu().getItem(currentPosition).setChecked(true);
        }

        updateToolbarTitle(currentPosition);
    }

    /**
     * تحديث بيانات المستخدم عند استئناف النشاط
     */
    @Override
    protected void onResume() {
        super.onResume();
        try {
            loadUserData();
            // التحقق من تزامن التنقل عند استئناف النشاط
            checkNavigationSync();
        } catch (Exception e) {
            Log.e("AdminResume", "Error in onResume: " + e.getMessage(), e);
        }
    }

    /**
     * محول ViewPager لإدارة الفراغمنت للمدير
     */
    private static class AdminDashboardPagerAdapter extends FragmentStateAdapter {

        AdminDashboardPagerAdapter(@NonNull AdminDashboardActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            try {
                Log.d("AdminDebug", "Creating fragment for position: " + position);

                switch (position) {
                    case 0:
                        return new Admin_DashboardFragment();
                    case 1:
                        return new Admin_UsersManagementFragment(); // إدارة المستخدمين
                    case 2:
                        return new AcademicYearManagementFragment(); // إدارة السنوات
                    case 3:
                        return new TermManagementFragment(); // إدارة الفصول
                    case 4:
                        return new AcademicProgramManagementFragment(); // إدارة البرامج
                    case 5:
                        // TODO: استبدل بفراغمنت الكورسات عندما يكون جاهزاً
                        return createPlaceholderFragment("إدارة الكورسات - قيد التطوير");
                    case 6:
                        // TODO: استبدل بفراغمنت التقارير عندما يكون جاهزاً
                        return createPlaceholderFragment("التقارير - قيد التطوير");
                    default:
                        throw new IllegalArgumentException("موضع غير صالح: " + position);
                }
            } catch (Exception e) {
                Log.e("AdminDebug", "Error creating fragment: " + e.getMessage(), e);
                // إرجاع فراغمنت فارغ في حالة الخطأ
                return new PlaceholderFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 7; // 7 عناصر لتطابق القائمة الجانبية
        }

        private Fragment createPlaceholderFragment(String message) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putString("message", message);
            fragment.setArguments(args);
            return fragment;
        }
    }

    /**
     * فراغمنت مؤقت للاستخدام أثناء التطوير
     */
    public static class PlaceholderFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_placeholder, container, false);
            TextView textView = view.findViewById(R.id.placeholder_text);
            textView.setText("قيد التطوير - سيتم إضافة المحتوى قريباً");
            return view;
        }
    }

    /**
     * إعداد القائمة الجانبية المحسنة مع إزالة التحديد من القائمة السفلية
     */
    private void setupEnhancedDrawerNavigation() {
        navigationView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);

            int itemId = item.getItemId();
            Log.d("DrawerNav", "Drawer item selected: " + itemId);

            // استخدام postDelayed لتجنب مشاكل التوقيت
            new android.os.Handler().postDelayed(() -> {
                try {
                    boolean isBottomNavItem = false;
                    int targetPosition = -1;

                    if (itemId == R.id.nav_admin_dashboard) {
                        targetPosition = 0;
                        isBottomNavItem = true;
                    } else if (itemId == R.id.nav_admin_user_management) {
                        targetPosition = 1;
                        isBottomNavItem = true;
                    } else if (itemId == R.id.nav_admin_academic_year) {
                        targetPosition = 2;
                        isBottomNavItem = true;
                    } else if (itemId == R.id.nav_admin_term) {
                        targetPosition = 3;
                        isBottomNavItem = true;
                    } else if (itemId == R.id.nav_admin_academic_program) {
                        targetPosition = 4;
                        isBottomNavItem = true;
                    } else if (itemId == R.id.nav_admin_course_management) {
                        targetPosition = 5;
                        isBottomNavItem = false; // ليس في القائمة السفلية
                    } else if (itemId == R.id.nav_admin_reports) {
                        targetPosition = 6;
                        isBottomNavItem = false; // ليس في القائمة السفلية
                    } else if (itemId == R.id.nav_settings) {
                        startActivity(new Intent(this, SettingsActivity.class));
                        return;
                    } else if (itemId == R.id.nav_profile) {
                        openProfile();
                        return;
                    } else if (itemId == R.id.nav_logout) {
                        performLogout();
                        return;
                    }

                    if (targetPosition != -1) {
                        // الانتقال إلى الصفحة المطلوبة
                        viewPager.setCurrentItem(targetPosition, true);

                        // إذا لم يكن العنصر في القائمة السفلية، أزل التحديد منها
                        if (!isBottomNavItem) {
                            clearBottomNavigationSelection();
                            Log.d("DrawerNav", "Bottom navigation selection cleared for non-bottom-nav item");
                        }

                        Log.d("DrawerNav", "Navigated to position: " + targetPosition +
                                ", isBottomNavItem: " + isBottomNavItem);
                    }

                } catch (Exception e) {
                    Log.e("DrawerNav", "Error handling drawer navigation: " + e.getMessage(), e);
                }
            }, 100); // تأخير بسيط لضمان الاستقرار

            return true;
        });
    }

    /**
     * إزالة التحديد من جميع عناصر القائمة السفلية
     */
    private void clearBottomNavigationSelection() {
        try {
            if (bottomNavigationView != null && bottomNavigationView.getMenu() != null) {
                Menu menu = bottomNavigationView.getMenu();
                for (int i = 0; i < menu.size(); i++) {
                    menu.getItem(i).setChecked(false);
                }
                Log.d("BottomNav", "Bottom navigation selection cleared");
            }
        } catch (Exception e) {
            Log.e("BottomNav", "Error clearing bottom navigation selection: " + e.getMessage());
        }
    }

    /**
     * إعادة تعيين التحديد في القائمة السفلية بناءً على الموضع
     */
    private void resetBottomNavigationSelection(int position) {
        try {
            if (bottomNavigationView != null && bottomNavigationView.getMenu() != null) {
                Menu menu = bottomNavigationView.getMenu();

                // إلغاء تحديد جميع العناصر أولاً
                for (int i = 0; i < menu.size(); i++) {
                    menu.getItem(i).setChecked(false);
                }

                // تحديد العنصر الحالي إذا كان ضمن النطاق
                if (position >= 0 && position < menu.size()) {
                    menu.getItem(position).setChecked(true);
                    Log.d("BottomNav", "Bottom navigation selection reset to position: " + position);
                } else {
                    Log.d("BottomNav", "Position " + position + " is not in bottom navigation");
                }
            }
        } catch (Exception e) {
            Log.e("BottomNav", "Error resetting bottom navigation selection: " + e.getMessage());
        }
    }

    /**
     * إعداد التنقل السفلي المحسن
     */
    private void setupEnhancedBottomNavigation() {
        try {
            Log.d("BottomNav", "Setting up enhanced bottom navigation");

            bottomNavigationView.setOnItemSelectedListener(item -> {
                try {
                    int itemId = item.getItemId();
                    Log.d("BottomNav", "Bottom nav item selected: " + itemId);

                    int targetPosition = -1;
                    if (itemId == R.id.fragment_admin_dashboard) {
                        targetPosition = 0;
                    } else if (itemId == R.id.fragment_admin_users) {
                        targetPosition = 1;
                    } else if (itemId == R.id.fragment_admin_reports) {
                        targetPosition = 2;
                    }
                    if (targetPosition != -1) {
                        viewPager.setCurrentItem(targetPosition, true);
                        Log.d("BottomNav", "Navigated to position: " + targetPosition);
                    }

                    return true;
                } catch (Exception e) {
                    Log.e("BottomNav", "Error in bottom nav selection: " + e.getMessage(), e);
                    return false;
                }
            });

            // تحديث القائمة الجانبية عند تغيير الصفحة
            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    try {
                        if (position >= 0 && position < 7) {
                            // تحديث القائمة الجانبية
                            updateDrawerSelection(position);

                            // إعادة تعيين التحديد في القائمة السفلية
                            resetBottomNavigationSelection(position);

                            // تحديث العنوان
                            updateToolbarTitle(position);

                            Log.d("PageChange", "Navigation synchronized for position: " + position);
                        }
                    } catch (Exception e) {
                        Log.e("PageChange", "Error in page change: " + e.getMessage(), e);
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    // يمكن إضافة معالجة إضافية هنا إذا لزم الأمر
                }
            });

            Log.d("BottomNav", "Enhanced bottom navigation setup completed successfully");

        } catch (Exception e) {
            Log.e("BottomNav", "Error setting up enhanced bottom navigation: " + e.getMessage(), e);
        }
    }

    /**
     * تحديث العنصر المحدد في القائمة الجانبية
     */
    private void updateDrawerSelection(int position) {
        try {
            Menu menu = navigationView.getMenu();

            // إلغاء تحديد جميع العناصر أولاً
            for (int i = 0; i < menu.size(); i++) {
                MenuItem menuItem = menu.getItem(i);
                menuItem.setChecked(false);
            }

            // تحديد العنصر الحالي بناءً على الموضع
            int drawerItemId = getDrawerItemIdByPosition(position);
            if (drawerItemId != -1) {
                MenuItem currentItem = menu.findItem(drawerItemId);
                if (currentItem != null) {
                    currentItem.setChecked(true);
                    Log.d("DrawerSelection", "Drawer item selected: " + drawerItemId + " for position: " + position);
                }
            }

        } catch (Exception e) {
            errorHandler.handleError("Error updating drawer selection", ErrorHandler.ERROR_TYPE_UI, e);
        }
    }

    /**
     * الحصول على معرف عنصر القائمة الجانبية بناءً على الموضع
     */
    private int getDrawerItemIdByPosition(int position) {
        switch (position) {
            case 0:
                return R.id.nav_admin_dashboard;
            case 1:
                return R.id.nav_admin_user_management; // إدارة المستخدمين
            case 2:
                return R.id.nav_admin_academic_year;
            case 3:
                return R.id.nav_admin_term;
            case 4:
                return R.id.nav_admin_academic_program;
            case 5:
                return R.id.nav_admin_course_management;
            case 6:
                return R.id.nav_admin_reports;
            default:
                return -1; // لا يوجد عنصر مطابق
        }
    }

    /**
     * تنفيذ تسجيل الخروج
     */
    private void performLogout() {
        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userPrefs.edit().clear().apply();
        SharedPreferences appPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        appPrefs.edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * عرض حوار "قريباً" للخصائص قيد التطوير
     */
    private void showComingSoonDialog(String featureName) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("قيد التطوير")
                .setMessage(featureName + " سيتم إضافتها في التحديثات القادمة")
                .setPositiveButton("حسناً", null)
                .show();
    }
}