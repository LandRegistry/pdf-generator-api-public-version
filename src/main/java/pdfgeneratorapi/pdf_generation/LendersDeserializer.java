package pdfgeneratorapi.pdf_generation;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class LendersDeserializer implements JsonDeserializer<Lenders> {

    @Override
    public Lenders deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
        throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();

        final String name = jsonObject.get("name").getAsString();

        final String name_extra_info = jsonObject.get("name_extra_info").getAsString();

        final LenderAddressLines[] addressLines = context.deserialize(jsonObject.get("addresses"), LenderAddressLines[].class);

        final Lenders lenders = new Lenders();
        lenders.setName(name);
        lenders.setNameExtraInfo(name_extra_info);
        lenders.setAddresses(addressLines);
        return lenders;
    }
}