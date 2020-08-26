package de.tubs.cs.ias.asm_test.taintaware.lazybasic;

import de.tubs.cs.ias.asm_test.config.Configuration;
import de.tubs.cs.ias.asm_test.taintaware.lazybasic.operation.BaseLayer;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintInformationable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRanges;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintSource;
import de.tubs.cs.ias.asm_test.utils.stats.Statistics;

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

    public IASTaintInformation(BaseLayer baseLayer) {
        this.layers = new ArrayList<>(Collections.singletonList(baseLayer));
    }

    public IASTaintInformation() {
        this.layers = new ArrayList<>();
        if (Configuration.getConfiguration().collectStats()) {
            Statistics.INSTANCE.incrementLazyTaintInformationCreated();
        }
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

    public synchronized List<IASTaintRange> getTaintRanges() {
        return new ArrayList<>(this.evaluate());
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

    private synchronized List<IASTaintRange> getPreviousRanges() {
        if (this.previous == null) {
            return new ArrayList<>(0);
        }
        return this.previous.getTaintRanges();
    }

    private synchronized boolean isBase() {
        return this.layers.size() == 1 && this.layers.get(0) instanceof BaseLayer;
    }

    private synchronized List<IASTaintRange> evaluate() {
        if (this.isBase()) {
            return ((BaseLayer) this.layers.get(0)).getBase();
        }

        if (Configuration.getConfiguration().collectStats()) {
            Statistics.INSTANCE.incrementLazyTaintInformationEvaluated();
        }

        List<IASTaintRange> previousRanges = this.getPreviousRanges();
        for (IASLayer layer : this.layers) {
            previousRanges = layer.apply(previousRanges);
        }
        if (Configuration.getConfiguration().useCaching()) {
            this.cache(previousRanges);
        }
        return previousRanges;
    }

    private synchronized void cache(List<IASTaintRange> ranges) {
        if (Configuration.getConfiguration().useCaching()) {
            this.layers.clear();
            this.layers.add(new BaseLayer(ranges));
            this.previous = null;
        }
    }

    public synchronized boolean isTainted() {
        return this.getTaintRanges().size() > 0;
    }

    public synchronized IASTaintSource getTaintFor(int position) {
        return new IASTaintRanges(this.getTaintRanges()).getTaintFor(position);
    }

    public synchronized boolean isTaintedAt(int index) {
        return new IASTaintRanges(this.getTaintRanges()).isTaintedAt(index);
    }
}
