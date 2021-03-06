package com.destinyapp.aplikasisdn07.Admin.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.destinyapp.aplikasisdn07.API.ApiRequest;
import com.destinyapp.aplikasisdn07.API.RetroServer;
import com.destinyapp.aplikasisdn07.Admin.MainAdminActivity;
import com.destinyapp.aplikasisdn07.Models.DataModel;
import com.destinyapp.aplikasisdn07.Models.ResponseModel;
import com.destinyapp.aplikasisdn07.R;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdapterVerifNilai extends RecyclerView.Adapter<AdapterVerifNilai.HolderData> {
    private List<DataModel> mList;
    private Context ctx;
    private String[] listItems;

    public AdapterVerifNilai(Context ctx, List<DataModel> mList){
        this.ctx = ctx;
        this.mList = mList;
    }

    @NonNull
    @Override
    public HolderData onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View layout = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_nilai_verif,viewGroup,false);
        HolderData holder = new HolderData(layout);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterVerifNilai.HolderData holderData, int posistion) {
        DataModel dm = mList.get(posistion);
        holderData.NIS.setText(dm.getNis());
        holderData.NamaSiswa.setText(dm.getNama_siswa());
        holderData.mapel.setText(dm.getNama_mapel());
        holderData.kelas.setText(dm.getNama_kelas());
        holderData.nilai.setText(dm.getNilai());
        if (dm.getVerif().equals("sudah")){
            holderData.verif.setText("Update");
        }else{
            holderData.verif.setText(dm.getVerif()+" Terverivikasi");
        }

        String url = dm.getProfile_siswa();
        String BASE_URL = ctx.getString(R.string.base_url);
        String URL = BASE_URL+"ProfilePicture/Siswa/"+url;
        Glide.with(ctx)
                .load(URL)
                .into(holderData.Profile);
        holderData.dm=dm;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class HolderData extends RecyclerView.ViewHolder{
        TextView NIS,NamaSiswa,kelas,mapel,nilai;
        Button verif;
        ImageView Profile;
        DataModel dm;
        HolderData(View v){
            super(v);
            NIS = (TextView)v.findViewById(R.id.tv_NIS);
            NamaSiswa = (TextView)v.findViewById(R.id.tv_NamaSiswa);
            kelas = (TextView)v.findViewById(R.id.tvKelas);
            mapel = (TextView)v.findViewById(R.id.tvMapel);
            nilai = (TextView)v.findViewById(R.id.tvNilai);
            verif = (Button)v.findViewById(R.id.btnStatus);
            Profile = (ImageView)v.findViewById(R.id.ivPhotoSiswa);
            verif.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String Verif=dm.getVerif();
                    String NIS=dm.getNis();
                    String Mapel=dm.getId_mapel();
                    if (Verif.equals("belum")){
                        Sure(NIS,Verif,Mapel);
                    }else if(Verif.equals("sudah")){
                        UpdateAbsensi(NIS,Mapel);
                    }
                }
            });
        }
    }
    private void UpdatePilih(String NIS,String status,String Mapel){
        if (status.equals("Delete")){

        }else if(status.equals("Update")){
            Intent goInput = new Intent(ctx, MainAdminActivity.class);
            goInput.putExtra("KEY_NILAI","update");
            goInput.putExtra("MAPEL",Mapel);
            goInput.putExtra("NIS",NIS);
            ctx.startActivities(new Intent[]{goInput});
        }
    }
    private void UpdateAbsensi(final String NIS,final String Mapel){
        listItems = new String[]{"Update","Delete"};
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(ctx);
        mBuilder.setTitle("Pilih Status Siswa");
        mBuilder.setSingleChoiceItems(listItems, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                String status = listItems[i];
                dialog.dismiss();
                UpdatePilih(NIS,status,Mapel);
            }
        });
        mBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }
    private void VerivikasiBelumNilai(String NIS,String verif,String Mapel){
        ApiRequest api = RetroServer.getClient().create(ApiRequest.class);
        Call<ResponseModel> VerifAbsen = api.updateVerifNilai(NIS,"sudah",Mapel);
        VerifAbsen.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                String Response = response.body().getResponse();
                if(Response.equals("Update")){
                    Toast.makeText(ctx,"Data Berhasil Terverifikasi",Toast.LENGTH_SHORT).show();
                    Intent goInput = new Intent(ctx, MainAdminActivity.class);
                    goInput.putExtra("NILAI","sudah");
                    ctx.startActivities(new Intent[]{goInput});
                }
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Toast.makeText(ctx,"Koneksi gagal",Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void Sure(final String NIS, final String Verif,final String Mapel){
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setMessage("Anda Ingin memverivikasi Absensi ini ? ")
                .setCancelable(false)
                .setPositiveButton("Iya", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        VerivikasiBelumNilai(NIS,Verif,Mapel);
                    }
                })
                .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                //Set your icon here
                .setTitle("Absensi")
                .setIcon(R.drawable.icon);
        AlertDialog alert = builder.create();
        alert.show();
    }
}
