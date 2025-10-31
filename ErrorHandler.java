package org.svuonline.lms.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * نظام تحقق من الأخطاء المركزي يمكن إعادة استخدامه في جميع النشاطات
 * مخصص لتطبيقات نظام إدارة التعلم (LMS)
 */
public class ErrorHandler {

    private static final String TAG = "LMS_ErrorHandler";
    private static ErrorHandler instance;
    private Context context;

    // أنواع الأخطاء الموسعة لتطبيق LMS
    public static final int ERROR_TYPE_CRITICAL = 1;
    public static final int ERROR_TYPE_NETWORK = 2;
    public static final int ERROR_TYPE_VALIDATION = 3;
    public static final int ERROR_TYPE_UI = 4;
    public static final int ERROR_TYPE_DATA = 5;
    public static final int ERROR_TYPE_DATABASE = 6;
    public static final int ERROR_TYPE_AUTHENTICATION = 7;
    public static final int ERROR_TYPE_AUTHORIZATION = 8;
    public static final int ERROR_TYPE_FILE_OPERATION = 9;
    public static final int ERROR_TYPE_API = 10;
    public static final int ERROR_TYPE_MEMORY = 11;
    public static final int ERROR_TYPE_PERMISSION = 12;
    public static final int ERROR_TYPE_BACKEND = 13;
    public static final int ERROR_TYPE_SYNC = 14;
    public static final int ERROR_TYPE_EXPORT = 15;
    public static final int ERROR_TYPE_IMPORT = 16;
    public static final int ERROR_TYPE_PAYMENT = 17;
    public static final int ERROR_TYPE_CONTENT = 18;
    public static final int ERROR_TYPE_SECURITY = 19;

