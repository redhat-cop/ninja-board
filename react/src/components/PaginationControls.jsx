import React from "react";
import { Pagination, PaginationVariant } from "@patternfly/react-core";

/**
 * @author fostimus
 */
const PaginationControls = props => {
  const onSetPage = (_event, pageNumber) => {
    props.setPage(pageNumber);
  };
  const onPerPageSelect = (_event, perPage) => {
    props.onPerPageSelect(perPage);
  };

  const onNextClick = _event => {
    let index = props.page * props.perPage;
    props.changeDisplayedRows(props.perPage, index);
    props.setPage(props.page + 1);
  };

  const onPreviousClick = _event => {
    let index = (props.page - 2) * props.perPage;
    props.changeDisplayedRows(props.perPage, index);
    props.setPage(props.page - 1);
  };

  const onFirstClick = _event => {
    props.changeDisplayedRows(props.perPage, 0);
    props.setPage(1);
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
      page={props.page}
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
