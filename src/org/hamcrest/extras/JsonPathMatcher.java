package org.hamcrest.extras;

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
            JsonObject jsonObject = new JsonParser().parse(source).getAsJsonObject();
            boolean pathFound = jsonObject.has(jsonPath);
            if (!pathFound) {
                mismatch.appendText("no path matching '").appendText(jsonPath).appendText("'");
                return false;
            }

            final String item = jsonObject.get(jsonPath).getAsString();
            if (!contentsMatcher.matches(item)) {
                mismatch.appendText("element at ").appendText(jsonPath).appendText(" ");
                contentsMatcher.describeMismatch(item, mismatch);
                return false;
            }
            return true;
        } catch (JsonSyntaxException e) {
            mismatch.appendText(e.getMessage());
        }
        return false;
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
