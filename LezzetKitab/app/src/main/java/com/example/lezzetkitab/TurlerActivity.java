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
import com.example.lezzetkitab.Model.Kategori;
import com.example.lezzetkitab.Model.Tur;
import com.example.lezzetkitab.ViewHolder.KategoriViewHolder;
import com.example.lezzetkitab.ViewHolder.TurViewHolder;
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

public class TurlerActivity extends AppCompatActivity {

    Button btn_tur_ekle;
    MaterialEditText edtTurAdi;
    FButton btnTurSec,btnTurYukle;


    //resimsecme
    public  static final int PICK_IMAGE_REQUEST=71;
    Uri kaydetmeUrl;

    //firebase tanımı
    FirebaseDatabase database;
    DatabaseReference TurYol;
    FirebaseStorage storage;
    StorageReference resimYolu;
   FirebaseRecyclerAdapter<Tur, TurViewHolder> adapter;
    RecyclerView recyler_tur;
    RecyclerView.LayoutManager layoutManager;

    Tur yeniTur;
    String kategoriId="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turler);
        database = FirebaseDatabase.getInstance();
        TurYol = database.getReference("Tur");
        storage = FirebaseStorage.getInstance();
        resimYolu = storage.getReference();
        recyler_tur=findViewById(R.id.rv_turler);
        recyler_tur.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyler_tur.setLayoutManager(layoutManager);


        btn_tur_ekle=findViewById(R.id.btn_tur_ekle);
        btn_tur_ekle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turEklemePenceresiGoster();
            }
        });

        if(getIntent()!=null){
            kategoriId=getIntent().getStringExtra("KategoriId");
        }
        if(!kategoriId.isEmpty()){
            turleriYukle(kategoriId);
        }
    }

    private void turleriYukle(String kategoriId) {
        Query filtrele=TurYol.orderByChild("kategoriid").equalTo(kategoriId);
        FirebaseRecyclerOptions<Tur> secenekler=new FirebaseRecyclerOptions.Builder<Tur>().setQuery(filtrele,Tur.class).build();
        adapter=new FirebaseRecyclerAdapter<Tur, TurViewHolder>(secenekler) {
            @Override
            protected void onBindViewHolder(@NonNull TurViewHolder turViewHolder, int i, @NonNull Tur tur) {
                turViewHolder.txtTurAdi.setText(tur.getAd());
                Picasso.with(getBaseContext()).load(tur.getResim()).into(turViewHolder.imageView);
               final Tur turtikla=tur;
                turViewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent turler=new Intent(TurlerActivity.this,YemeklerActivity.class) ;
                        turler.putExtra("TurId",adapter.getRef(position).getKey());
                        startActivity(turler);
                    }
                });
            }

            @NonNull
            @Override
            public TurViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView=LayoutInflater.from(parent.getContext()).inflate(R.layout.tur_satiri_ogesi,parent,false);
                return new TurViewHolder(itemView);
            }
        };
        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyler_tur.setAdapter(adapter);


    }


    private void turEklemePenceresiGoster() {
        final  AlertDialog.Builder builder=new AlertDialog.Builder(TurlerActivity.this);

        builder.setTitle("Yeni Tür Ekle");
        builder.setMessage("Lütfen Bilgilerinizi Yazınız...");
        LayoutInflater layoutinflater=this.getLayoutInflater();
        View yeni_tur_ekleme_penceresi=layoutinflater.inflate(R.layout.yeni_tur_ekleme_penceresi,null);
        edtTurAdi=yeni_tur_ekleme_penceresi.findViewById(R.id.edtTurAdi);
        btnTurSec=yeni_tur_ekleme_penceresi.findViewById(R.id.btnTurSec);
        btnTurYukle=yeni_tur_ekleme_penceresi.findViewById(R.id.btnTurYukle);

        btnTurSec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                resimSec();
            }
        });
        btnTurYukle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resimYukle();
            }
        });
        builder.setView(yeni_tur_ekleme_penceresi);
        builder.setIcon(R.drawable.ic_baseline_restaurant_24);

        builder.setPositiveButton("EKLE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(yeniTur!=null){
                    TurYol.push().setValue(yeniTur);
                    Toast.makeText(TurlerActivity.this,yeniTur.getAd()+"Tür Eklendi",Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(TurlerActivity.this,"Resim Yüklendi",Toast.LENGTH_SHORT).show();
                    resimDosyasi.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            yeniTur=new Tur(edtTurAdi.getText().toString(),uri.toString(),kategoriId);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pDialog.dismiss();
                    Toast.makeText(TurlerActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
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
            btnTurSec.setText("Seçildi");
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
            turGuncellemePenceresi(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }
        if(item.getTitle().equals("Sil")){
            turSil(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    private void turSil(String key) {
        TurYol.child(key).removeValue();
    }

    private void turGuncellemePenceresi(String key,Tur item) {
        final  AlertDialog.Builder builder=new AlertDialog.Builder(TurlerActivity.this);

        builder.setTitle("Yeni Tür Ekle");
        builder.setMessage("Lütfen Bilgilerinizi Yazınız...");
        LayoutInflater layoutinflater=this.getLayoutInflater();
        View yeni_tur_ekleme_penceresi=layoutinflater.inflate(R.layout.yeni_tur_ekleme_penceresi,null);
        edtTurAdi=yeni_tur_ekleme_penceresi.findViewById(R.id.edtTurAdi);
        btnTurSec=yeni_tur_ekleme_penceresi.findViewById(R.id.btnTurSec);
        btnTurYukle=yeni_tur_ekleme_penceresi.findViewById(R.id.btnTurYukle);

        btnTurSec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                resimSec();
            }
        });
        btnTurYukle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resimDegis(item);
            }
        });
        builder.setView(yeni_tur_ekleme_penceresi);
        builder.setIcon(R.drawable.ic_baseline_restaurant_24);

        builder.setPositiveButton("GÜNCELLE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                item.setAd(edtTurAdi.getText().toString());
                TurYol.child(key).setValue(item);
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

    private void resimDegis(Tur item) {
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
                    Toast.makeText(TurlerActivity.this,"Resim Güncellendi",Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(TurlerActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
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