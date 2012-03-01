package org.hamcrest.extras;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import static org.hamcrest.Matchers.any;
import static org.hamcrest.extras.Condition.matched;
import static org.hamcrest.extras.Condition.notMatched;

/**
* @author Steve Freeman 2012 http://www.hamcrest.com
*/
public class JsonPathMatcher extends TypeSafeDiagnosingMatcher<String> {
    private final String jsonPath;
    private Condition.Step<? super JsonObject, JsonElement> findElement;
    private Condition.Match<JsonElement> elementContents;

    public JsonPathMatcher(String jsonPath, Matcher<String> contentsMatcher) {
        this.jsonPath = jsonPath;
        this.findElement = new FindElement(jsonPath);
        this.elementContents = new ElementContentsMatch(jsonPath, contentsMatcher);
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
        return new JsonPathMatcher(jsonPath, any(String.class));
    }

    @SuppressWarnings("unchecked")
    public static Matcher<String> hasJsonElement(final String jsonPath, final Matcher<?> contentsMatcher) {
        return new JsonPathMatcher(jsonPath, (Matcher<String>) contentsMatcher);
    }


    private Condition<JsonObject> parse(String source, Description mismatch) {
        try {
            return matched(new JsonParser().parse(source).getAsJsonObject(), mismatch);
        } catch (JsonSyntaxException e) {
            mismatch.appendText(e.getMessage());
        }
        return notMatched();
    }

    private static class ElementContentsMatch implements Condition.Match<JsonElement> {
        private final String jsonPath;
        private final Matcher<String> contentsMatcher;

        public ElementContentsMatch(String jsonPath, Matcher<String> contentsMatcher) {
            this.jsonPath = jsonPath;
            this.contentsMatcher = contentsMatcher;
        }

        public boolean apply(JsonElement element, Description mismatch) {
            final String item = element.getAsString();
            if (!contentsMatcher.matches(item)) {
                mismatch.appendText("element at ").appendText(jsonPath).appendText(" ");
                contentsMatcher.describeMismatch(item, mismatch);
                return false;
            }
            return true;
        }
    }

    private static class FindElement implements Condition.Step<JsonObject, JsonElement> {
        private final String jsonPath;

        public FindElement(String jsonPath) {
            this.jsonPath = jsonPath;
        }

        public Condition<JsonElement> apply(JsonObject root, Description mismatch) {
            if (root.has(jsonPath)) {
                return matched(root.get(jsonPath), mismatch);
            }
            mismatch.appendText("no path matching '").appendText(jsonPath).appendText("'");
            return notMatched();
        }
    }
}
