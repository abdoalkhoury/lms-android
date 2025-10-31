package org.svuonline.lms.data.model;

public class CourseSection {
    private long sectionId;
    private long courseId;
    private String titleEn;
    private String titleAr;
    private int displayOrder;

    public CourseSection() {
    }

    public CourseSection(long sectionId, long courseId, String titleEn, String titleAr, int displayOrder) {
        this.sectionId = sectionId;
        this.courseId = courseId;
        this.titleEn = titleEn;
        this.titleAr = titleAr;
        this.displayOrder = displayOrder;
    }

    // Getters and Setters
    public long getSectionId() { return sectionId; }
    public void setSectionId(long sectionId) { this.sectionId = sectionId; }

    public long getCourseId() { return courseId; }
    public void setCourseId(long courseId) { this.courseId = courseId; }

    public String getTitleEn() { return titleEn; }
    public void setTitleEn(String titleEn) { this.titleEn = titleEn; }

    public String getTitleAr() { return titleAr; }
    public void setTitleAr(String titleAr) { this.titleAr = titleAr; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
}