package com.example.watermonitoringsystem.models.map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterItem;

public class SensorMarker implements ClusterItem {
    private final LatLng position;
    private final String title;

    public SensorMarker(LatLng position, String title) {
        this.position = position;
        this.title = title;
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return position;
    }

    @Nullable
    @Override
    public String getTitle() {
        return title;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return null;
    }

    @Override
    public String toString() {
        return "SensorMarker{" +
                "position=" + position +
                ", title='" + title + '\'' +
                '}';
    }
}
