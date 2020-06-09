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
import {
  tempScorecardData,
  scorecardExample
} from "../config/TempScorecardData";

/**
 * @author fostimus
 */
const ScorecardSection = () => {
  return (
    <PageSection>
      <SortableTable />
    </PageSection>
  );
};

export default ScorecardSection;

export const SortableTable = props => {
  /**
   * State Initialization
   */
  let initialColumns = [];

  getColumnHeadersFromObject(scorecardExample, initialColumns);

  //TODO: replace tempScorecardData with serverData when "live" data becomes available
  const initialRows = tempScorecardData.map(scorecard => {
    let row = [];
    for (var i = 0; i < initialColumns.length; i++) {
      row.push("0");
    }
    createObjectRow(scorecard, row, initialColumns);
    return row;
  });

  // create collection of columns with sortable functionality
  const sortableColumns = initialColumns.map(header => {
    const sortableColumn = { title: header, transforms: [sortable] };
    return sortableColumn;
  });

  const [serverData, setServerData] = useState({});
  const [columns, setColumns] = useState(sortableColumns);
  const [rows, setRows] = useState(initialRows);
  const [sortBy, setSortBy] = useState({});

  /**
   *  Methods that faciliate retrieval and manipulation of data
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

  useEffect(() => {
    //TODO: fill this in and integrate when there is data coming from backend
    API.get(`/scorecard`).then(res => {});
  }, [serverData]);

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

const isObject = value => {
  return value && typeof value === "object" && value.constructor === Object;
};

//recursive method to get all column headers into one array
// TODO: potentially move logic of setting up rows into this function, save some performance
const getColumnHeadersFromObject = (object, columnHeaders) => {
  Object.keys(object).forEach(key => {
    // set up column headers
    if (!columnHeaders.includes(key)) {
      if (!isObject(object[key])) {
        columnHeaders.push(key);
      } else {
        getColumnHeadersFromObject(object[key], columnHeaders);
      }
    }
  });
};

// recursive method to set up a row in the same order as the table headers
const createObjectRow = (object, row, initialColumns) => {
  Object.keys(object).forEach(key => {
    const position = initialColumns.indexOf(key);
    if (position === -1) {
      createObjectRow(object[key], row, initialColumns);
    } else {
      // enter the value directly into the correct position, remove a "0" from the array
      row.splice(position, 1, object[key].toString());
    }
  });
};
