package de.tubs.cs.ias.asm_test.taintaware.lazybasic;

import de.tubs.cs.ias.asm_test.taintaware.lazybasic.operation.BaseLayer;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRanges;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintSource;

import java.util.*;

public class IASTaintInformation {
    private static final int THRESHOLD = 30;
    private final List<IASLayer> layers;
    private IASTaintInformation previous;

    public IASTaintInformation(List<IASLayer> layers, IASTaintInformation previous) {
        this.layers = new LinkedList<>();
        this.appendLayers(layers);
        this.previous = previous;
    }

    public IASTaintInformation(BaseLayer baseLayer) {
        this.layers = Collections.singletonList(baseLayer);
    }

    public IASTaintInformation() {
        this.layers = Collections.emptyList();
    }

    private void appendLayers(List<IASLayer> layers) {
        for (IASLayer layer : layers) {
            this.appendLayer(layer);
        }
    }

    private void appendLayer(IASLayer layer) {
        if (this.getLayerDepth() >= THRESHOLD) {
            this.evaluate();
        }
        this.layers.add(layer);
    }

    public List<IASTaintRange> getTaintRanges() {
        return this.evaluate();
    }

    private int getLayerDepth() {
        return this.getOwnLayerCount() + this.getPreviousLayerCount();
    }

    private int getPreviousLayerCount() {
        return this.previous == null ? 0 : this.previous.getLayerDepth();
    }

    private int getOwnLayerCount() {
        return this.layers == null ? 0 : this.layers.size();
    }

    private List<IASTaintRange> getPreviousRanges() {
        if (this.previous == null) {
            return new ArrayList<>(0);
        }
        return this.previous.getTaintRanges();
    }

    private boolean isBase() {
        return this.layers.size() == 1 && this.layers.get(0) instanceof BaseLayer;
    }

    private List<IASTaintRange> evaluate() {
        if (this.isBase()) {
            return ((BaseLayer)this.layers.get(0)).getBase();
        }

        List<IASTaintRange> previousRanges = this.getPreviousRanges();
        for (IASLayer layer : this.layers) {
            previousRanges = layer.apply(previousRanges);
        }
        this.layers.clear();
        this.layers.add(new BaseLayer(previousRanges));
        this.previous = null;
        return previousRanges;
    }

    public boolean isTainted() {
        return this.getTaintRanges().size() > 0;
    }

    public IASTaintSource getTaintFor(int position) {
        return new IASTaintRanges(this.getTaintRanges()).getTaintFor(position);
    }

    public boolean isTaintedAt(int index) {
        return new IASTaintRanges(this.getTaintRanges()).isTaintedAt(index);
    }
}
