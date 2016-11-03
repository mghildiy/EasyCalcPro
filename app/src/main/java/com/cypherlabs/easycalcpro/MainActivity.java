package com.cypherlabs.easycalcpro;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.Policy;
import com.google.android.vending.licensing.ServerManagedPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final byte[] SALT = new byte[] {
            98,108,-19,-117,-50,-111,7,95,32,1,76,-52,-116,32,-113,-99,112,-104,113,-113
    };

    private EditText input;
    private TextView output;
    private TextView outputInWords;
    private final List<String> entries = new ArrayList<>();
    private List<StringBuilder> inputBreakdowns = null;
    private String words_output_lang = null;
    private String englishOutput = Constants.EMPTY_STRING;
    private String hindiOutput = Constants.EMPTY_STRING;
    private boolean wordOutputOn = true;

    private TextView mStatusText;
    private LicenseCheckerCallback mLicenseCheckerCallback;
    private LicenseChecker mChecker;
    // A handler on the UI thread.
    private Handler mHandler;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Code block to validate application licensing
        //final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        //String deviceId =  tm.getDeviceId();
        String deviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        // Library calls this when it's done.
        mLicenseCheckerCallback = new EasyCalcProLicenseCheckerCallback();
        mHandler = new Handler();
        mChecker = new LicenseChecker(
                this, new ServerManagedPolicy(this,
                new AESObfuscator(SALT, getPackageName(), deviceId)),
                getResources().getString(R.string.BASE64_PUBLIC_KEY)
        );
        doCheck();
        //

        this.input = (EditText) findViewById(R.id.input);
        this.output = (TextView) findViewById(R.id.output);
        this.outputInWords = (TextView) findViewById(R.id.output_words);
        //restore last session values
        SharedPreferences easyCalcSharedPreferences = null;
        easyCalcSharedPreferences = getSharedPreferences(Constants.ACTIVITY_STATE_PREFS, MODE_PRIVATE);
        if (easyCalcSharedPreferences != null) {
            String savedIputBreakDowns = easyCalcSharedPreferences.getString(Constants.INPUTBREAKDOWNS, Constants.EMPTY_STRING);
            GsonBuilder gsonb = new GsonBuilder();
            Gson gson = gsonb.create();
            Type type = new TypeToken<List<StringBuilder>>() {
            }.getType();
            this.inputBreakdowns = gson.fromJson(savedIputBreakDowns, type);
            if (this.inputBreakdowns == null) {
                inputBreakdowns = new ArrayList<>();
            }

            //restore input and numeral output
            this.input.setText(easyCalcSharedPreferences.getString(Constants.INPUT, Constants.EMPTY_STRING));
            this.output.setText(easyCalcSharedPreferences.getString(Constants.OUTPUT_NUMERALS, ""));

            //restore language setting
            this.words_output_lang = easyCalcSharedPreferences.getString(Constants.WORDS_OUTPUT_LANG, null);
            if (words_output_lang == null) {
                this.words_output_lang = getResources().getString(R.string.main_english);
            }
            //restore english and hindi output values
            this.englishOutput = easyCalcSharedPreferences.getString(Constants.OUTPUT_ENGLISH, Constants.EMPTY_STRING);
            this.hindiOutput = easyCalcSharedPreferences.getString(Constants.OUTPUT_HINDI, Constants.EMPTY_STRING);
            //set word output as per language setting
            if (this.words_output_lang.equals(getResources().getString(R.string.main_english))) {
                MainActivity.this.outputInWords.setText(MainActivity.this.englishOutput);
            }
            if (this.words_output_lang.equals(getResources().getString(R.string.main_hindi))) {
                MainActivity.this.outputInWords.setText(MainActivity.this.hindiOutput);
            }

            //restore word output on/off boolean
            this.wordOutputOn = easyCalcSharedPreferences.getBoolean(Constants.WORD_OUTPUT_ON, true);
            if (!this.wordOutputOn) {
                this.outputInWords.setVisibility(View.GONE);
            }
        }

        entries.add("7");
        entries.add("8");
        entries.add("9");
        entries.add("/");

        entries.add("4");
        entries.add("5");
        entries.add("6");
        entries.add("*");

        entries.add("1");
        entries.add("2");
        entries.add("3");
        entries.add("-");

        entries.add("0");
        entries.add(".");
        entries.add("=");
        entries.add("+");

        if (this.wordOutputOn) {
            entries.add("ON");//to show word ourput or not
        } else {
            entries.add("OFF");//to show word ourput or not
        }
        entries.add("AC");//Clear function
        entries.add("DEL");//Clear last character
        entries.add("CPY");//copy functionality

        GridView gridview = (GridView) findViewById(R.id.gridview);
        TextAdapter textAdapter = new TextAdapter(this, this.entries);
        gridview.setAdapter(textAdapter);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private static final String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG,e.getMessage());
        }
        return "";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.main_english) {
            this.words_output_lang = getResources().getString(R.string.main_english);
            MainActivity.this.outputInWords.setText(MainActivity.this.englishOutput);
            return true;
        }
        if (id == R.id.main_hindi) {
            this.words_output_lang = getResources().getString(R.string.main_hindi);
            MainActivity.this.outputInWords.setText(MainActivity.this.hindiOutput);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.cypherlabs.easycalcpro/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.cypherlabs.easycalcpro/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    class TextAdapter extends BaseAdapter {
        private final Context context;
        private final List<String> gridEntries;

        public TextAdapter(Context context, List<String> gridEntries) {
            this.context = context;
            this.gridEntries = new ArrayList<>();
            this.gridEntries.addAll(gridEntries);
        }

        @Override
        public int getCount() {
            return this.gridEntries.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            TextView tv;
            if (convertView == null) {
                tv = new TextView(context);
            } else {
                tv = (TextView) convertView;
            }
            tv.setText(this.gridEntries.get(position));
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(30);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView buttonPressed = (TextView) v;
                    CharSequence keyPressed = buttonPressed.getText();

                    if (keyPressed.equals("ON")) {
                        buttonPressed.setText("OFF");
                        MainActivity.this.wordOutputOn = false;
                        MainActivity.this.outputInWords.setVisibility(View.GONE);
                        return;
                    }
                    if (keyPressed.equals("OFF")) {
                        buttonPressed.setText("ON");
                        MainActivity.this.wordOutputOn = true;
                        MainActivity.this.outputInWords.setVisibility(View.VISIBLE);
                        return;
                    }

                    if(keyPressed.equals("=")){
                        MainActivity.this.inputBreakdowns.clear();
                        MainActivity.this.input.getText().clear();
                        MainActivity.this.inputBreakdowns.add(new StringBuilder(MainActivity.this.output.getText().toString()));
                        MainActivity.this.input.setText(MainActivity.this.output.getText().toString());
                        return;
                    }

                    if (keyPressed.equals("CPY")) {
                        ClipboardManager clipMan = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        StringBuilder sb = new StringBuilder();
                        sb.append(MainActivity.this.input.getText().toString() + "\r\n ");
                        sb.append(MainActivity.this.output.getText().toString() + "\r\n ");
                        sb.append(MainActivity.this.outputInWords.getText().toString());
                        ClipData clipData = ClipData.newPlainText("Content", sb);
                        clipMan.setPrimaryClip(clipData);

                        Toast toast = Toast.makeText(getApplicationContext(), Constants.COPY_TOAST, Toast.LENGTH_SHORT);
                        toast.show();

                        return;
                    }

                    if (Utils.isCharacterAnOperator(keyPressed.charAt(0))) {
                        //make sure that first input is not an operator
                        if (MainActivity.this.inputBreakdowns.size() == 0) {
                            return;
                        } else {
                            //if last entry is an operator,replace it, else add a new operator
                            StringBuilder lastEntry = MainActivity.this.inputBreakdowns.get(MainActivity.this.inputBreakdowns.size() - 1);
                            if (Utils.isCharacterAnOperator(lastEntry.charAt(0))) {
                                lastEntry.delete(lastEntry.length() - 1, lastEntry.length());
                                lastEntry.append(keyPressed.charAt(0));
                            } else {
                                StringBuilder operator = new StringBuilder(keyPressed);
                                MainActivity.this.inputBreakdowns.add(operator);
                            }

                            MainActivity.this.input.getText().clear();
                            for (StringBuilder entry : MainActivity.this.inputBreakdowns) {
                                MainActivity.this.input.append(entry.toString());
                            }
                            return;
                        }
                    } else {
                        if (Utils.isCharacterAnOperand(keyPressed.charAt(0))) {
                            if (MainActivity.this.inputBreakdowns.size() == 0) {
                                StringBuilder operator = new StringBuilder(keyPressed);
                                MainActivity.this.inputBreakdowns.add(operator);
                            } else {
                                StringBuilder lastEntry = MainActivity.this.inputBreakdowns.get(MainActivity.this.inputBreakdowns.size() - 1);
                                //if last entry is an operator, add another entry, else modify last entry
                                if (Utils.isCharacterAnOperator(lastEntry.charAt(0))) {
                                    StringBuilder operand = new StringBuilder(keyPressed);
                                    MainActivity.this.inputBreakdowns.add(operand);
                                } else {
                                    //if key pressed is '.' and last entry already contains '.', make no change
                                    if (keyPressed.charAt(0) == '.') {
                                        if (lastEntry.indexOf(".") == -1) {
                                            lastEntry.append(keyPressed.charAt(0));
                                        }
                                    } else {
                                        //lastEntry.append(keyPressed.charAt(0));
                                        lastEntry.append(keyPressed);
                                    }
                                }
                            }
                        } else {
                            //it means its a DEL/AC scenario
                            if (keyPressed.equals("AC")) {
                                MainActivity.this.inputBreakdowns.clear();
                            } else {
                                if (MainActivity.this.inputBreakdowns.size() > 0) {
                                    StringBuilder lastEntry = MainActivity.this.inputBreakdowns.get(MainActivity.this.inputBreakdowns.size() - 1);
                                    if (Utils.isCharacterAnOperator(lastEntry.charAt(0))) {
                                        MainActivity.this.inputBreakdowns.remove(MainActivity.this.inputBreakdowns.size() - 1);
                                    } else {
                                        if (lastEntry.length() > 1) {
                                            lastEntry.delete(lastEntry.length() - 1, lastEntry.length());
                                        } else {
                                            MainActivity.this.inputBreakdowns.remove(MainActivity.this.inputBreakdowns.size() - 1);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (MainActivity.this.inputBreakdowns.size() == 0) {
                        MainActivity.this.input.getText().clear();
                        MainActivity.this.output.setText(Constants.EMPTY_STRING);
                        MainActivity.this.outputInWords.setText(Constants.EMPTY_STRING);
                        MainActivity.this.englishOutput = Constants.EMPTY_STRING;
                        MainActivity.this.hindiOutput = Constants.EMPTY_STRING;
                        return;
                    }

                    MainActivity.this.input.getText().clear();
                    List<String> components = new LinkedList<>();
                    for (StringBuilder entry : MainActivity.this.inputBreakdowns) {
                        MainActivity.this.input.append(entry.toString());
                        if (entry.length() == 1 && entry.charAt(0) == '.') {
                            components.add("0.0");
                        } else {
                            components.add(entry.toString());
                        }
                    }

                    //if last character is an operator, remove it
                    if (Utils.isCharacterAnOperator(components.get(components.size() - 1).charAt(0))) {
                        components.remove(components.size() - 1);
                    }

                    try {
                        Utils.solver(components);
                        ///////////////////
                        components.add("*");
                        components.add("1");
                        Utils.solver(components);
                        ///////////////////
                        StringBuilder inputForWords = new StringBuilder(components.get(0));
                        if (inputForWords.charAt(0) == '-') {
                            inputForWords.delete(0, 1);
                        }
                        String resultInEnglish = Utils.convertNumberToWordsInEnglish(inputForWords);
                        String resultInHindi = Utils.convertNumberToWordsInHindi(inputForWords);

                        MainActivity.this.output.setText(components.get(0));
                        MainActivity.this.englishOutput = resultInEnglish;
                        MainActivity.this.hindiOutput = resultInHindi;
                        if (MainActivity.this.words_output_lang.equals(getResources().getString(R.string.main_english))) {
                            MainActivity.this.outputInWords.setText(MainActivity.this.englishOutput);
                        }
                        if (MainActivity.this.words_output_lang.equals(getResources().getString(R.string.main_hindi))) {
                            MainActivity.this.outputInWords.setText(MainActivity.this.hindiOutput);
                        }
                    } catch (ArithmeticException ae) {
                        //Log.d(MainActivity.TAG, "Computation result: ∞");
                        MainActivity.this.output.setText(Constants.INFINITE_SYMBOL);
                        MainActivity.this.outputInWords.setText(Constants.UNDEFINED);
                    }
                }
            });

            GradientDrawable gd = new GradientDrawable();
            gd.setColor(getResources().getColor(R.color.Lavender)); // Changes this drawable to use a single color instead of a gradient
            gd.setCornerRadius(5);
            gd.setStroke(1, 0xFF000000);
            tv.setBackgroundDrawable(gd);

            return tv;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        this.saveState();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.saveState();
        mChecker.onDestroy();
    }

    private void saveState() {
        SharedPreferences easyCalcSharedPreferences = getSharedPreferences(Constants.ACTIVITY_STATE_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = easyCalcSharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(this.inputBreakdowns);
        editor.putString(Constants.INPUTBREAKDOWNS, json);
        editor.putString(Constants.INPUT, this.input.getText().toString());
        editor.putString(Constants.OUTPUT_NUMERALS, this.output.getText().toString());
        editor.putString(Constants.OUTPUT_ENGLISH, this.englishOutput);
        editor.putString(Constants.OUTPUT_HINDI, this.hindiOutput);
        editor.putString(Constants.WORDS_OUTPUT_LANG, this.words_output_lang);
        editor.putBoolean(Constants.WORD_OUTPUT_ON, this.wordOutputOn);
        editor.apply();
    }

    private void doCheck() {
        //mCheckLicenseButton.setEnabled(false);
        setProgressBarIndeterminateVisibility(true);
        //mStatusText.setText(R.string.checking_license);
        mChecker.checkAccess(mLicenseCheckerCallback);
    }

    protected Dialog onCreateDialog(int id) {
        final boolean bRetry = id == 1;
        // We have only one dialog.
        return new AlertDialog.Builder(this)
                .setTitle(R.string.unlicensed_dialog_title)
                .setMessage(bRetry ? R.string.unlicensed_dialog_retry_body : R.string.unlicensed_dialog_body)
                .setPositiveButton(R.string.buy_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                "http://market.android.com/details?id=" + getPackageName()));
                        startActivity(marketIntent);
                        finish();
                    }
                })
                .setNegativeButton(R.string.quit_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNeutralButton("Re-Check", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        doCheck();
                    }
                })
                .setCancelable(false)
                .setOnKeyListener(new DialogInterface.OnKeyListener(){
                    public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                        //Log.i("License", "Key Listener");
                        finish();
                        return true;
                    }
                })
                .create();
    }

    protected Dialog onCreateDialogAlt(int id) {
        final boolean bRetry = id == 1;
        return new AlertDialog.Builder(this)
                .setTitle(R.string.unlicensed_dialog_title)
                .setMessage(bRetry ? R.string.unlicensed_dialog_retry_body : R.string.unlicensed_dialog_body)
                .setPositiveButton(bRetry ? R.string.retry_button : R.string.buy_button, new DialogInterface.OnClickListener() {
                    boolean mRetry = bRetry;
                    public void onClick(DialogInterface dialog, int which) {
                        if ( mRetry ) {
                            doCheck();
                        } else {
                            Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                    "http://market.android.com/details?id=" + getPackageName()));
                            startActivity(marketIntent);
                        }
                    }
                })
                .setNegativeButton(R.string.quit_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(false)
                .setOnKeyListener(new DialogInterface.OnKeyListener(){
                    public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                        //Log.i("License", "Key Listener");
                        finish();
                        return true;
                    }
                })
                .create();
    }

    private class EasyCalcProLicenseCheckerCallback implements LicenseCheckerCallback {
        private static final String INNERTAG = "EasyCalcProLicenseCheckerCallback";

        @SuppressLint("LongLogTag")
        @Override
        public void applicationError(int errorCode) {
            //Log.e(INNERTAG,String.valueOf(errorCode));
            //Log.i(INNERTAG, "Error:"+errorCode);
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }
            //dontAllow(errorCode);
            //showDialog(0);
            //Toast toast = Toast.makeText(getApplicationContext(), "Response applicationError:"+errorCode, Toast.LENGTH_LONG);
            //toast.show();
        }

        @SuppressLint("LongLogTag")
        public void allow(int reason) {
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }
            //Log.i(INNERTAG,"License Accepted!");
            // Should allow user access.
            //displayResult(getString(R.string.allow));
            //Toast toast = Toast.makeText(getApplicationContext(), "Response allow:"+reason, Toast.LENGTH_LONG);
            //toast.show();
        }

        @SuppressLint("LongLogTag")
        public void dontAllow(int reason) {
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }
            //displayResult(getString(R.string.dont_allow));
            //Log.i(INNERTAG,"License denied!");
            //Log.i(INNERTAG,"Reason for denial:"+reason);
            //Toast toast = Toast.makeText(getApplicationContext(), "Response dontAllow:"+reason, Toast.LENGTH_LONG);
            //toast.show();

            displayDialog(reason == Policy.RETRY);
        }

        private void displayDialog(final boolean showRetry) {
            mHandler.post(new Runnable() {
                public void run() {
                    setProgressBarIndeterminateVisibility(false);
                    showDialog(showRetry ? 1 : 0);
                    //mCheckLicenseButton.setEnabled(true);
                }
            });
        }

        private void displayResult(final String result) {
            mHandler.post(new Runnable() {
                public void run() {
                    mStatusText.setText(result);
                    setProgressBarIndeterminateVisibility(false);
                    //mCheckLicenseButton.setEnabled(true);
                }
            });
        }
    }
}


