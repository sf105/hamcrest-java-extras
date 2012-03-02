package org.hamcrest.extras;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.hamcrest.*;

import java.util.ArrayList;

import static org.hamcrest.Matchers.any;
import static org.hamcrest.extras.Condition.matched;
import static org.hamcrest.extras.Condition.notMatched;

/**
* @author Steve Freeman 2012 http://www.hamcrest.com
*/
public class JsonPathMatcher extends TypeSafeDiagnosingMatcher<String> {
    private final String jsonPath;
    private Condition.Step<? super JsonObject, JsonElement> findElement;
    private Matcher<JsonElement> elementContents;

    public JsonPathMatcher(String jsonPath, Matcher<JsonElement> elementContents) {
        this.jsonPath = jsonPath;
        this.findElement = new FindElement(jsonPath);
        this.elementContents = elementContents;
    }

    @Override
    protected boolean matchesSafely(String source, Description mismatch) {
        return parse(source, mismatch)
                .and(findElement)
                .matches(elementContents);
    }


    public void describeTo(Description description) {
       description.appendText("Json with path '").appendText(jsonPath).appendText("'");
    }

    public static Matcher<String> hasJsonPath(final String jsonPath) {
        return new JsonPathMatcher(jsonPath, any(JsonElement.class));
    }

    @SuppressWarnings("unchecked")
    public static Matcher<String> hasJsonElement(final String jsonPath, final Matcher<String> contentsMatcher) {
        return new JsonPathMatcher(jsonPath, elementWith(contentsMatcher, jsonPath));
    }


    private Condition<JsonObject> parse(String source, Description mismatch) {
        try {
            return matched(new JsonParser().parse(source).getAsJsonObject(), mismatch);
        } catch (JsonSyntaxException e) {
            mismatch.appendText(e.getMessage());
        }
        return notMatched();
    }

    private static Matcher<JsonElement> elementWith(Matcher<String> contentsMatcher, String jsonPath) {
        return new FeatureMatcher<JsonElement, String>(contentsMatcher, "element at " + jsonPath, "content") {
            @Override
            protected String featureValueOf(JsonElement actual) { return actual.getAsString(); }
        };
    }

    private static class FindElement implements Condition.Step<JsonElement, JsonElement> {
        private final String jsonPath;

        public FindElement(String jsonPath) {
            this.jsonPath = jsonPath;
        }

        public Condition<JsonElement> apply(JsonElement root, Description mismatch) {
            Condition<JsonElement> current = matched(root, mismatch);
            for (Segment segment : split(jsonPath)) {
                current = current.and(segment);
            }
            return current;
        }
    }
    
    private static Iterable<Segment> split(String jsonPath) {
        final ArrayList<Segment> segments = new ArrayList<Segment>();
        for (String pathSegment : jsonPath.split("\\.")) {
            segments.add(new Segment(pathSegment));
        }
        return segments;
    }

    private static class Segment implements Condition.Step<JsonElement, JsonElement> {
        private final String pathSegment;
        public Segment(String pathSegment) {
            this.pathSegment = pathSegment;
        }
        public Condition<JsonElement> apply(JsonElement current, Description mismatch) {
            if (current.isJsonObject()) {
                return nextObject(current, mismatch);
            }                                
            mismatch.appendText("no object at '").appendText(pathSegment).appendText("'");
            return notMatched();
        }

        private Condition<JsonElement> nextObject(JsonElement current, Description mismatch) {
            final JsonObject object = current.getAsJsonObject();
            if (!object.has(pathSegment)) {
                mismatch.appendText("missing element '").appendText(pathSegment).appendText("'");
                return notMatched();
            }
            return matched(object.get(pathSegment), mismatch);
        }
    }
}
