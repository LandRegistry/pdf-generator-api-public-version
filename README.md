# PDF Generator API

## Dependencies
None

## Interface Specification

### Create PDF
Request: `POST /v1/pdf`

It takes a json set that is what is used to build the summary.  Depending on the title it could have multiple proprietors, lenders and addresses for each.  If the receipt data is not sent a receipt will not be constructed.  

Minimal json required:
```json
{
	"proprietors": [{
		"name": "Jayme Bailey",
		"addresses": [{
			"lines": ["71 Stemson Avenue", "Exeter", "EX4 8FY"]
		}],
		"name_extra_info": ""
	}],
	"last_changed": "2013-12-03T07:41:27+00:00",
	"last_changed_readable": "This title was last changed on 03 December 2013 at 07:41:27",
	"is_caution_title": false,
	"number": "DN1234",
	"summary_heading": "Summary of title",
	"address_lines": ["71 Stemson Avenue", "Exeter", "EX4 8FY"],
	"edition_date": "2013-12-03",
	"tenure": "Freehold",
	"class_of_title": "Absolute",
	"proprietor_type_heading": "Owners"
}
```

Additional Extras:
Lenders
```json
{"lenders": [{
		"name": "Hagenes, Glover and Russel Inc",
		"addresses": [{
			"lines": ["10 Test Boulevard", "testwood", "Warrington", "WA3 7QH"]
		}],
		"name_extra_info": ""
	}]
}
```

PPI
```json
{
    "ppi_data": "The price stated to have been paid on 29 November 2013 was \u00a34,995"
}
```
Receipt
```json
{
    "receipt": {
		"address1": "Land Registry",
		"date": "26 July 2017 at 12:01:38",
		"reg_number": "GB 8888 181 53",
		"vat": "0.50",
		"address2": "Trafalgar House",
		"total": "3.00",
		"net": "2.50",
		"address4": "Croydon",
		"postcode": "CR0 2AQ",
		"address3": "1 Bedford Park",
		"title_number": "DN1234",
		"trans_id": "2631201383"
	},
}
```


Response: `200`
```json
{
    "status": "OK"
}
```
The pdf is then streamed to requesting application.  Currently the receiving application has to define the response with 
```
"Content-Disposition" = "attachment; filename=summary-of-{}.pdf"
```
if it needs to be downloaded automatically for the user (recommended behaviour) rather than displayed in the browser.


## Errors
- "No title summary in request" - `NOTITLE` - `400`
- "Title information is incomplete" - `NOTITLEINFO` - `400`
- "PDF Rendering has failed" - `GEN` - `500`
- "Streaming PDF has failed" - `STREAM` - `500`

## Skeleton Documentation

This app is derived from the Spark Skeleton API, for further documentation please refer to the [README](http://192.168.249.38/skeletons/spark-skeleton-api/blob/master/README.md).