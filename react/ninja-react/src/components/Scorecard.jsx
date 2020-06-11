import React, { useState, useEffect } from "react";
import {
  Table,
  TableHeader,
  TableBody,
  sortable,
  SortByDirection
} from "@patternfly/react-table";
import { PageSection } from "@patternfly/react-core";
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
  const [sortBy, setSortBy] = useState({});

  /**
   * Retrieve and process data from server
   */
  useEffect(() => {
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

      setColumns(sortableColumns);
      setRows(scorecardRows);
    });
  }, []);

  /**
   *  Manipulation of data
   */
  const onSort = (_event, index, direction) => {
    const sortedRows = rows.sort((a, b) =>
      a[index] < b[index] ? -1 : a[index] > b[index] ? 1 : 0
    );
    setSortBy({
      index,
      direction
    });
    setRows(
      direction === SortByDirection.asc ? sortedRows : sortedRows.reverse()
    );
  };

  return (
    <Table
      aria-label="Sortable Table"
      sortBy={sortBy}
      onSort={onSort}
      cells={columns}
      rows={rows}
    >
      <TableHeader />
      <TableBody />
    </Table>
  );
};

/**
 * Helper methods
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
