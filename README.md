# StationCalendar

This takes a autogenerated schedule (generated by net-scheduler.com's ePro scheduling software)
and turns it ito a print friendly html document.

With the initial commit the user has to login, navigate to the desired monthly schedule page, then save it locally;
then running this program will prompt for the files location and (using JSoup) pretty up the file and output it to the console
where you again copy and paste it into your favorite editor prior to saving as a html file, opening (crome is working best) for printing.
