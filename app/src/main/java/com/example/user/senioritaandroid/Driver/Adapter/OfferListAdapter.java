package com.example.user.senioritaandroid.Driver.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.user.senioritaandroid.Driver.Offer;
import com.example.user.senioritaandroid.R;
import com.example.user.senioritaandroid.User.User;

import java.util.List;

public class OfferListAdapter extends ArrayAdapter<Offer> {

    private Context context;
    private int resource;

    public OfferListAdapter(Context context, int resource, List<Offer> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Long  id = getItem(position).getId();
        String pointA = getItem(position).getPointA();
        String pointB = getItem(position).getPointB();
        String price = getItem(position).getPrice();
        User driver = getItem(position).getDriver();
        Offer offer = new Offer(id, pointA, pointB, price, driver);

        LayoutInflater inflater = LayoutInflater.from(this.context);
        convertView = inflater.inflate(this.resource, parent, false);

        TextView priceView = (TextView) convertView.findViewById(R.id.priceMyOffers);
        TextView pointAView = (TextView) convertView.findViewById(R.id.pointAMyOffers);
        TextView pointBView = (TextView) convertView.findViewById(R.id.pointBMyOffers);
        pointAView.setText(pointA);
        pointBView.setText(pointB);
        priceView.setText(price);
        return convertView;
    }
}
