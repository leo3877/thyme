package org.yixi.thyme.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 批处理器
 *
 * @author yixi
 * @since 1.0.0
 */
@SuppressWarnings("all")
public abstract class Batcher<In, Out> {

    private final int size;

    protected List<In> buffer = new ArrayList<>();
    protected volatile boolean flush;

    private int totalSize;

    protected Batcher childBatcher;

    public Batcher(int size) {
        this.size = size;
    }

    public void addParentBatcher(Batcher batcher) {
        batcher.addChildBatcher(this);
    }

    public Batcher addChildBatcher(Batcher batcher) {
        childBatcher = batcher;
        return this;
    }

    public synchronized Batcher flush() {
        flush = true;
        doRun();
        return this;
    }

    public synchronized Batcher addObject(In t) {
        buffer.add(t);
        totalSize += 1;
        doRun();
        return this;
    }

    public synchronized Batcher addObjects(Collection<In> c) {
        c.forEach(this::addObject);
        return this;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public int getSize() {
        return size;
    }

    public abstract List<Out> run(List<In> buffer);

    private void doRun() {
        if (buffer.size() == size || flush && buffer.size() > 0) {
            List<Out> outer = run(new ArrayList<>(buffer));
            if (childBatcher != null) {
                childBatcher.addObjects(outer);
                if (flush) {
                    childBatcher.flush();
                }
            }
            buffer.clear();
        }
    }
}
