package pdfgeneratorapi.pdf_generation;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class ProprietorsDeserializer implements JsonDeserializer<Proprietors> {

  @Override
  public Proprietors deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
      throws JsonParseException {
    final JsonObject jsonObject = json.getAsJsonObject();

    final String name = jsonObject.get("name").getAsString();

    final String name_extra_info = jsonObject.get("name_extra_info").getAsString();

    final AddressLines[] addressLines = context.deserialize(jsonObject.get("addresses"), AddressLines[].class);

    final Proprietors proprietors = new Proprietors();
    proprietors.setName(name);
    proprietors.setNameExtraInfo(name_extra_info);
    proprietors.setAddresses(addressLines);
    return proprietors;
  }
}