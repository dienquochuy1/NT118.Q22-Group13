package com.example.myapplication.data;

import com.google.gson.annotations.SerializedName;

public class PaginationDto {
    @SerializedName("total")
    private int total;

    @SerializedName("per_page")
    private int perPage;

    @SerializedName("current_page")
    private int currentPage;

    @SerializedName("last_page")
    private int lastPage;

    @SerializedName("has_next_page")
    private boolean hasNextPage;

    @SerializedName("next_page")
    private Integer nextPage; // Dùng Integer thay vì int vì có thể mang giá trị null khi hết trang

    // Getters
    public int getTotal() { return total; }
    public int getPerPage() { return perPage; }
    public int getCurrentPage() { return currentPage; }
    public int getLastPage() { return lastPage; }
    public boolean isHasNextPage() { return hasNextPage; }
    public Integer getNextPage() { return nextPage; }

    // Setters
    public void setTotal(int total) { this.total = total; }
    public void setPerPage(int perPage) { this.perPage = perPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
    public void setLastPage(int lastPage) { this.lastPage = lastPage; }
    public void setHasNextPage(boolean hasNextPage) { this.hasNextPage = hasNextPage; }
    public void setNextPage(Integer nextPage) { this.nextPage = nextPage; }
}