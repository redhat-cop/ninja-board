//update the data here to make any content updates to the NavBar.
//top level objects, currently only 2, are for the expandable portion of the NavBar.
//the lower level objects under the "links" key are the actual links within that expandable.
export const navBarLinks = [
  {
    id: 1,
    expandableName: "Ninja Program",
    groupId: "ninja",
    links: [
      {
        id: 1,
        routeName: "Scorecards",
        routePath: "scorecards",
        target:
          "https://community-ninja-board-ninja-board.int.open.paas.redhat.com/community-ninja-board/scorecards.jsp"
      },
      {
        id: 2,
        routeName: "Leaderboard",
        routePath: "leaderboard",
        target:
          "https://community-ninja-board-ninja-board.int.open.paas.redhat.com/community-ninja-board/leaderboard.jsp"
      },
      {
        id: 3,
        routeName: "Events",
        routePath: "events",
        target:
          "https://community-ninja-board-ninja-board.int.open.paas.redhat.com/community-ninja-board/events.jsp"
      },
      {
        id: 4,
        routeName: "Tasks",
        routePath: "tasks",
        target:
          "https://community-ninja-board-ninja-board.int.open.paas.redhat.com/community-ninja-board/tasks.jsp"
      },
      {
        id: 5,
        routeName: "Registration Form",
        routePath: "registration-form",
        target:
          "https://docs.google.com/a/redhat.com/forms/d/e/1FAIpQLSdWGcCks2zKKnVoZFQz3CieLQDc1lsSex_Knwh_-eyRm0ZQTg/viewform"
      },
      {
        id: 6,
        routeName: "Responses Spreadsheet",
        routePath: "responses-spreadsheets",
        target:
          "https://docs.google.com/spreadsheets/d/1E91hT_ZpySyvhnANxqZ7hcBSM2EEd9TqfQF-cavB8hQ"
      },
      {
        id: 7,
        routeName: "Support - Trello Query",
        routePath: "support/trello-query",
        target:
          "https://community-ninja-board-ninja-board.int.open.paas.redhat.com/community-ninja-board/support-trello-card.jsp"
      },
      {
        id: 8,
        routeName: "Support - Trello Reconciliation",
        routePath: "support/trello-reconciliation",
        target:
          "https://community-ninja-board-ninja-board.int.open.paas.redhat.com/community-ninja-board/support-trello-all.jsp"
      }
    ]
  },
  {
    id: 2,
    expandableName: "Admin",
    groupId: "admin",
    links: [
      {
        id: 1,
        routeName: "Config",
        routePath: "config",
        target:
          "https://community-ninja-board-ninja-board.int.open.paas.redhat.com/community-ninja-board/config.jsp"
      },
      {
        id: 2,
        routeName: "Database",
        routePath: "database",
        target:
          "https://community-ninja-board-ninja-board.int.open.paas.redhat.com/community-ninja-board/database.jsp"
      }
    ]
  }
];
