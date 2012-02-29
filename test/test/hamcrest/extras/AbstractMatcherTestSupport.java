/*  Copyright (c) 2000-2006 hamcrest.org
 */
package test.hamcrest.extras;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Test;

import static org.junit.Assert.*;

@SuppressWarnings("UnusedDeclaration")
public abstract class AbstractMatcherTestSupport  {

    // Create an instance of the Matcher so some generic safety-net tests can be run on it.
    protected abstract Matcher<?> createMatcher();

    @Test public void
    isNullSafe() {
        createMatcher().matches(null);
    }

    @Test public void
    copesWithUnknownTypes() {
        createMatcher().matches(new UnknownType());
    }


    public static <T> void assertMatches(String message, Matcher<? super T> c, T arg) {
        assertTrue(message, c.matches(arg));
    }

    public static <T> void assertDoesNotMatch(String message, Matcher<? super T> c, T arg) {
        assertFalse(message, c.matches(arg));
    }

    public static void assertDescription(String expected, Matcher<?> matcher) {
        Description description = new StringDescription();
        description.appendDescriptionOf(matcher);
        assertEquals("Expected description", expected, description.toString());
    }

    public static <T> void assertMismatchDescription(String expected, Matcher<? super T> matcher, T arg) {
        Description description = new StringDescription();
        assertFalse("Precondition: Matcher should not match item.", matcher.matches(arg));
        matcher.describeMismatch(arg, description);
        assertEquals("Expected mismatch description", expected, description.toString());
    }

    public static class UnknownType {
    }

}
