package com.example.notepad;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class NoteActivity extends AppCompatActivity {

    private boolean mIsViewingOrUpdating;
    private long mNoteCreationTime;
    private String mFileName;
    private Note mLoadedNote = null;

    private EditText mEtTitle;
    private EditText mEtContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        mEtTitle = (EditText) findViewById(R.id.note_et_title);
        mEtContent = (EditText) findViewById(R.id.note_et_content);

        //открытие файла
        mFileName = getIntent().getStringExtra(Utilities.EXTRAS_NOTE_FILENAME);
        if(mFileName != null && !mFileName.isEmpty() && mFileName.endsWith(Utilities.FILE_EXTENSION)) {
            mLoadedNote = Utilities.getNoteByFileName(getApplicationContext(), mFileName);
            if (mLoadedNote != null) {
                mEtTitle.setText(mLoadedNote.getTitle());
                mEtContent.setText(mLoadedNote.getContent());
                mNoteCreationTime = mLoadedNote.getDateTime();
                mIsViewingOrUpdating = true;
            }
            //создание файла
        } else {
            mNoteCreationTime = System.currentTimeMillis();
            mIsViewingOrUpdating = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //загрузка меню в зависимости от сосотояния
        if(mIsViewingOrUpdating) {
            getMenuInflater().inflate(R.menu.menu_note_view, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_note_add, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_save_note:
            case R.id.action_update:
                validateAndSaveNote();
                break;

            case R.id.action_delete:
                actionDelete();
                break;

            case R.id.action_cancel:
                actionCancel();
                break;
            case R.id.action_settings:
                Intent intent = new Intent(NoteActivity.this, Setting.class);
                startActivity(intent);
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        float fSize = Float.parseFloat(sharedPreferences.getString("Размер", "20"));
        mEtTitle.setTextSize(fSize);
        mEtContent.setTextSize(fSize);


        int color = Color.BLACK;
        if (sharedPreferences.getBoolean(getString(R.string.action_color_red), false)) {
            color += Color.RED;
        }
        if (sharedPreferences.getBoolean(getString(R.string.action_color_green), false)) {
            color += Color.GREEN;
        }
        if (sharedPreferences.getBoolean(getString(R.string.action_color_blue), false)) {
            color += Color.BLUE;
        }
        mEtTitle.setTextColor(color);
        mEtContent.setTextColor(color);

        int backcolor = Color.WHITE;
        if (sharedPreferences.getBoolean(getString(R.string.night_mode), false)) {

            backcolor = Color.GRAY;
            color = Color.WHITE;
        } else {
            backcolor = Color.WHITE;
            color = Color.BLACK;
        }
        mEtTitle.setBackgroundColor(backcolor);
        mEtContent.setBackgroundColor(backcolor);
    }

    @Override
    public void onBackPressed() {
        actionCancel();
    }


    private void actionDelete() {

        AlertDialog.Builder dialogDelete = new AlertDialog.Builder(this)
                .setTitle("Удалить заметку")
                .setMessage("Вы действительно хотите удалить заметку?")
                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(mLoadedNote != null && Utilities.deleteFile(getApplicationContext(), mFileName)) {
                            Toast.makeText(NoteActivity.this, mLoadedNote.getTitle() + " Удалено"
                                    , Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(NoteActivity.this, "Не может удалить заметку '" + mLoadedNote.getTitle() + "'"
                                    , Toast.LENGTH_SHORT).show();
                        }
                        finish();
                    }
                })
                .setNegativeButton("Нет", null);

        dialogDelete.show();
    }


    private void actionCancel() {

        if(!checkNoteAltred()) {
            finish();
        } else {
            AlertDialog.Builder dialogCancel = new AlertDialog.Builder(this)
                    .setTitle("Отменить изменения?")
                    .setMessage("Вы уверены,что не хотите сохранять измененния?")
                    .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton("Нет", null);
            dialogCancel.show();
        }
    }


    private boolean checkNoteAltred() {
        if(mIsViewingOrUpdating) {
            return mLoadedNote != null && (!mEtTitle.getText().toString().equalsIgnoreCase(mLoadedNote.getTitle())
                    || !mEtContent.getText().toString().equalsIgnoreCase(mLoadedNote.getContent()));
        } else {
            return !mEtTitle.getText().toString().isEmpty() || !mEtContent.getText().toString().isEmpty();
        }
    }


    private void validateAndSaveNote() {


        String title = mEtTitle.getText().toString();
        String content = mEtContent.getText().toString();

        if(title.isEmpty()) {
            Toast.makeText(NoteActivity.this, "Пожалуйста введите заголовок!"
                    , Toast.LENGTH_SHORT).show();
            return;
        }

        if(content.isEmpty()) {
            Toast.makeText(NoteActivity.this, "Пожалуйста введите содержание заметки"
                    , Toast.LENGTH_SHORT).show();
            return;
        }


        if(mLoadedNote != null) {
            mNoteCreationTime = mLoadedNote.getDateTime();
        } else {
            mNoteCreationTime = System.currentTimeMillis();
        }


        if(Utilities.saveNote(this, new Note(mNoteCreationTime, title, content))) { //success!

            Toast.makeText(this, "Заметка сохранена", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Не удалось сохранить заметку.Убедитесь ,что" +
                    "на устройстве достаточно места", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}
