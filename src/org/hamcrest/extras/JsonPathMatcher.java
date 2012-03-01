package org.hamcrest.extras;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.hamcrest.*;

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
        return new JsonPathMatcher(jsonPath, Matchers.any(JsonElement.class));
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
            protected String featureValueOf(JsonElement actual) {
                return actual.getAsString();
            }
        };
    }

    private static class FindElement implements Condition.Step<JsonElement, JsonElement> {
        private final String jsonPath;

        public FindElement(String jsonPath) {
            this.jsonPath = jsonPath;
        }

        public Condition<JsonElement> apply(JsonElement root, Description mismatch) {
            Condition<JsonElement> current = matched(root, mismatch);
            for (String segment : jsonPath.split("\\.")) {
                current = current
                            .and(asObject(segment))
                            .and(nextSegment(segment));
            }
            return current;
        }
        
        private Condition.Step<JsonElement, JsonObject> asObject(final String segment) {
            return new Condition.Step<JsonElement, JsonObject>() {
                public Condition<JsonObject> apply(JsonElement element, Description mismatch) {
                    if (element.isJsonObject()) {
                        return matched(element.getAsJsonObject(), mismatch);
                    }
                    mismatch.appendText("no object at '").appendText(segment).appendText("'");
                    return notMatched();
                }
            };
        }
        private Condition.Step<JsonObject, JsonElement> nextSegment(final String segment) {
            return new Condition.Step<JsonObject, JsonElement>() {
                public Condition<JsonElement> apply(JsonObject object, Description mismatch) {
                    if (object.has(segment)) {
                        return matched(object.get(segment), mismatch);
                    }
                    mismatch.appendText("missing element '").appendText(segment).appendText("'");
                    return notMatched();
                }
            };
        }
    }
}
