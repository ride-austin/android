package com.rideaustin.ui.common;

/**
 * Encapsulates data about single change in {@link android.support.v7.widget.RecyclerView.Adapter}
 * Created by Sergey Petrov on 10/04/2017.
 */

public class AdapterItemChange {

    public enum Type {
        ADD, REMOVE
    }

    private int position;

    private int count;

    private Type type;

    private AdapterItemChange(Type type, int position, int count) {
        this.type = type;
        this.position = position;
        this.count = count;
    }

    public int getPosition() {
        return position;
    }

    public int getCount() {
        return count;
    }

    public Type getType() {
        return type;
    }

    public boolean isAddAction() {
        return type == Type.ADD;
    }

    public boolean isRemoveAction() {
        return type == Type.REMOVE;
    }

    public static AdapterItemChange add(int position) {
        return new AdapterItemChange(Type.ADD, position, 1);
    }

    public static AdapterItemChange remove(int position) {
        return new AdapterItemChange(Type.REMOVE, position, 1);
    }

    public static AdapterItemChange addRange(int position, int count) {
        return new AdapterItemChange(Type.ADD, position, count);
    }

    public static AdapterItemChange removeRange(int position, int count) {
        return new AdapterItemChange(Type.REMOVE, position, count);
    }

}
