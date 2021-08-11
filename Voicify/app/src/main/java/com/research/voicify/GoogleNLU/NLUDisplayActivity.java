package com.research.voicify.GoogleNLU;

import android.opengl.ETC1;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.language.v1.CloudNaturalLanguage;
import com.google.api.services.language.v1.CloudNaturalLanguageRequest;
import com.google.api.services.language.v1.CloudNaturalLanguageScopes;
import com.google.api.services.language.v1.model.AnalyzeEntitiesRequest;
import com.google.api.services.language.v1.model.AnalyzeEntitiesResponse;
import com.google.api.services.language.v1.model.AnnotateTextRequest;
import com.google.api.services.language.v1.model.Document;
import com.google.api.services.language.v1.model.Entity;
import com.google.api.services.language.v1.model.Features;
import com.research.voicify.GoogleNLU.SynonymsHandling.DetectedSynonym;
import com.research.voicify.GoogleNLU.SynonymsHandling.SynonymsAPI;
import com.research.voicify.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NLUDisplayActivity extends AppCompatActivity {
    EditText editTextView; // View to get the text to be analyzed
    TextView resultTextView; // View to display the results obtained after analysis
    NestedScrollView nestedScrollView; // Wrapper view so that if the results are getting out of bounds from screen so the user can scroll and see complete results
    private static final int LOADER_ACCESS_TOKEN = 1; // Token used to initiate the request loader
    private GoogleCredential mCredential = null; //GoogleCredential object so that the requests for NLP Api could be made

    // A  Thread on which the Api request will be made and results will be delivered. As network calls cannot be made on the amin thread, so we are creating a separate thread for the network calls
    private Thread mThread;

    // Google Request for the NLP Api. This actually is acting like a Http client queue that will process each request and response from the Google Cloud server
    private final BlockingQueue<CloudNaturalLanguageRequest<? extends GenericJson>> mRequests
            = new ArrayBlockingQueue<>(3);

    public ArrayList<EntityModel> entityModels = new ArrayList<EntityModel>();
    // Api for CloudNaturalLanguage from the Google Client library, this is the instance of our request that we will make to analyze the text.
    private CloudNaturalLanguage mApi = new CloudNaturalLanguage.Builder(
            new NetHttpTransport(),
            JacksonFactory.getDefaultInstance(),
            new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) throws IOException {
                    mCredential.initialize(request);
                }
            }).build();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nludisplay);
        editTextView = (EditText) findViewById(R.id.text_et);
        resultTextView = (TextView) findViewById(R.id.result_tv);
        nestedScrollView = (NestedScrollView) findViewById(R.id.nsv);
        prepareApi();

    }

    /**
     * Method called on the click of the Button
     * @param view -> the view which is clicked
     */
    public void startAnalysis(View view) {
        String textToAnalyze = editTextView.getText().toString();
        if (TextUtils.isEmpty(textToAnalyze)) {
            editTextView.setError("Err");
        } else {
            editTextView.setError(null);
            analyzeTextUsingCloudNLPApi(textToAnalyze);
        }
    }

    /**
     * This function will send the text to Cloud Api for analysis
     * @param text -> String to be analyzed
     */
    public void analyzeTextUsingCloudNLPApi(String text) {
        try {
            mRequests.add(mApi
                    .documents()
                    .analyzeEntities(new AnalyzeEntitiesRequest()
                            .setDocument(new Document()
                                    .setContent(text)
                                    .setType("PLAIN_TEXT"))));
        } catch (IOException e) {
            Log.e("TAG", "Failed to create analyze request.", e);
        }
    }


    /**
     * Preparing the Cloud Api before maiking the actual request.
     * This method will actually initiate the AccessTokenLoader async task on completion
     * of which we will recieve the token that should be set in our request for Cloud NLP Api.
     */
    private void prepareApi() {
        // Initiate token refresh
        getSupportLoaderManager().initLoader(LOADER_ACCESS_TOKEN, null,
                new LoaderManager.LoaderCallbacks<String>() {
                    @Override
                    public Loader<String> onCreateLoader(int id, Bundle args) {
                            return new AccessTokenLoader(NLUDisplayActivity.this);
                    }

                    @Override
                    public void onLoadFinished(Loader<String> loader, String token) {
                        setAccessToken(token);
                    }

                    @Override
                    public void onLoaderReset(Loader<String> loader) {
                    }
                });
    }


    /**
     * This method will set the token from the Credentials.json file to the Google credential object.
     * @param token -> token recieved from the Credentials.json file.
     */
    public void setAccessToken(String token) {
        mCredential = new GoogleCredential()
                .setAccessToken(token)
                .createScoped(CloudNaturalLanguageScopes.all());
        startWorkerThread();
    }


    /**
     * This method will actually initiate a Thread and on this thread we will execute our Api request
     * and responses.
     *
     * Responses recieved will be delivered from here.
     */
    private void startWorkerThread() {
        if (mThread != null) {
            return;
        }
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (mThread == null) {
                        break;
                    }
                    try {
                        // API calls are executed here in this worker thread
                        deliverResponse(mRequests.take().execute());
                    } catch (InterruptedException e) {
                        Log.e("TAG", "Interrupted.", e);
                        break;
                    } catch (IOException e) {
                        Log.e("TAG", "Failed to execute a request.", e);
                    }
                }
            }
        });
        mThread.start();
    }


    /**
     * this method will handle the response recieved from the Cloud NLP request.
     * The response is a JSON object only.
     * This has been casted to GenericJson from Google Cloud so that the developers can easily parse through the same and can understand the response.
     *
     *
     * @param response --> the JSON object recieved as a response for the cloud NLP Api request
     */
    private void deliverResponse(final GenericJson response) {
        Log.d("TAG", "Generic Response --> " + response);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(NLUDisplayActivity.this, "Response Recieved from Cloud NLP API", Toast.LENGTH_SHORT).show();

                try {
                    resultTextView.setText(response.toPrettyString());
                    nestedScrollView.setVisibility(View.VISIBLE);
                    final List<Entity> entities = ((AnalyzeEntitiesResponse) response).getEntities();
                    final int size = entities.size();
                    for (int i = 0; i < size; i++) {
                        entityModels.add( new EntityModel(entities.get(i)));
                    }
                    for (int i = 0; i < size; i++) {
                        String entityName = entityModels.get(i).name;
                        generateSynonyms(entityName);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });


    }

    /**
     * This is a method which can make calls to the free synonyms API service.
     * This function will concatenate 10 most accurate synonyms and print to the log.
     * @param entityName the string in English that you want to find corresponding synonyms
     **/
    private void generateSynonyms(String entityName){
        Retrofit retrofit = new Retrofit.Builder()  // retrofit boilerplate
                .baseUrl("https://api.datamuse.com/")
                .addConverterFactory(GsonConverterFactory.create()) // add converter
                .build();

        SynonymsAPI service = retrofit.create(SynonymsAPI.class);
        Call<List<DetectedSynonym>> serviceSynonyms = service.getSynonyms(entityName);  // pass in input string to find synonyms.
        serviceSynonyms.enqueue(new Callback<List<DetectedSynonym>>(){  // asynchronous call

            @Override
            public void onResponse(Call<List<DetectedSynonym>> call, Response<List<DetectedSynonym>> response) {
                List<DetectedSynonym> detectedSynonyms = response.body();   // get the body store data
                assert detectedSynonyms != null;    // null check
                StringBuilder returnString = new StringBuilder("Synonyms: ");
                int numOfWords = 0;
                for(DetectedSynonym detectedSynonym: detectedSynonyms){
                    if(detectedSynonym.getWord() != null){
                        returnString.append(detectedSynonym.getWord() + "/");      // append them onto a string to log to logcat
                        numOfWords ++;
                    }
                    if (numOfWords > 10) break; // get 10 most relevant only
                }
                Log.d("Test Synonyms", returnString.toString());    // print to log cat
            }

            @Override
            public void onFailure(Call<List<DetectedSynonym>> call, Throwable t) {
                Log.d("Test Synonyms", "Failed to connect to server");
            }
        });
    }
}
