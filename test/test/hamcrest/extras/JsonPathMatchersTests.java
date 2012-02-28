package test.hamcrest.extras;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;

import static test.hamcrest.extras.AbstractMatcherTestSupport.assertMatches;

public class JsonPathMatchersTests {
    public final static String JSON_SRC =
            "{ 'toplevel': 'a top level' }";


    @Test public void
    matches_one_level_path() {
        assertMatches("top level field", hasJsonPath("toplevel"), JSON_SRC);
    }

    private Matcher<String> hasJsonPath(final String jsonPath) {
        return new TypeSafeDiagnosingMatcher<String>() {
            @Override
            protected boolean matchesSafely(String source, Description mismatchDescription) {
                JsonElement json = new JsonParser().parse(source);
                return json.getAsJsonObject().has(jsonPath);
            }

            public void describeTo(Description description) {
               description.appendText("Json with path '").appendText(jsonPath).appendText("'");
            }
        };
    }

}
