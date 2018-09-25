# Events Calendar
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![](https://jitpack.io/v/tizisdeepan/eventscalendar.svg)](https://jitpack.io/#tizisdeepan/eventscalendar)

![Screenshot 2](https://github.com/tizisdeepan/eventscalendar/blob/master/screenshots/ss2.png)

## What is Events Calendar?
Events Calendar is a developer-friendly library that helps you achieve a cool Calendar UI with events mapping. You can customise every pixel of the calendar as per your wish and still achieve in implementing all the functionalities of the native android calendar in addition with adding dots to the calendar which represents the presence of an event on the respective dates. It can be done easily, you are just a few steps away from implementing your own badass looking Calendar for your very own project!

## Implementation
### [1] In your app module gradle file
```gradle
dependencies {
    implementation 'com.github.tizisdeepan:dots:1.0.1'
}
```

### [2] In your project level gradle file
```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```
### [3] Use EventsCalendar in your layout.xml
```xml
<com.events.calendar.views.EventsCalendar
            android:id="@+id/eventsCalendar"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="#000000"
            android:overScrollMode="never"
            app:eventDotColor="#ff0000"
            app:isBoldTextOnSelectionEnabled="true"
            app:monthTitleColor="#ffffff"
            app:primaryTextColor="#c4c4c4"
            app:secondaryTextColor="#666666"
            app:selectedTextColor="#000000"
            app:selectionColor="#ffe600"
            app:weekHeaderColor="#c6c6c6" />
```
### [5] Implement EventCalendar.Callback on your Activity/ Fragment
```kotlin
class MainActivity : AppCompatActivity(), EventsCalendar.Callback {
    ...
    override fun onMonthChanged(monthStartDate: Calendar?) {
        Log.e("MON", "CHANGED")
    }

    override fun onDaySelected(selectedDate: Calendar?) {
        Log.e("DAY", "SELECTED")
    }
}
```
### [6] Create instances and set default values for the EventsCalendar in your Activity/ Fragment
```kotlin
//set today's date [today: Calendar Object]
eventsCalendar.setToday(today)
//set starting month [start: Calendar Object] and ending month [end: Calendar Object]
eventsCalendar.setMonthRange(start, end)
//set start day of the week as you wish [startday: Int, doReset: Boolean]
eventsCalendar.setWeekStartDay(Calendar.SUNDAY, false)
//set current date and scrolls the calendar to the corresponding month of the selected date [today: Calendar]
eventsCalendar.setCurrentSelectedDate(today)
//set font for dates
eventsCalendar.setDatesTypeface(typeface)
//set font for title of the calendar
eventsCalendar.setMonthTitleTypeface(typeface)
//set font for week names
eventsCalendar.setWeekHeaderTypeface(typeface)
//set the callback for EventsCalendar
eventsCalendar.setCallback(this)
```
###Documentation

|XML|Kotlin/Java|Description|
|---|---|---|
|app:primaryTextColor|eventsCalendar.setPrimaryTextColor(color: Int)|Sets primary text color of the calendar (selectable dates)|
|app:secondaryTextColor|eventsCalendar.setSecondaryTextColor(color: Int)|Sets secondary text color of the calendar (disabled dates)|
|app:selectedTextColor|eventsCalendar.setSelectedTextColor(color: Int)|Sets text color of the selected date|
|app:selectionColor|eventsCalendar.setSelectionColor(color: Int)|Sets color for the selection circle|
|app:weekHeaderColor|eventsCalendar.setWeekHeaderColor(color: Int)|Sets text color for the week header labels|
|app:monthTitleColor|eventsCalendar.setMonthTitleColor(color: Int)|Sets text color for the month title in the calendar view|
|app:eventDotColor|eventsCalendar.setEventDotColor(color: Int)|Sets color for the event dots marked in the calendar view|
|app:isBoldTextOnSelectionEnabled|eventsCalendar.setIsBoldTextOnSelectionEnabled(isEnabled: Boolean)|Sets whether the dates should be highlighted or not|

Voila! You have implemented an awesome Events Calendar for your Android Project now!
