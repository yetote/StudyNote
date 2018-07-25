package com.example.ether.videodemo9;

public class DataTuple<A, B> {
    private final A result;
    private final B data;

    public DataTuple(A result, B data) {
        this.result = result;
        this.data = data;
    }

    @Override
    public String toString() {
        return "DataTuple{" +
                "result=" + result +
                ", data=" + data.toString() +
                '}';
    }
}
