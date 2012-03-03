package org.hamcrest.extras;

import com.google.gson.*;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

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
        this.findElement = findElementStep(jsonPath);
        this.elementContents = elementContents;
    }

    @Override
    protected boolean matchesSafely(String source, Description mismatch) {
        return parse(source, mismatch)
                .and(findElement)
                .matching(elementContents);
    }


    public void describeTo(Description description) {
       description.appendText("Json with path '").appendText(jsonPath).appendText("'")
                  .appendDescriptionOf(elementContents);
    }

    @Factory
    public static Matcher<String> hasJsonPath(final String jsonPath) {
        return new JsonPathMatcher(jsonPath, any(JsonElement.class));
    }

    @Factory
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

    private static Matcher<JsonElement> elementWith(final Matcher<String> contentsMatcher, final String jsonPath) {
        return new TypeSafeDiagnosingMatcher<JsonElement>() {
            @Override
            protected boolean matchesSafely(JsonElement element, Description mismatch) {
                return jsonPrimitive(element, mismatch).matching(contentsMatcher);
            }

            private Condition<String> jsonPrimitive(JsonElement element, Description mismatch) {
                if (element.isJsonPrimitive()) {
                    return matched(element.getAsJsonPrimitive().getAsString(), mismatch);
                }
                mismatch.appendText("element was ").appendValue(element);
                return notMatched();
            }

            public void describeTo(Description description) {
                description.appendText("element at ").appendText(jsonPath).appendDescriptionOf(contentsMatcher);
            }
        };
    }

    private static Condition.Step<JsonElement, JsonElement> findElementStep(final String jsonPath) {
        return new Condition.Step<JsonElement, JsonElement>() {
            public Condition<JsonElement> apply(JsonElement root, Description mismatch) {
                Condition<JsonElement> current = matched(root, mismatch);
                for (JsonPathSegment nextSegment : split(jsonPath)) {
                    current = current.then(nextSegment);
                }
                return current;
            }
        };
    }


    private static Iterable<JsonPathSegment> split(String jsonPath) {
        final ArrayList<JsonPathSegment> segments = new ArrayList<JsonPathSegment>();
        final StringBuilder pathSoFar = new StringBuilder();
        for (String pathSegment : jsonPath.split("\\.")) {
            pathSoFar.append(pathSegment);
            final int leftBracket = pathSegment.indexOf('[');
            if (leftBracket == -1) {
                segments.add(new JsonPathSegment(pathSegment, pathSoFar.toString()));
            } else {
                segments.add(new JsonPathSegment(pathSegment.substring(0, leftBracket), pathSoFar.toString()));
                segments.add(new JsonPathSegment(pathSegment.substring(
                        leftBracket + 1, pathSegment.length() - 1), pathSoFar.toString()));
            }
            pathSoFar.append(".");
        }
        return segments;
    }
}
