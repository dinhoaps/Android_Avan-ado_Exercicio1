package com.androidavancado.posiot.meump3player;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidavancado.posiot.meump3player.service.MyMp3Binder;
import com.androidavancado.posiot.meump3player.service.MyMp3Service;

public class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private MyMp3Service mMP3Service;
    //private ProgressBar mPrgDuracao;
    //private TextView mTxtMusica;
    //private TextView mTxtDuracao;
    private String mMusica;
    private SimpleCursorAdapter mAdapter;

    String[] colunas = new String[]{
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns._ID
    };
    private Handler mHandler = new Handler();

    private Thread mThreadProgresso = new Thread() {
        public void run() {
            //atualizarTela();
            if (mMP3Service.getTempoTotal() > mMP3Service.getTempoDecorrido()) {
                mHandler.postDelayed(this, 1000);
            }
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int[] componentes = new int[]{
                android.R.id.text1,
                android.R.id.text2
        };
        mAdapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_2,
                null,
                colunas,
                componentes,
                0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
            getSupportLoaderManager().initLoader(0, null, this);
        } else {
            Toast.makeText(this, "Permissão negada.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter.getCount() == 0) {
            String permissao = Manifest.permission.READ_EXTERNAL_STORAGE;
            if (ActivityCompat.checkSelfPermission(this, permissao) ==
                    PackageManager.PERMISSION_GRANTED) {
                getSupportLoaderManager().initLoader(0, null, this);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{permissao}, 0);
            }
        }
        Intent it = new Intent(this, MyMp3Service.class);
        //startService(it);
        //bindService(it, this, BIND_AUTO_CREATE);
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Cursor cursor = (Cursor)adapterView.getItemAtPosition(i);
        String musica = cursor.getString(
                cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));

        Intent intent = new Intent(this, MyMp3Activity.class);
        intent.putExtra("nomeMusica", musica);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(
                this,
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                colunas,
                MediaStore.Audio.AudioColumns.IS_MUSIC +" = 1",
                null,
                null);
    }
    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
        ListView listView = (ListView)findViewById(R.id.listView);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);
    }
    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
