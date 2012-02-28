package test.hamcrest.extras;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;

import static test.hamcrest.extras.AbstractMatcherTestSupport.assertMatches;
import static test.hamcrest.extras.AbstractMatcherTestSupport.assertMismatchDescription;
import static test.hamcrest.extras.JsonPathMatchersTests.JsonPathMatcher.hasJsonPath;

public class JsonPathMatchersTests {
    public final static String JSON_SRC =
            "{ 'toplevel': 'a top level' }";


    @Test public void
    matches_one_level_path() {
        assertMatches("top level field", hasJsonPath("toplevel"), JSON_SRC);
        assertMismatchDescription("no path matching 'nottoplevel'", hasJsonPath("nottoplevel"), JSON_SRC);
    }

    @Test public void
    rejects_invalid_json() {
        assertMismatchDescription(
                "com.google.gson.stream.MalformedJsonException: Expected EOF at line 1 column 5",
                hasJsonPath("something"),
                "not really json");
    }

    public static class JsonPathMatcher extends TypeSafeDiagnosingMatcher<String> {
        private final String jsonPath;

        public JsonPathMatcher(String jsonPath) {
            this.jsonPath = jsonPath;
        }

        @Override
        protected boolean matchesSafely(String source, Description mismatch) {
            try {
                JsonElement json = new JsonParser().parse(source);
                boolean pathFound = json.getAsJsonObject().has(jsonPath);
                if (!pathFound) {
                    mismatch.appendText("no path matching '").appendText(jsonPath).appendText("'");
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
            return new JsonPathMatcher(jsonPath);
        }
    }
}
