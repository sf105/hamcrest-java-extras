package test.hamcrest.extras;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.hamcrest.StringDescription;
import org.hamcrest.extras.Condition;
import org.hamcrest.extras.JsonPathMatcher;
import org.junit.Test;

import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Steve Freeman 2012 http://www.hamcrest.com
 */
public class JsonPathMatcherSegmentTest {
    final StringDescription description = new StringDescription();
    private final JsonElement child = new JsonPrimitive("a child");

    @Test public void
    returns_the_child_of_a_JsonObject() {
        final JsonObject parent = new JsonObject();
        parent.add("child", child);

        assertTrue("child", next(parent, "child").matches(sameInstance(child)));
    }

    @Test public void
    returns_not_matched_for_wrong_property() {
        final JsonObject parent = new JsonObject();

        assertFalse("not child", next(parent, "child").matches(any(JsonElement.class)));
    }

    @Test public void
    returns_the_element_of_a_JsonArray() {
        final JsonArray parent = new JsonArray();
        parent.add(child);

        assertTrue("first element", next(parent, "0").matches(sameInstance(child)));
    }

    @Test public void
    returns_not_matched_for_array_out_of_bounds() {
        final JsonArray parent = new JsonArray();
        parent.add(child);

        assertFalse("out of bounds", next(parent, "2").matches(any(JsonElement.class)));
    }

    @Test public void
    returns_not_matched_for_array_index_notANumber() {
        final JsonArray parent = new JsonArray();

        assertFalse("not a number", next(parent, "xx").matches(any(JsonElement.class)));
    }

    private Condition<JsonElement> next(JsonElement parent, String pathSegment) {
        return new JsonPathMatcher.Segment(pathSegment, "path so far").apply(parent, description);
    }
}
