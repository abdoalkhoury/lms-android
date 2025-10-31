package org.svuonline.lms.ui.fragments.admin_fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.svuonline.lms.R;
import org.svuonline.lms.data.model.AcademicProgram;
import org.svuonline.lms.data.model.User;
import org.svuonline.lms.data.repository.AcademicProgramRepository;
import org.svuonline.lms.data.repository.UserRepository;
import org.svuonline.lms.ui.adapters.UserCardAdapter;
import org.svuonline.lms.ui.data.UserCardData;
import org.svuonline.lms.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * فراغمنت لعرض وإدارة المستخدمين مع خيارات التصفية، البحث، وتبديل العرض.
 */
public class Admin_UsersManagementFragment extends Fragment {

    // مفاتيح SharedPreferences
    private static final String PREFS_NAME = "AppPreferences";
    private static final String PREF_VIEW_MODE = "users_view_mode";

    // عناصر واجهة المستخدم
    private RecyclerView recyclerView;
    private TextInputEditText searchBar;
    private TextInputLayout textInputLayout;
    private MaterialButton cardsBtn;
    private MaterialButton listBtn;
    private MaterialButton filterBtn;
    private MaterialButton allUsersBtn;
    private MaterialButton studentsBtn;
    private MaterialButton teachersBtn;
    private MaterialButton adminsBtn;
    private MaterialButton activeUsersBtn;
    private MaterialButton inactiveUsersBtn;
    private MaterialButton suspendedUsersBtn;
    private LinearLayout roleFilterLayout;
    private LinearLayout statusFilterLayout;
    private MaterialButton addUserBtn;

    // المستودعات
    private UserRepository userRepository;

    // بيانات الفراغمنت
    private boolean isListView;
    private String currentRoleFilter = "";
    private String currentStatusFilter = "";
    private String currentSearchQuery = "";
    private List<UserCardData> userCardList;
    private UserCardAdapter adapter;
    private SharedPreferences preferences;

    // ثوابت تخزين صورة المستخدم في قاعدة البيانات
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_READ_STORAGE = 2;

