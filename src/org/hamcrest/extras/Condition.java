package org.hamcrest.extras;

import org.hamcrest.Description;

/**
 * Based on https://github.com/npryce/maybe-java
 *
 *
 * @author Steve Freeman 2012 http://www.hamcrest.com
 */

public abstract class Condition<T> {
    public interface Step<I, O> {
        Condition<O> apply(I value, Description mismatch);
    }

    public interface Match<I> {
        boolean apply(I value, Description mismatch);
    }

    public abstract boolean matches(Match<T> match);
    public abstract <U> Condition<U> and(Step<? super T, U> mapping);

    public static <T> Condition<T> notMatched() {
        return new Condition<T>() {
            @Override public boolean matches(Match<T> match) { return false; }

            @Override public <U> Condition<U> and(Step<? super T, U> mapping) {
                return notMatched();
            }
        };
    }

    public static <T> Condition<T> matched(final T theValue, final Description mismatch) {
        return new Condition<T>() {
            @Override
            public boolean matches(Match<T> match) {
                return match.apply(theValue, mismatch);
            }

            @Override
            public <U> Condition<U> and(Step<? super T, U> next) {
                return next.apply(theValue, mismatch);
            }
        };
    }

}
