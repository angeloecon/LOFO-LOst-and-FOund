package com.shruti.lofo.ui.Lost;


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

public class LostViewModel extends ViewModel {
    private final MutableLiveData<List<Item>> lostItemLiveData = new MutableLiveData<>();
    private final MutableLiveData<String>errorLiveData = new MutableLiveData<>();

    public LiveData<List<Item>> getLostItem() {
        return lostItemLiveData;
    }

    public LiveData<String> getError(){
        return errorLiveData;
    }

    public void fetchLostItems (boolean forceRefresh) {
        if (!forceRefresh && lostItemLiveData.getValue() != null) {
            return;
        }

        ApiService apiService = RetrofitClient.getApiService();
        Call<List<Item>> call = apiService.getItems("Lost");

        call.enqueue(new Callback<List<Item>>() {
            @Override
            public void onResponse(Call<List<Item>> call, Response<List<Item>> response) {
                if(response.isSuccessful() && response.body() != null) {
                    lostItemLiveData.setValue(response.body());
                } else {
                    errorLiveData.setValue("Failed to load Lost items.");
                }
            }

            @Override
            public void onFailure(Call<List<Item>> call, Throwable t) {
                Log.e("LostViewModel", "API Error: " + t.getMessage());
                errorLiveData.setValue("Network error while loading feed.");
            }
        });
    }
}