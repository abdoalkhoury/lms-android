package org.svuonline.lms.ui.fragments.admin_fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.svuonline.lms.R;
import org.svuonline.lms.data.repository.DashboardRepository;
import org.svuonline.lms.ui.adapters.AlertAdapter;
import org.svuonline.lms.ui.adapters.QuickActionAdapter;
import org.svuonline.lms.ui.adapters.StatCardAdapter;
import org.svuonline.lms.ui.adapters.ActivityAdapter;
import org.svuonline.lms.ui.data.AlertItem;
import org.svuonline.lms.ui.data.QuickActionItem;
import org.svuonline.lms.ui.data.StatCardItem;
import org.svuonline.lms.ui.data.ActivityItem;
import java.util.ArrayList;
import java.util.List;

/**
 * فراغمنت لوحة تحكم المدير الرئيسية
 */
public class Admin_DashboardFragment extends Fragment {

    private RecyclerView statsRecyclerView;
    private RecyclerView alertsRecyclerView;
    private RecyclerView activityRecyclerView;
    private RecyclerView quickActionsRecyclerView;

    private StatCardAdapter statCardAdapter;
    private AlertAdapter alertAdapter;
    private ActivityAdapter activityAdapter;
    private QuickActionAdapter quickActionAdapter;

    private DashboardRepository dashboardRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initComponents();
        initViews(view);
        loadDashboardData();
    }

    /**
     * تهيئة المستودعات
     */
    private void initComponents() {
        dashboardRepository = new DashboardRepository(requireContext());
    }

    /**
     * تهيئة عناصر الواجهة
     */
    private void initViews(View view) {
        statsRecyclerView = view.findViewById(R.id.statsRecyclerView);
        alertsRecyclerView = view.findViewById(R.id.alertsRecyclerView);
        activityRecyclerView = view.findViewById(R.id.activityRecyclerView);
        quickActionsRecyclerView = view.findViewById(R.id.quickActionsRecyclerView);

        setupRecyclerViews();
    }

    /**
     * إعداد RecyclerViews
     */
    private void setupRecyclerViews() {
        // إحصائيات سريعة - شبكة 2 أعمدة
        statsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        statCardAdapter = new StatCardAdapter(getStatCards());
        statsRecyclerView.setAdapter(statCardAdapter);
        statsRecyclerView.setNestedScrollingEnabled(false);

        // التنبيهات - قائمة عمودية
        alertsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        alertAdapter = new AlertAdapter(getAlerts());
        alertsRecyclerView.setAdapter(alertAdapter);
        alertsRecyclerView.setNestedScrollingEnabled(false);

        // النشاط الأخير - قائمة عمودية
        activityRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        activityAdapter = new ActivityAdapter(getRecentActivities());
        activityRecyclerView.setAdapter(activityAdapter);
        activityRecyclerView.setNestedScrollingEnabled(false);

        // الوصول السريع - شبكة 3 أعمدة
        quickActionsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        quickActionAdapter = new QuickActionAdapter(getQuickActions());
        quickActionsRecyclerView.setAdapter(quickActionAdapter);
        quickActionsRecyclerView.setNestedScrollingEnabled(false);

        // إضافة مستمع النقر للإجراءات السريعة
        quickActionAdapter.setOnQuickActionClickListener(new QuickActionAdapter.OnQuickActionClickListener() {
            @Override
            public void onQuickActionClick(QuickActionItem action) {
                handleQuickAction(action);
            }
        });
    }

    /**
     * تحميل بيانات لوحة التحكم
     */
    private void loadDashboardData() {
        // يمكن جلب البيانات الحقيقية من قاعدة البيانات هنا
        // حالياً نستخدم بيانات تجريبية
        updateStatsFromRepository();
    }

    /**
     * تحديث الإحصائيات من قاعدة البيانات
     */
    private void updateStatsFromRepository() {
        // جلب البيانات الحقيقية من الـ Repository
        int totalUsers = dashboardRepository.getTotalUsers();
        int totalCourses = dashboardRepository.getTotalCourses();
        int activeAssignments = dashboardRepository.getActiveAssignments();
        int pendingRequests = dashboardRepository.getPendingRequests();

        // تحديث البيانات في الـ Adapter
        List<StatCardItem> updatedStats = new ArrayList<>();
        updatedStats.add(new StatCardItem("👥", "إجمالي المستخدمين", String.valueOf(totalUsers), R.color.Custom_MainColorBlue));
        updatedStats.add(new StatCardItem("📚", "إجمالي المقررات", String.valueOf(totalCourses), R.color.Custom_MainColorGreen));
        updatedStats.add(new StatCardItem("📝", "الواجبات النشطة", String.valueOf(activeAssignments), R.color.Custom_MainColorGolden));
        updatedStats.add(new StatCardItem("⏰", "طلبات بانتظار الموافقة", String.valueOf(pendingRequests), R.color.Custom_MainColorPurple));

        statCardAdapter.updateData(updatedStats);
    }

    /**
     * بيانات الإحصائيات التجريبية
     */
    private List<StatCardItem> getStatCards() {
        List<StatCardItem> statCards = new ArrayList<>();
        statCards.add(new StatCardItem("👥", "إجمالي المستخدمين", "1,247", R.color.Custom_MainColorBlue));
        statCards.add(new StatCardItem("📚", "إجمالي المقررات", "85", R.color.Custom_MainColorGreen));
        statCards.add(new StatCardItem("📝", "الواجبات النشطة", "23", R.color.Custom_MainColorGolden));
        statCards.add(new StatCardItem("⏰", "طلبات بانتظار الموافقة", "5", R.color.Custom_MainColorPurple));
        return statCards;
    }

    /**
     * بيانات التنبيهات التجريبية
     */
    private List<AlertItem> getAlerts() {
        List<AlertItem> alerts = new ArrayList<>();
        alerts.add(new AlertItem("⚠️", "3 حسابات غير نشطة منذ شهر", "إدارة المستخدمين", R.color.Custom_MainColorOrange));
        alerts.add(new AlertItem("📅", "5 واجبات متأخرة في التسليم", "مراجعة الواجبات", R.color.Custom_MainColorDarkPink));
        alerts.add(new AlertItem("💾", "مساحة التخزين 85% ممتلئة", "إدارة التخزين", R.color.Custom_MainColorTeal));
        alerts.add(new AlertItem("👥", "5 طلبات تسجيل جديدة", "الموافقة على الطلبات", R.color.Custom_MainColorBlue));
        return alerts;
    }

    /**
     * بيانات النشاط الأخير
     */
    private List<ActivityItem> getRecentActivities() {
        List<ActivityItem> activities = new ArrayList<>();
        activities.add(new ActivityItem("مستخدم جديد", "أحمد محمد - تم التسجيل منذ 2 ساعة", "👤"));
        activities.add(new ActivityItem("مقرر جديد", "الذكاء الاصطناعي - تمت إضافته أمس", "📚"));
        activities.add(new ActivityItem("تسجيل دخول", "مدير النظام - منذ 30 دقيقة", "🔐"));
        activities.add(new ActivityItem("تحديث نظام", "نسخة 2.1 - تم التحديث صباحاً", "⚙️"));
        activities.add(new ActivityItem("نسخ احتياطي", "تم إنشاء نسخة احتياطية - 05:00 ص", "💾"));
        return activities;
    }

    /**
     * بيانات الوصول السريع
     */
    private List<QuickActionItem> getQuickActions() {
        List<QuickActionItem> actions = new ArrayList<>();
        actions.add(new QuickActionItem("👥", "إدارة المستخدمين", R.color.Custom_MainColorBlue));
        actions.add(new QuickActionItem("📚", "إدارة المقررات", R.color.Custom_MainColorGreen));
        actions.add(new QuickActionItem("📊", "التقارير", R.color.Custom_MainColorGolden));
        actions.add(new QuickActionItem("⚙️", "إعدادات النظام", R.color.Custom_MainColorPurple));
        actions.add(new QuickActionItem("👨‍🏫", "المدرسون", R.color.Custom_MainColorTeal));
        actions.add(new QuickActionItem("📧", "الإشعارات", R.color.Custom_MainColorDarkPink));
        return actions;
    }

    /**
     * تحديث البيانات عند استئناف الفراغمنت
     */
    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData();
    }

    /**
     * معالجة النقر على الإجراءات السريعة
     */
    private void handleQuickAction(QuickActionItem action) {
        String actionTitle = action.getTitle();

        switch (actionTitle) {
            case "إدارة المستخدمين":
                // الانتقال إلى إدارة المستخدمين
                navigateToUsersManagement();
                break;
            case "إدارة المقررات":
                showToast("الانتقال إلى إدارة المقررات");
                break;
            case "التقارير":
                showToast("الانتقال إلى التقارير");
                break;
            case "إعدادات النظام":
                showToast("الانتقال إلى إعدادات النظام");
                break;
            case "المدرسون":
                showToast("الانتقال إلى إدارة المدرسين");
                break;
            case "الإشعارات":
                showToast("الانتقال إلى الإشعارات");
                break;
        }
    }

    /**
     * الانتقال إلى إدارة المستخدمين
     */
    private void navigateToUsersManagement() {
        // TODO: تنفيذ الانتقال إلى شاشة إدارة المستخدمين
        showToast("الانتقال إلى إدارة المستخدمين");
    }

    /**
     * عرض رسالة Toast
     */
    private void showToast(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}