package pdfgeneratorapi.pdf_generation;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class AddressLinesDeserializer implements JsonDeserializer<AddressLines> {

    @Override
    public AddressLines deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
        throws JsonParseException {
    final JsonObject jsonAddressObject = json.getAsJsonObject();

    final JsonArray jsonAddressLines = jsonAddressObject.get("lines").getAsJsonArray();
    final String[] lines = new String[jsonAddressLines.size()];
    for (int i = 0; i < lines.length; i++) {
        final JsonElement jsonAddressLine = jsonAddressLines.get(i);
        lines[i] = jsonAddressLine.getAsString();
    }

    final AddressLines addressLines = new AddressLines();
    addressLines.setLines(lines);
    return addressLines;
    }
}