# Events Calendar
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![](https://jitpack.io/v/tizisdeepan/eventscalendar.svg)](https://jitpack.io/#tizisdeepan/eventscalendar)

<table>
    <tr><td align="center"><img src="https://github.com/tizisdeepan/eventscalendar/blob/master/screenshots/ss1.png" alt="Single Selection Mode" width="100%"></td>
    <td align="center"><img src="https://github.com/tizisdeepan/eventscalendar/blob/master/screenshots/ss2.png" alt="Range Selection Mode" width="100%"></td>
    <td align="center"><img src="https://github.com/tizisdeepan/eventscalendar/blob/master/screenshots/ss3.png" alt="Multiple Selection Mode" width="100%"></td></tr>
    <tr><td align="center"><b>Single Selection Mode</b></td>
    <td align="center"><b>Range Selection Mode</b></td>
    <td align="center"><b>Multiple Selection Mode</b></td></tr>
</table>

## What is Events Calendar?
Events Calendar is a user-friendly library that helps you achieve a cool Calendar UI with events mapping. You can customise every pixel of the calendar as per your wish and still achieve in implementing all the functionalities of the native android calendar in addition with adding dots to the calendar which represents the presence of an event on the respective dates. Events Calendar also has multi-lingual support. You are just a few steps away from implementing your own badass looking Calendar for your very own project!

## Implementation
### [1] In your app module gradle file
```gradle
dependencies {
    implementation 'com.github.tizisdeepan:eventscalendar:1.6.1'
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
    app:datesTextSize="16sp"
    app:eventDotColor="#ff0000"
    app:isBoldTextOnSelectionEnabled="true"
    app:monthTitleColor="#ffffff"
    app:monthTitleTextSize="16sp"
    app:primaryTextColor="#c4c4c4"
    app:secondaryTextColor="#666666"
    app:selectedTextColor="#000000"
    app:selectionColor="#ffe600"
    app:weekHeaderColor="#c6c6c6"
    app:rangeSelectionColor="#ffe600"
    app:rangeSelectionStartColor="#c1ae01"
    app:rangeSelectionEndColor="#c1ae01"
    app:weekHeaderTextSize="16sp" />
```
### [4] Implement EventCalendar.Callback on your Activity/ Fragment
```kotlin
class MainActivity : AppCompatActivity(), EventsCalendar.Callback {
    ...
    override fun onDayLongPressed(selectedDate: Calendar?) {
        Log.e("LONG", "CLICKED")
    }
    
    override fun onMonthChanged(monthStartDate: Calendar?) {
        Log.e("MON", "CHANGED")
    }

    override fun onDaySelected(selectedDate: Calendar?) {
        Log.e("SHORT", "CLICKED")
    }
}
```
### [5] Create instances and set default values for the EventsCalendar in your Activity/ Fragment
```kotlin
eventsCalendar.setSelectionMode(eventsCalendar.MULTIPLE_SELECTION) //set mode of Calendar
              .setToday(today) //set today's date [today: Calendar]
              .setMonthRange(start, end) //set starting month [start: Calendar] and ending month [end: Calendar]
              .setWeekStartDay(Calendar.SUNDAY, false) //set start day of the week as you wish [startday: Int, doReset: Boolean]
              .setCurrentSelectedDate(today) //set current date and scrolls the calendar to the corresponding month of the selected date [today: Calendar]
              .setDatesTypeface(typeface) //set font for dates
              .setDateTextFontSize(16f) //set font size for dates
              .setMonthTitleTypeface(typeface) //set font for title of the calendar
              .setMonthTitleFontSize(16f) //set font size for title of the calendar
              .setWeekHeaderTypeface(typeface) //set font for week names
              .setWeekHeaderFontSize(16f) //set font size for week names
              .setCallback(this) //set the callback for EventsCalendar
              .addEvent(c) //set events on the EventsCalendar [c: Calendar]
              .disableDate(dc) //disable a specific day on the EventsCalendar [c: Calendar]
              .disableDaysInWeek(Calendar.SATURDAY, Calendar.SUNDAY) //disable days in a week on the whole EventsCalendar [varargs days: Int]
              .build()
```
### [6] You can change selection mode of the calendar as you wish
```kotlin
eventsCalendar.setSelectionMode(eventsCalendar.SINGLE_SELECTION)
```
**Available Selection modes are,**
1. **SINGLE_SELECTION** -> can select single day at a time
2. **RANGE_SELECTION** -> can select range of days at a time
2. **MULTIPLE_SELECTION** -> can select multiple days at a time (not necessary that the selected days be consequtive)
### Documentation

|XML|Kotlin/Java|Description|
|---|---|---|
|`app:primaryTextColor`|`setPrimaryTextColor(color: Int)`|**Primary Text** color of the calendar (selectable dates)|
|`app:secondaryTextColor`|`setSecondaryTextColor(color: Int)`|**Secondary Text** color of the calendar (disabled dates)|
|`app:selectedTextColor`|`setSelectedTextColor(color: Int)`|Text color of the **Selected** date|
|`app:selectionColor`|`setSelectionColor(color: Int)`|Color for the **Selection Circle**|
|`app:rangeSelectionColor`|`setRangeSelectionColor(color: Int)`|Color for the **Selection Background**|
|`app:rangeSelectionStartColor`|`setRangeSelectionStartColor(color: Int)`|Color for the **Range Start Selection Background**|
|`app:rangeSelectionEndColor`|`setRangeSelectionEndColor(color: Int)`|Color for the **Range End Selection Background**|
|`app:weekHeaderColor`|`setWeekHeaderColor(color: Int)`|Text color for the **Week Header** labels|
|`app:weekHeaderTextSize`|`setWeekHeaderFontSize(size: Float)`|Text size for the **Week Header** labels|
|`app:datesTextSize`|`setDateTextFontSize(size: Float)`|Text size for the **Date** labels|
|`app:monthTitleColor`|`setMonthTitleColor(color: Int)`|Text color for the **Month Title** in the calendar view|
|`app:monthTitleTextSize`|`setMonthTitleFontSize(size: Float)`|Text size for the **Month Title** in the calendar view|
|`app:eventDotColor`|`setEventDotColor(color: Int)`|Color for the **Event Dots** marked in the calendar view|
|`app:isBoldTextOnSelectionEnabled`|`setIsBoldTextOnSelectionEnabled(isEnabled: Boolean)`|Sets whether the dates should be **highlighted** or not|

Voila! You have implemented an awesome Events Calendar for your Android Project now!

Developed By
------------

* Deepan Elango - <tizisdeepan@gmail.com>

<a href="https://twitter.com/tizisdeepan">
  <img alt="Follow me on Twitter" src="./screenshots/twitter.png" />
</a>
<a href="https://www.linkedin.com/in/tizisdeepan/">
  <img alt="Add me to Linkedin" src="./screenshots/linkedin.png" />
</a>
