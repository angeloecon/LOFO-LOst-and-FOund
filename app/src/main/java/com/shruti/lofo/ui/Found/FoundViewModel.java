package com.shruti.lofo.ui.Found;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.shruti.lofo.api.ApiService;
import com.shruti.lofo.api.RetrofitClient;
import com.shruti.lofo.data.model.Item;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoundViewModel extends ViewModel {

    private final MutableLiveData<List<Item>> foundItemLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    public LiveData<List<Item>> getFoundItems () { return foundItemLiveData; }

    public LiveData<String> getError () { return errorLiveData; }

    public void fetchFoundItems (boolean forceRefresh) {
        if(!forceRefresh && foundItemLiveData.getValue() != null){
            return;
        }

        ApiService apiService = RetrofitClient.getApiService();
        Call<List<Item>> call = apiService.getItems("Found");

        call.enqueue(new Callback<List<Item>>() {
            @Override
            public void onResponse(Call<List<Item>> call, Response<List<Item>> response) {
                if(response.isSuccessful() && response.body() != null){
                    foundItemLiveData.setValue(response.body());
                } else {
                    errorLiveData.setValue("Failed to load Found items.");
                }
            }

            @Override
            public void onFailure(Call<List<Item>> call, Throwable t) {
                Log.e("FoundViewModel", "API Error " + t.getMessage());
                errorLiveData.setValue("Network error while loading feed");
            }
        });
    }
}
