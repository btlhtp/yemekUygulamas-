package com.example.lezzetkitab.Model;

public class Tur {
    private String ad;
    private String resim;
    private String kategoriid;

    public Tur() {
    }

    public Tur(String ad, String resim, String kategoriid) {
        this.ad = ad;
        this.resim = resim;
        this.kategoriid = kategoriid;
    }

    public String getAd() {
        return ad;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }

    public String getResim() {
        return resim;
    }

    public void setResim(String resim) {
        this.resim = resim;
    }

    public String getKategoriid() {
        return kategoriid;
    }

    public void setKategoriid(String kategoriid) {
        this.kategoriid = kategoriid;
    }
}
