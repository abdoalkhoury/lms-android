package org.svuonline.lms.data.model;

public class SectionTool {
    private long toolId;
    private long sectionId;
    private String nameEn;
    private String nameAr;
    private String actionType; // file action, Assignment action, Participants action

    public SectionTool() {
    }

    public SectionTool(long toolId, long sectionId, String nameEn, String nameAr, String actionType) {
        this.toolId = toolId;
        this.sectionId = sectionId;
        this.nameEn = nameEn;
        this.nameAr = nameAr;
        this.actionType = actionType;
    }

    // Getters and Setters
    public long getToolId() { return toolId; }
    public void setToolId(long toolId) { this.toolId = toolId; }

    public long getSectionId() { return sectionId; }
    public void setSectionId(long sectionId) { this.sectionId = sectionId; }

    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }

    public String getNameAr() { return nameAr; }
    public void setNameAr(String nameAr) { this.nameAr = nameAr; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
}