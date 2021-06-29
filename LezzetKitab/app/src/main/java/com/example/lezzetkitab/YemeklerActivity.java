package com.example.lezzetkitab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.lezzetkitab.Arayüz.ItemClickListener;
import com.example.lezzetkitab.Model.Tur;
import com.example.lezzetkitab.Model.Yemekler;
import com.example.lezzetkitab.ViewHolder.TurViewHolder;
import com.example.lezzetkitab.ViewHolder.YemekViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import info.hoang8f.widget.FButton;

public class YemeklerActivity extends AppCompatActivity {
Button btn_yemek_ekle;
    MaterialEditText edtYemekAdi;
    MaterialEditText edtMalzemeler;
    MaterialEditText edtYapilisi;
    MaterialEditText edtIzlemelinki;
    MaterialEditText edtPufNokta;



    FButton btnYemekSec,btnYemekYukle;
    Uri kaydetmeUrl;
    FirebaseDatabase database;
    DatabaseReference YemekYol;
    FirebaseStorage storage;
    StorageReference resimYolu;
    FirebaseRecyclerAdapter<Yemekler, YemekViewHolder> adapter;
    RecyclerView recyler_yemek;
    RecyclerView.LayoutManager layoutManager;

    public  static final int PICK_IMAGE_REQUEST=71;
    Yemekler yeniYemek;
    String turId="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yemekler);

        database = FirebaseDatabase.getInstance();
        YemekYol = database.getReference("Yemek");
        storage = FirebaseStorage.getInstance();
        resimYolu = storage.getReference();
        recyler_yemek=findViewById(R.id.rv_yemek);
        recyler_yemek.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
       recyler_yemek.setLayoutManager(layoutManager);


