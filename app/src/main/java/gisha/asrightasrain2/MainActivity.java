package gisha.asrightasrain2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.HttpEntity;
import android.os.AsyncTask;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Log;
import  org.apache.http.client.methods.HttpGet;
import org.apache.http.StatusLine;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.SeekBar;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements  TextToSpeech.OnInitListener {
        TextView tvWeather, tvPlace, tvTimezone;

    private TextToSpeech mTts;
    private EditText mEditText;
    private Button mSayButton;

    private float mPitch = 1.0f;
    private float mSpeed = 1.0f;


    @Override
        protected void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            tvWeather = (TextView) findViewById(R.id.tvWeatherInfo);
            tvPlace = (TextView) findViewById(R.id.tvPlaceInfo);
            tvTimezone = (TextView) findViewById(R.id.tvTimeZone);

            mTts = new TextToSpeech(this, this);

            mEditText = (EditText) findViewById(R.id.editText);
            mSayButton = (Button) findViewById(R.id.buttonSayIt);

            SeekBar pitchSeekBar = (SeekBar) findViewById(R.id.seekBarPitch);
            pitchSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    mPitch = (float) progress / 100;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

            SeekBar speedSeekBar = (SeekBar) findViewById(R.id.seekBarSpeed);
            speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    mSpeed = (float) progress / 100;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        }

    public void onClick(View v) {
        sayWords();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = mTts.setLanguage(Locale.US);
            // используем русский язык
            //Locale locale = new Locale("ru");
            //int result = mTts.setLanguage(locale);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Данный язык не поддерживается");
            } else {
                mSayButton.setEnabled(true);
                sayWords();
            }

        } else {
            Log.e("TTS", "Не удалось инициализировать движок!");
        }

    }

    @Override
    public void onDestroy() {
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
        super.onDestroy();
    }

    private void sayWords() {
        mTts.setPitch(mPitch);
        mTts.setSpeechRate(mSpeed);
        // Получим текст из текстового поля

        String text = mEditText.getText().toString();
        // Проговариваем
        mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    public String readJSONData(String URL) {
            StringBuilder stringBuilder = new StringBuilder();
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(URL);
            try {
                HttpResponse response = httpClient.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    InputStream inputStream = entity.getContent();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    inputStream.close();
                } else {
                    Log.d("Geonames", "Ошибка при чтении файла");
                }
            } catch (Exception e) {
                Log.d("GeoNames", e.getLocalizedMessage());
            }
            return stringBuilder.toString();
        }

    public void btnGetWeather(View view) {
        EditText txtLat = (EditText) findViewById(R.id.txtLat);
        EditText txtLong = (EditText) findViewById(R.id.txtLong);

        new GetWeatherTask()
                .execute("http://ws.geonames.org/findNearByWeatherJSON?lat="
                        + txtLat.getEditableText().toString() + "&lng="
                        + txtLong.getText().toString()+ "&username=darinshik");

        // Бонус. Узнаем часовой пояс
        new GetTimezoneTask()
                .execute("http://api.geonames.org/timezoneJSON?lat="
                        + txtLat.getEditableText().toString() + "&lng="
                        + txtLong.getText().toString() + "&username=darinshik");

    }

    private class GetWeatherTask extends AsyncTask<String, Void, String> {
            protected String doInBackground(String... urls) {
                return readJSONData(urls[0]);
            }

            protected void onPostExecute(String result) {
                mTts.setPitch(mPitch);
                mTts.setSpeechRate(mSpeed);
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject weatherObservationItems = new JSONObject(
                            jsonObject.getString("weatherObservation"));

                   tvWeather.setText("Облачность: "
                          + weatherObservationItems.getString("clouds")
                            + ", Температура: "
                          + weatherObservationItems.getString("temperature")
                           + ", Роса: "
                           + weatherObservationItems.getString("dewPoint")
                           + ", Влажность: "
                           + weatherObservationItems.getString("humidity")
                           + ", Скорость ветра: "
                           + weatherObservationItems.getString("windDirection"));
                    String text = "Cloudiness: "
                            + weatherObservationItems.getString("clouds")
                            + ", Temperature: "
                            + weatherObservationItems.getString("temperature")
                            + ", Dew Point: "
                            + weatherObservationItems.getString("dewPoint")
                            + ", Humidity: "
                            + weatherObservationItems.getString("humidity")
                            + ", Wind Direction: "
                            + weatherObservationItems.getString("windDirection");
                    mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);

                } catch (Exception e) {
                    Log.d("GeoNames", e.getLocalizedMessage());
                }






            }
        }

    private class GetTimezoneTask extends AsyncTask<String, Void, String> {
            protected String doInBackground(String... urls) {
                return readJSONData(urls[0]);
            }

            protected void onPostExecute(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);

                    tvTimezone.setText("Country: "
                            + jsonObject.getString("countryName")
                            + ", Time zoneId: "
                            + jsonObject.getString("timezoneId"));

                } catch (Exception e) {
                    Log.d("GeoNames", e.getLocalizedMessage());
                }
            }
        }

}


