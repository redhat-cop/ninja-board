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
        routePath: "scorecards"
      },
      {
        id: 2,
        routeName: "Leaderboard",
        routePath: "leaderboard"
      },
      {
        id: 3,
        routeName: "Events",
        routePath: "events"
      },
      {
        id: 4,
        routeName: "Tasks",
        routePath: "tasks"
      },
      {
        id: 5,
        routeName: "Registration Form",
        routePath: "registration-form"
      },
      {
        id: 6,
        routeName: "Responses Spreadsheet",
        routePath: "responses-spreadsheets"
      },
      {
        id: 7,
        routeName: "Support - Trello Query",
        routePath: "support/trello-query"
      },
      {
        id: 8,
        routeName: "Support - Trello Reconciliation",
        routePath: "support/trello-reconciliation"
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
        routePath: "config"
      },
      {
        id: 2,
        routeName: "Database",
        routePath: "database"
      }
    ]
  }
];
