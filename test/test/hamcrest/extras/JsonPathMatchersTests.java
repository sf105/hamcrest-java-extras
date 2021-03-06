package test.hamcrest.extras;

import org.junit.Test;

import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.extras.JsonPathMatcher.hasJsonElement;
import static org.hamcrest.extras.JsonPathMatcher.hasJsonPath;
import static test.hamcrest.extras.AbstractMatcherTestSupport.assertMatches;
import static test.hamcrest.extras.AbstractMatcherTestSupport.assertMismatchDescription;

public class JsonPathMatchersTests {
    public final static String JSON_SRC =
            "{ 'onelevel': 'a top level'," +
              " 'anobject': { 'inobject': 'inobjectvalue' }," +
              " 'anarray': [ 'zero', 'one', { 'two': 'deep' }]," +
              " 'withnull' : null }";


    @Test public void
    matches_one_level() {
        assertMatches("top level field", hasJsonPath("onelevel"), JSON_SRC);
        assertMismatchDescription("missing element at 'notonelevel'", hasJsonPath("notonelevel"), JSON_SRC);
    }

    @Test public void
    matches_contents_of_one_level() {
        assertMatches("top level field", hasJsonElement("onelevel", equalTo("a top level")), JSON_SRC);
        assertMismatchDescription(
                "was \"a top level\"",
                hasJsonElement("onelevel", equalTo("bad")),
                JSON_SRC);
        assertMismatchDescription(
                "element was <{\"inobject\":\"inobjectvalue\"}>",
                hasJsonElement("anobject", any(String.class)),
                JSON_SRC);
        assertMismatchDescription(
                "element was <null>",
                hasJsonElement("withnull", any(String.class)),
                JSON_SRC);

    }

    @Test public void
    matches_two_level_object() {
        assertMismatchDescription(
                "missing element at 'anobject.missing'",
                hasJsonElement("anobject.missing", any(String.class)),
                JSON_SRC);

        assertMismatchDescription(
                "was \"inobjectvalue\"",
                hasJsonElement("anobject.inobject", equalTo("wrong")),
                JSON_SRC);
        assertMatches("second level field", hasJsonElement("anobject.inobject", equalTo("inobjectvalue")), JSON_SRC);
    }

    @Test public void
    matches_array_element() {
        assertMatches("array value", hasJsonElement("anarray[1]", equalTo("one")), JSON_SRC);
        assertMismatchDescription(
                "index 10 too large in anarray[10]",
                hasJsonElement("anarray[10]", any(String.class)), JSON_SRC);
        assertMismatchDescription(
                "index not a number in anarray[xx]",
                hasJsonElement("anarray[xx]", any(String.class)), JSON_SRC);
        assertMismatchDescription(
                "was \"one\"",
                hasJsonElement("anarray[1]", equalTo("wrong")),
                JSON_SRC);
    }

    @Test public void
    matches_an_element_in_an_object_in_an_array() {
        assertMatches("array value", hasJsonElement("anarray[2].two", equalTo("deep")), JSON_SRC);
        assertMismatchDescription(
                "missing element at 'anarray[2].wrong'",
                hasJsonElement("anarray[2].wrong", any(String.class)),
                JSON_SRC);
        assertMismatchDescription(
                "was \"deep\"",
                hasJsonElement("anarray[2].two", equalTo("wrong")),
                JSON_SRC);
    }

    @Test public void
    rejects_invalid_json() {
        assertMismatchDescription(
                "com.google.gson.stream.MalformedJsonException: Expected EOF at line 1 column 5",
                hasJsonPath("something"),
                "not really json");
    }

    @Test public void
    rejects_empty_json() {
        assertMismatchDescription(
                "missing element at 'something'",
                hasJsonPath("something"),
                "{}");
    }

}
