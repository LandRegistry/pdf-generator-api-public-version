package pdfgeneratorapi.pdf_generation;

import java.util.Arrays;
public class Proprietors {

  public String name;
  public String name_extra_info;
  public AddressLines[] addressLines;

  public String getName() {
    return name;
  }

  public String getNameExtraInfo() {
    return name_extra_info;
  }

  public AddressLines[] getAddresses() {
    return addressLines;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setNameExtraInfo(final String name_extra_info) {
    this.name_extra_info = name_extra_info;
  }

  public void setAddresses(final AddressLines[] addressLines) {
    this.addressLines = addressLines;
  }

}