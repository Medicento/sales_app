package com.safdar.medicento.salesappmedicento.networking.data;

public class SalesPerson {
    private String mName;
    private float mTotalSales;
    private int mReturn;
    private float mEarnings;
    private String mId;
    private String mAllocatedAreaId;

    public SalesPerson(String name, float totalSales, int returns, float earnings, String id, String allocatedAreaId) {
        mName = name;
        mTotalSales = totalSales;
        mReturn = returns;
        mEarnings = earnings;
        mId = id;
        mAllocatedAreaId = allocatedAreaId;
    }

    public String getName() {
        return mName;
    }

    public float getTotalSales() {
        return mTotalSales;
    }

    public int getReturn() {
        return mReturn;
    }

    public float getEarnings() {
        return mEarnings;
    }

    public String getId() {
        return mId;
    }

    public String getAllocatedArea() {
        return mId;
    }
}
