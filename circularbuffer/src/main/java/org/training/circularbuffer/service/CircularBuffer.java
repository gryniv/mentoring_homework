package org.training.circularbuffer.service;

import java.util.*;


public class CircularBuffer<T> implements Buffer<T> {

    private LinkedList<T> buffer = new LinkedList<>();

    private int maxSize;
    private int tail;

    public CircularBuffer(int maxSize) {
        this.maxSize = maxSize;
        this.tail = maxSize - 1;
    }

    @Override
    public void put(T o) {
        buffer.add(o);
        if (buffer.size() > maxSize) {
            buffer.remove();
        }
    }

    @Override
    public T get() throws RuntimeException {
        if (maxSize != 1) {
            if (tail < 0) {
                tail = maxSize - 1;
            }
            return buffer.get(tail--);
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public Object[] toObjectArray() {
        return reverse(buffer).toArray();
    }

    @Override
    public T[] toArray() {
        if (buffer.size() != 0) {
            int bufferSize = buffer.size();
            final Class<?> componentType = buffer.get(0).getClass();
            T[] r = (T[]) java.lang.reflect.Array.newInstance(componentType, bufferSize);
            Iterator<Object> it = Arrays.stream(buffer.toArray()).iterator();

            for (int i = 0; i < r.length; i++) {
                r[i] = (T) it.next();
            }
            return r;
        } else {
            return (T[]) new Object[0];
        }
    }

    @Override
    public List<T> asList() {
        return reverse(buffer);
    }

    @Override
    public void addAll(List<? extends T> toAdd) {
        for (T a : toAdd) {
            put(a);
        }
    }

    @Override
    public void sort(Comparator<? super T> comparator) {
        buffer.sort(comparator);
    }

    @Override
    public boolean isEmpty() {
        return buffer.isEmpty();
    }

    private LinkedList<T> reverse(LinkedList<T> list) {
        LinkedList<T> buffer = new LinkedList<>(list);
        Collections.reverse(buffer);
        return buffer;
    }

}

