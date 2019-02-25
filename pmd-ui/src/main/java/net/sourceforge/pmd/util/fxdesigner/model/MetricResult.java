/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.util.fxdesigner.model;

import java.util.AbstractMap.SimpleEntry;

import net.sourceforge.pmd.lang.metrics.MetricKey;

/**
 * @author Clément Fournier
 * @since 6.0.0
 */
public class MetricResult {

    private final SimpleEntry<MetricKey<?>, Double> simpleEntry;


    public MetricResult(MetricKey<?> key, Double value) {
        simpleEntry = new SimpleEntry<>(key, value);
    }


    public MetricKey<?> getKey() {
        return simpleEntry.getKey();
    }


    public Double getValue() {
        return simpleEntry.getValue();
    }
}
