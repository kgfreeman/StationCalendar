package StationCalendar2;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

public class StationCalendar2 {

	private String unitNumber = "";
	private Document doc;
	private String finalCalendar = "";
	private String monthAndYear = "";

	StationCalendar2(File input) {

		try { // open the input file
			doc = Jsoup.parse(input, "UTF-8");
		} catch (IOException e) {
			System.out.println("ARGH !!!!!!!!!!!!!!!");
			e.printStackTrace();
		} finally {
		}

		unitNumber = doc.select("font.CalendarItems b").first().text();

		monthAndYear = doc.select(".ScheduleViewLabel").text();
		monthAndYear = monthAndYear.replaceAll("\\u00A0", ""); // removes &nbsp;

		// / create a whitelist of allowed html tags and attributes
		Whitelist whitey = new Whitelist();
		whitey.addTags("td", "b", "font", "div", "a");
		whitey.addAttributes("font", "class");
		whitey.addAttributes("font", "color");
		whitey.addAttributes("a", "class");

		// / run document thru the whitelist cleaner
		Cleaner cleanDoc = new Cleaner(whitey);
		doc = cleanDoc.clean(doc);

		// makeCalendar();
		// add table head
		String finalCalendar = "<!DOCTYPE html><html>\n<head>\n<title>"
				+ unitNumber
				+ " "
				+ monthAndYear
				+ "</title>\n<link rel=\"stylesheet\" type=\"text/css\" href=\"CalendarStylesheet.css\">\n</head>\n<body>\n<table>\n";

		// add table caption
		finalCalendar += " <caption>" 
					+ monthAndYear 
					+ "</caption>\n";

		// add table header row
		finalCalendar += " <tr>\n  <th>Sunday</th>\n  <th>Monday</th>\n  <th>Tuesday</th>\n  <th>Wednesday</th>\n  <th>Thursday</th>\n  <th>Friday</th>\n  <th>Saturday</th> </tr>\n ";

		//
		finalCalendar += getAndCleanTable2();

		doc = Jsoup.parse(finalCalendar);
		shadeDays();

	}

	// ////////////////////////////////////// DOM version
	public String getAndCleanTable2() {
		String localString = "<tr>";
		int dayCounter = 0;

		doc.select("font > b").remove(); // remove unit number from each
											// employee line
		doc.select("a > b").remove(); // remove unit number for open shifts

		doc.select("td > div").remove(); // remove unit number from each day

		doc.select(".MyItems").addClass("CalendarItems"); // makes your days
															// look like all the
															// others
		doc.select(".MyItems").removeClass("MyItems");

		// looking for the many different keys that have been used to indicate
		// open shifts and adding class=open
		doc.getElementsByAttributeValue("color", "red")
				.select(".CalendarItems").addClass("open");
		doc.select(".PickupItems").wrap("<font class=\"CalendarItems open\">");
		doc.select(".PickupLinkM").wrap("<font class=\"CalendarItems open\">");

		Elements items = doc.getElementsByTag("font");
		for (Element thing : items) {
			// every seven days add close and open table row tags
			if (thing.hasClass("largeboldtext")
					&& (dayCounter % 7 == 0 && dayCounter > 0)) {
				localString += "</tr>\n<tr>";
			}

			// this adds the open table data tag and adds the date span
			if (thing.hasClass("largeboldtext")) {
				localString += "\n<td><span>" + thing.text() + "</span> ";
				dayCounter++;
			}

			// takes care of the person part of the table data
			else if (thing.hasClass("CalendarItems")) {
				if (thing.hasClass("open")) {
					localString += "<span class=\"open\">";
				} else {
					localString += "<span>";
				}
				localString += thing.text() + "</span>";
			}
		}

		// //// clean up local string before returning it
		localString = localString.replaceAll("\\u00A0", " "); // removes &nbsp;

		// remove license levels / titles from entries
		localString = localString.replaceAll("[SJ]R MEDIC :", " "); // jr and sr
																	// medics
		localString = localString.replaceAll("EMT(/S)? :", " "); // emt and
																	// emt/s
		localString = localString.replaceAll("Supervisor :", ""); // supervisor

		// kills all the (00:00) formatted shift duration time spans
		localString = localString.replaceAll("(\\(\\d\\d:\\d\\d\\))", "");
		// // kills all the (0:00) formatted shift duration time spans
		localString = localString.replaceAll("(\\(\\d:\\d\\d\\))", "");

		return localString;
	}

	// ////////////////////Shade days changes the background color of days at
	// the start and end of the calendar that don't belong to the current month
	// by adding a class to the table data tag that the stylesheet will shade
	public void shadeDays() {
		Document localDoc;
		boolean shadeDays = true;
		int date, previousDate = 0;

		localDoc = doc; 
		
		// find dates from the previous month that are at the beginning of the calendar
		for (Element day : localDoc.select("td > span:eq(0)")) {
			date = Integer.parseInt(day.text().toString());

			if ((date == 1) && (previousDate == 0)) {
				shadeDays = Boolean.logicalXor(shadeDays, true); 
			} // checks to see if the first day of the month falls on the first
				// day of the calendar

			if (date < previousDate) { // toggles shading at the end of the month
				shadeDays = Boolean.logicalXor(shadeDays, true);
			}

			if (shadeDays == true)
				day.parent()
				   .addClass("NON"); // NON is the class in the TD that is shaded in stylesheet

			previousDate = date;
		}
		doc = localDoc;

		return;
	}

	public String toString() {
		return doc.toString();
	}

	// /////////// MAIN
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////main
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		
		Scanner keyboard = new Scanner(System.in);
		System.out.print(" <!  source HTML filename ? ");
		
		String inputFileName = keyboard.next();
		
		File input = new File("C:/Users/HVA/Documents/KevinFreeman/Calendar/"
				+ inputFileName);

		System.out.println(input + " >");
		
		StationCalendar2 sc = new StationCalendar2(input);

		System.out.println(sc.toString());
		
		System.exit(0);
		
		
	/*	
		
		if (args.length != 1) {
			System.out
					.println("usage: java StationCalendar.jar -jar source_html_file");
			System.exit(1);
		}

		File input = new File("C:/Users/HVA/Documents/KevinFreeman/Calendar/"
				+ args[0]);

		StationCalendar sc = new StationCalendar(input);

		System.out.println(sc.toString());
*/
	}
}