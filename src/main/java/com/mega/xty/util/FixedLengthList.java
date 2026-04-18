package com.mega.xty.util;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Consumer;

/**
 * 有限长度列表，基于索引快捷删除元素的列表
 * 每次将新元素压入队首，移除多余队尾元素
 *
 * @param <T> 元素类型
 */
public class FixedLengthList<T> extends LinkedList<T> {
    private final int maxSize;
    private final Consumer<T> addTask;
    private final Consumer<T> removeTask;
    private final boolean setMode = true;

    public FixedLengthList(int size, Consumer<T> addTask, Consumer<T> removeTask) {
        super();
        this.addTask = addTask;
        this.removeTask = removeTask;
        this.maxSize = size;
    }

    public FixedLengthList(int size) {
        this(size, null, null);
    }

    @Override
    public boolean add(T t) {
        if (setMode && this.contains(t))
            return false;
        if (this.size() >= maxSize) {
            this.removeFirst();
        }
        if (addTask != null && t != null)
            addTask.accept(t);
        return super.add(t);
    }

    @Override
    public void addFirst(T t) {
        if (addTask != null && t != null)
            addTask.accept(t);
        super.addFirst(t);
    }

    @Override
    public void addLast(T t) {
        if (addTask != null && t != null)
            addTask.accept(t);
        super.addLast(t);
    }

    @Override
    public T removeFirst() {
        T t = super.removeFirst();
        if (removeTask != null && t != null)
            removeTask.accept(t);
        return t;
    }

    @Override
    public T removeLast() {
        T t = super.removeLast();
        if (removeTask != null && t != null) {
            removeTask.accept(t);
        }
        return t;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        if (removeTask != null)
            c.forEach(e -> {
                try {
                    if (e != null) removeTask.accept((T) e);
                } catch (ClassCastException ignored) {
                }
            });
        return super.removeAll(c);
    }

    @Override
    public void add(int index, T element) {
        if (setMode && this.contains(element))
            return;
        if (index > maxSize - 1) index = maxSize - 1;
        if (this.size() >= maxSize)
            this.removeFirst();
        if (addTask != null && element != null)
            addTask.accept(element);
        super.add(index, element);
    }

    @Override
    public boolean remove(Object o) {
        try {
            if (removeTask != null && o != null)
                removeTask.accept((T) o);
        } catch (ClassCastException ignored) {
        }
        return super.remove(o);
    }

    @Override
    public T remove(int index) {
        T e = super.remove(index);
        if (removeTask != null && e != null)
            removeTask.accept(e);
        return e;
    }
}
