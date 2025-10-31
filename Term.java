package org.svuonline.lms.data.model;

public class Term {
    private long termId;
    private long academicYearId;
    private String name;
    private String startDate;
    private String endDate;

    public Term() {
    }

    public Term(long termId, long academicYearId, String name, String startDate, String endDate) {
        this.termId = termId;
        this.academicYearId = academicYearId;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
    public long getTermId() { return termId; }
    public void setTermId(long termId) { this.termId = termId; }

    public long getAcademicYearId() { return academicYearId; }
    public void setAcademicYearId(long academicYearId) { this.academicYearId = academicYearId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}