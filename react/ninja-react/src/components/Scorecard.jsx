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
  // treat each object liket a dictionary, get the "keys" and use them as the column names
  // after going through entire payload, sort columns alphabetically
  //
  // use the values as the row data, each row being ONE scorecard. populate each position in the row based on the key from JSON object.

  let initialColumns = [];

  getColumnHeadersFromObject(scorecardExample, initialColumns);

  const createObjectRow = (object, row) => {
    Object.keys(object).forEach(key => {
      const position = initialColumns.indexOf(key);
      if (position == -1) {
        createObjectRow(object[key], row);
      } else {
        row.splice(position, 1, object[key]);
      }
    });
  };

  const initialRows = tempScorecardData.map(scorecard => {
    let row = [];
    for (var i = 0; i < initialColumns.length; i++) {
      row.push(0)
    }
    createObjectRow(scorecard, row);
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
    //TODO: ensure this actually syncs to the backend API
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
const getColumnHeadersFromObject = (object, columnHeaders, rows) => {
  Object.keys(object).forEach(key => {
    // set up column headers
    if (!columnHeaders.includes(key)) {
      if (!isObject(object[key])) {
        columnHeaders.push(key);
      } else {
        getColumnHeadersFromObject(object[key], columnHeaders);
      }
    }

    let row = [];
    row.push();
  });
};
