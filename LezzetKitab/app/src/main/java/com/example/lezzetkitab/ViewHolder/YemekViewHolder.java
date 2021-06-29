package com.example.lezzetkitab.ViewHolder;

import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lezzetkitab.Arayüz.ItemClickListener;
import com.example.lezzetkitab.R;

public class YemekViewHolder  extends RecyclerView.ViewHolder implements
        View.OnClickListener,View.OnCreateContextMenuListener{

    public TextView txtYemekAdi;
    public TextView txtMalzemesi;
    public TextView txtYapisilisi;
    public TextView txtYemekPufNokta;
    public TextView txtYemekIzlemeLinki;
    public ImageView imageView;

    private ItemClickListener itemClickListener;


    public YemekViewHolder(@NonNull View itemView) {
        super(itemView);
        txtYemekAdi=itemView.findViewById(R.id.yemek_adi);
        txtMalzemesi=itemView.findViewById(R.id.yemek_malzemeleri);
        txtYapisilisi=itemView.findViewById(R.id.yemek_yapilisi);
        txtYemekPufNokta=itemView.findViewById(R.id.yemek_pufnoktasi);
        txtYemekIzlemeLinki=itemView.findViewById(R.id.yemek_izlemelinki);
        imageView=itemView.findViewById(R.id.yemek_resmi);
        itemView.setOnClickListener(this);
        itemView.setOnCreateContextMenuListener(this);
    }
    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition(),false);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0,0,getAdapterPosition(),"Güncelle");
        menu.add(0,1,getAdapterPosition(),"Sil");
    }
}
