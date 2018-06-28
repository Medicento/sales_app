package com.safdar.medicento.salesappmedicento.helperData;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.safdar.medicento.salesappmedicento.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OrderedMedicineAdapter extends ArrayAdapter<OrderedMedicine> {

    public static List<OrderedMedicine> mList;

    public OrderedMedicineAdapter(Context context, int resource, List<OrderedMedicine> objects) {
        super(context, resource, objects);
        mList = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        TextView MedName = null, MedCompany = null, MedQty = null, MedCost = null, incQty, decQty;
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_order_medicine, parent, false);
            MedName = convertView.findViewById(R.id.medicine_name);
            MedCompany = convertView.findViewById(R.id.medicine_company);
            MedQty = convertView.findViewById(R.id.medicine_qty);
            MedCost = convertView.findViewById(R.id.medicine_cost);
            incQty = convertView.findViewById(R.id.inc_qty);
            decQty = convertView.findViewById(R.id.dec_qty);

            viewHolder = new ViewHolder(incQty, decQty, MedQty, mList.get(position));
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        MedName = convertView.findViewById(R.id.medicine_name);
        MedCompany = convertView.findViewById(R.id.medicine_company);
        MedQty = convertView.findViewById(R.id.medicine_qty);
        MedCost = convertView.findViewById(R.id.medicine_cost);
        incQty = convertView.findViewById(R.id.inc_qty);
        decQty = convertView.findViewById(R.id.dec_qty);

        viewHolder.mIncQty.setTag(position);
        viewHolder.mDecQty.setTag(position);
        OrderedMedicine orderedMedicine = getItem(position);

        assert orderedMedicine != null;
        MedName.setText(orderedMedicine.getMedicineName());
        MedCompany.setText(orderedMedicine.getMedicineCompany());
        MedQty.setText(String.valueOf(orderedMedicine.getQty()));
        MedCost.setText(String.valueOf(orderedMedicine.getCost()));

        return convertView;
    }

    private class ViewHolder {
        TextView mIncQty, mDecQty, mQty;
        OrderedMedicine mOrderedMedicine;

        public ViewHolder(TextView incQty, TextView decQty, TextView qty, OrderedMedicine medicine) {
            mIncQty = incQty;
            mDecQty = decQty;
            mQty = qty;
            mIncQty.setOnClickListener(mQtyClickListener);
            mDecQty.setOnClickListener(mQtyClickListener);
            mOrderedMedicine = medicine;
        }

        private View.OnClickListener mQtyClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.inc_qty) {
                    for (OrderedMedicine ordMed : mList) {
                        if (ordMed.getMedicineName().equals(mOrderedMedicine.getMedicineName()) &&
                                ordMed.getMedicineCompany().equals(mOrderedMedicine.getMedicineCompany())) {
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
                } else {
                    Iterator<OrderedMedicine> iterator = mList.iterator();
                    while (iterator.hasNext()) {
                        OrderedMedicine ordMed = iterator.next();
                        if (ordMed.getMedicineName().equals(mOrderedMedicine.getMedicineName()) &&
                                ordMed.getMedicineCompany().equals(mOrderedMedicine.getMedicineCompany())) {
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
                        }
                    }
                }
            }
        };
    }

    @Override
    public void add(@Nullable OrderedMedicine object) {
        for (OrderedMedicine ordMed : mList) {
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