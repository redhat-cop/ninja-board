import ReactDOM from 'react-dom';
import "@patternfly/react-core/dist/styles/base.css";

import React, { useState, useEffect } from 'react';
import TagIcon from '@patternfly/react-icons/dist/esm/icons/tag-icon';

import { Icon, Pagination, PaginationVariant, ToggleGroup, ToggleGroupItem, ToggleGroupItemProps, Truncate } from '@patternfly/react-core';
import { TableComposable, Caption, Thead, Tr, Th, Tbody, Td, ThProps } from '@patternfly/react-table';
import axios from 'axios';
import { Score } from 'src/interfaces/Score';




export default function Scorecards(){

    const [scores, setScores] = useState<Score[]>()
    const [activeSortIndex, setActiveSortIndex] = React.useState<number | undefined>(undefined);
    const [itemCount, setItemCount] = React.useState<number | 0>(0);
    const [activeSortDirection, setActiveSortDirection] = React.useState<'asc' | 'desc' | undefined>(undefined);
    const columnNames = {
        name: 'Name',
        id: 'Id',
        level: 'Ninja Belt',
        pointsNextLvl: 'Points To Next Level',
        servicesSupportClosed: 'Services Support Closed Issues',
        servicesSupportRvwd: 'ServicesSupport Reviewed Merge Request',
        trelloCardsClosed: 'Trello Cards Closed',
        ThoughtLeadershipClosed: 'ThoughtLeadership Cards Closed',
        gitReviewedPR: 'Github Reviewed Pull Requests',
        gitClosesIss: 'Github Closed Issues',
        total: 'Total',
        servicesMergeRqts: 'ServicesSupport Merge Requests',
        gitPR: 'Github Pull Requests',
        gitMR: 'Gitlab Merge Requests',
        servicesSupportCards: 'ServicesSupport Cards Closed',
        gitlabReviewedMR: 'Gitlab Reviewed Merge Requests'
    };

    const [page, setPage] = React.useState(1);
    const [perPage, setPerPage] = React.useState(100);

    useEffect(() => {
        axios( process.env.REACT_APP_API +'/api/scorecards/')
        .then((response) => {
            setScores(response.data.data);
            setItemCount(response.data.data.length);
            console.log(scores);
        })
        /* Using Fetch
        fetch("https://jsonplaceholder.typicode.com/users")
          .then((response) => response.json())
          .then((response) => {
            setContacts(response);
            setError(null);
          })
          .catch(setError);
        */
      }, []);
      
      const getSortableRowValues = (sco: Score): (string | number)[] => {
        const { id, name, level} = sco;
        return [id, name, level];
      };
    
      let sortedRepositories = scores;
      if (activeSortIndex !== undefined) {
        sortedRepositories = scores?.sort((a, b) => {
          const aValue = getSortableRowValues(a)[activeSortIndex];
          const bValue = getSortableRowValues(b)[activeSortIndex];
          if (typeof aValue === 'number') {
            // Numeric sort
            if (activeSortDirection === 'asc') {
              return (aValue as number) - (bValue as number);
            }
            return (bValue as number) - (aValue as number);
          } else {
            // String sort
            if (activeSortDirection === 'asc') {
              return (aValue as string).localeCompare(bValue as string);
            }
            return (bValue as string).localeCompare(aValue as string);
          }
        });
      }
    

    const getSortParams = (columnIndex: number): ThProps['sort'] => ({
    sortBy: {
        index: activeSortIndex ,
        direction: activeSortDirection
    },
    onSort: (_event, index, direction) => {
        setActiveSortIndex(index);
        setActiveSortDirection(direction);
    },
    columnIndex
    });

    const onSetPage = (_event: React.MouseEvent | React.KeyboardEvent | MouseEvent, newPage: number) => {
        setPage(newPage);
    };

    const onPerPageSelect = (
        _event: React.MouseEvent | React.KeyboardEvent | MouseEvent,
        newPerPage: number,
        newPage: number
    ) => {
        setPerPage(newPerPage);
        setPage(newPage);
    };

    return (
            <div>
                <Pagination
                itemCount={itemCount}
                perPage={perPage}
                page={page}
                onSetPage={onSetPage}
                widgetId="pagination-options-menu-top"
                onPerPageSelect={onPerPageSelect}
                >
                <TableComposable aria-label="Sortable table custom toolbar">
                <Thead>
                <Tr>
                    <Th/>
                    <Th sort={getSortParams(1)} modifier="wrap">{columnNames.name}</Th>
                    <Th >{columnNames.total}</Th>
                    <Th modifier="wrap">{columnNames.level}</Th>
                    <Th modifier="wrap">{columnNames.pointsNextLvl}</Th>
                    <Th modifier="wrap">{columnNames.gitReviewedPR}</Th>
                    <Th modifier="wrap">{columnNames.gitClosesIss}</Th>
                    <Th modifier="wrap">{columnNames.servicesMergeRqts}</Th>
                    <Th modifier="wrap">{columnNames.servicesSupportClosed}</Th>
                    <Th modifier="wrap">{columnNames.servicesSupportRvwd}</Th>
                    <Th modifier="wrap">{columnNames.gitPR}</Th>
                    <Th modifier="wrap">{columnNames.gitMR}</Th>
                    <Th modifier="wrap">{columnNames.trelloCardsClosed}</Th>
                    <Th modifier="wrap">{columnNames.ThoughtLeadershipClosed}</Th>
                    <Th modifier="wrap">{columnNames.servicesSupportCards}</Th>
                    <Th modifier="wrap">{columnNames.gitlabReviewedMR}</Th>

                </Tr>
                </Thead>
                <Tbody>
                {scores?.map((sco, rowIndex) => (
                    <Tr key={rowIndex}>
                        <Td/>
                        <Td dataLabel={columnNames.name}>
                            <Truncate content={sco.name.substring(0,30)}/>
                        </Td>
                        <Td dataLabel={columnNames.total}>{sco.total}</Td>
                        <Td dataLabel={columnNames.level} alt={sco.level}>
                            <Icon>
                                <TagIcon color={sco.level=='ZERO'?'none':sco.level=='GREY'?'grey':sco.level='BLUE'?'blue':sco.level='RED'?'red':'none'}/>{sco.level.toLowerCase()}
                            </Icon>
                        </Td>
                        <Td dataLabel={columnNames.pointsNextLvl}>{sco.pointsToNextLevel}</Td>
                        <Td dataLabel={columnNames["Github Reviewed Pull Requests"]}>{sco["Github Reviewed Pull Requests"]}</Td>
                        <Td dataLabel={columnNames["Github Closed Issues"]}>{sco["Github Closed Issues"]}</Td>
                        <Td dataLabel={columnNames["ServicesSupport Merge Requests"]}>{sco["ServicesSupport Merge Requests"]}</Td>
                        <Td dataLabel={columnNames["ServicesSupport Closed Issues"]}>{sco["ServicesSupport Closed Issues"]}</Td>
                        <Td dataLabel={columnNames["ServicesSupport Reviewed Merge Requests"]}>{sco["ServicesSupport Reviewed Merge Requests"]}</Td>
                        <Td dataLabel={columnNames["Github Pull Requests"]}>{sco["Github Pull Requests"]}</Td>
                        <Td dataLabel={columnNames["Gitlab Merge Requests"]}>{sco["Gitlab Merge Requests"]}</Td>
                        <Td dataLabel={columnNames["Trello Cards Closed"]}>{sco["Trello Cards Closed"]}</Td>
                        <Td dataLabel={columnNames["ThoughtLeadership Cards Closed"]}>{sco["ThoughtLeadership Cards Closed"]}</Td>
                        <Td dataLabel={columnNames["ServicesSupport Cards Closed"]}>{sco["ServicesSupport Cards Closed"]}</Td>
                        <Td dataLabel={columnNames["Gitlab Reviewed Merge Requests"]}>{sco["Gitlab Reviewed Merge Requests"]}</Td>

                    </Tr>
                ))}
                </Tbody>
                </TableComposable>
                </Pagination>
            </div>
    )

}