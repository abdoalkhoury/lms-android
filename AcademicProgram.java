package org.svuonline.lms.data.model;

public class AcademicProgram {
    private int programId;
    private String code;
    private String nameEn;
    private String nameAr;
    private int programDuration;

    // إضافة constructor فارغ
    public AcademicProgram() {
    }

    public AcademicProgram(int programId, String code, String nameEn, String nameAr, int programDuration) {
        this.programId = programId;
        this.code = code;
        this.nameEn = nameEn;
        this.nameAr = nameAr;
        this.programDuration = programDuration;
    }

    // Getters and Setters
    public int getProgramId() { return programId; }
    public void setProgramId(int programId) { this.programId = programId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }

    public String getNameAr() { return nameAr; }
    public void setNameAr(String nameAr) { this.nameAr = nameAr; }

    public int getProgramDuration() { return programDuration; }
    public void setProgramDuration(int programDuration) { this.programDuration = programDuration; }
}