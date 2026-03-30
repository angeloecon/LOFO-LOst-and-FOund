package com.shruti.lofo.ui.DashBoard;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.shruti.lofo.api.ApiService;
import com.shruti.lofo.api.RetrofitClient;
import com.shruti.lofo.data.model.Item;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashBoardViewModel extends ViewModel {

    private final MutableLiveData<List<Item>> recentItemLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public LiveData<List<Item>> getRecentItem() {
        return  recentItemLiveData;
    }

    public LiveData<String> getError() {
        return  errorLiveData;
    }

    public void fetchFeed(boolean forceRefresh) {
        if(!forceRefresh && getRecentItem().getValue() != null){
            return;
        }

        ApiService apiService = RetrofitClient.getApiService();
        Call<List<Item>> call = apiService.getItems(null);

        call.enqueue(new Callback<List<Item>>() {
            @Override
            public void onResponse(Call<List<Item>> call, Response<List<Item>> response) {
                if(response.isSuccessful() && response.body() != null){
                     List<Item> fetchedItems = response.body();

                     // for top and to limit the front items
                     List<Item> topTenItem = new ArrayList<>();

                     int limit = Math.min(fetchedItems.size(), 10);
                     for (int i = 0; i < limit; i++){
                         topTenItem.add(fetchedItems.get(i));
                     }

                     recentItemLiveData.setValue(topTenItem);
                } else {
                    errorLiveData.setValue("Failed to load feed.");
                 }
            }

            @Override
            public void onFailure(Call<List<Item>> call, Throwable t) {
                Log.e("DashBoard", "API Error: " + t.getMessage());
                errorLiveData.setValue("Network error while loading feed.");
            }
        });
    }

}