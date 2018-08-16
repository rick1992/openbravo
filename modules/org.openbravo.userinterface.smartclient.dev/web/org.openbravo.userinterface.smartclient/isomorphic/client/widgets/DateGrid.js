/*
 * Isomorphic SmartClient
 * Version v10.0d_2014-02-13 (2014-02-13)
 * Copyright(c) 1998 and beyond Isomorphic Software, Inc. All rights reserved.
 * "SmartClient" is a trademark of Isomorphic Software, Inc.
 *
 * licensing@smartclient.com
 *
 * http://smartclient.com/license
 */

 

// This file creates a mini-calendar that is used to pick a date, for example, you might have a
// button next to a form date field that brings this file up.




//>	@class	DateGrid
//
// A ListGrid subclass that manages calendar views.
//
// @treeLocation Client Reference/Forms
// @visibility external
//<
if (isc.ListGrid == null) {
    isc.Log.logInfo("Source for DateGrid included in this module, but required " +
        "superclass (ListGrid) is not loaded. This can occur if the Forms module is " +
        "loaded without the Grids module. DateGrid class will not be defined within " + 
        "this page.", "moduleDependencies");
} else {

// create a customized ListGrid to show the days in a month
isc.ClassFactory.defineClass("DateGrid", "ListGrid");

isc.DateGrid.addProperties({
    width: 10,
    height: 10,
    cellHeight: 20,
    autoFitData: "vertical",
    minFieldWidth: 20,
    autoFitMaxRows: 5,
    useCellRollOvers: true,
    canSelectCells: true,
    leaveScrollbarGap: false,
    canResizeFields: false,
    headerButtonProperties: {
        padding: 0
    },
    headerHeight: 20,
    canSort: false,
    canEdit: false,

    showSortArrow: isc.ListGrid.NONE,
    showFiscalYear: false,
    showFiscalWeek: false,
    showCalendarWeek: false,
    
    loadingDataMessage: "",
    alternateRecordStyles: false,
    
    showHeaderMenuButton: false,
    showHeaderContextMenu: false,
    
    cellPadding: 0,

    // we need to locate rows by cell-value, not PK or whatever else
    locateRowsBy: "targetCellValue",
    
    fiscalYearFieldTitle: "Year",
    weekFieldTitle: "Wk",
    
    canReorderFields: false,
    
    bodyProperties: {
        canSelectOnRightMouse: false,
        overflow: "visible"
    },
    
    headerProperties: {
        overflow: "visible"
    },

    initWidget : function () {
        this.shortDayNames = isc.Date.getShortDayNames(3);
        this.shortDayTitles = isc.Date.getShortDayNames(this.dayNameLength);
        this.shortMonthNames = isc.Date.getShortMonthNames();
        
        this.Super("initWidget", arguments);

        this.refreshUI();
    },
    
    getTitleField : function () {
        return null;
    },
    
    getCellAlign : function (record, rowNum, colNum) {
        return "center";
    },

    formatCellValue : function (value, record, rowNum, colNm) {
        if (value && value.getDate) return value.getDate();
        return "" + value;
    },

    getCellStyle : function (record, rowNum, colNum) {
        var field = this.getField(colNum);
        var selected = record.fiscalWeek == this.selectedWeek;
        if (field.name == "fiscalYear") {
            return !selected ? this.baseFiscalYearStyle : this.selectedWeekStyle;
        } else if (field.name == "fiscalWeek" || field.name == "calendarWeek") {
            return !selected ? this.baseWeekStyle : this.selectedWeekStyle;
        }

        var date = this.getCellDate(record, rowNum, colNum),
            isDisabled = this.dateIsDisabled(date),
            isOtherMonth = date.getMonth() != this.workingMonth,
            style = this.Super("getCellStyle", arguments);
        ;

        if (field.isDateField) {
            if ((isDisabled || isOtherMonth)) {
                
                style = field.isWeekend ? this.disabledWeekendStyle : this.disabledWeekdayStyle;

                var isOver = (this.getEventRow() == rowNum && this.getEventColumn() == colNum),
                    isSelected = this.cellSelection ? 
                                        this.cellSelection.isSelected(rowNum, colNum) : false,
                    overIndex = style.indexOf("Over"),
                    selectedIndex = style.indexOf("Selected")
                ;

                if (overIndex >= 0) style = style.substring(0, overIndex);
                if (selectedIndex >= 0) style = style.substring(0, selectedIndex);
                
                if (isSelected) style += "Selected";
                if (isOver) style += "Over";
            }
        }

        return style;
        
    },
    
    cellMouseDown : function (record, rowNum, colNum) {
        var date = this.getCellDate(record, rowNum, colNum);
        if (!date) return true;
        if (this.dateIsDisabled(date)) return false;
        return true;
    },
    
    cellClick : function (record, rowNum, colNum) {
        var date = this.getCellDate(record, rowNum, colNum);
        if (!date) return true;

        if (this.dateIsDisabled(date)) {
            return true;
        }

        this.dateClick(date.getFullYear(), date.getMonth(), date.getDate());
    },
    dateClick : function (year, month, date) {},
    
    cellSelectionChanged : function (cellList) {
        var sel = this.getCellSelection();
        for (var i=0; i<cellList.length; i++) {
            var cell = cellList[i];
            if (sel.cellIsSelected(cell[0], cell[1])) {
                if (this.selectedWeek != this.getRecord(cell[0]).fiscalWeek) {
                    this.setSelectedWeek(this.getRecord(cell[0]).fiscalWeek);
                }
                return;
            }
        }
        return;
    },
    
    setSelectedWeek : function (weekNum) {
        this.selectedWeek = weekNum;
        this.markForRedraw();
        this.selectedWeekChanged(this.selectedWeek);
    },
    selectedWeekChanged : function (weekNum) {},
    
    getWorkingMonth : function () {
        return this.workingMonth;
    },
    getSelectedDate : function () {
        return null;
    },
    
    disableMarkedDates : function () {
        this.disabledDateStrings = [];
        if (this.disabledDates && this.disabledDates.length > 0) {
            for (var i=0; i<this.disabledDates.length; i++) {
                this.disabledDateStrings[i] = this.disabledDates[i].toShortDate();
            }
        }
    },

    dateIsDisabled : function (date) {
        if (!date) return;
        if (this.disableWeekends && this.dateIsWeekend(date)) return true;
        var disabled = this.disabledDateStrings.contains(date.toShortDate());
        return disabled;
    },
    
    getCellDate : function (record, rowNum, colNum) {
        if (colNum < this.dateColumnOffset || !this.getField(colNum)) return;
        var rDate = record.rowStartDate,
            date = Date.createLogicalDate(rDate.getFullYear(), rDate.getMonth(), 
                rDate.getDate()+(colNum - this.dateColumnOffset))
        ;
        return date;
    },

    selectDateCell : function (date) {
        var selection = this.getCellSelection(),
            cell = this.getDateCell(date)
        ;

        if (!cell) return;

        if (cell.colNum != null) selection.selectSingleCell(cell.rowNum, cell.colNum);
        this.setSelectedWeek(cell.record.fiscalWeek);
    },

    getDateCell : function (date) {
        // returns an object with rowNum, colNum and record
        var selection = this.getCellSelection(),
            data = this.data
        ;

        if (date && data && data.length > 0) {
            var dayCount = this.showWeekends == false ? 5 : 7;
            for (var i=0; i<data.length; i++) {
                var record = data[i];
                if (record) {
                    for (var j=0; j<dayCount; j++) {
                        var dateDay = date.getDay();
                        if (Date.compareLogicalDates(record[this.shortDayNames[date.getDay()]], date) == 0) {
                            var fieldName = this.shortDayNames[date.getDay()],
                                field = this.getField(fieldName),
                                fieldNum = field ? this.getFieldNum(field.name) : null
                            ;
                            if (field) {
                                return { rowNum: i, colNum: fieldNum, record: record };
                            }
                            break;
                        }
                    }
                }
            }
        }
    },

    shouldDisableDate : function (date) {
        var result = this.dateIsDisabled(date);
        return result;
    },

    getRowHeight : function () {
        if (this._storedRowHeight == null) {
            var bodyHeight = this.body.getVisibleHeight();
            this._storedRowHeight = Math.floor(Math.max(20, Math.floor(bodyHeight / this.data.length)));
        }
        return this._storedRowHeight;
    },

    setStartDate : function (startDate) {
        var year = startDate.getFullYear(),
            month = startDate.getMonth(),
            date = startDate.getDate(),
            monthStart = Date.createLogicalDate(year, month, 1),
            day = monthStart.getDay()
        ;

        var weekDate = monthStart.duplicate();
        
        var delta=0;
        if (day > this.firstDayOfWeek) {
            // we need to tweak the start date
            delta = (day-this.firstDayOfWeek) * -1;
        } else if (day < this.firstDayOfWeek) {
            delta = (this.firstDayOfWeek-day)-7;
        }
        
        var weekStart = Date.createLogicalDate(year, month, 1 + delta, 0);
                
        //this.logWarn("in setStartDate - original is " + startDate.toShortDate() + "\n\n" + 
        //    "year, month, date, monthStart, monthDay, delta ***  final date \n" + 
        //    year+", "+month+", "+date+", "+monthStart.toShortDate()+", "+day+", "+delta+" - *** " + weekStart.toShortDate()
        //);

        this.workingMonth = startDate.getMonth();
        this.startDate = weekStart;
        this.buildCalendarData();
        this.markForRedraw();
    },
    
    refreshUI : function (startDate) {
        this.setFields(this.getFieldList());

        startDate = startDate || this.startDate;
        if (startDate) this.setStartDate(startDate);
    },

    getFieldList : function () {
        var fields = [];

        this.dateColumnOffset = 0;
        if (this.showFiscalYear) {
            fields.add({ name: "fiscalYear", type: "number", title: this.fiscalYearFieldTitle, width: 30, 
                align: "center", cellAlign: "center", showRollOver: false, showDown: false,
                baseStyle: this.baseFiscalYearStyle,
                headerBaseStyle: this.fiscalYearHeaderStyle || this.baseFiscalYearStyle
            });
            this.dateColumnOffset++;
        }
        if (this.showFiscalWeek) {
            fields.add({ name: "fiscalWeek", type: "number", title: this.weekFieldTitle, width: 25, 
                align: "center", showRollOver: false, showDown: false,
                baseStyle: this.baseWeekStyle,
                headerBaseStyle: this.weekHeaderStyle || this.baseWeekStyle
            });
            this.dateColumnOffset++;
        }
        if (this.showCalendarWeek) {
            fields.add({ name: "calendarWeek", type: "number", title: this.weekFieldTitle, width: 25, 
                align: "center", showRollOver: false, showDown: false,
                baseStyle: this.baseWeekStyle,
                headerBaseStyle: this.weekHeaderStyle || this.baseWeekStyle
            });
            this.dateColumnOffset++;
        }
        
        var weekendDays = isc.Date.getWeekendDays();
        
        for (var i=0; i<this.shortDayNames.length; i++) {
            var dayNumber = i + this.firstDayOfWeek;
            if (dayNumber > 6) dayNumber-=7;
            // don't add fields for weekends if showWeekends is false
            if (!this.showWeekends && weekendDays.contains(dayNumber)) continue;
            var field = { 
                name: this.shortDayNames[dayNumber], 
                title: this.shortDayTitles[dayNumber],
                type: "text",
                align: "center",
                width: "*",
                padding: 0,
                isDateField: true,
                dateOffset: i,
                showRollOver: false,
                showDown: false
            };
            if (weekendDays.contains(dayNumber)) {
                field.isWeekend = true;
                field.baseStyle = this.baseWeekendStyle;
                field.headerBaseStyle = this.weekendHeaderStyle;
            } else {
                field.baseStyle = this.baseWeekdayStyle;
                field.headerBaseStyle = this.headerBaseStyle;
            }
            fields.add(field);
        }

        this.disableMarkedDates();
        
        return fields;
    },
    
    _weekendDays: null,
    dateIsWeekend : function (date) {
        if (!date) return false;
        if (this._weekendDays == null) this._weekendDays = isc.Date.getWeekendDays();
        return this._weekendDays.contains(date.getDay())
    },

    buildCalendarData : function (startDate) {
        if (startDate) this.startDate = startDate;
        startDate = this.startDate;
        
        var records = [],
            date = startDate,
            startMonth = this.startDate.getMonth(),
            // start date is start of the week - likely in the previous month.
            // We may need to jump up a year:
            // - working month is dec - end date will be start of jan of next year
            // - start date is dec, working month is jan (of next year after start date),
            //   end date is start of feb
            yearWrap = (startMonth == 11 || this.workingMonth == 11),
            sDate2 = Date.createLogicalDate(startDate.getFullYear() + (yearWrap ? 1 : 0), 
                            (this.workingMonth == 11 ? 0 : this.workingMonth + 1), 1)
        ;
        var delta = (sDate2.getTime() - date.getTime()) / 1000 / 60 / 60 / 24,
            weeks = delta / 7
        ;
        
        var counter = Math.floor(weeks) + (delta % 7 > 0 ? 1 : 0);
        
        for (var i =0; i<=counter; i++) {
            var thisDate = Date.createLogicalDate(date.getFullYear(), date.getMonth(), date.getDate() + (i*7));
            if (i == counter && thisDate.getMonth() != this.workingMonth) {
                break;
            }
            records.add(this.getWeekRecord(thisDate));
        }
        
        this.setData(records);

        this.selectDateCell(this.getSelectedDate()) 
    },

    getFiscalCalendar : function () {
        return this.fiscalCalendar || Date.getFiscalCalendar();
    },

    
    // set this to false to allow the DateGrid to NOT always show fiscal week 1 - instead, it 
    // may show either the highest partial week or 1, depending on where the fiscalStartDate is
    alwaysShowFirstFiscalWeek: true,
    getWeekRecord : function (date) {
        var fiscalCalendar = this.getFiscalCalendar(),
            // fiscal year object for start date
            fiscalYear = date.getFiscalYear(fiscalCalendar),
            // end of week date
            endDate = new Date(date.getTime() + (6*86400000));
            
        if (date.logicalDate) endDate.logicalDate = true;
                                
        var record = { 
            // first date within the row
            rowStartDate: date,
            rowEndDate: date + 7,
            
            // fiscalYear for the start date
            fiscalYear: fiscalYear.fiscalYear, 
            // fiscalYear for the end date
            fiscalYearEnd: endDate.getFiscalYear(fiscalCalendar).fiscalYear, 
            
            // fiscal week (for the start date)
            fiscalWeek: date.getFiscalWeek(fiscalCalendar, this.firstDayOfWeek),
            // fiscal week end (for the end date)
            fiscalWeekEnd: endDate.getFiscalWeek(fiscalCalendar, this.firstDayOfWeek),
            
            // calendar week (for the first day of week)
            calendarWeek: date.getWeek(this.firstDayOfWeek) 
        };
        
        
        
        // If we hit a fiscal week boundary, or a fiscalYear boundary, show the
        // week / year title in which more days in the week fall.
        
        if (record.fiscalWeek != record.fiscalWeekEnd) {

            var roundUpYear = false,
                roundUpWeek = false;
                
            if (record.fiscalYear != record.fiscalYearEnd) {
                if (!this.alwaysShowFirstFiscalWeek) {
                    var newYearStartDay =  Date.getFiscalStartDate(endDate, fiscalCalendar).getDay(),
                        delta = newYearStartDay - this.firstDayOfWeek;
                    if (delta < 0) delta += 6;
                    if (delta < 3) roundUpYear = true;
                } else roundUpYear = true;
            }
            
            if (!roundUpYear) {
                var yearStartDay = Date.getFiscalStartDate(date, fiscalCalendar).getDay(),
                    delta = yearStartDay - this.firstDayOfWeek;
                if (delta < 0) delta += 6;
                if (delta > 0 && delta < 3) roundUpWeek = true;
            }
            
            if (roundUpYear) {
                record.fiscalYear = record.fiscalYearEnd;
                record.fiscalWeek = 1;
            } else if (roundUpWeek) {
                record.fiscalWeek += 1;
            }
            
            
            
        }

        var year = date.getFullYear(),
            month = date.getMonth(),
            weekendDays = Date.getWeekendDays()
        ;
        for (var i=0; i<7; i++) {
            var thisDate = Date.createLogicalDate(year, month, date.getDate() + i, 0);
            //if (this.showWeekends || !weekendDays.contains(thisDate.getDay())) {
                var dayName = this.shortDayNames[thisDate.getDay()];
                record[dayName] = thisDate;
            //}
        }
        
        return record;
    }
});

} // END of if (isc.ListGrid == null) else case