    // متغيرات تخزين صورة المستخدم في قاعدة البيانات
    private String currentProfilePicturePath = "";
    private ImageView currentProfileImageView;
    private Dialog currentUserDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            Log.d("UsersFragment", "Creating view for UsersManagementFragment");
            return inflater.inflate(R.layout.fragment_admin_users_management, container, false);
        } catch (Exception e) {
            Log.e("UsersFragment", "Error in onCreateView: " + e.getMessage(), e);
            e.printStackTrace();

            // عرض واجهة بديلة في حالة الخطأ
            View errorView = inflater.inflate(R.layout.fragment_error, container, false);
            TextView errorText = errorView.findViewById(R.id.error_text);
            errorText.setText("حدث خطأ في تحميل واجهة إدارة المستخدمين: " + e.getMessage());
            return errorView;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // تهيئة المكونات
        initComponents();

        // تصحيح: عرض جميع المستخدمين في قاعدة البيانات
        userRepository.debugAllUsers();

        // تهيئة الواجهة
        initViews(view);

        // تهيئة البيانات
        initData();

        // إعداد مستمعات الأحداث
        setupListeners();

        // تحميل المستخدمين
        loadUsers(currentRoleFilter, currentStatusFilter, currentSearchQuery);
    }

    /**
     * تهيئة المستودعات
     */
    private void initComponents() {
        userRepository = new UserRepository(requireContext());
        preferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * تهيئة عناصر الواجهة
     */
    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        searchBar = view.findViewById(R.id.search_bar);
        textInputLayout = view.findViewById(R.id.outlinedTextField);
        cardsBtn = view.findViewById(R.id.cardsBtn);
        listBtn = view.findViewById(R.id.listBtn);
        filterBtn = view.findViewById(R.id.filterBtn);
        roleFilterLayout = view.findViewById(R.id.roleFilterLayout);
        statusFilterLayout = view.findViewById(R.id.statusFilterLayout);
        addUserBtn = view.findViewById(R.id.addUserBtn);

        // أزرار التصفية حسب الدور
        allUsersBtn = view.findViewById(R.id.allUsersBtn);
        studentsBtn = view.findViewById(R.id.studentsBtn);
        teachersBtn = view.findViewById(R.id.teachersBtn);
        adminsBtn = view.findViewById(R.id.adminsBtn);

        // أزرار التصفية حسب الحالة
        activeUsersBtn = view.findViewById(R.id.activeUsersBtn);
        inactiveUsersBtn = view.findViewById(R.id.inactiveUsersBtn);
        suspendedUsersBtn = view.findViewById(R.id.suspendedUsersBtn);
    }

    /**
     * تهيئة البيانات (العرض، RecyclerView)
     */
    private void initData() {
        isListView = preferences.getString(PREF_VIEW_MODE, "cards").equals("list");
        userCardList = new ArrayList<>();
        adapter = new UserCardAdapter(userCardList, isListView, new UserCardAdapter.OnUserActionListener() {
            @Override
            public void onEditUser(UserCardData user) {
                // فتح حوار تعديل المستخدم
                openEditUserDialog(user);
            }

            @Override
            public void onDeleteUser(UserCardData user) {
                // تنفيذ حذف المستخدم
                showDeleteConfirmationDialog(user);
            }

            @Override
            public void onViewDetails(UserCardData user) {
                // TODO: تنفيذ عرض التفاصيل
                showUserDetailsDialog(user);
            }
        });

        updateRecyclerViewLayout();
        recyclerView.setAdapter(adapter);
        updateButtonStates();
    }

    /**
     * إعداد مستمعات الأحداث
     */
    private void setupListeners() {
        // إعداد أزرار تبديل العرض
        cardsBtn.setOnClickListener(v -> switchToCardsView());
        listBtn.setOnClickListener(v -> switchToListView());

        // إعداد زر الفلتر
        filterBtn.setOnClickListener(v -> toggleFilterButtons());

        // إعداد أزرار التصفية حسب الدور
        MaterialButton[] roleButtons = {allUsersBtn, studentsBtn, teachersBtn, adminsBtn};
        for (MaterialButton button : roleButtons) {
            button.setOnClickListener(v -> applyRoleFilter(button));
        }

        // إعداد أزرار التصفية حسب الحالة
        MaterialButton[] statusButtons = {activeUsersBtn, inactiveUsersBtn, suspendedUsersBtn};
        for (MaterialButton button : statusButtons) {
            button.setOnClickListener(v -> applyStatusFilter(button));
        }

        // إعداد زر الإضافة
        addUserBtn.setOnClickListener(v -> addNewUser());

        // إعداد البحث
        searchBar.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchBar.setOnFocusChangeListener((v, hasFocus) -> {
            textInputLayout.setStartIconTintList(AppCompatResources.getColorStateList(
                    requireContext(), hasFocus ? R.color.md_theme_primary : R.color.Med_Grey));
            textInputLayout.setStartIconDrawable(hasFocus ? R.drawable.searchselect : R.drawable.search);
            if (hasFocus) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> showKeyboard(searchBar), 100);
            }
        });

        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                currentSearchQuery = v.getText().toString().trim();
                loadUsers(currentRoleFilter, currentStatusFilter, currentSearchQuery);
                hideKeyboard(v);
                searchBar.clearFocus();
                return true;
            }
            return false;
        });

        textInputLayout.setEndIconOnClickListener(v -> {
            searchBar.setText("");
            currentSearchQuery = "";
            loadUsers(currentRoleFilter, currentStatusFilter, currentSearchQuery);
        });

        // إخفاء لوحة المفاتيح عند النقر خارج الحقل
        recyclerView.setOnTouchListener((v, event) -> {
            searchBar.clearFocus();
            hideKeyboard(v);
            return false;
        });
    }

    /**
     * تبديل إلى عرض البطاقات
     */
    private void switchToCardsView() {
        if (!isListView) return;
        isListView = false;
        adapter.setListView(false);
        updateRecyclerViewLayout();
        adapter.notifyDataSetChanged();
        updateButtonStates();
        preferences.edit().putString(PREF_VIEW_MODE, "cards").apply();
    }

    /**
     * تبديل إلى عرض القوائم
     */
    private void switchToListView() {
        if (isListView) return;
        isListView = true;
        adapter.setListView(true);
        updateRecyclerViewLayout();
        adapter.notifyDataSetChanged();
        updateButtonStates();
        preferences.edit().putString(PREF_VIEW_MODE, "list").apply();
    }

    /**
     * إظهار/إخفاء أزرار التصفية
     */
    private void toggleFilterButtons() {
        boolean isVisible = roleFilterLayout.getVisibility() == View.VISIBLE;

        if (!isVisible) {
            roleFilterLayout.setVisibility(View.VISIBLE);
            statusFilterLayout.setVisibility(View.VISIBLE);
            filterBtn.setSelected(true);
            filterBtn.setIconTint(AppCompatResources.getColorStateList(requireContext(), R.color.md_theme_primary));
        } else {
            roleFilterLayout.setVisibility(View.GONE);
            statusFilterLayout.setVisibility(View.GONE);
            filterBtn.setSelected(false);
            filterBtn.setIconTint(AppCompatResources.getColorStateList(requireContext(), R.color.Custom_Black));
            currentRoleFilter = "";
            currentStatusFilter = "";
        }
        loadUsers(currentRoleFilter, currentStatusFilter, currentSearchQuery);
    }

    /**
     * تطبيق تصفية الدور
     */
    /**
     * تطبيق تصفية الدور بدعم لأسماء متعددة
     */
    private void applyRoleFilter(MaterialButton button) {
        int selectedColor = ContextCompat.getColor(requireContext(), R.color.md_theme_primary);
        int defaultColor = ContextCompat.getColor(requireContext(), R.color.dark_grey);
        MaterialButton[] roleButtons = {allUsersBtn, studentsBtn, teachersBtn, adminsBtn};

        for (MaterialButton btn : roleButtons) {
            btn.setBackgroundTintList(ColorStateList.valueOf(btn == button ? selectedColor : defaultColor));
        }

        if (button == allUsersBtn) {
            currentRoleFilter = "";
        } else if (button == studentsBtn) {
            currentRoleFilter = "student";
        } else if (button == teachersBtn) {
            // دعم أسماء متعددة للمدرسين
            currentRoleFilter = "teacher,tutor,instructor,professor";
        } else if (button == adminsBtn) {
            currentRoleFilter = "admin,administrator";
        }

        Log.d("UsersFilter", "Applying role filter: " + currentRoleFilter);
        loadUsers(currentRoleFilter, currentStatusFilter, currentSearchQuery);
    }

    /**
     * تطبيق تصفية الحالة
     */
    private void applyStatusFilter(MaterialButton button) {
        int selectedColor = ContextCompat.getColor(requireContext(), R.color.md_theme_primary);
        int defaultColor = ContextCompat.getColor(requireContext(), R.color.dark_grey);
        MaterialButton[] statusButtons = {activeUsersBtn, inactiveUsersBtn, suspendedUsersBtn};

        for (MaterialButton btn : statusButtons) {
            btn.setBackgroundTintList(ColorStateList.valueOf(btn == button ? selectedColor : defaultColor));
        }

        if (button == activeUsersBtn) {
            currentStatusFilter = "active";
        } else if (button == inactiveUsersBtn) {
            currentStatusFilter = "inactive";
        } else if (button == suspendedUsersBtn) {
            currentStatusFilter = "suspended";
        }

        loadUsers(currentRoleFilter, currentStatusFilter, currentSearchQuery);
    }

    /**
     * إضافة مستخدم جديد
     */
    private void addNewUser() {
        // إنشاء الحوار
        currentUserDialog = new Dialog(requireContext());
        currentUserDialog.setContentView(R.layout.dialog_add_user);
        currentUserDialog.setCancelable(true);

        // تهيئة عناصر الحوار
        TextInputEditText etNameEn = currentUserDialog.findViewById(R.id.etNameEn);
        TextInputEditText etNameAr = currentUserDialog.findViewById(R.id.etNameAr);
        TextInputEditText etEmail = currentUserDialog.findViewById(R.id.etEmail);
        TextInputEditText etPassword = currentUserDialog.findViewById(R.id.etPassword);
        TextInputEditText etPhone = currentUserDialog.findViewById(R.id.etPhone);
        TextInputEditText etFacebook = currentUserDialog.findViewById(R.id.etFacebook);
        TextInputEditText etWhatsApp = currentUserDialog.findViewById(R.id.etWhatsApp);
        TextInputEditText etTelegram = currentUserDialog.findViewById(R.id.etTelegram);
        TextInputEditText etBioEn = currentUserDialog.findViewById(R.id.etBioEn);
        TextInputEditText etBioAr = currentUserDialog.findViewById(R.id.etBioAr);
        AutoCompleteTextView actvRole = currentUserDialog.findViewById(R.id.actvRole);
        AutoCompleteTextView actvProgram = currentUserDialog.findViewById(R.id.actvProgram);
        AutoCompleteTextView actvStatus = currentUserDialog.findViewById(R.id.actvStatus);
        MaterialButton btnUploadImage = currentUserDialog.findViewById(R.id.btnUploadImage);
        ImageView ivProfilePreview = currentUserDialog.findViewById(R.id.ivProfilePreview);
        MaterialButton btnCancel = currentUserDialog.findViewById(R.id.btnCancel);
        MaterialButton btnAdd = currentUserDialog.findViewById(R.id.btnAdd);

        // حفظ المرجع للصورة
        currentProfileImageView = ivProfilePreview;
        currentProfilePicturePath = "";

        // إعداد زر رفع الصورة
        btnUploadImage.setOnClickListener(v -> {
            checkStoragePermissionAndPickImage();
        });

        // تعيين النصوص بناءً على اللغة
        if (isArabicLocale()) {
            setupArabicDialog(currentUserDialog, etNameEn, etNameAr, etEmail, etPassword, etPhone,
                    etFacebook, etWhatsApp, etTelegram, etBioEn, etBioAr,
                    actvRole, actvProgram, actvStatus, btnCancel, btnAdd);
        } else {
            setupEnglishDialog(currentUserDialog, etNameEn, etNameAr, etEmail, etPassword, etPhone,
                    etFacebook, etWhatsApp, etTelegram, etBioEn, etBioAr,
                    actvRole, actvProgram, actvStatus, btnCancel, btnAdd);
        }

        // مستمع زر الإلغاء
        btnCancel.setOnClickListener(v -> currentUserDialog.dismiss());

        // مستمع زر الإضافة
        btnAdd.setOnClickListener(v -> {
            if (validateUserInput(etNameEn, etNameAr, etEmail, etPassword, actvRole, actvStatus)) {
                saveNewUser(etNameEn, etNameAr, etEmail, etPassword, etPhone,
                        etFacebook, etWhatsApp, etTelegram, etBioEn, etBioAr,
                        actvRole, actvProgram, actvStatus, currentProfilePicturePath);
                currentUserDialog.dismiss();
            }
        });

        // إظهار الحوار
        showDialog(currentUserDialog);
    }

    /**
     * التحقق من الصلاحيات واختيار الصورة
     */
    private void checkStoragePermissionAndPickImage() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // طلب الصلاحية
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_READ_STORAGE);
        } else {
            // الصلاحية ممنوحة، افتح معرض الصور
            openImagePicker();
        }
    }

    /**
     * فتح معرض الصور
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    /**
     * معالجة نتيجة اختيار الصورة
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            handleSelectedImage(imageUri);
        }
    }

    /**
     * معالجة الصورة المختارة
     */
    private void handleSelectedImage(Uri imageUri) {
        try {
            // عرض الصورة في ImageView
            currentProfileImageView.setImageURI(imageUri);

            // حفظ مسار الصورة أو اسم الملف
            String fileName = "profile_" + System.currentTimeMillis() + ".jpg";
            currentProfilePicturePath = fileName;

            showToast("تم اختيار الصورة بنجاح");

        } catch (Exception e) {
            Log.e("ImagePicker", "Error handling selected image: " + e.getMessage());
            showToast("خطأ في تحميل الصورة");
        }
    }

    /**
     * معالجة نتائج طلب الصلاحيات
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_READ_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // الصلاحية ممنوحة، افتح معرض الصور
                openImagePicker();
            } else {
                showToast("يجب منح صلاحية الوصول إلى الصور لاختيار صورة الملف الشخصي");
            }
        }
    }

    /**
     * إعداد الحوار باللغة العربية
     */
    private void setupArabicDialog(Dialog dialog, TextInputEditText etNameEn, TextInputEditText etNameAr,
                                   TextInputEditText etEmail, TextInputEditText etPassword,
                                   TextInputEditText etPhone, TextInputEditText etFacebook,
                                   TextInputEditText etWhatsApp, TextInputEditText etTelegram,
                                   TextInputEditText etBioEn, TextInputEditText etBioAr,
                                   AutoCompleteTextView actvRole, AutoCompleteTextView actvProgram,
                                   AutoCompleteTextView actvStatus, MaterialButton btnCancel, MaterialButton btnAdd) {

        // تعيين العنوان
        TextView title = dialog.findViewById(R.id.textView);
        if (title != null) {
            title.setText("إضافة مستخدم جديد");
        }

        // إعداد قائمة الأدوار بالعربية
        String[] roles = {"طالب", "مدرس", "مدير", "منسق"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, roles);
        actvRole.setAdapter(roleAdapter);
        actvRole.setThreshold(1);

        // إعداد قائمة البرامج بالعربية من قاعدة البيانات
        setupProgramsDropdown(actvProgram, true);
        actvProgram.setThreshold(1);

        // إعداد قائمة حالات الحساب
        String[] statuses = {"نشط", "غير نشط", "موقوف"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, statuses);
        actvStatus.setAdapter(statusAdapter);
        actvStatus.setThreshold(1);
    }

    /**
     * إعداد الحوار باللغة الإنجليزية
     */
    private void setupEnglishDialog(Dialog dialog, TextInputEditText etNameEn, TextInputEditText etNameAr,
                                    TextInputEditText etEmail, TextInputEditText etPassword,
                                    TextInputEditText etPhone, TextInputEditText etFacebook,
                                    TextInputEditText etWhatsApp, TextInputEditText etTelegram,
                                    TextInputEditText etBioEn, TextInputEditText etBioAr,
                                    AutoCompleteTextView actvRole, AutoCompleteTextView actvProgram,
                                    AutoCompleteTextView actvStatus, MaterialButton btnCancel, MaterialButton btnAdd) {

        // تعيين العنوان
        TextView title = dialog.findViewById(R.id.textView);
        if (title != null) {
            title.setText("Add New User");
        }

        // إعداد قائمة الأدوار بالإنجليزية
        String[] roles = {"Student", "Teacher", "Admin", "Coordinator"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, roles);
        actvRole.setAdapter(roleAdapter);
        actvRole.setThreshold(1);

        // إعداد قائمة البرامج بالإنجليزية من قاعدة البيانات
        setupProgramsDropdown(actvProgram, false);
        actvProgram.setThreshold(1);

        // إعداد قائمة حالات الحساب
        String[] statuses = {"Active", "Inactive", "Suspended"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, statuses);
        actvStatus.setAdapter(statusAdapter);
        actvStatus.setThreshold(1);
    }

    /**
     * إعداد قائمة البرامج من قاعدة البيانات
     */
    private void setupProgramsDropdown(AutoCompleteTextView actvProgram, boolean isArabic) {
        try {
            AcademicProgramRepository programRepo = new AcademicProgramRepository(requireContext());
            List<String> programNames = programRepo.getProgramNames(isArabic);

            // إضافة خيار "اختر برنامج" أو "Select Program" في البداية
            List<String> allPrograms = new ArrayList<>();
            allPrograms.add(isArabic ? "اختر برنامج" : "Select Program");
            allPrograms.addAll(programNames);

            ArrayAdapter<String> programAdapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, allPrograms);
            actvProgram.setAdapter(programAdapter);

        } catch (Exception e) {
            Log.e("ProgramsDropdown", "Error loading programs: " + e.getMessage());

            // استخدام قيم افتراضية في حالة الخطأ
            String[] defaultPrograms = isArabic ?
                    new String[]{"اختر برنامج", "هندسة معلوماتية", "علوم حاسوب"} :
                    new String[]{"Select Program", "Information Technology", "Computer Science"};

            ArrayAdapter<String> defaultAdapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, defaultPrograms);
            actvProgram.setAdapter(defaultAdapter);
        }
    }

    /**
     * التحقق من صحة بيانات المستخدم
     */
    private boolean validateUserInput(TextInputEditText etNameEn, TextInputEditText etNameAr,
                                      TextInputEditText etEmail, TextInputEditText etPassword,
                                      AutoCompleteTextView actvRole, AutoCompleteTextView actvStatus) {

        String nameEn = etNameEn.getText().toString().trim();
        String nameAr = etNameAr.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String role = actvRole.getText().toString().trim();
        String status = actvStatus.getText().toString().trim();

        // التحقق من الحقول المطلوبة
        if (nameEn.isEmpty()) {
            etNameEn.setError("الاسم بالإنجليزية مطلوب");
            return false;
        }

        if (nameAr.isEmpty()) {
            etNameAr.setError("الاسم بالعربية مطلوب");
            return false;
        }

        if (email.isEmpty()) {
            etEmail.setError("البريد الإلكتروني مطلوب");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("صيغة البريد الإلكتروني غير صحيحة");
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("كلمة المرور مطلوبة");
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("كلمة المرور يجب أن تكون 6 أحرف على الأقل");
            return false;
        }

        if (role.isEmpty()) {
            actvRole.setError("الدور مطلوب");
            return false;
        }

        if (status.isEmpty()) {
            actvStatus.setError("حالة الحساب مطلوبة");
            return false;
        }

        return true;
    }

    /**
     * حفظ المستخدم الجديد في قاعدة البيانات
     */
    private void saveNewUser(TextInputEditText etNameEn, TextInputEditText etNameAr,
                             TextInputEditText etEmail, TextInputEditText etPassword,
                             TextInputEditText etPhone, TextInputEditText etFacebook,
                             TextInputEditText etWhatsApp, TextInputEditText etTelegram,
                             TextInputEditText etBioEn, TextInputEditText etBioAr,
                             AutoCompleteTextView actvRole, AutoCompleteTextView actvProgram,
                             AutoCompleteTextView actvStatus, String profilePicturePath) {

        // جمع البيانات من الحقول
        String nameEn = etNameEn.getText().toString().trim();
        String nameAr = etNameAr.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String facebook = etFacebook.getText().toString().trim();
        String whatsapp = etWhatsApp.getText().toString().trim();
        String telegram = etTelegram.getText().toString().trim();
        String bioEn = etBioEn.getText().toString().trim();
        String bioAr = etBioAr.getText().toString().trim();
        String roleText = actvRole.getText().toString().trim();
        String programText = actvProgram.getText().toString().trim();
        String statusText = actvStatus.getText().toString().trim();

        // تحويل القيم للنظام
        String role = convertRoleToEnglish(roleText);
        String status = convertStatusToEnglish(statusText);
        int programId = getProgramIdFromName(programText);

        // إذا لم يتم اختيار صورة، استخدم صورة افتراضية
        String profilePicture = profilePicturePath.isEmpty() ? "default_profile.jpg" : profilePicturePath;

        // الحصول على التاريخ الحالي
        String currentDate = getCurrentDateTime();

        // إنشاء كائن المستخدم مع جميع الحقول
        User newUser = new User(
                0, // ID سيتم تعيينه تلقائياً
                nameEn,
                nameAr,
                email,
                role,
                status,
                phone,
                facebook,
                whatsapp,
                telegram,
                profilePicture,
                bioEn,
                bioAr,
                programId,
                currentDate, // created_at
                currentDate  // updated_at
        );

        // تشفير كلمة المرور
        String passwordHash = Utils.hashPassword(password);

        // حفظ المستخدم في قاعدة البيانات
        boolean success = userRepository.addUser(newUser, passwordHash);

        if (success) {
            showToast("تم إضافة المستخدم بنجاح");
            // تحديث قائمة المستخدمين
            loadUsers(currentRoleFilter, currentStatusFilter, currentSearchQuery);
        } else {
            showToast("فشل في إضافة المستخدم");
        }
    }

    /**
     * الحصول على معرف البرنامج من اسمه (من قاعدة البيانات)
     */
    private int getProgramIdFromName(String programName) {
        if (programName == null || programName.isEmpty() ||
                programName.equals(getString(R.string.program_select)) ||
                programName.equals("Select Program") ||
                programName.equals("اختر برنامج")) {
            return 0; // لا برنامج
        }

        try {
            AcademicProgramRepository programRepo = new AcademicProgramRepository(requireContext());
            List<AcademicProgram> programs = programRepo.getAllPrograms();

            // البحث عن البرنامج بالاسم العربي أو الإنجليزي
            for (AcademicProgram program : programs) {
                if (program.getNameAr().equals(programName) || program.getNameEn().equals(programName)) {
                    return program.getProgramId();
                }
            }

            return 0; // لم يتم العثور على البرنامج

        } catch (Exception e) {
            Log.e("ProgramID", "Error getting program ID: " + e.getMessage());
            return 0;
        }
    }

    /**
     * إظهار الحوار مع الإعدادات الصحيحة
     */
    private void showDialog(Dialog dialog) {
        dialog.show();

        // ضبط حجم الحوار
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(layoutParams);
        }
    }

    /**
     * تحديث تخطيط RecyclerView
     */
    private void updateRecyclerViewLayout() {
        if (isListView) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        } else {
            // استخدام LinearLayoutManager للبطاقات بدلاً من GridLayoutManager
            // لجعل كل بطاقة تأخذ كامل عرض الشاشة
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        recyclerView.setAdapter(adapter);
    }

    /**
     * حساب عدد الأعمدة
     */
    private int calculateNoOfColumns(int columnWidthDp) {
        float screenWidthDp = getResources().getDisplayMetrics().widthPixels / getResources().getDisplayMetrics().density;
        return (int) (screenWidthDp / columnWidthDp + 0.5);
    }

    /**
     * تحديث حالة أزرار تبديل العرض
     */
    private void updateButtonStates() {
        cardsBtn.setSelected(!isListView);
        listBtn.setSelected(isListView);
        listBtn.setIconTint(AppCompatResources.getColorStateList(
                requireContext(), isListView ? R.color.md_theme_primary : R.color.Custom_Black));
        cardsBtn.setIconTint(AppCompatResources.getColorStateList(
                requireContext(), isListView ? R.color.Custom_Black : R.color.md_theme_primary));
    }

    /**
     * التحقق من اللغة العربية
     */
    private boolean isArabicLocale() {
        String selectedLanguage = preferences.getString("selected_language", "en");
        return "ar".equals(selectedLanguage);
    }

    /**
     * تحميل المستخدمين
     */
    private void loadUsers(String roleFilter, String statusFilter, String searchQuery) {
        userCardList.clear();

        // TODO: استبدال هذا بالاستعلام الحقيقي من قاعدة البيانات
        List<User> users = userRepository.getAllUsers(roleFilter, statusFilter, searchQuery);

        for (User user : users) {
            UserCardData cardData = new UserCardData(
                    user.getUserId(),
                    isArabicLocale() ? user.getNameAr() : user.getNameEn(),
                    user.getEmail(),
                    user.getRole(),
                    user.getAccountStatus(),
                    user.getProfilePicture(),
                    user.getProgramId()
            );
            userCardList.add(cardData);
        }

        adapter.notifyDataSetChanged();
    }

    /**
     * إخفاء لوحة المفاتيح
     */
    private void hideKeyboard(View view) {
        if (getActivity() != null) {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    /**
     * إظهار لوحة المفاتيح
     */
    private void showKeyboard(View view) {
        if (getActivity() != null) {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    /**
     * تحديث المستخدمين عند استئناف الفراغمنت
     */
    @Override
    public void onResume() {
        super.onResume();
        loadUsers(currentRoleFilter, currentStatusFilter, currentSearchQuery);
    }

    /**
     * عرض رسالة Toast
     */
    private void showToast(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * تحويل الدور من العربية/الإنجليزية إلى القيمة الإنجليزية في النظام
     */
    private String convertRoleToEnglish(String roleText) {
        if (roleText == null || roleText.isEmpty()) {
            return "student"; // قيمة افتراضية
        }

        // التحقق من النص العربي أولاً
        switch (roleText.trim()) {
            case "طالب":
                return "student";
            case "مدرس":
                return "tutor";
            case "مدير":
                return "admin";
            case "منسق":
                return "coordinator";
        }

        // التحقق من النص الإنجليزي
        switch (roleText.trim().toLowerCase()) {
            case "student":
                return "student";
            case "teacher":
            case "tutor":
            case "instructor":
                return "tutor";
            case "admin":
            case "administrator":
                return "admin";
            case "coordinator":
                return "coordinator";
        }

        // إذا لم يتطابق مع أي شيء، استخدم القيمة الافتراضية
        return "student";
    }

    /**
     * تحويل حالة الحساب من العربية/الإنجليزية إلى القيمة الإنجليزية في النظام
     */
    private String convertStatusToEnglish(String statusText) {
        if (statusText == null || statusText.isEmpty()) {
            return "active"; // قيمة افتراضية
        }

        // التحقق من النص العربي أولاً
        switch (statusText.trim()) {
            case "نشط":
                return "active";
            case "غير نشط":
                return "inactive";
            case "موقوف":
                return "suspended";
        }

        // التحقق من النص الإنجليزي
        switch (statusText.trim().toLowerCase()) {
            case "active":
                return "active";
            case "inactive":
                return "inactive";
            case "suspended":
                return "suspended";
        }

        // إذا لم يتطابق مع أي شيء، استخدم القيمة الافتراضية
        return "active";
    }

    /**
     * الحصول على التاريخ والوقت الحالي بتنسيق SQLite
     */
    private String getCurrentDateTime() {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
            return sdf.format(new java.util.Date());
        } catch (Exception e) {
            Log.e("DateTime", "Error getting current date: " + e.getMessage());
            return "2025-01-01 00:00:00"; // قيمة افتراضية في حالة الخطأ
        }
    }

    /**
     * فتح حوار تعديل المستخدم
     */
    private void openEditUserDialog(UserCardData userCard) {
        // جلب البيانات الكاملة للمستخدم من قاعدة البيانات
        User user = userRepository.getUserById(userCard.getUserId());

        if (user == null) {
            showToast("تعذر تحميل بيانات المستخدم");
            return;
        }

        // إنشاء الحوار
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_edit_user);
        dialog.setCancelable(true);

        // تهيئة عناصر الحوار
        TextInputEditText etNameEn = dialog.findViewById(R.id.etEditNameEn);
        TextInputEditText etNameAr = dialog.findViewById(R.id.etEditNameAr);
        TextInputEditText etEmail = dialog.findViewById(R.id.etEditEmail);
        TextInputEditText etPhone = dialog.findViewById(R.id.etEditPhone);
        TextInputEditText etFacebook = dialog.findViewById(R.id.etEditFacebook);
        TextInputEditText etWhatsApp = dialog.findViewById(R.id.etEditWhatsApp);
        TextInputEditText etTelegram = dialog.findViewById(R.id.etEditTelegram);
        TextInputEditText etBioEn = dialog.findViewById(R.id.etEditBioEn);
        TextInputEditText etBioAr = dialog.findViewById(R.id.etEditBioAr);
        TextInputEditText etNewPassword = dialog.findViewById(R.id.etEditNewPassword);
        AutoCompleteTextView actvRole = dialog.findViewById(R.id.actvEditRole);
        AutoCompleteTextView actvProgram = dialog.findViewById(R.id.actvEditProgram);
        AutoCompleteTextView actvStatus = dialog.findViewById(R.id.actvEditStatus);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnEditCancel);
        MaterialButton btnSave = dialog.findViewById(R.id.btnEditSave);
        TextView tvTitle = dialog.findViewById(R.id.tvEditTitle);

        // تعبئة البيانات الحالية
        fillUserDataInForm(user, etNameEn, etNameAr, etEmail, etPhone, etFacebook,
                etWhatsApp, etTelegram, etBioEn, etBioAr, actvRole, actvProgram, actvStatus);

        // إعداد القوائم المنسدلة
        setupEditDialogDropdowns(actvRole, actvProgram, actvStatus);

        // مستمع زر الإلغاء
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // مستمع زر الحفظ
        btnSave.setOnClickListener(v -> {
            if (validateEditUserInput(etNameEn, etNameAr, etEmail, actvRole, actvStatus)) {
                updateUserInDatabase(user.getUserId(), etNameEn, etNameAr, etEmail, etPhone,
                        etFacebook, etWhatsApp, etTelegram, etBioEn, etBioAr,
                        actvRole, actvProgram, actvStatus, etNewPassword.getText().toString());
                dialog.dismiss();
            }
        });

        // إظهار الحوار
        showDialog(dialog);
    }

    /**
     * تعبئة بيانات المستخدم في النموذج
     */
    private void fillUserDataInForm(User user, TextInputEditText etNameEn, TextInputEditText etNameAr,
                                    TextInputEditText etEmail, TextInputEditText etPhone,
                                    TextInputEditText etFacebook, TextInputEditText etWhatsApp,
                                    TextInputEditText etTelegram, TextInputEditText etBioEn,
                                    TextInputEditText etBioAr, AutoCompleteTextView actvRole,
                                    AutoCompleteTextView actvProgram, AutoCompleteTextView actvStatus) {

        etNameEn.setText(user.getNameEn());
        etNameAr.setText(user.getNameAr());
        etEmail.setText(user.getEmail());
        etPhone.setText(user.getPhone() != null ? user.getPhone() : "");
        etFacebook.setText(user.getFacebookUrl() != null ? user.getFacebookUrl() : "");
        etWhatsApp.setText(user.getWhatsappNumber() != null ? user.getWhatsappNumber() : "");
        etTelegram.setText(user.getTelegramHandle() != null ? user.getTelegramHandle() : "");
        etBioEn.setText(user.getBioEn() != null ? user.getBioEn() : "");
        etBioAr.setText(user.getBioAr() != null ? user.getBioAr() : "");

        // تعيين الدور
        String roleDisplay = convertRoleToDisplay(user.getRole(), isArabicLocale());
        actvRole.setText(roleDisplay, false);

        // تعيين حالة الحساب
        String statusDisplay = convertStatusToDisplay(user.getAccountStatus(), isArabicLocale());
        actvStatus.setText(statusDisplay, false);

        // تعيين البرنامج إذا كان موجوداً
        if (user.getProgramId() > 0) {
            String programName = getProgramNameById(user.getProgramId(), isArabicLocale());
            actvProgram.setText(programName, false);
        }
    }

    /**
     * إعداد القوائم المنسدلة في حوار التعديل
     */
    private void setupEditDialogDropdowns(AutoCompleteTextView actvRole,
                                          AutoCompleteTextView actvProgram,
                                          AutoCompleteTextView actvStatus) {

        // إعداد قائمة الأدوار
        String[] roles = isArabicLocale() ?
                new String[]{"طالب", "مدرس", "مدير", "منسق"} :
                new String[]{"Student", "Teacher", "Admin", "Coordinator"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, roles);
        actvRole.setAdapter(roleAdapter);
        actvRole.setThreshold(1);

        // إعداد قائمة البرامج من قاعدة البيانات
        setupProgramsDropdown(actvProgram, isArabicLocale());
        actvProgram.setThreshold(1);

        // إعداد قائمة حالات الحساب
        String[] statuses = isArabicLocale() ?
                new String[]{"نشط", "غير نشط", "موقوف"} :
                new String[]{"Active", "Inactive", "Suspended"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, statuses);
        actvStatus.setAdapter(statusAdapter);
        actvStatus.setThreshold(1);
    }

    /**
     * التحقق من صحة بيانات التعديل
     */
    private boolean validateEditUserInput(TextInputEditText etNameEn, TextInputEditText etNameAr,
                                          TextInputEditText etEmail, AutoCompleteTextView actvRole,
                                          AutoCompleteTextView actvStatus) {

        String nameEn = etNameEn.getText().toString().trim();
        String nameAr = etNameAr.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String role = actvRole.getText().toString().trim();
        String status = actvStatus.getText().toString().trim();

        if (nameEn.isEmpty()) {
            etNameEn.setError("الاسم بالإنجليزية مطلوب");
            return false;
        }

        if (nameAr.isEmpty()) {
            etNameAr.setError("الاسم بالعربية مطلوب");
            return false;
        }

        if (email.isEmpty()) {
            etEmail.setError("البريد الإلكتروني مطلوب");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("صيغة البريد الإلكتروني غير صحيحة");
            return false;
        }

        if (role.isEmpty()) {
            actvRole.setError("الدور مطلوب");
            return false;
        }

        if (status.isEmpty()) {
            actvStatus.setError("حالة الحساب مطلوبة");
            return false;
        }

        return true;
    }

    /**
     * تحديث بيانات المستخدم في قاعدة البيانات
     */
    /**
     * تحديث بيانات المستخدم في قاعدة البيانات
     */
    private void updateUserInDatabase(long userId, TextInputEditText etNameEn, TextInputEditText etNameAr,
                                      TextInputEditText etEmail, TextInputEditText etPhone,
                                      TextInputEditText etFacebook, TextInputEditText etWhatsApp,
                                      TextInputEditText etTelegram, TextInputEditText etBioEn,
                                      TextInputEditText etBioAr, AutoCompleteTextView actvRole,
                                      AutoCompleteTextView actvProgram, AutoCompleteTextView actvStatus,
                                      String newPassword) {

        // جمع البيانات من الحقول
        String nameEn = etNameEn.getText().toString().trim();
        String nameAr = etNameAr.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String facebook = etFacebook.getText().toString().trim();
        String whatsapp = etWhatsApp.getText().toString().trim();
        String telegram = etTelegram.getText().toString().trim();
        String bioEn = etBioEn.getText().toString().trim();
        String bioAr = etBioAr.getText().toString().trim();
        String roleText = actvRole.getText().toString().trim();
        String programText = actvProgram.getText().toString().trim();
        String statusText = actvStatus.getText().toString().trim();

        // تحويل القيم للنظام
        String role = convertRoleToEnglish(roleText);
        String status = convertStatusToEnglish(statusText);
        int programId = getProgramIdFromName(programText);

        // إنشاء كائن المستخدم المحدث
        User updatedUser = new User(
                userId,
                nameEn,
                nameAr,
                email,
                role,
                status,
                phone,
                facebook,
                whatsapp,
                telegram,
                "", // profile picture - يمكن إضافته لاحقاً
                bioEn,
                bioAr,
                programId
        );

        // تحديث بيانات المستخدم
        boolean success = userRepository.updateUser(updatedUser);

        // تحديث كلمة المرور إذا تم إدخالها
        if (success && !newPassword.isEmpty()) {
            if (newPassword.length() >= 6) {
                String passwordHash = Utils.hashPassword(newPassword);
                userRepository.updateUserPassword(userId, passwordHash);
            } else {
                showToast("كلمة المرور يجب أن تكون 6 أحرف على الأقل");
            }
        }

        if (success) {
            showToast("تم تحديث بيانات المستخدم بنجاح");
            // تحديث القائمة
            loadUsers(currentRoleFilter, currentStatusFilter, currentSearchQuery);
        } else {
            showToast("فشل في تحديث بيانات المستخدم");
        }
    }

    /**
     * تحويل الدور للنظام الإنجليزي
     */
    private String convertRoleToDisplay(String role, boolean isArabic) {
        switch (role.toLowerCase()) {
            case "student":
                return isArabic ? "طالب" : "Student";
            case "tutor":
            case "teacher":
                return isArabic ? "مدرس" : "Teacher";
            case "admin":
                return isArabic ? "مدير" : "Admin";
            case "coordinator":
                return isArabic ? "منسق" : "Coordinator";
            default:
                return isArabic ? "طالب" : "Student";
        }
    }

    /**
     * تحويل حالة الحساب للعرض
     */
    private String convertStatusToDisplay(String status, boolean isArabic) {
        switch (status.toLowerCase()) {
            case "active":
                return isArabic ? "نشط" : "Active";
            case "inactive":
                return isArabic ? "غير نشط" : "Inactive";
            case "suspended":
                return isArabic ? "موقوف" : "Suspended";
            default:
                return isArabic ? "نشط" : "Active";
        }
    }

    /**
     * عرض تفاصيل المستخدم باستخدام التصميم المخصص
     */
    private void showUserDetailsDialog(UserCardData userCard) {
        // جلب البيانات الكاملة للمستخدم
        User user = userRepository.getUserById(userCard.getUserId());

        if (user == null) {
            showToast("تعذر تحميل بيانات المستخدم");
            return;
        }

        // إنشاء حوار التفاصيل
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_user_details);
        dialog.setCancelable(true);

        // تهيئة عناصر الحوار
        TextView tvNameEn = dialog.findViewById(R.id.tvDetailNameEn);
        TextView tvNameAr = dialog.findViewById(R.id.tvDetailNameAr);
        TextView tvEmail = dialog.findViewById(R.id.tvDetailEmail);
        TextView tvRole = dialog.findViewById(R.id.tvDetailRole);
        TextView tvStatus = dialog.findViewById(R.id.tvDetailStatus);
        TextView tvPhone = dialog.findViewById(R.id.tvDetailPhone);
        TextView tvProgram = dialog.findViewById(R.id.tvDetailProgram);
        TextView tvFacebook = dialog.findViewById(R.id.tvDetailFacebook);
        TextView tvWhatsApp = dialog.findViewById(R.id.tvDetailWhatsApp);
        TextView tvTelegram = dialog.findViewById(R.id.tvDetailTelegram);
        TextView tvBioEn = dialog.findViewById(R.id.tvDetailBioEn);
        TextView tvBioAr = dialog.findViewById(R.id.tvDetailBioAr);
        TextView tvCreatedAt = dialog.findViewById(R.id.tvDetailCreatedAt);
        MaterialButton btnClose = dialog.findViewById(R.id.btnDetailClose);

        // تعبئة البيانات
        tvNameEn.setText(user.getNameEn());
        tvNameAr.setText(user.getNameAr());
        tvEmail.setText(user.getEmail());
        tvRole.setText(convertRoleToDisplay(user.getRole(), isArabicLocale()));
        tvStatus.setText(convertStatusToDisplay(user.getAccountStatus(), isArabicLocale()));
        tvPhone.setText(user.getPhone() != null && !user.getPhone().isEmpty() ? user.getPhone() : "غير محدد");
        tvProgram.setText(user.getProgramId() > 0 ? getProgramNameById(user.getProgramId(), isArabicLocale()) : "غير محدد");
        tvFacebook.setText(user.getFacebookUrl() != null && !user.getFacebookUrl().isEmpty() ? user.getFacebookUrl() : "غير محدد");
        tvWhatsApp.setText(user.getWhatsappNumber() != null && !user.getWhatsappNumber().isEmpty() ? user.getWhatsappNumber() : "غير محدد");
        tvTelegram.setText(user.getTelegramHandle() != null && !user.getTelegramHandle().isEmpty() ? user.getTelegramHandle() : "غير محدد");
        tvBioEn.setText(user.getBioEn() != null && !user.getBioEn().isEmpty() ? user.getBioEn() : "غير محدد");
        tvBioAr.setText(user.getBioAr() != null && !user.getBioAr().isEmpty() ? user.getBioAr() : "غير محدد");
        tvCreatedAt.setText(user.getCreatedAt() != null ? user.getCreatedAt() : "غير محدد");

        // مستمع زر الإغلاق
        btnClose.setOnClickListener(v -> dialog.dismiss());

        // إظهار الحوار
        showDialog(dialog);
    }

    /**
     * الحصول على اسم البرنامج بواسطة ID
     */
    private String getProgramNameById(int programId, boolean isArabic) {
        if (programId <= 0) {
            return isArabic ? "غير محدد" : "Not specified";
        }

        try {
            AcademicProgramRepository programRepo = new AcademicProgramRepository(requireContext());
            return programRepo.getProgramNameById(programId, isArabic);
        } catch (Exception e) {
            Log.e("ProgramName", "Error getting program name: " + e.getMessage());
            return isArabic ? "غير محدد" : "Not specified";
        }
    }

    /**
     * عرض حوار تأكيد حذف المستخدم
     */
    private void showDeleteConfirmationDialog(UserCardData user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        if (isArabicLocale()) {
            builder.setTitle("تأكيد الحذف")
                    .setMessage("هل أنت متأكد من أنك تريد حذف المستخدم '" + user.getName() + "'؟\n\nهذا الإجراء لا يمكن التراجع عنه.")
                    .setPositiveButton("حذف", (dialog, which) -> {
                        deleteUser(user);
                    })
                    .setNegativeButton("إلغاء", (dialog, which) -> {
                        dialog.dismiss();
                    });
        } else {
            builder.setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete user '" + user.getName() + "'?\n\nThis action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        deleteUser(user);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    });
        }

        AlertDialog dialog = builder.create();

        // تخصيص ألوان الأزرار
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_error));
            negativeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_primary));
        });

        dialog.show();
    }

    /**
     * حذف المستخدم
     */
    private void deleteUser(UserCardData user) {
        try {
            // التحقق مما إذا كان يمكن حذف المستخدم
            if (!userRepository.canDeleteUser(user.getUserId())) {
                showCannotDeleteDialog(user);
                return;
            }

            // تنفيذ الحذف
            boolean success = userRepository.deleteUser(user.getUserId());

            if (success) {
                showToast(isArabicLocale() ? "تم حذف المستخدم بنجاح" : "User deleted successfully");
                // تحديث القائمة
                loadUsers(currentRoleFilter, currentStatusFilter, currentSearchQuery);
            } else {
                showToast(isArabicLocale() ? "فشل في حذف المستخدم" : "Failed to delete user");
            }

        } catch (Exception e) {
            Log.e("DeleteUser", "Error deleting user: " + e.getMessage());
            showToast(isArabicLocale() ? "حدث خطأ أثناء حذف المستخدم" : "Error occurred while deleting user");
        }
    }

    /**
     * عرض رسالة عدم إمكانية الحذف مع خيار التعطيل
     */
    private void showCannotDeleteDialog(UserCardData user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        if (isArabicLocale()) {
            builder.setTitle("لا يمكن الحذف")
                    .setMessage("لا يمكن حذف المستخدم '" + user.getName() + "' لأنه مرتبط بمقررات دراسية.\n\nماذا تريد أن تفعل؟")
                    .setPositiveButton("تعطيل الحساب", (dialog, which) -> {
                        disableUserAccount(user);
                    })
                    .setNegativeButton("إلغاء", (dialog, which) -> {
                        dialog.dismiss();
                    });
        } else {
            builder.setTitle("Cannot Delete")
                    .setMessage("Cannot delete user '" + user.getName() + "' because they are associated with courses.\n\nWhat would you like to do?")
                    .setPositiveButton("Disable Account", (dialog, which) -> {
                        disableUserAccount(user);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    });
        }

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * تعطيل حساب المستخدم بدلاً من الحذف
     */
    private void disableUserAccount(UserCardData user) {
        try {
            // جلب بيانات المستخدم الحالية
            User currentUser = userRepository.getUserById(user.getUserId());
            if (currentUser == null) {
                showToast("تعذر تحميل بيانات المستخدم");
                return;
            }

            // تحديث حالة الحساب إلى غير نشط
            currentUser.setAccountStatus("inactive");
            boolean success = userRepository.updateUser(currentUser);

            if (success) {
                showToast(isArabicLocale() ? "تم تعطيل حساب المستخدم بنجاح" : "User account disabled successfully");
                // تحديث القائمة
                loadUsers(currentRoleFilter, currentStatusFilter, currentSearchQuery);
            } else {
                showToast(isArabicLocale() ? "فشل في تعطيل حساب المستخدم" : "Failed to disable user account");
            }

        } catch (Exception e) {
            Log.e("DisableUser", "Error disabling user: " + e.getMessage());
            showToast(isArabicLocale() ? "حدث خطأ أثناء تعطيل الحساب" : "Error occurred while disabling account");
        }
    }
}