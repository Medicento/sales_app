package com.safdar.medicento.salesappmedicento.networking.data;

public class SalesPharmacy {
    private String mPharmacyName;
    private String mPharmacyAddress;
    private String mId;
    private String mAreaId;

    public SalesPharmacy (String phamaName, String address, String id, String areaId) {
        mPharmacyName = phamaName;
        mPharmacyAddress = address;
        mId = id;
        mAreaId = areaId;
    }

    public String getPharmacyName() {
        return mPharmacyName;
    }

    public String getPharmacyAddress() {
        return mPharmacyAddress;
    }

    public String getId() {
        return mId;
    }

    public String getAreaId() {
        return mAreaId;
    }
}
