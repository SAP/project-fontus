package de.tubs.cs.ias.asm_test.taintaware.range.testHelper;

/**
 * Created by d059349 on 16.07.17.
 */
@SuppressWarnings("ALL")
// David: As I didn't write this Code and don't want to mess with it I suppressed the warnings.
// TODO: Work out whether we can adapt it to the style of the remaining project?
public class CatchException {

    public interface ThrowingFunction {
        void doAction();
    }

    public static Throwable catchException(ThrowingFunction func) {
        Throwable thrown = null;

        try {
            func.doAction();
        } catch (Throwable err) {
            thrown = err;
        }

        if (thrown == null) {
            throw new RuntimeException("No exception thrown!");
        }

        return thrown;
    }

}
