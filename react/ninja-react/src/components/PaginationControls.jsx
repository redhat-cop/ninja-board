import React, { useState } from "react";
import { Pagination, PaginationVariant } from "@patternfly/react-core";
import { PageSection } from "@patternfly/react-core";

const PaginationControls = props => {
  const [page, setPage] = useState(1);

  const onSetPage = (_event, pageNumber) => {
    setPage(pageNumber);
  };
  const onPerPageSelect = (_event, perPage) => {
    props.onPerPageSelect(perPage);
  };

  const onNextClick = _event => {
    let index = page * props.perPage;
    props.changeDisplayedRows(props.perPage, index);
    setPage(page + 1);
  };

  const onPreviousClick = _event => {
    let index = (page - 2) * props.perPage;
    props.changeDisplayedRows(props.perPage, index);
    setPage(page - 1);
  };

  const onFirstClick = _event => {
    props.changeDisplayedRows(props.perPage, 0);
    setPage(1);
  };

  const onLastClick = _event => {
    let amountOfRows = props.rows % props.perPage;
    let index = props.rows - amountOfRows;
    props.changeDisplayedRows(amountOfRows, index);
  };

  return (
    <Pagination
      itemCount={props.rows}
      widgetId="pagination-options-menu-bottom"
      perPage={props.perPage}
      page={page}
      variant={PaginationVariant.bottom}
      onSetPage={onSetPage}
      onPerPageSelect={onPerPageSelect}
      onNextClick={onNextClick}
      onPreviousClick={onPreviousClick}
      onFirstClick={onFirstClick}
      onLastClick={onLastClick}
    />
  );
};

export default PaginationControls;
