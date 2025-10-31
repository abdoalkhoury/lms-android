package org.svuonline.lms.data.model;

public class User {
    private long userId;
    private String nameEn;
    private String nameAr;
    private String email;
    private String role;
    private String accountStatus;
    private String phone;
    private String facebookUrl;
    private String whatsappNumber;
    private String telegramHandle;
    private String profilePicture;
    private String bioEn;
    private String bioAr;
    private int programId;
    private String createdAt;
    private String updatedAt;

    // Constructor بدون created_at و updated_at (للتوافق مع الكود القديم)
    public User(long userId, String nameEn, String nameAr, String email, String role,
                String accountStatus, String phone, String facebookUrl, String whatsappNumber,
                String telegramHandle, String profilePicture, String bioEn, String bioAr,
                int programId) {
        this.userId = userId;
        this.nameEn = nameEn;
        this.nameAr = nameAr;
        this.email = email;
        this.role = role;
        this.accountStatus = accountStatus;
        this.phone = phone;
        this.facebookUrl = facebookUrl;
        this.whatsappNumber = whatsappNumber;
        this.telegramHandle = telegramHandle;
        this.profilePicture = profilePicture;
        this.bioEn = bioEn;
        this.bioAr = bioAr;
        this.programId = programId;
        this.createdAt = "";
        this.updatedAt = "";
    }

    // Constructor جديد مع created_at و updated_at
    public User(long userId, String nameEn, String nameAr, String email, String role,
                String accountStatus, String phone, String facebookUrl, String whatsappNumber,
                String telegramHandle, String profilePicture, String bioEn, String bioAr,
                int programId, String createdAt, String updatedAt) {
        this.userId = userId;
        this.nameEn = nameEn;
        this.nameAr = nameAr;
        this.email = email;
        this.role = role;
        this.accountStatus = accountStatus;
        this.phone = phone;
        this.facebookUrl = facebookUrl;
        this.whatsappNumber = whatsappNumber;
        this.telegramHandle = telegramHandle;
        this.profilePicture = profilePicture;
        this.bioEn = bioEn;
        this.bioAr = bioAr;
        this.programId = programId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters لجميع الحقول
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }

    public String getNameAr() { return nameAr; }
    public void setNameAr(String nameAr) { this.nameAr = nameAr; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getFacebookUrl() { return facebookUrl; }
    public void setFacebookUrl(String facebookUrl) { this.facebookUrl = facebookUrl; }

    public String getWhatsappNumber() { return whatsappNumber; }
    public void setWhatsappNumber(String whatsappNumber) { this.whatsappNumber = whatsappNumber; }

    public String getTelegramHandle() { return telegramHandle; }
    public void setTelegramHandle(String telegramHandle) { this.telegramHandle = telegramHandle; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public String getBioEn() { return bioEn; }
    public void setBioEn(String bioEn) { this.bioEn = bioEn; }

    public String getBioAr() { return bioAr; }
    public void setBioAr(String bioAr) { this.bioAr = bioAr; }

    public int getProgramId() { return programId; }
    public void setProgramId(int programId) { this.programId = programId; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}