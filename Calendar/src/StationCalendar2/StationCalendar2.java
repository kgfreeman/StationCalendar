package StationCalendar2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

public class StationCalendar2 {

	private Document calendarDocument;
	private String unitNumber = "";
	private String monthAndYear = "";

	StationCalendar2(File input) {

		try {
			calendarDocument = Jsoup.parse(input, "UTF-8");
		} catch (IOException e) {
			System.out.println("Unable to open File : " + input.toString());
			e.printStackTrace();
		}
		
		
		unitNumber = calendarDocument.select("font.CalendarItems b").first().text();
		
		monthAndYear = calendarDocument.select(".ScheduleViewLabel").text();
		monthAndYear = monthAndYear.replaceAll("\\u00A0", ""); // removes &nbsp;

		// / create a whitelist of allowed html tags and attributes
		Whitelist whitey = new Whitelist();
		whitey.addTags("td", "b", "font", "div", "a");
		whitey.addAttributes("font", "class");
		whitey.addAttributes("font", "color");
		whitey.addAttributes("a", "class");

		// / run document thru the whitelist cleaner
		Cleaner cleanDoc = new Cleaner(whitey);
		calendarDocument = cleanDoc.clean(calendarDocument);

		
		String finalCalendar = 
				"<!DOCTYPE html><html>\n<head>\n<title>" + unitNumber + " " + monthAndYear
				+ "</title>\n<link rel=\"stylesheet\" type=\"text/css\" href=\"CalendarStylesheet.css\">\n</head>\n<body>\n<table>\n";

		finalCalendar += " <caption>" + monthAndYear + "</caption>\n";

		String headerRow =  " <tr>\n  <th>Sunday</th>\n  <th>Monday</th>\n  <th>Tuesday</th>\n  <th>Wednesday</th>\n  <th>Thursday</th>\n  <th>Friday</th>\n  <th>Saturday</th> </tr>\n ";	
		
		finalCalendar += headerRow;
		finalCalendar += getAndCleanTable2();

		calendarDocument = Jsoup.parse(finalCalendar);
		shadeDaysThatAreInOtherMonths();

	}

	// ////////////////////////////////////// DOM version
	public String getAndCleanTable2() {
		String tableToBeReturned = "<tr>";
		int dayCounter = 0;

		String unitNumberDOM_onEmployeeLine = "font > b";
		String unitNumberForOpenShiftsDOM = "a > b";
		String unitNumberDOMforEachDay = "td > div";
		
		calendarDocument.select(unitNumberDOM_onEmployeeLine).remove(); 
		calendarDocument.select(unitNumberForOpenShiftsDOM).remove(); 
		calendarDocument.select(unitNumberDOMforEachDay).remove();
	
		
		
		String classTagOfItemsInMySchedule = ".MyItems";
		String classTagOfGeneralItems = "CalendarItems";		
		
		calendarDocument.select(classTagOfItemsInMySchedule).addClass(classTagOfGeneralItems);
		calendarDocument.select(classTagOfItemsInMySchedule).removeClass(classTagOfItemsInMySchedule);

		
		// looking for the many different keys that have been used to indicate
		// open shifts and adding class=open
		calendarDocument.getElementsByAttributeValue("color", "red").select(".CalendarItems").addClass("open");
		calendarDocument.select(".PickupItems").wrap("<font class=\"CalendarItems open\">");
		calendarDocument.select(".PickupLinkM").wrap("<font class=\"CalendarItems open\">");

		
		Elements items = calendarDocument.getElementsByTag("font");
		for (Element item : items) {
			// every seven days add close and open table row tags
			if (item.hasClass("largeboldtext")
					&& (dayCounter % 7 == 0 && dayCounter > 0)) {
				tableToBeReturned += "</tr>\n<tr>";
			}

			// this adds the open table data tag and adds the date span
			if (item.hasClass("largeboldtext")) {
				tableToBeReturned += "\n<td><span>" + item.text() + "</span> ";
				dayCounter++;
			}

			// takes care of the person part of the table data
			else if (item.hasClass(classTagOfGeneralItems)) {
				if (item.hasClass("open")) {
					tableToBeReturned += "<span class=\"open\">";
				} else {
					tableToBeReturned += "<span>";
				}
				tableToBeReturned += item.text() + "</span>";
			}
		}


		tableToBeReturned = tableToBeReturned.replaceAll("\\u00A0", " "); // removes &nbsp;
		
		tableToBeReturned = tableToBeReturned.replaceAll("[SJ]R MEDIC :", " "); 
		tableToBeReturned = tableToBeReturned.replaceAll("EMT(/S)? :", " ");
		tableToBeReturned = tableToBeReturned.replaceAll("Supervisor :", "");

		tableToBeReturned = tableToBeReturned.replaceAll("(\\(\\d\\d:\\d\\d\\))", "");  //(00:00) formatted shift duration time spans
		tableToBeReturned = tableToBeReturned.replaceAll("(\\(\\d:\\d\\d\\))", "");  // (0:00) formatted shift duration time spans

		return tableToBeReturned;
	}

	public void shadeDaysThatAreInOtherMonths() {
		Document localDoc;
		boolean shadeDays = true;
		int todaysDate, yesterdaysDate = 0;

		localDoc = calendarDocument; 

		// find dates from the previous month that are at the beginning of the calendar
		for (Element day : localDoc.select("td > span:eq(0)")) {
			todaysDate = Integer.parseInt(day.text().toString());

			if ((todaysDate == 1) && (yesterdaysDate == 0)) {
				shadeDays = Boolean.logicalXor(shadeDays, true); 
			} // checks to see if the first day of the month falls on the first
			// day of the calendar

			if (todaysDate < yesterdaysDate) { // toggles shading at the end of the month
				shadeDays = Boolean.logicalXor(shadeDays, true);
			}

			if (shadeDays == true) {
				day.parent().addClass("DayIsInAnotherMonth");
			}
			yesterdaysDate = todaysDate;
		}
		calendarDocument = localDoc;

		return;
	}

	public String toString() {  return calendarDocument.toString(); }


	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////main
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String inputFileName;

		if (args.length == 1) {
			inputFileName = args[0];
		}
		else {


			Scanner keyboard = new Scanner(System.in);
			System.out.print(" <!  source HTML filename ? ");

			inputFileName = keyboard.next();
			System.out.println(inputFileName + " >");
			
			keyboard.close();
		}

		File inputFile = new File("C:/Users/HVA/Documents/KevinFreeman/Calendar/"+ inputFileName);

		StationCalendar2 stationCalendar = new StationCalendar2(inputFile);
		System.out.println(stationCalendar.toString());

		System.exit(0);

	}
}