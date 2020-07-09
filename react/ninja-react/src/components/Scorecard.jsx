import React, { useState, useEffect, useCallback } from "react";
import {
  Table,
  TableHeader,
  TableBody,
  sortable,
  SortByDirection
} from "@patternfly/react-table";
import { PageSection } from "@patternfly/react-core";
import PaginationControls from "./PaginationControls";
import API from "../config/ServerUrls";
import "../assets/css/horizontal-scroll.css";

/**
 * @author fostimus
 */
const ScorecardSection = () => {
  return (
    <PageSection>
      <div className="horizontal-scroll">
        <SortableTable />
      </div>
    </PageSection>
  );
};

export default ScorecardSection;

export const SortableTable = props => {
  /**
   * State Initialization
   */
  const [columns, setColumns] = useState([]);
  const [rows, setRows] = useState([]);
  const [displayedRows, setDisplayedRows] = useState([]);
  const [page, setPage] = useState(1);
  const [perPage, setPerPage] = useState(10);
  const [sortBy, setSortBy] = useState({});

  /**
   * method to set/change which rows are displayed
   */
  const changeDisplayedRows = useCallback(
    (amountOfRows, index, serverData = rows) => {
      //set the appropriate value to end looping at
      let targetEnd = index + amountOfRows;
      let loopEnd =
        serverData.length > targetEnd ? targetEnd : serverData.length;

      //create displayedRows from array of all rows
      let toBeDisplayed = [];
      for (let i = index; i < loopEnd; i++) {
        toBeDisplayed.push(serverData[i]);
      }

      setDisplayedRows(toBeDisplayed);
    },
    [rows]
  );

  /**
   * Retrieve and process data from server
   */
  useEffect(() => {
    let mounted = true;
    API.get(`/scorecard`).then(response => {
      let tableHeaders = [];

      const scorecardMaps = processServerData(response.data, tableHeaders);

      const scorecardRows = scorecardMaps.map(scorecardMap => {
        return createScorecardRow(scorecardMap, tableHeaders);
      });

      // create collection of columns with sortable functionality
      const sortableColumns = tableHeaders.map(header => {
        const sortableColumn = { title: header, transforms: [sortable] };
        return sortableColumn;
      });

      if (mounted) {
        setColumns(sortableColumns);
        setRows(scorecardRows);
        let loopEnd =
          scorecardRows.length > perPage ? perPage : scorecardRows.length;

        //create displayedRows from array of all rows
        let toBeDisplayed = [];
        for (let i = 0; i < loopEnd; i++) {
          toBeDisplayed.push(scorecardRows[i]);
        }

        setDisplayedRows(toBeDisplayed);
      }
    });
    return () => (mounted = false);
  }, [perPage]);

  /**
   *  Manipulation of data
   */
   // uses displayedRows as opposed to rows, since we only want to sort the rows currently displayed
  const onSort = (_event, index, direction) => {
    const sortedRows = displayedRows.sort((a, b) =>
      a[index] < b[index] ? -1 : a[index] > b[index] ? 1 : 0
    );
    setSortBy({
      index,
      direction
    });
    setDisplayedRows(
      direction === SortByDirection.asc ? sortedRows : sortedRows.reverse()
    );
  };

  const onPerPageSelect = newPerPage => {
    let currentIndex = page * perPage;
    changeDisplayedRows(newPerPage, currentIndex);
    setPerPage(newPerPage);
  };

  return (
    <React.Fragment>
      <Table
        aria-label="Sortable Table"
        sortBy={sortBy}
        onSort={onSort}
        cells={columns}
        rows={displayedRows}
      >
        <TableHeader />
        <TableBody />
      </Table>
      <PaginationControls
        rows={rows.length}
        page={page}
        setPage={setPage}
        perPage={perPage}
        onPerPageSelect={onPerPageSelect}
        changeDisplayedRows={changeDisplayedRows}
      />
    </React.Fragment>
  );
};

/**
 * Helper methods - don't directly use state
 */
const isObject = value => {
  return value && typeof value === "object" && value.constructor === Object;
};

//return value: Array of Maps, each map represents a scorecard
const processServerData = (object, columnHeaders) => {
  return object.map(scorecard => {
    let scorecardMap = new Map();
    getColumnHeadersAndScorecardMaps(scorecard, columnHeaders, scorecardMap);
    return scorecardMap;
  });
};

//recursive method to get all column headers into one array
const getColumnHeadersAndScorecardMaps = (
  scorecard,
  columnHeaders,
  scorecardMap
) => {
  Object.keys(scorecard).forEach(key => {
    if (!isObject(scorecard[key])) {
      // set up column headers
      if (!columnHeaders.includes(key)) {
        columnHeaders.push(key);
      }
      // populate scorecardMap
      scorecardMap.set(key, scorecard[key]);
    } else {
      // if value is an object, recursively call this function to flatten headers and scorecardMap
      getColumnHeadersAndScorecardMaps(
        scorecard[key],
        columnHeaders,
        scorecardMap
      );
    }
  });
};

const createScorecardRow = (scorecardMap, initialColumns) => {
  // initialize the row with 0 as each element
  let row = [];
  for (var i = 0; i < initialColumns.length; i++) {
    row.push("0");
  }

  // place the value of the scorecard map in the correct position, based on column headers
  for (const [key, value] of scorecardMap) {
    const position = initialColumns.indexOf(key);

    row.splice(position, 1, value.toString());
  }

  return row;
};
