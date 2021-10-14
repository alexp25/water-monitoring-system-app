package com.example.watermonitoringsystem.models.app;

import java.util.Objects;

/**
 * @author Ioan-Alexandru Chirita
 */
public class ChannelsData {

    private int channelId;
    private double waterFlowValue;

    public ChannelsData() {
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public double getWaterFlowValue() {
        return waterFlowValue;
    }

    public void setWaterFlowValue(double waterFlowValue) {
        this.waterFlowValue = waterFlowValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChannelsData that = (ChannelsData) o;
        return channelId == that.channelId &&
                Double.compare(that.waterFlowValue, waterFlowValue) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelId, waterFlowValue);
    }

    @Override
    public String toString() {
        return "ChannelsData{" +
                "channelId=" + channelId +
                ", waterFlowValue=" + waterFlowValue +
                '}';
    }
}
