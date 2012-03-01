package org.hamcrest.extras;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

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

    public abstract boolean matches(Matcher<T> match);
    public abstract <U> Condition<U> and(Step<? super T, U> mapping);

    public static <T> Condition<T> notMatched() {
        return new Condition<T>() {
            @Override public boolean matches(Matcher<T> match) { return false; }

            @Override public <U> Condition<U> and(Step<? super T, U> mapping) {
                return notMatched();
            }
        };
    }

    public static <T> Condition<T> matched(final T theValue, final Description mismatch) {
        return new Condition<T>() {
            @Override
            public boolean matches(Matcher<T> matcher) {
                if (!matcher.matches(theValue)) {
                    matcher.describeMismatch(theValue, mismatch);
                    return false;
                }
                return true;
            }

            @Override
            public <U> Condition<U> and(Step<? super T, U> next) {
                return next.apply(theValue, mismatch);
            }
        };
    }
}
