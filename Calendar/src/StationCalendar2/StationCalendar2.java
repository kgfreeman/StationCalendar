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

	private static final String STYLESHEET_LINK = "<link rel=\"stylesheet\" type=\"text/css\" href=\"CalendarStylesheet.css\">\n";
	private final String classTag_Of_GeneralItems = "CalendarItems";
	private final String classTag_For_OpenShifts = "open";
	private Document calendarDocument;
	private String unitNumber;
	private String monthAndYear;

	StationCalendar2(File input) {

		openRawHTMLCalendarAsADocument(input);
		getUnitNumber();
		getMonthAndYear();
		
		startCleaningHTMLUsingWhitelist();
				
		buidlHTMLCalendarPageAsADocument();
		shadeTheDaysFromOtherMonths();

	}

	private void buidlHTMLCalendarPageAsADocument() {
		String finalCalendar = ""; 
		finalCalendar += buildHTMLHead();
		finalCalendar += buildHTMLBody();

		calendarDocument = Jsoup.parse(finalCalendar);
	}

	private String buildHTMLBody() {
		String HTMLString ="";
		HTMLString += "<body>\n"; 
		HTMLString += "<table>\n";
		HTMLString += returnHTMLTableCaption();
		HTMLString += returnDaysOfTheWeekTableHeader();	
		
		HTMLString += returnBodyOfCalendarTable();
		return HTMLString;
	}

	private String returnHTMLTableCaption() {
		return " <caption>" + getMonthAndYear() + "</caption>\n";
	}

	private String returnDaysOfTheWeekTableHeader() {
		return " <tr>\n  <th>Sunday</th>\n  <th>Monday</th>\n  <th>Tuesday</th>\n  <th>Wednesday</th>\n  <th>Thursday</th>\n  <th>Friday</th>\n  <th>Saturday</th> </tr>\n ";
	}

	private String buildHTMLHead() {
		String doctype = "<!DOCTYPE html><html>\n";
		String titleRow = "<title>" + getUnitNumber() + " " + getMonthAndYear() + "</title>\n";
		return doctype + "<head>\n" + titleRow + STYLESHEET_LINK + "</head>\n";
	}

	private void openRawHTMLCalendarAsADocument(File input) {
		try {
			calendarDocument = Jsoup.parse(input, "UTF-8");
		} catch (IOException e) {
			System.out.println("Unable to open File : " + input.toString());
			e.printStackTrace();
		}
	}

	private void startCleaningHTMLUsingWhitelist() {
		// / create a whitelist of allowed html tags and attributes
		Whitelist whitey = new Whitelist();
		whitey.addTags("td", "b", "font", "div", "a");
		whitey.addAttributes("font", "class");
		whitey.addAttributes("font", "color");
		whitey.addAttributes("a", "class");

		// / run document thru the whitelist cleaner
		Cleaner cleanDoc = new Cleaner(whitey);
		calendarDocument = cleanDoc.clean(calendarDocument);
	}

	private String getMonthAndYear() {
		if(monthAndYear == null ) {
			monthAndYear = removeNonBreakingSpaces(calendarDocument.select(".ScheduleViewLabel").text());
		}
		return monthAndYear;
	}

	private String getUnitNumber() {
		if (unitNumber == null) {
			unitNumber = calendarDocument.select("font.CalendarItems b").first().text();
		}
		return unitNumber;
	}

	private String returnBodyOfCalendarTable() {
		String tableToBeReturned = "<tr>";

		removeUnitNumbers();
	
		String classTagOfItemsInMySchedule = ".MyItems";
		replace_MyItemsClass_With_GeneralItemClass(classTagOfItemsInMySchedule,classTag_Of_GeneralItems);

		replaceManyLablesForOpenShiftsWith_Open_class();
		tableToBeReturned = makeBodyOfCalendarTable(tableToBeReturned);
		tableToBeReturned = addClosingHTMLTags(tableToBeReturned);
		
		tableToBeReturned = removeNonBreakingSpaces(tableToBeReturned); 
		tableToBeReturned = removeLicenseLevels(tableToBeReturned);
		tableToBeReturned = removeTimesWithinParens(tableToBeReturned); 

		return tableToBeReturned;
	}

	private String addClosingHTMLTags(String tableToBeReturned) {
		tableToBeReturned += "</td>\n    </tr>\n   </tbody>\n  </table>\n </body>\n</html>";
		return tableToBeReturned;
	}

	private String makeBodyOfCalendarTable(String calendarTable) {
		Elements items = calendarDocument.getElementsByTag("font");
		
		String classTag_for_DayInCalendar = "largeboldtext";
		int dayCounter = 0;
		for (Element item : items) {
			// every seven days add close and open table row tags
			if (item.hasClass(classTag_for_DayInCalendar) && (dayCounter % 7 == 0 && dayCounter > 0)) {
				calendarTable += "</tr>\n<tr>";
			}

			if (item.hasClass(classTag_for_DayInCalendar)) {
				calendarTable += "\n<td><span>" + item.text() + "</span> ";
				dayCounter++;
			}

			else if (item.hasClass(classTag_Of_GeneralItems)) {
				if (item.hasClass(classTag_For_OpenShifts)) {
					calendarTable += "<span class=\"open\">";
				} else {
					calendarTable += "<span>";
				}
				calendarTable += item.text() + "</span> ";
			}
		}
		return calendarTable;
	}

	private void replace_MyItemsClass_With_GeneralItemClass(String targetClass, String replacementClass) {
		calendarDocument.select(targetClass)
						.addClass(replacementClass)
						.removeClass(targetClass);
		return;
	}

	private void removeUnitNumbers() {
		String unitNumberOnEmployeeLine_DOMSelector = "font > b";
		String unitNumberForOpenShifts_DOMSelector = "a > b";
		String unitNumberDOMforEachDay_DOMSelector = "td > div";	
		calendarDocument.select(unitNumberOnEmployeeLine_DOMSelector).remove(); 
		calendarDocument.select(unitNumberForOpenShifts_DOMSelector).remove(); 
		calendarDocument.select(unitNumberDOMforEachDay_DOMSelector).remove();
	}

	private void replaceManyLablesForOpenShiftsWith_Open_class() {
		// looking for the many different keys that have been used to indicate
		// open shifts and adding class=open
	
		calendarDocument.getElementsByAttributeValue("color", "red").select(".CalendarItems").addClass(classTag_For_OpenShifts);
		calendarDocument.select(".PickupItems").wrap("<font class=\"CalendarItems open\">");
		calendarDocument.select(".PickupLinkM").wrap("<font class=\"CalendarItems open\">");
		return ;
	}

	private String removeTimesWithinParens(String tableToBeReturned) {
		tableToBeReturned = tableToBeReturned.replaceAll("(\\(\\d:\\d\\d\\))", "");  // (0:00) formatted shift duration time spans
		tableToBeReturned = tableToBeReturned.replaceAll("(\\(\\d\\d:\\d\\d\\))", ""); //(00:00) formatted shift duration time spans
		return tableToBeReturned;
	}

	private String removeNonBreakingSpaces(String returnString) {
		return returnString.replaceAll("\\u00A0", " ");
	}
	
	private String removeLicenseLevels(String returnString) {
		returnString = returnString.replaceAll("[SJ]R MEDIC :", " "); 
		returnString = returnString.replaceAll("EMT(/S)? :", " ");
		returnString = returnString.replaceAll("Supervisor :", "");
		return returnString;
	}
	
	private void shadeTheDaysFromOtherMonths() {

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

	public String toString() {  
		return calendarDocument.toString(); 
		}


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