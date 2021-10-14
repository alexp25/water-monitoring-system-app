package com.example.watermonitoringsystem.models.sqldb;

/**
 * @author Ioan-Alexandru Chirita
 */
public class Topic {

    private int id;
    private int code;
    private String name;
    private int logRate;

    public Topic() {
    }

    public Topic(int id, int code, String name, int logRate) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.logRate = logRate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLogRate() {
        return logRate;
    }

    public void setLogRate(int logRate) {
        this.logRate = logRate;
    }

    @Override
    public String toString() {
        return "Topic{" +
                "id=" + id +
                ", code=" + code +
                ", name='" + name + '\'' +
                ", logRate=" + logRate +
                '}';
    }
}
