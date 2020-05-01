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
        linkName: "Scorecards",
        target:
          ""
      },
      {
        id: 2,
        linkName: "Leaderboard",
        target:
          ""
      },
      {
        id: 3,
        linkName: "Events",
        target:
          ""
      },
      {
        id: 4,
        linkName: "Tasks",
        target:
          ""
      },
      {
        id: 5,
        linkName: "Registration Form",
        target:
          ""
      },
      {
        id: 6,
        linkName: "Responses Spreadsheet",
        target:
          ""
      },
      {
        id: 7,
        linkName: "Support - Trello Query",
        target:
          ""
      },
      {
        id: 8,
        linkName: "Support - Trello Reconciliation",
        target:
          ""
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
        linkName: "Config (Here be dragons!)",
        target:
          ""
      },
      {
        id: 2,
        linkName: "Database (Here be dragons!)",
        target:
          ""
      }
    ]
  }
];
