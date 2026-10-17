package com.example.horizontravel.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.ServiceSlot;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static final String PREF_NAME = "horizon_cart_prefs";
    private static final String KEY_CART_ITEMS = "cart_items";

    public interface OnCartChangeListener {
        void onCartChanged(int itemCount);
    }

    private static CartManager instance;
    private final SharedPreferences prefs;
    private final List<OnCartChangeListener> listeners = new ArrayList<>();

    private CartManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new CartManager(context);
        }
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("CartManager has not been initialized");
        }
        return instance;
    }

    public void addListener(OnCartChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(OnCartChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        int count = getCount();
        for (OnCartChangeListener listener : listeners) {
            listener.onCartChanged(count);
        }
    }

    public synchronized List<ServiceSlot> getItems() {
        String json = prefs.getString(KEY_CART_ITEMS, null);
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<ServiceSlot>>() {}.getType();
        List<ServiceSlot> items = ApiClient.getGson().fromJson(json, type);
        return items != null ? items : new ArrayList<>();
    }

    public synchronized boolean addItem(ServiceSlot slot) {
        if (slot == null || slot.getId() == null) return false;
        List<ServiceSlot> items = getItems();
        for (ServiceSlot item : items) {
            if (slot.getId().equals(item.getId())) {
                return false; // Already in cart
            }
        }
        items.add(slot);
        saveItems(items);
        notifyListeners();
        return true;
    }

    public synchronized void removeItem(long slotId) {
        List<ServiceSlot> items = getItems();
        boolean removed = false;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() != null && items.get(i).getId() == slotId) {
                items.remove(i);
                removed = true;
                break;
            }
        }
        if (removed) {
            saveItems(items);
            notifyListeners();
        }
    }

    public synchronized void clear() {
        prefs.edit().remove(KEY_CART_ITEMS).apply();
        notifyListeners();
    }

    public synchronized int getCount() {
        return getItems().size();
    }

    public synchronized double getTotal() {
        List<ServiceSlot> items = getItems();
        double sum = 0.0;
        for (ServiceSlot s : items) {
            sum += s.getPrice();
        }
        return sum;
    }

    public synchronized boolean contains(long slotId) {
        List<ServiceSlot> items = getItems();
        for (ServiceSlot item : items) {
            if (item.getId() != null && item.getId() == slotId) {
                return true;
            }
        }
        return false;
    }

    private void saveItems(List<ServiceSlot> items) {
        String json = ApiClient.getGson().toJson(items);
        prefs.edit().putString(KEY_CART_ITEMS, json).apply();
    }
}
