package org.hamcrest.extras;

import com.google.gson.*;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.ArrayList;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
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
        final StringBuilder pathSoFar = new StringBuilder();
        for (String pathSegment : jsonPath.split("\\.")) {
            pathSoFar.append(pathSegment);
            final int leftBracket = pathSegment.indexOf('[');
            if (leftBracket == -1) {
                segments.add(new Segment(pathSegment, pathSoFar.toString()));
            } else {
                segments.add(new Segment(pathSegment.substring(0, leftBracket), pathSoFar.toString()));
                segments.add(new Segment(pathSegment.substring(
                        leftBracket + 1, pathSegment.length() - 1), pathSoFar.toString()));
            }
        }
        return segments;
    }

    public static class Segment implements Condition.Step<JsonElement, JsonElement> {
        private final String pathSegment;
        private final String pathSoFar;

        public Segment(String pathSegment, String pathSoFar) {
            this.pathSegment = pathSegment;
            this.pathSoFar = pathSoFar;
        }
        public Condition<JsonElement> apply(JsonElement current, Description mismatch) {
            if (current.isJsonObject()) {
                return nextObject(current, mismatch);
            }
            if (current.isJsonArray()) {
                return nextArrayElement(current, mismatch);
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

        private Condition<JsonElement> nextArrayElement(JsonElement current, Description mismatch) {
            final JsonArray array = current.getAsJsonArray();
            final int index = parseInt(pathSegment);
            if (index > array.size()) {
                mismatch.appendText(format("index %d too large in ", index))
                        .appendText(pathSoFar);
                return notMatched();
            }
            return matched(array.get(index), mismatch);
        }
    }
}
