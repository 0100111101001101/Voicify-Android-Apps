package com.research.voicify.GoogleNLU;

import androidx.recyclerview.widget.RecyclerView;

import com.google.cloud.automl.v1.ExamplePayload;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface GoogleAutoML {
    @Headers( "Content-Type: application/json; charset=utf-8")
    @POST("projects/684234233070/locations/us-central1/models/TEN1859834913499381760:predict")
    Call<RespondBody> getEntities(@Body RequestBody body);
}
