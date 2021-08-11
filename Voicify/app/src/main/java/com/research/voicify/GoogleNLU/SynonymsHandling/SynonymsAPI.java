package com.research.voicify.GoogleNLU.SynonymsHandling;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SynonymsAPI {
    // this is the Retrofit syntax
    @GET("words")
    Call<List<DetectedSynonym>> getSynonyms (@Query("ml") String word);
}
