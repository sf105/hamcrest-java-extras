package test.hamcrest.extras;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.extras.JsonPathMatcher.hasJsonElement;
import static org.hamcrest.extras.JsonPathMatcher.hasJsonPath;
import static test.hamcrest.extras.AbstractMatcherTestSupport.assertMatches;
import static test.hamcrest.extras.AbstractMatcherTestSupport.assertMismatchDescription;

public class JsonPathMatchersTests {
    public final static String JSON_SRC =
            "{ 'toplevel': 'a top level' }";


    @Test public void
    matches_one_level_path() {
        assertMatches("top level field", hasJsonPath("toplevel"), JSON_SRC);
        assertMismatchDescription("no path matching 'nottoplevel'", hasJsonPath("nottoplevel"), JSON_SRC);
    }

    @Test public void
    matches_contents_of_one_level_path() {
        assertMatches("top level field", hasJsonElement("toplevel", equalTo("a top level")), JSON_SRC);
        assertMismatchDescription(
                "element at toplevel was \"a top level\"",
                hasJsonElement("toplevel", equalTo("bad")),
                JSON_SRC);
    }

    @Test public void
    rejects_invalid_json() {
        assertMismatchDescription(
                "com.google.gson.stream.MalformedJsonException: Expected EOF at line 1 column 5",
                hasJsonPath("something"),
                "not really json");
    }

}
