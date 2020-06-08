import React from "react";
import {
  Table,
  TableHeader,
  TableBody,
  sortable,
  SortByDirection
} from "@patternfly/react-table";
import { PageSection } from "@patternfly/react-core";
import API from "../config/ServerUrls";
import Scorecard from "giveback_ninja";

/**
 * @author fostimus
 */
export default function ScorecardSection() {
  return (
    <PageSection>
      <SortableTable />
    </PageSection>
  );
}

class SortableTable extends React.Component {


  constructor(props) {
    super(props);
    //TODO: populate rows and columns dynamically based on serverData
    this.state = {
      serverData: {},
      columns: [
        { title: "Repositories", transforms: [sortable] },
        "Branches",
        { title: "Pull requests", transforms: [sortable] },
        "Workspaces",
        "Last Commit"
      ],
      rows: [
        ["one", "two", "a", "four", "five"],
        ["a", "two", "k", "four", "five"],
        ["p", "two", "b", "four", "five"]
      ],
      sortBy: {}
    };
    this.onSort = this.onSort.bind(this);

    this.getScorecardData = event => {
      event.preventDefault();

      //TODO: ensure this actually syncs to the backend API
      API.get(`/scorecard`).then(res => {
        console.log(res);
        console.log(res.data);
        this.setState({
          serverData: res.data
        });
      });
    };
  }

  onSort(_event, index, direction) {
    const sortedRows = this.state.rows.sort((a, b) =>
      a[index] < b[index] ? -1 : a[index] > b[index] ? 1 : 0
    );
    this.setState({
      sortBy: {
        index,
        direction
      },
      rows:
        direction === SortByDirection.asc ? sortedRows : sortedRows.reverse()
    });
  }

  render() {
    const { columns, rows, sortBy } = this.state;

    return (
      <Table
        aria-label="Sortable Table"
        sortBy={sortBy}
        onSort={this.onSort}
        cells={columns}
        rows={rows}
      >
        <TableHeader />
        <TableBody />
      </Table>
    );
  }
}
