package com.sap.fontus.taintaware.lazybasic;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.taintaware.lazybasic.operation.DeleteLayer;
import com.sap.fontus.taintaware.lazybasic.operation.InsertLayer;
import com.sap.fontus.taintaware.shared.*;
import com.sap.fontus.utils.stats.Statistics;
import com.sap.fontus.taintaware.lazybasic.operation.BaseLayer;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;

import java.util.*;

public class IASTaintInformation implements IASTaintInformationable {
    private final List<IASLayer> layers;
    private IASTaintInformation previous;

    public IASTaintInformation(List<IASLayer> layers, IASTaintInformation previous) {
        this.layers = new ArrayList<>();
        this.previous = previous;
        this.appendLayers(layers);
        if (Configuration.getConfiguration().collectStats()) {
            Statistics.INSTANCE.incrementLazyTaintInformationCreated();
        }
    }

    public IASTaintInformation(IASTaintInformation previous) {
        this.layers = new ArrayList<>();
        this.previous = previous;
        if (Configuration.getConfiguration().collectStats()) {
            Statistics.INSTANCE.incrementLazyTaintInformationCreated();
        }
    }

    public IASTaintInformation(BaseLayer baseLayer) {
        this.layers = new ArrayList<>(Collections.singletonList(baseLayer));
        if (Configuration.getConfiguration().collectStats()) {
            Statistics.INSTANCE.incrementLazyTaintInformationCreated();
        }
    }

    public IASTaintInformation(int size) {
        this(new BaseLayer(size));
    }

    public IASTaintInformation(int size, List<IASTaintRange> ranges) {
        this(new BaseLayer(new IASTaintRanges(size, ranges)));
    }

    public IASTaintInformation(IASTaintRanges ranges) {
        this(new BaseLayer(ranges));
    }

    private synchronized void appendLayers(List<IASLayer> layers) {
        for (IASLayer layer : layers) {
            this.appendLayer(layer);
        }
    }

    private synchronized void appendLayer(IASLayer layer) {
        if (this.getLayerDepth() >= Configuration.getConfiguration().getLayerThreshold()) {
            if (Configuration.getConfiguration().collectStats()) {
                Statistics.INSTANCE.incrementLazyThresholdExceededCount();
            }
            this.cache(this.evaluate());
        }
        this.layers.add(layer);
    }

    public synchronized IASTaintRanges getTaintRanges() {
        return this.evaluate().copy();
    }

    @Override
    public IASTaintRanges getTaintRanges(int length) {
        return this.getTaintRanges();
    }

    private synchronized int getLayerDepth() {
        return this.getOwnLayerCount() + this.getPreviousLayerCount();
    }

    private synchronized int getPreviousLayerCount() {
        return this.previous == null ? 0 : this.previous.getLayerDepth();
    }

    private synchronized int getOwnLayerCount() {
        return this.layers == null ? 0 : this.layers.size();
    }

    private synchronized IASTaintRanges getPreviousRanges() {
        if (this.previous == null) {
            return null;
        }
        return this.previous.getTaintRanges();
    }

    private synchronized boolean isBase() {
        return this.layers.size() == 1 && this.layers.get(0) instanceof BaseLayer;
    }

    private synchronized IASTaintRanges evaluate() {
        if (this.isBase()) {
            return ((BaseLayer) this.layers.get(0)).getBase();
        }

        if (Configuration.getConfiguration().collectStats()) {
            Statistics.INSTANCE.incrementLazyTaintInformationEvaluated();
        }

        IASTaintRanges previousRanges = this.getPreviousRanges();
        for (IASLayer layer : this.layers) {
            previousRanges = layer.apply(previousRanges);
        }
        if (Configuration.getConfiguration().useCaching()) {
            this.cache(previousRanges);
        }
        return previousRanges;
    }

    private synchronized void cache(IASTaintRanges ranges) {
        if (Configuration.getConfiguration().useCaching()) {
            this.layers.clear();
            this.layers.add(new BaseLayer(ranges));
            this.previous = null;
        }
    }

    public synchronized boolean isTainted() {
        return this.getTaintRanges().isTainted();
    }

    @Override
    public IASTaintInformationable deleteWithShift(int start, int end) {
        IASTaintInformation copied = this.copy();
        copied.appendLayer(new DeleteLayer(start, end));
        return copied;
    }

    @Override
    public IASTaintInformationable clearTaint(int start, int end) {
        return this.replaceTaint(start, end, new IASTaintInformation(end - start));
    }

    @Override
    public IASTaintInformationable replaceTaint(int start, int end, IASTaintInformationable taintInformation) {
        IASTaintInformation copied = this.copy();
        copied.appendLayer(new DeleteLayer(start, end));
        copied.appendLayer(new InsertLayer(start, (IASTaintInformation) taintInformation));
        return copied;
    }

    @Override
    public IASTaintInformationable insertWithShift(int offset, IASTaintInformationable taintInformation) {
        IASTaintInformation copied = this.copy();
        copied.appendLayer(new InsertLayer(offset, (IASTaintInformation) taintInformation));
        return copied;
    }

    @Override
    public IASTaintInformation copy() {
        return new IASTaintInformation(this);
    }

    @Override
    public IASTaintInformationable reversed() {
        IASTaintRanges ranges = this.getTaintRanges();
        ranges.reversed();
        return new IASTaintInformation(ranges);
    }

    @Override
    public synchronized IASTaintMetadata getTaint(int position) {
        return this.getTaintRanges().getTaintFor(position);
    }

    @Override
    public IASTaintInformationable setTaint(int start, int end, IASTaintMetadata taint) {
        this.appendLayer(new DeleteLayer(start, end));
        this.appendLayer(new InsertLayer(start, new IASTaintInformation(end - start, Arrays.asList(new IASTaintRange(0, end - start, taint)))));
        return this;
    }

    @Override
    public IASTaintInformationable resize(int length) {
        IASTaintRanges ranges = this.getTaintRanges();
        ranges.resize(length);
        return new IASTaintInformation(ranges);
    }

    @Override
    public IASTaintInformationable slice(int start, int end) {
        IASTaintInformation sliced = this.copy();
        sliced.appendLayer(new DeleteLayer(end));
        sliced.appendLayer(new DeleteLayer(0, start));
        return sliced;
    }

    public synchronized boolean isTaintedAt(int index) {
        return this.getTaintRanges().isTaintedAt(index);
    }

    public int getLength() {
        return this.getTaintRanges().getLength();
    }
}
