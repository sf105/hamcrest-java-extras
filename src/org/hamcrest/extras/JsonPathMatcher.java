package org.hamcrest.extras;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import static org.hamcrest.Matchers.any;

/**
* @author Steve Freeman 2012 http://www.hamcrest.com
*/
public class JsonPathMatcher extends TypeSafeDiagnosingMatcher<String> {
    private final String jsonPath;
    private final Matcher<?> contentsMatcher;

    public JsonPathMatcher(String jsonPath, Matcher<String> contentsMatcher) {
        this.jsonPath = jsonPath;
        this.contentsMatcher = contentsMatcher;
    }

    @Override
    protected boolean matchesSafely(String source, Description mismatch) {
        try {
            JsonObject root = new JsonParser().parse(source).getAsJsonObject();
            final JsonElement element = root.get(jsonPath);
            if (element == null) {
                mismatch.appendText("no path matching '").appendText(jsonPath).appendText("'");
                return false;
            }

            return matchesContentsOf(mismatch, element);
        } catch (JsonSyntaxException e) {
            mismatch.appendText(e.getMessage());
        }
        return false;
    }

    private boolean matchesContentsOf(Description mismatch, JsonElement element) {
        final String item = element.getAsString();
        if (!contentsMatcher.matches(item)) {
            mismatch.appendText("element at ").appendText(jsonPath).appendText(" ");
            contentsMatcher.describeMismatch(item, mismatch);
            return false;
        }
        return true;
    }

    public void describeTo(Description description) {
       description.appendText("Json with path '").appendText(jsonPath).appendText("'");
    }

    public static Matcher<String> hasJsonPath(final String jsonPath) {
        return new JsonPathMatcher(jsonPath, any(String.class));
    }

    public static Matcher<String> hasJsonElement(final String jsonPath, final Matcher<?> contentsMatcher) {
        return new JsonPathMatcher(jsonPath, (Matcher<String>) contentsMatcher);
    }

}
