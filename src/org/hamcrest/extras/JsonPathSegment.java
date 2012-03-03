package org.hamcrest.extras;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.hamcrest.Description;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static org.hamcrest.extras.Condition.matched;
import static org.hamcrest.extras.Condition.notMatched;

/**
* @author Steve Freeman 2012 http://www.hamcrest.com
*/
public class JsonPathSegment implements Condition.Step<JsonElement, JsonElement> {
    private final String pathSegment;
    private final String pathSoFar;

    public JsonPathSegment(String pathSegment, String pathSoFar) {
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
        mismatch.appendText("no value at '").appendText(pathSoFar).appendText("'");
        return notMatched();
    }

    private Condition<JsonElement> nextObject(JsonElement current, Description mismatch) {
        final JsonObject object = current.getAsJsonObject();
        if (!object.has(pathSegment)) {
            mismatch.appendText("missing element at '").appendText(pathSoFar).appendText("'");
            return notMatched();
        }
        return matched(object.get(pathSegment), mismatch);
    }

    private Condition<JsonElement> nextArrayElement(JsonElement current, Description mismatch) {
        final JsonArray array = current.getAsJsonArray();
        try {
            return arrayElementIn(array, mismatch);
        } catch (NumberFormatException e) {
            mismatch.appendText("index not a number in ").appendText(pathSoFar);
            return notMatched();
        }
    }

    private Condition<JsonElement> arrayElementIn(JsonArray array, Description mismatch) {
        final int index = parseInt(pathSegment);
        if (index > array.size()) {
            mismatch.appendText(format("index %d too large in ", index)).appendText(pathSoFar);
            return notMatched();
        }
        return matched(array.get(index), mismatch);
    }
}
