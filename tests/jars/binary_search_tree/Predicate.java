/**
 * A predicate tests whether a condition holds
 */
@FunctionalInterface
public interface Predicate {
    /**
     * Does the condition hold for the provided value
     * @param value The predicate's parameter
     * @return Does the predicate hold?
     */
    boolean apply(int value);
}
