package pdfgeneratorapi.pdf_generation;

public class TitleSummary {

  public String number;
  public String tenure;
  public String is_caution_title;
  public String ppi_data;
  public String[] address_lines;
  public Proprietors[] proprietors;
  public Lenders[] lenders;
  public String summary_heading;
  public String proprietor_type_heading;
  public Receipt receipt;
  public String last_changed_readable;


  public Proprietors[] getProprietors() {
    return proprietors;
  }

  public String getLastChanged() {
    return last_changed_readable;
  }

  public Receipt getReceipt() {
    return receipt;
  }

  public Lenders[] getLenders() {
    return lenders;
  }

  public String getTitleNumber() {
    return number;
  }

  public String getTenure() {
    return tenure;
  }

  public String getIsCautionTitle() {
    return is_caution_title;
  }

  public String getPpiData() {
    return ppi_data;
  }

  public String[] getPropertyAddressLines() {
    return address_lines;
  }

  public String getSummaryHeading() {
    return summary_heading;
  }

  public String getProprietorTypeHeading() {
    return proprietor_type_heading;
  }

}