    private ErrorHandler(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized ErrorHandler getInstance(Context context) {
        if (instance == null) {
            instance = new ErrorHandler(context);
        }
        return instance;
    }

    /**
     * معالجة الأخطاء العامة
     */
    public void handleError(String message, int errorType) {
        handleError(message, errorType, null, null);
    }

    public void handleError(String message, int errorType, Exception exception) {
        handleError(message, errorType, exception, null);
    }

    /**
     * معالجة الأخطاء مع الاستثناء وبيانات إضافية
     */
    public void handleError(String message, int errorType, Exception exception, String additionalData) {
        try {
            // تسجيل الخطأ
            logError(message, errorType, exception, additionalData);

            // عرض رسالة للمستخدم (إذا كان النشاط نشطاً)
            showUserFriendlyError(message, errorType, additionalData);

        } catch (Exception e) {
            Log.e(TAG, "Error in error handler: " + e.getMessage());
        }
    }

    /**
     * تسجيل الخطأ بشكل مفصل
     */
    private void logError(String message, int errorType, Exception exception, String additionalData) {
        String errorTypeStr = getErrorTypeString(errorType);

        Log.e(TAG, "=== LMS ERROR REPORT ===");
        Log.e(TAG, "Type: " + errorTypeStr);
        Log.e(TAG, "Message: " + message);

        if (additionalData != null && !additionalData.isEmpty()) {
            Log.e(TAG, "Additional Data: " + additionalData);
        }

        if (exception != null) {
            Log.e(TAG, "Exception: " + exception.getMessage());

            // الحصول على stack trace كامل
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            Log.e(TAG, "Stack Trace: " + sw.toString());
        }

        Log.e(TAG, "Timestamp: " + System.currentTimeMillis());
        Log.e(TAG, "=== END ERROR REPORT ===");
    }

    /**
     * عرض رسالة خطأ للمستخدم
     */
    private void showUserFriendlyError(String message, int errorType, String additionalData) {
        try {
            // إذا لم يكن context نشطاً، لا تعرض الحوار
            if (!(context instanceof Activity)) {
                return;
            }

            Activity activity = (Activity) context;
            if (activity.isFinishing() || activity.isDestroyed()) {
                return;
            }

            activity.runOnUiThread(() -> {
                try {
                    String title = getErrorTitle(errorType);
                    String userMessage = getUserFriendlyMessage(message, errorType, additionalData);

                    new AlertDialog.Builder(activity)
                            .setTitle(title)
                            .setMessage(userMessage)
                            .setPositiveButton("حسناً", null)
                            .setCancelable(true)
                            .show();
                } catch (Exception e) {
                    Log.e(TAG, "Error showing error dialog: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in showUserFriendlyError: " + e.getMessage());
        }
    }

    /**
     * عرض حوار خطأ مع خيارات متقدمة
     */
    public void showAdvancedErrorDialog(Activity activity, String message, int errorType,
                                        String additionalData, DialogInterface.OnClickListener positiveAction) {
        try {
            if (activity.isFinishing() || activity.isDestroyed()) {
                return;
            }

            String title = getErrorTitle(errorType);
            String userMessage = getUserFriendlyMessage(message, errorType, additionalData);

            AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                    .setTitle(title)
                    .setMessage(userMessage)
                    .setCancelable(false);

            if (positiveAction != null) {
                builder.setPositiveButton("إعادة المحاولة", positiveAction)
                        .setNegativeButton("إلغاء", null);
            } else {
                builder.setPositiveButton("حسناً", null);
            }

            builder.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing advanced error dialog: " + e.getMessage());
        }
    }

    /**
     * === دوال التحقق المتخصصة لتطبيق LMS ===
     */

    /**
     * تحقق من اتصال قاعدة البيانات
     */
    public boolean validateDatabaseConnection(Object databaseHelper) {
        try {
            if (databaseHelper == null) {
                handleError("Database connection failed", ERROR_TYPE_DATABASE,
                        null, "Database helper is null");
                return false;
            }

            // TODO: إضافة تحقق فعلي من اتصال قاعدة البيانات
            boolean isConnected = true; // قيمة مؤقتة

            if (!isConnected) {
                handleError("Cannot connect to database", ERROR_TYPE_DATABASE);
                return false;
            }

            return true;
        } catch (Exception e) {
            handleError("Database connection error", ERROR_TYPE_DATABASE, e);
            return false;
        }
    }

    /**
     * تحقق من صلاحيات المستخدم
     */
    public boolean validateUserPermissions(String userRole, String requiredPermission) {
        try {
            if (userRole == null || userRole.isEmpty()) {
                handleError("User role not defined", ERROR_TYPE_AUTHORIZATION);
                return false;
            }

            // TODO: إضافة منطق التحقق من الصلاحيات حسب الدور
            boolean hasPermission = true; // قيمة مؤقتة

            if (!hasPermission) {
                handleError("User does not have required permission", ERROR_TYPE_AUTHORIZATION,
                        null, "Role: " + userRole + ", Required: " + requiredPermission);
                return false;
            }

            return true;
        } catch (Exception e) {
            handleError("Permission validation error", ERROR_TYPE_AUTHORIZATION, e);
            return false;
        }
    }

    /**
     * تحقق من صحة البيانات
     */
    public boolean validateData(Object data, String dataName) {
        try {
            if (data == null) {
                handleError("بيانات " + dataName + " غير متوفرة", ERROR_TYPE_DATA);
                return true;
            }

            // تحقق إضافي حسب نوع البيانات
            if (data instanceof String && ((String) data).isEmpty()) {
                handleError("بيانات " + dataName + " فارغة", ERROR_TYPE_DATA);
                return true;
            }

            if (data instanceof List && ((List<?>) data).isEmpty()) {
                handleError("قائمة " + dataName + " فارغة", ERROR_TYPE_DATA);
                return true;
            }

            return false;
        } catch (Exception e) {
            handleError("Error validating data: " + dataName, ERROR_TYPE_VALIDATION, e);
            return true;
        }
    }

    /**
     * تحقق من عمليات الملفات (رفع/تحميل)
     */
    public boolean validateFileOperation(String filePath, String operationType) {
        try {
            if (filePath == null || filePath.isEmpty()) {
                handleError("File path is empty", ERROR_TYPE_FILE_OPERATION,
                        null, "Operation: " + operationType);
                return false;
            }

            // TODO: إضافة تحقق من وجود الملف وصحته
            boolean isValid = true; // قيمة مؤقتة

            if (!isValid) {
                handleError("File operation failed", ERROR_TYPE_FILE_OPERATION,
                        null, "File: " + filePath + ", Operation: " + operationType);
                return false;
            }

            return true;
        } catch (Exception e) {
            handleError("File operation error", ERROR_TYPE_FILE_OPERATION, e,
                    "File: " + filePath + ", Operation: " + operationType);
            return false;
        }
    }

    /**
     * تحقق من استجابة API
     */
    public boolean validateApiResponse(Object response, String apiEndpoint) {
        try {
            if (response == null) {
                handleError("API response is null", ERROR_TYPE_API,
                        null, "Endpoint: " + apiEndpoint);
                return false;
            }

            // TODO: إضافة تحقق من حالة الاستجابة (success, error, etc.)
            boolean isSuccess = true; // قيمة مؤقتة

            if (!isSuccess) {
                handleError("API request failed", ERROR_TYPE_API,
                        null, "Endpoint: " + apiEndpoint);
                return false;
            }

            return true;
        } catch (Exception e) {
            handleError("API call error", ERROR_TYPE_API, e,
                    "Endpoint: " + apiEndpoint);
            return false;
        }
    }

    /**
     * تحقق من الذاكرة المتاحة
     */
    public boolean validateMemoryAvailability(long requiredMemory) {
        try {
            Runtime runtime = Runtime.getRuntime();
            long availableMemory = runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory());

            if (availableMemory < requiredMemory) {
                handleError("Insufficient memory", ERROR_TYPE_MEMORY,
                        null, "Required: " + requiredMemory + ", Available: " + availableMemory);
                return false;
            }

            return true;
        } catch (Exception e) {
            handleError("Memory check error", ERROR_TYPE_MEMORY, e);
            return false;
        }
    }

    /**
     * تحقق من صلاحية بيانات المستخدم
     */
    public boolean validateUserAuthentication(String username, String userRole) {
        try {
            if (username == null || username.isEmpty()) {
                handleError("Username is empty", ERROR_TYPE_AUTHENTICATION);
                return false;
            }

            if (userRole == null || userRole.isEmpty()) {
                handleError("User role is empty", ERROR_TYPE_AUTHENTICATION);
                return false;
            }

            // TODO: إضافة تحقق من صلاحية الجلسة
            boolean isAuthenticated = true; // قيمة مؤقتة

            if (!isAuthenticated) {
                handleError("User authentication failed", ERROR_TYPE_AUTHENTICATION,
                        null, "Username: " + username + ", Role: " + userRole);
                return false;
            }

            return true;
        } catch (Exception e) {
            handleError("Authentication error", ERROR_TYPE_AUTHENTICATION, e);
            return false;
        }
    }

    /**
     * تحقق من عملية التزامن (Sync)
     */
    public boolean validateSyncOperation(String syncType, Object syncData) {
        try {
            if (syncData == null) {
                handleError("Sync data is null", ERROR_TYPE_SYNC,
                        null, "Sync type: " + syncType);
                return false;
            }

            // TODO: إضافة تحقق من بيانات التزامن
            boolean isValid = true; // قيمة مؤقتة

            if (!isValid) {
                handleError("Sync operation failed", ERROR_TYPE_SYNC,
                        null, "Sync type: " + syncType);
                return false;
            }

            return true;
        } catch (Exception e) {
            handleError("Sync operation error", ERROR_TYPE_SYNC, e,
                    "Sync type: " + syncType);
            return false;
        }
    }

    /**
     * تحقق من عملية التصدير
     */
    public boolean validateExportOperation(String exportType, Object data) {
        try {
            if (data == null) {
                handleError("Export data is null", ERROR_TYPE_EXPORT,
                        null, "Export type: " + exportType);
                return false;
            }

            if (data instanceof List && ((List<?>) data).isEmpty()) {
                handleError("No data to export", ERROR_TYPE_EXPORT,
                        null, "Export type: " + exportType);
                return false;
            }

            return true;
        } catch (Exception e) {
            handleError("Export operation error", ERROR_TYPE_EXPORT, e,
                    "Export type: " + exportType);
            return false;
        }
    }

    /**
     * تحقق من عملية الاستيراد
     */
    public boolean validateImportOperation(String importType, Object importData) {
        try {
            if (importData == null) {
                handleError("Import data is null", ERROR_TYPE_IMPORT,
                        null, "Import type: " + importType);
                return false;
            }

            // TODO: إضافة تحقق من تنسيق البيانات المستوردة
            boolean isValidFormat = true; // قيمة مؤقتة

            if (!isValidFormat) {
                handleError("Invalid import data format", ERROR_TYPE_IMPORT,
                        null, "Import type: " + importType);
                return false;
            }

            return true;
        } catch (Exception e) {
            handleError("Import operation error", ERROR_TYPE_IMPORT, e,
                    "Import type: " + importType);
            return false;
        }
    }

    /**
     * تحقق من أمان البيانات
     */
    public boolean validateDataSecurity(Object sensitiveData) {
        try {
            if (sensitiveData == null) {
                handleError("Sensitive data is null", ERROR_TYPE_SECURITY);
                return false;
            }

            // TODO: إضافة تحقق من تشفير البيانات الحساسة
            boolean isSecure = true; // قيمة مؤقتة

            if (!isSecure) {
                handleError("Data security check failed", ERROR_TYPE_SECURITY);
                return false;
            }

            return true;
        } catch (Exception e) {
            handleError("Data security error", ERROR_TYPE_SECURITY, e);
            return false;
        }
    }

    /**
     * الحصول على وصف نوع الخطأ
     */
    private String getErrorTypeString(int errorType) {
        switch (errorType) {
            case ERROR_TYPE_CRITICAL: return "CRITICAL";
            case ERROR_TYPE_NETWORK: return "NETWORK";
            case ERROR_TYPE_VALIDATION: return "VALIDATION";
            case ERROR_TYPE_UI: return "UI";
            case ERROR_TYPE_DATA: return "DATA";
            case ERROR_TYPE_DATABASE: return "DATABASE";
            case ERROR_TYPE_AUTHENTICATION: return "AUTHENTICATION";
            case ERROR_TYPE_AUTHORIZATION: return "AUTHORIZATION";
            case ERROR_TYPE_FILE_OPERATION: return "FILE_OPERATION";
            case ERROR_TYPE_API: return "API";
            case ERROR_TYPE_MEMORY: return "MEMORY";
            case ERROR_TYPE_PERMISSION: return "PERMISSION";
            case ERROR_TYPE_BACKEND: return "BACKEND";
            case ERROR_TYPE_SYNC: return "SYNC";
            case ERROR_TYPE_EXPORT: return "EXPORT";
            case ERROR_TYPE_IMPORT: return "IMPORT";
            case ERROR_TYPE_PAYMENT: return "PAYMENT";
            case ERROR_TYPE_CONTENT: return "CONTENT";
            case ERROR_TYPE_SECURITY: return "SECURITY";
            default: return "UNKNOWN";
        }
    }

    /**
     * الحصول على عنوان الخطأ
     */
    private String getErrorTitle(int errorType) {
        switch (errorType) {
            case ERROR_TYPE_CRITICAL: return "خطأ حرج";
            case ERROR_TYPE_NETWORK: return "مشكلة في الاتصال";
            case ERROR_TYPE_VALIDATION: return "خطأ في التحقق";
            case ERROR_TYPE_UI: return "مشكلة في الواجهة";
            case ERROR_TYPE_DATA: return "مشكلة في البيانات";
            case ERROR_TYPE_DATABASE: return "خطأ في قاعدة البيانات";
            case ERROR_TYPE_AUTHENTICATION: return "خطأ في المصادقة";
            case ERROR_TYPE_AUTHORIZATION: return "خطأ في الصلاحيات";
            case ERROR_TYPE_FILE_OPERATION: return "خطأ في ملف";
            case ERROR_TYPE_API: return "خطأ في الاتصال بالخادم";
            case ERROR_TYPE_MEMORY: return "مشكلة في الذاكرة";
            case ERROR_TYPE_PERMISSION: return "خطأ في الأذونات";
            case ERROR_TYPE_BACKEND: return "خطأ في الخادم";
            case ERROR_TYPE_SYNC: return "خطأ في المزامنة";
            case ERROR_TYPE_EXPORT: return "خطأ في التصدير";
            case ERROR_TYPE_IMPORT: return "خطأ في الاستيراد";
            case ERROR_TYPE_PAYMENT: return "خطأ في الدفع";
            case ERROR_TYPE_CONTENT: return "خطأ في المحتوى";
            case ERROR_TYPE_SECURITY: return "خطأ أمني";
            default: return "حدث خطأ";
        }
    }

    /**
     * الحصول على رسالة خطأ مخصصة للمستخدم
     */
    private String getUserFriendlyMessage(String technicalMessage, int errorType, String additionalData) {
        String baseMessage;

        switch (errorType) {
            case ERROR_TYPE_CRITICAL:
                baseMessage = "حدث خطأ غير متوقع. يرجى إعادة فتح التطبيق.";
                break;
            case ERROR_TYPE_NETWORK:
                baseMessage = "يرجى التحقق من اتصال الإنترنت والمحاولة مرة أخرى.";
                break;
            case ERROR_TYPE_DATABASE:
                baseMessage = "حدث خطأ في الوصول إلى البيانات. سيتم إعادة المحاولة تلقائياً.";
                break;
            case ERROR_TYPE_AUTHENTICATION:
                baseMessage = "حدث خطأ في تسجيل الدخول. يرجى إعادة المحاولة.";
                break;
            case ERROR_TYPE_AUTHORIZATION:
                baseMessage = "ليس لديك الصلاحيات الكافية للقي بهذه العملية.";
                break;
            case ERROR_TYPE_FILE_OPERATION:
                baseMessage = "حدث خطأ في معالجة الملف. تأكد من صحة الملف وحاول مرة أخرى.";
                break;
            case ERROR_TYPE_API:
                baseMessage = "تعذر الاتصال بالخادم. يرجى المحاولة لاحقاً.";
                break;
            case ERROR_TYPE_MEMORY:
                baseMessage = "الذاكرة غير كافية. يرجى إغلاق بعض التطبيقات والمحاولة مرة أخرى.";
                break;
            case ERROR_TYPE_SYNC:
                baseMessage = "حدث خطأ في مزامنة البيانات. سيتم المحاولة تلقائياً عند توفر الاتصال.";
                break;
            case ERROR_TYPE_EXPORT:
                baseMessage = "تعذر تصدير البيانات. تأكد من وجود مساحة كافية وحاول مرة أخرى.";
                break;
            case ERROR_TYPE_IMPORT:
                baseMessage = "تعذر استيراد البيانات. تأكد من صحة تنسيق الملف.";
                break;
            case ERROR_TYPE_SECURITY:
                baseMessage = "حدث خطأ أمني. يرجى إعادة تسجيل الدخول.";
                break;
            default:
                baseMessage = "حدث خطأ: " + technicalMessage;
        }

        if (additionalData != null && !additionalData.isEmpty()) {
            baseMessage += "\n\nالتفاصيل: " + additionalData;
        }

        return baseMessage;
    }

    /**
     * تسجيل حالة النظام
     */
    public void logSystemState(String component, Object... stateInfo) {
        try {
            Log.d(TAG, "=== LMS SYSTEM STATE: " + component + " ===");
            for (int i = 0; i < stateInfo.length; i += 2) {
                if (i + 1 < stateInfo.length) {
                    Log.d(TAG, stateInfo[i] + ": " + stateInfo[i + 1]);
                }
            }
            Log.d(TAG, "=== END SYSTEM STATE ===");
        } catch (Exception e) {
            Log.e(TAG, "Error logging system state: " + e.getMessage());
        }
    }

    /**
     * تنظيف الموارد
     */
    public void cleanup() {
        context = null;
        instance = null;
    }
}