package com.safdar.medicento.salesappmedicento.networking.data;

public class Medicine {
    private String mMedicentoName;
    private String mCompanyName;
    private int mPrice;
    private String mId;

    public Medicine (String medicentoName, String companyName, int price, String id) {
        mMedicentoName = medicentoName;
        mCompanyName = companyName;
        mPrice = price;
        mId = id;
    }

    public String getMedicentoName() {
        return mMedicentoName;
    }

    public String getCompanyName() {
        return mCompanyName;
    }

    public int getPrice() {
        return mPrice;
    }

    public String getId() {
        return mId;
    }
}
