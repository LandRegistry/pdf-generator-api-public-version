package pdfgeneratorapi.pdf_generation;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.xmp.XMPException;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.pdfa.PdfADocument;
import com.itextpdf.layout.property.TabAlignment;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.color.*;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.kernel.pdf.canvas.draw.*;

import java.io.IOException;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.IOUtils;

import pdfgeneratorapi.views.General;

public class PdfRenderer {

    private static final Logger logger = LoggerFactory.getLogger(General.class);

    // These are settings used in the document.
    Float leading = 18.0f;
    Integer docFontSize = 14;

    public Paragraph h2Heading(String text, PdfFont fonttype) {
        Paragraph headingPara = new Paragraph(text);
        headingPara.setRole(PdfName.H2);
        headingPara.setMargin(0);
        headingPara.setFont(fonttype).setFontSize(docFontSize);
        headingPara.setWidthPercent(33);
        headingPara.setKeepWithNext(true);
        headingPara.setFixedLeading(leading);

        return headingPara;
    }

    public Paragraph genericRightContent(String text, PdfFont font) {
        Paragraph content = new Paragraph(text);
        content.setFontSize(docFontSize).setFont(font);
        content.setWidthPercent(66);
        content.setHorizontalAlignment(HorizontalAlignment.RIGHT);
        content.setMarginTop(-leading);
        content.setFixedLeading(leading);
        content.setKeepTogether(true);

        return content;
    }

    public void receiptRow(Paragraph receiptBlock, String labelText, String receiptData, PdfFont label, PdfFont content) {
        receiptBlock.add(new Text(labelText).setFont(label));
        receiptBlock.add(new Tab());
        receiptBlock.addTabStops(new TabStop(100, TabAlignment.LEFT));
        receiptBlock.add(new Text(receiptData).setFont(content));
    }

    public void peopleBlockSettings(List peopleBlock, PdfFont font) {
        peopleBlock.setFont(font).setFontSize(docFontSize);
        peopleBlock.setWidthPercent(66);
        peopleBlock.setHorizontalAlignment(HorizontalAlignment.RIGHT);
        peopleBlock.setMarginTop(-leading);
    }

    public static ByteArrayOutputStream main(TitleSummary title_summary) throws Exception {

        logger.info("STARTED: Main PDF Rendering");
        // Read the PDF into a byte stream ready to return
        ByteArrayOutputStream pdfOutput = new PdfRenderer().manipulatePdf(title_summary);

        logger.debug("ENDED: Main PDF Rendering");
        return pdfOutput;
    }