        btn_yemek_ekle=findViewById(R.id.btn_yemek_ekle);
        btn_yemek_ekle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                yemekEklemePenceresiGoster();
            }
        });
        if(getIntent()!=null){
            turId=getIntent().getStringExtra("TurId");
        }
        if(!turId.isEmpty()){
            yemekYukle(turId);
        }
    }


    private void yemekYukle(String turId) {
        Query filtrele = YemekYol.orderByChild("turid").equalTo(turId);
        FirebaseRecyclerOptions<Yemekler> secenekler = new FirebaseRecyclerOptions.Builder<Yemekler>().setQuery(filtrele, Yemekler.class).build();
        adapter = new FirebaseRecyclerAdapter<Yemekler, YemekViewHolder>(secenekler) {
            @NonNull
            @Override
            public YemekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.yemek_satiri_ogesi, parent, false);
                return new YemekViewHolder(itemView);
            }

            @Override
            protected void onBindViewHolder(@NonNull YemekViewHolder yemekViewHolder, int i, @NonNull Yemekler yemekler) {
                yemekViewHolder.txtYemekAdi.setText(yemekler.getYemekadi());
                yemekViewHolder.txtMalzemesi.setText(yemekler.getMalzemeler());
                yemekViewHolder.txtYapisilisi.setText(yemekler.getYapilis());
                yemekViewHolder.txtYemekPufNokta.setText(yemekler.getPufnoktasi());
                yemekViewHolder.txtYemekIzlemeLinki.setText(yemekler.getIzlemelinki());
                Picasso.with(getBaseContext()).load(yemekler.getResim()).into(yemekViewHolder.imageView);
                final Yemekler turtikla = yemekler;
                yemekViewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent turler = new Intent(YemeklerActivity.this, YemeklerActivity.class);
                        turler.putExtra("TurId", adapter.getRef(position).getKey());
                        startActivity(turler);
                    }
                });
            }
        };
        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyler_yemek.setAdapter(adapter);
    }






    private void yemekEklemePenceresiGoster() {
        final  AlertDialog.Builder builder=new AlertDialog.Builder(YemeklerActivity.this);

        builder.setTitle("Yeni Yemek Ekle");
        builder.setMessage("Lütfen Bilgilerinizi Yazınız...");
        LayoutInflater layoutinflater=this.getLayoutInflater();
        View yeni_yemek_ekleme_penceresi=layoutinflater.inflate(R.layout.yeni_yemek_ekleme_penceresi,null);
        edtIzlemelinki=yeni_yemek_ekleme_penceresi.findViewById(R.id.edtIzlemelinki);
        edtMalzemeler=yeni_yemek_ekleme_penceresi.findViewById(R.id.edtMalzeme);
        edtPufNokta=yeni_yemek_ekleme_penceresi.findViewById(R.id.edtPufNokta);
        edtYapilisi=yeni_yemek_ekleme_penceresi.findViewById(R.id.edtYapilis);
        edtYemekAdi=yeni_yemek_ekleme_penceresi.findViewById(R.id.edtYemekAdi);
        btnYemekSec=yeni_yemek_ekleme_penceresi.findViewById(R.id.btnYemekSec);
        btnYemekYukle=yeni_yemek_ekleme_penceresi.findViewById(R.id.btnYemekYukle);

        btnYemekSec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               resimSec();
            }
        });
        btnYemekYukle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               resimYukle();
            }
        });
        builder.setView(yeni_yemek_ekleme_penceresi);
        builder.setIcon(R.drawable.ic_baseline_restaurant_24);

        builder.setPositiveButton("EKLE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(yeniYemek!=null){
                    YemekYol.push().setValue(yeniYemek);
                    Toast.makeText(YemeklerActivity.this,yeniYemek.getYemekadi()+"Yemek Eklendi",Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("VAZGEÇ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();

    }

    private void resimYukle() {
        if(kaydetmeUrl!=null){
            final ProgressDialog pDialog=new ProgressDialog(this);
            pDialog.setMessage("Yükleniyor...");
            pDialog.show();

            String resimAdi= UUID.randomUUID().toString();
            final  StorageReference resimDosyasi=resimYolu.child("resimler/"+resimAdi);
            resimDosyasi.putFile(kaydetmeUrl).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    pDialog.dismiss();
                    Toast.makeText(YemeklerActivity.this,"Resim Yüklendi",Toast.LENGTH_SHORT).show();
                    resimDosyasi.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            yeniYemek=new Yemekler();
                            yeniYemek.setYemekadi(edtYemekAdi.getText().toString());
                            yeniYemek.setMalzemeler(edtMalzemeler.getText().toString());
                            yeniYemek.setYapilis(edtYapilisi.getText().toString());
                            yeniYemek.setPufnoktasi(edtPufNokta.getText().toString());
                            yeniYemek.setIzlemelinki(edtIzlemelinki.getText().toString());
                            yeniYemek.setTurid(turId);yeniYemek.setResim(uri.toString());

                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pDialog.dismiss();
                    Toast.makeText(YemeklerActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                    double progress=(100.0* snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
                    pDialog.setMessage("%" +progress+"yüklendi");
                }
            });


        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICK_IMAGE_REQUEST && resultCode==RESULT_OK && data!=null && data.getData() != null){
            kaydetmeUrl=data.getData();
            btnYemekSec.setText("Seçildi");
        }
    }

    private void resimSec() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Resim Seç"),PICK_IMAGE_REQUEST);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if(item.getTitle().equals("Güncelle")){
           YemekGuncellemePenceresi(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }
        if(item.getTitle().equals("Sil")){
            YemekSil(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    private void YemekSil(String key) {
        YemekYol.child(key).removeValue();
    }

    private void YemekGuncellemePenceresi(String key,Yemekler item) {
        final  AlertDialog.Builder builder=new AlertDialog.Builder(YemeklerActivity.this);

        builder.setTitle("Yeni Yemek Ekle");
        builder.setMessage("Lütfen Bilgilerinizi Yazınız...");
        LayoutInflater layoutinflater=this.getLayoutInflater();
        View yeni_yemek_ekleme_penceresi=layoutinflater.inflate(R.layout.yeni_yemek_ekleme_penceresi,null);
        edtYemekAdi=yeni_yemek_ekleme_penceresi.findViewById(R.id.edtYemekAdi);
        edtMalzemeler=yeni_yemek_ekleme_penceresi.findViewById(R.id.edtMalzeme);
        edtYapilisi=yeni_yemek_ekleme_penceresi.findViewById(R.id.edtYapilis);
        edtPufNokta=yeni_yemek_ekleme_penceresi.findViewById(R.id.edtPufNokta);
        edtIzlemelinki=yeni_yemek_ekleme_penceresi.findViewById(R.id.edtIzlemelinki);
        btnYemekSec=yeni_yemek_ekleme_penceresi.findViewById(R.id.btnYemekSec);
        btnYemekYukle=yeni_yemek_ekleme_penceresi.findViewById(R.id.btnYemekYukle);

        btnYemekSec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                resimSec();
            }
        });
        btnYemekYukle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resimDegis(item);
            }
        });
        builder.setView(yeni_yemek_ekleme_penceresi);
        builder.setIcon(R.drawable.ic_baseline_restaurant_24);

        builder.setPositiveButton("GÜNCELLE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                item.setYemekadi(edtYemekAdi.getText().toString());
                item.setMalzemeler(edtMalzemeler.getText().toString());
                 item.setYapilis(edtYapilisi.getText().toString());
                item.setPufnoktasi(edtPufNokta.getText().toString());
                item.setIzlemelinki(edtIzlemelinki.getText().toString());
                YemekYol.child(key).setValue(item);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("VAZGEÇ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();

    }

    private void resimDegis(Yemekler item) {
        if(kaydetmeUrl!=null){
            final ProgressDialog pDialog=new ProgressDialog(this);
            pDialog.setMessage("Yükleniyor...");
            pDialog.show();

            String resimAdi= UUID.randomUUID().toString();
            final  StorageReference resimDosyasi=resimYolu.child("resimler/"+resimAdi);
            resimDosyasi.putFile(kaydetmeUrl).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    pDialog.dismiss();
                    Toast.makeText(YemeklerActivity.this,"Resim Güncellendi",Toast.LENGTH_SHORT).show();
                    resimDosyasi.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            item.setResim(uri.toString());

                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pDialog.dismiss();
                    Toast.makeText(YemeklerActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                    double progress=(100.0* snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
                    pDialog.setMessage("%" +progress+"yüklendi");
                }
            });


        }
    }
    }

