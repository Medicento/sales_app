package com.safdar.medicento.salesappmedicento;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Iterator;
import java.util.List;

public class OrderedMedicineAdapter extends ArrayAdapter<OrderedMedicine> {

    List<OrderedMedicine> mList;
    public OrderedMedicineAdapter(Context context, int resource, List<OrderedMedicine> objects){
        super(context, resource, objects);
        mList = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null){
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_ordered_medicine, parent, false);
        }

        TextView MedName = convertView.findViewById(R.id.medicine_name);
        TextView MedCompany = convertView.findViewById(R.id.medicine_company);
        TextView MedQty = convertView.findViewById(R.id.medicine_qty);
        TextView MedCost = convertView.findViewById(R.id.medicine_cost);

        OrderedMedicine orderedMedicine = getItem(position);

        assert orderedMedicine != null;
        MedName.setText(orderedMedicine.getMedicineName());
        MedCompany.setText(orderedMedicine.getMedicineCompany());
        MedQty.setText(String.valueOf(orderedMedicine.getQty()));
        MedCost.setText(String.valueOf(orderedMedicine.getCost()));

        return convertView;
    }

    @Override
    public void add(@Nullable OrderedMedicine object) {
        for (OrderedMedicine ordMed: mList) {
            if (ordMed.getMedicineName().equals(object.getMedicineName()) &&
                    ordMed.getMedicineCompany().equals(object.getMedicineCompany())) {
                int qty = ordMed.getQty();
                qty++;
                ordMed.setQty(qty);
                float rate = ordMed.getRate();
                ordMed.setCost(qty * rate);
                int pos = mList.indexOf(ordMed);
                mList.set(pos, ordMed);
                notifyDataSetChanged();
                return;
            }
        }
        super.add(object);
    }

    public int sub(OrderedMedicine object) {
        Iterator<OrderedMedicine> iterator = mList.iterator();
        while (iterator.hasNext()) {
            OrderedMedicine ordMed = iterator.next();
            if (ordMed.getMedicineName().equals(object.getMedicineName()) &&
                    ordMed.getMedicineCompany().equals(object.getMedicineCompany())) {
                int qty = ordMed.getQty();
                qty--;
                ordMed.setQty(qty);
                float rate = ordMed.getRate();
                ordMed.setCost(qty * rate);
                if (qty == 0) {
                    iterator.remove();
                } else {
                    ordMed.setQty(qty);
                    int pos = mList.indexOf(ordMed);
                    mList.set(pos, ordMed);
                }
                notifyDataSetChanged();
                return qty;
            }
        }
        return 0;

    }
}