    public ByteArrayOutputStream manipulatePdf(TitleSummary title_summary) throws IOException, XMPException {

        logger.info("STARTED: manipulatePdf");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        DeviceRgb borderColor = new DeviceRgb(200, 200, 200);

        InputStream is = getClass().getResourceAsStream("/sRGB_CS_profile.icm");

        PdfADocument pdfDoc = new PdfADocument(new PdfWriter(baos), PdfAConformanceLevel.PDF_A_1A,
                new PdfOutputIntent("Custom", "", "http://www.color.org", "sRGB IEC61966-2.1", is));

        Document document = new Document(pdfDoc, new PageSize(PageSize.A4));

        // Ensure that the PDF is a tagged PDF
        pdfDoc.setTagged();

        // Set PDF metadata
        pdfDoc.getCatalog().setViewerPreferences(new PdfViewerPreferences().setDisplayDocTitle(true));
        pdfDoc.getCatalog().setLang(new PdfString("en-GB"));
        PdfDocumentInfo info = pdfDoc.getDocumentInfo();
        info.setTitle(title_summary.getSummaryHeading() + " " + title_summary.getTitleNumber());
        info.setCreator("HM Land Registry");

        // InputStream gdsFont = getClass().getResourceAsStream("/GDSTransportWebsite.ttf");
        // InputStream gdsFontBold = getClass().getResourceAsStream("/GDSTransportWebsite-Bold.ttf");

        byte[] fontByte = IOUtils.toByteArray(getClass().getResource("/GDSTransportWebsite.ttf").openStream());
        byte[] fontByteBold = IOUtils.toByteArray(getClass().getResource("/GDSTransportWebsite-Bold.ttf").openStream());

        // Embed font (required for PDF/UA)
        PdfFont font = PdfFontFactory.createFont(fontByte, PdfEncodings.WINANSI, true);
        PdfFont boldFont = PdfFontFactory.createFont(fontByteBold, PdfEncodings.WINANSI, true);

        // govuk and fpi header
        Paragraph govuk = new Paragraph();

        byte[] govUkImage = IOUtils.toByteArray(getClass().getResource("/govuk-logo.png").openStream());
        Image logo = new Image(ImageDataFactory.create(govUkImage));
        logo.getAccessibilityProperties().setAlternateDescription("GOV.UK");
        logo.scaleToFit(150f, 150f);
        logo.setMarginRight(10.0f);
        logo.setMarginTop(-10f);
        govuk.add(logo);

        Text fpi = new Text("\nFind property information");
        fpi.setFont(boldFont).setFontSize(15).setRelativePosition(0.0f, 0.0f, 0.0f, 8.0f);
        govuk.add(fpi);

        document.add(govuk);

        // Title summary heading
        Paragraph summaryHeading = new Paragraph(title_summary.getSummaryHeading() + " " + title_summary.getTitleNumber());
        summaryHeading.setFont(boldFont).setFontSize(20);
        summaryHeading.setRole(PdfName.H1);
        summaryHeading.setMarginTop(-10f);
        summaryHeading.setMarginBottom(5f);
        document.add(summaryHeading);

        // Purchased on date and time statement
        if ( title_summary.getReceipt() != null ) {
            Receipt receiptData = title_summary.getReceipt();
            Paragraph purchaseOnStatement = new Paragraph();
            purchaseOnStatement.setFont(boldFont).setFontSize(docFontSize).setFont(font).setRelativePosition(0.0f, 0.0f, 0.0f, 10.0f);
            purchaseOnStatement.add("Purchased on " + receiptData.getDate() + ".");
            document.add(purchaseOnStatement);
        }

        // Fluff about the information changing

        Paragraph informationChangeBlurb = new Paragraph();
        informationChangeBlurb.setFontSize(docFontSize).setFont(font);
        informationChangeBlurb.setFixedLeading(leading);
        informationChangeBlurb.add("This information can change if we receive an application. ");
        informationChangeBlurb.add("This service is unable to tell you whether or not there is an application pending with HM Land Registry.");
        informationChangeBlurb.setKeepTogether(true);
        informationChangeBlurb.setMarginTop(-10f);
        informationChangeBlurb.setMarginBottom(20f);
        document.add(informationChangeBlurb);

        // Property address heading
        Paragraph addressHeading = h2Heading("Address:", font);
        addressHeading.setKeepWithNext(false);
        document.add(addressHeading);

        // Property Address
        Paragraph propertyAddressBlock = genericRightContent("", boldFont);

        String[] propertyAddress = title_summary.getPropertyAddressLines();

        for(String line : propertyAddress){
	        propertyAddressBlock.add(line);
            propertyAddressBlock.add("\n");
        }

        propertyAddressBlock.add("\n");

        document.add(propertyAddressBlock);

        // Owners, Cautioners, Leasholders information
        // Heading
        document.add(h2Heading(title_summary.getProprietorTypeHeading() + ":", font));

        // Addresses
        List propertyPeopleBlock = new List();
        peopleBlockSettings(propertyPeopleBlock, font);

        Proprietors[] propertyPeople = title_summary.getProprietors();

        for(Proprietors person : propertyPeople){
            ListItem item = new ListItem();
            item.setKeepTogether(true);
            item.setListSymbol("");

            Paragraph itemHeading = new Paragraph(person.getName());
            itemHeading.setFixedLeading(leading);
            itemHeading.setRole(PdfName.H3);
            itemHeading.setFont(boldFont);
            itemHeading.setMarginTop(0);
            item.add(itemHeading);

            List addressList = new List();

            AddressLines[] addresses = person.getAddresses();
            for(AddressLines address : addresses){
                String[] linesOfAddress = address.getLines();


                Paragraph addressItemContents = new Paragraph();
                addressItemContents.setFixedLeading(leading);
                addressItemContents.setKeepTogether(true);

                for(String line : linesOfAddress){
	                addressItemContents.add(line);
                    addressItemContents.add("\n").setFont(font);
                }

                if( addresses.length > 1 ) {
                    ListItem addressItem = new ListItem();
                    addressItem.setListSymbol("");
                    addressItem.setMarginBottom(leading);
                    addressItem.add(addressItemContents);
                    addressList.add(addressItem);
                } else {
                    item.add(addressItemContents);
                }
            }

            item.add(addressList);
            propertyPeopleBlock.add(item);
        }

        document.add(propertyPeopleBlock);

        // Lenders, if the register has them

        // Heading
        if ( title_summary.getLenders() != null ) {

            if ( title_summary.getLenders().length > 1 ) {
                document.add(h2Heading("Lenders:", font));
            } else {
                document.add(h2Heading("Lender:", font));
            }

            // Lender names and addresses
            List lenderPeopleBlock = new List();
            peopleBlockSettings(lenderPeopleBlock, font);

            Lenders[] lenderPeople = title_summary.getLenders();

            for(Lenders person : lenderPeople){

                ListItem lenderItem = new ListItem();
                lenderItem.setKeepTogether(true);
                lenderItem.setListSymbol("");

                Paragraph lenderName = new Paragraph(person.getName());
                lenderName.setRole(PdfName.H3);
                lenderName.setFont(boldFont);
                lenderName.setFixedLeading(leading);
                lenderName.setMarginTop(0);
                lenderItem.add(lenderName);

                List lenderAddresses = new List();

                LenderAddressLines[] addresses = person.getAddresses();
                for(LenderAddressLines address : addresses){
                    String[] linesOfAddress = address.getLines();

                    Paragraph lenderAddressItemContents = new Paragraph();
                    lenderAddressItemContents.setKeepTogether(true);
                    lenderAddressItemContents.setFont(font);
                    lenderAddressItemContents.setFixedLeading(leading);

                    for(String line : linesOfAddress){
                        lenderAddressItemContents.add(line);
                        lenderAddressItemContents.add("\n");
                    }

                    if( addresses.length > 1 ) {
                        ListItem lenderAddressItem = new ListItem();
                        lenderAddressItem.setListSymbol("");
                        lenderAddressItem.add(lenderAddressItemContents);
                        lenderAddresses.add(lenderAddressItem);
                    } else {
                        lenderItem.add(lenderAddressItemContents);
                    }
                }

                lenderItem.add(lenderAddresses);
                lenderPeopleBlock.add(lenderItem);
            }

            document.add(lenderPeopleBlock);

        }

        // Tenure
        document.add(h2Heading("Tenure:\n", font));

        document.add(genericRightContent(title_summary.getTenure(), boldFont));

        // PPI Data
        if ( title_summary.getPpiData() != null ) {
            document.add(h2Heading("Price paid:", font));
            document.add(genericRightContent(title_summary.getPpiData(), boldFont));
        }

        // Last changed date
        Paragraph lastChangedHeading = new Paragraph();
        lastChangedHeading.setFont(boldFont).setFontSize(docFontSize).setFont(font);
        lastChangedHeading.add(title_summary.getLastChanged() + ".");
        document.add(lastChangedHeading);

        // Receipt table

        Div receiptContainer = new Div();
        receiptContainer.setKeepTogether(true);

        if ( title_summary.getReceipt() != null ) {
            SolidLine solidLine = new SolidLine();
            solidLine.setColor(borderColor);
            LineSeparator hr = new LineSeparator(solidLine);
            hr.setMarginTop(40.0f);
            hr.setMarginBottom(40.0f);
            receiptContainer.add(hr);

            Receipt receiptData = title_summary.getReceipt();
            Float receiptLeading = 20f;

            Paragraph receiptHeading = new Paragraph("VAT Receipt");
            receiptHeading.setFontSize(docFontSize).setFont(boldFont);
            receiptHeading.setRole(PdfName.H2);

            receiptContainer.add(receiptHeading);

            Paragraph receiptBlockLeft = new Paragraph();
            receiptBlockLeft.setFixedLeading(receiptLeading).setFontSize(12).setMargin(0).setKeepTogether(true);

            receiptRow(receiptBlockLeft, "Date:", receiptData.getDate() + "\n", boldFont, font);

            receiptRow(receiptBlockLeft, "Transaction ID:", receiptData.getTransId() + "\n", boldFont, font);

            receiptRow(receiptBlockLeft, "Description:", title_summary.getSummaryHeading() + ": " + title_summary.getTitleNumber() + "\n", boldFont, font);

            receiptRow(receiptBlockLeft, "Net amount:", "£" + receiptData.getNet() + "\n", boldFont, font);

            receiptRow(receiptBlockLeft, "VAT @ 20%:", "£" + receiptData.getVat() + "\n", boldFont, font);

            receiptRow(receiptBlockLeft, "Total inc VAT:", "£" + receiptData.getTotal(), boldFont, font);

            Float receiptColumnWidth = 300f;
            receiptBlockLeft.setWidth(receiptColumnWidth);

            receiptContainer.add(receiptBlockLeft);

            Paragraph receiptBlockRight = new Paragraph();
            receiptBlockRight.setFontSize(12);
            receiptBlockRight.setFixedLeading(receiptLeading);

            receiptBlockRight.add(new Text(receiptData.getAddress1() + "\n").setFont(boldFont));
            receiptBlockRight.add(new Text(receiptData.getAddress2() + "\n").setFont(font));
            receiptBlockRight.add(new Text(receiptData.getAddress3() + "\n").setFont(font));
            receiptBlockRight.add(new Text(receiptData.getAddress4() + "\n").setFont(font));
            receiptBlockRight.add(new Text(receiptData.getPostcode() + "\n").setFont(font));

            receiptBlockRight.add(new Text("VAT registration number:\n").setFont(boldFont));

            Text vatRegNumber = new Text(receiptData.getRegNumber() + "\n");
            vatRegNumber.setFont(font);
            receiptBlockRight.add(vatRegNumber);

            receiptBlockRight.setWidth(200f);
            receiptBlockRight.setRelativePosition(receiptColumnWidth + 25f, 0f, 0f, 0f);
            receiptBlockRight.setMarginTop(receiptLeading * -6);

            receiptBlockRight.setKeepTogether(true);

            receiptContainer.add(receiptBlockRight);

        }

        // Contact block
        Paragraph contactBlock = new Paragraph("If you have any problems with your purchase, you can contact HM Land Registry on 0300 006 0411.");
        contactBlock.setFont(font);
        contactBlock.setMarginTop(40.0f);
        receiptContainer.add(contactBlock);

        document.add(receiptContainer);

        // Finally close and finish off the pdfa document
        document.close();

        return baos;

    }

}
