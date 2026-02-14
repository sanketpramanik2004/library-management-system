package com.library.library_management.dto;

public class DashboardStatsDTO {

    private long totalBooks;
    private long issuedBooks;
    private long returnedBooks;
    private double totalFine;

    // getters
    public long getTotalBooks() {
        return totalBooks;
    }

    public long getIssuedBooks() {
        return issuedBooks;
    }

    public long getReturnedBooks() {
        return returnedBooks;
    }

    public double getTotalFine() {
        return totalFine;
    }

    // setters
    public void setTotalBooks(long totalBooks) {
        this.totalBooks = totalBooks;
    }

    public void setIssuedBooks(long issuedBooks) {
        this.issuedBooks = issuedBooks;
    }

    public void setReturnedBooks(long returnedBooks) {
        this.returnedBooks = returnedBooks;
    }

    public void setTotalFine(double totalFine) {
        this.totalFine = totalFine;
    }
}
