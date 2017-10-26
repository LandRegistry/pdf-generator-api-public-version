package pdfgeneratorapi.pdf_generation;

import java.util.Arrays;
public class Lenders {

  private String name;
  private String name_extra_info;
  private LenderAddressLines[] lenderAddressLines;

  public String getName() {
    return name;
  }

  public String getNameExtraInfo() {
    return name_extra_info;
  }

  public LenderAddressLines[] getAddresses() {
    return lenderAddressLines;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setNameExtraInfo(final String name_extra_info) {
    this.name_extra_info = name_extra_info;
  }

  public void setAddresses(final LenderAddressLines[] lenderAddressLines) {
    this.lenderAddressLines = lenderAddressLines;
  }

}